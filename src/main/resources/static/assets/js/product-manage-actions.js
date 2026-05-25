/**
 * 상품 관리 그리드 — /app/e-commerce/product/product-manage
 * API: /api/ecommerce/products · 엑셀: PrintMallGridExcel
 * @module product-manage-actions
 */

(function () {

  'use strict';



  var C = window.PrintMallCommon;

  var $ = C.$;

  var escapeHtml = C.escapeHtml;

  var escapeAttr = C.escapeAttr;

  var fetchJson = C.fetchJson;

  var codeLabel = C.codeLabel;



  var state = { loading: false, products: [] };
  var productGridView = null;



  // --- 이미지·카드 렌더 ---



  function resolveImg(url) {

    if (!url || !String(url).trim()) {

      return '/assets/img/products/1.jpg';

    }

    var t = String(url).trim();

    if (t.indexOf('http://') === 0 || t.indexOf('https://') === 0 || t.indexOf('/') === 0) {

      return t;

    }

    return '/' + t;

  }



  function getImages(row) {

    var urls = [];

    if (row.imageUrls && row.imageUrls.length) {

      urls = row.imageUrls.slice();

    } else if (row.imgUrl) {

      urls = [row.imgUrl];

    }

    if (!urls.length) {

      return ['/assets/img/products/1.jpg'];

    }

    var main = row.mainImageUrl || row.imgUrl;

    if (!main) {

      return urls.map(resolveImg);

    }

    var mainResolved = resolveImg(main);

    var ordered = [];

    var rest = [];

    urls.forEach(function (u) {

      if (resolveImg(u) === mainResolved && !ordered.length) {

        ordered.push(u);

      } else {

        rest.push(u);

      }

    });

    if (!ordered.length) {

      return urls.map(resolveImg);

    }

    return ordered.concat(rest).map(resolveImg);

  }



  function categoryLabel(code) {

    return codeLabel('PRODUCT_CATEGORY', code);

  }



  function formatPrice(n) {

    if (window.PrintMallCurrency && window.PrintMallCurrency.formatWon) {

      return window.PrintMallCurrency.formatWon(n);

    }

    if (n == null || n === '') {

      return '0원';

    }

    return Number(n).toLocaleString('ko-KR') + '원';

  }



  function stockHtml(qty) {

    var q = parseInt(qty, 10);

    if (isNaN(q) || q <= 0) {

      return '<strong class="text-danger">품절</strong>';

    }

    return '<strong class="text-success">재고 ' + escapeHtml(q) + '개</strong>';

  }



  function statusBadgeHtml(cd) {

    if ((cd || '').toUpperCase() === 'NEW') {

      return '<span class="badge rounded-pill bg-success position-absolute mt-2 me-2 z-index-2 top-0 end-0">New</span>';

    }

    return '';

  }



  function renderImageBlock(images, detailUrl, statusCd, uid) {

    if (images.length <= 1) {

      return (

        '<div class="position-relative rounded-top overflow-hidden">' +

        '<a class="d-block" href="' +

        escapeHtml(detailUrl) +

        '"><img class="img-fluid rounded-top product-grid-card-img" src="' +

        escapeAttr(images[0]) +

        '" alt="" onerror="this.src=\'/assets/img/products/1.jpg\'"/></a>' +

        statusBadgeHtml(statusCd) +

        '</div>'

      );

    }

    var slides = images

      .map(function (url) {

        return (

          '<div class="swiper-slide"><a class="d-block" href="' +

          escapeHtml(detailUrl) +

          '"><img class="rounded-top img-fluid product-grid-card-img" src="' +

          escapeAttr(url) +

          '" alt="" onerror="this.src=\'/assets/img/products/1.jpg\'"/></a></div>'

        );

      })

      .join('');

    return (

      '<div class="position-relative rounded-top overflow-hidden">' +

      '<div class="swiper-container theme-slider product-manage-swiper" id="swiper-' +

      uid +

      '" data-swiper=\'{"autoplay":true,"autoHeight":true,"spaceBetween":5,"loop":true,"loopedSlides":5,"navigation":{"nextEl":"#swiper-' +

      uid +

      ' .swiper-button-next","prevEl":"#swiper-' +

      uid +

      ' .swiper-button-prev"}}\'>' +

      '<div class="swiper-wrapper">' +

      slides +

      '</div>' +

      '<div class="swiper-nav">' +

      '<div class="swiper-button-next swiper-button-white"></div>' +

      '<div class="swiper-button-prev swiper-button-white"></div>' +

      '</div></div>' +

      statusBadgeHtml(statusCd) +

      (images.length > 1

        ? '<span class="badge rounded-pill bg-dark position-absolute mt-2 ms-2 z-index-2 top-0 start-0">' +

          images.length +

          '</span>'

        : '') +

      '</div>'

    );

  }



  function renderCard(row, index) {

    var id = row.productId;

    var detailUrl = '/app/e-commerce/product/product-details?id=' + encodeURIComponent(id);

    var editUrl = '/app/e-commerce/product/product-register?id=' + encodeURIComponent(id);

    var imagesUrl = '/app/e-commerce/product/product-images?id=' + encodeURIComponent(id);

    var images = getImages(row);

    var uid = 'pm-' + id;



    return (

      '<div class="mb-4 col-md-6 col-lg-4 product-manage-card" data-product-id="' +

      escapeAttr(id) +

      '">' +

      '<div class="border rounded-1 h-100 d-flex flex-column justify-content-between pb-3">' +

      '<div class="overflow-hidden">' +

      renderImageBlock(images, detailUrl, row.statusCd, uid) +

      '<div class="p-3">' +

      '<h5 class="fs-0"><a class="text-dark" href="' +

      escapeHtml(detailUrl) +

      '">' +

      escapeHtml(row.productNm) +

      '</a></h5>' +

      '<p class="fs--1 mb-3"><span class="text-500">' +

      escapeHtml(categoryLabel(row.categoryCd)) +

      '</span></p>' +

      '<h5 class="fs-md-2 text-warning mb-0 d-flex align-items-center mb-3">' +

      formatPrice(row.price) +

      '</h5>' +

      '<p class="fs--1 mb-1">상품 ID: <strong>#' +

      escapeHtml(id) +

      '</strong></p>' +

      '<p class="fs--1 mb-1">재고: ' +

      stockHtml(row.stockQty) +

      '</p>' +

      '</div></div>' +

      '<div class="d-flex flex-between-center px-3">' +

      '<div class="fs--1 text-600">#' +

      (index + 1) +

      '</div>' +

      '<div>' +

      '<a class="btn btn-sm btn-falcon-default me-1" href="' +

      escapeHtml(imagesUrl) +

      '" data-bs-toggle="tooltip" title="이미지"><span class="fas fa-images"></span></a>' +

      '<a class="btn btn-sm btn-falcon-default me-1" href="' +

      escapeHtml(editUrl) +

      '" data-bs-toggle="tooltip" title="수정"><span class="fas fa-edit"></span></a>' +

      '<button type="button" class="btn btn-sm btn-falcon-default btn-delete-product" data-id="' +

      escapeAttr(id) +

      '" data-bs-toggle="tooltip" title="삭제"><span class="fas fa-trash-alt text-danger"></span></button>' +

      '</div></div></div></div>'

    );

  }



  // --- 검색·정렬 ---



  function sortProducts(products) {

    var sortEl = $('product-sort');

    var mode = sortEl ? sortEl.value : 'newest';

    var list = products.slice();

    list.sort(function (a, b) {

      if (mode === 'price') {

        return Number(a.price) - Number(b.price);

      }

      if (mode === 'newest' || !mode) {

        return Number(b.productId) - Number(a.productId);

      }

      return String(a.productNm || '').localeCompare(String(b.productNm || ''), 'ko');

    });

    return list;

  }



  function getSearchValues() {

    var nm = $('search-product-nm');

    var cat = $('search-category-cd');

    var st = $('search-status-cd');

    var nmM = $('search-product-nm-mobile');

    var catM = $('search-category-cd-mobile');

    var stM = $('search-status-cd-mobile');

    return {

      productNm: (nm && nm.value.trim()) || (nmM && nmM.value.trim()) || '',

      categoryCd: (cat && cat.value) || (catM && catM.value) || '',

      statusCd: (st && st.value) || (stM && stM.value) || ''

    };

  }



  function buildQuery() {

    var v = getSearchValues();

    var p = new URLSearchParams();

    if (v.productNm) {

      p.set('productNm', v.productNm);

    }

    if (v.categoryCd) {

      p.set('categoryCd', v.categoryCd);

    }

    if (v.statusCd) {

      p.set('statusCd', v.statusCd);

    }

    return p;

  }



  function updateCountLabel(n) {

    var el = $('product-grid-count-label');

    if (el) {

      el.textContent = n ? '등록 상품 ' + n + '건' : '등록 상품 0건';

    }

  }



  // --- API 조회·그리드 ---



  function bindProductDeleteButtons(grid) {
    if (!grid) {
      return;
    }
    grid.querySelectorAll('.btn-delete-product').forEach(function (btn) {
      btn.addEventListener('click', function (e) {
        e.preventDefault();
        e.stopPropagation();
        var pid = btn.getAttribute('data-id');
        if (!pid || !window.confirm('이 상품을 삭제하시겠습니까?')) {
          return;
        }
        fetchJson('/api/ecommerce/products/' + encodeURIComponent(pid), {
          method: 'DELETE'
        })
          .then(function (r) {
            if (!r.ok || !r.data.success) {
              throw new Error((r.data && r.data.message) || '삭제 실패');
            }
            loadProducts();
          })
          .catch(function (err) {
            window.alert(err.message || '삭제 중 오류');
          });
      });
    });
  }

  function initProductCardWidgets(grid) {
    if (!grid) {
      return;
    }
    if (typeof window.bootstrap !== 'undefined') {
      grid.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(function (el) {
        new bootstrap.Tooltip(el);
      });
    }
    if (typeof window.Swiper !== 'undefined') {
      grid.querySelectorAll('.product-manage-swiper').forEach(function (el) {
        try {
          var cfg = el.getAttribute('data-swiper');
          var options = cfg ? JSON.parse(cfg) : {};
          if (el.swiper) {
            el.swiper.destroy(true, true);
          }
          new window.Swiper(el, options);
        } catch (ignore) {
          /* theme global init */
        }
      });
    }
  }

  var PRODUCT_EMPTY_HTML =
    '<div class="col-12 text-center text-600 py-5">조회 결과가 없습니다.<br/><a href="/app/e-commerce/product/product-register" class="btn btn-sm btn-primary mt-2">상품 등록</a></div>';

  function getProductGridView() {
    if (!productGridView && C.createGridViewPager) {
      productGridView = C.createGridViewPager({
        pagerRootId: 'product-manage-grid-pager',
        containerId: 'product-manage-grid',
        emptyHtml: PRODUCT_EMPTY_HTML,
        renderPage: paintProductCards
      });
    }
    return productGridView;
  }

  function paintProductCards(items, meta) {
    var grid = $('product-manage-grid');
    if (!grid || !items.length) {
      return;
    }
    var rowOffset = meta && meta.start ? meta.start - 1 : 0;
    grid.innerHTML = items
      .map(function (row, i) {
        return renderCard(row, rowOffset + i);
      })
      .join('');
    bindProductDeleteButtons(grid);
    initProductCardWidgets(grid);
  }

  /**
   * API 조회 결과로 상품 카드 그리드를 렌더링합니다.
   * @param {Array<Object>} products 상품 목록
   * @returns {void}
   */
  function renderGrid(products) {
    state.products = products;
    var sorted = sortProducts(products);
    updateCountLabel(sorted.length);
    var view = getProductGridView();
    if (view) {
      view.setData(sorted);
    }
  }



  /**
   * 검색 조건으로 상품 목록을 API에서 조회합니다.
   * @returns {void}
   */
  function loadProducts() {

    if (state.loading) {

      return;

    }

    state.loading = true;

    var grid = $('product-manage-grid');

    if (grid) {

      grid.innerHTML = '<div class="col-12 text-center text-600 py-5">조회 중...</div>';

    }

    fetchJson('/api/ecommerce/products?' + buildQuery().toString())

      .then(function (r) {

        if (!r.ok) {

          throw new Error('조회 실패');

        }

        renderGrid(r.data.products || []);

      })

      .catch(function () {

        if (grid) {

          grid.innerHTML =

            '<div class="col-12 text-center text-danger py-5">조회 중 오류가 발생했습니다.</div>';

        }

        updateCountLabel(0);

      })

      .finally(function () {

        state.loading = false;

      });

  }



  function bindSearch() {

    var form = $('product-search-form');

    if (form) {

      form.addEventListener('submit', function (e) {

        e.preventDefault();

        loadProducts();

      });

    }

    var btnM = $('btn-search-mobile');

    if (btnM) {

      btnM.addEventListener('click', function () {

        var nm = $('search-product-nm-mobile');

        var cat = $('search-category-cd-mobile');

        var st = $('search-status-cd-mobile');

        var nmD = $('search-product-nm');

        var catD = $('search-category-cd');

        var stD = $('search-status-cd');

        if (nmD && nm) {

          nmD.value = nm.value;

        }

        if (catD && cat) {

          catD.value = cat.value;

        }

        if (stD && st) {

          stD.value = st.value;

        }

        loadProducts();

      });

    }

    var sortEl = $('product-sort');

    if (sortEl) {

      sortEl.addEventListener('change', function () {

        renderGrid(state.products);

      });

    }

  }



  // --- 엑셀 (PrintMallGridExcel) ---



  function bindExcelActions() {

    if (!window.PrintMallGridExcel) {

      return;

    }

    window.PrintMallGridExcel.bind({

      exportBtnId: 'btn-product-excel-export',

      templateBtnId: 'btn-product-excel-template',

      uploadInputId: 'product-excel-upload-input',

      exportUrl: '/api/ecommerce/products/excel/export',

      templateUrl: '/api/ecommerce/products/excel/template',

      importUrl: '/api/ecommerce/products/excel/import',

      buildExportQuery: buildQuery,

      confirmMessage: '선택한 엑셀 파일로 상품을 일괄 등록·수정하시겠습니까?',

      onSuccess: function () {

        loadProducts();

      }

    });

  }



  /**
   * 상품 관리 화면 초기화 — 검색·엑셀·목록 조회 바인딩.
   * @returns {void}
   */
  window.ProductManageInit = function () {

    bindSearch();

    bindExcelActions();

    loadProducts();

  };

})();


