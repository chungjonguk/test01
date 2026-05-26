/**
 * 사이드바 현재 메뉴 선택 — 동일 URL이 여러 섹션에 있을 때 구역(zone) 우선
 */
(function (global) {
  'use strict';

  function pathKey(path) {
    if (!path) {
      return '';
    }
    var p = String(path).trim();
    if (p.indexOf('://') >= 0) {
      try {
        p = new URL(p, window.location.origin).pathname;
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

  function getScreenContextEl() {
    return (
      document.querySelector('body[data-menu-path-key]') ||
      document.querySelector('[data-menu-path-key]') ||
      document.body
    );
  }

  function getCurrentMenuPathKey() {
    var ctx = getScreenContextEl();
    if (ctx) {
      var fromBody = ctx.getAttribute('data-menu-path-key');
      if (fromBody) {
        return pathKey(fromBody);
      }
    }
    if (global.__CURRENT_SCREEN_PATH_KEY__) {
      return pathKey(global.__CURRENT_SCREEN_PATH_KEY__);
    }
    return pathKey(global.location.pathname);
  }

  function getPreferredMenuZone() {
    var ctx = getScreenContextEl();
    return ctx ? ctx.getAttribute('data-menu-zone') || '' : '';
  }

  function resolveMenuPathKey(link) {
    var fromData = link.getAttribute('data-menu-path');
    if (fromData) {
      return pathKey(fromData);
    }
    return pathKey(link.getAttribute('href'));
  }

  function isPageLink(link) {
    var href = link.getAttribute('href');
    return href && href.charAt(0) !== '#' && href.indexOf('javascript:') !== 0;
  }

  function getMenuZone(link) {
    var zoneHost = link.closest('[data-menu-zone]');
    return zoneHost ? zoneHost.getAttribute('data-menu-zone') || '' : '';
  }

  function collapseDepth(link, navRoot) {
    var depth = 0;
    var el = link;
    while (el && el !== navRoot) {
      if (el.classList && el.classList.contains('collapse')) {
        depth += 1;
      }
      el = el.parentElement;
    }
    return depth;
  }

  function pickShallowest(matches, navRoot) {
    var best = matches[0];
    var minDepth = collapseDepth(best, navRoot);
    for (var i = 1; i < matches.length; i += 1) {
      var depth = collapseDepth(matches[i], navRoot);
      if (depth < minDepth) {
        minDepth = depth;
        best = matches[i];
      }
    }
    return best;
  }

  function pickActiveMenuLink(matches, navRoot) {
    if (!matches || !matches.length) {
      return null;
    }
    if (matches.length === 1) {
      return matches[0];
    }

    var preferredZone = getPreferredMenuZone();
    if (preferredZone) {
      var zoned = matches.filter(function (link) {
        return getMenuZone(link) === preferredZone;
      });
      if (zoned.length) {
        return pickShallowest(zoned, navRoot);
      }
    }

    return pickShallowest(matches, navRoot);
  }

  function isSidebarIconOnlyMode() {
    var doc = global.document && global.document.documentElement;
    return (
      doc &&
      doc.classList.contains('navbar-vertical-collapsed') &&
      !doc.classList.contains('navbar-vertical-collapsed-hover')
    );
  }

  /** 접힌 사이드바: 숨겨진 collapse 안 링크는 현재 메뉴 후보에서 제외 */
  function isVisibleInCollapsedSidebar(link, navRoot) {
    if (!isSidebarIconOnlyMode()) {
      return true;
    }
    var collapse = link.closest('.collapse');
    while (collapse && collapse !== navRoot) {
      if (!collapse.classList.contains('show')) {
        return false;
      }
      collapse = collapse.parentElement
        ? collapse.parentElement.closest('.collapse')
        : null;
    }
    return link.offsetParent !== null;
  }

  function findMatchingMenuLinks(navRoot, currentKey) {
    if (!navRoot || !currentKey) {
      return [];
    }
    var matches = [];
    navRoot.querySelectorAll('a.nav-link[href]').forEach(function (link) {
      if (!isPageLink(link) || !isVisibleInCollapsedSidebar(link, navRoot)) {
        return;
      }
      if (resolveMenuPathKey(link) === currentKey) {
        matches.push(link);
      }
    });
    return matches;
  }

  function findActiveMenuLink(navRoot) {
    if (!navRoot) {
      return null;
    }
    var currentKey = getCurrentMenuPathKey();
    if (!currentKey) {
      return null;
    }
    return pickActiveMenuLink(findMatchingMenuLinks(navRoot, currentKey), navRoot);
  }

  global.MenuNavActive = {
    pathKey: pathKey,
    getCurrentMenuPathKey: getCurrentMenuPathKey,
    getPreferredMenuZone: getPreferredMenuZone,
    resolveMenuPathKey: resolveMenuPathKey,
    isPageLink: isPageLink,
    findMatchingMenuLinks: findMatchingMenuLinks,
    pickActiveMenuLink: pickActiveMenuLink,
    findActiveMenuLink: findActiveMenuLink
  };
})(typeof window !== 'undefined' ? window : globalThis);
