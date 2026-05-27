/**
 * 코드 관리 — /admin/codes
 * 상단 조회그리드 + 코드그룹/상세코드 그리드
 * API: /api/admin/codes
 * @module code-management-actions
 * @version 29
 */
(function () {
  'use strict';

  var COL_SEARCH = 8;
  var COL_GROUP = 10;
  var COL_DETAIL = 9;

  var searchPager = null;
  var groupPager = null;
  var detailPager = null;

  var C = window.PrintMallCommon || {};
  var formatDt = C.formatDt || function (v) {
    return v == null ? '—' : String(v);
  };

  var state = {
    groups: {},
    filteredKeys: null,
    selectedGroup: null,
    selectedCode: null,
    checkedGroups: [],
    checkedCodes: [],
    searched: false,
    loading: false,
    saving: false,
    editGroupKey: null,
    editDetailCodeVal: null
  };

  function handleUnauthorized(res) {
    return C.goLoginIfUnauthorized && C.goLoginIfUnauthorized(res.status);
  }

  function escapeAttr(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/"/g, '&quot;')
      .replace(/</g, '&lt;');
  }

  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/"/g, '&quot;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  function truncateCell(text) {
    var value = text == null ? '' : String(text);
    if (!value) {
      value = '—';
    }
    return (
      '<span class="code-mgmt-truncate" title="' +
      escapeAttr(value) +
      '">' +
      escapeHtml(value) +
      '</span>'
    );
  }

  function codeIdCell(codeId, key, btnClass, colClass) {
    var id = codeId || key || '';
    return (
      '<td class="align-middle py-2 ' +
      colClass +
      '">' +
      '<div class="code-mgmt-code-cell">' +
      '<strong class="code-mgmt-truncate" title="' +
      escapeAttr(id) +
      '">' +
      escapeHtml(id) +
      '</strong>' +
      editBtnHtml(btnClass, 'data-group', key, '코드그룹 수정') +
      '</div></td>'
    );
  }

  function codeValCell(codeVal) {
    return (
      '<td class="align-middle py-2 col-code-val">' +
      '<div class="code-mgmt-code-cell">' +
      '<code class="code-mgmt-truncate" title="' +
      escapeAttr(codeVal) +
      '">' +
      escapeHtml(codeVal) +
      '</code>' +
      editBtnHtml('code-detail-edit-btn', 'data-code-val', codeVal, '상세코드 수정') +
      '</div></td>'
    );
  }

  function noCell(index) {
    return (
      '<td class="ps-3 align-middle text-center py-2 col-no">' +
      (index + 1) +
      '</td>'
    );
  }

  function groupCheckCell(key) {
    var checked = state.checkedGroups.indexOf(key) !== -1;
    return (
      '<td class="align-middle py-2 code-mgmt-check-cell">' +
      '<div class="form-check fs-0 mb-0 d-flex align-items-center justify-content-center">' +
      '<input class="form-check-input code-group-check" type="checkbox" value="' +
      escapeAttr(key) +
      '"' +
      (checked ? ' checked' : '') +
      ' aria-label="' +
      escapeAttr(key) +
      ' 선택"></div></td>'
    );
  }

  function detailCheckCell(code) {
    var checked = state.checkedCodes.indexOf(code) !== -1;
    return (
      '<td class="align-middle py-2 code-mgmt-check-cell">' +
      '<div class="form-check fs-0 mb-0 d-flex align-items-center justify-content-center">' +
      '<input class="form-check-input code-detail-check" type="checkbox" value="' +
      escapeAttr(code) +
      '"' +
      (checked ? ' checked' : '') +
      ' aria-label="' +
      escapeAttr(code) +
      ' 선택"></div></td>'
    );
  }

  function pruneCheckedGroups() {
    var keys = getFilteredKeys();
    state.checkedGroups = state.checkedGroups.filter(function (key) {
      return keys.indexOf(key) !== -1 && state.groups[key];
    });
  }

  function pruneCheckedCodes() {
    if (!state.selectedGroup || !state.groups[state.selectedGroup]) {
      state.checkedCodes = [];
      return;
    }
    var codes = state.groups[state.selectedGroup].codes || [];
    var valid = codes.map(function (c) { return c.codeVal; });
    state.checkedCodes = state.checkedCodes.filter(function (code) {
      return valid.indexOf(code) !== -1;
    });
  }

  function updateGroupCheckAll() {
    var master = document.getElementById('code-group-check-all');
    if (!master) {
      return;
    }
    var keys = getFilteredKeys();
    if (!keys.length) {
      master.checked = false;
      master.indeterminate = false;
      master.disabled = true;
      return;
    }
    master.disabled = false;
    var checkedCount = keys.filter(function (key) {
      return state.checkedGroups.indexOf(key) !== -1;
    }).length;
    master.checked = checkedCount === keys.length;
    master.indeterminate = checkedCount > 0 && checkedCount < keys.length;
  }

  function updateDetailCheckAll() {
    var master = document.getElementById('code-detail-check-all');
    if (!master) {
      return;
    }
    var groupKey = state.selectedGroup;
    if (!groupKey || !state.groups[groupKey]) {
      master.checked = false;
      master.indeterminate = false;
      master.disabled = true;
      return;
    }
    var codes = state.groups[groupKey].codes || [];
    if (!codes.length) {
      master.checked = false;
      master.indeterminate = false;
      master.disabled = true;
      return;
    }
    master.disabled = false;
    var checkedCount = codes.filter(function (c) {
      return state.checkedCodes.indexOf(c.codeVal) !== -1;
    }).length;
    master.checked = checkedCount === codes.length;
    master.indeterminate = checkedCount > 0 && checkedCount < codes.length;
  }

  function setGroupChecked(key, checked) {
    if (!key) {
      return;
    }
    var idx = state.checkedGroups.indexOf(key);
    if (checked && idx === -1) {
      state.checkedGroups.push(key);
    } else if (!checked && idx !== -1) {
      state.checkedGroups.splice(idx, 1);
    }
  }

  function setCodeChecked(code, checked) {
    if (!code) {
      return;
    }
    var idx = state.checkedCodes.indexOf(code);
    if (checked && idx === -1) {
      state.checkedCodes.push(code);
    } else if (!checked && idx !== -1) {
      state.checkedCodes.splice(idx, 1);
    }
  }

  function bindGroupCheckboxes() {
    var master = document.getElementById('code-group-check-all');
    if (master && !master._codeMgmtBound) {
      master._codeMgmtBound = true;
      master.addEventListener('change', function () {
        var keys = getFilteredKeys();
        if (master.checked) {
          state.checkedGroups = keys.slice();
        } else {
          state.checkedGroups = [];
        }
        renderGroups();
      });
    }
    document.querySelectorAll('#code-group-grid-body .code-group-check').forEach(function (cb) {
      cb.addEventListener('click', function (e) {
        e.stopPropagation();
      });
      cb.addEventListener('change', function () {
        setGroupChecked(cb.value, cb.checked);
        if (cb.checked) {
          selectGroup(cb.value);
        }
        updateGroupCheckAll();
      });
    });
    updateGroupCheckAll();
  }

  function bindDetailCheckboxes() {
    var master = document.getElementById('code-detail-check-all');
    if (master && !master._codeMgmtBound) {
      master._codeMgmtBound = true;
      master.addEventListener('change', function () {
        var groupKey = state.selectedGroup;
        if (!groupKey || !state.groups[groupKey]) {
          return;
        }
        var codes = state.groups[groupKey].codes || [];
        if (master.checked) {
          state.checkedCodes = codes.map(function (c) { return c.codeVal; });
        } else {
          state.checkedCodes = [];
        }
        renderDetails();
      });
    }
    document.querySelectorAll('#code-detail-grid-body .code-detail-check').forEach(function (cb) {
      cb.addEventListener('click', function (e) {
        e.stopPropagation();
      });
      cb.addEventListener('change', function () {
        setCodeChecked(cb.value, cb.checked);
        if (cb.checked) {
          selectCode(cb.value);
        }
        updateDetailCheckAll();
      });
    });
    updateDetailCheckAll();
  }

  function useYnCell(useYn) {
    var yn = useYn || 'Y';
    var ynClass = yn === 'Y' ? 'badge-soft-success' : 'badge-soft-warning';
    return (
      '<td class="align-middle text-center py-2 col-use"><span class="badge ' +
      ynClass +
      '">' +
      yn +
      '</span></td>'
    );
  }

  function auditCells(item) {
    item = item || {};
    return (
      '<td class="align-middle py-2 col-reg-id d-none d-xl-table-cell">' +
      truncateCell(item.regId) +
      '</td>' +
      '<td class="align-middle py-2 text-600 col-reg-dt d-none d-xl-table-cell">' +
      truncateCell(formatDt(item.regdateDt)) +
      '</td>' +
      '<td class="align-middle py-2 col-upd-id d-none d-xl-table-cell">' +
      truncateCell(item.updateId) +
      '</td>' +
      '<td class="align-middle py-2 text-600 col-upd-dt d-none d-xl-table-cell">' +
      truncateCell(formatDt(item.updateDt)) +
      '</td>'
    );
  }

  function notify(message, icon) {
    if (window.Swal) {
      window.Swal.fire({
        toast: true,
        position: 'top-end',
        icon: icon || 'info',
        title: message,
        showConfirmButton: false,
        timer: 2600,
        timerProgressBar: true
      });
      return;
    }
    window.alert(message);
  }

  function getSearchCriteria() {
    var codeIdEl = document.getElementById('search-code-id');
    var codeNmEl = document.getElementById('search-code-nm');
    var useEl = document.getElementById('search-group-use');
    return {
      codeId: codeIdEl ? codeIdEl.value.trim().toUpperCase() : '',
      codeNm: codeNmEl ? codeNmEl.value.trim() : '',
      useYn: useEl ? useEl.value.trim() : ''
    };
  }

  function matchGroup(key, criteria) {
    var g = state.groups[key];
    if (!g) {
      return false;
    }
    if (criteria.codeId && key.indexOf(criteria.codeId) === -1) {
      return false;
    }
    if (criteria.codeNm && (g.codeNm || '').indexOf(criteria.codeNm) === -1) {
      return false;
    }
    if (criteria.useYn && (g.useYn || 'Y') !== criteria.useYn) {
      return false;
    }
    return true;
  }

  function getFilteredKeys() {
    if (state.filteredKeys) {
      return state.filteredKeys.slice().sort();
    }
    return Object.keys(state.groups).sort();
  }

  function applyGroupsFromApi(groups) {
    var next = {};
    (groups || []).forEach(function (g) {
      var key = g.codeId;
      if (!key) {
        return;
      }
      next[key] = {
        codeId: key,
        codeNm: g.codeNm || key,
        useYn: g.useYn || 'Y',
        detailCount: g.detailCount != null ? g.detailCount : (g.codes || []).length,
        regId: g.regId || '',
        regdateDt: g.regdateDt || null,
        updateId: g.updateId || '',
        updateDt: g.updateDt || null,
        codes: (g.codes || []).map(function (c) {
          var codeVal = c.codeVal || c.code || '';
          return {
            codeId: c.codeId || key,
            codeVal: codeVal,
            sort: c.sort || 0,
            useYn: c.useYn || 'Y',
            regId: c.regId || '',
            regdateDt: c.regdateDt || null,
            updateId: c.updateId || '',
            updateDt: c.updateDt || null
          };
        })
      };
    });
    state.groups = next;
    state.filteredKeys = Object.keys(next).sort();
    state.checkedGroups = [];
    state.checkedCodes = [];
  }

  function finishSearch(notifySuccess) {
    if (state.filteredKeys.length) {
      if (
        !state.selectedGroup ||
        state.filteredKeys.indexOf(state.selectedGroup) === -1
      ) {
        state.selectedGroup = state.filteredKeys[0];
      }
    } else {
      state.selectedGroup = null;
      state.selectedCode = null;
    }
    renderSearchGrid();
    renderGroups();
    renderDetails();
    if (notifySuccess) {
      notify(state.filteredKeys.length + '건의 코드를 조회했습니다.', 'success');
    }
  }

  function parseApiResponse(res) {
    if (handleUnauthorized(res)) {
      return new Promise(function () {});
    }
    if (!res.ok) {
      return res
        .json()
        .catch(function () {
          return { message: '요청 처리 중 오류가 발생했습니다. (' + res.status + ')' };
        })
        .then(function (body) {
          var msg =
            (body && (body.message || body.error || body.detail)) ||
            '요청 처리 중 오류가 발생했습니다. (' + res.status + ')';
          throw new Error(msg);
        });
    }
    return res.json();
  }

  function deleteGroupsApi(codeIds) {
    var params = new URLSearchParams();
    codeIds.forEach(function (id) {
      params.append('codeIds', id);
    });
    return fetch('/api/admin/codes/groups?' + params.toString(), {
      method: 'DELETE',
      headers: { Accept: 'application/json' },
      credentials: 'same-origin'
    }).then(parseApiResponse);
  }

  function deleteCodeValuesApi(codeId, codeVals) {
    var params = new URLSearchParams();
    codeVals.forEach(function (val) {
      params.append('codeVals', val);
    });
    return fetch(
      '/api/admin/codes/' + encodeURIComponent(codeId) + '/values?' + params.toString(),
      {
        method: 'DELETE',
        headers: { Accept: 'application/json' },
        credentials: 'same-origin'
      }
    ).then(parseApiResponse);
  }

  /**
   * 검색 조건으로 코드그룹 목록을 API에서 조회합니다.
   * @returns {void}
   */
  function searchGroups() {
    if (state.loading) {
      return;
    }
    var criteria = getSearchCriteria();
    var params = new URLSearchParams();
    if (criteria.codeId) {
      params.set('codeId', criteria.codeId);
    }
    if (criteria.codeNm) {
      params.set('codeNm', criteria.codeNm);
    }
    if (criteria.useYn) {
      params.set('useYn', criteria.useYn);
    }

    state.loading = true;
    state.searched = true;
    fetch('/api/admin/codes?' + params.toString(), {
      method: 'GET',
      headers: { Accept: 'application/json' },
      credentials: 'same-origin'
    })
      .then(function (res) {
        if (handleUnauthorized(res)) {
          return;
        }
        if (!res.ok) {
          throw new Error('조회 API 오류 (' + res.status + ')');
        }
        return res.json();
      })
      .then(function (data) {
        if (!data) {
          return;
        }
        applyGroupsFromApi(data.groups);
        finishSearch(true);
      })
      .catch(function (err) {
        state.groups = {};
        state.filteredKeys = [];
        state.selectedGroup = null;
        state.selectedCode = null;
        state.checkedGroups = [];
        state.checkedCodes = [];
        renderSearchGrid();
        renderGroups();
        renderDetails();
        notify(
          err && err.message ? err.message : '코드 조회 중 오류가 발생했습니다.',
          'error'
        );
      })
      .finally(function () {
        state.loading = false;
      });
  }

  function resetSearch() {
    var form = document.getElementById('code-group-search-form');
    if (form) {
      form.reset();
    }
    state.groups = {};
    state.filteredKeys = null;
    state.searched = false;
    state.selectedGroup = null;
    state.selectedCode = null;
    state.checkedGroups = [];
    state.checkedCodes = [];
    renderSearchGrid();
    renderGroups();
    renderDetails();
    notify('조회 조건을 초기화했습니다.', 'info');
  }

  function selectGroup(key) {
    if (!key || !state.groups[key]) {
      return;
    }
    state.selectedGroup = key;
    state.selectedCode = null;
    state.checkedCodes = [];
    renderSearchGrid();
    renderGroups();
    renderDetails();
  }

  function selectCode(codeVal) {
    if (!codeVal || !state.selectedGroup) {
      return;
    }
    var g = state.groups[state.selectedGroup];
    if (!g || !g.codes || !g.codes.some(function (c) { return c.codeVal === codeVal; })) {
      return;
    }
    state.selectedCode = codeVal;
    renderDetails();
  }

  function appendPromptField(container, f) {
    var fieldId = 'swal-' + f.id;
    var wrap = document.createElement('div');
    wrap.className = 'mb-2 text-start code-mgmt-prompt-field';

    var label = document.createElement('label');
    label.className = 'form-label mb-1';
    label.setAttribute('for', fieldId);
    label.textContent = f.label;
    wrap.appendChild(label);

    var control;
    if (f.type === 'select' && (f.codeGroup || (f.options && f.options.length))) {
      control = document.createElement('select');
      control.className = 'form-select form-select-sm code-mgmt-prompt-select';
      control.id = fieldId;
      var selectedVal = f.value != null ? String(f.value) : '';
      if (f.codeGroup && window.PrintMallCommon && window.PrintMallCommon.fillCodeSelect) {
        control.setAttribute('data-code-group', f.codeGroup);
        window.PrintMallCommon.fillCodeSelect(control, f.codeGroup, selectedVal);
      } else {
        f.options.forEach(function (opt) {
          var option = document.createElement('option');
          var val = opt.value != null ? String(opt.value) : '';
          option.value = val;
          option.textContent = opt.label != null ? String(opt.label) : val;
          if (val === selectedVal) {
            option.selected = true;
          }
          control.appendChild(option);
        });
      }
    } else {
      control = document.createElement('input');
      control.className = 'form-control form-control-sm';
      control.id = fieldId;
      control.type = 'text';
      if (f.placeholder) {
        control.placeholder = f.placeholder;
      }
      if (f.value) {
        control.value = f.value;
      }
    }
    wrap.appendChild(control);
    container.appendChild(wrap);
  }

  function mountPromptFields(fields) {
    var popup = window.Swal && window.Swal.getPopup ? window.Swal.getPopup() : null;
    var container = popup
      ? popup.querySelector('#code-mgmt-prompt-fields')
      : document.getElementById('code-mgmt-prompt-fields');
    if (!container) {
      return;
    }
    container.innerHTML = '';
    fields.forEach(function (f) {
      appendPromptField(container, f);
    });
  }

  function readPromptFieldValues(fields) {
    var result = {};
    fields.forEach(function (f) {
      var el = document.getElementById('swal-' + f.id);
      result[f.id] = el ? String(el.value).trim() : '';
    });
    return result;
  }

  function promptInput(title, fields) {
    if (!window.Swal) {
      return new Promise(function (resolve) {
        var result = {};
        for (var i = 0; i < fields.length; i++) {
          var f = fields[i];
          var val;
          if (f.type === 'select' && f.options && f.options.length) {
            var labels = f.options.map(function (o) {
              return o.label + '(' + o.value + ')';
            });
            val = window.prompt(
              title + ' — ' + f.label + ' [' + labels.join(', ') + ']',
              f.value || f.options[0].value || ''
            );
          } else {
            val = window.prompt(title + ' — ' + f.label, f.value || '');
          }
          if (val === null) {
            resolve(null);
            return;
          }
          result[f.id] = val.trim();
        }
        resolve(result);
      });
    }
    return window.Swal.fire({
      title: title,
      html: '<div id="code-mgmt-prompt-fields" class="code-mgmt-prompt-fields"></div>',
      customClass: {
        htmlContainer: 'code-mgmt-swal-html'
      },
      showCancelButton: true,
      confirmButtonText: '확인',
      cancelButtonText: '취소',
      focusConfirm: false,
      didOpen: function () {
        mountPromptFields(fields);
      },
      preConfirm: function () {
        return readPromptFieldValues(fields);
      }
    }).then(function (res) {
      return res.isConfirmed ? res.value : null;
    });
  }

  function ensureSearchPager() {
    if (searchPager || !(window.PrintMallCommon && window.PrintMallCommon.createGridPager)) {
      return;
    }
    searchPager = window.PrintMallCommon.createGridPager({
      rootId: 'code-group-search-grid-pager',
      onPageChange: function (pageKeys, meta) {
        paintSearchGridRows(pageKeys, meta);
      }
    });
  }

  function paintSearchGridRows(pageKeys, meta) {
    var tbody = document.getElementById('code-group-search-grid-body');
    if (!tbody) {
      return;
    }
    var rowOffset = meta && meta.start ? meta.start - 1 : 0;
    if (!pageKeys.length) {
      tbody.innerHTML =
        '<tr><td colspan="' +
        COL_SEARCH +
        '" class="text-center text-600 py-4">조회 결과가 없습니다.</td></tr>';
      return;
    }
    tbody.innerHTML = pageKeys
      .map(function (key, index) {
        var g = state.groups[key];
        var active = key === state.selectedGroup ? ' table-active' : '';
        return (
          '<tr class="code-group-search-row' +
          active +
          '" data-group="' +
          key +
          '" role="button" tabindex="0">' +
          noCell(rowOffset + index) +
          codeIdCell(g.codeId, key, 'code-group-edit-btn', 'col-code') +
          '<td class="align-middle py-2 col-code-nm">' +
          truncateCell(g.codeNm) +
          '</td>' +
          useYnCell(g.useYn) +
          auditCells(g) +
          '</tr>'
        );
      })
      .join('');
    tbody.querySelectorAll('.code-group-search-row').forEach(function (row) {
      var key = row.getAttribute('data-group');
      function onSelect() {
        selectGroup(key);
      }
      row.addEventListener('click', onSelect);
      row.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onSelect();
        }
      });
    });
  }

  function renderSearchGrid() {
    var tbody = document.getElementById('code-group-search-grid-body');
    var countEl = document.getElementById('code-search-result-count');
    if (!tbody) {
      return;
    }
    if (!state.searched) {
      if (searchPager) {
        searchPager.setItems([]);
      }
      tbody.innerHTML =
        '<tr><td colspan="' +
        COL_SEARCH +
        '" class="text-center text-600 py-4">조회 버튼을 눌러 그룹 정보를 불러오세요.</td></tr>';
      if (countEl) {
        countEl.textContent = '—';
      }
      return;
    }
    var keys = state.filteredKeys || [];
    if (countEl) {
      countEl.textContent = keys.length + '건';
    }
    if (!keys.length) {
      if (searchPager) {
        searchPager.setItems([]);
      }
      tbody.innerHTML =
        '<tr><td colspan="' +
        COL_SEARCH +
        '" class="text-center text-600 py-4">조회 결과가 없습니다.</td></tr>';
      return;
    }
    ensureSearchPager();
    if (searchPager) {
      searchPager.setItems(keys);
      return;
    }
    paintSearchGridRows(keys, { start: 1, totalItems: keys.length });
  }

  function ensureGroupPager() {
    if (groupPager || !(window.PrintMallCommon && window.PrintMallCommon.createGridPager)) {
      return;
    }
    groupPager = window.PrintMallCommon.createGridPager({
      rootId: 'code-group-grid-pager',
      onPageChange: function (pageKeys, meta) {
        paintGroupRows(pageKeys, meta);
      }
    });
  }

  function paintGroupRows(pageKeys, meta) {
    var tbody = document.getElementById('code-group-grid-body');
    if (!tbody) {
      return;
    }
    var rowOffset = meta && meta.start ? meta.start - 1 : 0;
    tbody.innerHTML = pageKeys
      .map(function (key, index) {
        var g = state.groups[key];
        var active = key === state.selectedGroup ? ' table-active' : '';
        var count = g.detailCount != null ? g.detailCount : g.codes ? g.codes.length : 0;
        return (
          '<tr class="code-group-row' +
          active +
          '" data-group="' +
          key +
          '" role="button" tabindex="0">' +
          groupCheckCell(key) +
          noCell(rowOffset + index) +
          codeIdCell(g.codeId, key, 'code-group-edit-btn', 'col-code-id') +
          '<td class="align-middle py-2 col-code-nm">' +
          truncateCell(g.codeNm) +
          '</td>' +
          useYnCell(g.useYn) +
          '<td class="align-middle text-center py-2 col-count"><span class="badge badge-soft-secondary">' +
          count +
          '</span></td>' +
          auditCells(g) +
          '</tr>'
        );
      })
      .join('');
    tbody.querySelectorAll('.code-group-row').forEach(function (row) {
      var key = row.getAttribute('data-group');
      function onSelect() {
        setGroupChecked(key, true);
        selectGroup(key);
      }
      row.addEventListener('click', function (e) {
        if (e.target.closest('.code-mgmt-check-cell')) {
          return;
        }
        onSelect();
      });
      row.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onSelect();
        }
      });
    });
    bindGroupCheckboxes();
    renderDetails();
  }

  function renderGroups() {
    var tbody = document.getElementById('code-group-grid-body');
    if (!tbody) {
      return;
    }
    var keys = getFilteredKeys();
    if (!keys.length) {
      if (groupPager) {
        groupPager.setItems([]);
      }
      tbody.innerHTML =
        '<tr><td colspan="' +
        COL_GROUP +
        '" class="text-center text-600 py-4">' +
        (state.searched ? '조회된 코드그룹이 없습니다.' : '등록된 코드그룹이 없습니다.') +
        '</td></tr>';
      if (!state.searched) {
        state.selectedGroup = null;
      }
      state.checkedGroups = [];
      updateGroupCheckAll();
      renderDetails();
      return;
    }
    pruneCheckedGroups();
    if (!state.selectedGroup || !state.groups[state.selectedGroup]) {
      state.selectedGroup = keys[0];
    }
    ensureGroupPager();
    if (groupPager) {
      groupPager.setItems(keys);
      return;
    }
    paintGroupRows(keys, { start: 1, totalItems: keys.length });
  }

  function ensureDetailPager() {
    if (detailPager || !(window.PrintMallCommon && window.PrintMallCommon.createGridPager)) {
      return;
    }
    detailPager = window.PrintMallCommon.createGridPager({
      rootId: 'code-detail-grid-pager',
      onPageChange: function (pageCodes, meta) {
        paintDetailRows(pageCodes, meta);
      }
    });
  }

  function paintDetailRows(pageCodes, meta) {
    var tbody = document.getElementById('code-detail-grid-body');
    var empty = document.getElementById('code-detail-empty');
    if (!tbody) {
      return;
    }
    var rowOffset = meta && meta.start ? meta.start - 1 : 0;
    if (!pageCodes.length) {
      tbody.innerHTML =
        '<tr><td colspan="' +
        COL_DETAIL +
        '" class="text-center text-600 py-4">상세코드가 없습니다. 상단 <strong>추가</strong>로 등록하세요.</td></tr>';
      updateDetailCheckAll();
      return;
    }
    if (empty) {
      empty.classList.add('d-none');
    }
    tbody.innerHTML = pageCodes
      .map(function (item, idx) {
        var active = state.selectedCode === item.codeVal ? ' table-active' : '';
        var globalIdx = rowOffset + idx;
        return (
          '<tr class="code-detail-row' +
          active +
          '" data-code-val="' +
          item.codeVal +
          '" data-index="' +
          globalIdx +
          '" role="button" tabindex="0">' +
          detailCheckCell(item.codeVal) +
          noCell(globalIdx) +
          codeValCell(item.codeVal) +
          '<td class="align-middle text-center py-2 col-sort">' +
          item.sort +
          '</td>' +
          useYnCell(item.useYn) +
          auditCells(item) +
          '</tr>'
        );
      })
      .join('');
    tbody.querySelectorAll('.code-detail-row').forEach(function (row) {
      var code = row.getAttribute('data-code-val');
      function onSelect() {
        setCodeChecked(code, true);
        selectCode(code);
      }
      row.addEventListener('click', function (e) {
        if (e.target.closest('.code-mgmt-check-cell')) {
          return;
        }
        onSelect();
      });
      row.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onSelect();
        }
      });
    });
    bindDetailCheckboxes();
  }

  function renderDetails() {
    var tbody = document.getElementById('code-detail-grid-body');
    var empty = document.getElementById('code-detail-empty');
    var badge = document.getElementById('selected-group-badge');
    if (!tbody) {
      return;
    }
    var groupKey = state.selectedGroup;
    if (badge) {
      var g0 = groupKey && state.groups[groupKey] ? state.groups[groupKey] : null;
      badge.textContent = g0
        ? (g0.codeId || groupKey) + (g0.codeNm ? ' · ' + g0.codeNm : '')
        : '그룹 선택';
    }
    if (!groupKey || !state.groups[groupKey]) {
      if (detailPager) {
        detailPager.setItems([]);
      }
      tbody.innerHTML = '';
      state.checkedCodes = [];
      updateDetailCheckAll();
      if (empty) {
        empty.classList.remove('d-none');
      }
      return;
    }
    pruneCheckedCodes();
    if (empty) {
      empty.classList.add('d-none');
    }
    var codes = state.groups[groupKey].codes || [];
    if (!codes.length) {
      if (detailPager) {
        detailPager.setItems([]);
      }
      tbody.innerHTML =
        '<tr><td colspan="' +
        COL_DETAIL +
        '" class="text-center text-600 py-4">상세코드가 없습니다. 상단 <strong>추가</strong>로 등록하세요.</td></tr>';
      updateDetailCheckAll();
      return;
    }
    ensureDetailPager();
    if (detailPager) {
      detailPager.setItems(codes);
      return;
    }
    paintDetailRows(codes, { start: 1, totalItems: codes.length });
  }

  function triggerGroupEdit(groupKey) {
    if (!groupKey || !state.groups[groupKey]) {
      notify('먼저 조회하거나 코드그룹을 추가하세요.', 'warning');
      return;
    }
    selectGroup(groupKey);
    editGroup(groupKey);
  }

  function triggerDetailEdit(codeVal) {
    if (!state.selectedGroup) {
      notify('코드그룹을 먼저 선택하세요.', 'warning');
      return;
    }
    if (!codeVal) {
      return;
    }
    selectCode(codeVal);
    editCode(codeVal);
  }

  function isEditInteractionIgnored(target) {
    return !!target.closest(
      '.code-mgmt-check-cell, .btn-add-group, .btn-del-group, .btn-add-code, .btn-del-code, #code-save-btn, #code-export-btn, #btn-search-groups, #btn-reset-search'
    );
  }

  function bindGridEditInteractions() {
    var panel = document.getElementById('code-management-panel');
    if (!panel || panel._codeMgmtEditBound) {
      return;
    }
    panel._codeMgmtEditBound = true;
    var lastEditAt = 0;

    function handleGridEdit(e) {
      var now = Date.now();
      if (now - lastEditAt < 400) {
        return;
      }
      lastEditAt = now;
      if (isEditInteractionIgnored(e.target)) {
        return;
      }
      if (e.target.closest('.code-group-edit-btn')) {
        return;
      }
      if (e.target.closest('.code-detail-edit-btn')) {
        return;
      }

      var groupRow = e.target.closest('.code-group-row, .code-group-search-row');
      if (groupRow) {
        var groupKey = groupRow.getAttribute('data-group');
        if (!groupKey) {
          return;
        }
        e.preventDefault();
        triggerGroupEdit(groupKey);
        return;
      }

      var detailRow = e.target.closest('.code-detail-row');
      if (detailRow) {
        var codeVal = detailRow.getAttribute('data-code-val');
        if (!codeVal) {
          return;
        }
        e.preventDefault();
        triggerDetailEdit(codeVal);
      }
    }

    panel.addEventListener('dblclick', function (e) {
      if (isEditInteractionIgnored(e.target)) {
        return;
      }
      if (e.target.closest('.code-group-edit-btn, .code-detail-edit-btn')) {
        return;
      }
      handleGridEdit(e);
    });
  }

  function refreshAll() {
    renderSearchGrid();
    renderGroups();
    renderDetails();
  }

  function syncFilteredKeysAfterAdd(key) {
    if (!state.searched) {
      return;
    }
    state.filteredKeys = state.filteredKeys || [];
    if (state.filteredKeys.indexOf(key) === -1) {
      state.filteredKeys.push(key);
      state.filteredKeys.sort();
    }
  }

  function syncFilteredKeysAfterDelete(key) {
    if (!state.searched || !state.filteredKeys) {
      return;
    }
    state.filteredKeys = state.filteredKeys.filter(function (k) {
      return k !== key;
    });
  }

  function normalizeUseYn(value) {
    return String(value || 'Y').toUpperCase() === 'N' ? 'N' : 'Y';
  }

  /** 값|표시명 형식은 그대로, 단순 코드만 대문자·언더스코어 정규화 */
  function normalizeCodeValInput(raw) {
    var trimmed = (raw || '').trim();
    if (!trimmed) {
      return '';
    }
    if (trimmed.indexOf('|') !== -1) {
      return trimmed;
    }
    return trimmed.toUpperCase().replace(/\s+/g, '_');
  }

  function showBootstrapModal(modalId) {
    var el = document.getElementById(modalId);
    if (!el) {
      notify('수정 창을 불러오지 못했습니다. Ctrl+F5로 새로고침 해주세요.', 'error');
      return null;
    }
    if (el.parentElement && el.parentElement !== document.body) {
      document.body.appendChild(el);
    }
    if (!window.bootstrap || !window.bootstrap.Modal) {
      notify('화면 모듈(Bootstrap)을 불러오지 못했습니다.', 'error');
      return null;
    }
    return window.bootstrap.Modal.getOrCreateInstance(el);
  }

  function editBtnHtml(className, dataAttr, dataValue, title) {
    return (
      '<button type="button" class="btn btn-link btn-sm p-0 ms-1 ' +
      className +
      '" ' +
      dataAttr +
      '="' +
      escapeAttr(dataValue) +
      '" title="' +
      title +
      '" aria-label="' +
      title +
      '">' +
      '<span class="fas fa-edit text-primary"></span></button>'
    );
  }

  function resetGroupAddForm() {
    var form = document.getElementById('code-group-add-form');
    if (form) {
      form.reset();
    }
    var useYn = document.getElementById('code-group-add-use-yn');
    if (useYn) {
      useYn.value = 'Y';
    }
  }

  function submitGroupAdd() {
    var codeIdEl = document.getElementById('code-group-add-code-id');
    var codeNmEl = document.getElementById('code-group-add-code-nm');
    var useYnEl = document.getElementById('code-group-add-use-yn');
    var codeIdRaw = codeIdEl ? codeIdEl.value.trim() : '';
    if (!codeIdRaw) {
      notify('코드ID를 입력하세요.', 'warning');
      return;
    }
    var key = codeIdRaw.toUpperCase().replace(/\s+/g, '_');
    if (state.groups[key]) {
      notify('이미 존재하는 코드ID입니다.', 'warning');
      return;
    }
    state.groups[key] = {
      codeId: key,
      codeNm: (codeNmEl && codeNmEl.value.trim()) || key,
      useYn: normalizeUseYn(useYnEl ? useYnEl.value : 'Y'),
      detailCount: 0,
      regId: '',
      regdateDt: null,
      updateId: '',
      updateDt: null,
      codes: []
    };
    state.selectedGroup = key;
    state.selectedCode = null;
    syncFilteredKeysAfterAdd(key);
    refreshAll();
    var modal = showBootstrapModal('code-group-add-modal');
    if (modal) {
      modal.hide();
    }
    persistToDb(buildSavePayloadForGroup(key), '코드그룹이 저장되었습니다.');
  }

  function openGroupEditModal(key) {
    var g = state.groups[key];
    if (!g) {
      return;
    }
    state.editGroupKey = key;
    var codeIdEl = document.getElementById('code-group-edit-code-id');
    var codeNmEl = document.getElementById('code-group-edit-code-nm');
    var useYnEl = document.getElementById('code-group-edit-use-yn');
    if (codeIdEl) {
      codeIdEl.value = g.codeId || key;
    }
    if (codeNmEl) {
      codeNmEl.value = g.codeNm || '';
    }
    if (useYnEl) {
      useYnEl.value = normalizeUseYn(g.useYn);
    }
    var modal = showBootstrapModal('code-group-edit-modal');
    if (modal) {
      modal.show();
      if (codeNmEl) {
        window.setTimeout(function () {
          codeNmEl.focus();
          codeNmEl.select();
        }, 200);
      }
    }
  }

  function submitGroupEdit() {
    var key = state.editGroupKey;
    if (!key || !state.groups[key]) {
      notify('수정할 코드그룹을 찾을 수 없습니다.', 'warning');
      return;
    }
    var codeNmEl = document.getElementById('code-group-edit-code-nm');
    var useYnEl = document.getElementById('code-group-edit-use-yn');
    var codeNm = codeNmEl ? codeNmEl.value.trim() : '';
    if (!codeNm) {
      notify('코드명을 입력하세요.', 'warning');
      return;
    }
    var g = state.groups[key];
    g.codeNm = codeNm;
    g.useYn = normalizeUseYn(useYnEl ? useYnEl.value : 'Y');
    state.selectedGroup = key;
    refreshAll();
    state.editGroupKey = null;
    var modal = showBootstrapModal('code-group-edit-modal');
    if (modal) {
      modal.hide();
    }
    persistToDb(buildSavePayloadForGroup(key), '코드그룹이 저장되었습니다.');
  }

  function editGroup(key) {
    if (!key || !state.groups[key]) {
      notify('수정할 코드그룹이 없습니다.', 'warning');
      return;
    }
    openGroupEditModal(key);
  }

  function addGroup() {
    resetGroupAddForm();
    var modal = showBootstrapModal('code-group-add-modal');
    if (modal) {
      modal.show();
      var codeIdEl = document.getElementById('code-group-add-code-id');
      if (codeIdEl) {
        window.setTimeout(function () {
          codeIdEl.focus();
        }, 200);
      }
      return;
    }
    promptInput('코드그룹 추가', [
      { id: 'codeId', label: '코드ID (code_id)', placeholder: '예: ORDER_TYPE' },
      { id: 'codeNm', label: '코드명 (code_nm)', placeholder: '예: 주문유형' },
      {
        id: 'useYn',
        label: '사용여부',
        type: 'select',
        value: 'Y',
        codeGroup: 'USE_YN_ADMIN'
      }
    ]).then(function (vals) {
      if (!vals || !vals.codeId) {
        return;
      }
      var key = vals.codeId.toUpperCase().replace(/\s+/g, '_');
      if (state.groups[key]) {
        notify('이미 존재하는 코드ID입니다.', 'warning');
        return;
      }
      state.groups[key] = {
        codeId: key,
        codeNm: vals.codeNm || key,
        useYn: normalizeUseYn(vals.useYn),
        detailCount: 0,
        regId: '',
        regdateDt: null,
        updateId: '',
        updateDt: null,
        codes: []
      };
      state.selectedGroup = key;
      state.selectedCode = null;
      syncFilteredKeysAfterAdd(key);
      refreshAll();
      persistToDb(buildSavePayloadForGroup(key), '코드그룹이 저장되었습니다.');
    });
  }

  function deleteGroup() {
    pruneCheckedGroups();
    var keysToDelete = state.checkedGroups.length
      ? state.checkedGroups.slice()
      : state.selectedGroup
        ? [state.selectedGroup]
        : [];
    if (!keysToDelete.length) {
      notify('삭제할 코드그룹을 체크하거나 선택하세요.', 'warning');
      return;
    }
    var detailCount = 0;
    keysToDelete.forEach(function (key) {
      var g = state.groups[key];
      if (g && g.codes) {
        detailCount += g.codes.length;
      } else if (g && g.detailCount) {
        detailCount += g.detailCount;
      }
    });
    var confirmText =
      keysToDelete.length === 1
        ? '"' +
          keysToDelete[0] +
          '" 코드그룹과 연결된 상세코드(' +
          detailCount +
          '건)를 DB에서 삭제할까요?'
        : keysToDelete.length +
          '개 코드그룹과 연결된 상세코드(' +
          detailCount +
          '건)를 DB에서 삭제할까요?';
    var doDelete = function () {
      deleteGroupsApi(keysToDelete)
        .then(function (data) {
          state.checkedGroups = [];
          state.selectedGroup = null;
          state.selectedCode = null;
          if (state.searched) {
            searchGroups();
          } else {
            keysToDelete.forEach(function (key) {
              delete state.groups[key];
              syncFilteredKeysAfterDelete(key);
            });
            refreshAll();
          }
          notify(data.message || '코드그룹이 삭제되었습니다.', 'success');
        })
        .catch(function (err) {
          notify(
            err && err.message ? err.message : '코드그룹 삭제 중 오류가 발생했습니다.',
            'error'
          );
        });
    };
    if (!window.Swal) {
      doDelete();
      return;
    }
    window.Swal.fire({
      title: '코드그룹 삭제',
      text: confirmText,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: '삭제',
      cancelButtonText: '취소'
    }).then(function (res) {
      if (res.isConfirmed) {
        doDelete();
      }
    });
  }

  function resetDetailAddForm() {
    var form = document.getElementById('code-detail-add-form');
    if (form) {
      form.reset();
    }
    var useYn = document.getElementById('code-detail-add-use-yn');
    if (useYn) {
      useYn.value = 'Y';
    }
    var sortEl = document.getElementById('code-detail-add-sort');
    var g = state.selectedGroup ? state.groups[state.selectedGroup] : null;
    if (sortEl && g) {
      sortEl.value = String((g.codes || []).length + 1);
    }
  }

  function submitDetailAdd() {
    if (!state.selectedGroup) {
      notify('코드그룹을 먼저 선택하세요.', 'warning');
      return;
    }
    var codeValEl = document.getElementById('code-detail-add-code-val');
    var sortEl = document.getElementById('code-detail-add-sort');
    var useYnEl = document.getElementById('code-detail-add-use-yn');
    var codeValRaw = codeValEl ? codeValEl.value.trim() : '';
    if (!codeValRaw) {
      notify('코드값을 입력하세요.', 'warning');
      return;
    }
    var g = state.groups[state.selectedGroup];
    var codeVal = normalizeCodeValInput(codeValRaw);
    if (g.codes.some(function (c) { return c.codeVal === codeVal; })) {
      notify('이미 존재하는 코드값입니다.', 'warning');
      return;
    }
    g.codes.push({
      codeId: state.selectedGroup,
      codeVal: codeVal,
      sort: parseInt(sortEl && sortEl.value, 10) || g.codes.length + 1,
      useYn: normalizeUseYn(useYnEl ? useYnEl.value : 'Y'),
      regId: '',
      regdateDt: null,
      updateId: '',
      updateDt: null
    });
    g.detailCount = g.codes.length;
    g.codes.sort(function (a, b) { return a.sort - b.sort; });
    state.selectedCode = codeVal;
    refreshAll();
    var modal = showBootstrapModal('code-detail-add-modal');
    if (modal) {
      modal.hide();
    }
    persistToDb(
      buildSavePayloadForGroup(state.selectedGroup),
      '상세코드가 DB에 저장되었습니다.'
    );
  }

  function openDetailEditModal(codeVal) {
    if (!state.selectedGroup || !state.groups[state.selectedGroup]) {
      return;
    }
    var g = state.groups[state.selectedGroup];
    var item = (g.codes || []).find(function (c) {
      return c.codeVal === codeVal;
    });
    if (!item) {
      notify('수정할 상세코드를 찾을 수 없습니다.', 'warning');
      return;
    }
    state.editDetailCodeVal = codeVal;
    var codeValEl = document.getElementById('code-detail-edit-code-val');
    var sortEl = document.getElementById('code-detail-edit-sort');
    var useYnEl = document.getElementById('code-detail-edit-use-yn');
    if (codeValEl) {
      codeValEl.value = item.codeVal;
    }
    if (sortEl) {
      sortEl.value = String(item.sort != null ? item.sort : 1);
    }
    if (useYnEl) {
      useYnEl.value = normalizeUseYn(item.useYn);
    }
    var modal = showBootstrapModal('code-detail-edit-modal');
    if (modal) {
      modal.show();
      if (sortEl) {
        window.setTimeout(function () {
          sortEl.focus();
          sortEl.select();
        }, 200);
      }
    }
  }

  function submitDetailEdit() {
    var codeVal = state.editDetailCodeVal;
    if (!state.selectedGroup || !codeVal) {
      notify('수정할 상세코드를 찾을 수 없습니다.', 'warning');
      return;
    }
    var g = state.groups[state.selectedGroup];
    var item = (g.codes || []).find(function (c) {
      return c.codeVal === codeVal;
    });
    if (!item) {
      notify('수정할 상세코드를 찾을 수 없습니다.', 'warning');
      return;
    }
    var sortEl = document.getElementById('code-detail-edit-sort');
    var useYnEl = document.getElementById('code-detail-edit-use-yn');
    item.sort = parseInt(sortEl && sortEl.value, 10) || item.sort || 1;
    item.useYn = normalizeUseYn(useYnEl ? useYnEl.value : 'Y');
    g.codes.sort(function (a, b) {
      return a.sort - b.sort;
    });
    g.detailCount = g.codes.length;
    state.selectedCode = codeVal;
    refreshAll();
    state.editDetailCodeVal = null;
    var modal = showBootstrapModal('code-detail-edit-modal');
    if (modal) {
      modal.hide();
    }
    persistToDb(
      buildSavePayloadForGroup(state.selectedGroup),
      '상세코드가 DB에 저장되었습니다.'
    );
  }

  function editCode(codeVal) {
    if (!state.selectedGroup) {
      notify('코드그룹을 먼저 선택하세요.', 'warning');
      return;
    }
    if (!codeVal) {
      notify('수정할 상세코드가 없습니다.', 'warning');
      return;
    }
    openDetailEditModal(codeVal);
  }

  function addCode() {
    if (!state.selectedGroup) {
      notify('조회 그리드 또는 코드그룹 그리드에서 그룹을 선택하세요.', 'warning');
      return;
    }
    resetDetailAddForm();
    var modal = showBootstrapModal('code-detail-add-modal');
    if (modal) {
      modal.show();
      var codeValEl = document.getElementById('code-detail-add-code-val');
      if (codeValEl) {
        window.setTimeout(function () {
          codeValEl.focus();
        }, 200);
      }
      return;
    }
    promptInput('상세코드 추가', [
      { id: 'codeVal', label: '코드값 (code_val)', placeholder: '예: SHIPPED' },
      { id: 'sort', label: '정렬', placeholder: '1' },
      {
        id: 'useYn',
        label: '사용여부',
        type: 'select',
        value: 'Y',
        codeGroup: 'USE_YN_ADMIN'
      }
    ]).then(function (vals) {
      if (!vals || !vals.codeVal) {
        return;
      }
      var g = state.groups[state.selectedGroup];
      var codeVal = normalizeCodeValInput(vals.codeVal);
      if (g.codes.some(function (c) { return c.codeVal === codeVal; })) {
        notify('이미 존재하는 코드값입니다.', 'warning');
        return;
      }
      g.codes.push({
        codeId: state.selectedGroup,
        codeVal: codeVal,
        sort: parseInt(vals.sort, 10) || g.codes.length + 1,
        useYn: normalizeUseYn(vals.useYn),
        regId: '',
        regdateDt: null,
        updateId: '',
        updateDt: null
      });
      g.detailCount = g.codes.length;
      g.codes.sort(function (a, b) { return a.sort - b.sort; });
      state.selectedCode = codeVal;
      refreshAll();
      notify('상세코드가 추가되었습니다.', 'success');
    });
  }

  function deleteCode() {
    if (!state.selectedGroup) {
      notify('코드그룹을 먼저 선택하세요.', 'warning');
      return;
    }
    pruneCheckedCodes();
    var codesToDelete = state.checkedCodes.length
      ? state.checkedCodes.slice()
      : state.selectedCode
        ? [state.selectedCode]
        : [];
    if (!codesToDelete.length) {
      notify('삭제할 상세코드를 체크하거나 선택하세요.', 'warning');
      return;
    }
    var g = state.groups[state.selectedGroup];
    var confirmText =
      codesToDelete.length === 1
        ? '"' + codesToDelete[0] + '" 코드를 삭제할까요?'
        : codesToDelete.length + '개 상세코드를 삭제할까요?';
    var groupKey = state.selectedGroup;
    var doDelete = function () {
      deleteCodeValuesApi(groupKey, codesToDelete)
        .then(function (data) {
          state.checkedCodes = [];
          state.selectedCode = null;
          if (state.searched) {
            searchGroups();
          } else {
            g.codes = g.codes.filter(function (c) {
              return codesToDelete.indexOf(c.codeVal) === -1;
            });
            g.detailCount = g.codes.length;
            refreshAll();
          }
          notify(data.message || codesToDelete.length + '개 상세코드가 삭제되었습니다.', 'success');
        })
        .catch(function (err) {
          notify(
            err && err.message ? err.message : '상세코드 삭제 중 오류가 발생했습니다.',
            'error'
          );
        });
    };
    if (window.Swal) {
      window.Swal.fire({
        title: '상세코드 삭제',
        text: confirmText,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: '삭제',
        cancelButtonText: '취소'
      }).then(function (res) {
        if (res.isConfirmed) {
          doDelete();
        }
      });
      return;
    }
    doDelete();
  }

  /**
   * 현재 조회 결과를 CSV 파일로 내보냅니다.
   * @returns {void}
   */
  function exportCsv() {
    var keys = state.searched ? state.filteredKeys || [] : Object.keys(state.groups).sort();
    var lines = [
      'code_id,code_nm,use_yn,reg_id,regdate_dt,update_id,update_dt,detail_count,code_val,detail_use_yn,detail_reg_id,detail_regdate_dt,detail_update_id,detail_update_dt,detail_sort'
    ];
    keys.forEach(function (key) {
      var g = state.groups[key];
      (g.codes || []).forEach(function (c) {
        lines.push(
          [
            g.codeId || key,
            g.codeNm || '',
            g.useYn || 'Y',
            g.regId || '',
            formatDt(g.regdateDt),
            g.updateId || '',
            formatDt(g.updateDt),
            g.detailCount != null ? g.detailCount : '',
            c.codeVal,
            c.useYn,
            c.regId || '',
            formatDt(c.regdateDt),
            c.updateId || '',
            formatDt(c.updateDt),
            c.sort
          ]
            .map(function (v) {
              var t = String(v);
              return /[",\n\r]/.test(t) ? '"' + t.replace(/"/g, '""') + '"' : t;
            })
            .join(',')
        );
      });
    });
    var blob = new Blob(['\uFEFF' + lines.join('\r\n')], {
      type: 'text/csv;charset=utf-8;'
    });
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = 'codes-' + new Date().toISOString().slice(0, 10) + '.csv';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    notify('CSV 파일을 다운로드했습니다.', 'success');
  }

  function groupToSaveDto(key) {
    var g = state.groups[key];
    if (!g) {
      return null;
    }
    return {
      codeId: g.codeId || key,
      codeNm: g.codeNm || key,
      useYn: normalizeUseYn(g.useYn),
      codes: (g.codes || []).map(function (c) {
        return {
          codeVal: c.codeVal,
          useYn: normalizeUseYn(c.useYn)
        };
      })
    };
  }

  function buildSavePayload() {
    var groups = [];
    Object.keys(state.groups)
      .sort()
      .forEach(function (key) {
        var dto = groupToSaveDto(key);
        if (dto) {
          groups.push(dto);
        }
      });
    return { groups: groups };
  }

  function buildSavePayloadForGroup(groupKey) {
    var dto = groupToSaveDto(groupKey);
    return { groups: dto ? [dto] : [] };
  }

  function reloadGroups() {
    var criteria = getSearchCriteria();
    var params = new URLSearchParams();
    if (criteria.codeId) {
      params.set('codeId', criteria.codeId);
    }
    if (criteria.codeNm) {
      params.set('codeNm', criteria.codeNm);
    }
    if (criteria.useYn) {
      params.set('useYn', criteria.useYn);
    }
    return fetch('/api/admin/codes?' + params.toString(), {
      method: 'GET',
      headers: { Accept: 'application/json' },
      credentials: 'same-origin'
    })
      .then(function (res) {
        if (handleUnauthorized(res)) {
          return;
        }
        if (!res.ok) {
          throw new Error('조회 API 오류 (' + res.status + ')');
        }
        return res.json();
      })
      .then(function (data) {
        if (!data) {
          return;
        }
        applyGroupsFromApi(data.groups);
        finishSearch(false);
      });
  }

  function persistToDb(payload, successMessage) {
    if (!payload.groups || !payload.groups.length) {
      return Promise.reject(new Error('저장할 코드그룹이 없습니다.'));
    }
    if (state.saving) {
      return Promise.reject(new Error('저장 중입니다. 잠시 후 다시 시도하세요.'));
    }
    state.saving = true;
    return saveCodesApi(payload)
      .then(function (data) {
        return reloadGroups().then(function () {
          return data;
        });
      })
      .then(function (data) {
        notify(successMessage || data.message || '저장되었습니다.', 'success');
        return data;
      })
      .catch(function (err) {
        notify(
          err && err.message ? err.message : '코드 저장 중 오류가 발생했습니다.',
          'error'
        );
        throw err;
      })
      .finally(function () {
        state.saving = false;
      });
  }

  function saveCodesApi(payload) {
    return fetch('/api/admin/codes/save', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json'
      },
      credentials: 'same-origin',
      body: JSON.stringify(payload)
    }).then(parseApiResponse);
  }

  /**
   * 조회된 전체 코드그룹·상세코드를 DB에 일괄 저장합니다.
   * @returns {void}
   */
  function saveAll() {
    var payload = buildSavePayload();
    if (!payload.groups.length) {
      notify('저장할 코드그룹이 없습니다. 조회하거나 그룹을 추가하세요.', 'warning');
      return;
    }
    persistToDb(payload);
  }

  function handlePanelClick(e) {
    var btn = e.target.closest('button');
    if (!btn) {
      return;
    }
    if (btn.classList.contains('code-group-edit-btn')) {
      e.preventDefault();
      e.stopPropagation();
      triggerGroupEdit(btn.getAttribute('data-group'));
      return;
    }
    if (btn.classList.contains('code-detail-edit-btn')) {
      e.preventDefault();
      e.stopPropagation();
      triggerDetailEdit(btn.getAttribute('data-code-val'));
      return;
    }
    if (btn.classList.contains('btn-add-group')) {
      e.preventDefault();
      addGroup();
      return;
    }
    if (btn.classList.contains('btn-del-group')) {
      e.preventDefault();
      deleteGroup();
      return;
    }
    if (btn.classList.contains('btn-add-code')) {
      e.preventDefault();
      addCode();
      return;
    }
    if (btn.classList.contains('btn-del-code')) {
      e.preventDefault();
      deleteCode();
      return;
    }
  }

  function bind() {
    var panel = document.getElementById('code-management-panel');
    if (!panel || panel._codeMgmtBound) {
      return;
    }
    panel._codeMgmtBound = true;

    var searchForm = document.getElementById('code-group-search-form');
    if (searchForm) {
      searchForm.addEventListener('submit', function (e) {
        e.preventDefault();
        searchGroups();
      });
    }
    var resetBtn = document.getElementById('btn-reset-search');
    if (resetBtn) {
      resetBtn.addEventListener('click', resetSearch);
    }

    panel.addEventListener('click', handlePanelClick);
    bindGridEditInteractions();

    var groupAddForm = document.getElementById('code-group-add-form');
    if (groupAddForm) {
      groupAddForm.addEventListener('submit', function (e) {
        e.preventDefault();
        submitGroupAdd();
      });
    }
    var detailAddForm = document.getElementById('code-detail-add-form');
    if (detailAddForm) {
      detailAddForm.addEventListener('submit', function (e) {
        e.preventDefault();
        submitDetailAdd();
      });
    }
    var groupEditForm = document.getElementById('code-group-edit-form');
    if (groupEditForm) {
      groupEditForm.addEventListener('submit', function (e) {
        e.preventDefault();
        submitGroupEdit();
      });
    }
    var detailEditForm = document.getElementById('code-detail-edit-form');
    if (detailEditForm) {
      detailEditForm.addEventListener('submit', function (e) {
        e.preventDefault();
        submitDetailEdit();
      });
    }

    var exportBtn = document.getElementById('code-export-btn');
    var saveBtn = document.getElementById('code-save-btn');
    if (exportBtn) {
      exportBtn.addEventListener('click', exportCsv);
    }
    if (saveBtn) {
      saveBtn.addEventListener('click', saveAll);
    }

    renderSearchGrid();
    renderGroups();
    renderDetails();
  }

  /**
   * 코드 관리 패널 이벤트·그리드 초기 렌더링을 수행합니다.
   * @returns {void}
   */
  function init() {
    bind();
  }

  /** @see init */
  window.CodeMgmtInit = init;

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
