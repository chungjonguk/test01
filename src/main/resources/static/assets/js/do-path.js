/**
 * PrintMall — 암호화 공개 URL (/e/{token}.do)
 * - fetch·링크 경로를 서버 API로 암호화
 * - 메뉴 필터용 경로 정규화
 */
(function (global) {
  'use strict';

  var EXCLUDED_PREFIXES = ['/auth/', '/assets/', '/vendors/', '/uploads/', '/error', '/api/'];
  var ENCRYPT_PREFIX = '/e/';
  var cache = Object.create(null);
  var pending = Object.create(null);

  function stripQuery(path) {
    if (!path) {
      return path;
    }
    var q = path.indexOf('?');
    var query = q >= 0 ? path.substring(q) : '';
    var base = q >= 0 ? path.substring(0, q) : path;
    var semi = base.indexOf(';');
    if (semi >= 0) {
      var before = base.substring(0, semi);
      var after = base.substring(semi + 1);
      var doIdx = after.indexOf('.do');
      if (doIdx >= 0 && before.slice(-3) !== '.do') {
        base = before + '.do';
      } else {
        base = before;
      }
    }
    return base + query;
  }

  /** 사이드바 메뉴 필터용 논리 경로 키 (암호화 전 href 보존) */
  function toMenuPathKey(path) {
    if (!path) {
      return '';
    }
    var p = stripQuery(path);
    if (p.indexOf('/e/') === 0) {
      return '';
    }
    if (p.length > 3 && p.slice(-3) === '.do') {
      p = p.slice(0, -3);
    }
    if (p === '/index') {
      return '/';
    }
    return p;
  }

  function ensureMenuPath(link, href) {
    if (!link || !href) {
      return;
    }
    var existing = link.getAttribute('data-menu-path');
    if (existing && existing.indexOf('/e/') !== 0) {
      return;
    }
    var key = toMenuPathKey(href);
    if (key) {
      link.setAttribute('data-menu-path', key);
    }
  }

  function hasNonDoFileExtension(path) {
    if (!path || path.slice(-3) === '.do') {
      return false;
    }
    var slash = path.lastIndexOf('/');
    var last = slash >= 0 ? path.substring(slash + 1) : path;
    var dot = last.indexOf('.');
    return dot > 0 && dot < last.length - 1;
  }

  function shouldSkip(path) {
    if (!path) {
      return true;
    }
    var p = stripQuery(path);
    var i;
    for (i = 0; i < EXCLUDED_PREFIXES.length; i += 1) {
      if (p.indexOf(EXCLUDED_PREFIXES[i]) === 0) {
        return true;
      }
    }
    if (p.indexOf(ENCRYPT_PREFIX) === 0 && p.slice(-3) === '.do') {
      return true;
    }
    if (p.length > 3 && p.slice(-3) === '.do') {
      return false;
    }
    return hasNonDoFileExtension(p);
  }

  function isEncrypted(path) {
    var p = stripQuery(path);
    return p.indexOf(ENCRYPT_PREFIX) === 0 && p.slice(-3) === '.do';
  }

  function fetchPublic(path) {
    var key = stripQuery(path);
    if (cache[key]) {
      return Promise.resolve(cache[key]);
    }
    if (pending[key]) {
      return pending[key];
    }
    pending[key] = fetch('/api/url/public?path=' + encodeURIComponent(key), { credentials: 'same-origin' })
      .then(function (res) {
        if (!res.ok) {
          throw new Error('HTTP ' + res.status);
        }
        return res.json();
      })
      .then(function (body) {
        var pub = body && body.path ? body.path : key;
        cache[key] = pub;
        delete pending[key];
        return pub;
      })
      .catch(function () {
        delete pending[key];
        return key;
      });
    return pending[key];
  }

  function toPublic(path) {
    if (!path) {
      return fetchPublic('/');
    }
    var full = path;
    var query = '';
    var qIdx = path.indexOf('?');
    if (qIdx >= 0) {
      full = path.substring(0, qIdx);
      query = path.substring(qIdx);
    }
    var p = full.trim();
    if (!p.length) {
      return fetchPublic('/').then(function (enc) {
        return enc + query;
      });
    }
    if (shouldSkip(p) || isEncrypted(p)) {
      return Promise.resolve(path);
    }
    return fetchPublic(p).then(function (enc) {
      return enc + query;
    });
  }

  function normalizePath(path) {
    if (!path) {
      return Promise.resolve(null);
    }
    return toPublic(path);
  }

  function patchFetch() {
    if (!global.fetch || global.__PRINTMALL_FETCH_ENCRYPT_PATCHED__) {
      return;
    }
    var original = global.fetch.bind(global);
    global.fetch = function (input, init) {
      if (typeof input === 'string' && input.charAt(0) === '/' && !shouldSkip(input)) {
        return toPublic(input).then(function (url) {
          return original(url, init);
        });
      }
      if (input && typeof input === 'object' && typeof input.url === 'string'
          && input.url.charAt(0) === '/' && !shouldSkip(input.url)) {
        return toPublic(input.url).then(function (url) {
          return original(new Request(url, input), init);
        });
      }
      return original(input, init);
    };
    global.__PRINTMALL_FETCH_ENCRYPT_PATCHED__ = true;
  }

  function rewriteLinksIn(root) {
    var scope = root && root.querySelectorAll ? root : document;
    var links = scope.querySelectorAll('a[href^="/"]');
    var tasks = [];
    links.forEach(function (link) {
      var href = link.getAttribute('href');
      if (!href || shouldSkip(href)) {
        return;
      }
      var logical = link.getAttribute('data-menu-path') || href;
      ensureMenuPath(link, logical);
      if (isEncrypted(href)) {
        return;
      }
      tasks.push(
        toPublic(href).then(function (enc) {
          if (enc && enc !== href) {
            link.setAttribute('href', enc);
          }
        })
      );
    });
    return Promise.all(tasks);
  }

  function rewritePageLinks() {
    return rewriteLinksIn(document);
  }

  function navigateLink(link) {
    if (!link) {
      return Promise.resolve();
    }
    var href = link.getAttribute('href') || '';
    var menuPath = link.getAttribute('data-menu-path');
    var logical = menuPath && menuPath.indexOf('/e/') !== 0 ? menuPath : href;
    if (!logical && !href) {
      return Promise.resolve();
    }
    if (href && shouldSkip(href) && (!logical || shouldSkip(logical))) {
      return Promise.resolve();
    }
    if (href && isEncrypted(href)) {
      global.location.assign(href);
      return Promise.resolve();
    }
    ensureMenuPath(link, logical || href);
    return toPublic(logical || href).then(function (enc) {
      if (enc) {
        link.setAttribute('href', enc);
        global.location.assign(enc);
      }
    });
  }

  function isDashboardQuickLink(link) {
    if (!link || link.tagName !== 'A') {
      return false;
    }
    var host = document.getElementById('dashboard-widgets-host');
    if (!host || !host.contains(link)) {
      return false;
    }
    if (link.classList.contains('dashboard-quick-link')) {
      return true;
    }
    return !!link.closest('[data-dashboard-widget="admin-quick-links"], [data-dashboard-widget="ecm-quick-links"]');
  }

  function onDashboardQuickLinkClick(e) {
    var link = e.target.closest('a');
    if (!isDashboardQuickLink(link)) {
      return;
    }
    var href = link.getAttribute('href') || '';
    var menuPath = link.getAttribute('data-menu-path') || '';
    if ((!href || href === '#' || href.indexOf('#!') === 0) && !menuPath) {
      return;
    }
    if (href && shouldSkip(href) && (!menuPath || shouldSkip(menuPath))) {
      return;
    }
    e.preventDefault();
    navigateLink(link);
  }

  global.PrintMallPath = {
    shouldSkip: shouldSkip,
    isEncrypted: isEncrypted,
    toPublic: toPublic,
    normalizePath: normalizePath,
    rewritePageLinks: rewritePageLinks,
    rewriteLinksIn: rewriteLinksIn,
    navigateLink: navigateLink
  };
  global.PrintMallDoPath = global.PrintMallPath;

  patchFetch();

  function notifyPathsReady() {
    global.dispatchEvent(new Event('printmall-paths-ready'));
  }

  function onDashboardWidgetsRendered(e) {
    var host = (e && e.detail && e.detail.host) || document.getElementById('dashboard-widgets-host');
    if (!host) {
      return;
    }
    rewriteLinksIn(host);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
      rewritePageLinks().then(notifyPathsReady);
    });
  } else {
    rewritePageLinks().then(notifyPathsReady);
  }

  document.addEventListener('dashboard-widgets-rendered', onDashboardWidgetsRendered);
  document.addEventListener('click', onDashboardQuickLinkClick, true);
})(typeof window !== 'undefined' ? window : globalThis);
