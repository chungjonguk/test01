/**
 * 대시보드 위젯 메뉴: Remove 시 해당 위젯(컬럼) 제거
 */
(function () {
  var STORAGE_KEY = 'dashboardRemovedWidgets';

  function readRemoved() {
    try {
      var raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return [];
      }
      var parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
      return [];
    }
  }

  function writeRemoved(ids) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(ids));
  }

  function getWidgetId(card) {
    return card.getAttribute('data-dashboard-widget');
  }

  function findWidgetColumn(card) {
    var parent = card.parentElement;
    if (parent && /\bcol(-|\b)/.test(parent.className)) {
      return parent;
    }
    return card.closest('[class*="col-"]') || card;
  }

  function removeWidget(card) {
    if (!card) {
      return;
    }
    var widgetId = getWidgetId(card);
    var col = findWidgetColumn(card);
    var target = col || card;
    target.style.transition = 'opacity 0.2s ease';
    target.style.opacity = '0';
    setTimeout(function () {
      target.remove();
    }, 200);

    if (widgetId) {
      var removed = readRemoved();
      if (removed.indexOf(widgetId) === -1) {
        removed.push(widgetId);
        writeRemoved(removed);
      }
    }
  }

  function applyStoredRemovals() {
    var removed = readRemoved();
    if (!removed.length) {
      return;
    }
    removed.forEach(function (id) {
      var card = document.querySelector('[data-dashboard-widget="' + id + '"]');
      if (card) {
        var col = findWidgetColumn(card);
        if (col) {
          col.remove();
        }
      }
    });
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
    e.preventDefault();
    e.stopPropagation();
    var card = removeBtn.closest('.card');
    if (!card) {
      return;
    }
    if (window.confirm('이 위젯을 대시보드에서 제거할까요?')) {
      removeWidget(card);
    }
  }

  function init() {
    applyStoredRemovals();
    document.body.addEventListener('click', onDocumentClick, true);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
