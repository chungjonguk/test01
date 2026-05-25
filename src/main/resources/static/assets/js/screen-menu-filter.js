/**
 * screen_list.use_yn = Y 인 메뉴만 사이드바에 표시
 */
(function () {
  'use strict';

  function normalizePath(path) {
    if (globalThis.PrintMallDoPath && globalThis.PrintMallDoPath.normalizePath) {
      return globalThis.PrintMallDoPath.normalizePath(path);
    }
    if (!path) {
      return '/index.do';
    }
    var trimmed = path.replace(/\/+$/, '').replace(/\.html$/, '');
    return trimmed.length ? trimmed : '/index.do';
  }

  function isPageLink(link) {
    var href = link.getAttribute('href');
    return href && href.charAt(0) !== '#' && href.indexOf('javascript:') !== 0;
  }

  function hideEmptyMenuBranches(root) {
    root.querySelectorAll('ul.nav.collapse').forEach(function (collapse) {
      var visibleChild = collapse.querySelector(
        '.nav-item:not(.d-none) > a.nav-link[href]:not([href^="#"])'
      );
      if (!visibleChild) {
        collapse.classList.add('d-none');
      } else {
        collapse.classList.remove('d-none');
      }
    });
  }

  function applyMenuFilter() {
    var navRoot = document.getElementById('navbarVerticalNav');
    var activeList = globalThis.__ACTIVE_SCREEN_URIS__;
    if (!navRoot || !activeList || !activeList.length) {
      return;
    }

    var activeSet = new Set(activeList.map(normalizePath));
    var pageLinks = navRoot.querySelectorAll('a.nav-link[href]');
    var hiddenCount = 0;
    var totalPageLinks = 0;

    pageLinks.forEach(function (link) {
      if (!isPageLink(link)) {
        return;
      }
      totalPageLinks += 1;

      var url;
      try {
        url = new URL(link.getAttribute('href'), window.location.origin);
      } catch (e) {
        return;
      }

      if (activeSet.has(normalizePath(url.pathname))) {
        return;
      }

      var item = link.closest('.nav-item');
      if (item) {
        item.classList.add('d-none');
      }
      link.classList.add('d-none');
      hiddenCount += 1;
    });

    if (totalPageLinks > 0 && hiddenCount >= totalPageLinks) {
      navRoot.querySelectorAll('.nav-item.d-none, a.nav-link.d-none').forEach(function (el) {
        el.classList.remove('d-none');
      });
      return;
    }

    hideEmptyMenuBranches(navRoot);
  }

  function runFilter() {
    if (window.SidebarMenuInit) {
      window.SidebarMenuInit.ensureSidebarExpanded();
    }
    applyMenuFilter();
    if (window.SidebarMenuInit) {
      window.SidebarMenuInit.expandActiveMenuPath();
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', runFilter);
  } else {
    runFilter();
  }
})();
