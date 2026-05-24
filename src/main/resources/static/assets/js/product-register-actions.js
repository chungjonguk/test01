/**
 * 상품 등록·수정 — /app/e-commerce/product/product-register
 * 이미지 최대 5개 (URL + 파일 업로드) · 대표 이미지 선택
 * API: /api/ecommerce/products, /api/ecommerce/products/upload-image
 * @module product-register-actions
 */
(function () {
  'use strict';

  var MAX_IMAGES = 5;

  function $(id) {
    return document.getElementById(id);
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

  function normalizeUrlKey(url) {
    return resolveImgUrl(url);
  }

  function showMessage(el, type, text) {
    if (!el) {
      return;
    }
    el.classList.remove('d-none', 'alert-success', 'alert-danger');
    el.classList.add(type === 'success' ? 'alert-success' : 'alert-danger');
    el.textContent = text;
  }

  function createSlotHtml(index) {
    return (
      '<div class="col-12 col-md-6 product-image-slot" data-slot-index="' +
      index +
      '">' +
      '<div class="card h-100 border product-image-slot-card">' +
      '<div class="card-header py-2 d-flex justify-content-between align-items-center flex-wrap gap-1">' +
      '<span class="fs--1 fw-semi-bold">이미지 ' +
      (index + 1) +
      '</span>' +
      '<label class="fs--2 mb-0 d-flex align-items-center gap-1 slot-main-label">' +
      '<input type="radio" name="product-main-image" class="slot-main-radio" title="대표 이미지"/>' +
      '<span class="fas fa-star text-warning"></span> 대표</label>' +
      '<button type="button" class="btn btn-link btn-sm text-danger p-0 btn-remove-slot" title="삭제">' +
      '<span class="fas fa-times"></span></button></div>' +
      '<div class="card-body">' +
      '<div class="text-center mb-2 border rounded-1 p-2 bg-white">' +
      '<img class="slot-preview rounded-1" alt="미리보기" style="max-height:140px;max-width:100%;object-fit:contain" ' +
      'src="/assets/img/products/1.jpg" onerror="this.src=\'/assets/img/products/1.jpg\'"/></div>' +
      '<label class="form-label fs--1">이미지 URL</label>' +
      '<input type="text" class="form-control form-control-sm slot-url" placeholder="/assets/img/products/2.jpg" maxlength="500"/>' +
      '<label class="form-label fs--1 mt-2">파일 업로드</label>' +
      '<input type="file" class="form-control form-control-sm slot-file" accept="image/jpeg,image/png,image/gif,image/webp"/>' +
      '<div class="slot-upload-msg fs--2 text-600 mt-1"></div>' +
      '</div></div></div>'
    );
  }

  /**
   * 상품 등록·수정 폼 초기화 — 이미지 슬롯·업로드·저장 이벤트 바인딩.
   * @returns {void}
   */
  window.ProductRegisterInit = function () {
    var form = $('product-register-form');
    var slotsRoot = $('product-image-slots');
    if (!form || !slotsRoot) {
      return;
    }

    var maxImages = parseInt(form.dataset.maxImages || slotsRoot.dataset.max || '5', 10);
    MAX_IMAGES = maxImages;
    var uploadUrl = slotsRoot.dataset.uploadUrl || '/api/ecommerce/products/upload-image';
    var apiBase = form.dataset.apiBase || '/api/ecommerce/products';
    var imagesPageBase = form.dataset.imagesUrl || '/app/e-commerce/product/product-images';
    var msgEl = $('product-form-message');
    var addBtn = $('btn-add-image-slot');
    var countHint = $('product-image-count-hint');

    var initial = window.__INITIAL_PRODUCT_IMAGES__ || [];
    if (!Array.isArray(initial)) {
      initial = [];
    }
    var initialMain = window.__INITIAL_MAIN_IMAGE_URL__ || '';

    function slotCount() {
      return slotsRoot.querySelectorAll('.product-image-slot').length;
    }

    function updateUi() {
      var n = slotCount();
      if (countHint) {
        countHint.textContent = n + ' / ' + MAX_IMAGES + ' 등록 슬롯 · 별(대표) 1개 선택';
      }
      if (addBtn) {
        addBtn.disabled = n >= MAX_IMAGES;
      }
      refreshMainHighlight();
    }

    function refreshMainHighlight() {
      slotsRoot.querySelectorAll('.product-image-slot-card').forEach(function (card) {
        card.classList.remove('border-warning', 'shadow-sm');
      });
      var checked = slotsRoot.querySelector('.slot-main-radio:checked');
      if (checked) {
        var card = checked.closest('.product-image-slot-card');
        if (card) {
          card.classList.add('border-warning', 'shadow-sm');
        }
      }
    }

    function ensureMainSelected() {
      var radios = slotsRoot.querySelectorAll('.slot-main-radio');
      var checked = slotsRoot.querySelector('.slot-main-radio:checked');
      if (checked) {
        return;
      }
      for (var i = 0; i < radios.length; i++) {
        var slot = radios[i].closest('.product-image-slot');
        var urlInput = slot && slot.querySelector('.slot-url');
        if (urlInput && urlInput.value.trim()) {
          radios[i].checked = true;
          refreshMainHighlight();
          return;
        }
      }
      if (radios.length) {
        radios[0].checked = true;
        refreshMainHighlight();
      }
    }

    function selectMainByUrl(mainUrl) {
      if (!mainUrl) {
        return false;
      }
      var key = normalizeUrlKey(mainUrl);
      var matched = false;
      slotsRoot.querySelectorAll('.product-image-slot').forEach(function (slot) {
        var urlInput = slot.querySelector('.slot-url');
        var radio = slot.querySelector('.slot-main-radio');
        if (urlInput && radio && normalizeUrlKey(urlInput.value) === key) {
          radio.checked = true;
          matched = true;
        }
      });
      if (matched) {
        refreshMainHighlight();
      }
      return matched;
    }

    function bindSlot(slotEl) {
      var urlInput = slotEl.querySelector('.slot-url');
      var fileInput = slotEl.querySelector('.slot-file');
      var preview = slotEl.querySelector('.slot-preview');
      var uploadMsg = slotEl.querySelector('.slot-upload-msg');
      var removeBtn = slotEl.querySelector('.btn-remove-slot');
      var mainRadio = slotEl.querySelector('.slot-main-radio');

      function refreshPreview() {
        var url = urlInput ? urlInput.value.trim() : '';
        if (preview) {
          preview.src = resolveImgUrl(url);
        }
      }

      if (urlInput) {
        urlInput.addEventListener('input', refreshPreview);
      }

      if (mainRadio) {
        mainRadio.addEventListener('change', refreshMainHighlight);
      }

      if (fileInput) {
        fileInput.addEventListener('change', function () {
          var file = fileInput.files && fileInput.files[0];
          if (!file) {
            return;
          }
          if (uploadMsg) {
            uploadMsg.textContent = '업로드 중...';
            uploadMsg.classList.remove('text-success', 'text-danger');
          }
          var fd = new FormData();
          fd.append('file', file);
          fetch(uploadUrl, { method: 'POST', body: fd })
            .then(function (res) {
              return res.json().then(function (d) {
                return { ok: res.ok, data: d };
              });
            })
            .then(function (r) {
              if (!r.ok || !r.data.success) {
                throw new Error((r.data && r.data.message) || '업로드 실패');
              }
              if (urlInput) {
                urlInput.value = r.data.url;
              }
              refreshPreview();
              if (!slotsRoot.querySelector('.slot-main-radio:checked') && mainRadio) {
                mainRadio.checked = true;
                refreshMainHighlight();
              }
              if (uploadMsg) {
                uploadMsg.textContent = '업로드 완료';
                uploadMsg.classList.add('text-success');
              }
            })
            .catch(function (err) {
              if (uploadMsg) {
                uploadMsg.textContent = err.message || '업로드 오류';
                uploadMsg.classList.add('text-danger');
              }
            })
            .finally(function () {
              fileInput.value = '';
            });
        });
      }

      if (removeBtn) {
        removeBtn.addEventListener('click', function () {
          var wasMain = mainRadio && mainRadio.checked;
          if (slotCount() <= 1) {
            if (urlInput) {
              urlInput.value = '';
            }
            refreshPreview();
            if (mainRadio) {
              mainRadio.checked = true;
            }
            refreshMainHighlight();
            return;
          }
          slotEl.remove();
          if (wasMain) {
            ensureMainSelected();
          }
          updateUi();
        });
      }

      refreshPreview();
    }

    function addSlot(url) {
      if (slotCount() >= MAX_IMAGES) {
        return;
      }
      var index = slotCount();
      var wrap = document.createElement('div');
      wrap.innerHTML = createSlotHtml(index);
      var slot = wrap.firstElementChild;
      slotsRoot.appendChild(slot);
      if (url) {
        var urlInput = slot.querySelector('.slot-url');
        if (urlInput) {
          urlInput.value = url;
        }
      }
      bindSlot(slot);
      updateUi();
    }

    if (addBtn) {
      addBtn.addEventListener('click', function () {
        addSlot('');
        ensureMainSelected();
      });
    }

    if (initial.length) {
      initial.forEach(function (u) {
        addSlot(u);
      });
    } else {
      addSlot('');
    }
    if (!selectMainByUrl(initialMain)) {
      ensureMainSelected();
    }
    updateUi();

    function collectImageUrls() {
      var urls = [];
      slotsRoot.querySelectorAll('.product-image-slot').forEach(function (slot) {
        var input = slot.querySelector('.slot-url');
        var v = input ? input.value.trim() : '';
        if (v) {
          urls.push(v);
        }
      });
      var unique = [];
      urls.forEach(function (u) {
        if (unique.indexOf(u) === -1) {
          unique.push(u);
        }
      });
      return unique;
    }

    function collectMainImageUrl(imageUrls) {
      var checked = slotsRoot.querySelector('.slot-main-radio:checked');
      if (checked) {
        var slot = checked.closest('.product-image-slot');
        var input = slot && slot.querySelector('.slot-url');
        var v = input ? input.value.trim() : '';
        if (v) {
          return v;
        }
      }
      return imageUrls.length ? imageUrls[0] : null;
    }

    form.addEventListener('submit', function (e) {
      e.preventDefault();
      var imageUrls = collectImageUrls();
      if (!imageUrls.length) {
        showMessage(msgEl, 'error', '상품 이미지를 1개 이상 등록해 주세요.');
        return;
      }
      if (imageUrls.length > MAX_IMAGES) {
        showMessage(msgEl, 'error', '이미지는 최대 ' + MAX_IMAGES + '개까지 등록할 수 있습니다.');
        return;
      }

      var mainImageUrl = collectMainImageUrl(imageUrls);
      if (!mainImageUrl) {
        showMessage(msgEl, 'error', '대표 이미지를 선택해 주세요.');
        return;
      }

      var payload = {
        productNm: $('product-nm').value.trim(),
        categoryCd: $('category-cd').value,
        price: parseFloat($('price').value, 10),
        stockQty: parseInt($('stock-qty').value, 10),
        statusCd: $('status-cd').value,
        imageUrls: imageUrls,
        mainImageUrl: mainImageUrl,
        description: $('description').value.trim() || null
      };

      var editMode = form.dataset.editMode === 'true';
      var productId = form.dataset.productId;
      var url = apiBase;
      var method = 'POST';
      if (editMode && productId) {
        url = apiBase + '/' + encodeURIComponent(productId);
        method = 'PUT';
        payload.productId = parseInt(productId, 10);
      }

      var btn = $('btn-save-product');
      if (btn) {
        btn.disabled = true;
      }

      fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify(payload)
      })
        .then(function (res) {
          return res.json().then(function (data) {
            return { ok: res.ok, data: data };
          });
        })
        .then(function (result) {
          if (!result.ok || !result.data.success) {
            throw new Error((result.data && result.data.message) || '저장 실패');
          }
          showMessage(msgEl, 'success', result.data.message || '저장되었습니다.');
          var newId = result.data.productId;
          setTimeout(function () {
            if (newId) {
              window.location.href = imagesPageBase + '?id=' + encodeURIComponent(newId);
            } else {
              window.location.href = form.dataset.manageUrl || '/app/e-commerce/product/product-manage';
            }
          }, 500);
        })
        .catch(function (err) {
          showMessage(msgEl, 'error', err.message || '오류');
        })
        .finally(function () {
          if (btn) {
            btn.disabled = false;
          }
        });
    });
  };
})();
