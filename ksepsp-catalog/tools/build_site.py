#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Сборка сайта-каталога KSEPSP: сравнение 9.5.1 и 9.6.2 + превью всех моделей.

Что делает:
  1. распаковывает оба архива KSEPSP-*.zip во временную папку build/;
  2. парсит все переименования (assets/minecraft/items/*.json) обеих версий;
  3. считает diff (добавлено / изменено / удалено: переименования, модели, текстуры, броня);
  4. рендерит PNG-превью для каждой пары (модель, tint) - софтверный рендер;
  5. выгружает геометрию моделей (для интерактивного 3D-просмотра в браузере) и текстуры;
  6. пишет site/data/*.json.

Запуск:  python3 tools/build_site.py [--skip-render] [--skip-geom]
"""

from __future__ import annotations

import argparse
import difflib
import hashlib
import json
import os
import shutil
import sys
import time
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))

from pack_lib import (  # noqa: E402
    CATEGORY_LABELS,
    ModelResolver,
    extract_zip,
    iter_armor,
    iter_cases,
    mcmeta_info,
    pack_stats,
    read_json,
)

CATEGORY_ORDER = ["weapons", "tools", "hats", "food", "potions", "furniture", "other", "item", "misc"]

BASE_ITEM_RU = {
    "apple": "Яблоко",
    "armor_stand": "Стойка для брони",
    "arrow": "Стрела",
    "beetroot": "Свёкла",
    "bow": "Лук",
    "bowl": "Миска",
    "bread": "Хлеб",
    "brush": "Кисть",
    "bundle": "Мешок",
    "carrot": "Морковь",
    "carved_pumpkin": "Вырезанная тыква",
    "cooked_beef": "Стейк",
    "cooked_chicken": "Жареная курица",
    "cooked_porkchop": "Жареная свинина",
    "cooked_salmon": "Жареный лосось",
    "cookie": "Печенье",
    "crossbow": "Арбалет",
    "diamond_axe": "Алмазный топор",
    "diamond_hoe": "Алмазная мотыга",
    "diamond_pickaxe": "Алмазная кирка",
    "diamond_shovel": "Алмазная лопата",
    "diamond_sword": "Алмазный меч",
    "dried_kelp": "Сушёная ламинария",
    "emerald": "Изумруд",
    "filled_map": "Заполненная карта",
    "firework_rocket": "Фейерверк",
    "fishing_rod": "Удочка",
    "flint_and_steel": "Огниво",
    "glass_bottle": "Стеклянная бутылочка",
    "goat_horn": "Козий рог",
    "golden_axe": "Золотой топор",
    "golden_hoe": "Золотая мотыга",
    "golden_pickaxe": "Золотая кирка",
    "golden_shovel": "Золотая лопата",
    "golden_sword": "Золотой меч",
    "honey_bottle": "Бутылочка мёда",
    "iron_axe": "Железный топор",
    "iron_hoe": "Железная мотыга",
    "iron_ingot": "Железный слиток",
    "iron_pickaxe": "Железная кирка",
    "iron_shovel": "Железная лопата",
    "iron_sword": "Железный меч",
    "item_frame": "Рамка",
    "leather_horse_armor": "Кожаная конская броня",
    "lingering_potion": "Туманное зелье",
    "mace": "Булава",
    "map": "Карта",
    "melon_slice": "Ломтик арбуза",
    "milk_bucket": "Ведро молока",
    "minecart": "Вагонетка",
    "mushroom_stew": "Тушёные грибы",
    "name_tag": "Табличка с именем",
    "nether_wart": "Адский нарост",
    "netherite_axe": "Незеритовый топор",
    "netherite_hoe": "Незеритовая мотыга",
    "netherite_pickaxe": "Незеритовая кирка",
    "netherite_shovel": "Незеритовая лопата",
    "netherite_sword": "Незеритовый меч",
    "painting": "Картина",
    "poisonous_potato": "Ядовитый картофель",
    "potato": "Картофель",
    "potion": "Зелье",
    "pumpkin_pie": "Тыквенный пирог",
    "rotten_flesh": "Гнилая плоть",
    "shears": "Ножницы",
    "shield": "Щит",
    "shulker_box": "Шалкеровый ящик",
    "snowball": "Снежок",
    "spectral_arrow": "Спектральная стрела",
    "spider_eye": "Паучий глаз",
    "splash_potion": "Метательное зелье",
    "spyglass": "Подзорная труба",
    "stick": "Палка",
    "stone_axe": "Каменный топор",
    "stone_hoe": "Каменная мотыга",
    "stone_pickaxe": "Каменная кирка",
    "stone_shovel": "Каменная лопата",
    "stone_sword": "Каменный меч",
    "suspicious_stew": "Подозрительное рагу",
    "sweet_berries": "Сладкие ягоды",
    "totem_of_undying": "Тотем бессмертия",
    "trident": "Трезубец",
    "wooden_axe": "Деревянный топор",
    "wooden_hoe": "Деревянная мотыга",
    "wooden_pickaxe": "Деревянная кирка",
    "wooden_shovel": "Деревянная лопата",
    "wooden_sword": "Деревянный меч",
    "writable_book": "Книга и перо",
    "written_book": "Написанная книга",
}
for _i in (5, 11, 13, "blocks", "cat", "chirp", "creator", "creator_music_box", "far", "mall",
           "mellohi", "otherside", "pigstep", "precipice", "relic", "stal", "strad", "wait", "ward"):
    BASE_ITEM_RU[f"music_disc_{_i}"] = f"Пластинка ({_i})"


# ---------------------------------------------------------------------------
# утилиты
# ---------------------------------------------------------------------------


def tint_key(tint) -> str:
    if not tint:
        return ""
    if tint.get("type") == "dye":
        return "t%08x" % (int(tint.get("default", 0)) & 0xFFFFFFFF)
    if tint.get("type") == "constant":
        return "t%08x" % (int(tint.get("value", 0)) & 0xFFFFFFFF)
    return "t?"


def slug(model_id: str, tint=None) -> str:
    s = model_id.replace(":", "__").replace("/", "__")
    tk = tint_key(tint)
    return f"{s}--{tk}" if tk else s


def names_key(names, base) -> str:
    return base + "|" + "|".join(sorted(n.strip().casefold() for n in names))


def hash_file(p: Path) -> str:
    h = hashlib.md5()
    with open(p, "rb") as f:
        for chunk in iter(lambda: f.read(1 << 16), b""):
            h.update(chunk)
    return h.hexdigest()


# ---------------------------------------------------------------------------
# рендер (в отдельном процессе)
# ---------------------------------------------------------------------------

_W = {}


def _init_worker(pack_root: str, extra_roots: list[str] | None = None):
    from render_lib import _TexCache
    from pack_lib import ModelResolver

    _W["resolver"] = ModelResolver(pack_root, extra_roots)
    _W["cache"] = _TexCache()


def _render_job(job):
    from render_lib import render_model

    model_id, tint, out_path = job
    try:
        img = render_model(
            _W["resolver"], model_id, tint, size=128, ssaa=3, tex_cache=_W["cache"]
        )
        img.save(out_path, optimize=True)
        return (out_path, None)
    except Exception as e:  # noqa: BLE001
        return (out_path, f"{model_id}: {e}")


# ---------------------------------------------------------------------------
# основная сборка
# ---------------------------------------------------------------------------


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--skip-render", action="store_true")
    ap.add_argument("--skip-geom", action="store_true")
    ap.add_argument("--workers", type=int, default=max(1, (os.cpu_count() or 2)))
    args = ap.parse_args()

    repo = Path(__file__).resolve().parents[2]
    root = repo / "ksepsp-catalog"
    build = root / "build"
    site = root / "site"
    data = site / "data"
    img_dir = data / "img"
    geom_dir = data / "geom"
    tex_dir = data / "tex"
    for d in (build, data, img_dir, geom_dir, tex_dir):
        d.mkdir(parents=True, exist_ok=True)

    versions = [
        ("9.5.1", repo / "KSEPSP-9-5-1.zip"),
        ("9.6.2", repo / "KSEPSP-9-6-2.zip"),
    ]

    t0 = time.time()
    info = {}
    parsed = {}
    resolvers = {}
    armor = {}

    for vid, zip_path in versions:
        pack_root = build / f"ksepsp-{vid}"
        if not (pack_root / "pack.mcmeta").exists() or not (pack_root / "assets").exists():
            print(f"[extract] {zip_path.name} -> {pack_root}")
            if pack_root.exists():
                shutil.rmtree(pack_root)
            extract_zip(str(zip_path), str(pack_root))
        else:
            print(f"[extract] {zip_path.name}: уже распакован")
        r = ModelResolver(str(pack_root))
        resolvers[vid] = r
        cases = iter_cases(str(pack_root), r)
        parsed[vid] = {"root": pack_root, "cases": cases}
        armor[vid] = iter_armor(str(pack_root))
        info[vid] = {
            "id": vid,
            "zip": zip_path.name,
            "zip_size": zip_path.stat().st_size,
            "mcmeta": mcmeta_info(str(pack_root)),
            "stats": pack_stats(str(pack_root)),
            "counts": {
                "cases": len(cases),
                "named": sum(1 for c in cases if c["names"]),
                "armor": len(armor[vid]),
            },
        }
        print(
            f"[parse] {vid}: переименований {len(cases)} "
            f"(с именами {info[vid]['counts']['named']}), брони {len(armor[vid])}"
        )

    v_old, v_new = versions[0][0], versions[1][0]
    resolvers[v_new].roots = [str(parsed[v_new]["root"]), str(parsed[v_old]["root"])]
    resolvers[v_new]._cache.clear()
    resolvers[v_new]._chain_cache.clear()

    # --- сопоставление переименований между версиями -------------------------
    def index(vid):
        idx = {}
        for c in parsed[vid]["cases"]:
            if not c["names"]:
                continue
            idx[names_key(c["names"], c["base"])] = c
        return idx

    idx_old, idx_new = index(v_old), index(v_new)

    def nset(c):
        return {n.strip().casefold() for n in c["names"]}

    # 1) точные совпадения (предмет + полный набор написаний)
    pairs = []            # (old, new, label_changed)
    paired_new = set()
    for k, c in idx_new.items():
        o = idx_old.get(k)
        if o is not None:
            pairs.append((o, c, False))
            paired_new.add(k)

    # 2) остальные сопоставляем по предмету и хотя бы одному совпадающему написанию
    #    (в 9.6.2 часть названий просто переписали: Helmet T-45 -> T-45 Helmet)
    left_old = {k: c for k, c in idx_old.items() if k not in {names_key(o["names"], o["base"]) for o, _c, _f in pairs}}
    left_new = {k: c for k, c in idx_new.items() if k not in paired_new}
    by_base_new: dict[str, list] = {}
    for k, c in left_new.items():
        by_base_new.setdefault(c["base"], []).append((k, c))
    used_new_keys = set()
    for k, o in list(left_old.items()):
        best, best_score, best_key = None, 0, None
        for kn, c in by_base_new.get(o["base"], []):
            if kn in used_new_keys:
                continue
            inter = len(nset(o) & nset(c))
            alln = len(nset(o) | nset(c))
            if not inter:
                continue
            score = inter / alln
            if score > best_score:
                best, best_score, best_key = c, score, kn
        if best is not None:
            pairs.append((o, best, True))
            used_new_keys.add(best_key)
            left_old.pop(k)
    for k in used_new_keys:
        left_new.pop(k, None)

    added = list(left_new.values())
    removed = list(left_old.values())
    changed = [(o, c) for o, c, _lc in pairs
               if o["model"] != c["model"] or tint_key(o.get("tints")) != tint_key(c.get("tints"))]
    renamed = [(o, c) for o, c, lc in pairs
               if lc and o["model"] == c["model"] and tint_key(o.get("tints")) == tint_key(c.get("tints"))]
    print(
        f"[diff] новых переименований: {len(added)}, изменённых: {len(changed)}, "
        f"с новым написанием: {len(renamed)}, удалённых: {len(removed)}"
    )

    # --- статистика по файлам ------------------------------------------------
    def snapshot(pack_root: Path):
        snap = {}
        for base in ("assets",):
            for p in (pack_root / base).rglob("*"):
                if p.is_file():
                    rel = str(p.relative_to(pack_root)).replace(os.sep, "/")
                    snap[rel] = (p.stat().st_size, hash_file(p))
        return snap

    snap_old, snap_new = snapshot(parsed[v_old]["root"]), snapshot(parsed[v_new]["root"])

    def file_diff(substr: str = ""):
        out = {"added": [], "removed": [], "changed": []}
        for rel, (size, h) in snap_new.items():
            if substr and substr not in rel:
                continue
            if rel not in snap_old:
                out["added"].append({"path": rel, "size": size})
            elif snap_old[rel][1] != h:
                out["changed"].append({"path": rel, "size": size})
        for rel, (size, _h) in snap_old.items():
            if substr and substr not in rel:
                continue
            if rel not in snap_new:
                out["removed"].append({"path": rel, "size": size})
        for k in out:
            out[k].sort(key=lambda x: x["path"])
        return out

    files_diff = {
        "models": file_diff("models/"),
        "textures": file_diff("textures/"),
        "items": file_diff("/items/"),
        "swappers": file_diff("/rpt/"),
        "cem": file_diff("optifine/cem/"),
        "other": file_diff(""),
    }

    # --- рендер превью -------------------------------------------------------
    # набор (модель, tint) для обеих версий
    variants: dict[str, tuple[str, dict | None]] = {}
    for vid in (v_old, v_new):
        for c in parsed[vid]["cases"]:
            if not c["names"]:
                continue
            allm = [(c["model"], c.get("tints"))]
            for s in c["states"]:
                if s.get("model"):
                    allm.append((s["model"], s.get("tints") or c.get("tints")))
            for m, t in allm:
                if not m:
                    continue
                variants[slug(m, t)] = (m, t)
    print(f"[render] уникальных пар (модель, tint): {len(variants)}")

    if not args.skip_render:
        todo = []
        for sl, (m, t) in variants.items():
            out = img_dir / f"{sl}.png"
            if not out.exists():
                todo.append((m, t, str(out)))
        print(f"[render] к рендеру: {len(todo)} (уже готово: {len(variants) - len(todo)})")
        t1 = time.time()
        done = 0
        errors = []
        if todo:
            with ProcessPoolExecutor(
                max_workers=args.workers,
                initializer=_init_worker,
                initargs=(str(parsed[v_new]["root"]), [str(parsed[v_old]["root"])]),
            ) as ex:
                for path, err in ex.map(_render_job, todo, chunksize=8):
                    done += 1
                    if err:
                        errors.append(err)
                    if done % 200 == 0:
                        el = time.time() - t1
                        print(
                            f"   {done}/{len(todo)}  {el:.0f}s  ~{done / max(el, 0.01):.1f} из/с",
                            flush=True,
                        )
        print(f"[render] готово за {time.time() - t1:.0f}s, ошибок: {len(errors)}")
        for e in errors[:10]:
            print("   !", e)

    # --- текстуры и геометрия для 3D-просмотра -------------------------------
    tex_map: dict[str, str] = {}  # ref -> имя файла в data/tex
    tex_hash: dict[str, str] = {}

    def tex_out_path(ref: str) -> str | None:
        """ref вида 'ksepsp:item/foo' -> имя файла в data/tex (копирует при необходимости)."""
        path_v = None
        for vid in (v_new, v_old):
            p = resolvers[vid].texture_png(ref)
            if p:
                path_v = Path(p)
                break
        if path_v is None:
            return None
        if ref in tex_map:
            return tex_map[ref]
        h = hash_file(path_v)
        name = tex_hash.get(h)
        if name is None:
            name = ref.replace(":", "__").replace("/", "__") + ".png"
            from PIL import Image

            with Image.open(path_v) as im:
                im.convert("RGBA").save(tex_dir / name, optimize=True)
            tex_hash[h] = name
        tex_map[ref] = name
        return name

    geom_index: dict[str, str] = {}  # ключ "vid|model" -> id в шардах
    geom_by_hash: dict[str, str] = {}
    geom_items: dict[str, list] = {}

    if not args.skip_geom:
        from render_lib import build_quads

        def geom_id(model_id: str, vid: str) -> str | None:
            key = f"{vid}|{model_id}"
            if key in geom_index:
                return geom_index[key]
            r = resolvers[vid]
            chain = r.chain(model_id)
            geom_hash_src = json.dumps(
                [
                    (
                        mid,
                        {
                            "elements": d.get("elements"),
                            "textures": d.get("textures"),
                            "parent": d.get("parent"),
                        },
                    )
                    for mid, d in chain
                ],
                sort_keys=True,
                default=str,
            )
            h = hashlib.md5(geom_hash_src.encode()).hexdigest()
            if h in geom_by_hash:
                geom_index[key] = geom_by_hash[h]
                return geom_by_hash[h]
            gid = "g%04d" % (len(geom_by_hash) + 1)
            try:
                quads, _ = build_quads(r, model_id, None, texture_png=r.texture_png)
            except Exception:
                quads = []
            quads_out = []
            for q in quads:
                tex_ref = tex_name_by_path.get(os.path.abspath(q["tex"]))
                if tex_ref is None:
                    continue
                quads_out.append(
                    {
                        "v": [[round(c, 4) for c in v] for v in q["verts"]],
                        "u": [[round(c, 3) for c in uv] for uv in q["uvs"]],
                        "t": tex_ref,
                        "s": round(float(q["shade"]), 3),
                        "tt": bool(q.get("tintable")),
                        "d": bool(q["double"]),
                    }
                )
            if not quads_out:
                return None
            geom_items[gid] = quads_out
            geom_by_hash[h] = gid
            geom_index[key] = gid
            return gid

        # карта путь-текстуры -> имя файла
        tex_name_by_path: dict[str, str] = {}
        # заранее скопируем все текстуры, на которые ссылаются модели
        refs = set()
        for vid in (v_old, v_new):
            r = resolvers[vid]
            for sl, (m, _t) in variants.items():
                try:
                    tex = r.textures(m)
                except Exception:
                    continue
                for v in tex.values():
                    ref = r.resolve_texture_ref(tex, v)
                    if ref:
                        refs.add(ref)
        for ref in sorted(refs):
            name = tex_out_path(ref)
            if not name:
                continue
            for vid in (v_old, v_new):
                for root in resolvers[vid].roots:
                    ns, _, path = ref.partition(":")
                    if not path:
                        ns, path = "minecraft", ns
                    p = os.path.join(root, "assets", ns, "textures", path + ".png")
                    if os.path.exists(p):
                        tex_name_by_path[os.path.abspath(p)] = f"tex/{name}"
        print(f"[3d] текстур выгружено: {len(tex_map)}")

    # --- запись JSON ---------------------------------------------------------
    def case_json(c, vid, status=None, other=None):
        """Компактная запись одного переименования для сайта."""
        main_slug = slug(c["model"], c.get("tints"))
        # состояния (натянутый лук, вид от 1-го лица и т.п.) группируем по картинке
        by_slug: dict[str, dict] = {}
        for s in c["states"]:
            if not s.get("model"):
                continue
            sl = slug(s["model"], s.get("tints") or c.get("tints"))
            if sl == main_slug:
                continue
            cond = (s.get("cond") or "").strip()
            if s.get("fb") and str(s["model"]).startswith("minecraft:"):
                cond = "ванильная модель" + ((" " + cond) if cond else "")
            e = by_slug.setdefault(sl, {"m": s["model"], "img": "img/" + sl + ".png", "conds": []})
            if cond and cond not in e["conds"]:
                e["conds"].append(cond)
        states = [
            {"cond": ", ".join(v["conds"]), "m": v["m"], "img": v["img"]} for v in by_slug.values()
        ]
        states.sort(key=lambda x: (x["cond"], x["m"]))

        out = {
            "b": c["base"],
            "n": c["names"],
            "c": c["category"],
            "g": c["group"] or "",
            "m": c["model"],
            "img": "img/" + main_slug + ".png",
            "era": c["era"] or "",
            "k": names_key(c["names"], c["base"]),
        }
        t = c.get("tints")
        if t:
            out["t"] = t
        if states:
            out["st"] = states[:24]
        if c.get("oversized"):
            out["big"] = 1
        # какие текстуры использует модель + нет ли битых
        r = resolvers[vid]
        refs = []
        miss = []
        for m in [c["model"]] + [s2["model"] for s2 in c["states"] if s2.get("model")]:
            try:
                tex = r.textures(m)
            except Exception:
                continue
            for v in tex.values():
                ref = r.resolve_texture_ref(tex, v)
                if not ref:
                    continue
                if ref not in refs:
                    refs.append(ref)
                if not r.texture_png(ref) and ref not in miss:
                    miss.append(ref)
        if refs:
            out["tx"] = refs
        if miss:
            out["miss"] = miss
        if not args.skip_geom:
            gid = geom_id(c["model"], vid)
            if gid:
                out["gid"] = gid
        if status:
            out["d"] = status
            if other is not None:
                out["dm"] = other["model"]
                if status == "renamed":
                    out["dn"] = other["names"]
                else:
                    out["dimg"] = "img/" + slug(other["model"], other.get("tints")) + ".png"
                if not args.skip_geom:
                    og = geom_id(other["model"], v_old if vid == v_new else v_new)
                    if og:
                        out["dgid"] = og
        return out

    # карта сопоставлений для статусов на карточках
    status_map: dict[tuple[str, str], tuple[str, dict]] = {}
    for o, c, lc in pairs:
        diff_kind = None
        partner = None
        if o["model"] != c["model"] or tint_key(o.get("tints")) != tint_key(c.get("tints")):
            diff_kind, partner = "changed", o
        elif lc:
            diff_kind, partner = "renamed", o
        if diff_kind:
            status_map[(v_new, names_key(c["names"], c["base"]))] = (diff_kind, partner)
            status_map[(v_old, names_key(o["names"], o["base"]))] = (diff_kind, c)
    for c in added:
        status_map[(v_new, names_key(c["names"], c["base"]))] = ("added", None)
    for c in removed:
        status_map[(v_old, names_key(c["names"], c["base"]))] = ("removed", None)

    out_items = {}
    for vid, other in ((v_new, v_old), (v_old, v_new)):
        arr = []
        for c in parsed[vid]["cases"]:
            if not c["names"]:
                continue
            k = names_key(c["names"], c["base"])
            status, partner = status_map.get((vid, k), (None, None))
            arr.append(case_json(c, vid, status, partner))
        arr.sort(key=lambda x: (CATEGORY_ORDER.index(x["c"]) if x["c"] in CATEGORY_ORDER else 99,
                                x["b"], x["n"][0] if x["n"] else ""))
        out_items[vid] = arr
        print(f"[write] items-{vid}.json: {len(arr)} записей")

    cats = []
    for cid in CATEGORY_ORDER:
        n_new = sum(1 for x in out_items[v_new] if x["c"] == cid)
        n_old = sum(1 for x in out_items[v_old] if x["c"] == cid)
        if n_new or n_old:
            cats.append(
                {
                    "id": cid,
                    "label": CATEGORY_LABELS.get(cid, cid),
                    "new": n_new,
                    "old": n_old,
                    "added": sum(1 for x in out_items[v_new] if x["c"] == cid and x.get("d") == "added"),
                    "changed": sum(1 for x in out_items[v_new] if x["c"] == cid and x.get("d") == "changed"),
                }
            )

    groups = {}
    for x in out_items[v_new]:
        groups.setdefault(x["c"], {})
        groups[x["c"]].setdefault(x["b"], 0)
        groups[x["c"]][x["b"]] += 1

    bases = []
    for b in sorted({x["b"] for x in out_items[v_new]}):
        cnt = sum(1 for x in out_items[v_new] if x["b"] == b)
        bases.append({"id": b, "ru": BASE_ITEM_RU.get(b, b), "count": cnt})

    # --- броня ---------------------------------------------------------------
    def armor_json(vid):
        seen = set()
        out = []
        for e in armor[vid]:
            names = e["names"]
            if not names:
                continue
            key = (e.get("slot") or e.get("set"), tuple(sorted(n.casefold() for n in names)))
            if key in seen:
                continue
            seen.add(key)
            item = {
                "slot": e.get("slot", ""),
                "names": names,
                "item": e.get("item") or "",
                "system": e.get("system", ""),
                "src": e.get("source", ""),
                "set": e.get("set", ""),
                "tex": e.get("texture") or "",
            }
            # превью файла текстуры
            ref = (e.get("texture") or "").split(":", 1)
            if len(ref) == 2:
                ns, path = ref[0], ref[1]
                if path.startswith("textures/"):
                    path = path[len("textures/"):]
                p = Path(parsed[vid]["root"]) / "assets" / ns / "textures" / (path + ".png")
                if p.exists():
                    name = "armor__" + p.name
                    target = data / "armor" / name
                    target.parent.mkdir(parents=True, exist_ok=True)
                    if not target.exists():
                        from PIL import Image

                        with Image.open(p) as im:
                            im.save(target, optimize=True)
                    item["img"] = "armor/" + name
            out.append(item)
        out.sort(key=lambda x: (x["slot"], x["names"][0]))
        return out

    armor_out = {vid: armor_json(vid) for vid in (v_old, v_new)}

    # текстуры всех наборов брони (для раздела «Броня»)
    armor_tex_dir = data / "armor"
    armor_tex_dir.mkdir(parents=True, exist_ok=True)
    copied = 0
    for vid in (v_new,):
        src_root = Path(parsed[vid]["root"]) / "assets" / "ksepsp" / "textures" / "armor"
        if not src_root.exists():
            continue
        for set_dir in sorted(src_root.iterdir()):
            if not set_dir.is_dir():
                continue
            out_dir = armor_tex_dir / set_dir.name
            out_dir.mkdir(exist_ok=True)
            for png in sorted(set_dir.glob("*.png")):
                target = out_dir / png.name
                if not target.exists():
                    shutil.copyfile(png, target)
                    copied += 1
    print(f"[write] текстур брони скопировано: {copied}")

    # заглушка «нет текстуры»
    from PIL import Image

    miss_img = Image.new("RGBA", (16, 16))
    for yy in range(16):
        for xx in range(16):
            miss_img.putpixel((xx, yy), (0, 0, 0, 255) if (xx < 8) == (yy < 8) else (248, 0, 248, 255))
    miss_img.save(data / "tex" / "__missing.png")
    print(f"[write] armor: 9.5.1={len(armor_out[v_old])}, 9.6.2={len(armor_out[v_new])}")

    # наборы брони (папки с текстурами)
    def armor_sets(vid):
        root_dir = Path(parsed[vid]["root"]) / "assets" / "ksepsp" / "textures" / "armor"
        res = []
        if not root_dir.exists():
            return res
        for d in sorted(root_dir.iterdir()):
            if not d.is_dir():
                continue
            files = sorted(p.name for p in d.glob("*.png"))
            if not files:
                continue
            res.append({"set": d.name, "files": files})
        return res

    armor_sets_out = {vid: armor_sets(vid) for vid in (v_old, v_new)}

    # --- diff-отчёт ----------------------------------------------------------
    def diff_entry(c, other=None):
        e = {
            "b": c["base"],
            "n": c["names"],
            "c": c["category"],
            "m": c["model"],
            "k": names_key(c["names"], c["base"]),
            "img": "img/" + slug(c["model"], c.get("tints")) + ".png",
        }
        if other is not None:
            e["om"] = other["model"]
            e["oimg"] = "img/" + slug(other["model"], other.get("tints")) + ".png"
        return e

    # подсказки: «удалённое» могло быть просто переименовано/исправлено
    def _norm_names(names):
        return " ".join("".join(ch for ch in n.casefold() if ch.isalnum()) for n in names)

    maybe = {}
    for r in removed:
        key = names_key(r["names"], r["base"])
        best, score = None, 0.0
        rn = _norm_names(r["names"])
        for a in added:
            if a["base"] != r["base"]:
                continue
            sc = difflib.SequenceMatcher(None, rn, _norm_names(a["names"])).ratio()
            if sc > score:
                best, score = a, sc
        if best is not None and score >= 0.82:
            maybe[key] = {
                "n": best["names"],
                "b": best["base"],
                "k": names_key(best["names"], best["base"]),
                "score": round(score, 3),
            }

    diff = {
        "old_v": v_old,
        "maybe_renamed": maybe,
        "new_v": v_new,
        "counts": {
            "added": len(added),
            "removed": len(removed),
            "changed": len(changed),
            "renamed": len(renamed),
            "cases_old": len(out_items[v_old]),
            "cases_new": len(out_items[v_new]),
        },
        "added": sorted((diff_entry(c) for c in added), key=lambda x: (x["c"], x["n"][0])),
        "removed": sorted(
            (
                diff_entry(c) | ({"maybe": maybe[names_key(c["names"], c["base"])]}
                                 if names_key(c["names"], c["base"]) in maybe else {})
                for c in removed
            ),
            key=lambda x: (x["c"], x["n"][0]),
        ),
        "changed": sorted((diff_entry(c, o) for o, c in changed), key=lambda x: (x["c"], x["n"][0])),
        "renamed": sorted(
            (
                diff_entry(c, o) | {"on": o["names"], "ok": names_key(o["names"], o["base"])}
                for o, c in renamed
            ),
            key=lambda x: (x["c"], x["n"][0]),
        ),
        "files": {
            k: {kk: len(vv) for kk, vv in v.items()} | {"list": {kk: vv[:600] for kk, vv in v.items()}}
            for k, v in files_diff.items()
        },
        "armor_added": [
            a for a in armor_out[v_new]
            if not any(
                set(x.casefold() for x in a["names"]) == set(x.casefold() for x in b["names"])
                and a["slot"] == b["slot"]
                for b in armor_out[v_old]
            )
        ],
        "armor_removed": [
            b for b in armor_out[v_old]
            if not any(
                set(x.casefold() for x in a["names"]) == set(x.casefold() for x in b["names"])
                and a["slot"] == b["slot"]
                for a in armor_out[v_new]
            )
        ],
    }

    meta = {
        "generated": time.strftime("%Y-%m-%d %H:%M"),
        "build_seconds": round(time.time() - t0, 1),
        "versions": [info[v_old], info[v_new]],
        "default_version": v_new,
        "categories": cats,
        "bases": bases,
        "groups": groups,
        "counts": {
            "cases": {vid: len(out_items[vid]) for vid in (v_old, v_new)},
            "names": {
                vid: len({n for x in out_items[vid] for n in x["n"]}) for vid in (v_old, v_new)
            },
            "models": {vid: len({x["m"] for x in out_items[vid]}) for vid in (v_old, v_new)},
            "tinted": {vid: sum(1 for x in out_items[vid] if x.get("t")) for vid in (v_old, v_new)},
            "multi_state": {vid: sum(1 for x in out_items[vid] if x.get("st")) for vid in (v_old, v_new)},
            "armor": {vid: len(armor_out[vid]) for vid in (v_old, v_new)},
            "armor_sets": {vid: len(armor_sets_out[vid]) for vid in (v_old, v_new)},
            "files": {vid: info[vid]["stats"]["files"] for vid in (v_old, v_new)},
            "size": {vid: info[vid]["stats"]["size"] for vid in (v_old, v_new)},
        },
        "categories_ru": CATEGORY_LABELS,
    }

    def write(name, obj):
        p = data / name
        with open(p, "w", encoding="utf-8") as f:
            json.dump(obj, f, ensure_ascii=False, separators=(",", ":"))
        print(f"[write] {name}: {p.stat().st_size / 1024:.0f} КБ")

    write("meta.json", meta)
    write(f"items-{v_old}.json", out_items[v_old])
    write(f"items-{v_new}.json", out_items[v_new])
    write("diff.json", diff)
    write("armor.json", {"old": {v_old: armor_out[v_old]}, "new": armor_out[v_new],
                         "sets": {v_old: armor_sets_out[v_old], v_new: armor_sets_out[v_new]},
                         "old_v": v_old, "new_v": v_new})

    # --- шарды геометрии -----------------------------------------------------
    if not args.skip_geom and geom_items:
        ids = sorted(geom_items.keys())
        per = 200
        shard_of: dict[str, int] = {}
        shards: dict[int, dict] = {}
        for i, gid in enumerate(ids):
            s = i // per
            shard_of[gid] = s
            shards.setdefault(s, {})[gid] = geom_items[gid]
        for s, payload in shards.items():
            with open(geom_dir / f"s{s}.json", "w", encoding="utf-8") as f:
                json.dump(payload, f, ensure_ascii=False, separators=(",", ":"))
        geoms_index = {gid: f"geom/s{shard_of[gid]}.json" for gid in ids}
        write("geom-index.json", geoms_index)
        total = sum(os.path.getsize(geom_dir / f"s{s}.json") for s in shards)
        print(f"[3d] моделей: {len(ids)}, шардов: {len(shards)}, объём {total / 1e6:.1f} МБ")

    total_size = sum(p.stat().st_size for p in data.rglob("*") if p.is_file())
    n_files = sum(1 for p in data.rglob("*") if p.is_file())
    print(f"[done] data: {n_files} файлов, {total_size / 1e6:.1f} МБ, {time.time() - t0:.0f}s")


if __name__ == "__main__":
    main()
