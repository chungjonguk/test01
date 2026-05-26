/**
 * 고객 목록 — /app/e-commerce/customers
 * API: GET /api/e-commerce/customers
 */
(function () {
  'use strict';

  var API_BASE = '/api/e-commerce/customers';
  var DETAIL_PATH = '/app/e-commerce/customer-details';
  var lastItems = [];

  function getRoot() {
    return document.getElementById('customersTable');
  }

  function getList() {
    var root = getRoot();
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

  function escapeHtml(text) {
    if (text == null) {
      return '';
    }
    return String(text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function formatJoined(value) {
    if (window.PrintMallCommon && window.PrintMallCommon.formatDate) {
      return window.PrintMallCommon.formatDate(value);
    }
    return value == null ? '' : String(value);
  }

  function initials(name) {
    if (!name) {
      return '?';
    }
    var parts = String(name).trim().split(/\s+/);
    if (parts.length >= 2) {
      return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
    }
    return name.substring(0, 2).toUpperCase();
  }

  function parseListOptions(root) {
    var raw = root.getAttribute('data-list');
    if (!raw) {
      return { valueNames: ['name', 'email', 'phone', 'address', 'joined'], page: 10, pagination: true };
    }
    try {
      return JSON.parse(raw);
    } catch (e) {
      return { valueNames: ['name', 'email', 'phone', 'address', 'joined'], page: 10, pagination: true };
    }
  }

  function createListInstance() {
    var root = getRoot();
    if (!root || !window.List) {
      return null;
    }
    var options = parseListOptions(root);
    if (options.pagination) {
      options.pagination = Object.assign(
        { item: "<li><button class='page' type='button'></button></li>" },
        options.pagination === true ? {} : options.pagination
      );
    }
    var list = new window.List(root, options);
    root._falconList = list;
    return list;
  }

  function buildRowHtml(item, index) {
    var name = escapeHtml(item.customerNm || '');
    var email = escapeHtml(item.email || '');
    var phone = escapeHtml(item.phone || '');
    var address = escapeHtml(item.address || '');
    var joined = escapeHtml(formatJoined(item.joinedDt));
    var ini = escapeHtml(initials(item.customerNm));
    var cbId = 'customer-' + index;

    return (
      '<tr class="btn-reveal-trigger">' +
      '<td class="align-middle py-2" style="width: 28px;">' +
      '<div class="form-check fs-0 mb-0 d-flex align-items-center">' +
      '<input class="form-check-input" type="checkbox" id="' +
      cbId +
      '" data-bulk-select-row="data-bulk-select-row" />' +
      '</div></td>' +
      '<td class="name align-middle white-space-nowrap py-2">' +
      '<a href="' +
      DETAIL_PATH +
      '">' +
      '<div class="d-flex align-items-center">' +
      '<div class="avatar avatar-xl me-2"><div class="avatar-name rounded-circle"><span>' +
      ini +
      '</span></div></div>' +
      '<div class="flex-1"><h5 class="mb-0 fs--1">' +
      name +
      '</h5></div></div></a></td>' +
      '<td class="email align-middle py-2"><a href="mailto:' +
      email +
      '">' +
      email +
      '</a></td>' +
      '<td class="phone align-middle white-space-nowrap py-2">' +
      (phone ? '<a href="tel:' + phone + '">' + phone + '</a>' : '—') +
      '</td>' +
      '<td class="address align-middle white-space-nowrap ps-5 py-2">' +
      (address || '—') +
      '</td>' +
      '<td class="joined align-middle py-2">' +
      joined +
      '</td>' +
      '<td class="align-middle white-space-nowrap py-2 text-end">' +
      '<div class="dropdown font-sans-serif position-static">' +
      '<button class="btn btn-link text-600 btn-sm dropdown-toggle btn-reveal" type="button" id="customer-dropdown-' +
      index +
      '" data-bs-toggle="dropdown" data-boundary="window" aria-haspopup="true" aria-expanded="false">' +
      '<span class="fas fa-ellipsis-h fs--1"></span></button>' +
      '<div class="dropdown-menu dropdown-menu-end border py-0" aria-labelledby="customer-dropdown-' +
      index +
      '">' +
      '<div class="py-2"><a class="dropdown-item" href="' +
      DETAIL_PATH +
      '">Edit</a>' +
      '<a class="dropdown-item text-danger" href="#!">Delete</a></div></div></div></td></tr>'
    );
  }

  function renderRows(items) {
    var tbody = document.getElementById('table-customers-body');
    if (!tbody) {
      return;
    }
    if (!items || !items.length) {
      tbody.innerHTML =
        '<tr><td colspan="7" class="text-center py-4 text-500">표시할 고객이 없습니다.</td></tr>';
      return;
    }
    tbody.innerHTML = items.map(buildRowHtml).join('');
  }

  function loadCustomers(keyword) {
    var q = keyword ? '?keyword=' + encodeURIComponent(keyword) : '';
    return fetch(API_BASE + q, { credentials: 'same-origin' })
      .then(function (res) {
        if (!res.ok) {
          throw new Error('HTTP ' + res.status);
        }
        return res.json();
      })
      .then(function (body) {
        if (!body || !body.success) {
          throw new Error((body && body.message) || '목록 조회 실패');
        }
        lastItems = body.items || [];
        renderRows(lastItems);
        var root = getRoot();
        if (root) {
          root._falconList = null;
        }
        createListInstance();
      })
      .catch(function (err) {
        var tbody = document.getElementById('table-customers-body');
        if (tbody) {
          tbody.innerHTML =
            '<tr><td colspan="7" class="text-center py-4 text-danger">고객 목록을 불러오지 못했습니다.</td></tr>';
        }
        notify((err && err.message) || '고객 목록 조회 오류', 'error');
      });
  }

  function applyFilters() {
    var keywordEl = document.getElementById('customer-filter-keyword');
    var keyword = keywordEl ? keywordEl.value.trim() : '';
    loadCustomers(keyword);
    var offcanvas = document.getElementById('customersFilterOffcanvas');
    if (offcanvas && window.bootstrap) {
      var instance = window.bootstrap.Offcanvas.getInstance(offcanvas);
      if (instance) {
        instance.hide();
      }
    }
  }

  function resetFilters() {
    var keywordEl = document.getElementById('customer-filter-keyword');
    if (keywordEl) {
      keywordEl.value = '';
    }
    loadCustomers('');
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
      if (!lastItems.length) {
        notify('보낼 고객이 없습니다.', 'warning');
        return;
      }
      items = Array.prototype.slice.call(document.querySelectorAll('#table-customers-body tr')).map(function (tr) {
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
    if (exportBtn && !exportBtn.dataset.customersBound) {
      exportBtn.dataset.customersBound = '1';
      exportBtn.addEventListener('click', exportCsv);
    }
    var applyFilterBtn = document.getElementById('customer-filter-apply');
    if (applyFilterBtn && !applyFilterBtn.dataset.customersBound) {
      applyFilterBtn.dataset.customersBound = '1';
      applyFilterBtn.addEventListener('click', applyFilters);
    }
    var resetFilterBtn = document.getElementById('customer-filter-reset');
    if (resetFilterBtn && !resetFilterBtn.dataset.customersBound) {
      resetFilterBtn.dataset.customersBound = '1';
      resetFilterBtn.addEventListener('click', resetFilters);
    }
    var bulkApplyBtn = document.getElementById('customers-bulk-apply-btn');
    if (bulkApplyBtn && !bulkApplyBtn.dataset.customersBound) {
      bulkApplyBtn.dataset.customersBound = '1';
      bulkApplyBtn.addEventListener('click', onBulkApply);
    }
    var keywordEl = document.getElementById('customer-filter-keyword');
    if (keywordEl && !keywordEl.dataset.customersBound) {
      keywordEl.dataset.customersBound = '1';
      keywordEl.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
          applyFilters();
        }
      });
    }
  }

  function init() {
    bind();
    loadCustomers('');
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
