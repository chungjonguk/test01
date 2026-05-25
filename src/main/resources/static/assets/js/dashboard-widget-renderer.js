/**
 * 홈 대시보드 — 카탈로그·구성(localStorage) 기반 위젯 렌더링 (DOM 이동)
 * 정렬 시 그리드 깨짐 방지: 행 자동 분할 + 짝 맞춤(pe/ps) 클래스 제거
 */
(function (global) {
  'use strict';

  var HOST_ID = 'dashboard-widgets-host';
  var TEMPLATES_ID = 'dashboard-widget-templates';
  var STAGING_ID = 'dashboard-widget-staging';
  var RENDER_EVENT = 'dashboard-widgets-rendered';

  function getStore() {
    return global.DashboardConfigStore;
  }

  function getCatalog() {
    var list = global.__DASHBOARD_WIDGET_CATALOG__;
    return Array.isArray(list) ? list : [];
  }

  function readConfig() {
    if (getStore()) {
      return getStore().read();
    }
    return { hidden: [], order: [] };
  }

  function defaultOrder(catalog) {
    return catalog
      .filter(function (w) {
        return w.defaultEnabled !== false;
      })
      .sort(function (a, b) {
        return (a.sortOrder || 0) - (b.sortOrder || 0);
      })
      .map(function (w) {
        return w.id;
      });
  }

  function resolveVisibleIds(config, catalog) {
    var catalogIds = catalog.map(function (w) {
      return w.id;
    });
    var hidden = config.hidden || [];
    var hiddenSet = {};
    hidden.forEach(function (id) {
      hiddenSet[id] = true;
    });

    var order = config.order && config.order.length ? config.order.slice() : defaultOrder(catalog);
    catalogIds.forEach(function (id) {
      if (order.indexOf(id) === -1) {
        order.push(id);
      }
    });

    return order.filter(function (id) {
      return catalogIds.indexOf(id) !== -1 && !hiddenSet[id];
    });
  }

  function getStaging(templatesRoot) {
    return (
      document.getElementById(STAGING_ID) ||
      (templatesRoot && templatesRoot.querySelector('[data-dashboard-staging]'))
    );
  }

  /** Bootstrap 12칸 기준 행 너비 (대략) */
  function layoutTier(meta) {
    var colClass = (meta && meta.colClass) || '';
    if (/\bcol-12\b/.test(colClass) && !/\bcol-(sm|md|lg|xl|xxl)-/.test(colClass)) {
      return { tier: 'full', width: 12, hostClass: 'col-12' };
    }
    if (/\bcol-lg-7\b/.test(colClass) || /\bcol-xxl-6\b/.test(colClass) || /\bcol-xl-8\b/.test(colClass)) {
      return { tier: 'wide', width: 12, hostClass: 'col-12' };
    }
    if (/\bcol-lg-6\b/.test(colClass) || /\bcol-lg-5\b/.test(colClass)) {
      return { tier: 'half', width: 6, hostClass: 'col-12 col-lg-6' };
    }
    return { tier: 'quarter', width: 3, hostClass: 'col-12 col-sm-6 col-md-6 col-lg-4 col-xxl-3' };
  }

  function findWidgetCard(widgetId, templatesRoot, host, staging) {
    var roots = [staging, host, templatesRoot];
    for (var i = 0; i < roots.length; i++) {
      var root = roots[i];
      if (!root) {
        continue;
      }
      var card = root.querySelector('[data-dashboard-widget="' + widgetId + '"]');
      if (card) {
        return card;
      }
    }
    return null;
  }

  function columnOfCard(card) {
    if (!card || !card.parentElement) {
      return null;
    }
    var col = card.parentElement;
    if (/\bcol(-|\b)/.test(col.className)) {
      return col;
    }
    return null;
  }

  function findWidgetColumn(widgetId, templatesRoot, host, staging, catalog) {
    var card = findWidgetCard(widgetId, templatesRoot, host, staging);
    if (!card) {
      return null;
    }
    var col = columnOfCard(card);
    if (!col) {
      return null;
    }
    var meta = catalog.find(function (w) {
      return w.id === widgetId;
    });
    if (meta) {
      var layout = layoutTier(meta);
      col.className = layout.hostClass;
    }
    return col;
  }

  function unwrapHost(host, staging) {
    if (!host || !staging) {
      return;
    }
    var rows = host.querySelectorAll(':scope > .dashboard-widget-row');
    rows.forEach(function (row) {
      while (row.firstChild) {
        staging.appendChild(row.firstChild);
      }
      row.remove();
    });
    while (host.firstChild) {
      staging.appendChild(host.firstChild);
    }
  }

  function createRow(gutter) {
    var row = document.createElement('div');
    row.className = 'row ' + (gutter || 'g-3') + ' mb-3 dashboard-widget-row';
    return row;
  }

  function appendToHost(host, col, layout) {
    var currentRow = host.lastElementChild;
    var needNewRow =
      !currentRow ||
      !currentRow.classList.contains('dashboard-widget-row') ||
      currentRow.getAttribute('data-row-width-used') === null;

    var used = needNewRow ? 0 : parseInt(currentRow.getAttribute('data-row-width-used'), 10) || 0;

    if (!needNewRow && used + layout.width > 12) {
      needNewRow = true;
    }

    if (needNewRow) {
      currentRow = createRow(layout.tier === 'quarter' ? 'g-3' : 'g-0');
      currentRow.setAttribute('data-row-width-used', '0');
      host.appendChild(currentRow);
      used = 0;
    }

    currentRow.appendChild(col);
    used += layout.width;
    currentRow.setAttribute('data-row-width-used', String(used));
  }

  function notifyAfterRender(host) {
    global.setTimeout(function () {
      global.dispatchEvent(new Event('resize'));
      if (typeof global.initUsingStorageWidget === 'function') {
        global.initUsingStorageWidget(host);
      }
      global.dispatchEvent(new CustomEvent(RENDER_EVENT, { detail: { host: host } }));
    }, 120);
  }

  function apply() {
    var host = document.getElementById(HOST_ID);
    var templatesRoot = document.getElementById(TEMPLATES_ID);
    var staging = getStaging(templatesRoot);
    if (!host || !templatesRoot || !staging) {
      return false;
    }

    var catalog = getCatalog();
    var visibleIds = resolveVisibleIds(readConfig(), catalog);

    unwrapHost(host, staging);

    var missing = [];
    visibleIds.forEach(function (widgetId) {
      var meta = catalog.find(function (w) {
        return w.id === widgetId;
      });
      var layout = layoutTier(meta);
      var col = findWidgetColumn(widgetId, templatesRoot, host, staging, catalog);
      if (!col) {
        missing.push(widgetId);
        return;
      }
      appendToHost(host, col, layout);
    });

    if (missing.length && global.console && global.console.warn) {
      global.console.warn('[dashboard] template missing for:', missing.join(', '));
    }

    notifyAfterRender(host);
    return true;
  }

  global.DashboardWidgetRenderer = {
    apply: apply,
    resolveVisibleIds: resolveVisibleIds,
    getCatalog: getCatalog,
    RENDER_EVENT: RENDER_EVENT
  };
})(typeof window !== 'undefined' ? window : globalThis);
