/**
 * 사이드바 초기 표시·활성 메뉴 펼침 (첫 로드 시 메뉴 구조 안 보임 방지)
 */
(function () {
  'use strict';

  var LG_BREAKPOINT = 992;

  function isDesktopSidebar() {
    return window.innerWidth >= LG_BREAKPOINT;
  }

  function getSidebarBreakpoint() {
    var nav = document.querySelector('.navbar-vertical');
    if (!nav) {
      return LG_BREAKPOINT;
    }
    var map = [
      ['navbar-expand-sm', 576],
      ['navbar-expand-md', 768],
      ['navbar-expand-lg', 992],
      ['navbar-expand-xl', 1200],
      ['navbar-expand-xxl', 1400]
    ];
    for (var i = map.length - 1; i >= 0; i--) {
      if (nav.classList.contains(map[i][0])) {
        return map[i][1];
      }
    }
    return LG_BREAKPOINT;
  }

  function ensureSidebarExpanded() {
    var collapseEl = document.getElementById('navbarVerticalCollapse');
    if (!collapseEl || document.body.classList.contains('layout-hide-sidebar')) {
      return;
    }
    if (window.innerWidth >= getSidebarBreakpoint()) {
      collapseEl.classList.add('show');
      var toggler = document.querySelector('[data-bs-target="#navbarVerticalCollapse"]');
      if (toggler) {
        toggler.setAttribute('aria-expanded', 'true');
      }
    }
  }

  function normalizePath(path) {
    if (!path) {
      return '/';
    }
    var trimmed = path.replace(/\/+$/, '').replace(/\.html$/, '');
    return trimmed.length ? trimmed : '/';
  }

  function expandActiveMenuPath() {
    var navRoot = document.getElementById('navbarVerticalNav');
    if (!navRoot) {
      return;
    }

    var currentPath = normalizePath(window.location.pathname);
    navRoot.querySelectorAll('a.nav-link[href]').forEach(function (link) {
      var href = link.getAttribute('href');
      if (!href || href.charAt(0) === '#' || href.indexOf('javascript:') === 0) {
        return;
      }
      var url;
      try {
        url = new URL(href, window.location.origin);
      } catch (e) {
        return;
      }
      if (normalizePath(url.pathname) !== currentPath) {
        return;
      }

      link.classList.add('active');
      var parentCollapse = link.closest('.collapse');
      while (parentCollapse) {
        parentCollapse.classList.add('show');
        var toggle = navRoot.querySelector(
          '[href="#' + parentCollapse.id + '"],[data-bs-target="#' + parentCollapse.id + '"]'
        );
        if (toggle) {
          toggle.classList.add('active');
          toggle.setAttribute('aria-expanded', 'true');
        }
        parentCollapse = parentCollapse.parentElement
          ? parentCollapse.parentElement.closest('.collapse')
          : null;
      }
    });
  }

  function init() {
    ensureSidebarExpanded();
    expandActiveMenuPath();
    document.documentElement.classList.add('sidebar-menu-ready');
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  window.addEventListener('resize', function () {
    if (isDesktopSidebar()) {
      ensureSidebarExpanded();
    }
  });

  window.SidebarMenuInit = {
    ensureSidebarExpanded: ensureSidebarExpanded,
    expandActiveMenuPath: expandActiveMenuPath
  };
})();
