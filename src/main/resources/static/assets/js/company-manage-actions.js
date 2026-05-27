/**
 * 업체 관리 — /admin/companies
 * API: /api/admin/companies
 * @module company-manage-actions
 */
(function () {
  'use strict';

  var API_BASE = '/api/admin/companies';
  var companyAddressForm = null;
  var companyGridView = null;

  function getAddressForm() {
    if (!companyAddressForm && window.PrintMallKakaoAddressForm) {
      companyAddressForm = window.PrintMallKakaoAddressForm.create({
        idPrefix: 'company',
        modalId: 'company-form-modal',
        onNotify: notify
      });
    }
    return companyAddressForm;
  }

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

  function updateBulkDeleteButton() {
    var btn = $('btn-company-delete-selected');
    var checkAll = $('company-check-all');
    if (!btn) {
      return;
    }
    var checked = document.querySelectorAll('.company-row-check:checked');
    btn.disabled = !checked.length;
    if (checkAll) {
      var all = document.querySelectorAll('.company-row-check');
      checkAll.checked = all.length > 0 && checked.length === all.length;
      checkAll.indeterminate = checked.length > 0 && checked.length < all.length;
    }
  }

  function getSelectedCompanyIds() {
    var ids = [];
    document.querySelectorAll('.company-row-check:checked').forEach(function (el) {
      var id = parseInt(el.value, 10);
      if (!isNaN(id)) {
        ids.push(id);
      }
    });
    return ids;
  }

  function getCompanyGridView() {
    if (!companyGridView && window.PrintMallCommon && window.PrintMallCommon.createGridViewPager) {
      companyGridView = window.PrintMallCommon.createGridViewPager({
        pagerRootId: 'company-grid-pager',
        containerId: 'company-grid-body',
        countElId: 'company-result-count',
        emptyColspan: 9,
        onBeforeRender: function () {
          var checkAll = $('company-check-all');
          if (checkAll) {
            checkAll.checked = false;
            checkAll.indeterminate = false;
          }
        },
        renderPage: function (companies) {
          var tbody = $('company-grid-body');
          if (!tbody || !companies.length) {
            return;
          }
          tbody.innerHTML = companies
            .map(function (row) {
              return (
                '<tr data-company-id="' +
                escapeHtml(row.companyId) +
                '">' +
                '<td class="text-center">' +
                '<input class="form-check-input company-row-check" type="checkbox" value="' +
                escapeHtml(row.companyId) +
                '" aria-label="업체 ' +
                escapeHtml(row.companyNm) +
                ' 선택" />' +
                '</td>' +
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
          updateBulkDeleteButton();
        }
      });
    }
    return companyGridView;
  }

  function renderRows(companies) {
    var view = getCompanyGridView();
    if (view) {
      view.setData(companies || []);
    }
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
    var af = getAddressForm();
    if (af) {
      af.clear();
    }
    $('form-status-cd').value = 'ACTIVE';
    $('form-use-yn').value = 'Y';
    $('form-memo').value = '';
    var hostRow = $('company-form-primary-host-row');
    var hostInput = $('form-primary-host');
    if (hostInput) {
      hostInput.value = '';
    }
    if (hostRow) {
      hostRow.style.display = '';
    }
  }

  function openFormModal(title, company) {
    clearForm();
    $('company-form-modal-label').textContent = title;
    var hostRow = $('company-form-primary-host-row');
    if (company) {
      if (hostRow) {
        hostRow.style.display = 'none';
      }
      $('form-company-id').value = company.companyId || '';
      $('form-company-nm').value = company.companyNm || '';
      $('form-biz-no').value = formatBizNo(company.bizNo || '');
      $('form-ceo-nm').value = company.ceoNm || '';
      $('form-tel').value = company.tel || '';
      $('form-email').value = company.email || '';
      var af = getAddressForm();
      if (af) {
        af.fillFromStored(company.address || '');
      }
      $('form-status-cd').value = company.statusCd || 'ACTIVE';
      $('form-use-yn').value = company.useYn || 'Y';
      $('form-memo').value = company.memo || '';
    } else if (hostRow) {
      hostRow.style.display = '';
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
      address: getAddressForm() ? getAddressForm().getStored() : '',
      statusCd: $('form-status-cd').value,
      useYn: $('form-use-yn').value,
      memo: $('form-memo').value.trim()
    };
    if (!payload.companyId) {
      var primaryHost = ($('form-primary-host') || {}).value;
      if (primaryHost && primaryHost.trim()) {
        payload.primaryHostName = primaryHost.trim();
      }
    }
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

  function confirmDelete(title, onConfirm) {
    if (window.Swal) {
      window.Swal.fire({
        title: title,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        confirmButtonColor: '#e63757'
      }).then(function (r) {
        if (r.isConfirmed) {
          onConfirm();
        }
      });
      return;
    }
    if (window.confirm(title)) {
      onConfirm();
    }
  }

  function deleteCompany(id) {
    confirmDelete('업체를 삭제할까요?', function () {
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
    });
  }

  function deleteSelectedCompanies() {
    var ids = getSelectedCompanyIds();
    if (!ids.length) {
      notify('삭제할 업체를 선택하세요.', 'warning');
      return;
    }
    confirmDelete('선택한 ' + ids.length + '건의 업체를 삭제할까요?', function () {
      fetch(API_BASE + '/batch-delete', {
        method: 'POST',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ companyIds: ids })
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
    });
  }

  function bind() {
    bindBizNoInput('form-biz-no');
    bindBizNoInput('search-biz-no');
    var af = getAddressForm();
    if (af) {
      af.bind();
    }

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
    var checkAll = $('company-check-all');
    if (checkAll) {
      checkAll.addEventListener('change', function () {
        var checked = checkAll.checked;
        document.querySelectorAll('.company-row-check').forEach(function (el) {
          el.checked = checked;
        });
        updateBulkDeleteButton();
      });
    }
    var bulkDeleteBtn = $('btn-company-delete-selected');
    if (bulkDeleteBtn) {
      bulkDeleteBtn.addEventListener('click', deleteSelectedCompanies);
    }
    var tbody = $('company-grid-body');
    if (tbody) {
      tbody.addEventListener('change', function (e) {
        if (e.target.classList.contains('company-row-check')) {
          updateBulkDeleteButton();
        }
      });
      tbody.addEventListener('click', function (e) {
        if (e.target.classList.contains('company-row-check')) {
          return;
        }
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

  if (document.getElementById('company-management-panel')) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', bind);
    } else {
      bind();
    }
  }
})();
