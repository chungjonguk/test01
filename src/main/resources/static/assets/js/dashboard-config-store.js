/**
 * 업체별 홈 대시보드 위젯 구성 (localStorage 캐시 + 서버 API)
 */
(function (global) {
  'use strict';

  var LEGACY_KEY = 'dashboardLayoutConfig';
  var LEGACY_REMOVED = 'dashboardRemovedWidgets';
  var VERSION = 2;
  var companyId = null;
  var serverSnapshot = null;

  function parseJson(raw) {
    try {
      return JSON.parse(raw);
    } catch (e) {
      return null;
    }
  }

  function defaultConfig() {
    return { version: VERSION, hidden: [], order: [] };
  }

  function normalizeConfig(raw) {
    if (!raw || typeof raw !== 'object') {
      return defaultConfig();
    }
    return {
      version: VERSION,
      hidden: Array.isArray(raw.hidden) ? raw.hidden.filter(Boolean) : [],
      order: Array.isArray(raw.order) ? raw.order.filter(Boolean) : []
    };
  }

  function storageKey(id) {
    return 'dashboardLayoutConfig:company:' + (id != null ? String(id) : '0');
  }

  function getCompanyId() {
    if (companyId != null) {
      return companyId;
    }
    var fromPage = global.__DASHBOARD_COMPANY_ID__;
    if (fromPage != null && fromPage !== '') {
      return Number(fromPage);
    }
    return null;
  }

  function setCompanyId(id) {
    companyId = id != null ? Number(id) : null;
    serverSnapshot = null;
  }

  function applyServerSnapshot(snapshot) {
    if (!snapshot || snapshot.companyId == null) {
      return;
    }
    setCompanyId(snapshot.companyId);
    serverSnapshot = normalizeConfig({
      hidden: snapshot.hidden,
      order: snapshot.order
    });
    writeLocal(serverSnapshot);
  }

  function readLocal() {
    var key = storageKey(getCompanyId());
    var stored = parseJson(localStorage.getItem(key));
    if (stored) {
      return normalizeConfig(stored);
    }
    var legacy = parseJson(localStorage.getItem(LEGACY_KEY));
    if (legacy) {
      return normalizeConfig(legacy);
    }
    return null;
  }

  function writeLocal(cfg) {
    localStorage.setItem(storageKey(getCompanyId()), JSON.stringify(normalizeConfig(cfg)));
  }

  function read() {
    if (serverSnapshot && getCompanyId() != null) {
      return normalizeConfig(serverSnapshot);
    }
    var fromPage = global.__DASHBOARD_LAYOUT_CONFIG__;
    if (fromPage && fromPage.companyId != null && Number(fromPage.companyId) === Number(getCompanyId())) {
      serverSnapshot = normalizeConfig(fromPage);
      writeLocal(serverSnapshot);
      return serverSnapshot;
    }
    var local = readLocal();
    if (local) {
      return local;
    }
    return defaultConfig();
  }

  function write(cfg) {
    var normalized = normalizeConfig(cfg);
    serverSnapshot = normalized;
    writeLocal(normalized);
    return saveToServer(normalized);
  }

  function saveToServer(cfg) {
    var id = getCompanyId();
    if (id == null) {
      return Promise.resolve(cfg);
    }
    return fetch('/api/dashboard/config', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        companyId: id,
        hidden: cfg.hidden,
        order: cfg.order
      })
    })
      .then(function (res) {
        if (!res.ok) {
          throw new Error('HTTP ' + res.status);
        }
        return res.json();
      })
      .then(function (body) {
        if (body && body.config) {
          applyServerSnapshot(body.config);
        }
        return read();
      });
  }

  function loadFromServer(id) {
    setCompanyId(id);
    return fetch('/api/dashboard/config?companyId=' + encodeURIComponent(id))
      .then(function (res) {
        if (!res.ok) {
          throw new Error('HTTP ' + res.status);
        }
        return res.json();
      })
      .then(function (body) {
        applyServerSnapshot(body);
        return read();
      });
  }

  function selectCompany(id) {
    return fetch('/api/dashboard/selected-company', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ companyId: id })
    })
      .then(function (res) {
        if (!res.ok) {
          throw new Error('HTTP ' + res.status);
        }
        return res.json();
      })
      .then(function (body) {
        if (body && body.config) {
          applyServerSnapshot(body.config);
        } else {
          setCompanyId(id);
        }
        return read();
      });
  }

  function reset() {
    var id = getCompanyId();
    localStorage.removeItem(storageKey(id));
    localStorage.removeItem(LEGACY_KEY);
    localStorage.removeItem(LEGACY_REMOVED);
    serverSnapshot = defaultConfig();
    return saveToServer(serverSnapshot);
  }

  function initFromPage() {
    if (global.__DASHBOARD_LAYOUT_CONFIG__) {
      applyServerSnapshot(global.__DASHBOARD_LAYOUT_CONFIG__);
    } else if (global.__DASHBOARD_COMPANY_ID__ != null) {
      setCompanyId(global.__DASHBOARD_COMPANY_ID__);
    }
  }

  initFromPage();

  global.DashboardConfigStore = {
    VERSION: VERSION,
    getCompanyId: getCompanyId,
    setCompanyId: setCompanyId,
    read: read,
    write: write,
    loadFromServer: loadFromServer,
    selectCompany: selectCompany,
    reset: reset,
    defaultConfig: defaultConfig,
    applyServerSnapshot: applyServerSnapshot
  };
})(typeof window !== 'undefined' ? window : globalThis);
