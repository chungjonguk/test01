/**
 * select[data-code-group] 요소를 공통코드 옵션으로 채웁니다.
 * 서버에서 window.__CODE_OPTIONS__ (code_id → [{value,label}]) 가 주입되어 있어야 합니다.
 */
(function () {
  'use strict';

  function fillSelect(select) {
    var groupId = select.getAttribute('data-code-group');
    if (!groupId || !window.__CODE_OPTIONS__) {
      return;
    }
    var options = window.__CODE_OPTIONS__[groupId];
    if (!options || !options.length) {
      return;
    }
    if (select.getAttribute('data-code-hydrated') === 'true') {
      return;
    }
    var selected = select.getAttribute('data-code-selected');
    if (selected == null) {
      selected = select.value;
    }
    var preserve = select.getAttribute('data-code-preserve') === 'true';
    if (!preserve) {
      select.innerHTML = '';
    }
    options.forEach(function (opt) {
      var el = document.createElement('option');
      el.value = opt.value != null ? opt.value : '';
      el.textContent = opt.label != null ? opt.label : el.value;
      if (selected != null && String(selected) === String(el.value)) {
        el.selected = true;
      }
      select.appendChild(el);
    });
    select.setAttribute('data-code-hydrated', 'true');
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
