/**
 * 주문 목록 — Filter / Export / Bulk / 페이지네이션 / 행 메뉴
 */
(function () {
  'use strict';

  var ROOT_ID = 'ordersTable';

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

  function currentPage(list) {
    var pageSize = list.page || 10;
    return Math.max(1, Math.ceil(list.i / pageSize));
  }

  function totalPages(list) {
    var pageSize = list.page || 10;
    var total = list.matchingItems ? list.matchingItems.length : list.items.length;
    return Math.max(1, Math.ceil(total / pageSize));
  }

  function goToPage(list, page) {
    var pageSize = list.page || 10;
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
    var list = getList();
    if (!list) {
      return;
    }
    var keywordEl = document.getElementById('order-filter-keyword');
    var statusEl = document.getElementById('order-filter-status');
    var keyword = keywordEl ? keywordEl.value.trim().toLowerCase() : '';
    var status = statusEl ? statusEl.value.trim() : '';

    list.filter(function (item) {
      var values = item.values();
      var statusText = (values.status || '').toLowerCase();
      var matchStatus = !status || statusText.indexOf(status.toLowerCase()) !== -1;
      if (!keyword) {
        return matchStatus;
      }
      var rowText = Object.keys(values)
        .map(function (k) {
          return values[k];
        })
        .join(' ')
        .toLowerCase();
      return matchStatus && rowText.indexOf(keyword) !== -1;
    });
    goToPage(list, 1);
    renderPaginationControls();
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
    var list = getList();
    if (list) {
      list.filter();
      list.search();
      goToPage(list, 1);
      renderPaginationControls();
    }
  }

  function exportCsv() {
    var list = getList();
    var items = list && list.matchingItems && list.matchingItems.length ? list.matchingItems : null;
    if (!items || !items.length) {
      var rows = document.querySelectorAll('#table-orders-body tr');
      if (!rows.length) {
        notify('보낼 주문이 없습니다.', 'warning');
        return;
      }
      items = Array.prototype.slice.call(rows).map(function (tr) {
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

  function statusBadgeClass(label) {
    var key = label.toLowerCase();
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
      label +
      icon +
      '</span>'
    );
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
      applyFilterBtn.addEventListener('click', function () {
        applyFilters();
        var offcanvas = document.getElementById('ordersFilterOffcanvas');
        if (offcanvas && window.bootstrap) {
          var instance = window.bootstrap.Offcanvas.getInstance(offcanvas);
          if (instance) {
            instance.hide();
          }
        }
      });
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

  function init(attempt) {
    bindStaticControls();
    bindRowDropdowns();
    var list = getList();
    if (list) {
      bindListHooks();
      renderPaginationControls();
      return;
    }
    if (attempt > 50) {
      return;
    }
    window.setTimeout(function () {
      init(attempt + 1);
    }, 100);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
      init(0);
    });
  } else {
    init(0);
  }
})();
