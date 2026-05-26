/**
 * 사이드바 초기 표시·활성 메뉴 펼침
 */
(function () {
  'use strict';

  var LG_BREAKPOINT = 992;
  var Nav = window.MenuNavActive;

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

  function isSidebarIconOnlyMode() {
    return (
      document.documentElement.classList.contains('navbar-vertical-collapsed') &&
      !document.documentElement.classList.contains('navbar-vertical-collapsed-hover')
    );
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

  function clearMenuCurrentMarkers(navRoot) {
    navRoot.querySelectorAll('.menu-current, .menu-current-group, a.nav-link.active').forEach(function (el) {
      el.classList.remove('menu-current', 'menu-current-group', 'active');
      el.removeAttribute('aria-current');
      el.removeAttribute('title');
    });
    navRoot.querySelectorAll('.menu-current-item').forEach(function (el) {
      el.classList.remove('menu-current-item');
    });
    navRoot.querySelectorAll('.menu-current-badge').forEach(function (el) {
      el.remove();
    });
  }

  function appendCurrentBadge(link) {
    if (!link || link.classList.contains('dropdown-indicator') || isSidebarIconOnlyMode()) {
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

  function markCurrentMenuGroup(toggle) {
    if (!toggle) {
      return;
    }
    toggle.classList.add('menu-current-group');
    toggle.setAttribute('aria-expanded', 'true');
  }

  function scrollCurrentMenuIntoView(link) {
    if (!link) {
      return;
    }
    var scrollHost =
      document.querySelector('.navbar-vertical-content') ||
      document.getElementById('navbarVerticalCollapse');
    if (!scrollHost) {
      return;
    }
    requestAnimationFrame(function () {
      try {
        link.scrollIntoView({ block: 'nearest', inline: 'nearest', behavior: 'smooth' });
      } catch (e) {
        link.scrollIntoView(false);
      }
    });
  }

  function expandActiveMenuPath() {
    if (!Nav) {
      return;
    }
    var navRoot = document.getElementById('navbarVerticalNav');
    if (!navRoot) {
      return;
    }

    clearMenuCurrentMarkers(navRoot);

    var activeLink = Nav.findActiveMenuLink(navRoot);
    if (!activeLink) {
      return;
    }

    activeLink.classList.add('active', 'menu-current');
    activeLink.setAttribute('aria-current', 'page');
    activeLink.setAttribute('title', '현재 페이지');
    appendCurrentBadge(activeLink);

    var navItem = activeLink.closest('.nav-item');
    if (navItem) {
      navItem.classList.add('menu-current-item');
    }

    var parentCollapse = activeLink.closest('.collapse');
    while (parentCollapse) {
      parentCollapse.classList.add('show');
      var toggle = navRoot.querySelector(
        '[href="#' + parentCollapse.id + '"],[data-bs-target="#' + parentCollapse.id + '"]'
      );
      markCurrentMenuGroup(toggle);
      parentCollapse = parentCollapse.parentElement
        ? parentCollapse.parentElement.closest('.collapse')
        : null;
    }

    scrollCurrentMenuIntoView(activeLink);
  }

  function init() {
    ensureSidebarExpanded();
    expandActiveMenuPath();
    document.documentElement.classList.add('sidebar-menu-ready');
    if (window.MenuLocation && window.MenuLocation.update) {
      window.MenuLocation.update();
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  document.addEventListener('printmall-paths-ready', expandActiveMenuPath);

  window.addEventListener('resize', function () {
    if (isDesktopSidebar()) {
      ensureSidebarExpanded();
    }
    expandActiveMenuPath();
  });

  window.SidebarMenuInit = {
    ensureSidebarExpanded: ensureSidebarExpanded,
    expandActiveMenuPath: expandActiveMenuPath
  };
})();
