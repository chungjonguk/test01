/**
 * 주문 목록 — /app/e-commerce/orders/order-list
 * API: GET /api/e-commerce/orders
 * @module order-list-actions
 */
(function () {
  'use strict';

  var API_BASE = '/api/e-commerce/orders';
  var ROOT_ID = 'ordersTable';
  var DETAIL_PATH = '/app/e-commerce/orders/order-details';
  var lastItems = [];

  function getRoot() {
    return document.getElementById(ROOT_ID);
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

  function formatOrderDate(value) {
    if (window.PrintMallCommon && window.PrintMallCommon.formatDate) {
      return window.PrintMallCommon.formatDate(value);
    }
    return value == null ? '' : String(value);
  }

  function formatAmount(amount) {
    if (amount == null) {
      return '—';
    }
    var n = Number(amount);
    if (isNaN(n)) {
      return escapeHtml(amount);
    }
    return '$' + n.toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
  }

  function collectFilterParams() {
    var keywordEl = document.getElementById('order-filter-keyword');
    var statusEl = document.getElementById('order-filter-status');
    return {
      keyword: keywordEl ? keywordEl.value.trim() : '',
      statusCd: statusEl ? statusEl.value.trim() : ''
    };
  }

  function buildApiQuery(params) {
    var parts = [];
    if (params.keyword) {
      parts.push('keyword=' + encodeURIComponent(params.keyword));
    }
    if (params.statusCd) {
      parts.push('statusCd=' + encodeURIComponent(params.statusCd));
    }
    return parts.length ? '?' + parts.join('&') : '';
  }

  function parseListOptions(root) {
    var raw = root.getAttribute('data-list');
    if (!raw) {
      return {
        valueNames: ['order', 'date', 'address', 'status', 'amount'],
        page: 30,
        pagination: { innerWindow: 2, left: 1, right: 1 }
      };
    }
    try {
      return JSON.parse(raw);
    } catch (e) {
      return {
        valueNames: ['order', 'date', 'address', 'status', 'amount'],
        page: 30,
        pagination: { innerWindow: 2, left: 1, right: 1 }
      };
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
        options.pagination
      );
    }
    var list = new window.List(root, options);
    root._falconList = list;
    return list;
  }

  function statusBadgeClass(label) {
    var key = (label || '').toLowerCase();
    if (key.indexOf('completed') !== -1) {
      return 'badge-soft-success';
    }
    if (key.indexOf('processing') !== -1) {
      return 'badge-soft-primary';
    }
    if (key.indexOf('hold') !== -1) {
      return 'badge-soft-secondary';
    }
    if (key.indexOf('pending') !== -1) {
      return 'badge-soft-warning';
    }
    return 'badge-soft-info';
  }

  function statusBadgeHtml(label) {
    var cls = statusBadgeClass(label);
    var icon =
      label.toLowerCase().indexOf('completed') !== -1
        ? '<span class="ms-1 fas fa-check" data-fa-transform="shrink-2"></span>'
        : label.toLowerCase().indexOf('hold') !== -1
          ? '<span class="ms-1 fas fa-ban" data-fa-transform="shrink-2"></span>'
          : label.toLowerCase().indexOf('pending') !== -1
            ? '<span class="ms-1 fas fa-stream" data-fa-transform="shrink-2"></span>'
            : label.toLowerCase().indexOf('processing') !== -1
              ? '<span class="ms-1 fas fa-redo" data-fa-transform="shrink-2"></span>'
              : '';
    return (
      '<span class="badge badge rounded-pill d-block ' +
      cls +
      '">' +
      escapeHtml(label) +
      icon +
      '</span>'
    );
  }

  function buildOrderRowHtml(item, index) {
    var orderNo = escapeHtml(item.orderNo || '');
    var customerNm = escapeHtml(item.customerNm || '');
    var email = escapeHtml(item.customerEmail || '');
    var orderDt = escapeHtml(formatOrderDate(item.orderDt));
    var shipTo = escapeHtml(item.shipTo || '');
    var statusLabel = item.statusLabel || item.statusCd || '';
    var amount = formatAmount(item.amount);
    var cbId = 'order-cb-' + index;

    return (
      '<tr class="btn-reveal-trigger">' +
      '<td class="align-middle" style="width: 28px;">' +
      '<div class="form-check fs-0 mb-0 d-flex align-items-center">' +
      '<input class="form-check-input" type="checkbox" id="' +
      cbId +
      '" data-bulk-select-row="data-bulk-select-row" />' +
      '</div></td>' +
      '<td class="order py-2 align-middle white-space-nowrap">' +
      '<a href="' +
      DETAIL_PATH +
      '"><strong>' +
      orderNo +
      '</strong></a> by <strong>' +
      customerNm +
      '</strong><br />' +
      '<a href="mailto:' +
      email +
      '">' +
      email +
      '</a></td>' +
      '<td class="date py-2 align-middle">' +
      orderDt +
      '</td>' +
      '<td class="address py-2 align-middle white-space-nowrap">' +
      shipTo +
      '</td>' +
      '<td class="status py-2 align-middle text-center fs-0 white-space-nowrap">' +
      statusBadgeHtml(statusLabel) +
      '</td>' +
      '<td class="amount py-2 align-middle text-end fs-0 fw-medium">' +
      amount +
      '</td>' +
      '<td class="py-2 align-middle white-space-nowrap text-end">' +
      '<div class="dropdown font-sans-serif position-static">' +
      '<button class="btn btn-link text-600 btn-sm dropdown-toggle btn-reveal" type="button" id="order-dropdown-' +
      index +
      '" data-bs-toggle="dropdown" data-boundary="window" aria-haspopup="true" aria-expanded="false">' +
      '<span class="fas fa-ellipsis-h fs--1"></span></button>' +
      '<div class="dropdown-menu dropdown-menu-end border py-0" aria-labelledby="order-dropdown-' +
      index +
      '">' +
      '<div class="py-2">' +
      '<a class="dropdown-item" href="#!">Completed</a>' +
      '<a class="dropdown-item" href="#!">Processing</a>' +
      '<a class="dropdown-item" href="#!">On Hold</a>' +
      '<a class="dropdown-item" href="#!">Pending</a>' +
      '<div class="dropdown-divider"></div>' +
      '<a class="dropdown-item text-danger" href="#!">Delete</a>' +
      '</div></div></div></td></tr>'
    );
  }

  function renderRows(items) {
    var tbody = document.getElementById('table-orders-body');
    if (!tbody) {
      return;
    }
    if (!items || !items.length) {
      tbody.innerHTML =
        '<tr><td colspan="7" class="text-center py-4 text-500">표시할 주문이 없습니다.</td></tr>';
      return;
    }
    tbody.innerHTML = items.map(buildOrderRowHtml).join('');
  }

  function loadOrders(params) {
    var queryParams = params || collectFilterParams();
    var url = API_BASE + buildApiQuery(queryParams);
    return fetch(url, { credentials: 'same-origin' })
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
        bindListHooks();
        renderPaginationControls();
      })
      .catch(function (err) {
        var tbody = document.getElementById('table-orders-body');
        if (tbody) {
          tbody.innerHTML =
            '<tr><td colspan="7" class="text-center py-4 text-danger">주문 목록을 불러오지 못했습니다.</td></tr>';
        }
        notify((err && err.message) || '주문 목록 조회 오류', 'error');
      });
  }

  function currentPage(list) {
    var pageSize =
      list.page ||
      (window.PrintMallCommon && window.PrintMallCommon.gridPageSize
        ? window.PrintMallCommon.gridPageSize()
        : window.PrintMallGridPager && window.PrintMallGridPager.getPageSize
          ? window.PrintMallGridPager.getPageSize()
          : 30);
    return Math.max(1, Math.ceil(list.i / pageSize));
  }

  function totalPages(list) {
    var pageSize =
      list.page ||
      (window.PrintMallCommon && window.PrintMallCommon.gridPageSize
        ? window.PrintMallCommon.gridPageSize()
        : window.PrintMallGridPager && window.PrintMallGridPager.getPageSize
          ? window.PrintMallGridPager.getPageSize()
          : 30);
    var total = list.matchingItems ? list.matchingItems.length : list.items.length;
    return Math.max(1, Math.ceil(total / pageSize));
  }

  function goToPage(list, page) {
    var pageSize =
      list.page ||
      (window.PrintMallCommon && window.PrintMallCommon.gridPageSize
        ? window.PrintMallCommon.gridPageSize()
        : window.PrintMallGridPager && window.PrintMallGridPager.getPageSize
          ? window.PrintMallGridPager.getPageSize()
          : 30);
    var maxPage = totalPages(list);
    var target = Math.min(Math.max(1, page), maxPage);
    list.show((target - 1) * pageSize + 1, pageSize);
  }

  function renderPaginationControls() {
    var root = getRoot();
    var list = getList();
    if (!root || !list) {
      return;
    }

    var ul = root.querySelector('.pagination');
    if (!ul) {
      return;
    }

    var page = currentPage(list);
    var maxPage = totalPages(list);
    ul.innerHTML = '';

    for (var p = 1; p <= maxPage; p += 1) {
      var li = document.createElement('li');
      li.className = 'page-item' + (p === page ? ' active' : '');
      var btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'page';
      btn.textContent = String(p);
      btn.addEventListener('click', function (pageNum) {
        return function (e) {
          e.preventDefault();
          goToPage(list, pageNum);
          renderPaginationControls();
        };
      }(p));
      li.appendChild(btn);
      ul.appendChild(li);
    }

    var prevBtn = root.querySelector('[data-list-pagination="prev"]');
    var nextBtn = root.querySelector('[data-list-pagination="next"]');
    if (prevBtn) {
      prevBtn.disabled = page <= 1;
      prevBtn.classList.toggle('disabled', page <= 1);
    }
    if (nextBtn) {
      nextBtn.disabled = page >= maxPage;
      nextBtn.classList.toggle('disabled', page >= maxPage);
    }
  }

  function applyFilters() {
    loadOrders(collectFilterParams());
    var offcanvas = document.getElementById('ordersFilterOffcanvas');
    if (offcanvas && window.bootstrap) {
      var instance = window.bootstrap.Offcanvas.getInstance(offcanvas);
      if (instance) {
        instance.hide();
      }
    }
  }

  function resetFilters() {
    var keywordEl = document.getElementById('order-filter-keyword');
    var statusEl = document.getElementById('order-filter-status');
    if (keywordEl) {
      keywordEl.value = '';
    }
    if (statusEl) {
      statusEl.value = '';
    }
    loadOrders({ keyword: '', statusCd: '' });
  }

  function exportCsv() {
    var list = getList();
    var items = list && list.matchingItems && list.matchingItems.length ? list.matchingItems : null;
    if (!items || !items.length) {
      if (!lastItems.length) {
        notify('보낼 주문이 없습니다.', 'warning');
        return;
      }
      items = Array.prototype.slice.call(document.querySelectorAll('#table-orders-body tr')).map(function (tr) {
        return {
          values: function () {
            return {
              order: (tr.querySelector('.order') || {}).innerText || '',
              date: (tr.querySelector('.date') || {}).innerText || '',
              address: (tr.querySelector('.address') || {}).innerText || '',
              status: (tr.querySelector('.status') || {}).innerText || '',
              amount: (tr.querySelector('.amount') || {}).innerText || ''
            };
          }
        };
      });
    }
    var lines = ['Order,Date,Ship To,Status,Amount'];
    items.forEach(function (item) {
      var v = item.values();
      var row = [v.order, v.date, v.address, v.status, v.amount].map(csvEscape).join(',');
      lines.push(row);
    });
    var blob = new Blob(['\uFEFF' + lines.join('\r\n')], { type: 'text/csv;charset=utf-8;' });
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = 'orders-' + new Date().toISOString().slice(0, 10) + '.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    notify('CSV 파일을 다운로드했습니다.', 'success');
  }

  function csvEscape(value) {
    var text = String(value).replace(/\s+/g, ' ').trim();
    if (/[",\n\r]/.test(text)) {
      return '"' + text.replace(/"/g, '""') + '"';
    }
    return text;
  }

  function onBulkApply() {
    var select = document.querySelector('#orders-bulk-actions select');
    var action = select ? select.value : '';
    if (!action) {
      notify('Bulk action을 선택하세요.', 'warning');
      return;
    }
    var checked = document.querySelectorAll('#table-orders-body [data-bulk-select-row]:checked');
    if (!checked.length) {
      notify('선택된 주문이 없습니다.', 'warning');
      return;
    }
    notify(checked.length + '건에 "' + action + '" 적용 (데모)', 'success');
  }

  function applyRowStatus(row, label) {
    var statusCell = row.querySelector('td.status');
    if (statusCell) {
      statusCell.innerHTML = statusBadgeHtml(label);
    }
  }

  function onRowMenuClick(e) {
    var item = e.target.closest('a.dropdown-item, button.dropdown-item');
    if (!item) {
      return;
    }
    var root = getRoot();
    if (!root || !root.contains(item)) {
      return;
    }
    var href = item.getAttribute('href');
    if (href && href !== '#' && href !== '#!') {
      return;
    }

    e.preventDefault();
    e.stopPropagation();

    var label = (item.textContent || '').trim();
    var row = item.closest('tr');
    var toggle = item.closest('.dropdown') && item.closest('.dropdown').querySelector('[data-bs-toggle="dropdown"]');

    if (item.classList.contains('text-danger')) {
      notify('삭제는 데모 화면에서 지원하지 않습니다.', 'info');
    } else if (row) {
      applyRowStatus(row, label);
      notify('주문 상태를 "' + label + '"(으)로 변경했습니다.', 'success');
    } else {
      notify('상태를 "' + label + '"(으)로 변경 (데모)', 'success');
    }

    if (toggle && window.bootstrap && window.bootstrap.Dropdown) {
      var instance = window.bootstrap.Dropdown.getInstance(toggle);
      if (instance) {
        instance.hide();
      }
    }
  }

  function bindRowDropdowns() {
    var root = getRoot();
    if (!root || root.dataset.orderRowMenuBound) {
      return;
    }
    root.dataset.orderRowMenuBound = '1';
    root.addEventListener('click', onRowMenuClick);
  }

  function bindStaticControls() {
    var exportBtn = document.getElementById('order-export-btn');
    if (exportBtn && !exportBtn.dataset.orderListBound) {
      exportBtn.dataset.orderListBound = '1';
      exportBtn.addEventListener('click', exportCsv);
    }

    var applyFilterBtn = document.getElementById('order-filter-apply');
    if (applyFilterBtn && !applyFilterBtn.dataset.orderListBound) {
      applyFilterBtn.dataset.orderListBound = '1';
      applyFilterBtn.addEventListener('click', applyFilters);
    }

    var resetFilterBtn = document.getElementById('order-filter-reset');
    if (resetFilterBtn && !resetFilterBtn.dataset.orderListBound) {
      resetFilterBtn.dataset.orderListBound = '1';
      resetFilterBtn.addEventListener('click', resetFilters);
    }

    var bulkApplyBtn = document.getElementById('orders-bulk-apply-btn');
    if (bulkApplyBtn && !bulkApplyBtn.dataset.orderListBound) {
      bulkApplyBtn.dataset.orderListBound = '1';
      bulkApplyBtn.addEventListener('click', onBulkApply);
    }

    var keywordEl = document.getElementById('order-filter-keyword');
    if (keywordEl && !keywordEl.dataset.orderListBound) {
      keywordEl.dataset.orderListBound = '1';
      keywordEl.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
          applyFilters();
        }
      });
    }
  }

  function bindListHooks() {
    var list = getList();
    if (!list || list._orderListHooksBound) {
      return;
    }
    list._orderListHooksBound = true;
    list.on('updated', function () {
      renderPaginationControls();
    });
  }

  function init() {
    bindStaticControls();
    bindRowDropdowns();
    loadOrders({ keyword: '', statusCd: '' });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
