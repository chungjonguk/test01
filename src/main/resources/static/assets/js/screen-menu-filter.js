/**
 * screen_list.use_yn = Y 인 메뉴만 사이드바에 표시
 */
(function () {
  function normalizePath(path) {
    if (!path) {
      return '/';
    }
    var trimmed = path.replace(/\/+$/, '').replace(/\.html$/, '');
    return trimmed.length ? trimmed : '/';
  }

  function hideEmptyMenuBranches(root) {
    var changed = true;
    while (changed) {
      changed = false;
      root.querySelectorAll('ul.nav.collapse').forEach(function (collapse) {
        var visibleChild = collapse.querySelector('.nav-item:not(.d-none) > a.nav-link[href]');
        if (!visibleChild) {
          var parentItem = collapse.closest('.nav-item');
          if (parentItem && !parentItem.classList.contains('d-none')) {
            parentItem.classList.add('d-none');
            changed = true;
          }
        }
      });
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    var navRoot = document.getElementById('navbarVerticalNav');
    var activeList = globalThis.__ACTIVE_SCREEN_URIS__;
    if (!navRoot || !activeList || !activeList.length) {
      return;
    }

    var activeSet = new Set(activeList.map(normalizePath));
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
      if (activeSet.has(normalizePath(url.pathname))) {
        return;
      }
      var item = link.closest('.nav-item');
      if (item) {
        item.classList.add('d-none');
      }
      link.classList.add('d-none');
    });

    hideEmptyMenuBranches(navRoot);
  });
})();
