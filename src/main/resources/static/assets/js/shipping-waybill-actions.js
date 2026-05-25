/**
 * 운송장 발급 — /admin/shipping
 * API: /api/shipping
 */
(function () {
  'use strict';

  var API_BASE = '/api/shipping';

  function $(id) {
    return document.getElementById(id);
  }

  function notify(msg, type) {
    if (window.Swal) {
      Swal.fire({ icon: type || 'info', title: msg, timer: 2200, showConfirmButton: false });
      return;
    }
    window.alert(msg);
  }

  function carrierLabel(code) {
    if (code === 'CJ') return 'CJ대한통운';
    if (code === 'EPOST') return '우체국';
    if (code === 'LOTTE') return '롯데택배';
    return code || '-';
  }

  function renderHistory(items) {
    var tbody = $('shipping-history-body');
    if (!tbody) return;
    if (!items || !items.length) {
      tbody.innerHTML =
        '<tr><td colspan="6" class="text-center text-600 py-3">발급 이력이 없습니다.</td></tr>';
      return;
    }
    tbody.innerHTML = items
      .map(function (row) {
        return (
          '<tr>' +
          '<td>' +
          (row.shipmentId || '-') +
          '</td>' +
          '<td>' +
          (row.carrierLabel || carrierLabel(row.carrierCd)) +
          '</td>' +
          '<td><strong>' +
          (row.invoiceNo || '-') +
          '</strong></td>' +
          '<td>' +
          (row.statusCd || '-') +
          '</td>' +
          '<td>' +
          (row.recipientNm || '-') +
          '</td>' +
          '<td>' +
          (row.issuedDt || row.regDt || '-') +
          '</td>' +
          '</tr>'
        );
      })
      .join('');
  }

  function loadStatus() {
    fetch(API_BASE + '/status')
      .then(function (r) {
        return r.json();
      })
      .then(function (data) {
        var el = $('shipping-status-hint');
        if (!el) return;
        var mock = data.mockEnabled ? '목(Mock) 발급 가능' : '실 API';
        var carriers = (data.carriers || [])
          .map(function (c) {
            return c.label + (c.configured ? '(연동설정됨)' : '(목)');
          })
          .join(', ');
        el.textContent = mock + ' · ' + carriers;
      })
      .catch(function () {
        var el = $('shipping-status-hint');
        if (el) el.textContent = '연동 상태를 불러오지 못했습니다.';
      });
  }

  function loadHistory(orderId) {
    if (!orderId) return;
    fetch(API_BASE + '/orders/' + encodeURIComponent(orderId))
      .then(function (r) {
        return r.json();
      })
      .then(function (data) {
        if (!data.success) {
          notify(data.message || '조회 실패', 'warning');
          return;
        }
        renderHistory(data.items);
      })
      .catch(function () {
        notify('발급 이력 조회 중 오류가 발생했습니다.', 'error');
      });
  }

  function issueWaybill(e) {
    e.preventDefault();
    var orderId = parseInt($('ship-order-id').value, 10);
    var carrier = $('ship-carrier').value;
    var boxCnt = parseInt($('ship-box-cnt').value, 10) || 1;
    var weight = parseFloat($('ship-weight').value) || 1;
    if (!orderId || orderId < 1) {
      notify('주문 ID를 입력하세요.', 'warning');
      return;
    }
    fetch(API_BASE + '/waybill', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: orderId,
        carrierCd: carrier,
        boxCnt: boxCnt,
        weightKg: weight
      })
    })
      .then(function (r) {
        return r.json().then(function (body) {
          return { ok: r.ok, body: body };
        });
      })
      .then(function (res) {
        if (!res.ok || !res.body.success) {
          notify(res.body.message || '운송장 발급에 실패했습니다.', 'error');
          return;
        }
        var mockNote = res.body.mock ? ' (목)' : '';
        var inv = res.body.shipment && res.body.shipment.invoiceNo;
        notify('운송장: ' + (inv || '-') + mockNote, 'success');
        loadHistory(orderId);
      })
      .catch(function () {
        notify('운송장 발급 요청 중 오류가 발생했습니다.', 'error');
      });
  }

  function init() {
    loadStatus();
    var form = $('shipping-waybill-form');
    if (form) {
      form.addEventListener('submit', issueWaybill);
    }
    var orderInput = $('ship-order-id');
    if (orderInput) {
      orderInput.addEventListener('change', function () {
        var id = parseInt(orderInput.value, 10);
        if (id > 0) loadHistory(id);
      });
    }
  }

  if (document.getElementById('shipping-waybill-panel')) {
    init();
  }
})();
