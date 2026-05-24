/**
 * 뷰포트가 좁아질 때 데스크톱 전용 사이드바 상태를 정리하고 모바일 오버레이를 연동합니다.
 */
(function () {
  'use strict';

  var BREAKPOINTS = {
    'navbar-expand-sm': 576,
    'navbar-expand-md': 768,
    'navbar-expand-lg': 992,
    'navbar-expand-xl': 1200,
    'navbar-expand-xxl': 1400
  };

  function getSidebarBreakpoint() {
    var nav = document.querySelector('.navbar-vertical');
    if (!nav) {
      return 992;
    }
    var keys = Object.keys(BREAKPOINTS);
    for (var i = keys.length - 1; i >= 0; i--) {
      if (nav.classList.contains(keys[i])) {
        return BREAKPOINTS[keys[i]];
      }
    }
    return 992;
  }

  function hideVerticalCollapse() {
    var collapseEl = document.getElementById('navbarVerticalCollapse');
    if (!collapseEl || !collapseEl.classList.contains('show')) {
      return;
    }
    if (window.bootstrap && window.bootstrap.Collapse) {
      var instance = window.bootstrap.Collapse.getInstance(collapseEl);
      if (instance) {
        instance.hide();
        return;
      }
    }
    collapseEl.classList.remove('show');
  }

  function syncSidebarForViewport() {
    var nav = document.querySelector('.navbar-vertical');
    var html = document.documentElement;
    var bp = nav ? getSidebarBreakpoint() : 0;
    var narrow = nav ? window.innerWidth < bp : window.innerWidth < 992;

    if (narrow) {
      html.classList.remove('navbar-vertical-collapsed', 'navbar-vertical-collapsed-hover');
      hideVerticalCollapse();
    } else {
      var collapseEl = document.getElementById('navbarVerticalCollapse');
      if (collapseEl) {
        collapseEl.classList.add('show');
        var toggler = document.querySelector('[data-bs-target="#navbarVerticalCollapse"]');
        if (toggler) {
          toggler.setAttribute('aria-expanded', 'true');
        }
      }
    }
    document.body.classList.toggle('layout-viewport-narrow', narrow);
    document.body.classList.toggle('layout-has-sidebar', !!nav && !document.body.classList.contains('layout-hide-sidebar'));
  }

  function bindOverlay() {
    var collapseEl = document.getElementById('navbarVerticalCollapse');
    if (!collapseEl) {
      return;
    }
    collapseEl.addEventListener('show.bs.collapse', function () {
      if (window.innerWidth < getSidebarBreakpoint()) {
        document.body.classList.add('sidebar-overlay-active');
      }
    });
    collapseEl.addEventListener('hidden.bs.collapse', function () {
      document.body.classList.remove('sidebar-overlay-active');
    });
  }

  var resizeTimer;
  window.addEventListener('resize', function () {
    clearTimeout(resizeTimer);
    resizeTimer = setTimeout(syncSidebarForViewport, 120);
  });

  document.addEventListener('click', function (event) {
    if (!document.body.classList.contains('sidebar-overlay-active')) {
      return;
    }
    if (event.target.closest('#navbarVerticalCollapse')) {
      return;
    }
    if (event.target.closest('[data-bs-target="#navbarVerticalCollapse"]')) {
      return;
    }
    hideVerticalCollapse();
    document.body.classList.remove('sidebar-overlay-active');
  });

  document.addEventListener('DOMContentLoaded', function () {
    syncSidebarForViewport();
    bindOverlay();
  });
})();
