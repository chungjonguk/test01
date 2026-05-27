/**
 * 암호화 URL(/e/{token}.do) 등 비루트 문서 경로에서
 * 상대 assets·uploads 경로가 깨지지 않도록 절대 URL로 보정합니다.
 */
(function (global) {
  'use strict';

  var SKIP_RE = /^(?:[a-z][a-z0-9+.-]*:|\/\/|\/|data:|blob:|#)/i;
  var ATTRS = ['src', 'href', 'poster'];
  var STYLE_URL_RE = /url\(\s*(['"]?)(?!https?:|\/\/|\/|data:|blob:)([^'")]+)\1\s*\)/gi;

  function appBase() {
    var baseEl = document.querySelector('base[href]');
    if (baseEl) {
      return baseEl.href;
    }
    return global.location.origin + (global.location.pathname || '/').replace(/[^/]*$/, '');
  }

  function toAbsolute(path) {
    if (path == null) {
      return '';
    }
    var p = String(path).trim();
    if (!p || SKIP_RE.test(p)) {
      return p;
    }
    try {
      var u = new URL(p, appBase());
      return u.pathname + (u.search || '');
    } catch (e) {
      return p.charAt(0) === '/' ? p : '/' + p;
    }
  }

  function toAbsoluteUrl(path) {
    if (path == null) {
      return '';
    }
    var p = String(path).trim();
    if (!p || SKIP_RE.test(p)) {
      return p;
    }
    try {
      return new URL(p, appBase()).href;
    } catch (e) {
      return p;
    }
  }

  function fixAttr(el, attr) {
    var val = el.getAttribute(attr);
    if (!val || SKIP_RE.test(val.trim())) {
      return;
    }
    var abs = toAbsoluteUrl(val);
    if (abs && abs !== val) {
      el.setAttribute(attr, abs);
    }
  }

  function fixInlineStyle(el) {
    var style = el.getAttribute('style');
    if (!style || style.indexOf('url(') < 0) {
      return;
    }
    var changed = false;
    var fixed = style.replace(STYLE_URL_RE, function (match, quote, path) {
      var abs = toAbsoluteUrl(path.trim());
      if (abs && abs !== path.trim()) {
        changed = true;
        return 'url(' + (quote || '') + abs + (quote || '') + ')';
      }
      return match;
    });
    if (changed) {
      el.setAttribute('style', fixed);
    }
  }

  function fixElement(el) {
    if (!el || el.nodeType !== 1) {
      return;
    }
    var tag = el.tagName;
    var i;
    if (tag === 'IMG' || tag === 'SOURCE' || tag === 'VIDEO' || tag === 'IMAGE') {
      for (i = 0; i < ATTRS.length; i += 1) {
        if (el.hasAttribute(ATTRS[i])) {
          fixAttr(el, ATTRS[i]);
        }
      }
    }
    if (el.hasAttribute('style')) {
      fixInlineStyle(el);
    }
    if (el.classList && el.classList.contains('bg-holder')) {
      fixInlineStyle(el);
    }
  }

  function fixTree(root) {
    if (!root) {
      return;
    }
    if (root.nodeType === 1) {
      fixElement(root);
    }
    var nodes = root.querySelectorAll
      ? root.querySelectorAll('img[src], source[src], video[src], video[poster], [style*="url("], .bg-holder[style]')
      : [];
    var j;
    for (j = 0; j < nodes.length; j += 1) {
      fixElement(nodes[j]);
    }
  }

  function observe() {
    if (!global.MutationObserver || !document.body) {
      return;
    }
    var obs = new MutationObserver(function (records) {
      records.forEach(function (rec) {
        rec.addedNodes.forEach(function (node) {
          if (node.nodeType === 1) {
            fixTree(node);
          }
        });
      });
    });
    obs.observe(document.body, { childList: true, subtree: true });
  }

  global.PrintMallAssetUrl = {
    toAbsolute: toAbsolute,
    toAbsoluteUrl: toAbsoluteUrl,
    fixTree: fixTree
  };

  function run() {
    fixTree(document.documentElement);
    observe();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', run);
  } else {
    run();
  }
})(typeof window !== 'undefined' ? window : globalThis);
