/**
 * 알림 등록 폼 — /api/social/notifications
 */
(function (global) {
  'use strict';

  var API = '/api/social/notifications';

  function setStatus(msg, isError) {
    var el = global.document.getElementById('notification-register-status');
    if (!el) return;
    el.textContent = msg || '';
    el.classList.toggle('text-danger', !!isError);
    el.classList.toggle('text-success', !!msg && !isError);
  }

  function submitForm(e) {
    e.preventDefault();
    var form = global.document.getElementById('notification-register-form');
    if (!form) return;

    var messageEl = global.document.getElementById('notification-message');
    var message = messageEl ? messageEl.value.trim() : '';
    if (!message) {
      setStatus('알림 내용을 입력하세요.', true);
      if (messageEl) messageEl.focus();
      return;
    }

    var payload = {
      senderNm: val('notification-sender-nm'),
      userNm: val('notification-user-nm'),
      message: message,
      sectionCd: val('notification-section') || 'NEW',
      timeIcon: val('notification-time-icon') || '📢'
    };

    var btn = global.document.getElementById('notification-register-submit');
    if (btn) btn.disabled = true;
    setStatus('등록 중…', false);

    global.fetch(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      credentials: 'same-origin',
      body: JSON.stringify(payload)
    })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (result) {
        if (!result.ok || !result.data.success) {
          throw new Error((result.data && result.data.message) || '등록에 실패했습니다.');
        }
        setStatus(result.data.message || '등록되었습니다.', false);
        form.reset();
        var icon = global.document.getElementById('notification-time-icon');
        if (icon) icon.value = '📢';
        var section = global.document.getElementById('notification-section');
        if (section) section.value = 'NEW';
        if (global.PrintMallNotificationList && global.PrintMallNotificationList.reload) {
          global.PrintMallNotificationList.reload();
        }
        if (global.PrintMallNotificationNav && global.PrintMallNotificationNav.refreshFromApi) {
          return global.PrintMallNotificationNav.refreshFromApi();
        }
      })
      .catch(function (err) {
        setStatus(err.message || '등록에 실패했습니다.', true);
      })
      .finally(function () {
        if (btn) btn.disabled = false;
      });
  }

  function val(id) {
    var el = global.document.getElementById(id);
    if (!el || !el.value) return '';
    return String(el.value).trim();
  }

  function init() {
    var form = global.document.getElementById('notification-register-form');
    if (form) {
      form.addEventListener('submit', submitForm);
    }
  }

  if (global.document.readyState === 'loading') {
    global.document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})(typeof window !== 'undefined' ? window : globalThis);
