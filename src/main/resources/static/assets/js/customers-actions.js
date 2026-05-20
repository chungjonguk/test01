/**
 * 고객 목록 — New / Filter / Export / Bulk Apply
 */
(function () {
  'use strict';

  function getList() {
    var root = document.getElementById('customersTable');
    return root && root._falconList ? root._falconList : null;
  }

  function notify(message, icon) {
    if (window.Swal) {
      window.Swal.fire({
        toast: true,
        position: 'top-end',
        icon: icon || 'info',
        title: message,
        showConfirmButton: false,
        timer: 2500,
        timerProgressBar: true
      });
      return;
    }
    window.alert(message);
  }

  function applyFilters() {
    var list = getList();
    if (!list) {
      return;
    }
    var keywordEl = document.getElementById('customer-filter-keyword');
    var keyword = keywordEl ? keywordEl.value.trim().toLowerCase() : '';
    list.filter(function (item) {
      if (!keyword) {
        return true;
      }
      var values = item.values();
      var rowText = Object.keys(values)
        .map(function (k) {
          return values[k];
        })
        .join(' ')
        .toLowerCase();
      return rowText.indexOf(keyword) !== -1;
    });
  }

  function resetFilters() {
    var keywordEl = document.getElementById('customer-filter-keyword');
    if (keywordEl) {
      keywordEl.value = '';
    }
    var list = getList();
    if (list) {
      list.filter();
      list.search();
    }
  }

  function csvEscape(value) {
    var text = String(value).replace(/\s+/g, ' ').trim();
    if (/[",\n\r]/.test(text)) {
      return '"' + text.replace(/"/g, '""') + '"';
    }
    return text;
  }

  function exportCsv() {
    var list = getList();
    var items = list && list.matchingItems && list.matchingItems.length ? list.matchingItems : null;
    if (!items || !items.length) {
      var rows = document.querySelectorAll('#table-customers-body tr');
      if (!rows.length) {
        notify('보낼 고객이 없습니다.', 'warning');
        return;
      }
      items = Array.prototype.slice.call(rows).map(function (tr) {
        return {
          values: function () {
            return {
              name: (tr.querySelector('.name') || {}).innerText || '',
              email: (tr.querySelector('.email') || {}).innerText || '',
              phone: (tr.querySelector('.phone') || {}).innerText || '',
              address: (tr.querySelector('.address') || {}).innerText || '',
              joined: (tr.querySelector('.joined') || {}).innerText || ''
            };
          }
        };
      });
    }
    var lines = ['Name,Email,Phone,Billing Address,Joined'];
    items.forEach(function (item) {
      var v = item.values();
      var row = [v.name, v.email, v.phone, v.address, v.joined].map(csvEscape).join(',');
      lines.push(row);
    });
    var blob = new Blob(['\uFEFF' + lines.join('\r\n')], { type: 'text/csv;charset=utf-8;' });
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = 'customers-' + new Date().toISOString().slice(0, 10) + '.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    notify('CSV 파일을 다운로드했습니다.', 'success');
  }

  function onBulkApply() {
    var select = document.querySelector('#table-customers-actions select');
    var action = select ? select.value : '';
    if (!action) {
      notify('Bulk action을 선택하세요.', 'warning');
      return;
    }
    var checked = document.querySelectorAll('#table-customers-body [data-bulk-select-row]:checked');
    if (!checked.length) {
      notify('선택된 고객이 없습니다.', 'warning');
      return;
    }
    notify(checked.length + '명에게 "' + action + '" 적용 (데모)', 'success');
  }

  function bind() {
    var exportBtn = document.getElementById('customers-export-btn');
    if (exportBtn) {
      exportBtn.addEventListener('click', exportCsv);
    }
    var applyFilterBtn = document.getElementById('customer-filter-apply');
    if (applyFilterBtn) {
      applyFilterBtn.addEventListener('click', function () {
        applyFilters();
        var offcanvas = document.getElementById('customersFilterOffcanvas');
        if (offcanvas && window.bootstrap) {
          var instance = window.bootstrap.Offcanvas.getInstance(offcanvas);
          if (instance) {
            instance.hide();
          }
        }
      });
    }
    var resetFilterBtn = document.getElementById('customer-filter-reset');
    if (resetFilterBtn) {
      resetFilterBtn.addEventListener('click', resetFilters);
    }
    var bulkApplyBtn = document.getElementById('customers-bulk-apply-btn');
    if (bulkApplyBtn) {
      bulkApplyBtn.addEventListener('click', onBulkApply);
    }
    var keywordEl = document.getElementById('customer-filter-keyword');
    if (keywordEl) {
      keywordEl.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
          applyFilters();
        }
      });
    }
  }

  function waitForList(attempt) {
    if (getList()) {
      bind();
      return;
    }
    if (attempt > 40) {
      bind();
      return;
    }
    window.setTimeout(function () {
      waitForList(attempt + 1);
    }, 100);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
      waitForList(0);
    });
  } else {
    waitForList(0);
  }
})();
