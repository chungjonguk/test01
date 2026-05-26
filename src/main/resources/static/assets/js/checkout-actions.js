/**
 * 결제(Checkout) — 장바구니 연동 + KG이니시스 표준결제(INIStdPay)
 */
(function () {
  'use strict';

  var DEFAULT_GOOD_NAME = 'PrintMall 주문';
  var SHIPPING_FEE = 0;
  var checkoutState = {
    items: [],
    subtotal: 0,
    total: 0,
    goodName: DEFAULT_GOOD_NAME
  };

  function notify(message, icon) {
    if (window.Swal) {
      window.Swal.fire({
        toast: true,
        position: 'top-end',
        icon: icon || 'info',
        title: message,
        showConfirmButton: false,
        timer: 3200,
        timerProgressBar: true
      });
      return;
    }
    window.alert(message);
  }

  function cartApi() {
    return window.PrintMallCartNav || null;
  }

  function parsePriceNumber(price) {
    var api = cartApi();
    if (api && typeof api.parsePriceNumber === 'function') {
      return api.parsePriceNumber(price);
    }
    if (price == null || price === '') {
      return 0;
    }
    var num = parseFloat(String(price).replace(/[^0-9.]/g, ''));
    return isNaN(num) ? 0 : num;
  }

  function normalizeQty(qty) {
    var api = cartApi();
    if (api && typeof api.normalizeQty === 'function') {
      return api.normalizeQty(qty);
    }
    var n = parseInt(qty, 10);
    return isNaN(n) || n < 1 ? 1 : n;
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

  function lineSubtotal(item) {
    return Math.round(getUnitPrice(item) * normalizeQty(item.qty) * 100) / 100;
  }

  function formatDisplayPrice(amount) {
    if (window.PrintMallCurrency && typeof window.PrintMallCurrency.formatWon === 'function') {
      return window.PrintMallCurrency.formatWon(amount);
    }
    return Math.round(amount).toLocaleString('ko-KR') + '원';
  }

  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/"/g, '&quot;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  function loadCartItems() {
    var api = cartApi();
    if (!api || typeof api.loadItems !== 'function') {
      return [];
    }
    return api.loadItems();
  }

  function buildGoodName(items) {
    if (!items.length) {
      return DEFAULT_GOOD_NAME;
    }
    if (items.length === 1) {
      var title = (items[0].title || '상품').trim();
      return title.length > 80 ? title.slice(0, 80) + '…' : title;
    }
    return 'PrintMall 주문 (' + items.length + '건)';
  }

  function calcTotals(items) {
    var subtotal = 0;
    items.forEach(function (item) {
      subtotal += lineSubtotal(item);
    });
    subtotal = Math.round(subtotal);
    var total = subtotal + SHIPPING_FEE;
    return {
      subtotal: subtotal,
      total: total,
      goodName: buildGoodName(items)
    };
  }

  function setPayAmountDisplay(amount) {
    var payAmount = Math.max(0, Math.round(amount));
    var text = payAmount > 0 ? formatDisplayPrice(payAmount) : '-';
    var els = [
      document.getElementById('checkout-pay-amount'),
      document.getElementById('checkout-pay-amount-primary'),
      document.getElementById('checkout-subtotal-amount'),
      document.getElementById('checkout-total-amount')
    ];
    els.forEach(function (el) {
      if (!el) {
        return;
      }
      if (el.id === 'checkout-subtotal-amount') {
        el.textContent = payAmount > 0 ? formatDisplayPrice(checkoutState.subtotal) : '-';
        return;
      }
      if (el.id === 'checkout-total-amount' || el.id === 'checkout-pay-amount' || el.id === 'checkout-pay-amount-primary') {
        el.textContent = text;
      }
      if (el.id === 'checkout-pay-amount') {
        el.setAttribute('data-amount', String(payAmount));
        el.setAttribute('data-currency-keep', 'true');
      }
    });
    var shippingEl = document.getElementById('checkout-shipping-amount');
    if (shippingEl) {
      shippingEl.textContent = formatDisplayPrice(SHIPPING_FEE);
    }
  }

  function setCheckoutEnabled(enabled) {
    var btn = document.getElementById('checkout-confirm-pay-btn');
    if (btn) {
      btn.disabled = !enabled;
    }
  }

  function renderOrderLines(items) {
    var tbody = document.getElementById('checkout-order-lines');
    var table = document.getElementById('checkout-order-table');
    var empty = document.getElementById('checkout-order-empty');
    if (!tbody) {
      return;
    }
    if (!items.length) {
      tbody.innerHTML = '';
      if (table) {
        table.classList.add('d-none');
      }
      if (empty) {
        empty.classList.remove('d-none');
      }
      setCheckoutEnabled(false);
      setPayAmountDisplay(0);
      return;
    }
    if (table) {
      table.classList.remove('d-none');
    }
    if (empty) {
      empty.classList.add('d-none');
    }
    tbody.innerHTML = items
      .map(function (item) {
        var qty = normalizeQty(item.qty);
        var unit = getUnitPrice(item);
        var line = lineSubtotal(item);
        var title = escapeHtml(item.title || '상품');
        return (
          '<tr class="border-bottom">' +
          '<th class="ps-3 py-2">' +
          escapeHtml(title) +
          ' × ' +
          qty +
          '<div class="text-400 fw-normal fs--2">단가 ' +
          escapeHtml(formatDisplayPrice(unit)) +
          '</div></th>' +
          '<th class="pe-3 py-2 text-end">' +
          escapeHtml(formatDisplayPrice(line)) +
          '</th></tr>'
        );
      })
      .join('');
    setCheckoutEnabled(true);
  }

  function syncFromCart() {
    var items = loadCartItems();
    var totals = calcTotals(items);
    checkoutState.items = items;
    checkoutState.subtotal = totals.subtotal;
    checkoutState.total = totals.total;
    checkoutState.goodName = totals.goodName;
    renderOrderLines(items);
    setPayAmountDisplay(totals.total);
    window.__CHECKOUT_CART__ = {
      items: items,
      subtotal: totals.subtotal,
      total: totals.total,
      goodName: totals.goodName
    };
  }

  function readAmount() {
    if (checkoutState.total > 0) {
      return checkoutState.total;
    }
    var el = document.getElementById('checkout-pay-amount');
    if (el && el.getAttribute('data-amount')) {
      var v = parseInt(el.getAttribute('data-amount'), 10);
      if (!isNaN(v) && v > 0) {
        return v;
      }
    }
    return 0;
  }

  function buildPrepareBody() {
    var buyer = window.__CHECKOUT_BUYER__ || {};
    return {
      amount: readAmount(),
      goodName: checkoutState.goodName || DEFAULT_GOOD_NAME,
      buyerName: buyer.name || '테스트구매자',
      buyerTel: buyer.tel || '01012345678',
      buyerEmail: buyer.email || 'buyer@printmall.local',
      customerId: buyer.customerId || 1,
      shipTo: 'PrintMall 배송지'
    };
  }

  function loadScript(url) {
    return new Promise(function (resolve, reject) {
      if (!url) {
        reject(new Error('스크립트 URL 없음'));
        return;
      }
      var existing = document.querySelector('script[data-inicis-stdpay="true"]');
      if (existing) {
        resolve();
        return;
      }
      var script = document.createElement('script');
      script.src = url;
      script.charset = 'UTF-8';
      script.setAttribute('data-inicis-stdpay', 'true');
      script.onload = function () {
        resolve();
      };
      script.onerror = function () {
        reject(new Error('이니시스 결제 스크립트 로드 실패'));
      };
      document.head.appendChild(script);
    });
  }

  function fillInicisForm(params) {
    var form = document.getElementById('inicis-pay-form');
    if (!form) {
      return;
    }
    form.innerHTML = '';
    Object.keys(params).forEach(function (key) {
      if (
        key === 'orderNo' ||
        key === 'stdPayJsUrl' ||
        key === 'mock' ||
        key === 'message' ||
        key === 'success' ||
        key === 'useRealGateway'
      ) {
        return;
      }
      var input = document.createElement('input');
      input.type = 'hidden';
      input.name = key;
      input.value = params[key] == null ? '' : String(params[key]);
      form.appendChild(input);
    });
  }

  function openInicisPay(params) {
    return loadScript(params.stdPayJsUrl).then(function () {
      fillInicisForm(params);
      if (typeof window.INIStdPay === 'undefined') {
        throw new Error('INIStdPay 객체를 찾을 수 없습니다.');
      }
      window.INIStdPay.pay('inicis-pay-form');
    });
  }

  function mockPay(orderNo) {
    return fetch('/api/payments/inicis/mock-complete?orderNo=' + encodeURIComponent(orderNo), {
      method: 'POST',
      headers: { Accept: 'application/json' }
    }).then(function (res) {
      return res.json().then(function (data) {
        return { ok: res.ok, data: data };
      });
    });
  }

  function preparePayment() {
    return fetch('/api/payments/inicis/prepare', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify(buildPrepareBody())
    }).then(function (res) {
      return res.json().then(function (data) {
        return { ok: res.ok, data: data };
      });
    });
  }

  function goSuccessPage(orderNo) {
    var api = cartApi();
    if (api && typeof api.clearItems === 'function') {
      api.clearItems();
    }
    var url = '/app/e-commerce/checkout/inicis/success?orderNo=' + encodeURIComponent(orderNo || '');
    window.location.href = url;
  }

  function bind() {
    var addAddressBtn = document.getElementById('checkout-add-address-btn');
    if (addAddressBtn) {
      addAddressBtn.addEventListener('click', function (e) {
        e.preventDefault();
        notify('배송지 추가 (데모)', 'info');
      });
    }

    var form = document.getElementById('checkout-payment-form');
    if (!form) {
      return;
    }

    form.addEventListener('submit', function (e) {
      e.preventDefault();
      if (!checkoutState.items.length || readAmount() <= 0) {
        notify('장바구니가 비어 있습니다. 상품을 담은 뒤 결제해 주세요.', 'warning');
        return;
      }
      var btn = document.getElementById('checkout-confirm-pay-btn');
      if (btn) {
        btn.disabled = true;
      }

      preparePayment()
        .then(function (result) {
          if (!result.ok || !result.data.success) {
            throw new Error((result.data && result.data.message) || '결제 준비 실패');
          }
          var data = result.data;
          if (data.mock) {
            return mockPay(data.orderNo).then(function (mockResult) {
              if (!mockResult.ok || !mockResult.data.success) {
                throw new Error((mockResult.data && mockResult.data.message) || '모의 결제 실패');
              }
              notify(mockResult.data.message || '모의 결제 완료', 'success');
              goSuccessPage(mockResult.data.orderNo);
            });
          }
          return openInicisPay(data);
        })
        .catch(function (err) {
          notify(err.message || '결제 처리 오류', 'error');
        })
        .finally(function () {
          if (btn) {
            btn.disabled = !checkoutState.items.length;
          }
        });
    });
  }

  function boot() {
    syncFromCart();
    bind();
  }

  window.addEventListener('printmall-cart-updated', function () {
    if (document.getElementById('checkoutPageRoot')) {
      syncFromCart();
    }
  });

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', boot);
  } else {
    boot();
  }
})();
