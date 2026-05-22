/**
 * 달러($) 표기를 원(원)으로 변환 — 데모/템플릿 화면 공통
 */
(function () {
  'use strict';

  var DOLLAR_RE = /(-?)\$([\d,]+(?:\.\d+)?)(K)?/gi;

  function formatWonNumber(num) {
    if (num == null || isNaN(num)) {
      return '0원';
    }
    return Math.round(Number(num)).toLocaleString('ko-KR') + '원';
  }

  function formatWonFromParts(sign, amount, hasK) {
    var raw = parseFloat(String(amount).replace(/,/g, ''));
    if (isNaN(raw)) {
      return null;
    }
    if (hasK) {
      raw *= 1000;
    }
    var formatted = formatWonNumber(raw);
    return (sign === '-' ? '-' : '') + formatted;
  }

  function convertDollarString(str) {
    if (!str || str.indexOf('$') === -1) {
      return str;
    }
    return str.replace(DOLLAR_RE, function (match, sign, amount, k) {
      var won = formatWonFromParts(sign, amount, !!k);
      return won != null ? won : match;
    });
  }

  function convertUsdLabel(str) {
    if (!str || !/USD/i.test(str)) {
      return str;
    }
    return str.replace(/\s*USD\b/gi, ' 원').replace(/USD/gi, '원');
  }

  function shouldSkipNode(node) {
    var parent = node.parentElement;
    if (!parent) {
      return true;
    }
    if (parent.closest('script, style, code, pre, textarea, svg, i, .fa, .fas, .fab, .far, .fal, .fad, [class*="fa-"]')) {
      return true;
    }
    if (parent.closest('[data-currency-keep]')) {
      return true;
    }
    if (parent.id === 'checkout-pay-amount') {
      return true;
    }
    return false;
  }

  function convertAttributes(root) {
    root.querySelectorAll('[data-plan-price]').forEach(function (el) {
      var v = el.getAttribute('data-plan-price');
      if (v && v.indexOf('$') !== -1) {
        el.setAttribute('data-plan-price', convertDollarString(v));
        var priceSpan = el.querySelector('span:last-child');
        if (priceSpan) {
          priceSpan.textContent = convertDollarString(priceSpan.textContent);
        }
      }
    });
  }

  function convertTextNodes(root) {
    var walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
    var node;
    while ((node = walker.nextNode())) {
      if (shouldSkipNode(node)) {
        continue;
      }
      var text = node.textContent;
      if (!text) {
        continue;
      }
      var next = convertUsdLabel(convertDollarString(text));
      if (next !== text) {
        node.textContent = next;
      }
    }
  }

  function convertPage(root) {
    root = root || document.body;
    if (!root) {
      return;
    }
    convertTextNodes(root);
    convertAttributes(root);
  }

  window.PrintMallCurrency = {
    formatWon: formatWonNumber,
    convertDollarString: convertDollarString,
    convertPage: convertPage
  };

  document.addEventListener('DOMContentLoaded', function () {
    convertPage(document.body);
  });
})();
