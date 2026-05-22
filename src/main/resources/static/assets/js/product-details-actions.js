/**
 * 상품 상세 — API ?id= 로드 (이미지 최대 5)
 */
(function () {
  'use strict';

  function escapeHtml(v) {
    return String(v == null ? '' : v)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function resolveImgUrl(url) {
    if (!url || !String(url).trim()) {
      return '/assets/img/products/1.jpg';
    }
    var t = String(url).trim();
    if (t.indexOf('http://') === 0 || t.indexOf('https://') === 0 || t.indexOf('/') === 0) {
      return t;
    }
    return '/' + t;
  }

  function formatPrice(n) {
    return n == null ? '0원' : Number(n).toLocaleString('ko-KR') + '원';
  }

  function renderGallery(images) {
    if (!images.length) {
      return '<p class="text-600">등록된 이미지가 없습니다.</p>';
    }
    var main = resolveImgUrl(images[0]);
    var thumbs = images
      .map(function (url) {
        var u = resolveImgUrl(url);
        return (
          '<img src="' +
          escapeHtml(u) +
          '" alt="" class="rounded-1 border me-1 mb-1" style="width:64px;height:64px;object-fit:cover"/>'
        );
      })
      .join('');
    return (
      '<div class="border rounded-1 p-2 bg-light text-center mb-2">' +
      '<img class="rounded-1 w-100" src="' +
      escapeHtml(main) +
      '" style="max-height:420px;object-fit:contain" onerror="this.src=\'/assets/img/products/1.jpg\'"/></div>' +
      '<div class="d-flex flex-wrap">' +
      thumbs +
      '</div>'
    );
  }

  window.ProductDetailsInit = function () {
    if (document.getElementById('product-detail-db')) {
      return;
    }
    var params = new URLSearchParams(window.location.search);
    var id = params.get('id');
    if (!id) {
      return;
    }
    var shell = document.getElementById('product-detail-api-shell');
    var body = document.getElementById('product-detail-api-body');
    if (!shell || !body) {
      return;
    }
    var apiBase = shell.dataset.apiBase || '/api/ecommerce/products';
    var imagesBase = shell.dataset.imagesBase || '/app/e-commerce/product/product-images';
    fetch(apiBase + '/' + encodeURIComponent(id), { headers: { Accept: 'application/json' } })
      .then(function (res) {
        if (!res.ok) {
          throw new Error('not found');
        }
        return res.json();
      })
      .then(function (p) {
        var images = p.imageUrls && p.imageUrls.length ? p.imageUrls.slice() : p.imgUrl ? [p.imgUrl] : [];
        var main = p.mainImageUrl || p.imgUrl;
        if (main && images.length > 1) {
          var mr = resolveImgUrl(main);
          var head = [];
          var tail = [];
          images.forEach(function (u) {
            if (resolveImgUrl(u) === mr && !head.length) {
              head.push(u);
            } else {
              tail.push(u);
            }
          });
          images = head.length ? head.concat(tail) : images;
        }
        body.innerHTML =
          '<div class="mb-3"><a class="btn btn-falcon-default btn-sm" href="' +
          escapeHtml(imagesBase + '?id=' + p.productId) +
          '"><span class="fas fa-images me-1"></span>이미지 전체 (' +
          images.length +
          ')</a></div>' +
          '<div class="row"><div class="col-lg-6">' +
          renderGallery(images) +
          '</div><div class="col-lg-6"><h5>' +
          escapeHtml(p.productNm) +
          '</h5><h4 class="text-warning">' +
          formatPrice(p.price) +
          '</h4><p>재고: ' +
          escapeHtml(p.stockQty) +
          '</p></div></div>';
        shell.classList.remove('d-none');
      })
      .catch(function () {
        body.innerHTML = '<div class="text-danger text-center py-4">상품을 찾을 수 없습니다.</div>';
        shell.classList.remove('d-none');
      });
  };
})();
