/**
 * 홈 대시보드 위젯 표시 설정 (localStorage).
 * @module dashboard-config-store
 */
(function (global) {
  'use strict';

  var STORAGE_KEY = 'dashboardLayoutConfig';
  var LEGACY_KEY = 'dashboardRemovedWidgets';
  var VERSION = 1;

  function parseJson(raw) {
    try {
      return JSON.parse(raw);
    } catch (e) {
      return null;
    }
  }

  function migrateLegacy() {
    var legacy = parseJson(localStorage.getItem(LEGACY_KEY));
    if (!Array.isArray(legacy) || !legacy.length) {
      return null;
    }
    return { version: VERSION, hidden: legacy.slice(), order: [] };
  }

  function defaultConfig() {
    return { version: VERSION, hidden: [], order: [] };
  }

  function normalizeConfig(raw) {
    if (!raw || typeof raw !== 'object') {
      return defaultConfig();
    }
    var hidden = Array.isArray(raw.hidden) ? raw.hidden.filter(Boolean) : [];
    var order = Array.isArray(raw.order) ? raw.order.filter(Boolean) : [];
    return { version: VERSION, hidden: hidden, order: order };
  }

  function read() {
    var stored = parseJson(localStorage.getItem(STORAGE_KEY));
    if (!stored) {
      var migrated = migrateLegacy();
      if (migrated) {
        write(migrated);
        return migrated;
      }
      return defaultConfig();
    }
    return normalizeConfig(stored);
  }

  function write(config) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(normalizeConfig(config)));
  }

  function isHidden(widgetId) {
    return read().hidden.indexOf(widgetId) !== -1;
  }

  function setHidden(widgetIds) {
    var cfg = read();
    cfg.hidden = widgetIds.slice();
    write(cfg);
    return cfg;
  }

  function setOrder(orderIds) {
    var cfg = read();
    cfg.order = orderIds.slice();
    write(cfg);
    return cfg;
  }

  function reset() {
    localStorage.removeItem(STORAGE_KEY);
    localStorage.removeItem(LEGACY_KEY);
    return defaultConfig();
  }

  global.DashboardConfigStore = {
    STORAGE_KEY: STORAGE_KEY,
    read: read,
    write: write,
    isHidden: isHidden,
    setHidden: setHidden,
    setOrder: setOrder,
    reset: reset,
    defaultConfig: defaultConfig
  };
})(typeof window !== 'undefined' ? window : globalThis);
