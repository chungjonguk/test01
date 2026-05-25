/**
 * 대시보드 업체 선택 — 세션·구성 연동
 */
(function () {
  'use strict';

  var store = window.DashboardConfigStore;
  var renderer = window.DashboardWidgetRenderer;

  function $(id) {
    return document.getElementById(id);
  }

  function notifyError(message) {
    if (window.Swal) {
      window.Swal.fire({ icon: 'error', title: message, timer: 2800 });
      return;
    }
    window.alert(message);
  }

  function onCompanyChange(select, reloadOnHome) {
    var id = select.value ? parseInt(select.value, 10) : null;
    if (!id || !store) {
      return;
    }
    store
      .selectCompany(id)
      .then(function () {
        if (reloadOnHome && renderer) {
          renderer.apply();
          return;
        }
        if (reloadOnHome) {
          global.location.reload();
        }
      })
      .catch(function () {
        notifyError('업체 구성을 불러오지 못했습니다.');
      });
  }

  function bind() {
    if (document.getElementById('dashboard-config-body')) {
      return;
    }
    var select = $('dashboard-company-select');
    if (!select || !store) {
      return;
    }
    var reloadOnHome = !!document.getElementById('dashboard-widgets-host');
    select.addEventListener('change', function () {
      onCompanyChange(select, reloadOnHome);
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bind);
  } else {
    bind();
  }
})();
