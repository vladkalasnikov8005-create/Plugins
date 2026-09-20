/* Интерактивный 3D-просмотр моделей KSEPSP через three.js.
   Геометрия приходит из data/geom-index.json + data/geom/sN.json (квады с UV и затенением),
   текстуры — обычные PNG из data/tex/. Освещение «запечено» в цвета вершин (как в Minecraft). */
(function (global) {
  'use strict';

  let THREE = null;
  let threePromise = null;

  function loadThree() {
    if (threePromise) return threePromise;
    threePromise = import('./vendor/three.module.min.js').then(function (mod) {
      THREE = mod;
      return mod;
    });
    return threePromise;
  }

  const shardCache = new Map();   // путь шарда -> Promise<объект>
  let geomIndex = null;
  let geomIndexPromise = null;

  function loadGeomIndex() {
    if (geomIndexPromise) return geomIndexPromise;
    geomIndexPromise = fetch('data/geom-index.json')
      .then(function (r) { return r.json(); })
      .then(function (j) { geomIndex = j; return j; });
    return geomIndexPromise;
  }

  function loadShard(path) {
    if (!shardCache.has(path)) {
      shardCache.set(path, fetch('data/' + path).then(function (r) { return r.json(); }));
    }
    return shardCache.get(path);
  }

  function getGeometry(gid) {
    return loadGeomIndex().then(function (idx) {
      const shard = idx[gid];
      if (!shard) return null;
      return loadShard(shard).then(function (data) { return data[gid] || null; });
    });
  }

  const texCache = new Map(); // url -> THREE.Texture

  function getTexture(url) {
    if (texCache.has(url)) return texCache.get(url);
    const tex = new THREE.TextureLoader().load(url);
    tex.magFilter = THREE.NearestFilter;
    tex.minFilter = THREE.NearestFilter;
    tex.generateMipmaps = false;
    tex.colorSpace = THREE.SRGBColorSpace;
    tex.wrapS = tex.wrapT = THREE.ClampToEdgeWrapping;
    texCache.set(url, tex);
    tex._ready = false;
    const img = new Image();
    img.onload = function () { tex._size = [img.naturalWidth, img.naturalHeight]; tex._ready = true; };
    img.src = url;
    return tex;
  }

  function hexToRgb(hex) {
    const h = hex.replace('#', '');
    const v = parseInt(h.length === 3 ? h.split('').map(function (c) { return c + c; }).join('') : h, 16);
    return [((v >> 16) & 255) / 255, ((v >> 8) & 255) / 255, (v & 255) / 255];
  }

  // 16 красителей Minecraft: id -> цвет и название
  const DYE = [
    ['white', '#f9fffe', 'Белый'],
    ['orange', '#f9801d', 'Оранжевый'],
    ['magenta', '#c74ebd', 'Пурпурный'],
    ['light_blue', '#3ab3da', 'Голубой'],
    ['yellow', '#fed83d', 'Жёлтый'],
    ['lime', '#80c71f', 'Лаймовый'],
    ['pink', '#f38baa', 'Розовый'],
    ['gray', '#474f52', 'Серый'],
    ['light_gray', '#9d9d97', 'Светло-серый'],
    ['cyan', '#169c9c', 'Бирюзовый'],
    ['purple', '#8932b8', 'Фиолетовый'],
    ['blue', '#3c44aa', 'Синий'],
    ['brown', '#835432', 'Коричневый'],
    ['green', '#5e7c16', 'Зелёный'],
    ['red', '#b02e26', 'Красный'],
    ['black', '#1d1d21', 'Чёрный']
  ];
  const SWATCHES = DYE.map(function (d) { return d[1]; });
  const DYE_COLORS = {};
  DYE.forEach(function (d) { DYE_COLORS[d[0]] = d[1]; DYE_COLORS[d[2]] = d[1]; });

  /**
   * Собирает буферы для three.js из квадов модели (данные из data/geom/*.json).
   * quads: [{v:[[x,y,z]x4], u:[[u,v]x4], t:'tex/foo.png', s: затенение, tt: перекрашивается, d: плоский}]
   * texSize(url) -> [w, h] | null  (нужно, чтобы UV попадали в пиксельную сетку текстуры)
   */
  function buildBuffers(quads, texSize) {
    const pos = [], uv = [], col = [], tt = [], base = [];
    let minX = 1e9, minY = 1e9, minZ = 1e9, maxX = -1e9, maxY = -1e9, maxZ = -1e9;
    const groups = new Map();
    const pending = [];
    quads.forEach(function (q) {
      const url = 'data/' + q.t;
      const size = texSize(url);
      if (!size) pending.push(url);
      const t0 = size ? size[0] : 16;
      const t1 = size ? size[1] : 16;
      if (!groups.has(url)) groups.set(url, { url: url, tris: [] });
      const g = groups.get(url);
      const baseIdx = pos.length / 3;
      for (let i = 0; i < 4; i++) {
        const v = q.v[i];
        pos.push(v[0], v[1], v[2]);
        if (v[0] < minX) minX = v[0]; if (v[0] > maxX) maxX = v[0];
        if (v[1] < minY) minY = v[1]; if (v[1] > maxY) maxY = v[1];
        if (v[2] < minZ) minZ = v[2]; if (v[2] > maxZ) maxZ = v[2];
        const u = q.u[i][0], vv = q.u[i][1];
        const uu = Math.max(0.001, Math.min(0.999, (u + 0.02) / t0));
        const vvv = 1 - Math.max(0.001, Math.min(0.999, (vv + 0.02) / t1));
        uv.push(uu, vvv);
        col.push(q.s, q.s, q.s);
        base.push(q.s);
        tt.push(q.tt ? 1 : 0);
      }
      g.tris.push(baseIdx, baseIdx + 1, baseIdx + 2, baseIdx, baseIdx + 2, baseIdx + 3);
    });
    const span = Math.max(maxX - minX, maxY - minY, maxZ - minZ, 1.5);
    return {
      pos: pos, uv: uv, col: col, base: base, tt: tt,
      groups: Array.from(groups.values()),
      pending: pending,
      center: [(minX + maxX) / 2, (minY + maxY) / 2, (minZ + maxZ) / 2],
      span: span,
      fitScale: span * 0.62
    };
  }

  class Viewer3D {
    constructor(canvas, opts) {
      this.canvas = canvas;
      this.opts = opts || {};
      this.yaw = -0.6;
      this.pitch = 0.42;
      this.zoom = 1;
      this.autoRotate = true;
      this.disposed = false;
      this.group = null;
      this.mesh = null;
      this.tint = [1, 1, 1];
      this.ready = false;
      this._bindEvents();
      this._init().then((function (self) { return function () { self.ready = true; if (self._pending) { self.show(self._pending.gid, self._pending.opts); self._pending = null; } }; })(this));
    }

    _init() {
      const self = this;
      return loadThree().then(function () {
        if (self.disposed) return;
        try {
          self.renderer = new THREE.WebGLRenderer({ canvas: self.canvas, antialias: true, alpha: true, powerPreference: 'low-power' });
        } catch (e) {
          self._fail(e);
          return;
        }
        self.renderer.setPixelRatio(Math.min(global.devicePixelRatio || 1, 2));
        self.scene = new THREE.Scene();
        self.camera = new THREE.OrthographicCamera(-1, 1, 1, -1, 0.01, 2000);
        self.root = new THREE.Group();
        self.scene.add(self.root);
        self._resize();
        self._loop();
      }).catch(function (e) { self._fail(e); });
    }

    _fail(e) {
      this.failed = true;
      this.ready = false;
      if (this.opts && this.opts.onFail) this.opts.onFail(e);
    }

    _resize() {
      if (!this.renderer) return;
      const r = this.canvas.getBoundingClientRect();
      const w = Math.max(1, Math.round(r.width));
      const h = Math.max(1, Math.round(r.height));
      this.renderer.setSize(w, h, false);
      this.aspect = w / h;
      this._applyCamera();
    }

    _applyCamera() {
      if (!this.camera) return;
      const s = (this.fitScale || 20) / this.zoom;
      const a = this.aspect || 1;
      if (a >= 1) {
        this.camera.left = -s * a; this.camera.right = s * a;
        this.camera.top = s; this.camera.bottom = -s;
      } else {
        this.camera.left = -s; this.camera.right = s;
        this.camera.top = s / a; this.camera.bottom = -s / a;
      }
      this.camera.position.set(0, 0, 300);
      this.camera.near = 1; this.camera.far = 1000;
      this.camera.updateProjectionMatrix();
    }

    _bindEvents() {
      const self = this;
      const c = this.canvas;
      let dragging = false, lx = 0, ly = 0, moved = false;
      c.addEventListener('pointerdown', function (e) {
        dragging = true; moved = false; lx = e.clientX; ly = e.clientY;
        c.setPointerCapture(e.pointerId);
      });
      c.addEventListener('pointermove', function (e) {
        if (!dragging) return;
        const dx = e.clientX - lx, dy = e.clientY - ly;
        lx = e.clientX; ly = e.clientY;
        if (Math.abs(dx) + Math.abs(dy) > 2) moved = true;
        self.autoRotate = false;
        self.yaw += dx * 0.012;
        self.pitch = Math.max(-1.45, Math.min(1.45, self.pitch + dy * 0.012));
      });
      const up = function (e) {
        dragging = false;
        if (e && e.pointerId !== undefined) { try { c.releasePointerCapture(e.pointerId); } catch (_) { } }
      };
      c.addEventListener('pointerup', up);
      c.addEventListener('pointercancel', up);
      c.addEventListener('wheel', function (e) {
        e.preventDefault();
        self.zoom = Math.max(0.45, Math.min(4, self.zoom * (e.deltaY > 0 ? 0.9 : 1.11)));
      }, { passive: false });
      c.addEventListener('dblclick', function () { self.zoom = 1; self.yaw = -0.6; self.pitch = 0.42; self.autoRotate = true; });
      if (global.ResizeObserver) {
        this._ro = new ResizeObserver(function () { self._resize(); });
        this._ro.observe(c);
      } else {
        global.addEventListener('resize', function () { self._resize(); });
      }
    }

    _loop() {
      if (this.disposed) return;
      const self = this;
      requestAnimationFrame(function () { self._loop(); });
      if (!this.renderer || !this.mesh) return;
      if (this.autoRotate) this.yaw += 0.006;
      this.root.rotation.set(0, 0, 0);
      // сначала «pitch» вокруг X, затем поворот вокруг Y
      this.root.rotation.order = 'YXZ';
      this.root.rotation.y = this.yaw;
      this.root.rotation.x = this.pitch;
      this.renderer.render(this.scene, this.camera);
    }

    setTint(hex) {
      this.tint = hex ? hexToRgb(hex) : [1, 1, 1];
      if (this.mesh) this._paintColors();
    }

    _paintColors() {
      const geo = this.mesh.geometry;
      const colorAttr = geo.getAttribute('color');
      const base = this._baseShade;
      const tt = this._tintable;
      const t = this.tint;
      for (let i = 0; i < colorAttr.count; i++) {
        const k = tt[i] ? 1 : 0;
        colorAttr.setXYZ(i,
          base[i] * (k ? t[0] : 1),
          base[i] * (k ? t[1] : 1),
          base[i] * (k ? t[2] : 1));
      }
      colorAttr.needsUpdate = true;
    }

    show(gid, opts) {
      opts = opts || {};
      if (this.failed) return Promise.resolve();
      if (!this.ready) { this._pending = { gid: gid, opts: opts }; return Promise.resolve(); }
      const self = this;
      return getGeometry(gid).then(function (quads) {
        if (self.disposed) return;
        if (self.mesh) {
          self.root.remove(self.mesh);
          self.mesh.geometry.dispose();
          self.mesh = null;
        }
        if (!quads || !quads.length) {
          if (opts.onEmpty) opts.onEmpty();
          return;
        }
        const buffers = buildBuffers(quads, function (url) {
          const tex = texCache.get(url);
          return tex && tex._size ? tex._size : null;
        });
        if (buffers.pending.length) {
          buffers.pending.forEach(function (url) { getTexture(url); });
        }
        const geo = new THREE.BufferGeometry();
        geo.setAttribute('position', new THREE.Float32BufferAttribute(buffers.pos, 3));
        geo.setAttribute('uv', new THREE.Float32BufferAttribute(buffers.uv, 2));
        geo.setAttribute('color', new THREE.Float32BufferAttribute(buffers.col, 3));
        this._baseShade = buffers.base;
        this._tintable = buffers.tt;

        const group = new THREE.Group();
        buffers.groups.forEach(function (entry) {
          const url = entry.url;
          if (!texCache.has(url)) getTexture(url);
          const tex = texCache.get(url);
          const sub = new THREE.BufferGeometry();
          sub.setAttribute('position', geo.getAttribute('position'));
          sub.setAttribute('uv', geo.getAttribute('uv'));
          sub.setAttribute('color', geo.getAttribute('color'));
          sub.setIndex(entry.tris);
          sub.computeBoundingSphere();
          const mat = new THREE.MeshBasicMaterial({
            map: tex,
            vertexColors: true,
            transparent: true,
            alphaTest: 0.02,
            side: THREE.DoubleSide,
            depthWrite: true
          });
          const m = new THREE.Mesh(sub, mat);
          m.frustumCulled = false;
          group.add(m);
        });

        const cx = buffers.center[0], cy = buffers.center[1], cz = buffers.center[2];
        this.fitScale = buffers.fitScale;
        group.position.set(-cx, -cy, -cz);
        // модель в 16 единицах; чуть уменьшим общий масштаб через камеру
        this.root.scale.setScalar(1);
        this.root.add(group);
        this.mesh = group;
        this._geo = geo;
        this._paintColors();
        this.autoRotate = true;
        this.yaw = -0.6;
        this.pitch = 0.42;
        this._applyCamera();
        if (pendingTex.size && this.renderer) {
          setTimeout(function () { self._applyCamera(); }, 60);
        }
        if (opts.onReady) opts.onReady();
      });
    }

    dispose() {
      this.disposed = true;
      if (this._ro) this._ro.disconnect();
      if (this.renderer) this.renderer.dispose();
      texCache.clear();
    }
  }

  global.Viewer3D = Viewer3D;
  global.Viewer3D.buildBuffers = buildBuffers;
  global.KSEPSP_DYE = { colors: DYE_COLORS, list: DYE, swatches: SWATCHES, hexToRgb: hexToRgb };
})(window);
