/**
 * 헤더·드롭다운 사용자 아바타 — 프로필 사진 URL 동기화
 */
(function (global) {
  'use strict';

  function cacheBust(url) {
    if (!url) {
      return '';
    }
    return url + (url.indexOf('?') >= 0 ? '&' : '?') + 't=' + Date.now();
  }

  function apply(profileUrl, initial) {
    var url = profileUrl && String(profileUrl).trim();
    var nodes = global.document.querySelectorAll('[data-user-nav-avatar]');
    nodes.forEach(function (el) {
      var img = el.querySelector('.user-nav-avatar-photo');
      var span = el.querySelector('.user-nav-avatar-initial');
      if (url) {
        if (!img) {
          img = global.document.createElement('img');
          img.className = 'user-nav-avatar-photo';
          img.alt = '';
          el.appendChild(img);
        }
        img.src = cacheBust(url);
        img.classList.remove('d-none');
        if (span) {
          span.classList.add('d-none');
        }
      } else {
        if (img) {
          img.classList.add('d-none');
          img.removeAttribute('src');
        }
        if (span) {
          span.classList.remove('d-none');
          if (initial) {
            span.textContent = initial;
          }
        }
      }
    });
    global.__USER_PROFILE_IMAGE_URL__ = url || '';
  }

  function initFromPage() {
    var url = global.__USER_PROFILE_IMAGE_URL__;
    if (url) {
      apply(url);
    }
  }

  global.PrintMallUserNavAvatar = {
    apply: apply,
    init: initFromPage
  };

  if (global.document.readyState === 'loading') {
    global.document.addEventListener('DOMContentLoaded', initFromPage);
  } else {
    initFromPage();
  }
})(window);
