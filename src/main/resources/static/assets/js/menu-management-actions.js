/**
 * 메뉴(화면) 관리 — screen_list 조회 전용
 */
(function () {
  'use strict';

  var state = {
    loading: false
  };

  function $(id) {
    return document.getElementById(id);
  }

  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/"/g, '&quot;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  function escapeAttr(value) {
    return escapeHtml(value).replace(/'/g, '&#39;');
  }

  function truncateCell(text) {
    var value = text == null ? '' : String(text);
    return (
      '<span class="menu-mgmt-truncate" title="' +
      escapeAttr(value) +
      '">' +
      escapeHtml(value || '—') +
      '</span>'
    );
  }

  function formatDt(value) {
    if (value == null || value === '') {
      return '—';
    }
    var s = String(value);
    if (s.indexOf('T') !== -1) {
      return s.replace('T', ' ').slice(0, 19);
    }
    return s.length > 19 ? s.slice(0, 19) : s;
  }

  function displayName(row) {
    if (row.screenNm != null && String(row.screenNm).trim() !== '') {
      return String(row.screenNm).trim();
    }
    if (row.screen_nm != null && String(row.screen_nm).trim() !== '') {
      return String(row.screen_nm).trim();
    }
    var uri = row.uriPath || row.uri_path || '';
    if (!uri) {
      return '—';
    }
    var segment = uri;
    var slash = segment.lastIndexOf('/');
    if (slash >= 0 && slash < segment.length - 1) {
      segment = segment.substring(slash + 1);
    }
    return segment.replace(/-/g, ' ') || uri;
  }

  function useYnBadge(useYn) {
    var yn = (useYn || 'Y').toUpperCase();
    if (yn === 'Y') {
      return '<span class="badge badge-soft-success">Y</span>';
    }
    return '<span class="badge badge-soft-secondary">N</span>';
  }

  function getSearchParams() {
    var params = new URLSearchParams();
    var screenId = $('search-screen-id');
    var screenNm = $('search-screen-nm');
    var uriPath = $('search-uri-path');
    var useYn = $('search-use-yn');
    if (screenId && screenId.value.trim()) {
      params.set('screenId', screenId.value.trim());
    }
    if (screenNm && screenNm.value.trim()) {
      params.set('screenNm', screenNm.value.trim());
    }
    if (uriPath && uriPath.value.trim()) {
      params.set('uriPath', uriPath.value.trim());
    }
    if (useYn && useYn.value) {
      params.set('useYn', useYn.value);
    }
    return params;
  }

  function renderGrid(screens) {
    var tbody = $('menu-grid-body');
    var countEl = $('menu-search-result-count');
    if (!tbody) {
      return;
    }
    var rows = screens || [];
    if (!rows.length) {
      tbody.innerHTML =
        '<tr><td colspan="8" class="text-center text-600 py-4">조회 결과가 없습니다.</td></tr>';
      if (countEl) {
        countEl.textContent = '0건';
      }
      return;
    }
    if (countEl) {
      countEl.textContent = rows.length + '건';
    }
    tbody.innerHTML = rows
      .map(function (row, index) {
        var screenId = row.screenId || '';
        var screenNm = displayName(row);
        var uriPath = row.uriPath || '';
        return (
          '<tr>' +
          '<td class="ps-3 align-middle text-center col-no">' +
          (index + 1) +
          '</td>' +
          '<td class="align-middle col-screen-id"><code class="fs--2 menu-mgmt-truncate d-block">' +
          escapeHtml(screenId) +
          '</code></td>' +
          '<td class="align-middle col-screen-nm">' +
          truncateCell(screenNm) +
          '</td>' +
          '<td class="align-middle col-uri">' +
          truncateCell(uriPath) +
          '</td>' +
          '<td class="align-middle text-center col-sort">' +
          escapeHtml(row.sortOrd) +
          '</td>' +
          '<td class="align-middle text-center col-use">' +
          useYnBadge(row.useYn) +
          '</td>' +
          '<td class="align-middle fs--2 col-update-id d-none d-xl-table-cell">' +
          truncateCell(row.updateId) +
          '</td>' +
          '<td class="align-middle fs--2 col-update-dt d-none d-xl-table-cell">' +
          truncateCell(formatDt(row.updateDt)) +
          '</td>' +
          '</tr>'
        );
      })
      .join('');
  }

  function loadMenus() {
    if (state.loading) {
      return;
    }
    state.loading = true;
    var tbody = $('menu-grid-body');
    if (tbody) {
      tbody.innerHTML =
        '<tr><td colspan="8" class="text-center text-600 py-4">조회 중...</td></tr>';
    }
    fetch('/api/admin/menus?' + getSearchParams().toString(), {
      headers: { Accept: 'application/json' }
    })
      .then(function (res) {
        if (!res.ok) {
          throw new Error('조회 실패');
        }
        return res.json();
      })
      .then(function (data) {
        renderGrid(data.screens || []);
      })
      .catch(function () {
        if (tbody) {
          tbody.innerHTML =
            '<tr><td colspan="8" class="text-center text-danger py-4">조회 중 오류가 발생했습니다.</td></tr>';
        }
      })
      .finally(function () {
        state.loading = false;
      });
  }

  function resetSearch() {
    var screenId = $('search-screen-id');
    var screenNm = $('search-screen-nm');
    var uriPath = $('search-uri-path');
    var useYn = $('search-use-yn');
    if (screenId) {
      screenId.value = '';
    }
    if (screenNm) {
      screenNm.value = '';
    }
    if (uriPath) {
      uriPath.value = '';
    }
    if (useYn) {
      useYn.value = '';
    }
  }

  function bindEvents() {
    var form = $('menu-search-form');
    if (form) {
      form.addEventListener('submit', function (e) {
        e.preventDefault();
        loadMenus();
      });
    }
    var resetBtn = $('btn-reset-menu-search');
    if (resetBtn) {
      resetBtn.addEventListener('click', function () {
        resetSearch();
        loadMenus();
      });
    }
  }

  window.MenuMgmtInit = function () {
    bindEvents();
    loadMenus();
  };
})();
