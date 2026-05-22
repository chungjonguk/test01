/**
 * 사용자 접속 로그 조회 — /admin/user-access-logs
 */
(function () {
  'use strict';

  var state = { loading: false };

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
      '<span class="access-log-truncate" title="' +
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

  function successBadge(yn) {
    var v = (yn || 'Y').toUpperCase();
    if (v === 'Y') {
      return '<span class="badge badge-soft-success">Y</span>';
    }
    return '<span class="badge badge-soft-danger">N</span>';
  }

  function accessTypeBadge(type) {
    var t = (type || '').toUpperCase();
    if (t === 'LOGIN') {
      return '<span class="badge badge-soft-primary">LOGIN</span>';
    }
    if (t === 'LOGOUT') {
      return '<span class="badge badge-soft-warning">LOGOUT</span>';
    }
    if (t === 'PAGE') {
      return '<span class="badge badge-soft-info">PAGE</span>';
    }
    return truncateCell(t);
  }

  function formatDateInput(d) {
    var y = d.getFullYear();
    var m = String(d.getMonth() + 1).padStart(2, '0');
    var day = String(d.getDate()).padStart(2, '0');
    return y + '-' + m + '-' + day;
  }

  function setDefaultDateRange() {
    var to = new Date();
    var from = new Date();
    from.setDate(from.getDate() - 30);
    var fromEl = $('search-date-from');
    var toEl = $('search-date-to');
    if (fromEl && !fromEl.value) {
      fromEl.value = formatDateInput(from);
    }
    if (toEl && !toEl.value) {
      toEl.value = formatDateInput(to);
    }
  }

  function getSearchParams() {
    var params = new URLSearchParams();
    var userId = $('search-user-id');
    var accessType = $('search-access-type');
    var loginType = $('search-login-type');
    var successYn = $('search-success-yn');
    var dateFrom = $('search-date-from');
    var dateTo = $('search-date-to');
    if (userId && userId.value.trim()) {
      params.set('userId', userId.value.trim());
    }
    if (accessType && accessType.value) {
      params.set('accessType', accessType.value);
    }
    if (loginType && loginType.value) {
      params.set('loginType', loginType.value);
    }
    if (successYn && successYn.value) {
      params.set('successYn', successYn.value);
    }
    if (dateFrom && dateFrom.value) {
      params.set('start', dateFrom.value);
    }
    if (dateTo && dateTo.value) {
      params.set('end', dateTo.value);
    }
    params.set('limit', '200');
    return params;
  }

  function renderGrid(logs) {
    var tbody = $('access-log-grid-body');
    var countEl = $('access-log-result-count');
    if (!tbody) {
      return;
    }
    var rows = logs || [];
    if (!rows.length) {
      tbody.innerHTML =
        '<tr><td colspan="10" class="text-center text-600 py-4">조회 결과가 없습니다.</td></tr>';
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
        return (
          '<tr>' +
          '<td class="ps-3 align-middle text-center col-no">' +
          (index + 1) +
          '</td>' +
          '<td class="align-middle col-access-dt fs--2">' +
          truncateCell(formatDt(row.accessDt)) +
          '</td>' +
          '<td class="align-middle col-user-id"><code class="fs--2">' +
          escapeHtml(row.userId || '—') +
          '</code></td>' +
          '<td class="align-middle col-user-nm d-none d-md-table-cell">' +
          truncateCell(row.userNm) +
          '</td>' +
          '<td class="align-middle text-center col-access-type">' +
          accessTypeBadge(row.accessTypeCd) +
          '</td>' +
          '<td class="align-middle text-center col-login-type d-none d-lg-table-cell">' +
          truncateCell(row.loginTypeCd) +
          '</td>' +
          '<td class="align-middle text-center col-success">' +
          successBadge(row.successYn) +
          '</td>' +
          '<td class="align-middle col-client-ip d-none d-xl-table-cell">' +
          truncateCell(row.clientIp) +
          '</td>' +
          '<td class="align-middle col-uri d-none d-xl-table-cell">' +
          truncateCell(row.requestUri) +
          '</td>' +
          '<td class="align-middle col-fail d-none d-xxl-table-cell">' +
          truncateCell(row.failReason) +
          '</td>' +
          '</tr>'
        );
      })
      .join('');
  }

  function loadLogs() {
    if (state.loading) {
      return;
    }
    state.loading = true;
    var tbody = $('access-log-grid-body');
    if (tbody) {
      tbody.innerHTML =
        '<tr><td colspan="10" class="text-center text-600 py-4">조회 중...</td></tr>';
    }
    fetch('/api/admin/user-access-logs?' + getSearchParams().toString(), {
      headers: { Accept: 'application/json' }
    })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          throw new Error((result.data && result.data.message) || '조회 실패');
        }
        renderGrid(result.data.logs || []);
      })
      .catch(function (err) {
        if (tbody) {
          tbody.innerHTML =
            '<tr><td colspan="10" class="text-center text-danger py-4">' +
            escapeHtml(err.message || '조회 중 오류가 발생했습니다.') +
            '</td></tr>';
        }
      })
      .finally(function () {
        state.loading = false;
      });
  }

  function resetSearch() {
    var userId = $('search-user-id');
    var accessType = $('search-access-type');
    var loginType = $('search-login-type');
    var successYn = $('search-success-yn');
    var dateFrom = $('search-date-from');
    var dateTo = $('search-date-to');
    if (userId) {
      userId.value = '';
    }
    if (accessType) {
      accessType.value = '';
    }
    if (loginType) {
      loginType.value = '';
    }
    if (successYn) {
      successYn.value = '';
    }
    if (dateFrom) {
      dateFrom.value = '';
    }
    if (dateTo) {
      dateTo.value = '';
    }
    setDefaultDateRange();
  }

  function bindEvents() {
    var form = $('access-log-search-form');
    if (form) {
      form.addEventListener('submit', function (e) {
        e.preventDefault();
        loadLogs();
      });
    }
    var resetBtn = $('btn-reset-access-log-search');
    if (resetBtn) {
      resetBtn.addEventListener('click', function () {
        resetSearch();
        loadLogs();
      });
    }
  }

  window.UserAccessLogMgmtInit = function () {
    setDefaultDateRange();
    bindEvents();
    loadLogs();
  };
})();
