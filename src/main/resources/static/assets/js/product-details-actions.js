/**
 * 상품 상세 — 이미지 Swiper 슬라이드 (최대 5장)
 */
(function () {
  'use strict';

  var SWIPER_CFG_MULTI =
    '{"spaceBetween":8,"loop":true,"loopedSlides":5,"autoplay":{"delay":4000,"disableOnInteraction":false},' +
    '"thumb":{"spaceBetween":8,"slidesPerView":5,"loop":true,"freeMode":true,"grabCursor":true,"loopedSlides":5,' +
    '"centeredSlides":true,"slideToClickedSlide":true,"watchSlidesVisibility":true,"watchSlidesProgress":true},' +
    '"slideToClickedSlide":true}';

  function escapeHtml(v) {
    return String(v == null ? '' : v)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function escapeAttr(v) {
    return escapeHtml(v).replace(/'/g, '&#39;');
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

  function orderImagesWithMain(images, mainUrl) {
    if (!images.length) {
      return [];
    }
    if (!mainUrl) {
      return images.slice();
    }
    var mr = resolveImgUrl(mainUrl);
    var head = [];
    var tail = [];
    images.forEach(function (u) {
      if (resolveImgUrl(u) === mr && !head.length) {
        head.push(u);
      } else {
        tail.push(u);
      }
    });
    return head.length ? head.concat(tail) : images.slice();
  }

  function formatPrice(n) {
    return n == null ? '0원' : Number(n).toLocaleString('ko-KR') + '원';
  }

  function buildSlidesHtml(images, altText) {
    return images
      .map(function (url, i) {
        var u = resolveImgUrl(url);
        return (
          '<div class="swiper-slide">' +
          '<div class="position-relative text-center">' +
          '<img class="rounded-1 w-100 product-details-main-img" src="' +
          escapeAttr(u) +
          '" alt="' +
          escapeAttr(altText || '') +
          '" style="max-height:420px;object-fit:contain" onerror="this.src=\'/assets/img/products/1.jpg\'"/>' +
          (i === 0
            ? '<span class="badge bg-warning text-dark position-absolute top-0 start-0 m-2 fs--2">대표</span>'
            : '') +
          '</div></div>'
        );
      })
      .join('');
  }

  function renderGalleryHtml(images, altText) {
    if (!images.length) {
      return '<p class="text-600 mb-0">등록된 이미지가 없습니다.</p>';
    }
    if (images.length === 1) {
      var single = resolveImgUrl(images[0]);
      return (
        '<div class="product-details-gallery border rounded-1 p-2 bg-light text-center">' +
        '<img class="rounded-1 w-100 product-details-main-img" src="' +
        escapeAttr(single) +
        '" alt="' +
        escapeAttr(altText || '') +
        '" style="max-height:420px;object-fit:contain" onerror="this.src=\'/assets/img/products/1.jpg\'"/>' +
        '</div>'
      );
    }
    return (
      '<div class="product-details-gallery border rounded-1 p-2 bg-light">' +
      '<div class="product-slider" id="galleryTop">' +
      '<div class="swiper-container theme-slider product-details-swiper" data-swiper=\'' +
      SWIPER_CFG_MULTI +
      "'>" +
      '<div class="swiper-wrapper">' +
      buildSlidesHtml(images, altText) +
      '</div>' +
      '<div class="swiper-nav">' +
      '<div class="swiper-button-next swiper-button-white"></div>' +
      '<div class="swiper-button-prev swiper-button-white"></div>' +
      '</div></div></div></div>'
    );
  }

  function initProductDetailSwipers(root) {
    var scope = root || document;
    if (typeof window.Swiper === 'undefined') {
      return;
    }
    scope.querySelectorAll('.product-details-swiper[data-swiper]').forEach(function (el) {
      if (el.swiper) {
        return;
      }
      try {
        var options = JSON.parse(el.getAttribute('data-swiper') || '{}');
        var thumbOpts = options.thumb;
        var thumbsInit = null;

        if (thumbOpts) {
          var thumbImages = el.querySelectorAll('.swiper-slide img');
          var slides = '';
          thumbImages.forEach(function (img) {
            slides +=
              '<div class="swiper-slide"><img class="img-fluid rounded mt-1" src="' +
              escapeAttr(img.src) +
              '" alt="" style="height:64px;object-fit:cover"/></div>';
          });
          var thumbs = document.createElement('div');
          thumbs.setAttribute('class', 'swiper-container thumb product-details-thumbs');
          thumbs.innerHTML = '<div class="swiper-wrapper">' + slides + '</div>';
          el.parentNode.appendChild(thumbs);
          thumbsInit = new window.Swiper(thumbs, thumbOpts);
        }

        var nav = el.querySelector('.swiper-nav');
        new window.Swiper(el, {
          spaceBetween: options.spaceBetween || 8,
          loop: options.loop !== false,
          loopedSlides: options.loopedSlides || 5,
          autoplay: options.autoplay,
          slideToClickedSlide: options.slideToClickedSlide,
          navigation: {
            nextEl: nav ? nav.querySelector('.swiper-button-next') : null,
            prevEl: nav ? nav.querySelector('.swiper-button-prev') : null
          },
          thumbs: thumbsInit ? { swiper: thumbsInit } : undefined
        });
      } catch (ignore) {
        /* theme.js may initialize */
      }
    });
  }

  window.ProductDetailsInit = function () {
    var dbPanel = document.getElementById('product-detail-db');
    if (dbPanel) {
      initProductDetailSwipers(dbPanel);
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
        images = orderImagesWithMain(images, p.mainImageUrl || p.imgUrl);
        body.innerHTML =
          '<div class="mb-3">' +
          '<a class="btn btn-falcon-default btn-sm" href="' +
          escapeHtml(imagesBase + '?id=' + p.productId) +
          '"><span class="fas fa-images me-1"></span>이미지 전체 (' +
          images.length +
          ')</a></div>' +
          '<div class="row"><div class="col-lg-6 mb-4 mb-lg-0">' +
          renderGalleryHtml(images, p.productNm) +
          '</div><div class="col-lg-6"><h5>' +
          escapeHtml(p.productNm) +
          '</h5><h4 class="text-warning">' +
          formatPrice(p.price) +
          '</h4><p>재고: ' +
          escapeHtml(p.stockQty) +
          '</p></div></div>';
        shell.classList.remove('d-none');
        initProductDetailSwipers(shell);
      })
      .catch(function () {
        body.innerHTML = '<div class="text-danger text-center py-4">상품을 찾을 수 없습니다.</div>';
        shell.classList.remove('d-none');
      });
  };
})();
