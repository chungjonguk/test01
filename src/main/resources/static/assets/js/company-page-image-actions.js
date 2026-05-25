/**
 * 업체별 페이지 이미지 — /admin/company-page-images
 * API: /api/admin/company-page-images
 */
(function () {
  'use strict';

  var C = window.PrintMallCommon || {};
  var $ = C.$ || function (id) {
    return document.getElementById(id);
  };
  var escapeHtml = C.escapeHtml || function (v) {
    return String(v == null ? '' : v)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  };
  var fetchJson = C.fetchJson || function (url, options) {
    return fetch(url, options || {}).then(function (res) {
      return res.json().then(function (data) {
        return { ok: res.ok, status: res.status, data: data };
      });
    });
  };

  var API = '/api/admin/company-page-images';
  var objectUrls = new WeakMap();

  function resolveImgUrl(url) {
    if (!url || !String(url).trim()) {
      return '';
    }
    var t = String(url).trim();
    if (t.indexOf('http://') === 0 || t.indexOf('https://') === 0 || t.indexOf('blob:') === 0) {
      return t;
    }
    if (t.indexOf('/') === 0) {
      return t;
    }
    return '/' + t;
  }

  function withCacheBust(url) {
    if (!url || url.indexOf('blob:') === 0) {
      return url;
    }
    var sep = url.indexOf('?') >= 0 ? '&' : '?';
    return url + sep + 't=' + Date.now();
  }

  function findSlotCol(pageCd) {
    return document.querySelector(
      '#company-page-image-slots [data-page-cd="' + pageCd + '"]'
    );
  }

  function getPreviewBox(col) {
    return col ? col.querySelector('.company-page-image-preview') : null;
  }

  function revokeLocalUrl(col) {
    var prev = objectUrls.get(col);
    if (prev) {
      URL.revokeObjectURL(prev);
      objectUrls.delete(col);
    }
  }

  function setPreviewBadge(col, text, className) {
    var badge = col ? col.querySelector('.company-page-image-preview-badge') : null;
    if (!badge) {
      return;
    }
    if (!text) {
      badge.classList.add('d-none');
      badge.textContent = '';
      return;
    }
    badge.className = 'badge fs--2 company-page-image-preview-badge ' + (className || 'badge-soft-secondary');
    badge.textContent = text;
    badge.classList.remove('d-none');
  }

  function renderPreview(col, image, options) {
    options = options || {};
    var box = getPreviewBox(col);
    if (!box) {
      return;
    }
    var img = box.querySelector('.company-page-image-preview-img');
    var empty = box.querySelector('.company-page-image-empty');
    var deleteBtn = col.querySelector('.btn-company-page-delete');
    var previewBtn = col.querySelector('.btn-company-page-preview');
    var altInput = col.querySelector('.company-page-image-alt');

    if (options.localFile) {
      revokeLocalUrl(col);
      var blobUrl = URL.createObjectURL(options.localFile);
      objectUrls.set(col, blobUrl);
      if (img) {
        img.src = blobUrl;
        img.alt = (altInput && altInput.value.trim()) || options.localFile.name || '미리보기';
        img.classList.remove('d-none');
      }
      if (empty) {
        empty.classList.add('d-none');
      }
      setPreviewBadge(col, '선택 미리보기', 'badge-soft-warning');
      if (deleteBtn) {
        deleteBtn.disabled = true;
      }
      if (previewBtn) {
        previewBtn.disabled = false;
      }
      return;
    }

    if (!image || !image.url) {
      revokeLocalUrl(col);
      if (img) {
        img.removeAttribute('src');
        img.classList.add('d-none');
      }
      if (empty) {
        empty.classList.remove('d-none');
      }
      setPreviewBadge(col, null);
      if (deleteBtn) {
        deleteBtn.disabled = true;
        deleteBtn.removeAttribute('data-image-id');
      }
      if (previewBtn) {
        previewBtn.disabled = true;
      }
      if (altInput && !options.keepAlt) {
        altInput.value = '';
      }
      return;
    }

    revokeLocalUrl(col);
    var src = options.bustCache ? withCacheBust(resolveImgUrl(image.url)) : resolveImgUrl(image.url);
    if (img) {
      img.onerror = function () {
        img.classList.add('d-none');
        if (empty) {
          empty.textContent = '이미지를 불러올 수 없습니다.';
          empty.classList.remove('d-none');
        }
        setPreviewBadge(col, '로드 실패', 'badge-soft-danger');
      };
      img.onload = function () {
        img.classList.remove('d-none');
        if (empty) {
          empty.classList.add('d-none');
        }
        setPreviewBadge(col, '등록됨', 'badge-soft-success');
      };
      img.src = src;
      img.alt = image.altText || '';
      img.classList.remove('d-none');
    }
    if (empty) {
      empty.classList.add('d-none');
    }
    if (deleteBtn) {
      deleteBtn.disabled = false;
      deleteBtn.setAttribute('data-image-id', String(image.imageId));
    }
    if (previewBtn) {
      previewBtn.disabled = false;
    }
    if (altInput && image.altText) {
      altInput.value = image.altText;
    }
  }

  function getCurrentPreviewSrc(col) {
    var img = col.querySelector('.company-page-image-preview-img');
    return img && img.src && !img.classList.contains('d-none') ? img.src : '';
  }

  function openPreviewModal(col) {
    var src = getCurrentPreviewSrc(col);
    if (!src) {
      showMessage('미리볼 이미지가 없습니다. 파일을 선택하거나 등록하세요.', true);
      return;
    }
    var modal = $('company-page-image-preview-modal');
    var modalImg = $('company-page-image-preview-modal-img');
    var label = col.querySelector('.card-header h6');
    var caption = $('company-page-image-preview-modal-caption');
    if (!modal || !modalImg) {
      window.open(src, '_blank');
      return;
    }
    modalImg.src = src;
    modalImg.alt = (col.querySelector('.company-page-image-alt') || {}).value || '';
    if (caption && label) {
      caption.textContent = label.textContent || '';
    }
    if (window.bootstrap && window.bootstrap.Modal) {
      window.bootstrap.Modal.getOrCreateInstance(modal).show();
    } else {
      modal.classList.add('show');
      modal.style.display = 'block';
    }
  }

  function showMessage(html, isError) {
    var box = $('company-page-image-message');
    if (!box) {
      return;
    }
    box.innerHTML =
      '<div class="alert alert-' +
      (isError ? 'danger' : 'success') +
      ' py-2 fs--1 mb-0" role="alert">' +
      html +
      '</div>';
  }

  function getCompanyId() {
    var select = $('dashboard-company-select');
    if (select && select.value) {
      return parseInt(select.value, 10);
    }
    var host = $('company-page-image-slots');
    if (host && host.getAttribute('data-initial-company-id')) {
      return parseInt(host.getAttribute('data-initial-company-id'), 10);
    }
    return null;
  }

  function applySlots(slots) {
    if (!slots || !slots.length) {
      return;
    }
    slots.forEach(function (slot) {
      var col = findSlotCol(slot.pageCd);
      if (!col) {
        return;
      }
      renderPreview(col, slot.image || null, { bustCache: true });
    });
  }

  function loadImages() {
    var companyId = getCompanyId();
    if (!companyId) {
      showMessage('업체를 선택하세요.', true);
      return Promise.resolve();
    }
    return fetchJson(API + '?companyId=' + companyId)
      .then(function (result) {
        if (!result.ok || !result.data || !result.data.success) {
          throw new Error((result.data && result.data.message) || '목록 조회 실패');
        }
        applySlots(result.data.slots || []);
        showMessage('업체 <strong>' + companyId + '</strong> 이미지를 불러왔습니다.', false);
      })
      .catch(function (err) {
        showMessage(escapeHtml(err.message || '조회 실패'), true);
      });
  }

  function uploadSlot(col) {
    var companyId = getCompanyId();
    var pageCd = col.getAttribute('data-page-cd');
    var fileInput = col.querySelector('.company-page-image-file');
    var altInput = col.querySelector('.company-page-image-alt');
    if (!companyId || !pageCd) {
      showMessage('업체·슬롯 정보가 없습니다.', true);
      return;
    }
    if (!fileInput || !fileInput.files || !fileInput.files.length) {
      showMessage('이미지 파일을 선택하세요.', true);
      return;
    }
    var form = new FormData();
    form.append('companyId', String(companyId));
    form.append('pageCd', pageCd);
    form.append('file', fileInput.files[0]);
    if (altInput && altInput.value.trim()) {
      form.append('altText', altInput.value.trim());
    }
    showMessage('업로드 중…', false);
    fetch(API, { method: 'POST', body: form, credentials: 'same-origin' })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (result) {
        if (!result.ok || !result.data.success) {
          throw new Error(result.data.message || '업로드 실패');
        }
        renderPreview(col, result.data.image, { bustCache: true });
        fileInput.value = '';
        showMessage(result.data.message || '등록되었습니다.', false);
      })
      .catch(function (err) {
        showMessage(escapeHtml(err.message || '업로드 실패'), true);
      });
  }

  function deleteImage(imageId, col) {
    if (!imageId) {
      return;
    }
    if (!window.confirm('이 슬롯의 이미지 등록을 삭제할까요?')) {
      return;
    }
    fetchJson(API + '/' + imageId, { method: 'DELETE' })
      .then(function (result) {
        if (!result.ok || !result.data || !result.data.success) {
          throw new Error((result.data && result.data.message) || '삭제 실패');
        }
        var fileInput = col.querySelector('.company-page-image-file');
        if (fileInput) {
          fileInput.value = '';
        }
        renderPreview(col, null);
        showMessage(result.data.message || '삭제되었습니다.', false);
      })
      .catch(function (err) {
        showMessage(escapeHtml(err.message || '삭제 실패'), true);
      });
  }

  function bindCompanySelect() {
    var select = $('dashboard-company-select');
    if (!select) {
      return;
    }
    select.addEventListener('change', function () {
      var id = select.value ? parseInt(select.value, 10) : null;
      if (!id) {
        return;
      }
      var chain = window.DashboardConfigStore
        ? window.DashboardConfigStore.selectCompany(id)
        : Promise.resolve();
      chain.then(loadImages).catch(function () {
        loadImages();
      });
    });
  }

  function bindSlots() {
    document.querySelectorAll('#company-page-image-slots [data-page-cd]').forEach(function (col) {
      var uploadBtn = col.querySelector('.btn-company-page-upload');
      var deleteBtn = col.querySelector('.btn-company-page-delete');
      var previewBtn = col.querySelector('.btn-company-page-preview');
      var fileInput = col.querySelector('.company-page-image-file');
      var altInput = col.querySelector('.company-page-image-alt');
      var previewImg = col.querySelector('.company-page-image-preview-img');

      if (fileInput) {
        fileInput.addEventListener('change', function () {
          var file = fileInput.files && fileInput.files[0];
          if (!file) {
            return;
          }
          if (!file.type || file.type.indexOf('image/') !== 0) {
            showMessage('이미지 파일만 선택할 수 있습니다.', true);
            fileInput.value = '';
            return;
          }
          renderPreview(col, null, { localFile: file });
        });
      }

      if (altInput && previewImg) {
        altInput.addEventListener('input', function () {
          if (previewImg.src && !previewImg.classList.contains('d-none')) {
            previewImg.alt = altInput.value.trim() || '미리보기';
          }
        });
      }

      if (previewBtn) {
        previewBtn.addEventListener('click', function () {
          openPreviewModal(col);
        });
      }

      if (previewImg) {
        previewImg.addEventListener('click', function () {
          if (!previewImg.classList.contains('d-none')) {
            openPreviewModal(col);
          }
        });
        previewImg.style.cursor = 'zoom-in';
        previewImg.title = '클릭하면 크게 보기';
      }

      if (uploadBtn) {
        uploadBtn.addEventListener('click', function () {
          uploadSlot(col);
        });
      }
      if (deleteBtn) {
        deleteBtn.addEventListener('click', function () {
          deleteImage(deleteBtn.getAttribute('data-image-id'), col);
        });
      }
    });
  }

  function init() {
    if (!$('company-page-image-slots')) {
      return;
    }
    bindSlots();
    bindCompanySelect();
    loadImages();
  }

  window.CompanyPageImageInit = init;
})();
