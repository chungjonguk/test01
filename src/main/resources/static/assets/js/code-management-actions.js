/**
 * 코드 관리 — 상단 조회그리드 + 좌(그룹) / 중(추가·삭제) / 우(상세코드)
 */
(function () {
  'use strict';

  var COL_SEARCH = 8;
  var COL_GROUP = 9;
  var COL_DETAIL = 10;

  var state = {
    groups: {},
    filteredKeys: null,
    selectedGroup: null,
    selectedCode: null,
    checkedGroups: [],
    checkedCodes: [],
    searched: false,
    loading: false
  };

  function formatDt(value) {
    if (value == null || value === '') {
      return '—';
    }
    var s = String(value);
    if (Array.isArray(value) && value.length >= 3) {
      var pad = function (n) {
        return n < 10 ? '0' + n : String(n);
      };
      return (
        value[0] +
        '-' +
        pad(value[1]) +
        '-' +
        pad(value[2]) +
        ' ' +
        pad(value[3] || 0) +
        ':' +
        pad(value[4] || 0) +
        ':' +
        pad(value[5] || 0)
      );
    }
    if (s.indexOf('T') !== -1) {
      return s.replace('T', ' ').slice(0, 19);
    }
    return s.length > 19 ? s.slice(0, 19) : s;
  }

  function escapeAttr(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/"/g, '&quot;')
      .replace(/</g, '&lt;');
  }

  function noCell(index) {
    return (
      '<td class="ps-3 align-middle text-center py-2">' +
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
    var valid = codes.map(function (c) { return c.code; });
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
      return state.checkedCodes.indexOf(c.code) !== -1;
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
          state.checkedCodes = codes.map(function (c) { return c.code; });
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

  function auditCells(item) {
    item = item || {};
    return (
      '<td class="align-middle white-space-nowrap py-2">' +
      (item.regId || '—') +
      '</td>' +
      '<td class="align-middle white-space-nowrap py-2 text-600">' +
      formatDt(item.regdateDt) +
      '</td>' +
      '<td class="align-middle white-space-nowrap py-2">' +
      (item.updateId || '—') +
      '</td>' +
      '<td class="align-middle white-space-nowrap py-2 text-600">' +
      formatDt(item.updateDt) +
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
    var keyEl = document.getElementById('search-group-key');
    var nameEl = document.getElementById('search-group-name');
    var useEl = document.getElementById('search-group-use');
    return {
      groupKey: keyEl ? keyEl.value.trim().toUpperCase() : '',
      groupName: nameEl ? nameEl.value.trim() : '',
      useYn: useEl ? useEl.value.trim() : ''
    };
  }

  function matchGroup(key, criteria) {
    var g = state.groups[key];
    if (!g) {
      return false;
    }
    if (criteria.groupKey && key.indexOf(criteria.groupKey) === -1) {
      return false;
    }
    if (criteria.groupName && (g.name || '').indexOf(criteria.groupName) === -1) {
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
        name: g.codeNm || key,
        useYn: g.useYn || 'Y',
        remark: '',
        regId: g.regId || '',
        regdateDt: g.regdateDt || null,
        updateId: g.updateId || '',
        updateDt: g.updateDt || null,
        codes: (g.codes || []).map(function (c) {
          return {
            code: c.code,
            name: c.name || c.code,
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

  function searchGroups() {
    if (state.loading) {
      return;
    }
    var criteria = getSearchCriteria();
    var params = new URLSearchParams();
    if (criteria.groupKey) {
      params.set('codeId', criteria.groupKey);
    }
    if (criteria.groupName) {
      params.set('codeNm', criteria.groupName);
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
        if (!res.ok) {
          throw new Error('조회 API 오류 (' + res.status + ')');
        }
        return res.json();
      })
      .then(function (data) {
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

  function selectCode(code) {
    if (!code || !state.selectedGroup) {
      return;
    }
    var g = state.groups[state.selectedGroup];
    if (!g || !g.codes || !g.codes.some(function (c) { return c.code === code; })) {
      return;
    }
    state.selectedCode = code;
    renderDetails();
  }

  function promptInput(title, fields) {
    if (!window.Swal) {
      return new Promise(function (resolve) {
        var result = {};
        for (var i = 0; i < fields.length; i++) {
          var f = fields[i];
          var val = window.prompt(title + ' — ' + f.label, f.value || '');
          if (val === null) {
            resolve(null);
            return;
          }
          result[f.id] = val.trim();
        }
        resolve(result);
      });
    }
    var html = fields
      .map(function (f) {
        return (
          '<div class="mb-2 text-start"><label class="form-label mb-1">' +
          f.label +
          '</label><input class="form-control form-control-sm" id="swal-' +
          f.id +
          '" type="text" placeholder="' +
          (f.placeholder || '') +
          '" value="' +
          (f.value || '') +
          '"></div>'
        );
      })
      .join('');
    return window.Swal.fire({
      title: title,
      html: html,
      showCancelButton: true,
      confirmButtonText: '확인',
      cancelButtonText: '취소',
      focusConfirm: false,
      preConfirm: function () {
        var result = {};
        fields.forEach(function (f) {
          var el = document.getElementById('swal-' + f.id);
          result[f.id] = el ? el.value.trim() : '';
        });
        return result;
      }
    }).then(function (res) {
      return res.isConfirmed ? res.value : null;
    });
  }

  function renderSearchGrid() {
    var tbody = document.getElementById('code-group-search-grid-body');
    var countEl = document.getElementById('code-search-result-count');
    if (!tbody) {
      return;
    }
    if (!state.searched) {
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
      tbody.innerHTML =
        '<tr><td colspan="' +
        COL_SEARCH +
        '" class="text-center text-600 py-4">조회 결과가 없습니다.</td></tr>';
      return;
    }
    tbody.innerHTML = keys
      .map(function (key, index) {
        var g = state.groups[key];
        var active = key === state.selectedGroup ? ' table-active' : '';
        var yn = g.useYn || 'Y';
        var ynClass = yn === 'Y' ? 'badge-soft-success' : 'badge-soft-warning';
        return (
          '<tr class="code-group-search-row' +
          active +
          '" data-group="' +
          key +
          '" role="button" tabindex="0">' +
          noCell(index) +
          '<td class="align-middle white-space-nowrap py-2"><strong>' +
          key +
          '</strong></td>' +
          '<td class="align-middle py-2">' +
          (g.name || '') +
          '</td>' +
          '<td class="align-middle text-center py-2"><span class="badge ' +
          ynClass +
          '">' +
          yn +
          '</span></td>' +
          auditCells(g) +
          '</tr>'
        );
      })
      .join('');
    tbody.querySelectorAll('.code-group-search-row').forEach(function (row) {
      function onSelect() {
        selectGroup(row.getAttribute('data-group'));
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

  function renderGroups() {
    var tbody = document.getElementById('code-group-grid-body');
    if (!tbody) {
      return;
    }
    var keys = getFilteredKeys();
    if (!keys.length) {
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
    tbody.innerHTML = keys
      .map(function (key, index) {
        var g = state.groups[key];
        var active = key === state.selectedGroup ? ' table-active' : '';
        var count = g.codes ? g.codes.length : 0;
        return (
          '<tr class="code-group-row' +
          active +
          '" data-group="' +
          key +
          '" role="button" tabindex="0">' +
          groupCheckCell(key) +
          noCell(index) +
          '<td class="align-middle white-space-nowrap py-2"><strong>' +
          key +
          '</strong></td>' +
          '<td class="align-middle py-2">' +
          (g.name || '') +
          '</td>' +
          '<td class="align-middle text-center py-2"><span class="badge badge-soft-secondary">' +
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

  function renderDetails() {
    var tbody = document.getElementById('code-detail-grid-body');
    var empty = document.getElementById('code-detail-empty');
    var badge = document.getElementById('selected-group-badge');
    if (!tbody) {
      return;
    }
    var groupKey = state.selectedGroup;
    if (badge) {
      badge.textContent = groupKey || '그룹 선택';
    }
    if (!groupKey || !state.groups[groupKey]) {
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
      tbody.innerHTML =
        '<tr><td colspan="' +
        COL_DETAIL +
        '" class="text-center text-600 py-4">상세코드가 없습니다. 상단 <strong>추가</strong>로 등록하세요.</td></tr>';
      updateDetailCheckAll();
      return;
    }
    tbody.innerHTML = codes
      .map(function (item, idx) {
        var active = state.selectedCode === item.code ? ' table-active' : '';
        var ynClass =
          item.useYn === 'Y' ? 'badge-soft-success' : 'badge-soft-warning';
        return (
          '<tr class="code-detail-row' +
          active +
          '" data-code="' +
          item.code +
          '" data-index="' +
          idx +
          '" role="button" tabindex="0">' +
          detailCheckCell(item.code) +
          noCell(idx) +
          '<td class="align-middle white-space-nowrap py-2"><code>' +
          item.code +
          '</code></td>' +
          '<td class="align-middle py-2">' +
          item.name +
          '</td>' +
          '<td class="align-middle text-center py-2">' +
          item.sort +
          '</td>' +
          '<td class="align-middle text-center py-2"><span class="badge ' +
          ynClass +
          '">' +
          item.useYn +
          '</span></td>' +
          auditCells(item) +
          '</tr>'
        );
      })
      .join('');
    tbody.querySelectorAll('.code-detail-row').forEach(function (row) {
      var code = row.getAttribute('data-code');
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

  function addGroup() {
    promptInput('코드그룹 추가', [
      { id: 'key', label: '코드', placeholder: '예: ORDER_TYPE' },
      { id: 'name', label: '코드명', placeholder: '예: 주문유형' }
    ]).then(function (vals) {
      if (!vals || !vals.key) {
        return;
      }
      var key = vals.key.toUpperCase().replace(/\s+/g, '_');
      if (state.groups[key]) {
        notify('이미 존재하는 코드입니다.', 'warning');
        return;
      }
      state.groups[key] = {
        name: vals.name || key,
        useYn: 'Y',
        remark: '',
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
      notify('코드그룹이 추가되었습니다. (저장 버튼으로 DB 반영)', 'success');
    });
  }

  function deleteGroup() {
    if (!state.selectedGroup) {
      notify('삭제할 코드그룹을 선택하세요.', 'warning');
      return;
    }
    var key = state.selectedGroup;
    var doDelete = function () {
      delete state.groups[key];
      state.selectedGroup = null;
      state.selectedCode = null;
      if (state.searched) {
        searchGroups();
      } else {
        refreshAll();
      }
      notify('코드그룹이 삭제되었습니다.', 'success');
    };
    if (!window.Swal) {
      doDelete();
      return;
    }
    window.Swal.fire({
      title: '코드그룹 삭제',
      text: '"' + key + '" 그룹과 상세코드를 모두 삭제할까요?',
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

  function addCode() {
    if (!state.selectedGroup) {
      notify('조회 그리드 또는 코드그룹 그리드에서 그룹을 선택하세요.', 'warning');
      return;
    }
    promptInput('상세코드 추가', [
      { id: 'code', label: '코드', placeholder: '예: SHIPPED' },
      { id: 'name', label: '코드명', placeholder: '예: 배송중' },
      { id: 'sort', label: '정렬', placeholder: '1' },
      { id: 'useYn', label: '사용(Y/N)', placeholder: 'Y', value: 'Y' }
    ]).then(function (vals) {
      if (!vals || !vals.code) {
        return;
      }
      var g = state.groups[state.selectedGroup];
      var code = vals.code.toUpperCase().replace(/\s+/g, '_');
      if (g.codes.some(function (c) { return c.code === code; })) {
        notify('이미 존재하는 코드입니다.', 'warning');
        return;
      }
      g.codes.push({
        code: code,
        name: vals.name || code,
        sort: parseInt(vals.sort, 10) || g.codes.length + 1,
        useYn: (vals.useYn || 'Y').toUpperCase() === 'N' ? 'N' : 'Y',
        regId: '',
        regdateDt: null,
        updateId: '',
        updateDt: null
      });
      g.codes.sort(function (a, b) { return a.sort - b.sort; });
      state.selectedCode = code;
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
    var doDelete = function () {
      g.codes = g.codes.filter(function (c) {
        return codesToDelete.indexOf(c.code) === -1;
      });
      state.checkedCodes = [];
      state.selectedCode = null;
      refreshAll();
      notify(codesToDelete.length + '개 상세코드가 삭제되었습니다.', 'success');
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

  function exportCsv() {
    var keys = state.searched ? state.filteredKeys || [] : Object.keys(state.groups).sort();
    var lines = [
      'CodeGroup,GroupName,UseYn,Remark,RegId,RegDt,UpdateId,UpdateDt,Code,CodeName,Sort,CodeUseYn,CodeRegId,CodeRegDt,CodeUpdateId,CodeUpdateDt'
    ];
    keys.forEach(function (key) {
      var g = state.groups[key];
      (g.codes || []).forEach(function (c) {
        lines.push(
          [
            key,
            g.name,
            g.useYn || 'Y',
            g.remark || '',
            g.regId || '',
            formatDt(g.regdateDt),
            g.updateId || '',
            formatDt(g.updateDt),
            c.code,
            c.name,
            c.sort,
            c.useYn,
            c.regId || '',
            formatDt(c.regdateDt),
            c.updateId || '',
            formatDt(c.updateDt)
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

  function saveAll() {
    notify('저장 API는 아직 연동되지 않았습니다. 조회는 common_code 테이블을 사용합니다.', 'info');
  }

  function handlePanelClick(e) {
    var btn = e.target.closest('button');
    if (!btn) {
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
    if (!panel) {
      return;
    }

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

  function init() {
    bind();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
