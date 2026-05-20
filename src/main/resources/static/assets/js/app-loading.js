/**
 * 전역 로딩바 — 페이지 이동, fetch, 수동 호출(AppLoading.show/hide)
 */
(function (global) {
  var BAR_ID = 'app-global-loading-bar';
  var count = 0;
  var modalOpen = false;

  function getBar() {
    return document.getElementById(BAR_ID);
  }

  function setBarVisible(visible) {
    var bar = getBar();
    if (!bar) {
      return;
    }
    bar.classList.toggle('d-none', !visible);
    bar.setAttribute('aria-hidden', visible ? 'false' : 'true');
    document.documentElement.classList.toggle('app-is-loading', visible);
  }

  function openModal(message) {
    if (modalOpen || typeof Swal === 'undefined') {
      return;
    }
    modalOpen = true;
    Swal.fire({
      title: '처리 중',
      html: message || '잠시만 기다려 주세요.',
      allowOutsideClick: false,
      allowEscapeKey: false,
      showConfirmButton: false,
      didOpen: function () {
        Swal.showLoading();
      }
    });
  }

  function closeModal() {
    if (!modalOpen || typeof Swal === 'undefined') {
      modalOpen = false;
      return;
    }
    Swal.close();
    modalOpen = false;
  }

  function setButtonBusy(btn, busy, busyHtml) {
    if (!btn) {
      return;
    }
    if (busy) {
      if (!btn.dataset.appLoadingOriginalHtml) {
        btn.dataset.appLoadingOriginalHtml = btn.innerHTML;
      }
      btn.disabled = true;
      btn.innerHTML =
        busyHtml ||
        '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>처리 중...';
      return;
    }
    if (btn.dataset.appLoadingOriginalHtml) {
      btn.innerHTML = btn.dataset.appLoadingOriginalHtml;
      delete btn.dataset.appLoadingOriginalHtml;
    }
    btn.disabled = false;
  }

  var AppLoading = {
    show: function (options) {
      options = options || {};
      count += 1;
      setBarVisible(true);
      if (options.modal) {
        openModal(options.message);
      }
      if (options.button) {
        setButtonBusy(options.button, true, options.buttonHtml);
      }
    },

    hide: function (force) {
      if (force) {
        count = 0;
      } else if (count > 0) {
        count -= 1;
      }
      if (count === 0) {
        setBarVisible(false);
        closeModal();
        document.querySelectorAll('[data-app-loading-original-html]').forEach(function (btn) {
          setButtonBusy(btn, false);
        });
      }
    },

    wrapPromise: function (promise, options) {
      AppLoading.show(options);
      return Promise.resolve(promise).finally(function () {
        AppLoading.hide();
      });
    },

    alert: function (icon, title, text) {
      if (typeof Swal === 'undefined') {
        window.alert(text || title);
        return;
      }
      Swal.fire({
        icon: icon,
        title: title,
        text: text,
        confirmButtonText: '확인',
        confirmButtonColor: '#2c7be5',
        timer: icon === 'success' ? 2500 : undefined,
        timerProgressBar: icon === 'success'
      });
    }
  };

  function shouldHandleLink(anchor) {
    if (!anchor || !anchor.getAttribute) {
      return false;
    }
    if (anchor.hasAttribute('data-no-loading')) {
      return false;
    }
    if (anchor.target === '_blank' || anchor.hasAttribute('download')) {
      return false;
    }
    var href = anchor.getAttribute('href');
    if (!href || href.charAt(0) === '#' || href.indexOf('javascript:') === 0) {
      return false;
    }
    try {
      var url = new URL(href, global.location.origin);
      if (url.origin !== global.location.origin) {
        return false;
      }
      var path = url.pathname.replace(/\/+$/, '') || '/';
      var current = global.location.pathname.replace(/\/+$/, '') || '/';
      return path !== current || url.search !== global.location.search;
    } catch (e) {
      return false;
    }
  }

  function bindNavigationLoading() {
    document.addEventListener(
      'click',
      function (e) {
        var anchor = e.target.closest('a[href]');
        if (!shouldHandleLink(anchor)) {
          return;
        }
        AppLoading.show();
      },
      true
    );

    document.addEventListener('submit', function (e) {
      var form = e.target;
      if (!form || form.tagName !== 'FORM' || form.hasAttribute('data-no-loading')) {
        return;
      }
      var method = (form.getAttribute('method') || 'get').toLowerCase();
      if (method === 'get' && !form.hasAttribute('data-loading-modal')) {
        AppLoading.show();
      } else if (method === 'post') {
        AppLoading.show({ modal: form.hasAttribute('data-loading-modal') });
      }
    });

    global.addEventListener('pageshow', function (ev) {
      AppLoading.hide(true);
    });

    global.addEventListener('beforeunload', function () {
      AppLoading.show();
    });
  }

  function patchFetch() {
    if (!global.fetch || global.fetch.__appLoadingPatched) {
      return;
    }
    var nativeFetch = global.fetch;
    global.fetch = function () {
      var args = arguments;
      var opts = args[1] || {};
      if (opts.headers && opts.headers['X-No-Loading']) {
        return nativeFetch.apply(global, args);
      }
      AppLoading.show();
      return nativeFetch.apply(global, args).finally(function () {
        AppLoading.hide();
      });
    };
    global.fetch.__appLoadingPatched = true;
  }

  function initInitialPageLoad() {
    if (document.readyState === 'loading') {
      AppLoading.show();
      document.addEventListener('DOMContentLoaded', function () {
        AppLoading.hide();
      });
      global.addEventListener('load', function () {
        AppLoading.hide(true);
      });
    }
  }

  global.AppLoading = AppLoading;

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () {
      bindNavigationLoading();
      patchFetch();
    });
  } else {
    bindNavigationLoading();
    patchFetch();
  }

  initInitialPageLoad();
})(window);
