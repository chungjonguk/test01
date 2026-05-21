/**
 * 이메일 상세(데모): 툴바·이전/다음 버튼 클릭 피드백
 */
(function () {
  var MESSAGES = {
    archive: '보관함으로 이동했습니다. (데모)',
    delete: '메일을 삭제했습니다. (데모)',
    unread: '읽지 않음으로 표시했습니다. (데모)',
    snooze: '나중에 알림으로 예약했습니다. (데모)',
    print: '인쇄 대화상자를 엽니다. (데모)',
    prev: '이전 메일입니다. (데모)',
    next: '다음 메일입니다. (데모)'
  };

  function notify(action) {
    var text = MESSAGES[action] || '처리되었습니다. (데모)';
    if (window.Swal) {
      Swal.fire({
        icon: 'success',
        title: text,
        timer: 1600,
        showConfirmButton: false
      });
      return;
    }
    window.alert(text);
  }

  function onClick(e) {
    var btn = e.target.closest('[data-email-action]');
    if (!btn || !document.querySelector('.email-detail-page')) {
      return;
    }
    var action = btn.getAttribute('data-email-action');
    if (!action) {
      return;
    }
    e.preventDefault();
    notify(action);
  }

  function init() {
    document.body.addEventListener('click', onClick);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
