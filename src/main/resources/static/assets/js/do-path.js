/**
 * PrintMall — .do URL 규칙 (클라이언트)
 * - same-origin fetch('/api/...') 경로에 .do 자동 부여
 * - screen-menu-filter 와 경로 정규화 공유 (PrintMallDoPath)
 * - 제외: /auth/, /assets/, /vendors/, 확장자 있는 정적 리소스
 */
(function (global) {
  'use strict';

  var EXCLUDED_PREFIXES = ['/auth/', '/assets/', '/vendors/', '/error'];

  function stripQuery(path) {
    var q = path.indexOf('?');
    return q >= 0 ? path.substring(0, q) : path;
  }

  function hasFileExtension(path) {
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
    if (p.slice(-3) === '.do') {
      return true;
    }
    return hasFileExtension(p);
  }

  function toDo(path) {
    if (!path) {
      return '/index.do';
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
      return '/index.do' + query;
    }
    if (shouldSkip(p)) {
      return path;
    }
    if (p.slice(-3) === '.do') {
      return path;
    }
    if (p === '/') {
      return '/index.do' + query;
    }
    if (p.length > 1 && p.charAt(p.length - 1) === '/') {
      p = p.slice(0, -1);
    }
    return p + '.do' + query;
  }

  function normalizePath(path) {
    if (!path) {
      return '/index.do';
    }
    var trimmed = stripQuery(path).replace(/\/+$/, '').replace(/\.html$/, '');
    if (!trimmed.length) {
      return '/index.do';
    }
    return toDo(trimmed);
  }

  function patchFetch() {
    if (!global.fetch || global.__PRINTMALL_FETCH_DO_PATCHED__) {
      return;
    }
    var original = global.fetch.bind(global);
    global.fetch = function (input, init) {
      if (typeof input === 'string' && input.charAt(0) === '/') {
        input = toDo(input);
      } else if (input && typeof input === 'object' && typeof input.url === 'string'
          && input.url.charAt(0) === '/') {
        input = new Request(toDo(input.url), input);
      }
      return original(input, init);
    };
    global.__PRINTMALL_FETCH_DO_PATCHED__ = true;
  }

  global.PrintMallDoPath = {
    shouldSkip: shouldSkip,
    toDo: toDo,
    normalizePath: normalizePath
  };

  patchFetch();
})(typeof window !== 'undefined' ? window : globalThis);
