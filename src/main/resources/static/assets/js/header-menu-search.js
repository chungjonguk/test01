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

  function stripDoSuffix(path) {
    if (!path) return '';
    var p = String(path).trim();
    if (p.length > 3 && p.slice(-3) === '.do') {
      p = p.slice(0, -3);
    }
    if (p === '/index') return '/';
    return p;
  }

  function normalizeUri(uri) {
    if (!uri) return '';
    var p = String(uri).trim();
    if (!p.length) return '';
    if (p.charAt(0) !== '/') p = '/' + p;
    if (p.length > 1 && p.charAt(p.length - 1) === '/') {
      p = p.slice(0, -1);
    }
    return stripDoSuffix(p);
  }

  function resolveHref(screen) {
    if (screen.href) return screen.href;
    return normalizeUri(screen.linkPath || screen.uriPath);
  }

  function resolveMenuPath(screen) {
    return normalizeUri(screen.linkPath || screen.uriPath);
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

  function isCurrentMenuPath(menuPath) {
    var ctx = global.__PAGE_MENU_CONTEXT__;
    if (!ctx || !ctx.pathKey || !menuPath) {
      return false;
    }
    return normalizeUri(ctx.pathKey) === normalizeUri(menuPath);
  }

  function renderMenuItem(screen) {
    var href = resolveHref(screen);
    var menuPath = resolveMenuPath(screen);
    var title = screen.screenNm || screen.screenId || menuPath || '메뉴';
    if (!href) return '';
    var currentCls = isCurrentMenuPath(menuPath) ? ' bg-200 fw-semi-bold' : '';
    return (
      '<a class="dropdown-item fs--1 px-card py-1 hover-primary' +
      currentCls +
      '" href="' +
      escapeHtml(href) +
      '" data-menu-path="' +
      escapeHtml(menuPath) +
      '">' +
      '<div class="d-flex align-items-center">' +
      '<span class="fas fa-circle me-2 text-300 fs--2"></span>' +
      '<div class="flex-1 min-w-0">' +
      '<div class="fw-normal title text-truncate">' +
      escapeHtml(title) +
      '</div>' +
      '<div class="fs--2 text-500 path text-truncate">' +
      escapeHtml(menuPath) +
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

  function bindSearchNavigation(box) {
    if (!box || box.getAttribute('data-search-nav-bound') === 'true') return;
    box.setAttribute('data-search-nav-bound', 'true');
    box.addEventListener('click', function (e) {
      var link = e.target.closest('a.dropdown-item[href^="/"]');
      if (!link) return;
      var href = link.getAttribute('href');
      if (!href || href.indexOf('/e/') === 0) return;
      var pathApi = global.PrintMallPath || global.PrintMallDoPath;
      if (!pathApi || !pathApi.toPublic) return;
      e.preventDefault();
      pathApi.toPublic(href).then(function (enc) {
        if (enc) global.location.assign(enc);
      });
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
      return s && resolveHref(s);
    });

    if (!rows.length) {
      listEl.innerHTML =
        '<div class="dropdown-item-text text-600 px-card py-2 fs--1">등록된 메뉴가 없습니다.</div>';
      return;
    }

    listEl.innerHTML = rows.map(renderMenuItem).join('');
    bindSearchNavigation(box);

    var pathApi = global.PrintMallPath || global.PrintMallDoPath;
    var needsRewrite = rows.some(function (s) {
      return !s.href;
    });
    var afterPaths =
      needsRewrite && pathApi && pathApi.rewritePageLinks
        ? pathApi.rewritePageLinks()
        : Promise.resolve();

    afterPaths.then(function () {
      initFuzzyList(box);
    });
  }

  function isAppLoggedIn() {
    var main = global.document.querySelector('main.main[data-app-logged-in]');
    if (!main) {
      return true;
    }
    return main.getAttribute('data-app-logged-in') === 'true';
  }

  function loadMenus() {
    var listEl = global.document.getElementById('header-menu-search-list');
    if (!listEl) return;
    if (!isAppLoggedIn()) {
      listEl.innerHTML =
        '<div class="dropdown-item-text text-600 px-card py-2 fs--1">로그인 후 메뉴 검색을 사용할 수 있습니다.</div>';
      return;
    }

    global
      .fetch(API, { headers: { Accept: 'application/json' }, credentials: 'same-origin' })
      .then(function (res) {
        if (res.status === 401 || res.status === 403) {
          return { authError: true, status: res.status };
        }
        if (!res.ok) {
          throw new Error('HTTP ' + res.status);
        }
        return res.json();
      })
      .then(function (data) {
        if (data && data.authError) {
          listEl.innerHTML =
            '<div class="dropdown-item-text text-danger px-card py-2 fs--1">세션이 만료되었습니다. 다시 로그인해 주세요.</div>';
          return;
        }
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
