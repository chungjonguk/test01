/**
 * 장바구니 — 프로모 코드, 상품 제거
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
        timer: 2500,
        timerProgressBar: true
      });
      return;
    }
    window.alert(message);
  }

  function bind() {
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

    document.querySelectorAll('.cart-remove-link').forEach(function (link) {
      link.addEventListener('click', function (e) {
        e.preventDefault();
        var row = link.closest('.row.gx-card');
        if (row && window.Swal) {
          window.Swal.fire({
            title: '상품을 제거할까요?',
            icon: 'question',
            showCancelButton: true,
            confirmButtonText: '제거',
            cancelButtonText: '취소'
          }).then(function (result) {
            if (result.isConfirmed) {
              row.remove();
              notify('상품이 제거되었습니다 (데모)', 'success');
            }
          });
          return;
        }
        if (row) {
          row.remove();
        }
        notify('상품이 제거되었습니다 (데모)', 'success');
      });
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bind);
  } else {
    bind();
  }
})();
