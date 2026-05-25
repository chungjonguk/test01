/**
 * 홈 대시보드 — 구성(localStorage) + 크기별 행(밴드) 렌더링
 */
(function (global) {
  'use strict';

  var HOST_ID = 'dashboard-widgets-host';
  var TEMPLATES_ID = 'dashboard-widget-templates';
  var STAGING_ID = 'dashboard-widget-staging';
  var RENDER_EVENT = 'dashboard-widgets-rendered';
  var Rules = global.DashboardLayoutRules;

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
    if (Rules) {
      return Rules.getVisibleIds(config.order, config.hidden, catalog);
    }
    return [];
  }

  function getStaging(templatesRoot) {
    return (
      document.getElementById(STAGING_ID) ||
      (templatesRoot && templatesRoot.querySelector('[data-dashboard-staging]'))
    );
  }

  function findWidgetCard(widgetId, templatesRoot, host, staging) {
    var roots = [staging, host, templatesRoot];
    var i;
    for (i = 0; i < roots.length; i++) {
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
    var parent = card.parentElement;
    if (
      parent.classList.contains('dashboard-widget-cell') ||
      /\bcol(-|\b)/.test(parent.className)
    ) {
      return parent;
    }
    return null;
  }

  function findWidgetColumn(widgetId, templatesRoot, host, staging) {
    var card = findWidgetCard(widgetId, templatesRoot, host, staging);
    if (!card) {
      return null;
    }
    return columnOfCard(card);
  }

  function prepareColumn(col, type) {
    var colClasses = Rules ? Rules.colClassForBandType(type) : 'col-12';
    col.className = colClasses + ' dashboard-widget-cell';
    var card = col.querySelector(':scope > .card');
    if (!card) {
      var root = col.querySelector(':scope > [data-dashboard-widget]');
      if (root && root.classList.contains('card')) {
        card = root;
      }
    }
    if (card) {
      card.classList.add('h-100');
      card.style.minWidth = '0';
    }
  }

  function unwrapHost(host, staging) {
    if (!host || !staging) {
      return;
    }
    var bands = host.querySelectorAll(':scope > .dashboard-widget-band');
    bands.forEach(function (band) {
      while (band.firstChild) {
        staging.appendChild(band.firstChild);
      }
      band.remove();
    });
    while (host.firstChild) {
      staging.appendChild(host.firstChild);
    }
  }

  function createBandRow(type) {
    var row = document.createElement('div');
    row.className = 'row g-3 mb-3 dashboard-widget-band';
    row.setAttribute('data-band-type', type);
    return row;
  }

  function notifyAfterRender(host) {
    global.setTimeout(function () {
      global.dispatchEvent(new Event('resize'));
      if (typeof global.initUsingStorageWidget === 'function') {
        global.initUsingStorageWidget(host);
      }
      global.dispatchEvent(new CustomEvent(RENDER_EVENT, { detail: { host: host } }));
    }, 150);
  }

  function apply() {
    var host = document.getElementById(HOST_ID);
    var templatesRoot = document.getElementById(TEMPLATES_ID);
    var staging = getStaging(templatesRoot);
    if (!host || !templatesRoot || !staging || !Rules) {
      return false;
    }

    var catalog = getCatalog();
    var config = readConfig();
    if (!config.order || !config.order.length) {
      config.order = Rules.autoArrangeOrder(catalog, config.hidden || []);
    }

    var validation = Rules.validate({
      hidden: config.hidden,
      catalog: catalog
    });

    if (!validation.valid) {
      if (global.console && global.console.warn) {
        global.console.warn('[dashboard] no visible widgets:', validation.errors);
      }
      return false;
    }

    var visibleIds = Rules.getVisibleIds(null, config.hidden, catalog);

    var bands = Rules.simulateBands(visibleIds, catalog);
    unwrapHost(host, staging);

    var missing = [];
    bands.forEach(function (band) {
      var row = createBandRow(band.type);
      band.widgetIds.forEach(function (widgetId) {
        var col = findWidgetColumn(widgetId, templatesRoot, host, staging);
        if (!col) {
          missing.push(widgetId);
          return;
        }
        prepareColumn(col, band.type);
        row.appendChild(col);
      });
      if (row.childElementCount) {
        host.appendChild(row);
      }
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
