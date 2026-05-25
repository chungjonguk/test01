/**
 * 대시보드 위젯 Remove → 구성 저장 후 재렌더링
 */
(function () {
  var store = window.DashboardConfigStore;
  var renderer = window.DashboardWidgetRenderer;

  function readConfig() {
    if (store) {
      return store.read();
    }
    return { hidden: [], order: [] };
  }

  function hideWidget(widgetId) {
    if (!widgetId) {
      return;
    }
    var cfg = readConfig();
    if (cfg.hidden.indexOf(widgetId) === -1) {
      cfg.hidden.push(widgetId);
      if (store) {
        store.write(cfg);
      }
    }
    if (renderer) {
      renderer.apply();
    }
  }

  function isRemoveTrigger(el) {
    if (!el) {
      return false;
    }
    if (el.getAttribute('data-widget-action') === 'remove') {
      return true;
    }
    return (
      el.classList.contains('dropdown-item') &&
      el.classList.contains('text-danger') &&
      /remove/i.test((el.textContent || '').trim())
    );
  }

  function onDocumentClick(e) {
    var removeBtn = e.target.closest('[data-widget-action="remove"], .dropdown-menu .dropdown-item.text-danger');
    if (!isRemoveTrigger(removeBtn)) {
      return;
    }
    var host = document.getElementById('dashboard-widgets-host');
    if (!host || !host.contains(removeBtn)) {
      return;
    }
    e.preventDefault();
    e.stopPropagation();
    var card = removeBtn.closest('[data-dashboard-widget]');
    if (!card) {
      return;
    }
    var widgetId = card.getAttribute('data-dashboard-widget');
    if (window.confirm('이 위젯을 대시보드에서 제거할까요? 구성 화면에서 다시 켤 수 있습니다.')) {
      hideWidget(widgetId);
    }
  }

  function init() {
    if (!document.getElementById('dashboard-widgets-host')) {
      return;
    }
    if (renderer) {
      renderer.apply();
    }
    document.body.addEventListener('click', onDocumentClick, true);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
