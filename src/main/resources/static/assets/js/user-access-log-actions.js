/**
 * 사용자 접속 로그 — /admin/user-access-logs
 * API: /api/admin/user-access-logs
 * @module user-access-log-actions
 */

(function () {

  'use strict';

  if (!window.PrintMallCommon) {
    window.UserAccessLogMgmtInit = function () {
      var tbody = document.getElementById('access-log-grid-body');
      if (tbody) {
        tbody.innerHTML =
          '<tr><td colspan="12" class="text-center text-danger py-4">공통 스크립트(app-common.js)를 불러오지 못했습니다.</td></tr>';
      }
    };
    return;
  }

  var C = window.PrintMallCommon;

  var $ = C.$;

  var escapeHtml = C.escapeHtml;

  var escapeAttr = C.escapeAttr;

  var formatDt = C.formatDt;

  var accessLogGridView = null;

  var fetchJson = C.fetchJson;



  var state = { loading: false };



  // --- 그리드 렌더 ---



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



  function deviceTypeBadge(type) {

    var t = (type || '').toUpperCase();

    if (t === 'MOBILE') {

      return '<span class="badge badge-soft-info">MOBILE</span>';

    }

    if (t === 'TABLET') {

      return '<span class="badge badge-soft-primary">TABLET</span>';

    }

    if (t === 'DESKTOP') {

      return '<span class="badge badge-soft-secondary">DESKTOP</span>';

    }

    return truncateCell(t || '—');

  }



  function formatDeviceInfo(row) {

    var parts = [];

    if (row.deviceOs) {

      parts.push(row.deviceOs);

    }

    if (row.deviceBrowser) {

      parts.push(row.deviceBrowser);

    }

    if (row.deviceModel) {

      parts.push(row.deviceModel);

    }

    var summary = parts.join(' · ');

    if (!summary) {

      return truncateCell(row.userAgent);

    }

    return truncateCell(summary);

  }



  function getAccessLogGridView() {
    if (!accessLogGridView && C.createGridViewPager) {
      accessLogGridView = C.createGridViewPager({
        pagerRootId: 'access-log-grid-pager',
        containerId: 'access-log-grid-body',
        countElId: 'access-log-result-count',
        emptyColspan: 12,
        renderPage: paintAccessLogRows
      });
    }
    return accessLogGridView;
  }

  function paintAccessLogRows(rows, meta) {
    var tbody = $('access-log-grid-body');
    if (!tbody || !rows.length) {
      return;
    }
    tbody.innerHTML = rows
      .map(function (row, index) {
        return (
          '<tr>' +
          '<td class="ps-3 align-middle text-center col-no">' +
          (C.gridRowNo ? C.gridRowNo(meta, index) : index + 1) +

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

          '<td class="align-middle col-client-ip"><code class="fs--2">' +

          escapeHtml(row.clientIp || '—') +

          '</code></td>' +

          '<td class="align-middle col-device d-none d-lg-table-cell">' +

          deviceTypeBadge(row.deviceTypeCd) +

          ' ' +

          formatDeviceInfo(row) +

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

  function renderGrid(logs) {
    var view = getAccessLogGridView();
    if (view) {
      view.setData(logs || []);
    }
  }

  // --- 검색·조회 ---



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



  function getSearchParams(includeDates) {

    var params = new URLSearchParams();

    var userId = $('search-user-id');

    var clientIp = $('search-client-ip');

    var accessType = $('search-access-type');

    var loginType = $('search-login-type');

    var successYn = $('search-success-yn');

    var dateFrom = $('search-date-from');

    var dateTo = $('search-date-to');

    if (userId && userId.value.trim()) {

      params.set('userId', userId.value.trim());

    }

    if (clientIp && clientIp.value.trim()) {

      params.set('clientIp', clientIp.value.trim());

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

    if (includeDates !== false) {
      if (dateFrom && dateFrom.value) {
        params.set('start', dateFrom.value);
      }
      if (dateTo && dateTo.value) {
        params.set('end', dateTo.value);
      }
    }

    params.set('limit', '200');

    return params;

  }



  /**
   * 검색 조건으로 접속 로그를 API에서 조회합니다.
   * @param {{useDates?: boolean}} [options] useDates=false 이면 서버 기준 최근 30일
   * @returns {void}
   */
  function loadLogs(options) {

    options = options || {};
    var useDates = options.useDates !== false;
    if (state.loading) {

      return;

    }

    state.loading = true;

    var tbody = $('access-log-grid-body');

    if (tbody) {

      tbody.innerHTML =

        '<tr><td colspan="12" class="text-center text-600 py-4">조회 중...</td></tr>';

    }

    fetchJson('/api/admin/user-access-logs?' + getSearchParams(useDates).toString())

      .then(function (result) {

        if (!result.ok) {

          throw new Error((result.data && result.data.message) || '조회 실패');

        }

        renderGrid(result.data.logs || []);

      })

      .catch(function (err) {

        if (tbody) {

          tbody.innerHTML =

            '<tr><td colspan="12" class="text-center text-danger py-4">' +

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

    var clientIp = $('search-client-ip');

    var accessType = $('search-access-type');

    var loginType = $('search-login-type');

    var successYn = $('search-success-yn');

    var dateFrom = $('search-date-from');

    var dateTo = $('search-date-to');

    if (userId) {

      userId.value = '';

    }

    if (clientIp) {

      clientIp.value = '';

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

        loadLogs({ useDates: true });

      });

    }

    var resetBtn = $('btn-reset-access-log-search');

    if (resetBtn) {

      resetBtn.addEventListener('click', function () {

        resetSearch();

        loadLogs({ useDates: false });

      });

    }

  }



  /**
   * 사용자 접속 로그 화면 초기화 — 기본 기간·검색·목록 조회 바인딩.
   * @returns {void}
   */
  window.UserAccessLogMgmtInit = function () {

    setDefaultDateRange();

    bindEvents();

    loadLogs({ useDates: false });

  };

})();


