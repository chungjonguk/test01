/**
 * 결제(Checkout) — 주소 추가, 결제 확인
 */
(function () {
  'use strict';

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

  function bind() {
    var addAddressBtn = document.getElementById('checkout-add-address-btn');
    if (addAddressBtn) {
      addAddressBtn.addEventListener('click', function (e) {
        e.preventDefault();
        notify('새 배송지 추가 (데모)', 'info');
      });
    }

    var form = document.getElementById('checkout-payment-form');
    if (form) {
      form.addEventListener('submit', function (e) {
        e.preventDefault();
        var card = document.getElementById('credit-card');
        if (card && card.checked) {
          var num = document.getElementById('inputNumber');
          if (num && !num.value.trim()) {
            notify('카드 번호를 입력하세요.', 'warning');
            num.focus();
            return;
          }
        }
        notify('결제가 완료되었습니다 (데모)', 'success');
      });
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bind);
  } else {
    bind();
  }
})();
