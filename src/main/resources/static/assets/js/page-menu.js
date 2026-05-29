/**
 * 사이드바 메뉴 — 서버가 내려준 __PAGE_MENU_CONTEXT__ 만 사용 (단일 진입점)
 */
(function (global) {
  'use strict';

  var LG = 992;

  /** zone(현재 화면) 필터와 무관하게 항상 표시 */
  var ALWAYS_VISIBLE_ZONES = ['admin', 'shopping-mall', 'dashboard'];

  function ctx() {
    return global.__PAGE_MENU_CONTEXT__ || null;
  }

  function pathKey(path) {
    if (!path) {
      return '';
    }
    var p = String(path).trim();
    if (p.indexOf('://') >= 0) {
      try {
        p = new URL(p, global.location.origin).pathname;
      } catch (e) {
        /* ignore */
      }
    }
    var q = p.indexOf('?');
    if (q >= 0) {
      p = p.substring(0, q);
    }
    if (p.length > 3 && p.slice(-3) === '.do') {
      p = p.slice(0, -3);
    }
    if (p.length > 1 && p.charAt(p.length - 1) === '/') {
      p = p.slice(0, -1);
    }
    if (p === '/index') {
      return '/';
    }
    return p || '/';
  }

  function linkKey(link) {
    var fromData = link.getAttribute('data-menu-path');
    if (fromData && fromData.indexOf('/e/') !== 0) {
      return pathKey(fromData);
    }
    var href = link.getAttribute('href');
    if (href && href.indexOf('/e/') !== 0) {
      return pathKey(href);
    }
    return '';
  }

  function linkZone(link) {
    var zoneLi = link.closest('[data-menu-zone]');
    return zoneLi ? zoneLi.getAttribute('data-menu-zone') || '' : '';
  }

  function navRoot() {
    return document.getElementById('navbarVerticalNav');
  }

  function activePathSet(c) {
    var set = Object.create(null);
    (c.activePaths || []).forEach(function (p) {
      if (p) {
        set[pathKey(p)] = true;
      }
    });
    if (c.pathKey) {
      set[pathKey(c.pathKey)] = true;
    }
    return set;
  }

  function isPersistentZone(zone) {
    return zone && ALWAYS_VISIBLE_ZONES.indexOf(zone) >= 0;
  }

  function isRoleMenuAllowed(key) {
    if (!key) {
      return false;
    }
    var c = ctx();
    if (!c || !c.roleCd || c.canWriteAll || c.roleCd === 'PLATFORM_ADMIN') {
      return true;
    }
    if (!c.allowedMenuPaths || !c.allowedMenuPaths.length) {
      return false;
    }
    var want = pathKey(key);
    for (var i = 0; i < c.allowedMenuPaths.length; i++) {
      if (pathKey(c.allowedMenuPaths[i]) === want) {
        return true;
      }
    }
    return false;
  }

  function shouldShowLink(link, c, active) {
    if (link.getAttribute('data-menu-persist') === 'true') {
      return true;
    }
    var key = linkKey(link);
    if (!key) {
      return false;
    }
    return isRoleMenuAllowed(key);
  }

  function setLinkVisible(link, show) {
    var hideTarget =
      link.closest('li.nav-item') ||
      link.closest('.nav-item') ||
      link.parentElement;
    if (!hideTarget) {
      return;
    }
    hideTarget.classList.toggle('d-none', !show);
    hideTarget.setAttribute('aria-hidden', show ? 'false' : 'true');
  }

  function applyVisibility() {
    var c = ctx();
    var nav = navRoot();
    if (!c || !nav) {
      return;
    }
    var active = activePathSet(c);
    nav.querySelectorAll('a.nav-link[href]').forEach(function (link) {
      if (link.classList.contains('dropdown-indicator')) {
        return;
      }
      setLinkVisible(link, shouldShowLink(link, c, active));
    });
    nav.querySelectorAll('li.nav-item[data-menu-zone]').forEach(function (zoneLi) {
      var zone = zoneLi.getAttribute('data-menu-zone');
      var showZone = !c.zone || zone === c.zone || isPersistentZone(zone);
      if (!showZone) {
        var links = zoneLi.querySelectorAll('a.nav-link[href]');
        for (var i = 0; i < links.length; i++) {
          var li = links[i].closest('li.nav-item') || links[i].parentElement;
          if (li && !li.classList.contains('d-none')) {
            showZone = true;
            break;
          }
        }
      }
      zoneLi.classList.toggle('d-none', !showZone);
    });
  }

  function clearMarkers(nav) {
    nav.querySelectorAll('.menu-current, .menu-current-group, a.nav-link.active').forEach(function (el) {
      el.classList.remove('menu-current', 'menu-current-group', 'active');
      el.removeAttribute('aria-current');
    });
    nav.querySelectorAll('.menu-current-item').forEach(function (el) {
      el.classList.remove('menu-current-item');
    });
    nav.querySelectorAll('.menu-current-badge').forEach(function (el) {
      el.remove();
    });
  }

  function appendBadge(link) {
    if (
      !link ||
      link.classList.contains('dropdown-indicator') ||
      (document.documentElement.classList.contains('navbar-vertical-collapsed') &&
        !document.documentElement.classList.contains('navbar-vertical-collapsed-hover'))
    ) {
      return;
    }
    if (link.querySelector('.menu-current-badge')) {
      return;
    }
    var badge = document.createElement('span');
    badge.className = 'menu-current-badge';
    badge.textContent = '현재';
    badge.setAttribute('aria-hidden', 'true');
    var flex = link.querySelector('.d-flex.align-items-center') || link;
    flex.appendChild(badge);
  }

  function findActiveLink(nav, c) {
    if (!c.pathKey) {
      return null;
    }
    var want = pathKey(c.pathKey);
    var links = nav.querySelectorAll('a.nav-link[href]');
    var picked = null;
    links.forEach(function (link) {
      if (link.classList.contains('dropdown-indicator')) {
        return;
      }
      if (linkKey(link) !== want) {
        return;
      }
      var z = linkZone(link);
      if (c.zone && z && z !== c.zone) {
        return;
      }
      if (!picked || (c.zone && z === c.zone)) {
        picked = link;
      }
    });
    return picked;
  }

  function expandToLink(link, nav) {
    var parentCollapse = link.closest('.collapse');
    while (parentCollapse) {
      parentCollapse.classList.add('show');
      var toggle = nav.querySelector(
        '[href="#' + parentCollapse.id + '"],[data-bs-target="#' + parentCollapse.id + '"]'
      );
      if (toggle) {
        toggle.classList.add('menu-current-group');
        toggle.setAttribute('aria-expanded', 'true');
      }
      parentCollapse = parentCollapse.parentElement
        ? parentCollapse.parentElement.closest('.collapse')
        : null;
    }
  }

  var CART_PATH_KEY = '/app/e-commerce/shopping-cart';
  var NOTIFICATION_PATH_KEYS = ['/app/social/notifications', '/app/social/notification-list'];

  function markActiveLink(link, nav) {
    link.classList.add('active', 'menu-current');
    link.setAttribute('aria-current', 'page');
    appendBadge(link);
    var item = link.closest('.nav-item');
    if (item) {
      item.classList.add('menu-current-item');
    }
    expandToLink(link, nav);
  }

  function applyActive() {
    var c = ctx();
    var nav = navRoot();
    if (!c || !nav) {
      return;
    }
    clearMarkers(nav);
    var want = pathKey(c.pathKey);
    if (want === CART_PATH_KEY) {
      var cartLinks = [];
      nav.querySelectorAll('a.nav-link[href]').forEach(function (link) {
        if (link.classList.contains('dropdown-indicator')) {
          return;
        }
        if (linkKey(link) !== CART_PATH_KEY) {
          return;
        }
        cartLinks.push(link);
      });
      if (!cartLinks.length) {
        return;
      }
      cartLinks.forEach(function (link) {
        markActiveLink(link, nav);
      });
      if (global.PrintMallCartNav && typeof global.PrintMallCartNav.refresh === 'function') {
        global.PrintMallCartNav.refresh();
      }
      return;
    }
    if (NOTIFICATION_PATH_KEYS.indexOf(want) >= 0) {
      var notificationLinks = [];
      nav.querySelectorAll('a.nav-link[href]').forEach(function (link) {
        if (link.classList.contains('dropdown-indicator')) {
          return;
        }
        if (NOTIFICATION_PATH_KEYS.indexOf(linkKey(link)) < 0) {
          return;
        }
        notificationLinks.push(link);
      });
      if (!notificationLinks.length) {
        return;
      }
      notificationLinks.forEach(function (link) {
        markActiveLink(link, nav);
      });
      if (
        global.PrintMallNotificationNav &&
        typeof global.PrintMallNotificationNav.refresh === 'function'
      ) {
        global.PrintMallNotificationNav.refresh();
      }
      return;
    }
    var link = findActiveLink(nav, c);
    if (!link) {
      return;
    }
    markActiveLink(link, nav);
  }

  function ensureSidebarExpanded() {
    var collapseEl = document.getElementById('navbarVerticalCollapse');
    if (!collapseEl || document.body.classList.contains('layout-hide-sidebar')) {
      return;
    }
    if (global.innerWidth >= LG) {
      collapseEl.classList.add('show');
      var toggler = document.querySelector('[data-bs-target="#navbarVerticalCollapse"]');
      if (toggler) {
        toggler.setAttribute('aria-expanded', 'true');
      }
    }
  }

  function applyAll() {
    applyVisibility();
    applyActive();
    document.documentElement.classList.add('sidebar-menu-ready');
  }

  function init() {
    if (document.body.classList.contains('layout-hide-sidebar')) {
      return;
    }
    ensureSidebarExpanded();
    applyAll();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  document.addEventListener('printmall-paths-ready', applyAll);
  global.addEventListener('resize', ensureSidebarExpanded);

  global.PageMenu = { applyAll: applyAll };
})(typeof window !== 'undefined' ? window : globalThis);
