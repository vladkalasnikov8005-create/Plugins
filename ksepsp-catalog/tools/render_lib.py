#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Мини-рендер Minecraft-моделей (item models) на numpy + Pillow.

Рендерит как «объёмные» модели (elements), так и плоские предметы
(item/generated: один квад с текстурой), с раскраской по сторонам света
и painter's algorithm (сортировка квадов по глубине + alpha blending).

Точки входа:
    build_quads(resolver, model_id, tint) -> list[quad]
    render_quads(quads, size=128, ssaa=3, yaw=35, pitch=20) -> PIL.Image (RGBA)
    render_model(resolver, model_id, ...) -> PIL.Image
"""

from __future__ import annotations

import math
import os
import tempfile

import numpy as np
from PIL import Image

_MISSING_TEX_PATH = None


def missing_texture_path() -> str:
    """Путь к PNG-заглушке «missing texture» (чёрно-розовая шахматка, как в Minecraft)."""
    global _MISSING_TEX_PATH
    if _MISSING_TEX_PATH and os.path.exists(_MISSING_TEX_PATH):
        return _MISSING_TEX_PATH
    a = np.zeros((16, 16, 4), dtype=np.uint8)
    for y in range(16):
        for x in range(16):
            if (x < 8) == (y < 8):
                a[y, x] = [0, 0, 0, 255]
            else:
                a[y, x] = [248, 0, 248, 255]
    p = os.path.join(tempfile.gettempdir(), "ksepsp_missing_texture.png")
    Image.fromarray(a, "RGBA").save(p)
    _MISSING_TEX_PATH = p
    return p

# ---------------------------------------------------------------------------
# грани: порядок углов TL, TR, BR, BL в экранном смысле (вид снаружи)
# ---------------------------------------------------------------------------

# множители «освещения» как в Minecraft (по направлению нормали)
FACE_SHADE = {
    "up": 1.0,
    "down": 0.5,
    "north": 0.8,
    "south": 0.8,
    "east": 0.6,
    "west": 0.6,
}

_ROT_AXIS = {
    "x": np.array([1.0, 0.0, 0.0]),
    "y": np.array([0.0, 1.0, 0.0]),
    "z": np.array([0.0, 0.0, 1.0]),
}


def _rot_matrix(axis: str, deg: float) -> np.ndarray:
    a = math.radians(deg)
    c, s = math.cos(a), math.sin(a)
    if axis == "x":
        return np.array([[1, 0, 0], [0, c, -s], [0, s, c]], dtype=float)
    if axis == "y":
        return np.array([[c, 0, s], [0, 1, 0], [-s, 0, c]], dtype=float)
    return np.array([[c, -s, 0], [s, c, 0], [0, 0, 1]], dtype=float)


def _face_corners(frm, to, face):
    x1, y1, z1 = frm
    x2, y2, z2 = to
    if face == "north":
        return [(x2, y2, z1), (x1, y2, z1), (x1, y1, z1), (x2, y1, z1)]
    if face == "south":
        return [(x1, y2, z2), (x2, y2, z2), (x2, y1, z2), (x1, y1, z2)]
    if face == "east":
        return [(x2, y2, z2), (x2, y2, z1), (x2, y1, z1), (x2, y1, z2)]
    if face == "west":
        return [(x1, y2, z1), (x1, y2, z2), (x1, y1, z2), (x1, y1, z1)]
    if face == "up":
        return [(x1, y2, z1), (x2, y2, z1), (x2, y2, z2), (x1, y2, z2)]
    if face == "down":
        return [(x1, y1, z2), (x2, y1, z2), (x2, y1, z1), (x1, y1, z1)]
    raise ValueError(face)


def _auto_uv(frm, to, face):
    x1, y1, z1 = frm
    x2, y2, z2 = to
    if face == "north":
        return [16 - x2, 16 - y2, 16 - x1, 16 - y1]
    if face == "south":
        return [x1, 16 - y2, x2, 16 - y1]
    if face == "east":
        return [16 - z2, 16 - y2, 16 - z1, 16 - y1]
    if face == "west":
        return [z1, 16 - y2, z2, 16 - y1]
    if face == "up":
        return [x1, z1, x2, z2]
    return [x1, 16 - z2, x2, 16 - z1]


def _uv_slots(uv, face_rot):
    u1, v1, u2, v2 = min(uv[0], uv[2]), min(uv[1], uv[3]), max(uv[0], uv[2]), max(uv[1], uv[3])
    slots = [(u1, v1), (u2, v1), (u2, v2), (u1, v2)]  # TL, TR, BR, BL
    r = int(round((face_rot or 0) / 90.0)) % 4
    return [slots[(i - r) % 4] for i in range(4)]


# ---------------------------------------------------------------------------
# построение квадов
# ---------------------------------------------------------------------------


def _tint_rgb(tint) -> tuple[float, float, float] | None:
    if not tint:
        return None
    if tint.get("type") == "dye" and tint.get("default") is not None:
        v = int(tint["default"]) & 0xFFFFFF
        return ((v >> 16 & 0xFF) / 255.0, (v >> 8 & 0xFF) / 255.0, (v & 0xFF) / 255.0)
    if tint.get("type") == "constant" and tint.get("value") is not None:
        v = int(tint["value"]) & 0xFFFFFF
        return ((v >> 16 & 0xFF) / 255.0, (v >> 8 & 0xFF) / 255.0, (v & 0xFF) / 255.0)
    return None


def build_quads(resolver, model_id, tint=None, texture_png=None):
    """Собирает квады модели (с учётом parent-цепочки и поворотов элементов)."""
    tex_map = resolver.textures(model_id)
    tint_rgb = _tint_rgb(tint)
    elements = resolver.elements(model_id)
    quads = []

    if elements:
        for el in elements:
            frm = [float(v) for v in el["from"]]
            to = [float(v) for v in el["to"]]
            rot = el.get("rotation")
            R = None
            origin = None
            if rot:
                origin = np.array([float(v) for v in rot.get("origin", [0, 0, 0])])
                R = _rot_matrix(rot.get("axis", "y"), float(rot.get("angle", 0)))

            def xf(p, R=R, origin=origin):
                p = np.array(p, dtype=float)
                return (R @ (p - origin) + origin).tolist() if R is not None else p.tolist()

            for face, fdata in (el.get("faces") or {}).items():
                uv = fdata.get("uv")
                uv = _auto_uv(frm, to, face) if uv is None else [float(v) for v in uv]
                ref = resolver.resolve_texture_ref(tex_map, fdata.get("texture", ""))
                png = texture_png(ref) if texture_png else None
                if png is None and texture_png is not None:
                    png = missing_texture_path()
                t_rgb = None
                key = (fdata.get("texture") or "").lstrip("#")
                tintable = key.lower() in ("layer0", "") or fdata.get("tintindex") is not None
                if tint_rgb is not None and tintable:
                    t_rgb = tint_rgb
                quads.append(
                    {
                        "verts": [xf(c) for c in _face_corners(frm, to, face)],
                        "uvs": _uv_slots(uv, fdata.get("rotation")),
                        "tex": png,
                        "shade": FACE_SHADE[face] if el.get("shade", True) else 1.0,
                        "tint": t_rgb,
                        "tintable": bool(tintable),
                        "double": False,
                        "face": face,
                    }
                )
        return quads, tex_map

    # --- нет elements -> item/generated: плоский спрайт из layer0..layerN
    layers = []
    for i in range(6):
        v = tex_map.get(f"layer{i}")
        if v is None:
            break
        ref = resolver.resolve_texture_ref(tex_map, v)
        png = texture_png(ref) if texture_png else None
        if png is None and texture_png is not None:
            png = missing_texture_path()
        if png:
            layers.append((png, tint_rgb if i == 0 else None))
    for i, (png, t) in enumerate(layers):
        try:
            with Image.open(png) as im:
                W, H = im.size
        except Exception:
            continue
        s = 16.0 / max(W, H)
        w, h = W * s, H * s
        x1, y1 = 8 - w / 2, 8 - h / 2
        x2, y2 = 8 + w / 2, 8 + h / 2
        z = 8.0
        quads.append(
            {
                "verts": [(x1, y2, z), (x2, y2, z), (x2, y1, z), (x1, y1, z)],
                "uvs": [(0, 0), (W, 0), (W, H), (0, H)],
                "tex": png,
                "shade": 1.0,
                "tint": t,
                "tintable": i == 0,
                "double": True,
                "face": "south",
            }
        )
    return quads, tex_map


# ---------------------------------------------------------------------------
# рендер
# ---------------------------------------------------------------------------


class _TexCache:
    def __init__(self):
        self.cache: dict[str, tuple[np.ndarray, int, int]] = {}

    def get(self, path):
        if path not in self.cache:
            with Image.open(path) as im:
                im = im.convert("RGBA")
                arr = np.asarray(im, dtype=np.uint8)
            self.cache[path] = (arr, arr.shape[1], arr.shape[0])
        return self.cache[path]


def render_quads(
    quads,
    size: int = 128,
    ssaa: int = 3,
    yaw: float = 35.0,
    pitch: float = 20.0,
    background=(0, 0, 0, 0),
    margin: float = 0.90,
    tex_cache: _TexCache | None = None,
    fit_scale: float | None = None,
):
    """Ортографический рендер списка квадов в RGBA-картинку."""
    if not quads:
        return Image.new("RGBA", (size, size), background)

    tex_cache = tex_cache or _TexCache()
    S = size * ssaa
    fb = np.zeros((S, S, 4), dtype=np.float32)
    if background[3] > 0:
        fb[:, :, 0] = background[0] / 255.0
        fb[:, :, 1] = background[1] / 255.0
        fb[:, :, 2] = background[2] / 255.0
        fb[:, :, 3] = background[3] / 255.0

    pts = np.array([v for q in quads for v in q["verts"]], dtype=float)
    center = (pts.min(axis=0) + pts.max(axis=0)) / 2.0
    ext = pts.max(axis=0) - pts.min(axis=0)
    span = max(ext.max(), 0.5)

    Ry = _rot_matrix("y", yaw)
    Rx = _rot_matrix("x", pitch)
    R = Rx @ Ry

    scale = (fit_scale if fit_scale is not None else (S * margin / span))
    half = S / 2.0

    tris = []
    for q in quads:
        pv = [(R @ (np.array(v, dtype=float) - center)) for v in q["verts"]]
        scr = [(half + p[0] * scale, half - p[1] * scale) for p in pv]
        depth = [p[2] for p in pv]
        _, tw, th = tex_cache.get(q["tex"])
        uvs = [(u / tw, v / th) for (u, v) in q["uvs"]]
        for (i, j, k) in ((0, 1, 2), (0, 2, 3)):
            tris.append(
                {
                    "p": [scr[i], scr[j], scr[k]],
                    "uv": [uvs[i], uvs[j], uvs[k]],
                    "d": (depth[i] + depth[j] + depth[k]) / 3.0,
                    "tex": q["tex"],
                    "shade": q["shade"],
                    "tint": q["tint"],
                }
            )

    tris.sort(key=lambda t: t["d"])  # дальние сначала

    for t in tris:
        arr, tw, th = tex_cache.get(t["tex"])
        (x0, y0), (x1, y1), (x2, y2) = t["p"]
        area = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0)
        if abs(area) < 1e-9:
            continue
        minx = max(int(math.floor(min(x0, x1, x2))), 0)
        maxx = min(int(math.ceil(max(x0, x1, x2))), S - 1)
        miny = max(int(math.floor(min(y0, y1, y2))), 0)
        maxy = min(int(math.ceil(max(y0, y1, y2))), S - 1)
        if minx > maxx or miny > maxy:
            continue
        xs = np.arange(minx, maxx + 1, dtype=np.float32) + 0.5
        ys = np.arange(miny, maxy + 1, dtype=np.float32) + 0.5
        gx, gy = np.meshgrid(xs, ys)
        # веса вершин B (индекс 1) и C (индекс 2)
        wB = ((gx - x0) * (y2 - y0) - (x2 - x0) * (gy - y0)) / area
        wC = ((x1 - x0) * (gy - y0) - (gx - x0) * (y1 - y0)) / area
        mask = (wB >= -0.002) & (wC >= -0.002) & (wB + wC <= 1.002)
        if not mask.any():
            continue
        u = (1 - wB - wC) * t["uv"][0][0] + wB * t["uv"][1][0] + wC * t["uv"][2][0]
        v = (1 - wB - wC) * t["uv"][0][1] + wB * t["uv"][1][1] + wC * t["uv"][2][1]
        tu = np.mod((u * tw).astype(np.int32), tw)
        tv = np.mod((v * th).astype(np.int32), th)
        texels = arr[tv, tu].astype(np.float32) / 255.0
        a = texels[:, :, 3] * mask
        if not (a > 0.001).any():
            continue
        rgb = texels[:, :, :3] * t["shade"]
        if t["tint"] is not None:
            rgb = rgb * np.array(t["tint"], dtype=np.float32)
        sub = fb[miny : maxy + 1, minx : maxx + 1, :]
        A = a[:, :, None]
        sub[:, :, :3] = sub[:, :, :3] * (1 - A) + rgb * A
        sub[:, :, 3:4] = sub[:, :, 3:4] * (1 - A) + A

    out = Image.fromarray((np.clip(fb, 0, 1) * 255).astype(np.uint8), "RGBA")
    if ssaa > 1:
        out = out.resize((size, size), Image.LANCZOS)
    return out


def render_model(resolver, model_id, tint=None, size=128, ssaa=3, yaw=35.0, pitch=20.0, tex_cache=None,
                 fit_scale=None):
    quads, _ = build_quads(resolver, model_id, tint, texture_png=resolver.texture_png)
    return render_quads(quads, size=size, ssaa=ssaa, yaw=yaw, pitch=pitch, tex_cache=tex_cache,
                        fit_scale=fit_scale)
