/**
 * 알림 목록 화면 — 조회(필터) 후 아이콘 리스트에 반영
 */
(function (global) {
  'use strict';

  var API = '/api/social/notifications';
  var state = { loading: false };

  function $(id) {
    return global.document.getElementById(id);
  }

  function getSearchParams() {
    var params = new URLSearchParams();
    params.set('grid', 'true');
    var user = $('search-notification-user');
    var sender = $('search-notification-sender');
    var readYn = $('search-notification-read');
    var keyword = $('search-notification-keyword');
    if (user && user.value.trim()) params.set('userNm', user.value.trim());
    if (sender && sender.value.trim()) params.set('senderNm', sender.value.trim());
    if (readYn && readYn.value) params.set('readYn', readYn.value);
    if (keyword && keyword.value.trim()) params.set('keyword', keyword.value.trim());
    return params;
  }

  function setLoading(loading) {
    state.loading = loading;
    var btn = $('btn-search-notifications');
    if (btn) btn.disabled = loading;
  }

  function loadList() {
    if (state.loading) return Promise.resolve();
    setLoading(true);
    var url = API + '?' + getSearchParams().toString();
    return global
      .fetch(url, { headers: { Accept: 'application/json' }, credentials: 'same-origin' })
      .then(function (res) {
        return res.json();
      })
      .then(function (data) {
        if (!data || !data.success) {
          throw new Error((data && data.message) || '조회에 실패했습니다.');
        }
        var nav = global.PrintMallNotificationNav;
        if (nav && nav.applyExternalItems) {
          nav.applyExternalItems(data.items || []);
        } else if (nav && nav.refresh) {
          nav.refresh();
        }
      })
      .catch(function (err) {
        alert(err.message || '조회에 실패했습니다.');
      })
      .finally(function () {
        setLoading(false);
      });
  }

  function resetSearch() {
    var form = $('notification-search-form');
    if (form) form.reset();
    if (global.PrintMallNotificationNav && global.PrintMallNotificationNav.refreshFromApi) {
      global.PrintMallNotificationNav.refreshFromApi();
    } else {
      loadList();
    }
  }

  function bind() {
    var searchForm = $('notification-search-form');
    if (searchForm) {
      searchForm.addEventListener('submit', function (e) {
        e.preventDefault();
        loadList();
      });
    }
    var resetBtn = $('btn-reset-notification-search');
    if (resetBtn) resetBtn.addEventListener('click', resetSearch);
  }

  function init() {
    bind();
  }

  global.PrintMallNotificationList = {
    reload: loadList
  };

  if (global.document.readyState === 'loading') {
    global.document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})(typeof window !== 'undefined' ? window : globalThis);
