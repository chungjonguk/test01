/**
 * 결제(Checkout) — KG이니시스 표준결제(INIStdPay) 연동
 */
(function () {
  'use strict';

  var DEFAULT_AMOUNT = 1004;
  var DEFAULT_GOOD_NAME = 'PrintMall 주문';

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

  function readAmount() {
    var el = document.getElementById('checkout-pay-amount');
    if (el && el.getAttribute('data-amount')) {
      var v = parseInt(el.getAttribute('data-amount'), 10);
      if (!isNaN(v) && v > 0) {
        return v;
      }
    }
    return DEFAULT_AMOUNT;
  }

  function buildPrepareBody() {
    var buyer = window.__CHECKOUT_BUYER__ || {};
    return {
      amount: readAmount(),
      goodName: DEFAULT_GOOD_NAME,
      buyerName: buyer.name || '테스트구매자',
      buyerTel: buyer.tel || '01012345678',
      buyerEmail: buyer.email || 'buyer@printmall.local',
      customerId: buyer.customerId || 1,
      shipTo: 'PrintMall 배송지'
    };
  }

  function fillInicisForm(params) {
    var form = document.getElementById('inicis-pay-form');
    if (!form) {
      return;
    }
    form.innerHTML = '';
    Object.keys(params).forEach(function (key) {
      if (key === 'orderNo' || key === 'stdPayJsUrl' || key === 'mock' || key === 'message' || key === 'success' || key === 'useRealGateway') {
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
    var url = '/app/e-commerce/checkout/inicis/success?orderNo=' + encodeURIComponent(orderNo || '');
    window.location.href = url;
  }

  function bind() {
    var addAddressBtn = document.getElementById('checkout-add-address-btn');
    if (addAddressBtn) {
      addAddressBtn.addEventListener('click', function (e) {
        e.preventDefault();
        notify('새 배송지 추가 (데모)', 'info');
      });
    }

    var form = document.getElementById('checkout-payment-form');
    if (!form) {
      return;
    }

    form.addEventListener('submit', function (e) {
      e.preventDefault();
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
            btn.disabled = false;
          }
        });
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bind);
  } else {
    bind();
  }
})();
