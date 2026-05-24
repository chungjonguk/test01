/**
 * 업체 관리 — /admin/companies
 * API: /api/admin/companies
 * @module company-manage-actions
 */
(function () {
  'use strict';

  var API_BASE = '/api/admin/companies';
  var KakaoAddress = window.PrintMallKakaoAddress;

  function notify(message, icon) {
    if (window.Swal) {
      window.Swal.fire({
        toast: true,
        position: 'top-end',
        icon: icon || 'info',
        title: message,
        showConfirmButton: false,
        timer: 2800,
        timerProgressBar: true
      });
      return;
    }
    window.alert(message);
  }

  function $(id) {
    return document.getElementById(id);
  }

  function digitsOnly(value) {
    return String(value == null ? '' : value).replace(/\D/g, '');
  }

  /** 사업자등록번호 10자리 → 000-00-00000 */
  function formatBizNo(value) {
    var d = digitsOnly(value).slice(0, 10);
    if (d.length <= 3) {
      return d;
    }
    if (d.length <= 5) {
      return d.slice(0, 3) + '-' + d.slice(3);
    }
    return d.slice(0, 3) + '-' + d.slice(3, 5) + '-' + d.slice(5);
  }

  function isValidBizNo(value) {
    if (!value || !String(value).trim()) {
      return true;
    }
    return /^\d{3}-\d{2}-\d{5}$/.test(String(value).trim());
  }

  function bindBizNoInput(id) {
    var el = $(id);
    if (!el) {
      return;
    }
    el.addEventListener('input', function () {
      var formatted = formatBizNo(el.value);
      if (el.value !== formatted) {
        el.value = formatted;
      }
    });
    el.addEventListener('blur', function () {
      el.value = formatBizNo(el.value);
    });
  }

  /** DB 저장 형식: [우편번호] 기본주소 / 상세주소 */
  function formatStoredAddress(postal, base, detail) {
    var b = (base || '').trim();
    var p = digitsOnly(postal).slice(0, 5);
    var d = (detail || '').trim();
    if (!b && !p && !d) {
      return '';
    }
    var line = p ? '[' + p + '] ' + b : b;
    if (d) {
      line += (line ? ' / ' : '') + d;
    }
    return line.trim();
  }

  function parseStoredAddress(value) {
    var raw = (value || '').trim();
    if (!raw) {
      return { postal: '', base: '', detail: '' };
    }
    var bracket = raw.match(/^\[(\d{5})\]\s*(.*)$/);
    if (bracket) {
      var rest = bracket[2];
      var slashIdx = rest.indexOf(' / ');
      if (slashIdx >= 0) {
        return {
          postal: bracket[1],
          base: rest.slice(0, slashIdx).trim(),
          detail: rest.slice(slashIdx + 3).trim()
        };
      }
      return { postal: bracket[1], base: rest.trim(), detail: '' };
    }
    return { postal: '', base: raw, detail: '' };
  }

  function clearAddressFields() {
    var ids = [
      'form-address-search-query',
      'form-address-zip',
      'form-address-base',
      'form-address-detail'
    ];
    ids.forEach(function (id) {
      var el = $(id);
      if (el) {
        el.value = '';
      }
    });
    var results = $('company-address-search-results');
    if (results) {
      results.innerHTML = '';
      results.classList.add('d-none');
    }
    var hint = $('company-address-search-hint');
    if (hint) {
      hint.classList.add('d-none');
      hint.textContent = '';
    }
  }

  function fillAddressFields(postal, base, detail) {
    if ($('form-address-zip')) {
      $('form-address-zip').value = postal || '';
    }
    if ($('form-address-base')) {
      $('form-address-base').value = base || '';
    }
    if ($('form-address-detail')) {
      $('form-address-detail').value = detail || '';
    }
  }

  function applyKakaoAddressItem(item) {
    if (!item || !KakaoAddress) {
      return;
    }
    fillAddressFields(item.postalCode || '', KakaoAddress.buildFormLine1(item), '');
    var detailEl = $('form-address-detail');
    if (detailEl) {
      detailEl.focus();
    }
    notify('주소가 입력되었습니다.', 'success');
  }

  function getModal() {
    var el = $('company-form-modal');
    if (!el || !window.bootstrap) {
      return null;
    }
    return window.bootstrap.Modal.getOrCreateInstance(el);
  }

  function collectSearchParams() {
    return {
      companyNm: ($('search-company-nm') || {}).value || '',
      bizNo: formatBizNo(($('search-biz-no') || {}).value || ''),
      statusCd: ($('search-status-cd') || {}).value || '',
      useYn: ($('search-use-yn') || {}).value || '',
      limit: 200
    };
  }

  function buildQuery(params) {
    var parts = [];
    Object.keys(params).forEach(function (key) {
      var val = params[key];
      if (val !== undefined && val !== null && String(val).length > 0) {
        parts.push(encodeURIComponent(key) + '=' + encodeURIComponent(val));
      }
    });
    return parts.length ? '?' + parts.join('&') : '';
  }

  function escapeHtml(text) {
    if (text == null) {
      return '';
    }
    return String(text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function renderRows(companies) {
    var tbody = $('company-grid-body');
    var countEl = $('company-result-count');
    if (!tbody) {
      return;
    }
    if (countEl) {
      countEl.textContent = (companies ? companies.length : 0) + '건';
    }
    if (!companies || !companies.length) {
      tbody.innerHTML =
        '<tr><td colspan="8" class="text-center text-600 py-4">조회 결과가 없습니다.</td></tr>';
      return;
    }
    tbody.innerHTML = companies
      .map(function (row) {
        return (
          '<tr data-company-id="' +
          escapeHtml(row.companyId) +
          '">' +
          '<td class="ps-3 text-center">' +
          escapeHtml(row.companyId) +
          '</td>' +
          '<td class="fw-semi-bold">' +
          escapeHtml(row.companyNm) +
          '</td>' +
          '<td class="d-none d-md-table-cell">' +
          escapeHtml(row.bizNo || '—') +
          '</td>' +
          '<td class="d-none d-lg-table-cell">' +
          escapeHtml(row.ceoNm || '—') +
          '</td>' +
          '<td class="d-none d-xl-table-cell">' +
          escapeHtml(row.tel || '—') +
          '</td>' +
          '<td class="text-center"><span class="badge badge-soft-primary">' +
          escapeHtml(row.statusCd) +
          '</span></td>' +
          '<td class="text-center">' +
          escapeHtml(row.useYn) +
          '</td>' +
          '<td class="text-end pe-3 text-nowrap">' +
          '<button type="button" class="btn btn-link btn-sm p-0 me-2 company-edit-btn" data-id="' +
          escapeHtml(row.companyId) +
          '">수정</button>' +
          '<button type="button" class="btn btn-link btn-sm p-0 text-danger company-delete-btn" data-id="' +
          escapeHtml(row.companyId) +
          '">삭제</button>' +
          '</td>' +
          '</tr>'
        );
      })
      .join('');
  }

  function loadList() {
    fetch(API_BASE + buildQuery(collectSearchParams()), { credentials: 'same-origin' })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          notify((result.data && result.data.message) || '조회에 실패했습니다.', 'error');
          renderRows([]);
          return;
        }
        renderRows(result.data.companies || []);
      })
      .catch(function () {
        notify('서버 연결에 실패했습니다.', 'error');
      });
  }

  function clearForm() {
    $('form-company-id').value = '';
    $('form-company-nm').value = '';
    $('form-biz-no').value = '';
    $('form-ceo-nm').value = '';
    $('form-tel').value = '';
    $('form-email').value = '';
    clearAddressFields();
    $('form-status-cd').value = 'ACTIVE';
    $('form-use-yn').value = 'Y';
    $('form-memo').value = '';
  }

  function openFormModal(title, company) {
    clearForm();
    $('company-form-modal-label').textContent = title;
    if (company) {
      $('form-company-id').value = company.companyId || '';
      $('form-company-nm').value = company.companyNm || '';
      $('form-biz-no').value = formatBizNo(company.bizNo || '');
      $('form-ceo-nm').value = company.ceoNm || '';
      $('form-tel').value = company.tel || '';
      $('form-email').value = company.email || '';
      var parsed = parseStoredAddress(company.address || '');
      fillAddressFields(parsed.postal, parsed.base, parsed.detail);
      $('form-status-cd').value = company.statusCd || 'ACTIVE';
      $('form-use-yn').value = company.useYn || 'Y';
      $('form-memo').value = company.memo || '';
    }
    var modal = getModal();
    if (modal) {
      modal.show();
    }
  }

  function loadCompanyForEdit(id) {
    fetch(API_BASE + '/' + encodeURIComponent(id), { credentials: 'same-origin' })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (result) {
        if (!result.ok || !result.data.company) {
          notify('업체 정보를 불러오지 못했습니다.', 'error');
          return;
        }
        openFormModal('업체 수정', result.data.company);
      })
      .catch(function () {
        notify('서버 연결에 실패했습니다.', 'error');
      });
  }

  function saveCompany(e) {
    e.preventDefault();
    var bizNo = formatBizNo($('form-biz-no').value);
    $('form-biz-no').value = bizNo;
    if (!isValidBizNo(bizNo)) {
      notify('사업자등록번호는 000-00-00000 형식(10자리)으로 입력해 주세요.', 'warning');
      $('form-biz-no').focus();
      return;
    }
    var payload = {
      companyId: $('form-company-id').value ? parseInt($('form-company-id').value, 10) : null,
      companyNm: $('form-company-nm').value.trim(),
      bizNo: bizNo,
      ceoNm: $('form-ceo-nm').value.trim(),
      tel: $('form-tel').value.trim(),
      email: $('form-email').value.trim(),
      address: formatStoredAddress(
        $('form-address-zip').value,
        $('form-address-base').value,
        $('form-address-detail').value
      ),
      statusCd: $('form-status-cd').value,
      useYn: $('form-use-yn').value,
      memo: $('form-memo').value.trim()
    };
    if (!payload.companyNm) {
      notify('업체명을 입력하세요.', 'warning');
      return;
    }
    fetch(API_BASE, {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          notify((result.data && result.data.message) || '저장에 실패했습니다.', 'error');
          return;
        }
        notify(result.data.message || '저장되었습니다.', 'success');
        var modal = getModal();
        if (modal) {
          modal.hide();
        }
        loadList();
      })
      .catch(function () {
        notify('서버 연결에 실패했습니다.', 'error');
      });
  }

  function deleteCompany(id) {
    var doDelete = function () {
      fetch(API_BASE + '/' + encodeURIComponent(id), {
        method: 'DELETE',
        credentials: 'same-origin'
      })
        .then(function (res) {
          return res.json().then(function (data) {
            return { ok: res.ok, data: data };
          });
        })
        .then(function (result) {
          if (!result.ok) {
            notify((result.data && result.data.message) || '삭제에 실패했습니다.', 'error');
            return;
          }
          notify(result.data.message || '삭제되었습니다.', 'success');
          loadList();
        })
        .catch(function () {
          notify('서버 연결에 실패했습니다.', 'error');
        });
    };
    if (window.Swal) {
      window.Swal.fire({
        title: '업체를 삭제할까요?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '삭제',
        cancelButtonText: '취소'
      }).then(function (r) {
        if (r.isConfirmed) {
          doDelete();
        }
      });
      return;
    }
    if (window.confirm('업체를 삭제할까요?')) {
      doDelete();
    }
  }

  function bindAddressSearch() {
    if (!KakaoAddress || typeof KakaoAddress.bindSearch !== 'function') {
      return;
    }
    KakaoAddress.bindSearch({
      queryId: 'form-address-search-query',
      btnId: 'btn-company-address-search',
      resultsId: 'company-address-search-results',
      hintId: 'company-address-search-hint',
      onSelect: applyKakaoAddressItem,
      onError: function (msg) {
        notify(msg, 'warning');
      }
    });
  }

  function bind() {
    bindBizNoInput('form-biz-no');
    bindBizNoInput('search-biz-no');
    bindAddressSearch();

    var searchForm = $('company-search-form');
    if (searchForm) {
      searchForm.addEventListener('submit', function (e) {
        e.preventDefault();
        loadList();
      });
    }
    var resetBtn = $('btn-reset-company-search');
    if (resetBtn) {
      resetBtn.addEventListener('click', function () {
        $('search-company-nm').value = '';
        $('search-biz-no').value = '';
        $('search-status-cd').value = '';
        $('search-use-yn').value = '';
        loadList();
      });
    }
    var addBtn = $('btn-company-add');
    if (addBtn) {
      addBtn.addEventListener('click', function () {
        openFormModal('업체 등록', null);
      });
    }
    var form = $('company-form');
    if (form) {
      form.addEventListener('submit', saveCompany);
    }
    var tbody = $('company-grid-body');
    if (tbody) {
      tbody.addEventListener('click', function (e) {
        var editBtn = e.target.closest('.company-edit-btn');
        if (editBtn) {
          loadCompanyForEdit(editBtn.getAttribute('data-id'));
          return;
        }
        var delBtn = e.target.closest('.company-delete-btn');
        if (delBtn) {
          deleteCompany(delBtn.getAttribute('data-id'));
        }
      });
    }
    loadList();
  }

  window.CompanyMgmtInit = bind;
})();
