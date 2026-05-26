/**
 * 테이블 시퀀스 관리 — sys_table_sequence 조회
 */
(function () {
  'use strict';

  var state = { loading: false, syncing: false };
  var seqGridView = null;
  var C = window.PrintMallCommon || {};

  function $(id) {
    return document.getElementById(id);
  }

  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/"/g, '&quot;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  function escapeAttr(value) {
    return escapeHtml(value).replace(/'/g, '&#39;');
  }

  function truncateCell(text) {
    var value = text == null ? '' : String(text);
    return (
      '<span class="text-truncate d-inline-block" style="max-width:12rem" title="' +
      escapeAttr(value) +
      '">' +
      escapeHtml(value || '—') +
      '</span>'
    );
  }

  var formatDt = C.formatDt;

  function useYnBadge(useYn) {
    var yn = (useYn || 'Y').toUpperCase();
    if (yn === 'Y') {
      return '<span class="badge badge-soft-success">Y</span>';
    }
    return '<span class="badge badge-soft-secondary">N</span>';
  }

  function getSearchParams() {
    var params = new URLSearchParams();
    var seqName = $('search-seq-name');
    var tableName = $('search-table-name');
    var useYn = $('search-seq-use-yn');
    if (seqName && seqName.value.trim()) {
      params.set('seqName', seqName.value.trim());
    }
    if (tableName && tableName.value.trim()) {
      params.set('tableName', tableName.value.trim());
    }
    if (useYn && useYn.value) {
      params.set('useYn', useYn.value);
    }
    return params;
  }

  function rowNo(meta, index) {
    if (C.gridRowNo) {
      return C.gridRowNo(meta, index);
    }
    return (meta && meta.start ? meta.start - 1 : 0) + index + 1;
  }

  function getSeqGridView() {
    if (!seqGridView && C.createGridViewPager) {
      seqGridView = C.createGridViewPager({
        pagerRootId: 'seq-grid-pager',
        containerId: 'seq-grid-body',
        countElId: 'seq-search-result-count',
        emptyColspan: 9,
        renderPage: function (rows, meta) {
          var tbody = $('seq-grid-body');
          if (!tbody || !rows.length) {
            return;
          }
          tbody.innerHTML = rows
            .map(function (row, index) {
              return (
                '<tr>' +
                '<td class="ps-3 align-middle text-center">' +
                rowNo(meta, index) +
                '</td>' +
                '<td class="align-middle"><code class="fs--2">' +
                escapeHtml(row.seqName) +
                '</code></td>' +
                '<td class="align-middle">' +
                escapeHtml(row.tableName) +
                '</td>' +
                '<td class="align-middle"><code class="fs--2">' +
                escapeHtml(row.columnName) +
                '</code></td>' +
                '<td class="align-middle text-end font-monospace">' +
                escapeHtml(row.nextVal) +
                '</td>' +
                '<td class="align-middle text-center d-none d-md-table-cell">' +
                escapeHtml(row.incrementBy != null ? row.incrementBy : '1') +
                '</td>' +
                '<td class="align-middle d-none d-lg-table-cell">' +
                truncateCell(row.description) +
                '</td>' +
                '<td class="align-middle text-center">' +
                useYnBadge(row.useYn) +
                '</td>' +
                '<td class="align-middle fs--2 d-none d-xl-table-cell">' +
                escapeHtml(formatDt ? formatDt(row.updateDt) : row.updateDt || '—') +
                '</td>' +
                '</tr>'
              );
            })
            .join('');
        }
      });
    }
    return seqGridView;
  }

  function renderGrid(sequences) {
    var view = getSeqGridView();
    if (view) {
      view.setData(sequences || []);
    }
  }

  function loadSequences() {
    if (state.loading) {
      return;
    }
    state.loading = true;
    var tbody = $('seq-grid-body');
    if (tbody) {
      tbody.innerHTML =
        '<tr><td colspan="9" class="text-center text-600 py-4">조회 중...</td></tr>';
    }
    fetch('/api/admin/table-sequences?' + getSearchParams().toString(), {
      headers: { Accept: 'application/json' }
    })
      .then(function (res) {
        if (!res.ok) {
          throw new Error('조회 실패');
        }
        return res.json();
      })
      .then(function (data) {
        renderGrid(data.sequences || []);
      })
      .catch(function () {
        if (tbody) {
          tbody.innerHTML =
            '<tr><td colspan="9" class="text-center text-danger py-4">조회 중 오류가 발생했습니다.</td></tr>';
        }
      })
      .finally(function () {
        state.loading = false;
      });
  }

  function syncFromCatalog() {
    if (state.syncing) {
      return;
    }
    if (!window.confirm('카탈로그에 정의된 시퀀스를 등록하고, 각 테이블 MAX(PK)로 next_val을 동기화할까요?')) {
      return;
    }
    state.syncing = true;
    var btn = $('btn-sync-sequences');
    if (btn) {
      btn.disabled = true;
    }
    fetch('/api/admin/table-sequences/sync', { method: 'POST', headers: { Accept: 'application/json' } })
      .then(function (res) {
        if (!res.ok) {
          throw new Error('동기화 실패');
        }
        return res.json();
      })
      .then(function (data) {
        if (data && data.message) {
          window.alert(data.message);
        }
        loadSequences();
      })
      .catch(function () {
        window.alert('카탈로그 동기화 중 오류가 발생했습니다.');
      })
      .finally(function () {
        state.syncing = false;
        if (btn) {
          btn.disabled = false;
        }
      });
  }

  function resetSearch() {
    var seqName = $('search-seq-name');
    var tableName = $('search-table-name');
    var useYn = $('search-seq-use-yn');
    if (seqName) {
      seqName.value = '';
    }
    if (tableName) {
      tableName.value = '';
    }
    if (useYn) {
      useYn.value = '';
    }
  }

  function bindEvents() {
    var form = $('seq-search-form');
    if (form) {
      form.addEventListener('submit', function (e) {
        e.preventDefault();
        loadSequences();
      });
    }
    var resetBtn = $('btn-reset-seq-search');
    if (resetBtn) {
      resetBtn.addEventListener('click', function () {
        resetSearch();
        loadSequences();
      });
    }
    var syncBtn = $('btn-sync-sequences');
    if (syncBtn) {
      syncBtn.addEventListener('click', syncFromCatalog);
    }
  }

  window.TableSequenceMgmtInit = function () {
    bindEvents();
    loadSequences();
  };
})();
