/**
 * 상단 헤더 메뉴 검색 — screen_list 기반 fuzzy search (List.js)
 */
(function (global) {
  'use strict';

  var API = '/api/screens';
  var listInstance = null;

  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/"/g, '&quot;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  function normalizeUri(uri) {
    if (!uri) return '';
    var p = String(uri).trim();
    if (!p.length) return '';
    if (p.charAt(0) !== '/') p = '/' + p;
    if (p.length > 1 && p.charAt(p.length - 1) === '/') {
      p = p.slice(0, -1);
    }
    return p;
  }

  function sortScreens(screens) {
    return (screens || []).slice().sort(function (a, b) {
      var sa = a.sortOrd != null ? Number(a.sortOrd) : 0;
      var sb = b.sortOrd != null ? Number(b.sortOrd) : 0;
      if (sa !== sb) return sa - sb;
      var na = (a.screenNm || '').toLowerCase();
      var nb = (b.screenNm || '').toLowerCase();
      return na.localeCompare(nb, 'ko');
    });
  }

  function renderMenuItem(screen) {
    var uri = normalizeUri(screen.uriPath);
    var title = screen.screenNm || screen.screenId || uri || '메뉴';
    if (!uri) return '';
    return (
      '<a class="dropdown-item fs--1 px-card py-1 hover-primary" href="' +
      escapeHtml(uri) +
      '" data-menu-path="' +
      escapeHtml(uri) +
      '">' +
      '<div class="d-flex align-items-center">' +
      '<span class="fas fa-circle me-2 text-300 fs--2"></span>' +
      '<div class="flex-1 min-w-0">' +
      '<div class="fw-normal title text-truncate">' +
      escapeHtml(title) +
      '</div>' +
      '<div class="fs--2 text-500 path text-truncate">' +
      escapeHtml(uri) +
      '</div>' +
      '</div></div></a>'
    );
  }

  function bindListFallback(box, list) {
    var fallback = box.querySelector('.fallback');
    if (!fallback || !list) return;
    list.on('updated', function (state) {
      var hasMatch = state.matchingItems && state.matchingItems.length > 0;
      fallback.classList.toggle('d-none', hasMatch);
    });
    list.search('');
  }

  function bindSearchClose(box, list) {
    var closeBtn = box.querySelector('[data-bs-dismiss="search"] button');
    var input = box.querySelector('.fuzzy-search');
    if (!closeBtn || !input) return;
    closeBtn.addEventListener('click', function () {
      input.value = '';
      if (list && list.fuzzySearch) {
        list.fuzzySearch('');
      } else if (list) {
        list.search('');
      }
      var fallback = box.querySelector('.fallback');
      if (fallback) fallback.classList.add('d-none');
    });
  }

  function initFuzzyList(box) {
    if (!global.List || !box) return;
    var listEl = box.querySelector('.list');
    if (!listEl) return;

    box.setAttribute('data-list', JSON.stringify({ valueNames: ['title', 'path'] }));

    if (listInstance && listInstance.listElement === listEl) {
      listInstance.reIndex();
      bindListFallback(box, listInstance);
      bindSearchClose(box, listInstance);
      return;
    }

    listInstance = new global.List(box, {
      valueNames: ['title', 'path'],
      listClass: 'list'
    });
    box._falconList = listInstance;
    bindListFallback(box, listInstance);
    bindSearchClose(box, listInstance);
  }

  function renderScreens(screens) {
    var listEl = global.document.getElementById('header-menu-search-list');
    var box = global.document.getElementById('header-menu-search-box');
    if (!listEl || !box) return;

    var rows = sortScreens(screens).filter(function (s) {
      return s && normalizeUri(s.uriPath);
    });

    if (!rows.length) {
      listEl.innerHTML =
        '<div class="dropdown-item-text text-600 px-card py-2 fs--1">등록된 메뉴가 없습니다.</div>';
      return;
    }

    listEl.innerHTML = rows.map(renderMenuItem).join('');

    var pathApi = global.PrintMallPath || global.PrintMallDoPath;
    var afterPaths =
      pathApi && pathApi.rewritePageLinks
        ? pathApi.rewritePageLinks()
        : Promise.resolve();

    afterPaths.then(function () {
      initFuzzyList(box);
    });
  }

  function loadMenus() {
    var listEl = global.document.getElementById('header-menu-search-list');
    if (!listEl) return;

    global
      .fetch(API, { headers: { Accept: 'application/json' }, credentials: 'same-origin' })
      .then(function (res) {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.json();
      })
      .then(function (data) {
        var screens = (data && data.screens) || [];
        renderScreens(screens);
      })
      .catch(function () {
        listEl.innerHTML =
          '<div class="dropdown-item-text text-danger px-card py-2 fs--1">메뉴를 불러오지 못했습니다.</div>';
      });
  }

  function init() {
    if (!global.document.getElementById('header-menu-search-box')) return;
    loadMenus();
  }

  if (global.document.readyState === 'loading') {
    global.document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})(typeof window !== 'undefined' ? window : globalThis);
