/**
 * 받은편지함: 메일 행 클릭 시 상세 화면으로 이동
 */
(function () {
  function init() {
    document.querySelectorAll('[data-inbox-row-href]').forEach(function (row) {
      var href = row.getAttribute('data-inbox-row-href');
      if (!href) {
        return;
      }
      row.style.cursor = 'pointer';
      row.addEventListener('click', function (e) {
        if (e.target.closest('a, button, input, label, .dropdown-menu')) {
          return;
        }
        window.location.href = href;
      });
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
