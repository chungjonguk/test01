/**

 * Settings 패널: 모든 설정 변경 적용 (change 이벤트 기반).

 * Navigation: vertical = 왼쪽 사이드바만, top = 상단 내비만 (페이지 이동 없음).

 */

(function () {

  var DEFAULT_CONFIG = {

    isNavbarVerticalCollapsed: false,

    theme: 'light',

    isRTL: false,

    isFluid: false,

    navbarStyle: 'vibrant',

    navbarPosition: 'vertical'

  };



  function writeStore(key, value) {

    if (typeof value === 'boolean') {

      localStorage.setItem(key, JSON.stringify(value));

    } else {

      localStorage.setItem(key, value);

    }

  }



  function readStore(key, fallback) {

    try {

      var raw = localStorage.getItem(key);

      if (raw === null || raw === '') {

        return fallback;

      }

      try {

        return JSON.parse(raw);

      } catch (e) {

        return raw;

      }

    } catch (err) {

      return fallback;

    }

  }



  function parseBool(value) {

    return value === true || value === 'true' || value === '1';

  }



  function normalizeNavbarPosition(pos) {

    if (pos === 'combo') {

      pos = 'vertical';

      writeStore('navbarPosition', pos);

    }

    return pos === 'top' ? 'top' : 'vertical';

  }



  function applyNavbarPosition(position) {

    position = normalizeNavbarPosition(position);

    var html = document.documentElement;

    html.setAttribute('data-navbar-position', position);

    html.classList.remove('layout-nav-vertical', 'layout-nav-top');

    html.classList.add(position === 'top' ? 'layout-nav-top' : 'layout-nav-vertical');

    var sidebar = document.getElementById('layoutSidebar');

    var topNav = document.getElementById('layoutNavbarTop');

    var headerVertical = document.getElementById('layoutHeaderVertical');

    var isTop = position === 'top';

    if (sidebar) {

      sidebar.style.display = isTop ? 'none' : '';

      sidebar.classList.toggle('d-none', isTop);

    }

    if (topNav) {

      topNav.style.display = isTop ? '' : 'none';

      topNav.classList.toggle('d-none', !isTop);

    }

    if (headerVertical) {

      headerVertical.style.display = isTop ? 'none' : '';

      headerVertical.classList.toggle('d-none', isTop);

    }

    document.querySelectorAll('.content').forEach(function (content) {

      if (isTop) {

        content.style.marginLeft = '0';

      } else {

        content.style.removeProperty('margin-left');

      }

    });



    var panel = getPanel();

    if (panel) {

      panel.querySelectorAll('.settings-navbar-style-section').forEach(function (el) {

        if (position === 'top') {

          el.classList.add('d-none');

        } else {

          el.classList.remove('d-none');

        }

      });

    }

  }



  function getPanel() {

    var panels = document.querySelectorAll('#settings-offcanvas');

    return panels.length ? panels[panels.length - 1] : null;

  }



  function dedupeSettingsPanels() {

    var panels = document.querySelectorAll('#settings-offcanvas');

    if (panels.length <= 1) {

      return;

    }

    var keep = panels[panels.length - 1];

    panels.forEach(function (panel) {

      if (panel === keep) {

        return;

      }

      var next = panel.nextElementSibling;

      if (next && next.classList.contains('setting-toggle')) {

        next.remove();

      }

      panel.remove();

    });

    var toggles = document.querySelectorAll('a.setting-toggle');

    for (var i = 0; i < toggles.length - 1; i++) {

      toggles[i].remove();

    }

  }



  function ensureSettingsVisible(panel) {

    if (!panel) {

      return;

    }

    panel.querySelectorAll('.text-warning').forEach(function (el) {

      var text = (el.textContent || '').toLowerCase();

      if (text.indexOf('example page') !== -1 || text.indexOf('cann\'t') !== -1 || text.indexOf('can\'t') !== -1) {

        el.remove();

      }

    });

    panel.querySelectorAll('.d-none').forEach(function (el) {

      if (el.querySelector('[data-theme-control]')) {

        el.classList.remove('d-none');

      }

    });

    panel.querySelectorAll('[data-theme-control]').forEach(function (input) {

      input.disabled = false;

      input.removeAttribute('disabled');

    });

    panel.querySelectorAll('.btn-group-navbar-style.d-none').forEach(function (el) {

      el.classList.remove('d-none');

    });

  }



  function applyRtl(enabled) {

    var html = document.documentElement;

    var linkDefault = document.getElementById('style-default');

    var linkRtl = document.getElementById('style-rtl');

    var userDefault = document.getElementById('user-style-default');

    var userRtl = document.getElementById('user-style-rtl');

    if (enabled) {

      html.setAttribute('dir', 'rtl');

      if (linkDefault) {

        linkDefault.setAttribute('disabled', '');

      }

      if (userDefault) {

        userDefault.setAttribute('disabled', '');

      }

      if (linkRtl) {

        linkRtl.removeAttribute('disabled');

      }

      if (userRtl) {

        userRtl.removeAttribute('disabled');

      }

    } else {

      html.setAttribute('dir', 'ltr');

      if (linkRtl) {

        linkRtl.setAttribute('disabled', '');

      }

      if (userRtl) {

        userRtl.setAttribute('disabled', '');

      }

      if (linkDefault) {

        linkDefault.removeAttribute('disabled');

      }

      if (userDefault) {

        userDefault.removeAttribute('disabled');

      }

    }

  }



  function applyFluid(enabled) {

    var container = document.querySelector('[data-layout]');

    if (!container) {

      return;

    }

    if (enabled) {

      container.classList.remove('container');

      container.classList.add('container-fluid');

    } else {

      container.classList.remove('container-fluid');

      container.classList.add('container');

    }

  }



  function applyNavbarStyle(value) {

    var navbarVertical = document.querySelector('.navbar-vertical');

    if (!navbarVertical) {

      return;

    }

    navbarVertical.classList.remove('navbar-card', 'navbar-inverted', 'navbar-vibrant');

    if (value && value !== 'transparent') {

      navbarVertical.classList.add('navbar-' + value);

    }

  }



  function applyTheme(value) {

    document.documentElement.classList.toggle('dark', value === 'dark');

    document.body.dispatchEvent(

      new CustomEvent('clickControl', {

        detail: { control: 'theme', value: value }

      })

    );

  }



  function syncThemeControls(panel) {

    if (!panel) {

      return;

    }

    var theme = readStore('theme', 'light');

    var isFluid = parseBool(readStore('isFluid', false));

    var navbarPosition = normalizeNavbarPosition(readStore('navbarPosition', 'vertical'));

    var navbarStyle = readStore('navbarStyle', 'vibrant');



    panel.querySelectorAll('[data-theme-control="theme"]').forEach(function (input) {

      if (input.type === 'radio') {

        input.checked = input.value === theme;

      }

    });

    panel.querySelectorAll('[data-theme-control="isFluid"]').forEach(function (input) {

      input.checked = isFluid;

    });

    panel.querySelectorAll('[data-theme-control="navbarPosition"]').forEach(function (input) {

      if (input.value === 'combo') {

        return;

      }

      input.checked = input.value === navbarPosition;

    });

    panel.querySelectorAll('[data-theme-control="navbarStyle"]').forEach(function (input) {

      if (input.type === 'radio') {

        input.checked = input.value === navbarStyle;

      }

    });

  }



  function updateStatusBadge(panel) {

    var badge = panel && panel.querySelector('#settingsCurrentStatus');

    if (!badge) {

      return;

    }

    badge.textContent =

      '현재: Theme=' +

      readStore('theme', 'light') +

      ', Fluid=' +

      (parseBool(readStore('isFluid', false)) ? 'ON' : 'OFF') +

      ', Nav=' +

      normalizeNavbarPosition(readStore('navbarPosition', 'vertical')) +

      ', Style=' +

      readStore('navbarStyle', 'vibrant');

  }



  function navigateNavbarPosition(input) {

    var value = normalizeNavbarPosition(input.value);

    writeStore('navbarPosition', value);

    applyNavbarPosition(value);

    sessionStorage.setItem('settingsOffcanvasOpen', '1');

    window.location.reload();

  }



  function applySettingChange(el) {

    var control = el.getAttribute('data-theme-control');

    var value = el.type === 'checkbox' ? el.checked : el.value;



    switch (control) {

      case 'theme':

        writeStore('theme', value);

        applyTheme(value);

        break;

      case 'isFluid':

        writeStore('isFluid', !!value);

        applyFluid(!!value);

        break;

      case 'navbarStyle':

        writeStore('navbarStyle', value);

        applyNavbarStyle(value);

        break;

      case 'navbarPosition':

        navigateNavbarPosition(el);

        return;

      default:

        return;

    }



    syncThemeControls(getPanel());

    updateStatusBadge(getPanel());

  }



  function resetAllSettings() {

    var config = typeof CONFIG !== 'undefined' ? CONFIG : DEFAULT_CONFIG;

    Object.keys(DEFAULT_CONFIG).forEach(function (key) {

      var val = config[key] !== undefined ? config[key] : DEFAULT_CONFIG[key];

      writeStore(key, val);

    });

    localStorage.removeItem('dashboardRemovedWidgets');

    sessionStorage.setItem('settingsOffcanvasOpen', '1');

    window.location.href = '/dashboard';

  }



  function blockThemeJsOnSettingsClicks() {

    document.body.addEventListener(

      'click',

      function (e) {

        var el = e.target.closest('#settings-offcanvas [data-theme-control]');

        if (!el || el.getAttribute('data-theme-control') === 'reset') {

          return;

        }

        e.stopImmediatePropagation();

      },

      true

    );

  }



  function bindSettingsHandlers() {

    document.body.addEventListener(

      'change',

      function (e) {

        var el = e.target;

        if (!el.matches || !el.matches('#settings-offcanvas [data-theme-control]')) {

          return;

        }

        var control = el.getAttribute('data-theme-control');

        if (control === 'reset') {

          return;

        }

        applySettingChange(el);

      },

      true

    );



    document.body.addEventListener(

      'click',

      function (e) {

        var btn = e.target.closest('[data-theme-control="reset"]');

        if (!btn || !btn.closest('#settings-offcanvas')) {

          return;

        }

        e.preventDefault();

        e.stopImmediatePropagation();

        resetAllSettings();

      },

      true

    );

  }



  function reopenOffcanvasIfNeeded() {

    if (sessionStorage.getItem('settingsOffcanvasOpen') !== '1') {

      return;

    }

    sessionStorage.removeItem('settingsOffcanvasOpen');

    var panel = getPanel();

    if (panel && window.bootstrap) {

      window.bootstrap.Offcanvas.getOrCreateInstance(panel).show();

    }

  }



  function init() {

    dedupeSettingsPanels();

    blockThemeJsOnSettingsClicks();

    bindSettingsHandlers();

    var panel = getPanel();

    ensureSettingsVisible(panel);

    var navPos = normalizeNavbarPosition(readStore('navbarPosition', 'vertical'));

    applyNavbarPosition(navPos);

    syncThemeControls(panel);

    updateStatusBadge(panel);

    applyTheme(readStore('theme', 'light'));

    writeStore('isRTL', false);

    applyRtl(false);

    applyFluid(parseBool(readStore('isFluid', false)));

    applyNavbarStyle(readStore('navbarStyle', 'vibrant'));

    reopenOffcanvasIfNeeded();

  }



  if (document.readyState === 'loading') {

    document.addEventListener('DOMContentLoaded', init);

  } else {

    init();

  }

})();


