/* KSEPSP-каталог: логика сайта.
   Данные: data/meta.json, data/items-<версия>.json, data/diff.json, data/armor.json, data/geom-index.json */
(function () {
  'use strict';

  const $ = (sel, root) => (root || document).querySelector(sel);
  const $$ = (sel, root) => Array.from((root || document).querySelectorAll(sel));

  const CAT_COLORS = {
    weapons: 'Оружие', tools: 'Инструменты', hats: 'Шапки', food: 'Еда и напитки',
    potions: 'Зелья', furniture: 'Мебель', other: 'Прочее', item: 'Разное'
  };
  const SLOT_RU = { helmet: 'Шлем', chestplate: 'Нагрудник', leggings: 'Штаны', boots: 'Ботинки' };

  const state = {
    version: null, meta: null, items: {}, diff: null, armor: null,
    query: '', cat: '', base: '', sort: 'cat', quick: 'all',
    shown: 0, filtered: [], pageSize: 150,
    models: null, modelQuery: '', modelsShown: 0, modelsSize: 120, modelsSort: 'count',
    armorQuery: '', armorSlot: '',
    filesGroup: 'models', filesKind: 'added'
  };

  // ---------------------------------------------------------------- утилиты
  function esc(s) {
    return String(s == null ? '' : s).replace(/[&<>"']/g, c => (
      { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  }
  function norm(s) {
    return String(s == null ? '' : s).toLowerCase().replace(/ё/g, 'е').replace(/[_\-]+/g, ' ').trim();
  }
  function fmtSize(b) {
    if (b == null) return '';
    if (b > 1024 * 1024) return (b / 1048576).toFixed(1) + ' МБ';
    if (b > 1024) return (b / 1024).toFixed(0) + ' КБ';
    return b + ' Б';
  }
  // Пути в JSON (img/..., armor/...) указаны относительно папки data/
  const D = p => (p ? 'data/' + p : p);

  function intTint(t) {
    if (!t) return null;
    const v = t.type === 'constant' ? t.value : t.default;
    if (v == null) return null;
    return '#' + ((v & 0xFFFFFF).toString(16).padStart(6, '0'));
  }
  function ruName(x) {
    const ru = x.n.find(n => /[А-Яа-яЁё]/.test(n));
    const en = x.n.find(n => !/[А-Яа-яЁё]/.test(n));
    return { ru: ru || x.n[0], en: en || '' };
  }
  function toast(msg) {
    const t = $('#toast');
    t.textContent = msg;
    t.hidden = false;
    clearTimeout(toast._t);
    toast._t = setTimeout(() => { t.hidden = true; }, 2200);
  }
  async function fetchJSON(url) {
    const r = await fetch(url);
    if (!r.ok) throw new Error(url + ': ' + r.status);
    return r.json();
  }

  // ---------------------------------------------------------------- загрузка
  async function boot() {
    state.meta = await fetchJSON('data/meta.json');
    state.version = state.meta.default_version;
    $('#build-info').textContent = state.meta.generated + ' · ' + state.meta.build_seconds + ' c';
    try {
      state.diff = await fetchJSON('data/diff.json');
      $('#tab-diff-count').textContent = '+' + state.diff.counts.added;
    } catch (e) { /* не критично */ }
    await loadItems(state.version);
    applyFilters(true);
    renderAbout();
    // остальные вкладки — по требованию
    bindUI();
  }

  async function loadItems(v) {
    if (state.items[v]) return state.items[v];
    const data = await fetchJSON('data/items-' + v + '.json');
    state.items[v] = data;
    return data;
  }

  async function switchVersion(v) {
    state.version = v;
    $$('.ver').forEach(b => b.classList.toggle('is-active', b.dataset.ver === v));
    if (!state.items[v]) {
      $('#grid').innerHTML = '<div class="skeleton" style="height:150px"></div>'.repeat(6);
      await loadItems(v);
    }
    applyFilters(true);
  }

  // ---------------------------------------------------------------- фильтры
  function renderCatalogFiltersCounts() {
    const items = state.items[state.version] || [];
    const catSel = $('#f-cat');
    const cur = catSel.value;
    catSel.innerHTML = '<option value="">все категории</option>' +
      state.meta.categories.map(c => {
        const n = items.filter(x => x.c === c.id).length;
        return n ? `<option value="${c.id}">${esc(c.label)} — ${n}</option>` : '';
      }).join('');
    catSel.value = cur;
    const baseSel = $('#f-base');
    const cur2 = baseSel.value;
    baseSel.innerHTML = '<option value="">все предметы</option>' +
      state.meta.bases.map(b => `<option value="${b.id}">${esc(b.ru)} (${b.count})</option>`).join('');
    baseSel.value = cur2;
  }

  function matches(x) {
    if (state.cat && x.c !== state.cat) return false;
    if (state.base && x.b !== state.base) return false;
    if (state.quick === 'added' && x.d !== 'added') return false;
    if (state.quick === 'changed' && x.d !== 'changed') return false;
    if (state.quick === 'removed' && x.d !== 'removed') return false;
    if (state.quick === 'renamed' && x.d !== 'renamed') return false;
    if (state.quick === 'tinted' && !x.t) return false;
    if (state.quick === 'multi' && !x.st) return false;
    if (state.quick === 'legacy' && x.era !== '8_0') return false;
    if (state.quick === 'broken' && !x.miss) return false;
    const q = state.query;
    if (q) {
      const nq = norm(q);
      const hay = norm(x.n.join(' ') + ' ' + x.b + ' ' + x.m + ' ' + (x.g || '') + ' ' + (state.meta.categories_ru[x.c] || ''));
      if (hay.indexOf(nq) === -1) return false;
    }
    return true;
  }

  function sortList(list) {
    const byName = (a, b) => ruName(a).ru.localeCompare(ruName(b).ru, 'ru');
    const order = state.meta.categories.map(c => c.id);
    switch (state.sort) {
      case 'name': list.sort(byName); break;
      case 'name-desc': list.sort((a, b) => byName(b, a)); break;
      case 'base': list.sort((a, b) => a.b.localeCompare(b.b) || byName(a, b)); break;
      case 'new': list.sort((a, b) => (a.d ? 0 : 1) - (b.d ? 0 : 1) || byName(a, b)); break;
      default:
        list.sort((a, b) => (order.indexOf(a.c) - order.indexOf(b.c)) || a.b.localeCompare(b.b) || byName(a, b));
    }
    return list;
  }

  function applyFilters(reset) {
    const items = state.items[state.version] || [];
    state.filtered = sortList(items.filter(matches));
    state.shown = 0;
    $('#grid').innerHTML = '';
    $('#empty').hidden = state.filtered.length > 0;
    $('#count-line').textContent = state.filtered.length + ' из ' + items.length + ' переименований';
    renderChunk();
    renderCatalogFiltersCounts();
  }

  function renderCatalogFiltersCounts() {
    const items = state.items[state.version] || [];
    const catSel = $('#f-cat');
    const cur = catSel.value;
    catSel.innerHTML = '<option value="">все категории</option>' +
      state.meta.categories.map(c => {
        const n = items.filter(x => x.c === c.id).length;
        return n ? `<option value="${c.id}">${esc(c.label)} — ${n}</option>` : '';
      }).join('');
    catSel.value = cur;
    const baseSel = $('#f-base');
    const cur2 = baseSel.value;
    baseSel.innerHTML = '<option value="">все предметы</option>' +
      state.meta.bases.map(b => `<option value="${b.id}">${esc(b.ru)} (${b.count})</option>`).join('');
    baseSel.value = cur2;
  }

  function cardHTML(x) {
    const nm = ruName(x);
    const tint = intTint(x.t);
    const tags = [];
    if (x.d === 'added') tags.push('<span class="tag new">NEW</span>');
    if (x.d === 'changed') tags.push('<span class="tag changed">ИЗМЕНЕНО</span>');
    if (x.d === 'removed') tags.push('<span class="tag removed">УДАЛЕНО</span>');
    if (x.d === 'renamed') tags.push('<span class="tag renamed">ДРУГОЕ НАПИСАНИЕ</span>');
    if (x.era === '8_0') tags.push('<span class="tag legacy">легаси 8.0</span>');
    if (x.miss) tags.push('<span class="tag broken">битая текстура</span>');
    if (x.t) tags.push(`<span class="tag tint"><i class="tint-dot" style="background:${tint || '#888'}"></i>перекраска</span>`);
    if (x.st) tags.push(`<span class="tag">состояний: ${x.st.length + 1}</span>`);
    const baseRu = (state.meta.bases.find(b => b.id === x.b) || {}).ru || x.b;
    return `<div class="card" data-k="${esc(x.k)}">
      <div class="card-img"><img loading="lazy" decoding="async" src="${D(x.img)}" alt=""></div>
      <div class="card-name">${esc(nm.ru)}${nm.en ? `<span class="en">${esc(nm.en)}</span>` : ''}</div>
      <div class="card-meta">
        <span class="tag cat-${x.c}">${esc(CAT_COLORS[x.c] || x.c)}</span>
        <span class="tag base">${esc(baseRu)}</span>
        ${tags.join('')}
      </div>
    </div>`;
  }

  function renderChunk() {
    const slice = state.filtered.slice(state.shown, state.shown + state.pageSize);
    if (!slice.length) { $('#load-more').hidden = true; return; }
    $('#grid').insertAdjacentHTML('beforeend', slice.map(cardHTML).join(''));
    state.shown += slice.length;
    $('#load-more').hidden = state.shown >= state.filtered.length;
  }

  // ---------------------------------------------------------------- модалка
  function findItem(k) {
    return (state.items[state.version] || []).find(x => x.k === k);
  }

  function openDetail(k) {
    const x = findItem(k);
    if (!x) return;
    const nm = ruName(x);
    const baseRu = (state.meta.bases.find(b => b.id === x.b) || {}).ru || x.b;
    const tint = intTint(x.t);
    const has3d = !!x.gid;
    const states = x.st || [];

    const html = `
    <div class="detail">
      <div>
        <div class="viewer">
          <canvas class="viewer-canvas" id="vk" ${has3d ? '' : 'hidden'}></canvas>
          <div class="viewer-2d" id="v2d" ${has3d ? 'hidden' : ''}><img src="${D(x.img)}" alt=""></div>
          <div class="viewer-bar">
            <button class="chip is-on" data-view="3d" ${has3d ? '' : 'disabled'}>3D</button>
            <button class="chip" data-view="png">PNG</button>
            <button class="chip" data-view="spin">⟳ вращать</button>
            <button class="chip" data-view="reset">сброс</button>
            <span class="hint">тяни мышью · колесо — зум</span>
          </div>
          ${x.t ? `<details class="tints" open>
            <summary>Перекраска в наковальне: цвет красителя</summary>
            <div class="tint-row">
              <button class="chip is-on" data-c="${tint || '#a06540'}" title="цвет по умолчанию из пака">цвет пака</button>
              <div class="tint-swatches">
                ${KSEPSP_DYE.list.map(d => `<button class="swatch" data-c="${d[1]}" style="background:${d[1]}" title="${d[2]}"></button>`).join('')}
              </div>
              <input type="color" id="tint-input" value="${tint || '#a06540'}" title="свой цвет">
              <span class="muted" id="tint-name">${tintLabel(tint)}</span>
            </div>
          </details>` : ''}
        </div>
        ${x.d === 'changed' ? `
        <div class="d-block"><h4>Было → стало</h4>
          ${x.on ? '' : ''}
          <div class="compare">
            <div><img src="${D(x.dimg)}" alt=""><div class="cap">9.5.1</div><div class="cap"><code>${esc(x.dm || '')}</code></div></div>
            <div class="arrow" style="text-align:center">→</div>
            <div><img src="${D(x.img)}" alt=""><div class="cap">9.6.2</div><div class="cap"><code>${esc(x.m)}</code></div></div>
          </div>
        </div>` : ''}
      </div>
      <div>
        <h3 class="d-title">${esc(nm.ru)}</h3>
        <div class="d-en">${esc(nm.en)}</div>
        <div class="card-meta" style="margin-bottom:12px">
          <span class="tag cat-${x.c}">${esc(CAT_COLORS[x.c] || x.c)}</span>
          ${x.d === 'added' ? '<span class="tag new">новое в 9.6.2</span>' : ''}
          ${x.d === 'changed' ? '<span class="tag changed">модель изменена в 9.6.2</span>' : ''}
          ${x.d === 'removed' ? '<span class="tag removed">удалено в 9.6.2</span>' : ''}
          ${x.d === 'renamed' ? '<span class="tag renamed">в 9.6.2 другое написание</span>' : ''}
          ${x.era === '8_0' ? '<span class="tag legacy">легаси-модель 8.0</span>' : ''}
          ${x.miss ? '<span class="tag broken">есть битая текстура</span>' : ''}
        </div>

        <div class="howto">
          <b>Как получить:</b>
          <ol>
            <li>Возьмите <b>${esc(baseRu)}</b> <span class="muted">(<code>${esc(x.b)}</code>)</span>.</li>
            <li>Переименуйте в наковальне в <code>${esc(nm.ru)}</code>${nm.en ? ` или <code>${esc(nm.en)}</code>` : ''}.</li>
            <li>Нужен ресурспак KSEPSP ${esc(state.version)} (регистр и пробелы важны).</li>
            ${x.d === 'renamed' && x.dn ? `<li class="muted">В версии 9.5.1 это же переименование писалось как <code>${x.dn.map(esc).join('</code> / <code>')}</code></li>` : ''}
          </ol>
        </div>

        <div class="d-block"><h4>Данные</h4>
          <dl class="kv2">
            <dt>Предмет</dt><dd><code>${esc(x.b)}</code></dd>
            <dt>Модель</dt><dd><code>${esc(x.m)}</code></dd>
            ${x.g ? `<dt>Группа в паке</dt><dd><code>${esc(x.g)}</code></dd>` : ''}
            <dt>Категория</dt><dd>${esc(CAT_COLORS[x.c] || x.c)}</dd>
            <dt>Эпоха моделей</dt><dd>${x.era === '8_0' ? '8.0 (легаси)' : x.era === '9_0' ? '9.0 (актуальная)' : '—'}</dd>
            <dt>Файл пака</dt><dd><code>assets/minecraft/items/${esc(x.b)}.json</code></dd>
            ${x.miss ? `<dt>Битые текстуры</dt><dd>${x.miss.map(m => '<code>' + esc(m) + '</code>').join(', ')} <span class="muted">— файла нет в паке</span></dd>` : ''}
          </dl>
        </div>

        ${states.length ? `<div class="d-block"><h4>Состояния (${states.length + 1})</h4>
          <div class="states-grid">
            <div class="state"><img loading="lazy" src="${D(x.img)}" alt=""><div class="c">обычное</div></div>
            ${states.map(s => `<div class="state"><img loading="lazy" src="${D(s.img)}" alt="" title="${esc(s.m)}"><div class="c">${esc(condRu(s.cond))}</div></div>`).join('')}
          </div></div>` : ''}

        ${x.tx && x.tx.length ? `<div class="d-block"><h4>Текстуры (${x.tx.length})</h4>
          <div class="tex-list">${x.tx.slice(0, 40).map(t => {
            const file = 'data/tex/' + t.split(':').join('__').replace(/\//g, '__') + '.png';
            const broken = (x.miss || []).indexOf(t) >= 0;
            const src = broken ? 'data/tex/__missing.png' : file;
            return `<div class="tex-item"><img loading="lazy" src="${src}" onerror="this.src='data/tex/__missing.png'" alt=""><span>${esc(t.split('/').pop())}</span>${broken ? '<span style="color:#ff8b8b">нет файла</span>' : ''}</div>`;
          }).join('')}</div></div>` : ''}
      </div>
    </div>`;

    const mb = $('#modal-body');
    mb.innerHTML = html;
    $('#modal').hidden = false;
    document.body.style.overflow = 'hidden';

    let viewer = null;
    if (has3d) {
      viewer = new Viewer3D($('#vk'), {
        onFail: () => {
          // нет WebGL / three.js не загрузился — показываем PNG
          $('#v2d').hidden = false; $('#vk').hidden = true;
          const b = document.querySelector('#modal [data-view="3d"]');
          if (b) { b.disabled = true; b.title = '3D недоступно в этом браузере'; }
          toast('3D недоступно — показываю картинку');
        }
      });
      viewer.show(x.gid);
      if (x.t) viewer.setTint(tint || '#a06540');
    }

    $$('#modal [data-view]').forEach(b => b.addEventListener('click', () => {
      const mode = b.dataset.view;
      if (mode === 'png') {
        $('#v2d').hidden = false; $('#vk').hidden = true;
      } else if (mode === '3d') {
        if (!viewer) { toast('3D недоступно для этой модели'); return; }
        $('#v2d').hidden = true; $('#vk').hidden = false;
        viewer._resize();
      } else if (mode === 'spin') {
        if (viewer) viewer.autoRotate = !viewer.autoRotate;
      } else if (mode === 'reset') {
        if (viewer) { viewer.zoom = 1; viewer.yaw = -0.6; viewer.pitch = 0.42; viewer.autoRotate = true; }
      }
    }));

    if (x.t && viewer) {
      const inp = $('#tint-input');
      inp.addEventListener('input', () => viewer.setTint(inp.value));
      $$('.swatch, .chip[data-c]', $('#modal')).forEach(sw => sw.addEventListener('click', () => {
        inp.value = sw.dataset.c;
        viewer.setTint(sw.dataset.c);
        const nm = $('#tint-name');
        if (nm) nm.textContent = sw.title || '';
        $$('.swatch, .chip[data-c]', $('#modal')).forEach(b => b.classList.toggle('is-on', b === sw));
      }));
    }
    window._detailViewer = viewer;
  }

  function tintLabel(t) {
    const hex = intTint(t);
    if (!hex) return '';
    const hit = KSEPSP_DYE.list.find(d => d[1].toLowerCase() === hex.toLowerCase());
    return (hit ? hit[2] + ' ' : '') + hex + ' (цвет по умолчанию)';
  }

  function exportList(kind) {
    const list = state.filtered;
    if (!list.length) { toast('Нечего выгружать — пустой список'); return; }
    let text;
    if (kind === 'csv') {
      const rows = [['предмет', 'предмет (рус)', 'название RU', 'название EN', 'модель', 'категория', 'версия', 'статус']];
      list.forEach(x => {
        const n = ruName(x);
        rows.push([x.b, baseRu(x.b), n.ru, n.en, x.m, CAT_COLORS[x.c] || x.c, state.version,
          x.d === 'added' ? 'новое' : x.d === 'changed' ? 'изменено' : x.d === 'removed' ? 'удалено' : '']);
      });
      text = '\ufeff' + rows.map(r => r.map(v => '"' + String(v).replace(/"/g, '""') + '"').join(';')).join('\r\n');
    } else {
      const lines = ['# KSEPSP ' + state.version + ' — названия переименований (' + list.length + ')',
        '# предмет | что написать в наковальне (RU) | (EN) | модель'];
      list.forEach(x => {
        const n = ruName(x);
        lines.push(x.b + ' | ' + n.ru + ' | ' + n.en + ' | ' + x.m);
      });
      text = lines.join('\n');
    }
    const blob = new Blob([text], { type: kind === 'csv' ? 'text/csv;charset=utf-8' : 'text/plain;charset=utf-8' });
    const a = document.createElement('a');
    a.href = URL.createObjectURL(blob);
    a.download = 'ksepsp-' + state.version + '-renames.' + (kind === 'csv' ? 'csv' : 'txt');
    document.body.appendChild(a); a.click(); a.remove();
    toast('Файл ' + a.download + ' сформирован');
  }

  function baseRu(b) {
    return (state.meta.bases.find(x => x.id === b) || {}).ru || b;
  }

  function condRu(c) {
    if (!c) return 'обычное';
    return c
      .replace(/use_duration>=([\d.]+)/g, 'натяжение ≥ $1')
      .replace(/display_context=([^\s]+)/g, (m, p1) => 'вид: ' + p1.split(',').map(v => ({
        gui: 'инвентарь', ground: 'на земле', fixed: 'в рамке', head: 'на голове',
        firstperson_righthand: '1-е лицо', firstperson_lefthand: '1-е лицо (левая)',
        thirdperson_righthand: '3-е лицо', thirdperson_lefthand: '3-е лицо (левая)'
      }[v] || v)).join('/'))
      .replace(/cond:true/g, 'если условие выполнено')
      .replace(/cond:false/g, 'иначе')
      .replace(/ванильная модель/g, 'без пака');
  }

  function closeModal() {
    $('#modal').hidden = true;
    document.body.style.overflow = '';
    if (window._detailViewer) { window._detailViewer.dispose(); window._detailViewer = null; }
    $('#modal-body').innerHTML = '';
  }

  // ---------------------------------------------------------------- сравнение
  async function renderDiff() {
    if (!state.diff) {
      state.diff = await fetchJSON('data/diff.json');
    }
    const d = state.diff;
    const c = d.counts;
    const mc = state.meta.counts;
    $('#diff-summary').innerHTML = [
      card('stat', c.cases_new, 'переименований в 9.6.2', 'info'),
      card('stat', '+' + c.added, 'новых названий', 'good'),
      card('stat', c.changed, 'изменённых (модель/цвет)', 'warn'),
      card('stat', c.renamed || 0, 'с новым написанием', 'warn'),
      card('stat', c.removed, 'больше не работает', 'bad'),
      card('stat', mc.models['9.6.2'] - mc.models['9.5.1'] > 0 ? '+' + (mc.models['9.6.2'] - mc.models['9.5.1']) : (mc.models['9.6.2'] - mc.models['9.5.1']), 'уникальных моделей', 'info'),
      card('stat', mc.armor_sets['9.6.2'], 'наборов брони (в 9.5.1 — 0)', 'good')
    ].join('');

    const added = d.added, changed = d.changed, removed = d.removed;
    $('#diff-added-count').textContent = added.length + ' шт.';
    $('#diff-changed-count').textContent = changed.length + ' шт.';
    $('#diff-removed-count').textContent = removed.length + ' шт.';
    $('#diff-armor-count').textContent = (d.armor_added || []).length + ' шт.';

    const renamed = d.renamed || [];
    $('#diff-renamed-count').textContent = renamed.length + ' шт.';
    $('#diff-renamed').innerHTML = renamed.map(a => `<div class="card changed-card" data-k="${esc(a.k)}">
        <div><div class="card-img"><img loading="lazy" src="${D(a.img)}" alt=""></div><div class="cap">9.5.1: ${esc((a.on || a.n).join(' / '))}</div></div>
        <div class="arrow">→</div>
        <div><div class="card-img"><img loading="lazy" src="${D(a.img)}" alt=""></div><div class="cap">9.6.2: ${esc(a.n.join(' / '))}</div></div>
        <div style="grid-column:1/-1"><div class="card-name">${esc(ruName(a).ru)}<span class="en">${esc(ruName(a).en)}</span></div></div>
      </div>`).join('');
    $('#diff-added').innerHTML = added.map(a => simpleDiffCard(a, 'new')).join('');
    $('#diff-removed').innerHTML = removed.map(a => simpleDiffCard(a, 'removed')).join('');
    $('#diff-changed').innerHTML = changed.map(a => `<div class="card changed-card" data-k="${esc(a.k)}" data-src="changed">
        <div><div class="card-img"><img loading="lazy" src="${D(a.oimg)}" alt=""></div><div class="cap">9.5.1</div></div>
        <div class="arrow">→</div>
        <div><div class="card-img"><img loading="lazy" src="${D(a.img)}" alt=""></div><div class="cap">9.6.2</div></div>
        <div style="grid-column:1/-1">
          <div class="card-name">${esc(ruName({ n: a.n }).ru)}<span class="en">${esc(ruName({ n: a.n }).en)}</span></div>
          <div class="card-meta"><span class="tag base">${esc(a.b)}</span><span class="tag cat-${a.c}">${esc(CAT_COLORS[a.c] || a.c)}</span>
            <span class="tag" title="${esc(a.om || '')}">модель: ${esc((a.om || '').split('/').pop())} → ${esc((a.m || '').split('/').pop())}</span></div>
        </div>
      </div>`).join('');
    $('#diff-armor').innerHTML = (d.armor_added || []).map(a => `
      <div class="armor-card">
        ${a.img ? `<img loading="lazy" src="${D(a.img)}" alt="">` : '<div style="width:64px"></div>'}
        <div><div class="nm">${esc(a.names[0])}</div><div class="en">${esc(a.names[1] || '')}</div>
        <div class="card-meta"><span class="tag">${esc(SLOT_RU[a.slot] || a.slot)}</span><span class="tag">${esc(a.item)}</span></div></div>
      </div>`).join('');
  }

  function card(cls, v, k, tone) {
    return `<div class="stat-card ${tone || ''}"><div class="v">${v}</div><div class="k">${esc(k)}</div></div>`;
  }
  function simpleDiffCard(a, kind) {
    const nm = ruName({ n: a.n });
    return `<div class="card" data-k="${esc(a.k || a.n.join('|'))}">
      <div class="card-img"><img loading="lazy" src="${D(a.img)}" alt=""></div>
      <div class="card-name">${esc(nm.ru)}${nm.en ? `<span class="en">${esc(nm.en)}</span>` : ''}</div>
      <div class="card-meta"><span class="tag ${kind}">${kind === 'new' ? 'NEW' : 'УДАЛЕНО'}</span>
        <span class="tag cat-${a.c}">${esc(CAT_COLORS[a.c] || a.c)}</span><span class="tag base">${esc(a.b)}</span></div>
    </div>`;
  }

  // ---------------------------------------------------------------- броня
  async function renderArmor() {
    if (!state.armor) state.armor = await fetchJSON('data/armor.json');
    const a = state.armor;
    const list = state.version === a.old_v ? (a.old[a.old_v] || []) : (a.new || []);
    const slots = Array.from(new Set(list.map(x => x.slot))).filter(Boolean);
    $('#armor-count').textContent = list.length + ' переименований · версия ' + state.version;
    $('#a-slots').innerHTML = ['<button class="chip is-on" data-slot="">все</button>']
      .concat(slots.map(s => `<button class="chip" data-slot="${esc(s)}">${esc(SLOT_RU[s] || s)}</button>`)).join('');
    const q = norm(state.armorQuery);
    const filtered = list.filter(x => {
      if (state.armorSlot && x.slot !== state.armorSlot) return false;
      if (q && norm(x.names.join(' ') + ' ' + x.set + ' ' + x.item).indexOf(q) === -1) return false;
      return true;
    });
    if (!filtered.length) {
      $('#armor-grid').innerHTML = list.length === 0
        ? `<div class="empty" style="grid-column:1/-1">В версии ${esc(state.version)} брони по переименованию нет — она появилась только в 9.6.2.<br>
           Переключите версию на <b>9.6.2</b> в шапке сайта или откройте вкладку «Сравнение».</div>`
        : '<div class="empty" style="grid-column:1/-1">Ничего не найдено</div>';
    } else $('#armor-grid').innerHTML = filtered.map(x => `
      <div class="armor-card">
        ${x.img ? `<img loading="lazy" src="${D(x.img)}" alt="">` : '<div style="width:64px"></div>'}
        <div>
          <div class="nm">${esc(x.names[0])}</div>
          <div class="en">${esc(x.names[1] || '')}</div>
          <div class="card-meta">
            <span class="tag">${esc(SLOT_RU[x.slot] || x.slot)}</span>
            <span class="tag">${esc(x.item || x.set)}</span>
          </div>
        </div>
      </div>`).join('');

    const sets = (a.sets[state.version] || []);
    $('#armor-sets-count').textContent = sets.length + ' наборов';
    $('#armor-sets').innerHTML = sets.map(s => `
      <div class="set-card">
        <h5>${esc(s.set)}</h5>
        <div class="set-files">${s.files.slice(0, 8).map(f =>
          `<img loading="lazy" src="data/armor/${esc(s.set)}/${esc(f)}" alt="${esc(f)}" title="${esc(f)}" onerror="this.style.display='none'">`).join('')}</div>
      </div>`).join('');
  }

  // ---------------------------------------------------------------- модели
  async function renderModels() {
    const items = state.items[state.version] || await loadItems(state.version);
    if (!state.models || state.modelsVersion !== state.version) {
      const map = new Map();
      items.forEach(x => {
        [x.m].concat((x.st || []).map(s => s.m)).filter(Boolean).forEach(m => {
          if (!map.has(m)) map.set(m, { m: m, items: [], cat: x.c, tx: [], img: null, main: false });
          const e = map.get(m);
          if (e.items.indexOf(x) === -1) e.items.push(x);
          if (m === x.m) { e.main = true; e.img = x.img; e.cat = x.c; e.tx = x.tx || []; }
          else if (!e.img && x.img) e.img = x.img;
          if (!e.tx.length && x.tx) e.tx = x.tx;
        });
      });
      state.models = Array.from(map.values()).sort((a, b) => b.items.length - a.items.length || a.m.localeCompare(b.m));
      state.modelsVersion = state.version;
      state.modelsShown = 0;
    }
    const list = state.models.filter(x => {
      if (!state.modelQuery) return true;
      const q = norm(state.modelQuery);
      return norm(x.m + ' ' + x.items.map(i => i.n.join(' ')).join(' ')).indexOf(q) !== -1;
    });
    $('#models-count').textContent = state.models.length + ' моделей в ' + state.version;
    const shown = list.slice(0, state.modelsShown + state.modelsSize);
    state.modelsShown = shown.length;
    $('#m-count-line').textContent = 'показано ' + shown.length + ' из ' + list.length;
    $('#models-table tbody').innerHTML = shown.map(x => `
      <tr>
        <td><img loading="lazy" src="${D(x.img || '')}" alt=""></td>
        <td><div class="path">${esc(x.m)}</div></td>
        <td><div class="nm">${x.items.length ? x.items.slice(0, 3).map(i => {
      const n = ruName(i);
      return esc(n.ru) + (n.en ? ` <span class="en">${esc(n.en)}</span>` : '');
    }).join('<br>') + (x.items.length > 3 ? `<br><span class="muted">ещё ${x.items.length - 3}…</span>` : '') : '<span class="muted">— (состояние модели)</span>'}</div></td>
        <td>${x.items.length ? '<code>' + esc(x.items[0].b) + '</code>' : ''}</td>
        <td><span class="tag cat-${x.cat}">${esc(CAT_COLORS[x.cat] || x.cat)}</span></td>
        <td><div class="tex-thumbs">${(x.tx || []).slice(0, 6).map(tt => {
      const file = 'data/tex/' + tt.split(':').join('__').replace(/\//g, '__') + '.png';
      return `<img loading="lazy" src="${file}" alt="" title="${esc(tt)}" onerror="this.style.opacity=.2">`;
    }).join('')}</div></td>
      </tr>`).join('');
    $('#m-load-more').hidden = shown.length >= list.length;
  }

  // ---------------------------------------------------------------- файлы
  async function renderFiles() {
    if (!state.diff) state.diff = await fetchJSON('data/diff.json');
    const groups = Object.keys(state.diff.files);
    $('#files-groups').innerHTML = groups.map(g => {
      const f = state.diff.files[g];
      const label = { models: 'модели', textures: 'текстуры', items: 'переименования', swappers: 'броня RPT', cem: 'модели сущностей', other: 'остальное' }[g] || g;
      return `<button class="chip ${g === state.filesGroup ? 'is-on' : ''}" data-group="${g}">${label} (+${f.added} / ~${f.changed} / −${f.removed})</button>`;
    }).join('');
    const f = state.diff.files[state.filesGroup];
    const kind = state.filesKind;
    const list = (f.list[kind] || []);
    $('#files-count').textContent = list.length + ' из ' + f[kind] + ' показано';
    $('#files-list').innerHTML = list.map(e => `
      <div class="file-row">
        <div class="k k-${kind}">${kind === 'added' ? 'ДОБАВЛЕН' : kind === 'changed' ? 'ИЗМЕНЁН' : 'УДАЛЁН'}</div>
        <div><code>${esc(e.path)}</code></div>
        <div class="sz">${fmtSize(e.size)}</div>
      </div>`).join('') || '<div class="empty">пусто</div>';
  }

  // ---------------------------------------------------------------- о паке
  function renderAbout() {
    const m = state.meta;
    const c = m.counts;
    const d = m.version_diff_counts || null;
    const vOld = m.versions[0], vNew = m.versions[1];
    $('#about-versions').innerHTML = [vOld, vNew].map(v => `
      <div class="stat-card ${v.id === m.default_version ? 'good' : ''}">
        <div class="v">${esc(v.id)}</div>
        <div class="k">
          ${esc(v.zip)} · ${fmtSize(v.zip_size)}<br>
          pack_format ${v.mcmeta.pack_format} · файлов ${v.stats.files}<br>
          переименований ${v.counts.named} · брони ${v.counts.armor}
        </div>
      </div>`).join('');

    const diff = {
      'weapons': '+64', 'tools': '+12', 'hats': '+41', 'food': '+1'
    };
    $('#about-diff').innerHTML = `
      <li>Переименований: <b>${c.cases['9.5.1']} → ${c.cases['9.6.2']}</b>, уникальных моделей <b>${c.models['9.5.1']} → ${c.models['9.6.2']}</b>.</li>
      <li>Новых названий: <b id="about-new"></b>, с другими моделями: <b id="about-changed"></b>, пропало: <b id="about-removed"></b>.</li>
      <li>Появилась <b>броня по переименованию</b>: 94 набора текстур и ${c.armor['9.6.2']} названий (новый механизм RPT/RPF, не ванильный).</li>
      <li>Файлов в паке: <b>${c.files['9.5.1']} → ${c.files['9.6.2']}</b> <span class="muted">(модели, текстуры, броня, эмиссивы)</span>.</li>
      <li id="about-files" class="muted"></li>
      <li>Размер архива: ${fmtSize(c.size['9.5.1'])} → ${fmtSize(c.size['9.6.2'])} (без учёта сжатия zip: ${fmtSize(vOld.zip_size)} → ${fmtSize(vNew.zip_size)}).</li>`;
    fetchJSON('data/diff.json').then(dd => {
      $('#about-new').textContent = '+' + dd.counts.added;
      $('#about-changed').textContent = dd.counts.changed;
      $('#about-removed').textContent = dd.counts.removed;
      const an = $('#about-renamed');
      if (an) an.textContent = dd.counts.renamed || 0;
      const fl = $('#about-files');
      if (fl && dd.files) {
        const f = dd.files;
        fl.innerHTML = 'По файлам: модели +' + f.models.added + '/−' + f.models.removed + ', текстуры +' + f.textures.added +
          '/−' + f.textures.removed + ', переопределения предметов +' + f.items.added + '/~' + f.items.changed +
          ', броня RPT +' + f.swappers.added + ', модели сущностей +' + f.cem.added + '.';
      }
    });
  }

  // ---------------------------------------------------------------- UI
  function showTab(name) {
    $$('.tab').forEach(t => t.classList.toggle('is-active', t.dataset.tab === name));
    $$('.panel').forEach(p => p.classList.toggle('is-active', p.id === 'panel-' + name));
    if (name === 'diff') renderDiff();
    if (name === 'armor') renderArmor();
    if (name === 'models') renderModels();
    if (name === 'files') renderFiles();
    window.scrollTo(0, 0);
  }

  function bindUI() {
    let qTimer;
    $('#q').addEventListener('input', e => {
      clearTimeout(qTimer);
      const v = e.target.value;
      qTimer = setTimeout(() => { state.query = v; applyFilters(true); }, 130);
    });
    $('#f-cat').addEventListener('change', e => { state.cat = e.target.value; applyFilters(true); });
    $('#f-base').addEventListener('change', e => { state.base = e.target.value; applyFilters(true); });
    $('#f-sort').addEventListener('change', e => { state.sort = e.target.value; applyFilters(true); });
    $('#quick-filters').addEventListener('click', e => {
      const b = e.target.closest('.chip'); if (!b) return;
      state.quick = b.dataset.f === 'all' ? 'all' : (state.quick === b.dataset.f ? 'all' : b.dataset.f);
      $$('#quick-filters .chip').forEach(c => c.classList.toggle('is-on', c.dataset.f === state.quick));
      applyFilters(true);
    });
    $('#reset').addEventListener('click', () => {
      state.query = ''; state.cat = ''; state.base = ''; state.quick = 'all'; state.sort = 'cat';
      $('#q').value = ''; $('#f-cat').value = ''; $('#f-base').value = ''; $('#f-sort').value = 'cat';
      $$('#quick-filters .chip').forEach(c => c.classList.toggle('is-on', c.dataset.f === 'all'));
      applyFilters(true);
    });
    $('#load-more').addEventListener('click', renderChunk);
    $('#export-txt').addEventListener('click', () => exportList('txt'));
    $('#export-csv').addEventListener('click', () => exportList('csv'));
    $('#grid').addEventListener('click', e => {
      const card = e.target.closest('.card'); if (!card) return;
      if (card.dataset.k) openDetail(card.dataset.k);
    });
    $('#diff-added').addEventListener('click', e => {
      const card = e.target.closest('.card'); if (!card) return;
      openDetailByName(card.dataset.k);
    });
    $('#diff-changed').addEventListener('click', e => {
      const card = e.target.closest('.card'); if (!card) return;
      openDetailByName(card.dataset.k);
    });
    $('#tabs').addEventListener('click', e => {
      const t = e.target.closest('.tab'); if (!t) return;
      showTab(t.dataset.tab);
    });
    $$('.ver').forEach(b => b.addEventListener('click', () => switchVersion(b.dataset.ver)));
    $('#modal').addEventListener('click', e => { if (e.target.closest('[data-close]')) closeModal(); });
    document.addEventListener('keydown', e => { if (e.key === 'Escape' && !$('#modal').hidden) closeModal(); });
    window.addEventListener('scroll', () => {
      if ($('#panel-catalog').classList.contains('is-active') && state.shown < state.filtered.length) {
        const close = window.innerHeight + window.scrollY > document.body.offsetHeight - 700;
        if (close) renderChunk();
      }
    }, { passive: true });
    $('#a-q').addEventListener('input', e => { state.armorQuery = e.target.value; renderArmor(); });
    $('#a-slots').addEventListener('click', e => {
      const b = e.target.closest('.chip'); if (!b) return;
      state.armorSlot = b.dataset.slot;
      $$('#a-slots .chip').forEach(c => c.classList.toggle('is-on', c.dataset.slot === state.armorSlot));
      renderArmor();
    });
    let mTimer;
    $('#m-q').addEventListener('input', e => {
      clearTimeout(mTimer);
      mTimer = setTimeout(() => { state.modelQuery = e.target.value; state.modelsShown = 0; renderModels(); }, 150);
    });
    $('#m-size').addEventListener('change', e => { state.modelsSize = +e.target.value; state.modelsShown = 0; renderModels(); });
    $('#m-load-more').addEventListener('click', () => renderModels());
    $('#files-kinds').addEventListener('click', e => {
      const b = e.target.closest('.chip'); if (!b) return;
      state.filesKind = b.dataset.kind;
      $$('#files-kinds .chip').forEach(c => c.classList.toggle('is-on', c.dataset.kind === state.filesKind));
      renderFiles();
    });
    $('#files-groups').addEventListener('click', e => {
      const b = e.target.closest('.chip'); if (!b) return;
      state.filesGroup = b.dataset.group; renderFiles();
    });
  }

  function openDetailByName(key) {
    const items = state.items[state.version] || [];
    const found = items.find(x => x.k === key) ||
      items.find(x => key.indexOf(x.b + '|') === 0 &&
        x.n.every(n => key.toLowerCase().indexOf(n.toLowerCase()) >= 0));
    if (found) { openDetail(found.k); return; }
    // если в текущей версии нет — предложим другую
    const other = Object.keys(state.items).find(v => (state.items[v] || []).some(x => x.k === key));
    if (other) {
      switchVersion(other).then(() => { const f2 = findItem(key); if (f2) openDetail(key); });
      toast('Это переименование есть в ' + other + ' — переключаю версию');
      return;
    }
    toast('Не нашёл это переименование в паке');
  }

  document.addEventListener('DOMContentLoaded', boot);
})();
