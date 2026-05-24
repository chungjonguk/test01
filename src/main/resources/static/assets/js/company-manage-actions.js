/**
 * 업체 관리 — /admin/companies
 * API: /api/admin/companies
 * @module company-manage-actions
 */
(function () {
  'use strict';

  var API_BASE = '/api/admin/companies';

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
      bizNo: ($('search-biz-no') || {}).value || '',
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
      .map(function (row, idx) {
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

  /**
   * 검색 조건으로 업체 목록을 API에서 조회합니다.
   * @returns {void}
   */
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
    $('form-address').value = '';
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
      $('form-biz-no').value = company.bizNo || '';
      $('form-ceo-nm').value = company.ceoNm || '';
      $('form-tel').value = company.tel || '';
      $('form-email').value = company.email || '';
      $('form-address').value = company.address || '';
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

  /**
   * 모달 폼 데이터로 업체를 등록·수정합니다.
   * @param {Event} e 폼 submit 이벤트
   * @returns {void}
   */
  function saveCompany(e) {
    e.preventDefault();
    var payload = {
      companyId: $('form-company-id').value ? parseInt($('form-company-id').value, 10) : null,
      companyNm: $('form-company-nm').value.trim(),
      bizNo: $('form-biz-no').value.trim(),
      ceoNm: $('form-ceo-nm').value.trim(),
      tel: $('form-tel').value.trim(),
      email: $('form-email').value.trim(),
      address: $('form-address').value.trim(),
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

  /**
   * 업체 ID로 삭제 API를 호출합니다.
   * @param {string|number} id 업체 ID
   * @returns {void}
   */
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

  function bind() {
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

  /**
   * 업체 관리 화면 초기화 — 검색·CRUD 이벤트 바인딩 및 목록 조회.
   * @returns {void}
   */
  window.CompanyMgmtInit = bind;
})();
