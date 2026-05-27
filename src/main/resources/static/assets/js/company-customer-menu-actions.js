/**
 * 고객 노출 메뉴 — /admin/company-customer-menus
 */
(function () {
  'use strict';

  var API = '/api/admin/company-customer-menus';
  var listEl = document.getElementById('customer-menu-list');
  var msgEl = document.getElementById('customer-menu-message');
  var saveBtn = document.getElementById('btn-save-customer-menus');
  var state = { candidates: [] };

  function showMsg(text, isError) {
    if (!msgEl) return;
    msgEl.className = 'mt-2 fs--1 ' + (isError ? 'text-danger' : 'text-success');
    msgEl.textContent = text;
  }

  function render() {
    if (!listEl) return;
    listEl.innerHTML = state.candidates
      .map(function (c) {
        return (
          '<div class="col-md-6 col-lg-4">' +
          '<div class="form-check">' +
          '<input class="form-check-input customer-menu-cb" type="checkbox" id="cm-' +
          btoa(c.menuPath).replace(/=/g, '') +
          '" data-path="' +
          c.menuPath +
          '" ' +
          (c.enabled ? 'checked' : '') +
          ' />' +
          '<label class="form-check-label" for="cm-' +
          btoa(c.menuPath).replace(/=/g, '') +
          '">' +
          (c.label || c.menuPath) +
          '</label></div></div>'
        );
      })
      .join('');
  }

  function load() {
    fetch(API, { credentials: 'same-origin' })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (r) {
        if (!r.ok) {
          showMsg((r.data && r.data.message) || '조회 실패', true);
          return;
        }
        state.candidates = r.data.candidates || [];
        render();
      })
      .catch(function () {
        showMsg('서버 연결 실패', true);
      });
  }

  function save() {
    var paths = [];
    document.querySelectorAll('.customer-menu-cb:checked').forEach(function (cb) {
      paths.push(cb.getAttribute('data-path'));
    });
    fetch(API, {
      method: 'PUT',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ menuPaths: paths })
    })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (r) {
        if (!r.ok) {
          showMsg((r.data && r.data.message) || '저장 실패', true);
          return;
        }
        state.candidates = r.data.candidates || state.candidates;
        render();
        showMsg(r.data.message || '저장되었습니다.', false);
      })
      .catch(function () {
        showMsg('저장 중 오류', true);
      });
  }

  if (saveBtn) {
    saveBtn.addEventListener('click', save);
  }
  load();
})();
