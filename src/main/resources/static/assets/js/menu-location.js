/**

 * 콘텐츠 상단 — 사이드바 기준 메뉴 위치(브레드크럼) 표시

 */

(function () {

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

    if (globalThis.__CURRENT_SCREEN_PATH_KEY__) {

      return pathKey(globalThis.__CURRENT_SCREEN_PATH_KEY__);

    }

    return pathKey(window.location.pathname);

  }



  function isPageLink(link) {

    var href = link.getAttribute('href');

    return href && href.charAt(0) !== '#' && href.indexOf('javascript:') !== 0;

  }



  function resolveMenuPathKey(link) {

    var fromData = link.getAttribute('data-menu-path');

    if (fromData) {

      return pathKey(fromData);

    }

    return pathKey(link.getAttribute('href'));

  }



  function linkText(link) {

    if (!link) {

      return '';

    }

    var textEl = link.querySelector('.nav-link-text');

    if (textEl) {

      return textEl.textContent.replace(/\s+/g, ' ').trim();

    }

    return link.textContent.replace(/\s+/g, ' ').trim();

  }



  function findSectionLi(navRoot, link) {

    var el = link.parentElement;

    while (el && el !== navRoot) {

      if (el.matches && el.matches('li.nav-item') && el.querySelector('.navbar-vertical-label')) {

        return el;

      }

      el = el.parentElement;

    }

    return null;

  }



  function findActiveLink() {
    var navRoot = document.getElementById('navbarVerticalNav');
    if (!navRoot) {
      return null;
    }
    if (window.MenuNavActive && window.MenuNavActive.findActiveMenuLink) {
      return window.MenuNavActive.findActiveMenuLink(navRoot);
    }
    return null;
  }



  function collectLocationTrail() {

    var navRoot = document.getElementById('navbarVerticalNav');

    if (!navRoot) {

      return [];

    }



    var activeLink = findActiveLink();

    if (!activeLink) {

      return fallbackTrailFromServer();

    }



    var trail = [];

    var sectionLi = findSectionLi(navRoot, activeLink);

    if (sectionLi) {

      var sectionLabel = sectionLi.querySelector('.navbar-vertical-label');

      if (sectionLabel) {

        var labelText = sectionLabel.textContent.replace(/\s+/g, ' ').trim();

        if (labelText) {

          trail.push({ type: 'section', text: labelText });

        }

      }

    }



    var collapse = activeLink.closest('.collapse');

    var parentCollapses = [];

    while (collapse && collapse !== navRoot) {

      parentCollapses.unshift(collapse);

      collapse = collapse.parentElement ? collapse.parentElement.closest('.collapse') : null;

    }



    parentCollapses.forEach(function (collapseEl) {

      var id = collapseEl.id;

      if (!id) {

        return;

      }

      var toggle = navRoot.querySelector(

        '[data-bs-target="#' + id + '"],[href="#' + id + '"]'

      );

      var text = linkText(toggle);

      if (text) {

        trail.push({ type: 'group', text: text });

      }

    });



    var currentText = linkText(activeLink);

    if (currentText) {

      trail.push({ type: 'page', text: currentText });

    }



    return trail;

  }



  function fallbackTrailFromServer() {
    var ctx = getScreenContextEl();
    var screenNm = ctx ? ctx.getAttribute('data-screen-nm') : null;

    if (screenNm) {

      return [{ type: 'page', text: screenNm.replace(/\s+/g, ' ').trim() }];

    }

    return [];

  }



  function renderBreadcrumb(trail) {

    var ol = document.getElementById('menu-location-breadcrumb');

    if (!ol) {

      return;

    }



    var homeHref = '/';

    var brand = document.querySelector('a.navbar-brand');

    if (brand && brand.getAttribute('href')) {

      homeHref = brand.getAttribute('href');

    }

    var homeItem =

      '<li class="breadcrumb-item"><a href="' +

      escapeHtml(homeHref) +

      '"><span class="fas fa-home me-1"></span>홈</a></li>';



    if (!trail.length) {

      return;

    }



    var items = [homeItem];

    trail.forEach(function (crumb, index) {

      var isLast = index === trail.length - 1;

      if (isLast) {

        items.push(

          '<li class="breadcrumb-item active" aria-current="page">' +

            escapeHtml(crumb.text) +

            '</li>'

        );

      } else {

        items.push('<li class="breadcrumb-item">' + escapeHtml(crumb.text) + '</li>');

      }

    });



    ol.innerHTML = items.join('');



    var titleEl = document.querySelector('#menu-location-bar h5');

    if (titleEl && trail.length) {

      titleEl.textContent = trail[trail.length - 1].text;

    }



    var pathBadge = document.getElementById('menu-location-path-badge');

    var pathText = document.getElementById('menu-location-path-text');

    if (pathBadge && pathText) {

      var pathLabel = trail.map(function (c) {

        return c.text;

      }).join(' › ');

      if (pathLabel) {

        pathText.textContent = pathLabel;

        pathBadge.classList.remove('d-none');

      } else {

        pathBadge.classList.add('d-none');

      }

    }

  }



  function escapeHtml(text) {

    return String(text)

      .replace(/&/g, '&amp;')

      .replace(/</g, '&lt;')

      .replace(/>/g, '&gt;')

      .replace(/"/g, '&quot;');

  }



  function update() {

    renderBreadcrumb(collectLocationTrail());

  }



  function init() {

    var bar = document.getElementById('menu-location-bar');

    if (!bar || document.body.classList.contains('layout-hide-sidebar')) {

      return;

    }

    update();

  }



  if (document.readyState === 'loading') {

    document.addEventListener('DOMContentLoaded', function () {

      document.addEventListener('printmall-paths-ready', init);

      setTimeout(init, 300);

    });

  } else {

    document.addEventListener('printmall-paths-ready', init);

    init();

  }



  window.MenuLocation = {

    update: update,

    collectLocationTrail: collectLocationTrail,

    getCurrentMenuPathKey: getCurrentMenuPathKey

  };

})();


