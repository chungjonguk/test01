/**
 * select[data-code-group] — 서버 주입 window.__CODE_OPTIONS__ 로 옵션 채움 (SSR 보조).
 */
(function () {
  'use strict';

  function fillSelect(select) {
    var groupId = select.getAttribute('data-code-group');
    if (!groupId || select.getAttribute('data-code-hydrated') === 'true') {
      return;
    }
    if (window.PrintMallCommon && window.PrintMallCommon.fillCodeSelect) {
      window.PrintMallCommon.fillCodeSelect(select, groupId, select.getAttribute('data-code-selected'));
    }
  }

  function init() {
    if (!window.__CODE_OPTIONS__) {
      return;
    }
    document.querySelectorAll('select[data-code-group]').forEach(fillSelect);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
