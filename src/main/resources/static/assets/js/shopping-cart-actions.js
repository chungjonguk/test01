/**
 * 장바구니 — 목록 렌더링, 수량 조절, 선택/개별 삭제, 프로모 코드
 */
(function () {
  'use strict';

  var DEMO_ITEMS = [
    {
      id: 'demo-1',
      title: 'Apple MacBook Pro 15" Z0V20008N: 2.9GHz 6-core 8th-Gen Intel Core i9, 32GB RAM',
      price: '$1292',
      imageUrl: '/assets/img/products/1.jpg',
      qty: 1
    },
    {
      id: 'demo-2',
      title: 'Apple iMac Pro (27-inch with Retina 5K Display, 3.0GHz 10-core Intel Xeon W, 1TB SSD)',
      price: '$2012',
      imageUrl: '/assets/img/products/2.jpg',
      qty: 1
    },
    {
      id: 'demo-3',
      title: 'Apple iPad Air 2019 (3GB RAM, 128GB ROM, 8MP Main Camera)',
      price: '$1024',
      imageUrl: '/assets/img/products/4.jpg',
      qty: 1
    },
    {
      id: 'demo-4',
      title: 'Apple iPhone XS Max (4GB RAM, 512GB ROM, 12MP Main Camera)',
      price: '$990',
      imageUrl: '/assets/img/products/3.jpg',
      qty: 1
    },
    {
      id: 'demo-5',
      title: 'Apple Watch Series 4 44mm GPS Only',
      price: '$400',
      imageUrl: '/assets/img/products/5.jpg',
      qty: 1
    },
    {
      id: 'demo-6',
      title: 'Nikon D3200 Digital DSLR Camera',
      price: '$2398',
      imageUrl: '/assets/img/products/7.jpg',
      qty: 1
    },
    {
      id: 'demo-7',
      title: 'Canon Standard Zoom Lens',
      price: '$400',
      imageUrl: '/assets/img/products/8.jpg',
      qty: 1
    }
  ];

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

  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/"/g, '&quot;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  function escapeAttr(value) {
    return escapeHtml(value).replace(/'/g, '&#39;');
  }

  function parsePriceNumber(price) {
    var api = cartApi();
    if (api && typeof api.parsePriceNumber === 'function') {
      return api.parsePriceNumber(price);
    }
    if (price == null || price === '') {
      return 0;
    }
    if (typeof price === 'number' && !isNaN(price)) {
      return price >= 0 ? price : 0;
    }
    var num = parseFloat(String(price).replace(/[^0-9.]/g, ''));
    return isNaN(num) ? 0 : num;
  }

  function getUnitPrice(item) {
    if (!item) {
      return 0;
    }
    var unit = parseFloat(item.unitPrice);
    if (!isNaN(unit) && unit > 0) {
      return unit;
    }
    return parsePriceNumber(item.price);
  }

  function enrichItemForCalc(item) {
    if (!item) {
      return item;
    }
    item.qty = normalizeQty(item.qty);
    var unit = getUnitPrice(item);
    item.unitPrice = unit;
    return item;
  }

  /** 소계 = 단가 × 수량 */
  function calcLineSubtotal(unit, qty) {
    var u = parseFloat(unit);
    if (isNaN(u) || u < 0) {
      u = 0;
    }
    var n = normalizeQty(qty);
    return Math.round(u * n * 100) / 100;
  }

  function formatDisplayPrice(amount) {
    if (window.PrintMallCurrency && typeof window.PrintMallCurrency.formatWon === 'function') {
      return window.PrintMallCurrency.formatWon(amount);
    }
    return '$' + Math.round(amount).toLocaleString('en-US');
  }

  function cartApi() {
    return window.PrintMallCartNav || null;
  }

  function normalizeQty(qty) {
    var api = cartApi();
    if (api && typeof api.normalizeQty === 'function') {
      return api.normalizeQty(qty);
    }
    var n = parseInt(qty, 10);
    if (isNaN(n) || n < 1) {
      return 1;
    }
    return n > 999 ? 999 : n;
  }

  function normalizeItems(items) {
    items.forEach(enrichItemForCalc);
    return items;
  }

  function ensureItems() {
    var api = cartApi();
    if (!api) {
      return [];
    }
    var items = api.loadItems();
    if (!items.length && api.saveItems) {
      items = DEMO_ITEMS.slice();
      api.saveItems(items);
      return api.loadItems();
    }
    normalizeItems(items);
    var needsSave = items.some(function (it) {
      var unit = parseFloat(it.unitPrice);
      return isNaN(unit) || unit <= 0;
    });
    if (needsSave && api.saveItems) {
      api.saveItems(items);
      return api.loadItems();
    }
    return items;
  }

  function lineTotal(item) {
    return calcLineSubtotal(getUnitPrice(item), item.qty);
  }

  function renderRow(item, index) {
    enrichItemForCalc(item);
    var title = escapeHtml(item.title || '상품');
    var unitAmount = item.unitPrice;
    var qty = item.qty;
    var subtotalAmount = calcLineSubtotal(unitAmount, qty);
    var unitPrice = escapeHtml(formatDisplayPrice(unitAmount));
    var lineSubtotal = escapeHtml(formatDisplayPrice(subtotalAmount));
    var subtotalHint = escapeHtml(formatDisplayPrice(unitAmount) + ' × ' + qty);
    var img = escapeAttr(item.imageUrl || '/assets/img/products/1.jpg');
    return (
      '<div class="cart-grid-row cart-item-row border-bottom border-200" data-cart-index="' +
      index +
      '" data-unit-price="' +
      unitAmount +
      '" data-line-qty="' +
      qty +
      '" data-line-subtotal="' +
      subtotalAmount +
      '">' +
      '<div class="cart-cell-check py-3">' +
      '<input class="form-check-input cart-item-check" type="checkbox" data-cart-index="' +
      index +
      '" aria-label="상품 선택"/>' +
      '</div>' +
      '<div class="cart-cell-product py-3">' +
      '<div class="d-flex align-items-center">' +
      '<img class="img-fluid rounded-1 me-2 d-none d-md-block flex-shrink-0" src="' +
      img +
      '" alt="" width="52" onerror="this.src=\'/assets/img/products/1.jpg\'"/>' +
      '<h5 class="fs-0 mb-0">' +
      title +
      '</h5></div></div>' +
      '<div class="cart-cell-unit py-3 text-end">' +
      '<span class="cart-unit-price text-900" data-currency-keep>' +
      unitPrice +
      '</span></div>' +
      '<div class="cart-cell-qty py-3 text-center">' +
      '<div class="input-group input-group-sm cart-qty-group mx-auto" style="width:6.5rem">' +
      '<button type="button" class="btn btn-outline-secondary cart-qty-minus" data-cart-index="' +
      index +
      '" title="수량 감소" aria-label="수량 감소">−</button>' +
      '<input type="text" class="form-control text-center px-1 cart-qty-value" value="' +
      qty +
      '" readonly aria-label="수량"/>' +
      '<button type="button" class="btn btn-outline-secondary cart-qty-plus" data-cart-index="' +
      index +
      '" title="수량 증가" aria-label="수량 증가">+</button>' +
      '</div></div>' +
      '<div class="cart-cell-subtotal py-3 text-end">' +
      '<span class="cart-line-subtotal fw-semi-bold text-900" data-currency-keep title="' +
      subtotalHint +
      '">' +
      lineSubtotal +
      '</span>' +
      '<span class="d-block fs--2 text-600 cart-subtotal-formula" data-currency-keep">' +
      subtotalHint +
      '</span></div>' +
      '<div class="cart-cell-action py-3 text-center">' +
      '<button type="button" class="btn btn-sm btn-outline-danger cart-remove-btn" data-cart-index="' +
      index +
      '" title="삭제"><span class="fas fa-trash-alt"></span></button>' +
      '</div></div>'
    );
  }

  function getSelectedIndices() {
    var checks = document.querySelectorAll('#cart-item-list .cart-item-check:checked');
    return Array.prototype.map.call(checks, function (el) {
      return parseInt(el.getAttribute('data-cart-index'), 10);
    }).filter(function (i) {
      return !isNaN(i);
    });
  }

  function syncSelectAllState() {
    var selectAll = document.getElementById('cart-select-all');
    var list = document.getElementById('cart-item-list');
    if (!selectAll || !list) {
      return;
    }
    var checks = list.querySelectorAll('.cart-item-check');
    if (!checks.length) {
      selectAll.checked = false;
      selectAll.indeterminate = false;
      selectAll.disabled = true;
      return;
    }
    selectAll.disabled = false;
    var checked = list.querySelectorAll('.cart-item-check:checked').length;
    selectAll.checked = checked === checks.length;
    selectAll.indeterminate = checked > 0 && checked < checks.length;
  }

  function updateDeleteSelectedButton() {
    var btn = document.getElementById('btn-cart-delete-selected');
    if (!btn) {
      return;
    }
    var selected = getSelectedIndices().length;
    btn.disabled = selected === 0;
    btn.setAttribute('aria-label', selected ? selected + '개 선택 삭제' : '선택 삭제');
  }

  function updateSummary(items) {
    var countEl = document.getElementById('cart-total-count');
    var priceEl = document.getElementById('cart-total-price');
    var titleEl = document.getElementById('cart-title-count');
    var clearBtn = document.getElementById('btn-cart-clear-all');
    var selectAll = document.getElementById('cart-select-all');
    var lineCount = items.length;
    var unitCount = 0;
    var total = 0;
    items.forEach(function (item) {
      var qty = normalizeQty(item.qty);
      unitCount += qty;
      total += calcLineSubtotal(getUnitPrice(item), qty);
    });
    if (countEl) {
      countEl.textContent = lineCount
        ? lineCount + '종 · ' + unitCount + '개'
        : '0건';
    }
    if (priceEl) {
      priceEl.textContent = lineCount ? formatDisplayPrice(total) : '-';
      priceEl.setAttribute('data-currency-keep', 'true');
    }
    if (titleEl) {
      titleEl.textContent = lineCount
        ? '장바구니 (' + lineCount + '종, ' + unitCount + '개)'
        : '장바구니';
    }
    document.querySelectorAll('#shoppingCartCard a[href*="checkout"]').forEach(function (link) {
      link.classList.toggle('disabled', lineCount === 0);
      link.setAttribute('aria-disabled', lineCount === 0 ? 'true' : 'false');
    });
    if (clearBtn) {
      clearBtn.disabled = lineCount === 0;
    }
    if (selectAll) {
      selectAll.disabled = lineCount === 0;
    }
    updateDeleteSelectedButton();
    syncSelectAllState();
  }

  function renderCart() {
    var list = document.getElementById('cart-item-list');
    var empty = document.getElementById('cart-empty');
    if (!list) {
      return;
    }
    var items = ensureItems();
    if (!items.length) {
      list.innerHTML = '';
      if (empty) {
        empty.classList.remove('d-none');
      }
      updateSummary(items);
      return;
    }
    if (empty) {
      empty.classList.add('d-none');
    }
    list.innerHTML = items
      .map(function (item, index) {
        return renderRow(item, index);
      })
      .join('');
    updateSummary(items);
  }

  function refreshRowAmounts(index, qtyOverride) {
    var api = cartApi();
    var items = api && api.loadItems ? api.loadItems() : ensureItems();
    if (index < 0 || index >= items.length) {
      return;
    }
    var item = items[index];
    enrichItemForCalc(item);
    var unit = item.unitPrice;
    var qty = qtyOverride != null ? normalizeQty(qtyOverride) : item.qty;
    var subtotalAmount = calcLineSubtotal(unit, qty);
    var row = document.querySelector(
      '#cart-item-list .cart-item-row[data-cart-index="' + index + '"]'
    );
    if (!row) {
      renderCart();
      return;
    }
    row.setAttribute('data-unit-price', String(unit));
    row.setAttribute('data-line-qty', String(qty));
    row.setAttribute('data-line-subtotal', String(subtotalAmount));
    var unitText = formatDisplayPrice(unit);
    var subtotalText = formatDisplayPrice(subtotalAmount);
    var formulaText = formatDisplayPrice(unit) + ' × ' + qty;
    row.querySelectorAll('.cart-unit-price').forEach(function (el) {
      el.textContent = unitText;
    });
    row.querySelectorAll('.cart-line-subtotal').forEach(function (el) {
      el.textContent = subtotalText;
      el.setAttribute('title', formulaText);
    });
    row.querySelectorAll('.cart-subtotal-formula').forEach(function (el) {
      el.textContent = formulaText;
    });
    var qtyEl = row.querySelector('.cart-qty-value');
    if (qtyEl) {
      qtyEl.value = String(qty);
    }
    updateSummary(items);
  }

  function removeAt(index) {
    var api = cartApi();
    if (!api || typeof api.removeItemAt !== 'function') {
      return;
    }
    api.removeItemAt(index);
    renderCart();
  }

  function removeMany(indices) {
    var api = cartApi();
    if (!api) {
      return;
    }
    if (typeof api.removeItemsAt === 'function') {
      api.removeItemsAt(indices);
    } else if (typeof api.removeItemAt === 'function') {
      indices
        .slice()
        .sort(function (a, b) {
          return b - a;
        })
        .forEach(function (idx) {
          api.removeItemAt(idx);
        });
    }
    renderCart();
  }

  function changeQty(index, delta) {
    var api = cartApi();
    var items = ensureItems();
    if (index < 0 || index >= items.length) {
      return;
    }
    var next = normalizeQty(items[index].qty) + delta;
    if (next < 1) {
      confirmRemove(index);
      return;
    }
    if (api && typeof api.updateItemAt === 'function') {
      api.updateItemAt(index, { qty: next });
    } else {
      items[index].qty = next;
      if (api && api.saveItems) {
        api.saveItems(items);
      }
    }
    refreshRowAmounts(index, next);
  }

  function confirmRemove(index) {
    var items = ensureItems();
    if (index < 0 || index >= items.length) {
      return;
    }
    var title = items[index].title || '이 상품';
    if (window.Swal) {
      window.Swal.fire({
        title: '장바구니에서 삭제할까요?',
        text: title.length > 60 ? title.slice(0, 60) + '…' : title,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        confirmButtonColor: '#d33'
      }).then(function (result) {
        if (result.isConfirmed) {
          removeAt(index);
          notify('상품을 삭제했습니다.', 'success');
        }
      });
      return;
    }
    if (window.confirm('장바구니에서 삭제할까요?')) {
      removeAt(index);
    }
  }

  function confirmRemoveSelected() {
    var indices = getSelectedIndices();
    if (!indices.length) {
      notify('삭제할 상품을 선택하세요.', 'warning');
      return;
    }
    var msg = '선택한 ' + indices.length + '개 상품을 장바구니에서 삭제할까요?';
    if (window.Swal) {
      window.Swal.fire({
        title: '선택 삭제',
        text: msg,
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        confirmButtonColor: '#d33'
      }).then(function (result) {
        if (result.isConfirmed) {
          removeMany(indices);
          notify(indices.length + '개 상품을 삭제했습니다.', 'success');
        }
      });
      return;
    }
    if (window.confirm(msg)) {
      removeMany(indices);
    }
  }

  function confirmClearAll() {
    if (!ensureItems().length) {
      return;
    }
    if (window.Swal) {
      window.Swal.fire({
        title: '장바구니를 비울까요?',
        text: '담은 상품이 모두 삭제됩니다.',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '전체 삭제',
        cancelButtonText: '취소',
        confirmButtonColor: '#d33'
      }).then(function (result) {
        if (result.isConfirmed) {
          var api = cartApi();
          if (api && api.clearItems) {
            api.clearItems();
          }
          renderCart();
          notify('장바구니를 비웠습니다.', 'success');
        }
      });
      return;
    }
    if (window.confirm('장바구니를 비울까요?')) {
      var api = cartApi();
      if (api && api.clearItems) {
        api.clearItems();
      }
      renderCart();
    }
  }

  function bind() {
    var list = document.getElementById('cart-item-list');
    if (list) {
      list.addEventListener('click', function (e) {
        var removeBtn = e.target.closest('.cart-remove-btn');
        if (removeBtn) {
          e.preventDefault();
          confirmRemove(parseInt(removeBtn.getAttribute('data-cart-index'), 10));
          return;
        }
        var minusBtn = e.target.closest('.cart-qty-minus');
        if (minusBtn) {
          e.preventDefault();
          changeQty(parseInt(minusBtn.getAttribute('data-cart-index'), 10), -1);
          return;
        }
        var plusBtn = e.target.closest('.cart-qty-plus');
        if (plusBtn) {
          e.preventDefault();
          changeQty(parseInt(plusBtn.getAttribute('data-cart-index'), 10), 1);
        }
      });

      list.addEventListener('change', function (e) {
        if (e.target && e.target.classList.contains('cart-item-check')) {
          syncSelectAllState();
          updateDeleteSelectedButton();
        }
      });
    }

    var selectAll = document.getElementById('cart-select-all');
    if (selectAll) {
      selectAll.addEventListener('change', function () {
        var checked = selectAll.checked;
        var checks = document.querySelectorAll('#cart-item-list .cart-item-check');
        checks.forEach(function (el) {
          el.checked = checked;
        });
        selectAll.indeterminate = false;
        updateDeleteSelectedButton();
      });
    }

    var deleteSelectedBtn = document.getElementById('btn-cart-delete-selected');
    if (deleteSelectedBtn) {
      deleteSelectedBtn.addEventListener('click', function (e) {
        e.preventDefault();
        confirmRemoveSelected();
      });
    }

    var clearBtn = document.getElementById('btn-cart-clear-all');
    if (clearBtn) {
      clearBtn.addEventListener('click', function (e) {
        e.preventDefault();
        confirmClearAll();
      });
    }

    document.querySelectorAll('#shoppingCartCard a[href*="checkout"]').forEach(function (link) {
      link.addEventListener('click', function (e) {
        if (!ensureItems().length) {
          e.preventDefault();
          notify('장바구니가 비어 있습니다.', 'warning');
        }
      });
    });

    var promoForm = document.getElementById('cart-promo-form');
    if (promoForm) {
      promoForm.addEventListener('submit', function (e) {
        e.preventDefault();
        var input = document.getElementById('cart-promo-code');
        var code = input ? input.value.trim() : '';
        if (!code) {
          notify('프로모 코드를 입력하세요.', 'warning');
          return;
        }
        notify('프로모 코드 "' + code + '" 적용 (데모)', 'success');
      });
    }
  }

  function boot() {
    bind();
    renderCart();
  }

  window.addEventListener('printmall-cart-updated', function () {
    if (document.getElementById('cart-item-list')) {
      renderCart();
    }
  });

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})();
