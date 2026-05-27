/**
 * 쇼핑몰 홈(product-grid) — 업체 도메인 시 store-catalog, 그 외 관리용 목록 API
 */
(function () {
  'use strict';

  var shell = document.getElementById('store-product-grid-shell');
  if (!shell) {
    return;
  }

  var grid = document.getElementById('store-product-grid');
  var countEl = document.getElementById('store-product-count');
  var storefront = shell.dataset.storefront === 'true';
  var apiUrl = storefront
    ? '/api/ecommerce/products/store-catalog'
    : '/api/ecommerce/products?statusCd=ACTIVE';

  function escapeHtml(s) {
    if (s == null) {
      return '';
    }
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function formatPrice(price) {
    if (price == null || price === '') {
      return '—';
    }
    var n = Number(price);
    if (isNaN(n)) {
      return escapeHtml(price);
    }
    return '₩' + n.toLocaleString('ko-KR');
  }

  function detailPath(id) {
    return '/app/e-commerce/product/product-details?id=' + encodeURIComponent(id);
  }

  function resolvePublicPath(path, callback) {
    var pathApi = window.PrintMallPath || window.PrintMallDoPath;
    if (pathApi && typeof pathApi.toPublic === 'function') {
      pathApi.toPublic(path).then(function (url) {
        callback(url || path);
      });
      return;
    }
    callback(path);
  }

  function resolvePublicPathBatch(paths, done) {
    var pathApi = window.PrintMallPath || window.PrintMallDoPath;
    if (!pathApi || typeof pathApi.toPublic !== 'function') {
      done(paths);
      return;
    }
    var out = paths.slice();
    var i = 0;
    function next() {
      if (i >= out.length) {
        done(out);
        return;
      }
      pathApi.toPublic(out[i]).then(function (url) {
        out[i] = url || out[i];
        i += 1;
        next();
      });
    }
    next();
  }

  function renderCard(product, detailHref) {
    var img = product.imgUrl || (product.imageUrls && product.imageUrls[0]) || '/assets/img/products/1.jpg';
    var stock = product.stockQty != null ? product.stockQty : 0;
    var stockLabel = stock > 0 ? 'Available' : 'Out of stock';
    var stockClass = stock > 0 ? 'text-success' : 'text-danger';
    return (
      '<div class="mb-4 col-md-6 col-lg-4">' +
      '<div class="border rounded-1 h-100 d-flex flex-column justify-content-between pb-3">' +
      '<div class="overflow-hidden">' +
      '<div class="position-relative rounded-top overflow-hidden">' +
      '<a class="d-block" href="' +
      escapeHtml(detailHref) +
      '"><img class="img-fluid rounded-top" src="' +
      escapeHtml(img) +
      '" alt="" style="max-height:220px;object-fit:cover;width:100%" onerror="this.src=\'/assets/img/products/1.jpg\'"/></a>' +
      '</div>' +
      '<div class="p-3">' +
      '<h5 class="fs-0"><a class="text-dark" href="' +
      escapeHtml(detailHref) +
      '">' +
      escapeHtml(product.productNm) +
      '</a></h5>' +
      '<p class="fs--1 mb-3"><span class="text-500">' +
      escapeHtml(product.categoryCd || '') +
      '</span></p>' +
      '<h5 class="fs-md-2 text-warning mb-0 mb-3">' +
      formatPrice(product.price) +
      '</h5>' +
      '<p class="fs--1 mb-1">Stock: <strong class="' +
      stockClass +
      '">' +
      escapeHtml(stockLabel) +
      '</strong></p>' +
      '</div></div>' +
      '<div class="d-flex flex-between-center px-3">' +
      '<div></div>' +
      '<div><a class="btn btn-sm btn-falcon-default" href="' +
      escapeHtml(detailHref) +
      '"><span class="fas fa-eye me-1"></span>상세</a>' +
      '<a class="btn btn-sm btn-falcon-default ms-1" href="#" role="button" data-add-to-cart="true" data-product-id="' +
      escapeHtml(product.productId) +
      '" data-bs-toggle="tooltip" title="장바구니"><span class="fas fa-cart-plus"></span></a></div>' +
      '</div></div></div>'
    );
  }

  function renderProducts(products) {
    if (!grid) {
      return;
    }
    if (!products || !products.length) {
      grid.innerHTML =
        '<div class="col-12 text-center text-600 py-5">등록된 상품이 없습니다.</div>';
      if (countEl) {
        countEl.textContent = '0 Products';
      }
      return;
    }
    var html = '';
    var pending = products.length;
    products.forEach(function (p) {
      resolvePublicPath(detailPath(p.productId), function (href) {
        html += renderCard(p, href);
        pending -= 1;
        if (pending === 0) {
          grid.innerHTML = html;
          if (countEl) {
            countEl.textContent = 'Showing 1-' + products.length + ' of ' + products.length + ' Products';
          }
        }
      });
    });
  }

  function load() {
    if (grid) {
      grid.innerHTML = '<div class="col-12 text-center text-600 py-5">상품을 불러오는 중...</div>';
    }
    fetch(apiUrl, { credentials: 'same-origin', headers: { Accept: 'application/json' } })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var msg =
            (result.data && result.data.message) ||
            '상품을 불러오지 못했습니다. 업체 도메인으로 접속했는지 확인해 주세요.';
          if (grid) {
            grid.innerHTML = '<div class="col-12 text-center text-danger py-5">' + escapeHtml(msg) + '</div>';
          }
          return;
        }
        renderProducts(result.data.products || []);
      })
      .catch(function () {
        if (grid) {
          grid.innerHTML = '<div class="col-12 text-center text-danger py-5">서버 연결에 실패했습니다.</div>';
        }
      });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', load);
  } else {
    load();
  }
})();
