/**
 * PrintMall 공통 프론트 유틸 — {@code window.PrintMallCommon}
 * layout.html 전역 로드.
 */
(function () {
  'use strict';

  /**
   * @param {string} id in: element id
   * @returns {HTMLElement|null} out: DOM 요소
   */
  function $(id) {
    return document.getElementById(id);
  }

  /**
   * @param {*} value in: 임의 값
   * @returns {string} out: HTML 이스케이프 문자열
   */
  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  /**
   * @param {*} value in: 속성에 넣을 값
   * @returns {string} out: HTML 속성 이스케이프 문자열
   */
  function escapeAttr(value) {
    return escapeHtml(value).replace(/'/g, '&#39;');
  }

  /**
   * @param {string} url in: 요청 URL
   * @param {RequestInit} [options] in: fetch 옵션 (body에 FormData 가능)
   * @returns {Promise<{ok: boolean, status: number, data: object}>} out: JSON 파싱 결과
   */
  function fetchJson(url, options) {
    options = options || {};
    var headers = Object.assign({ Accept: 'application/json' }, options.headers || {});
    if (options.body && !(options.body instanceof FormData)) {
      headers['Content-Type'] = headers['Content-Type'] || 'application/json';
    }
    options.headers = headers;
    return fetch(url, options).then(function (res) {
      return res.json().then(function (data) {
        return { ok: res.ok, status: res.status, data: data };
      });
    });
  }

  /**
   * @param {string} message in: 알림 메시지
   * @param {string} [icon] in: SweetAlert 아이콘 (success|error|info 등)
   */
  function notify(message, icon) {
    if (window.Swal) {
      window.Swal.fire({
        toast: true,
        position: 'top-end',
        icon: icon || 'info',
        title: message,
        showConfirmButton: false,
        timer: 2800,
        timerProgressBar: true
      });
      return;
    }
    window.alert(message);
  }

  /**
   * @param {*} value in: ISO 문자열·배열·Date 표현
   * @returns {string} out: {@code yyyy-MM-dd HH:mm:ss} 또는 {@code —}
   */
  function formatDt(value) {
    if (value == null || value === '') {
      return '—';
    }
    var s = String(value);
    if (Array.isArray(value) && value.length >= 3) {
      var pad = function (n) {
        return n < 10 ? '0' + n : String(n);
      };
      return (
        value[0] +
        '-' +
        pad(value[1]) +
        '-' +
        pad(value[2]) +
        ' ' +
        pad(value[3] || 0) +
        ':' +
        pad(value[4] || 0) +
        ':' +
        pad(value[5] || 0)
      );
    }
    if (s.indexOf('T') !== -1) {
      return s.replace('T', ' ').slice(0, 19);
    }
    return s.length > 19 ? s.slice(0, 19) : s;
  }

  /**
   * @param {string} groupCode in: 코드 그룹 (예: PRODUCT_CATEGORY)
   * @param {string} code in: 코드값
   * @returns {string} out: 한글 라벨 또는 코드값
   */
  function codeLabel(groupCode, code) {
    if (!code) {
      return '—';
    }
    var opts = window.__CODE_OPTIONS__ && window.__CODE_OPTIONS__[groupCode];
    if (opts) {
      for (var i = 0; i < opts.length; i++) {
        if (opts[i].value === code) {
          return opts[i].label || code;
        }
      }
    }
    return code;
  }

  /**
   * @param {URLSearchParams|Object|string|Function|null} query in: 쿼리 또는 생성 함수
   * @returns {string} out: {@code ?key=val} 접미사 (없으면 빈 문자열)
   */
  function toQuerySuffix(query) {
    if (query == null) {
      return '';
    }
    if (typeof query === 'function') {
      return toQuerySuffix(query());
    }
    if (query instanceof URLSearchParams) {
      var params = query.toString();
      return params ? '?' + params : '';
    }
    if (typeof query === 'string') {
      if (!query) {
        return '';
      }
      return query.indexOf('?') === 0 ? query : '?' + query;
    }
    if (typeof query === 'object') {
      var search = new URLSearchParams();
      Object.keys(query).forEach(function (key) {
        var val = query[key];
        if (val !== undefined && val !== null && String(val).length > 0) {
          search.set(key, val);
        }
      });
      var built = search.toString();
      return built ? '?' + built : '';
    }
    return '';
  }

  window.PrintMallCommon = {
    $: $,
    escapeHtml: escapeHtml,
    escapeAttr: escapeAttr,
    fetchJson: fetchJson,
    notify: notify,
    formatDt: formatDt,
    codeLabel: codeLabel,
    toQuerySuffix: toQuerySuffix
  };
})();
