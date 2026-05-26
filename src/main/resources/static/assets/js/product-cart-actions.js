/**
 * 상품 목록·그리드·관리 — 장바구니 담기 ↔ 상단/사이드바 장바구니 메뉴 연동
 */
(function () {
  'use strict';

  function notify(message, icon) {
    if (window.Swal) {
      window.Swal.fire({
        toast: true,
        position: 'top-end',
        icon: icon || 'success',
        title: message,
        showConfirmButton: false,
        timer: 2200,
        timerProgressBar: true
      });
      return;
    }
    window.alert(message);
  }

  function closestCol(row, selector) {
    if (!row) {
      return null;
    }
    if (row.closest) {
      return row.closest(selector);
    }
    return null;
  }

  function extractProduct(button) {
    var root =
      closestCol(button, '.product-manage-card') ||
      closestCol(button, '.p-card') ||
      closestCol(button, '.card') ||
      button.parentElement;
    if (!root) {
      return { title: '상품', price: '', imageUrl: '', id: '' };
    }
    var titleEl =
      root.querySelector('h5 a') ||
      root.querySelector('h5') ||
      root.querySelector('.fs-0 a') ||
      root.querySelector('a.text-dark');
    var priceEl = root.querySelector('h4.text-warning, h4.fs-1.text-warning, h5.text-warning, .text-warning.h4');
    var imgEl = root.querySelector('img[src]');
    var title = titleEl ? titleEl.textContent.trim() : '상품';
    var price = priceEl ? priceEl.textContent.trim() : '';
    var imageUrl = imgEl ? imgEl.getAttribute('src') : '';
    var id = '';
    var card = closestCol(button, '.product-manage-card');
    if (card && card.getAttribute('data-product-id')) {
      id = 'product-' + card.getAttribute('data-product-id');
    }
    if (!id && imgEl && imgEl.src) {
      var match = imgEl.src.match(/products\/([^./?]+)/);
      if (match) {
        id = 'product-' + match[1];
      }
    }
    if (!id && title) {
      id = 'product-' + title.slice(0, 40).replace(/\s+/g, '-').toLowerCase();
    }
    return { id: id, title: title, price: price, imageUrl: imageUrl };
  }

  function addToCart(button) {
    if (!window.PrintMallCartNav || typeof window.PrintMallCartNav.addItem !== 'function') {
      notify('장바구니 연동을 불러오지 못했습니다. 페이지를 새로고침해 주세요.', 'warning');
      return;
    }
    var product = extractProduct(button);
    window.PrintMallCartNav.addItem(product);
    var count = window.PrintMallCartNav.getCount();
    notify('장바구니에 담았습니다 (' + count + '건)', 'success');
  }

  function onDocumentClick(e) {
    var target = e.target;
    if (!target || !target.closest) {
      return;
    }
    var btn = target.closest('[data-add-to-cart]');
    if (!btn) {
      return;
    }
    e.preventDefault();
    e.stopPropagation();
    addToCart(btn);
  }

  function bindStatic() {
    document.querySelectorAll('[data-add-to-cart]').forEach(function (btn) {
      if (btn.getAttribute('data-cart-bound') === '1') {
        return;
      }
      btn.setAttribute('data-cart-bound', '1');
      btn.addEventListener('click', function (e) {
        e.preventDefault();
        addToCart(btn);
      });
    });
  }

  function init() {
    document.addEventListener('click', onDocumentClick, true);
    bindStatic();
  }

  window.PrintMallProductCart = {
    addToCart: addToCart,
    extractProduct: extractProduct
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
