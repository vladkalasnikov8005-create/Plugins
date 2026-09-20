#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Разбор ресурспаков KSEPSP (9.5.1 / 9.6.2).

Что делает модуль:
  * читает assets/minecraft/items/*.json  -> список переименований (case'ов):
      базовый предмет -> имя (RU/EN) -> модель -> текстуры;
  * умеет резолвить цепочку parent'ов модели (ksepsp:item/... -> item/generated и т.п.);
  * собирает каталог брони из assets/minecraft/rpt/swappers/** (система RPF/RPT)
    и из старого OptiFine-формата (.properties), если он есть;
  * считает diff между двумя версиями пака.

Только стандартная библиотека + (для рендера, см. render_lib.py) pillow/numpy.
"""

from __future__ import annotations

import json
import os
import re
import zipfile

# ----------------------------------------------------------------------------
# распаковка zip
# ----------------------------------------------------------------------------


def extract_zip(zip_path: str, out_dir: str) -> str:
    """Распаковывает пак (с фоллбэком для кривых имён файлов) в out_dir."""
    os.makedirs(out_dir, exist_ok=True)
    with zipfile.ZipFile(zip_path) as z:
        for info in z.infolist():
            name = info.filename
            if not (info.flag_bits & 0x800):
                raw = name.encode("cp437", errors="replace")
                for enc in ("utf-8", "cp866", "cp1251"):
                    try:
                        name = raw.decode(enc)
                        break
                    except Exception:
                        continue
            target = os.path.join(out_dir, name)
            if info.is_dir():
                os.makedirs(target, exist_ok=True)
                continue
            os.makedirs(os.path.dirname(target), exist_ok=True)
            with z.open(info) as src, open(target, "wb") as dst:
                dst.write(src.read())
    return out_dir


def read_json(path: str):
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


# ----------------------------------------------------------------------------
# модели
# ----------------------------------------------------------------------------


class ModelResolver:
    """Резолвер моделей внутри пака: parent-цепочки, текстуры, элементы.

    extra_roots — дополнительные паки (например старая версия), куда резолвер
    заглядывает, если файла нет в основном паке.
    """

    def __init__(self, pack_root: str, extra_roots: list[str] | None = None):
        self.root = pack_root
        self.roots = [pack_root] + [r for r in (extra_roots or []) if r != pack_root]
        self._cache: dict[str, dict | None] = {}
        self._chain_cache: dict[str, list[tuple[str, dict]]] = {}

    def model_path(self, model_id: str) -> str:
        ns, _, path = model_id.partition(":")
        if not path:
            ns, path = "minecraft", ns
        return os.path.join(self.root, "assets", ns, "models", path + ".json")

    def load(self, model_id: str) -> dict | None:
        if model_id in self._cache:
            return self._cache[model_id]
        ns, _, path = model_id.partition(":")
        if not path:
            ns, path = "minecraft", ns
        d = None
        for root in self.roots:
            p = os.path.join(root, "assets", ns, "models", path + ".json")
            if os.path.exists(p):
                d = read_json(p)
                break
        self._cache[model_id] = d
        return d

    def chain(self, model_id: str) -> list[tuple[str, dict]]:
        """[(model_id, json), ...] от самой модели к корню."""
        if model_id in self._chain_cache:
            return self._chain_cache[model_id]
        out: list[tuple[str, dict]] = []
        cur = model_id
        for _ in range(32):
            d = self.load(cur)
            if d is None:
                out.append((cur, {"__missing__": True}))
                break
            out.append((cur, d))
            parent = d.get("parent")
            if not parent:
                break
            if ":" not in parent:
                parent = "minecraft:" + parent
            cur = parent
        self._chain_cache[model_id] = out
        return out

    def textures(self, model_id: str) -> dict[str, str]:
        tex: dict[str, str] = {}
        for _, d in reversed(self.chain(model_id)):  # от корня к ребёнку
            tex.update(d.get("textures") or {})
        return tex

    def display(self, model_id: str) -> dict:
        disp: dict = {}
        for _, d in reversed(self.chain(model_id)):
            disp.update(d.get("display") or {})
        return disp

    def elements(self, model_id: str) -> list[dict] | None:
        for _, d in self.chain(model_id):
            if isinstance(d.get("elements"), list):
                return d["elements"]
        return None

    def resolve_texture_ref(self, tex: dict[str, str], value: str) -> str | None:
        """'#layer0' -> 'ksepsp:item/foo' (или None, если битая ссылка)."""
        seen = set()
        while value.startswith("#"):
            key = value[1:]
            if key in seen or key not in tex:
                return None
            seen.add(key)
            value = tex[key]
        return value

    def texture_png(self, ref: str) -> str | None:
        """Путь к существующему .png для ссылки вида 'ksepsp:item/foo'."""
        if not ref:
            return None
        ns, _, path = ref.partition(":")
        if not path:
            ns, path = "minecraft", ns
        for root in self.roots:
            p = os.path.join(root, "assets", ns, "textures", path + ".png")
            if os.path.exists(p):
                return p
        return None


# ----------------------------------------------------------------------------
# переименования предметов (assets/minecraft/items/*.json)
# ----------------------------------------------------------------------------

CATEGORY_LABELS = {
    "weapons": "Оружие",
    "tools": "Инструменты",
    "hats": "Шапки и аксессуары",
    "food": "Еда и напитки",
    "furniture": "Мебель и предметы",
    "potions": "Зелья и медицина",
    "other": "Прочее",
    "armor": "Броня",
    "misc": "Разное",
}


def category_of(model_id: str) -> str:
    ns, _, path = model_id.partition(":")
    parts = path.split("/")
    # ksepsp:item/9_0/<category>/...
    if len(parts) >= 3 and parts[0] == "item" and re.fullmatch(r"\d+_\d+", parts[1]):
        return parts[2]
    if len(parts) >= 2:
        return parts[0]
    return "other"


def era_of(model_id: str) -> str | None:
    ns, _, path = model_id.partition(":")
    parts = path.split("/")
    if len(parts) >= 3 and parts[0] == "item" and re.fullmatch(r"\d+_\d+", parts[1]):
        return parts[1]
    return None


def _tint_of(model: dict):
    tints = model.get("tints") or []
    for t in tints:
        if t.get("type") == "minecraft:dye":
            return {"type": "dye", "default": t.get("default")}
        if t.get("type") == "minecraft:constant":
            return {"type": "constant", "value": t.get("value")}
    return None


VANILLA_TYPES = {
    "model", "select", "range_dispatch", "condition", "composite", "empty", "special",
}


def _is_vanilla_type(t: str) -> bool:
    return t in VANILLA_TYPES


def collect_states(node: dict, cond: str = "", depth: int = 0, is_fb: bool = False):
    """Рекурсивно обходит дерево item-модели и собирает все листья minecraft:model.

    Возвращает список состояний: {model, tints, cond, node}.
    `cond` - человекочитаемое условие (use_duration>=0.65 / display_context=gui ...).
    """
    out = []
    if not isinstance(node, dict) or depth > 12:
        return out
    t = (node.get("type") or "").removeprefix("minecraft:")
    if t == "model":
        out.append(
            {
                "model": node.get("model"),
                "tints": _tint_of(node),
                "cond": cond,
                "fb": is_fb,
                "node": node,
            }
        )
        return out
    if t == "select":
        prop = (node.get("property") or "").replace("minecraft:", "")
        for case in node.get("cases", []) or []:
            when = case.get("when")
            vals = [when] if isinstance(when, str) else list(when or [])
            label = ",".join(str(v) for v in vals)
            if prop == "component":
                sub_cond = cond
            else:
                sub_cond = (cond + " " if cond else "") + f"{prop}={label}"
            out.extend(collect_states(case.get("model") or {}, sub_cond, depth + 1, is_fb))
        if node.get("fallback") is not None:
            out.extend(collect_states(node["fallback"], cond, depth + 1, True))
        return out
    if t == "range_dispatch":
        prop = (node.get("property") or "").replace("minecraft:", "")
        for e in node.get("entries", []) or []:
            thr = e.get("threshold")
            sub_cond = (cond + " " if cond else "") + f"{prop}>={thr}"
            out.extend(collect_states(e.get("model") or {}, sub_cond, depth + 1))
        if node.get("fallback") is not None:
            out.extend(collect_states(node["fallback"], cond, depth + 1))
        return out
    if t == "condition":
        for key, label in (("on_true", "cond:true"), ("on_false", "cond:false")):
            if node.get(key) is not None:
                sub_cond = (cond + " " if cond else "") + label
                out.extend(collect_states(node[key], sub_cond, depth + 1, key == "on_false"))
        return out
    if t == "composite":
        for sub in node.get("models", []) or []:
            out.extend(collect_states(sub, cond, depth + 1, is_fb))
        return out
    if t and not _is_vanilla_type(t):
        # rpt: / rpf: и прочие сторонние модели - фиксируем как есть
        out.append({"model": None, "tints": None, "cond": cond, "fb": is_fb, "node": node})
        return out
    if node:
        out.append({"model": None, "tints": None, "cond": cond, "fb": is_fb, "node": node})
    return out


def iter_cases(pack_root: str, resolver: ModelResolver | None = None):
    """Все переименования из items/*.json.

    Отдаёт список dict'ов:
        base     - базовый предмет (iron_sword, bow, ...)
        names    - список имён (RU/EN), которые надо написать в наковальне
        states   - состояния модели: [{model, tints, cond}, ...]
        model    - основная модель (первое состояние без условия)
        category - категория (weapons/hats/...)
        era      - 8_0 (легаси) / 9_0 / None
        group    - группа внутри категории (например bow/assault_rifle)
    """
    items_dir = os.path.join(pack_root, "assets", "minecraft", "items")
    out = []
    if not os.path.isdir(items_dir):
        return out
    for fname in sorted(os.listdir(items_dir)):
        if not fname.endswith(".json"):
            continue
        base = fname[:-5]
        d = read_json(os.path.join(items_dir, fname))
        model = d.get("model") or {}
        top = (model.get("type") or "").removeprefix("minecraft:")
        if top == "select" and (model.get("property") or "").endswith("component"):
            for case in model.get("cases", []) or []:
                when = case.get("when")
                names = [when] if isinstance(when, str) else list(when or [])
                names = [n for n in names if isinstance(n, str)]
                states = collect_states(case.get("model") or {})
                out.append(_make_case(base, names, states, d))
        else:
            states = collect_states(model)
            out.append(_make_case(base, [], states, d))
    return out


def _pick_main(states: list[dict]):
    """Выбирает «главное» состояние модели: иконка в инвентаре, иначе ksepsp-модель."""
    with_model = [s for s in states if s.get("model")]
    if not with_model:
        return None

    def is_custom(s):
        return str(s["model"]).startswith("ksepsp:")

    for s in with_model:  # иконка в инвентаре
        if "display_context=gui" in (s.get("cond") or "") and is_custom(s):
            return s
    for s in with_model:  # обычная модель (не fallback) и не ванильная
        if not s.get("cond") and not s.get("fb") and is_custom(s):
            return s
    for s in with_model:
        if is_custom(s):
            return s
    for s in with_model:
        if not s.get("cond") and not s.get("fb"):
            return s
    return with_model[0]


def _make_case(base: str, names: list[str], states: list[dict], item_def: dict):
    main_state = _pick_main(states)
    main = main_state["model"] if main_state else None
    models = [s["model"] for s in states if s.get("model")]
    cat = category_of(main) if main else "misc"
    era = era_of(main) if main else None
    group = None
    if main:
        parts = main.partition(":")[2].split("/")
        if len(parts) >= 4:
            group = "/".join(parts[2:-1])
    return {
        "base": base,
        "names": names,
        "states": states,
        "model": main,
        "tints": (main_state or {}).get("tints"),
        "models": models,
        "category": cat,
        "era": era,
        "group": group,
        "oversized": bool(item_def.get("oversized_in_gui")),
    }


# ----------------------------------------------------------------------------
# броня / прочие свапы текстур (RPF/RPT + OptiFine)
# ----------------------------------------------------------------------------


def iter_armor(pack_root: str):
    """Каталог брони: rpt/swappers/** + легаси OptiFine .properties."""
    entries = []

    # --- современный формат: assets/minecraft/rpt/swappers/**/*.json
    sw_root = os.path.join(pack_root, "assets", "minecraft", "rpt", "swappers")
    if os.path.isdir(sw_root):
        for root, _dirs, files in os.walk(sw_root):
            for fn in sorted(files):
                if not fn.endswith(".json"):
                    continue
                rel = os.path.relpath(os.path.join(root, fn), sw_root).replace(os.sep, "/")
                d = read_json(os.path.join(root, fn))
                entries.extend(_rpt_cases(rel, d))

    # --- старый формат: OptiFine CIT .properties
    armor_root = os.path.join(pack_root, "assets", "ksepsp", "textures", "armor")
    if os.path.isdir(armor_root):
        for root, _dirs, files in os.walk(armor_root):
            for fn in sorted(files):
                if not fn.endswith(".properties") or fn.endswith("_icon.properties"):
                    continue
                info = _parse_properties(os.path.join(root, fn))
                if info.get("type") != "armor":
                    continue
                names = _names_from_regex(info.get("nbt.display.Name", ""))
                setname = os.path.basename(root)
                item = info.get("matchItems", "")
                layer = ""
                for k in info:
                    if k.startswith("texture."):
                        layer = k.split(".", 1)[1]
                        break
                entries.append(
                    {
                        "system": "optifine",
                        "source": os.path.relpath(os.path.join(root, fn), pack_root).replace(os.sep, "/"),
                        "slot": _slot_of_item(item),
                        "opts": [],
                        "names": names,
                        "texture": f"ksepsp:textures/armor/{setname}/" + (info.get("texture." + layer, setname) or setname),
                        "set": setname,
                        "item": item,
                        "layer": layer,
                        "comment": _first_comment(os.path.join(root, fn)),
                    }
                )
    return entries


ITEM_SLOT_RU = {
    "leather_helmet": "Шлем",
    "leather_chestplate": "Нагрудник",
    "leather_leggings": "Штаны",
    "leather_boots": "Ботинки",
    "iron_helmet": "Шлем",
    "iron_chestplate": "Нагрудник",
    "iron_leggings": "Штаны",
    "iron_boots": "Ботинки",
}


def _slot_of_item(item: str) -> str:
    if "helmet" in item:
        return "helmet"
    if "chestplate" in item or "tunic" in item:
        return "chestplate"
    if "leggings" in item:
        return "leggings"
    if "boots" in item:
        return "boots"
    return item


def _rpt_cases(rel: str, d: dict, path_opts: list | None = None, out: list | None = None):
    """Разбор rpt:component/rpt:apply дерева в плоский список переименований."""
    out = [] if out is None else out
    path_opts = path_opts or []
    if not isinstance(d, dict):
        return out

    # набор опций, которые были применены выше по дереву
    opts = list(path_opts)
    for key in ("arm_transform",):
        if d.get("type", "").startswith("rpt:") and key in d:
            opts.append(f"{key}={d[key]}")

    parts = rel.split("/")
    # textures/entity/equipment/humanoid/leather.json  -> humanoid / leather
    target = "/".join(parts[1:-1]) if len(parts) > 2 else rel
    slot = target.split("/")[-1]

    for case in d.get("cases", []) or []:
        when = case.get("when")
        names = [when] if isinstance(when, str) else list(when or [])
        names = [n for n in names if isinstance(n, str)]
        child = case.get("child") or {}
        sub = _rpt_cases(rel, child, opts, [])
        if sub:
            for s in sub:
                s["names"] = names + s["names"]
            out.extend(sub)
        elif child.get("type") == "rpt:apply":
            out.append(
                {
                    "system": "rpt",
                    "source": rel,
                    "target": target,
                    "slot": slot,
                    "opts": opts,
                    "names": names,
                    "texture": child.get("value"),
                    "item": None,
                    "set": target,
                    "layer": slot,
                    "comment": None,
                }
            )
    return out


def _parse_properties(path: str) -> dict:
    props = {}
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, v = line.split("=", 1)
            props[k.strip()] = v.strip()
    return props


def _first_comment(path: str) -> str | None:
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        for line in f:
            line = line.strip()
            if line.startswith("#"):
                return line.lstrip("#").strip()
    return None


def _names_from_regex(rx: str):
    """iregex:(\\u041a...|Bomber Jacket) -> ['Куртка бомбер', 'Bomber Jacket']"""
    if not rx:
        return []
    m = re.match(r"\s*iregex:?\((.*)\)\s*$", rx, re.S) or re.match(r"\s*regex:?\((.*)\)\s*$", rx, re.S)
    body = m.group(1) if m else rx
    out = []
    for part in re.split(r"(?<!\\)\|", body):
        part = part.strip()
        if not part:
            continue
        try:
            part = part.encode().decode("unicode_escape").encode("latin1").decode("utf-8")
        except Exception:
            try:
                part = part.encode().decode("unicode_escape")
            except Exception:
                pass
        out.append(part)
    return out


# ----------------------------------------------------------------------------
# статистика по паку
# ----------------------------------------------------------------------------


def pack_stats(pack_root: str) -> dict:
    files = 0
    size = 0
    by_ext: dict[str, int] = {}
    for root, _dirs, fs in os.walk(pack_root):
        for f in fs:
            p = os.path.join(root, f)
            files += 1
            try:
                size += os.path.getsize(p)
            except OSError:
                pass
            ext = os.path.splitext(f)[1].lower() or "(no ext)"
            by_ext[ext] = by_ext.get(ext, 0) + 1
    return {"files": files, "size": size, "by_ext": by_ext}


def mcmeta_info(pack_root: str) -> dict:
    p = os.path.join(pack_root, "pack.mcmeta")
    if not os.path.exists(p):
        return {}
    d = read_json(p).get("pack", {})
    return {
        "pack_format": d.get("pack_format"),
        "min_format": d.get("min_format"),
        "max_format": d.get("max_format"),
        "supported_formats": d.get("supported_formats"),
        "description": d.get("description"),
    }
