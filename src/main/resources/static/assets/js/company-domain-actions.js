/**
 * 업체별 도메인 등록 — /admin/company-domains
 * API: /api/admin/company-domains
 */
(function () {
  'use strict';

  var API_BASE = '/api/admin/company-domains';
  var COMPANY_API = '/api/admin/companies';
  var domainGridView = null;

  function $(id) {
    return document.getElementById(id);
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

  function verifyBadgeClass(code) {
    if (code === 'VERIFIED') {
      return 'badge-soft-success';
    }
    if (code === 'FAILED') {
      return 'badge-soft-danger';
    }
    return 'badge-soft-warning';
  }

  function formatSslValidityCell(row) {
    if (row.sslYn !== 'Y') {
      return '<span class="text-500">—</span>';
    }
    if (!row.sslCertNotAfter) {
      return '<span class="text-warning">미등록</span>';
    }
    var range =
      escapeHtml(row.sslCertNotBefore || '?') + ' ~ ' + escapeHtml(row.sslCertNotAfter);
    if (row.sslCertExpired) {
      return (
        '<span class="badge badge-soft-danger me-1">만료</span><span class="text-600">' + range + '</span>'
      );
    }
    if (row.sslCertExpiringSoon) {
      return (
        '<span class="badge badge-soft-warning me-1">임박(' +
        escapeHtml(row.sslCertDaysRemaining) +
        '일)</span><span class="text-600">' +
        range +
        '</span>'
      );
    }
    return '<span class="text-800">' + range + '</span>';
  }

  function syncSslCertSection() {
    var sslYn = $('form-ssl-yn');
    var section = $('ssl-cert-section');
    if (!sslYn || !section) {
      return;
    }
    if (sslYn.value === 'Y') {
      section.classList.remove('d-none');
    } else {
      section.classList.add('d-none');
      clearSslCertFields();
    }
  }

  function clearSslCertFields() {
    var file = $('form-ssl-cert-file');
    if (file) {
      file.value = '';
    }
    ['form-ssl-cert-file-id', 'form-ssl-cert-not-before', 'form-ssl-cert-not-after', 'form-ssl-cert-subject', 'form-ssl-cert-issuer'].forEach(
      function (id) {
        var el = $(id);
        if (el) {
          el.value = '';
        }
      }
    );
    var panel = $('ssl-cert-validity-panel');
    if (panel) {
      panel.classList.add('d-none');
    }
  }

  function renderSslValidityPanel(data) {
    var panel = $('ssl-cert-validity-panel');
    if (!panel) {
      return;
    }
    if (!data || !data.sslCertNotAfter) {
      panel.classList.add('d-none');
      return;
    }
    panel.classList.remove('d-none');
    var rangeEl = $('ssl-cert-validity-range');
    var subjectEl = $('ssl-cert-validity-subject');
    var issuerEl = $('ssl-cert-validity-issuer');
    var statusEl = $('ssl-cert-validity-status');
    if (rangeEl) {
      rangeEl.textContent =
        (data.sslCertNotBefore || '—') + ' ~ ' + (data.sslCertNotAfter || '—');
    }
    if (subjectEl) {
      subjectEl.textContent = 'Subject: ' + (data.sslCertSubject || '—');
    }
    if (issuerEl) {
      issuerEl.textContent = 'Issuer: ' + (data.sslCertIssuer || '—');
    }
    if (statusEl) {
      if (data.sslCertExpired) {
        statusEl.innerHTML = '<span class="badge badge-soft-danger">만료됨</span>';
      } else if (data.sslCertExpiringSoon) {
        statusEl.innerHTML =
          '<span class="badge badge-soft-warning">만료 ' +
          escapeHtml(data.sslCertDaysRemaining) +
          '일 전</span>';
      } else if (data.sslCertDaysRemaining != null) {
        statusEl.innerHTML =
          '<span class="badge badge-soft-success">유효 (잔여 ' +
          escapeHtml(data.sslCertDaysRemaining) +
          '일)</span>';
      } else {
        statusEl.innerHTML = '<span class="badge badge-soft-success">유효</span>';
      }
    }
  }

  function applySslCertData(data) {
    if (!data) {
      return;
    }
    if ($('form-ssl-cert-file-id')) {
      $('form-ssl-cert-file-id').value = data.fileId || data.sslCertFileId || '';
    }
    if ($('form-ssl-cert-not-before')) {
      $('form-ssl-cert-not-before').value = data.sslCertNotBefore || '';
    }
    if ($('form-ssl-cert-not-after')) {
      $('form-ssl-cert-not-after').value = data.sslCertNotAfter || '';
    }
    if ($('form-ssl-cert-subject')) {
      $('form-ssl-cert-subject').value = data.sslCertSubject || '';
    }
    if ($('form-ssl-cert-issuer')) {
      $('form-ssl-cert-issuer').value = data.sslCertIssuer || '';
    }
    renderSslValidityPanel(data);
  }

  function uploadSslCert() {
    var fileInput = $('form-ssl-cert-file');
    if (!fileInput || !fileInput.files || !fileInput.files.length) {
      notify('SSL 인증서 파일을 선택하세요.', 'warning');
      return;
    }
    var hostName = $('form-host-name') ? $('form-host-name').value.trim() : '';
    var formData = new FormData();
    formData.append('file', fileInput.files[0]);
    if (hostName) {
      formData.append('hostName', hostName);
    }
    fetch(API_BASE + '/ssl-cert/upload', {
      method: 'POST',
      credentials: 'same-origin',
      body: formData
    })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          notify((result.data && result.data.message) || '인증서 등록에 실패했습니다.', 'error');
          return;
        }
        applySslCertData(result.data);
        notify(result.data.message || '인증서가 등록되었습니다.', 'success');
      })
      .catch(function () {
        notify('서버 연결에 실패했습니다.', 'error');
      });
  }

  function getCompanyId() {
    var select = $('dashboard-company-select');
    if (select && select.value) {
      return parseInt(select.value, 10);
    }
    var panel = $('company-domain-panel');
    if (panel && panel.getAttribute('data-initial-company-id')) {
      return parseInt(panel.getAttribute('data-initial-company-id'), 10);
    }
    return null;
  }

  function syncAddButton() {
    var btn = $('btn-domain-add');
    if (btn) {
      btn.disabled = !getCompanyId();
    }
  }

  function renderCompanyInfo(company) {
    var box = $('company-domain-info');
    if (!box) {
      return;
    }
    if (!company) {
      box.classList.add('d-none');
      box.innerHTML = '';
      return;
    }
    box.classList.remove('d-none');
    box.innerHTML =
      '<strong>' +
      escapeHtml(company.companyNm) +
      '</strong> (ID ' +
      escapeHtml(company.companyId) +
      ')' +
      ' · 상태 <span class="badge badge-soft-primary">' +
      escapeHtml(company.statusCd || '—') +
      '</span>' +
      ' · 사용 ' +
      escapeHtml(company.useYn || '—') +
      (company.bizNo ? ' · 사업자번호 ' + escapeHtml(company.bizNo) : '') +
      (company.ceoNm ? ' · 대표 ' + escapeHtml(company.ceoNm) : '') +
      (company.tel ? ' · ' + escapeHtml(company.tel) : '');
  }

  function loadCompanyInfo(companyId) {
    if (!companyId) {
      renderCompanyInfo(null);
      return Promise.resolve();
    }
    return fetch(COMPANY_API + '/' + companyId, { credentials: 'same-origin' })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (result) {
        if (result.ok && result.data && result.data.company) {
          renderCompanyInfo(result.data.company);
        } else {
          renderCompanyInfo(null);
        }
      })
      .catch(function () {
        renderCompanyInfo(null);
      });
  }

  function ensureCompanySelectOptions() {
    var select = $('dashboard-company-select');
    if (!select || select.options.length > 0) {
      return Promise.resolve();
    }
    return fetch(COMPANY_API + '?useYn=Y&limit=500', { credentials: 'same-origin' })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (result) {
        if (!result.ok || !result.data || !result.data.companies) {
          return;
        }
        select.innerHTML = '';
        result.data.companies.forEach(function (c) {
          var opt = document.createElement('option');
          opt.value = c.companyId;
          var label = c.companyNm || '';
          if (c.statusCd && c.statusCd !== 'ACTIVE') {
            label += ' (' + c.statusCd + ')';
          }
          opt.textContent = label;
          select.appendChild(opt);
        });
        var panel = $('company-domain-panel');
        var initial = panel && panel.getAttribute('data-initial-company-id');
        if (initial) {
          select.value = initial;
        } else if (select.options.length) {
          select.selectedIndex = 0;
        }
        syncAddButton();
      });
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

  function collectSearchParams() {
    var companyId = getCompanyId();
    return {
      companyId: companyId,
      hostName: ($('search-host-name') || {}).value || '',
      useYn: ($('search-use-yn') || {}).value || '',
      limit: 200
    };
  }

  function updateBulkDeleteButton() {
    var btn = $('btn-domain-delete-selected');
    var checkAll = $('domain-check-all');
    if (!btn) {
      return;
    }
    var checked = document.querySelectorAll('.domain-row-check:checked');
    btn.disabled = !checked.length;
    if (checkAll) {
      var all = document.querySelectorAll('.domain-row-check');
      checkAll.checked = all.length > 0 && checked.length === all.length;
      checkAll.indeterminate = checked.length > 0 && checked.length < all.length;
    }
  }

  function getSelectedDomainIds() {
    var ids = [];
    document.querySelectorAll('.domain-row-check:checked').forEach(function (el) {
      var id = parseInt(el.value, 10);
      if (!isNaN(id)) {
        ids.push(id);
      }
    });
    return ids;
  }

  function getDomainGridView() {
    if (!domainGridView && window.PrintMallCommon && window.PrintMallCommon.createGridViewPager) {
      domainGridView = window.PrintMallCommon.createGridViewPager({
        pagerRootId: 'domain-grid-pager',
        containerId: 'domain-grid-body',
        countElId: 'domain-result-count',
        emptyColspan: 9,
        onBeforeRender: function () {
          var checkAll = $('domain-check-all');
          if (checkAll) {
            checkAll.checked = false;
            checkAll.indeterminate = false;
          }
        },
        renderPage: function (domains) {
          var tbody = $('domain-grid-body');
          if (!tbody || !domains.length) {
            return;
          }
          tbody.innerHTML = domains
            .map(function (row) {
              return (
                '<tr data-domain-id="' +
                escapeHtml(row.domainId) +
                '">' +
                '<td class="text-center">' +
                '<input class="form-check-input domain-row-check" type="checkbox" value="' +
                escapeHtml(row.domainId) +
                '" aria-label="도메인 선택" />' +
                '</td>' +
                '<td class="ps-3 text-center">' +
                escapeHtml(row.domainId) +
                '</td>' +
                '<td class="fw-semi-bold">' +
                escapeHtml(row.hostName) +
                (row.primaryYn === 'Y'
                  ? ' <span class="badge badge-soft-primary fs--2">대표</span>'
                  : '') +
                '</td>' +
                '<td class="d-none d-md-table-cell text-600">' +
                '<a href="' +
                escapeHtml(row.accessUrl) +
                '" target="_blank" rel="noopener noreferrer">' +
                escapeHtml(row.accessUrl) +
                '</a></td>' +
                '<td class="text-center">' +
                escapeHtml(row.primaryYn) +
                '</td>' +
                '<td class="text-center d-none d-lg-table-cell">' +
                escapeHtml(row.sslYn) +
                '</td>' +
                '<td class="d-none d-xl-table-cell fs--2">' +
                formatSslValidityCell(row) +
                '</td>' +
                '<td class="text-center"><span class="badge ' +
                verifyBadgeClass(row.verifyStatusCd) +
                '">' +
                escapeHtml(row.verifyStatusCd) +
                '</span></td>' +
                '<td class="text-center">' +
                escapeHtml(row.useYn) +
                '</td>' +
                '<td class="text-end pe-3 text-nowrap">' +
                '<button type="button" class="btn btn-link btn-sm p-0 me-2 domain-edit-btn" data-id="' +
                escapeHtml(row.domainId) +
                '">수정</button>' +
                '<button type="button" class="btn btn-link btn-sm p-0 text-danger domain-delete-btn" data-id="' +
                escapeHtml(row.domainId) +
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
    return domainGridView;
  }

  function renderRows(domains) {
    var view = getDomainGridView();
    if (view) {
      view.setData(domains || []);
    }
  }

  function loadList() {
    var companyId = getCompanyId();
    if (!companyId) {
      notify('업체를 선택하세요.', 'warning');
      renderRows([]);
      return;
    }
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
        renderRows(result.data.domains || []);
      })
      .catch(function () {
        notify('서버 연결에 실패했습니다.', 'error');
      });
  }

  function getModal() {
    var el = $('domain-form-modal');
    if (!el || !window.bootstrap) {
      return null;
    }
    return window.bootstrap.Modal.getOrCreateInstance(el);
  }

  function clearForm() {
    $('form-domain-id').value = '';
    $('form-host-name').value = '';
    $('form-primary-yn').value = 'N';
    $('form-ssl-yn').value = 'Y';
    $('form-verify-status-cd').value = 'PENDING';
    $('form-use-yn').value = 'Y';
    $('form-memo').value = '';
    clearSslCertFields();
    syncSslCertSection();
    $('domain-form-modal-label').textContent = '도메인 등록';
  }

  function openAddModal() {
    var companyId = getCompanyId();
    if (!companyId) {
      notify('업체를 선택하세요.', 'warning');
      return;
    }
    clearForm();
    var modal = getModal();
    if (modal) {
      modal.show();
    }
  }

  function openEditModal(domainId) {
    fetch(API_BASE + '/' + domainId, { credentials: 'same-origin' })
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, data: data };
        });
      })
      .then(function (result) {
        if (!result.ok || !result.data || !result.data.domain) {
          notify('도메인 정보를 불러오지 못했습니다.', 'error');
          return;
        }
        var d = result.data.domain;
        $('form-domain-id').value = d.domainId;
        $('form-host-name').value = d.hostName || '';
        $('form-primary-yn').value = d.primaryYn || 'N';
        $('form-ssl-yn').value = d.sslYn || 'Y';
        $('form-verify-status-cd').value = d.verifyStatusCd || 'PENDING';
        $('form-use-yn').value = d.useYn || 'Y';
        $('form-memo').value = d.memo || '';
        applySslCertData(d);
        syncSslCertSection();
        $('domain-form-modal-label').textContent = '도메인 수정';
        var modal = getModal();
        if (modal) {
          modal.show();
        }
      })
      .catch(function () {
        notify('서버 연결에 실패했습니다.', 'error');
      });
  }

  function saveDomain(e) {
    e.preventDefault();
    var companyId = getCompanyId();
    if (!companyId) {
      notify('업체를 선택하세요.', 'warning');
      return;
    }
    var payload = {
      domainId: $('form-domain-id').value ? parseInt($('form-domain-id').value, 10) : null,
      companyId: companyId,
      hostName: $('form-host-name').value.trim(),
      primaryYn: $('form-primary-yn').value,
      sslYn: $('form-ssl-yn').value,
      verifyStatusCd: $('form-verify-status-cd').value,
      useYn: $('form-use-yn').value,
      memo: $('form-memo').value.trim(),
      sslCertFileId: $('form-ssl-cert-file-id').value
        ? parseInt($('form-ssl-cert-file-id').value, 10)
        : null,
      sslCertNotBefore: $('form-ssl-cert-not-before').value || null,
      sslCertNotAfter: $('form-ssl-cert-not-after').value || null,
      sslCertSubject: $('form-ssl-cert-subject').value || null,
      sslCertIssuer: $('form-ssl-cert-issuer').value || null
    };
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

  function deleteDomain(domainId) {
    if (!window.confirm('이 도메인을 삭제할까요?')) {
      return;
    }
    fetch(API_BASE + '/' + domainId, { method: 'DELETE', credentials: 'same-origin' })
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
  }

  function deleteSelected() {
    var ids = getSelectedDomainIds();
    if (!ids.length) {
      return;
    }
    if (!window.confirm(ids.length + '건의 도메인을 삭제할까요?')) {
      return;
    }
    fetch(API_BASE + '/batch-delete', {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ domainIds: ids })
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
  }

  function bindCompanySelect() {
    var select = $('dashboard-company-select');
    if (!select) {
      return;
    }
    select.addEventListener('change', function () {
      onCompanyChanged();
    });
  }

  function onCompanyChanged() {
    syncAddButton();
    var id = getCompanyId();
    if (!id) {
      renderCompanyInfo(null);
      renderRows([]);
      return;
    }
    var chain = window.DashboardConfigStore
      ? window.DashboardConfigStore.selectCompany(id)
      : Promise.resolve();
    chain
      .then(function () {
        return loadCompanyInfo(id);
      })
      .then(loadList)
      .catch(function () {
        loadCompanyInfo(id).then(loadList);
      });
  }

  function bindEvents() {
    var searchForm = $('domain-search-form');
    if (searchForm) {
      searchForm.addEventListener('submit', function (e) {
        e.preventDefault();
        loadList();
      });
    }
    var resetBtn = $('btn-reset-domain-search');
    if (resetBtn) {
      resetBtn.addEventListener('click', function () {
        $('search-host-name').value = '';
        $('search-use-yn').value = '';
        loadList();
      });
    }
    var addBtn = $('btn-domain-add');
    if (addBtn) {
      addBtn.addEventListener('click', openAddModal);
    }
    var sslYn = $('form-ssl-yn');
    if (sslYn) {
      sslYn.addEventListener('change', syncSslCertSection);
    }
    var sslUploadBtn = $('btn-ssl-cert-upload');
    if (sslUploadBtn) {
      sslUploadBtn.addEventListener('click', uploadSslCert);
    }
    var form = $('domain-form');
    if (form) {
      form.addEventListener('submit', saveDomain);
    }
    var delSel = $('btn-domain-delete-selected');
    if (delSel) {
      delSel.addEventListener('click', deleteSelected);
    }
    var checkAll = $('domain-check-all');
    if (checkAll) {
      checkAll.addEventListener('change', function () {
        document.querySelectorAll('.domain-row-check').forEach(function (el) {
          el.checked = checkAll.checked;
        });
        updateBulkDeleteButton();
      });
    }
    var tbody = $('domain-grid-body');
    if (tbody) {
      tbody.addEventListener('change', function (e) {
        if (e.target && e.target.classList.contains('domain-row-check')) {
          updateBulkDeleteButton();
        }
      });
      tbody.addEventListener('click', function (e) {
        var editBtn = e.target.closest('.domain-edit-btn');
        if (editBtn) {
          openEditModal(editBtn.getAttribute('data-id'));
          return;
        }
        var delBtn = e.target.closest('.domain-delete-btn');
        if (delBtn) {
          deleteDomain(delBtn.getAttribute('data-id'));
        }
      });
    }
  }

  function init() {
    if (!$('company-domain-panel')) {
      return;
    }
    bindEvents();
    bindCompanySelect();
    ensureCompanySelectOptions().then(function () {
      syncAddButton();
      if (getCompanyId()) {
        onCompanyChanged();
      }
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
