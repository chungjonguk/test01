/**
 * 한국 전화번호 입력 포맷·검증 (공통)
 */
(function (global) {
  'use strict';

  function digitsOnly(value) {
    return String(value == null ? '' : value).replace(/\D/g, '');
  }

  function format(value) {
    var d = digitsOnly(value);
    if (!d) {
      return '';
    }
    if (d.indexOf('02') === 0) {
      if (d.length <= 2) {
        return d;
      }
      if (d.length <= 5) {
        return d.slice(0, 2) + '-' + d.slice(2);
      }
      if (d.length <= 9) {
        return d.slice(0, 2) + '-' + d.slice(2, 5) + '-' + d.slice(5);
      }
      if (d.length === 10) {
        return d.slice(0, 2) + '-' + d.slice(2, 6) + '-' + d.slice(6);
      }
      return d.slice(0, 2) + '-' + d.slice(2, 6) + '-' + d.slice(6, 10);
    }
    if (d.indexOf('01') === 0) {
      if (d.length <= 3) {
        return d;
      }
      if (d.length <= 7) {
        return d.slice(0, 3) + '-' + d.slice(3);
      }
      if (d.length <= 10) {
        return d.slice(0, 3) + '-' + d.slice(3, 6) + '-' + d.slice(6);
      }
      return d.slice(0, 3) + '-' + d.slice(3, 7) + '-' + d.slice(7, 11);
    }
    if (d.charAt(0) === '0') {
      if (d.length <= 3) {
        return d;
      }
      if (d.length <= 6) {
        return d.slice(0, 3) + '-' + d.slice(3);
      }
      if (d.length <= 10) {
        return d.slice(0, 3) + '-' + d.slice(3, 6) + '-' + d.slice(6);
      }
      return d.slice(0, 3) + '-' + d.slice(3, 7) + '-' + d.slice(7, 11);
    }
    return d;
  }

  function isValid(value) {
    var d = digitsOnly(value);
    if (!d) {
      return true;
    }
    if (d.indexOf('02') === 0) {
      return d.length === 9 || d.length === 10;
    }
    if (d.indexOf('01') === 0) {
      return d.length === 10 || d.length === 11;
    }
    if (d.charAt(0) === '0') {
      return d.length === 10 || d.length === 11;
    }
    return false;
  }

  function bindInput(el, options) {
    if (!el) {
      return;
    }
    var label = (options && options.label) || '전화번호';
    function onInput() {
      var pos = el.selectionStart;
      var before = el.value;
      el.value = format(el.value);
      if (pos != null && before !== el.value) {
        el.selectionStart = el.selectionEnd = el.value.length;
      }
    }
    el.addEventListener('input', onInput);
    el.addEventListener('blur', function () {
      var v = el.value.trim();
      if (!v) {
        el.classList.remove('is-invalid');
        return;
      }
      el.value = format(v);
      if (!isValid(el.value)) {
        el.classList.add('is-invalid');
        el.setAttribute('title', label + ' 형식이 올바르지 않습니다. 예: 010-1234-5678');
      } else {
        el.classList.remove('is-invalid');
        el.removeAttribute('title');
      }
    });
    if (el.value) {
      el.value = format(el.value);
    }
  }

  global.PrintMallPhoneFormat = {
    digitsOnly: digitsOnly,
    format: format,
    isValid: isValid,
    bindInput: bindInput
  };
})(window);
