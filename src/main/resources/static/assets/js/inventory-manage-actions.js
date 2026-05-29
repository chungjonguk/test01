/**
 * 재고관리 — /admin/inventory
 * API: /api/admin/inventory
 * @module inventory-manage-actions
 */
(function () {
  'use strict';

  var API_BASE = '/api/admin/inventory';
  var inventoryGridView = null;
  var adjustModal = null;
  var lastRows = [];

  function $(id) {
    return document.getElementById(id);
  }

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

  function rowNo(meta, index) {
    if (window.PrintMallCommon && window.PrintMallCommon.gridRowNo) {
      return window.PrintMallCommon.gridRowNo(meta, index);
    }
    return index + 1;
  }

  function stockLevelBadge(level) {
    if (level === 'ZERO') {
      return '<span class="badge badge-soft-danger">품절</span>';
    }
    if (level === 'LOW') {
      return '<span class="badge badge-soft-warning">부족</span>';
    }
    return '<span class="badge badge-soft-success">정상</span>';
  }

  function stockQtyHtml(qty) {
    var q = qty != null ? qty : 0;
    if (q <= 0) {
      return '<span class="text-danger fw-bold">0</span>';
    }
    if (q <= 10) {
      return '<span class="text-warning fw-bold">' + escapeHtml(q) + '</span>';
    }
    return '<span class="fw-semi-bold">' + escapeHtml(q) + '</span>';
  }

  function formatPrice(price) {
    if (price == null) {
      return '—';
    }
    var n = Number(price);
    if (isNaN(n)) {
      return escapeHtml(price);
    }
    return n.toLocaleString('ko-KR');
  }

  function collectSearchParams() {
    return {
      productNm: ($('search-product-nm') || {}).value || '',
      categoryCd: ($('search-category-cd') || {}).value || '',
      statusCd: ($('search-status-cd') || {}).value || '',
      stockFilter: ($('search-stock-filter') || {}).value || ''
    };
  }

  function buildQuery(params) {
    var parts = [];
    Object.keys(params).forEach(function (key) {
      var val = params[key];
      if (val !== undefined && val !== null && String(val).length > 0) {
        parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(val));
      }
    });
    return parts.length ? '?' + parts.join('&') : '';
  }

  function getInventoryGridView() {
    if (!inventoryGridView && window.PrintMallCommon && window.PrintMallCommon.createGridViewPager) {
      inventoryGridView = window.PrintMallCommon.createGridViewPager({
        pagerRootId: 'inventory-grid-pager',
        containerId: 'inventory-grid-body',
        countElId: 'inventory-result-count',
        emptyColspan: 9,
        renderPage: function (rows, meta) {
          lastRows = rows || [];
          var tbody = $('inventory-grid-body');
          if (!tbody || !rows.length) {
            return;
          }
          tbody.innerHTML = rows
            .map(function (row, index) {
              return (
                '<tr data-product-id="' +
                escapeHtml(row.productId) +
                '">' +
                '<td class="ps-3 text-center">' +
                rowNo(meta, index) +
                '</td>' +
                '<td class="text-center">' +
                escapeHtml(row.productId) +
                '</td>' +
                '<td class="fw-semi-bold">' +
                escapeHtml(row.productNm) +
                '</td>' +
                '<td class="d-none d-md-table-cell"><code class="fs--2">' +
                escapeHtml(row.categoryCd || '—') +
                '</code></td>' +
                '<td class="text-end d-none d-lg-table-cell">' +
                formatPrice(row.price) +
                '</td>' +
                '<td class="text-center">' +
                stockQtyHtml(row.stockQty) +
                '</td>' +
                '<td class="text-center">' +
                stockLevelBadge(row.stockLevel) +
                '</td>' +
                '<td class="text-center d-none d-xl-table-cell"><span class="badge badge-soft-primary">' +
                escapeHtml(row.statusCd || '—') +
                '</span></td>' +
                '<td class="text-end pe-3 text-nowrap">' +
                '<button type="button" class="btn btn-link btn-sm p-0 inventory-adjust-btn" data-id="' +
                escapeHtml(row.productId) +
                '">조정</button>' +
                '<a class="btn btn-link btn-sm p-0 ms-2" href="/app/e-commerce/product/product-details?productId=' +
                encodeURIComponent(row.productId) +
                '">상세</a>' +
                '</td>' +
                '</tr>'
              );
            })
            .join('');
        }
      });
    }
    return inventoryGridView;
  }

  function renderGrid(items) {
    var view = getInventoryGridView();
    if (view) {
      view.setData(items || []);
    }
  }

  function loadList() {
    var tbody = $('inventory-grid-body');
    if (tbody) {
      tbody.innerHTML =
        '<tr><td colspan="9" class="text-center text-600 py-4">조회 중...</td></tr>';
    }
    var C = window.PrintMallCommon || {};
    var fetchJson = C.fetchJson || function (url, options) {
      return fetch(url, options).then(function (res) {
        return (C.parseFetchResponse ? C.parseFetchResponse(res) : res.json()).then(function (data) {
          return { ok: res.ok, status: res.status, data: data || {} };
        });
      });
    };
    fetchJson(API_BASE + buildQuery(collectSearchParams()))
      .then(function (result) {
        if (!result.ok) {
          if (C.handleQueryApiFailure && C.handleQueryApiFailure(result.status, result.data)) {
            renderGrid([]);
            return;
          }
          notify(
            C.queryErrorMessage
              ? C.queryErrorMessage(result.status, result.data, '조회에 실패했습니다.')
              : '조회에 실패했습니다.',
            'error'
          );
          renderGrid([]);
          return;
        }
        renderGrid((result.data && result.data.items) || []);
      })
      .catch(function (err) {
        notify((err && err.message) || '서버 연결에 실패했습니다.', 'error');
        if (tbody) {
          tbody.innerHTML =
            '<tr><td colspan="9" class="text-center text-danger py-4">' +
            ((err && err.message) || '조회 중 오류가 발생했습니다.') +
            '</td></tr>';
        }
      });
  }

  function getAdjustModal() {
    if (!adjustModal && window.bootstrap) {
      var el = $('inventory-adjust-modal');
      if (el) {
        adjustModal = window.bootstrap.Modal.getOrCreateInstance(el);
      }
    }
    return adjustModal;
  }

  function openAdjustModal(productId) {
    var row = lastRows.find(function (r) {
      return String(r.productId) === String(productId);
    });
    if (!row) {
      notify('상품 정보를 찾을 수 없습니다. 다시 조회해 주세요.', 'warning');
      return;
    }
    $('adjust-product-id').value = row.productId;
    $('adjust-product-label').textContent = '#' + row.productId + ' · ' + (row.productNm || '');
    $('adjust-current-stock').textContent = row.stockQty != null ? row.stockQty : 0;
    $('adjust-type').value = 'SET';
    $('adjust-quantity').value = row.stockQty != null ? row.stockQty : 0;
    var modal = getAdjustModal();
    if (modal) {
      modal.show();
    }
  }

  function submitAdjust(e) {
    e.preventDefault();
    var productId = $('adjust-product-id').value;
    var adjustType = $('adjust-type').value;
    var quantity = parseInt($('adjust-quantity').value, 10);
    if (!productId || isNaN(quantity) || quantity < 0) {
      notify('수량을 올바르게 입력해 주세요.', 'warning');
      return;
    }
    fetch(API_BASE + '/' + encodeURIComponent(productId) + '/adjust', {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify({ adjustType: adjustType, quantity: quantity })
    })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (result) {
        if (!result.ok || !result.data.success) {
          notify((result.data && result.data.message) || '재고 반영에 실패했습니다.', 'error');
          return;
        }
        notify(result.data.message || '재고가 반영되었습니다.', 'success');
        var modal = getAdjustModal();
        if (modal) {
          modal.hide();
        }
        loadList();
      })
      .catch(function () {
        notify('서버 연결에 실패했습니다.', 'error');
      });
  }

  function resetSearch() {
    var form = $('inventory-search-form');
    if (form) {
      form.reset();
    }
  }

  function bind() {
    var searchForm = $('inventory-search-form');
    if (searchForm) {
      searchForm.addEventListener('submit', function (e) {
        e.preventDefault();
        loadList();
      });
    }
    var resetBtn = $('btn-reset-inventory-search');
    if (resetBtn) {
      resetBtn.addEventListener('click', function () {
        resetSearch();
        loadList();
      });
    }
    var adjustForm = $('inventory-adjust-form');
    if (adjustForm) {
      adjustForm.addEventListener('submit', submitAdjust);
    }
    var tbody = $('inventory-grid-body');
    if (tbody) {
      tbody.addEventListener('click', function (e) {
        var btn = e.target.closest('.inventory-adjust-btn');
        if (btn) {
          openAdjustModal(btn.getAttribute('data-id'));
        }
      });
    }
    var typeEl = $('adjust-type');
    if (typeEl) {
      typeEl.addEventListener('change', function () {
        if (typeEl.value === 'SET') {
          var cur = $('adjust-current-stock');
          $('adjust-quantity').value = cur ? cur.textContent : 0;
        } else {
          $('adjust-quantity').value = '0';
        }
      });
    }
    loadList();
  }

  window.InventoryMgmtInit = bind;

  if (document.getElementById('inventory-management-panel')) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', bind);
    } else {
      bind();
    }
  }
})();
