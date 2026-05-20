/**
 * 청구(Billing) — 플랜 변경, 결제 수단, 무료 체험, 문의 모달
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

  function updatePaymentFields() {
    var paypal = document.getElementById('paypal');
    var cardFields = document.getElementById('billing-card-fields');
    if (!cardFields) {
      return;
    }
    var usePaypal = paypal && paypal.checked;
    cardFields.style.display = usePaypal ? 'none' : '';
  }

  function selectPlan(option) {
    var name = option.getAttribute('data-plan-name');
    var price = option.getAttribute('data-plan-price');
    var summary = document.getElementById('billing-plan-summary');
    if (summary && name && price) {
      summary.innerHTML =
        name +
        ' — Free for 30 days, cancel at any time.<br />' +
        price +
        ' / month after trial';
    }
    var btn = document.getElementById('change-plan');
    if (btn && name) {
      btn.textContent = name;
    }
    notify(name + ' 플랜이 선택되었습니다.', 'success');
  }

  function bind() {
    document.querySelectorAll('input[name="billing"]').forEach(function (radio) {
      radio.addEventListener('change', updatePaymentFields);
    });
    updatePaymentFields();

    document.querySelectorAll('.billing-plan-option').forEach(function (option) {
      function onPick(e) {
        if (e.type === 'keydown' && e.key !== 'Enter' && e.key !== ' ') {
          return;
        }
        e.preventDefault();
        selectPlan(option);
      }
      option.addEventListener('click', onPick);
      option.addEventListener('keydown', onPick);
    });

    var form = document.getElementById('billing-form');
    if (form) {
      form.addEventListener('submit', function (e) {
        e.preventDefault();
        var method = document.getElementById('credit-card');
        var isCard = method && method.checked;
        if (isCard) {
          var cardNumber = document.getElementById('cardNumber');
          if (cardNumber && !cardNumber.value.trim()) {
            notify('카드 번호를 입력하세요.', 'warning');
            cardNumber.focus();
            return;
          }
        }
        var cycle = document.getElementById('billing-cycle');
        var cycleLabel = cycle ? cycle.options[cycle.selectedIndex].text : 'Monthly';
        notify('무료 체험이 시작되었습니다 (' + cycleLabel + ', 데모)', 'success');
      });
    }

    var questionForm = document.getElementById('billing-question-form');
    if (questionForm) {
      questionForm.addEventListener('submit', function (e) {
        e.preventDefault();
        if (!questionForm.checkValidity()) {
          questionForm.reportValidity();
          return;
        }
        var modalEl = document.getElementById('exampleModal');
        if (modalEl && window.bootstrap) {
          var modal = window.bootstrap.Modal.getInstance(modalEl);
          if (modal) {
            modal.hide();
          }
        }
        questionForm.reset();
        notify('문의가 접수되었습니다 (데모)', 'success');
      });
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bind);
  } else {
    bind();
  }
})();
