/**

 * screen_list.use_yn = Y 인 메뉴만 사이드바에 표시

 * (data-menu-path · 구역(zone) · 논리 경로 키 매칭)

 */

(function () {

  'use strict';



  function normalizePath(path) {

    if (!path) {

      return '';

    }

    try {

      return new URL(path, window.location.origin).pathname;

    } catch (e) {

      return path;

    }

  }



  function pathKey(path) {

    var p = normalizePath(path);

    if (!p) {

      return '';

    }

    if (p.length > 3 && p.endsWith('.do')) {

      p = p.slice(0, -3);

    }

    if (p === '/index') {

      return '/';

    }

    return p;

  }



  function resolveMenuPathKey(link) {

    var fromData = link.getAttribute('data-menu-path');

    if (fromData) {

      return pathKey(fromData);

    }

    return pathKey(link.getAttribute('href'));

  }



  var ALWAYS_VISIBLE_PATH_KEYS = [

    '/',

    '/dashboard-home',

    '/shop-home',

    '/users',

    '/admin/menus',

    '/admin/codes',

    '/admin/companies',

    '/admin/dashboard-config',

    '/admin/company-page-images',
    '/admin/company-domains',

    '/admin/user-access-logs',

    '/admin/inventory',

    '/admin/shipping',

    '/admin/nas',

    '/admin/media-storage',

    '/app/e-commerce/orders/order-list',

    '/app/e-commerce/orders/order-details',

    '/app/e-commerce/customers',

    '/app/e-commerce/invoice',

    '/app/e-commerce/product/product-list',

    '/app/e-commerce/product/product-grid',

    '/app/e-commerce/billing',

    '/app/e-commerce/shopping-cart',

    '/app/e-commerce/checkout',

    '/pages/user/profile',

    '/pages/faq/faq-basic',

    '/dashboard/e-commerce'

  ];



  function isAlwaysVisibleMenuLink(link) {

    if (link && link.closest('[data-menu-zone="shopping-mall"]')) {

      return true;

    }

    var key = resolveMenuPathKey(link);

    if (!key) {

      return false;

    }

    for (var i = 0; i < ALWAYS_VISIBLE_PATH_KEYS.length; i += 1) {

      if (key === ALWAYS_VISIBLE_PATH_KEYS[i]) {

        return true;

      }

    }

    return false;

  }



  function buildPathKeySet(pathKeys) {

    var set = new Set();

    if (!pathKeys) {

      return set;

    }

    pathKeys.forEach(function (item) {

      if (item) {

        set.add(item);

      }

    });

    return set;

  }



  function buildActiveKeySet(activeList) {

    var set = new Set();

    if (!activeList) {

      return set;

    }

    activeList.forEach(function (item) {

      if (!item) {

        return;

      }

      set.add(normalizePath(item));

      set.add(pathKey(item));

    });

    return set;

  }



  function isPageLink(link) {

    var href = link.getAttribute('href');

    return href && href.charAt(0) !== '#' && href.indexOf('javascript:') !== 0;

  }



  function isMenuVisible(link, pathKeySet, activeKeySet) {

    if (isAlwaysVisibleMenuLink(link)) {

      return true;

    }

    var key = resolveMenuPathKey(link);

    if (key && pathKeySet.has(key)) {

      return true;

    }

    var href = link.getAttribute('href');

    if (!href) {

      return false;

    }

    try {

      var pathname = normalizePath(new URL(href, window.location.origin).pathname);

      return activeKeySet.has(pathname) || activeKeySet.has(pathKey(pathname));

    } catch (e) {

      return false;

    }

  }



  function hideEmptyMenuBranches(root) {

    root.querySelectorAll('ul.nav.collapse').forEach(function (collapse) {

      var visibleChild = collapse.querySelector(

        '.nav-item:not(.d-none) > a.nav-link[href]:not([href^="#"])'

      );

      var hidden = !visibleChild;

      collapse.classList.toggle('d-none', hidden);

      if (collapse.id) {

        var toggle = root.querySelector(

          '[href="#' + collapse.id + '"],[data-bs-target="#' + collapse.id + '"]'

        );

        if (toggle) {

          toggle.classList.toggle('d-none', hidden);

        }

      }

    });

  }



  function applyMenuFilter() {

    var navRoot = document.getElementById('navbarVerticalNav');

    var pathKeys = globalThis.__ACTIVE_SCREEN_PATH_KEYS__;

    var activeList = globalThis.__ACTIVE_SCREEN_URIS__;

    if (!navRoot) {

      return;

    }

    if ((!pathKeys || !pathKeys.length) && (!activeList || !activeList.length)) {

      return;

    }



    var pathKeySet = buildPathKeySet(pathKeys);

    var activeKeySet = buildActiveKeySet(activeList);

    var pageLinks = navRoot.querySelectorAll('a.nav-link[href]');

    var hiddenCount = 0;

    var totalPageLinks = 0;



    pageLinks.forEach(function (link) {

      if (!isPageLink(link)) {

        return;

      }

      totalPageLinks += 1;



      if (isMenuVisible(link, pathKeySet, activeKeySet)) {

        link.classList.remove('d-none');

        var showItem = link.closest('.nav-item');

        if (showItem) {

          showItem.classList.remove('d-none');

        }

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

    if (window.MenuLocation && window.MenuLocation.update) {

      window.MenuLocation.update();

    }

  }



  if (document.readyState === 'loading') {

    document.addEventListener('DOMContentLoaded', function () {

      runFilter();

      document.addEventListener('printmall-paths-ready', runFilter);

    });

  } else {

    runFilter();

    document.addEventListener('printmall-paths-ready', runFilter);

  }

})();


