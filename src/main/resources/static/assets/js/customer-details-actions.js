/**
 * 고객 상세 — 버튼/메뉴 데모 동작
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

  function preventNav(e) {
    e.preventDefault();
  }

  function bindClick(id, message, icon) {
    var el = document.getElementById(id);
    if (!el) {
      return;
    }
    el.addEventListener('click', function (e) {
      preventNav(e);
      notify(message, icon);
    });
  }

  function bind() {
    bindClick('customer-add-note-btn', '메모 추가 (데모)', 'success');
    bindClick('customer-update-details-btn', '상세 정보 수정 (데모)', 'info');
    bindClick('customer-refund-btn', '환불 요청 (데모)', 'warning');
    bindClick('customer-save-btn', '변경 사항 저장 (데모)', 'success');
    bindClick('customer-more-logs-btn', '추가 로그는 백엔드 연동 시 표시됩니다.', 'info');

    document.querySelectorAll('.customer-action-link').forEach(function (link) {
      link.addEventListener('click', function (e) {
        preventNav(e);
        var action = link.getAttribute('data-action') || 'action';
        var labels = {
          edit: '고객 정보 수정',
          report: '신고 접수',
          archive: '고객 보관',
          delete: '고객 삭제'
        };
        var label = labels[action] || action;
        if (action === 'delete') {
          if (window.Swal) {
            window.Swal.fire({
              title: label + '?',
              text: '데모 화면입니다. 실제 삭제되지 않습니다.',
              icon: 'warning',
              showCancelButton: true,
              confirmButtonText: '확인',
              cancelButtonText: '취소'
            }).then(function (result) {
              if (result.isConfirmed) {
                notify(label + ' (데모)', 'success');
              }
            });
            return;
          }
        }
        notify(label + ' (데모)', action === 'delete' ? 'warning' : 'success');
      });
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bind);
  } else {
    bind();
  }
})();
