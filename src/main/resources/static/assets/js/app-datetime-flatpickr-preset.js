/**
 * PrintMall — flatpickr 기본 날짜 형식
 * 날짜: yyyy-MM-dd (Y-m-d)
 * 일시: yyyy-MM-dd HH:mm:ss (Y-m-d H:i:S, 24시간)
 */
(function () {
  'use strict';

  var FP_DATE = 'Y-m-d';
  var FP_DATETIME = 'Y-m-d H:i:S';
  var PH_DATE = 'yyyy-MM-dd';
  var PH_DATETIME = 'yyyy-MM-dd HH:mm:ss';
  var LEGACY_DATE_FORMATS = { 'd/m/y': true, 'm/d/Y': true, 'd/m/Y': true };

  function parseOptions(el) {
    var raw = el.getAttribute('data-options');
    if (!raw) {
      return {};
    }
    try {
      return JSON.parse(raw);
    } catch (e) {
      return {};
    }
  }

  function normalizeDateFormat(fmt) {
    if (!fmt || LEGACY_DATE_FORMATS[fmt]) {
      return FP_DATE;
    }
    if (fmt.indexOf('d/m/y') !== -1) {
      return fmt.replace(/d\/m\/y/g, 'Y-m-d').replace(/H:i(?!:)/g, 'H:i:S');
    }
    return fmt;
  }

  function patchDatetimePickers() {
    document.querySelectorAll('.datetimepicker').forEach(function (el) {
      var opts = parseOptions(el);
      if (opts.noCalendar) {
        if (!opts.dateFormat || opts.dateFormat === 'H:i') {
          opts.dateFormat = 'H:i:S';
        }
        if (!el.getAttribute('placeholder')) {
          el.setAttribute('placeholder', 'HH:mm:ss');
        }
      } else if (opts.enableTime) {
        opts.dateFormat = normalizeDateFormat(opts.dateFormat);
        if (opts.dateFormat === FP_DATE || LEGACY_DATE_FORMATS[opts.dateFormat]) {
          opts.dateFormat = FP_DATETIME;
        }
        if (!el.getAttribute('placeholder') || el.getAttribute('placeholder').indexOf('d/m') !== -1) {
          el.setAttribute('placeholder', PH_DATETIME);
        }
      } else {
        opts.dateFormat = normalizeDateFormat(opts.dateFormat);
        if (!el.getAttribute('placeholder') || el.getAttribute('placeholder').indexOf('d/m') !== -1) {
          el.setAttribute('placeholder', PH_DATE);
        }
        if (opts.mode === 'range' && (!el.getAttribute('placeholder') || el.getAttribute('placeholder').indexOf('d/m') !== -1)) {
          el.setAttribute('placeholder', PH_DATE + ' to ' + PH_DATE);
        }
      }
      el.setAttribute('data-options', JSON.stringify(opts));
    });
  }

  function run() {
    patchDatetimePickers();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', run);
  } else {
    run();
  }

  window.PrintMallDateTime = {
    datePattern: PH_DATE,
    dateTimePattern: PH_DATETIME,
    flatpickrDate: FP_DATE,
    flatpickrDateTime: FP_DATETIME,
    patchDatetimePickers: patchDatetimePickers
  };
})();
