/**
 * 장바구니 — 상단 아이콘 ↔ 사이드바 메뉴 연동 (경로·수량·현재 페이지)
 */
(function (global) {
  'use strict';

  var CART_PATH_KEY = '/app/e-commerce/shopping-cart';
  var STORAGE_KEY = 'printmall.cart.itemCount';
  var ITEMS_KEY = 'printmall.cart.items';

  function pathKey(path) {
    if (!path) {
      return '';
    }
    var p = String(path).trim();
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

  function countFromDom() {
    var card = document.getElementById('shoppingCartCard');
    if (!card) {
      return null;
    }
    var removes = card.querySelectorAll('.cart-remove-link');
    if (removes.length) {
      return removes.length;
    }
    var rows = card.querySelectorAll('.cart-item-row');
    if (rows.length) {
      return rows.length;
    }
    return card.querySelectorAll('.row.gx-card.mx-0.align-items-center.border-bottom').length;
  }

  function loadItems() {
    try {
      var raw = global.localStorage.getItem(ITEMS_KEY);
      if (!raw) {
        return [];
      }
      var parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
      return [];
    }
  }

  function parsePriceNumber(price) {
    if (price == null || price === '') {
      return 0;
    }
    if (typeof price === 'number' && !isNaN(price)) {
      return price >= 0 ? price : 0;
    }
    var s = String(price).trim();
    var hasK = /\d\s*K$/i.test(s) || /K\b/i.test(s);
    var num = parseFloat(s.replace(/[^0-9.]/g, ''));
    if (isNaN(num)) {
      return 0;
    }
    if (hasK) {
      num *= 1000;
    }
    return num;
  }

  function enrichItem(item) {
    if (!item) {
      return item;
    }
    var unit = item.unitPrice;
    if (typeof unit !== 'number' || isNaN(unit) || unit <= 0) {
      item.unitPrice = parsePriceNumber(item.price);
    }
    item.qty = normalizeQty(item.qty);
    return item;
  }

  function normalizeQty(qty) {
    var n = parseInt(qty, 10);
    if (isNaN(n) || n < 1) {
      return 1;
    }
    return n > 999 ? 999 : n;
  }

  function lineCount(items) {
    return Array.isArray(items) ? items.length : 0;
  }

  function totalQty(items) {
    var list = Array.isArray(items) ? items : [];
    return list.reduce(function (sum, item) {
      return sum + normalizeQty(item && item.qty);
    }, 0);
  }

  function saveItems(items) {
    var list = (Array.isArray(items) ? items : []).map(enrichItem);
    var count = lineCount(list);
    global.localStorage.setItem(ITEMS_KEY, JSON.stringify(list));
    global.localStorage.setItem(STORAGE_KEY, String(count));
    refreshBadges(count);
    global.document.dispatchEvent(
      new CustomEvent('printmall-cart-updated', { detail: { count: count, items: list } })
    );
    return list;
  }

  function getCount() {
    var items = loadItems();
    if (items.length > 0) {
      return lineCount(items);
    }
    var stored = global.localStorage.getItem(STORAGE_KEY);
    if (stored === null || stored === '') {
      return 0;
    }
    var n = parseInt(stored, 10);
    return isNaN(n) ? 0 : Math.max(0, n);
  }

  function setCount(count) {
    var n = Math.max(0, parseInt(count, 10) || 0);
    var items = loadItems();
    if (items.length > n) {
      saveItems(items.slice(0, n));
      return n;
    }
    global.localStorage.setItem(STORAGE_KEY, String(n));
    refreshBadges(n);
    global.document.dispatchEvent(new CustomEvent('printmall-cart-updated', { detail: { count: n } }));
    return n;
  }

  function removeItemAt(index) {
    var items = loadItems();
    var idx = parseInt(index, 10);
    if (isNaN(idx) || idx < 0 || idx >= items.length) {
      return items;
    }
    items.splice(idx, 1);
    return saveItems(items);
  }

  function removeItemsAt(indices) {
    var items = loadItems();
    var sorted = (Array.isArray(indices) ? indices : [])
      .map(function (i) {
        return parseInt(i, 10);
      })
      .filter(function (i) {
        return !isNaN(i) && i >= 0 && i < items.length;
      })
      .sort(function (a, b) {
        return b - a;
      });
    sorted.forEach(function (idx) {
      items.splice(idx, 1);
    });
    return saveItems(items);
  }

  function updateItemAt(index, patch) {
    var items = loadItems();
    var idx = parseInt(index, 10);
    if (isNaN(idx) || idx < 0 || idx >= items.length) {
      return null;
    }
    var item = items[idx];
    if (patch && typeof patch.qty !== 'undefined') {
      item.qty = normalizeQty(patch.qty);
    }
    if (patch && patch.title) {
      item.title = patch.title;
    }
    if (patch && patch.price) {
      item.price = patch.price;
    }
    saveItems(items);
    return item;
  }

  function clearItems() {
    return saveItems([]);
  }

  function addItem(item) {
    var payload = item || {};
    var title = (payload.title || '').trim();
    if (!title) {
      title = '상품';
    }
    var addQty = normalizeQty(payload.qty || 1);
    var id = payload.id || 'item-' + Date.now() + '-' + Math.floor(Math.random() * 1000);
    var items = loadItems();
    if (payload.id) {
      for (var i = 0; i < items.length; i++) {
        if (items[i].id === payload.id) {
          items[i].qty = normalizeQty(items[i].qty) + addQty;
          saveItems(items);
          return items[i];
        }
      }
    }
    var entry = {
      id: id,
      title: title,
      price: payload.price || '',
      unitPrice: parsePriceNumber(payload.unitPrice || payload.price),
      imageUrl: payload.imageUrl || '',
      qty: addQty,
      addedAt: Date.now()
    };
    items.push(entry);
    saveItems(items);
    return entry;
  }

  function formatBadge(n) {
    return n > 99 ? '99+' : String(n);
  }

  function refreshBadges(count) {
    var n = typeof count === 'number' ? count : getCount();
    global.document.querySelectorAll('[data-cart-badge]').forEach(function (badge) {
      var wrap = badge.closest('.notification-indicator') || badge.closest('a.nav-link');
      if (n > 0) {
        badge.textContent = formatBadge(n);
        badge.classList.remove('d-none');
        if (badge.classList.contains('notification-indicator-number')) {
          badge.style.display = '';
        }
        if (wrap && wrap.classList.contains('notification-indicator')) {
          wrap.classList.add('notification-indicator-fill');
        }
      } else {
        badge.textContent = '';
        badge.classList.add('d-none');
        if (badge.classList.contains('notification-indicator-number')) {
          badge.style.display = 'none';
        }
        if (wrap && wrap.classList.contains('notification-indicator')) {
          wrap.classList.remove('notification-indicator-fill');
        }
      }
    });
  }

  function syncHeaderActive() {
    var link = global.document.getElementById('header-cart-link');
    if (!link) {
      return;
    }
    var ctx = global.__PAGE_MENU_CONTEXT__;
    var onCart = ctx && pathKey(ctx.pathKey) === CART_PATH_KEY;
    link.classList.toggle('active', !!onCart);
    if (onCart) {
      link.setAttribute('aria-current', 'page');
      link.setAttribute('title', '장바구니 (현재 화면)');
    } else {
      link.removeAttribute('aria-current');
      link.setAttribute('title', '장바구니');
    }
  }

  function init() {
    var domCount = countFromDom();
    if (domCount !== null) {
      if (loadItems().length === 0) {
        setCount(domCount);
      } else {
        refreshBadges(getCount());
      }
    } else {
      refreshBadges(getCount());
    }
    syncHeaderActive();
  }

  global.PrintMallCartNav = {
    CART_PATH_KEY: CART_PATH_KEY,
    getCount: getCount,
    setCount: setCount,
    loadItems: loadItems,
    saveItems: saveItems,
    addItem: addItem,
    removeItemAt: removeItemAt,
    removeItemsAt: removeItemsAt,
    updateItemAt: updateItemAt,
    normalizeQty: normalizeQty,
    parsePriceNumber: parsePriceNumber,
    getTotalQty: function () {
      return totalQty(loadItems());
    },
    clearItems: clearItems,
    refresh: function () {
      refreshBadges(getCount());
      syncHeaderActive();
    },
    pathKey: pathKey
  };

  global.addEventListener('printmall-cart-updated', function (e) {
    if (e && e.detail && typeof e.detail.count === 'number') {
      setCount(e.detail.count);
    } else {
      refreshBadges(getCount());
    }
    syncHeaderActive();
  });

  if (global.document.readyState === 'loading') {
    global.document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  global.document.addEventListener('printmall-paths-ready', function () {
    refreshBadges(getCount());
    syncHeaderActive();
  });
})(typeof window !== 'undefined' ? window : globalThis);
