/**
 * 대시보드 구성 — /admin/dashboard-config
 */
(function () {
  'use strict';

  var store = window.DashboardConfigStore;
  if (!store) {
    return;
  }

  var hiddenSet = {};
  var order = [];
  var categoryFilter = '';

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
        timer: 2600,
        timerProgressBar: true
      });
      return;
    }
    window.alert(message);
  }

  function getCatalog() {
    var list = window.__DASHBOARD_WIDGET_CATALOG__;
    return Array.isArray(list) ? list : [];
  }

  function getDefaultIds(catalog) {
    var defaults = window.__DASHBOARD_DEFAULT_IDS__;
    if (Array.isArray(defaults) && defaults.length) {
      return defaults.slice();
    }
    return catalog
      .filter(function (w) {
        return w.defaultEnabled !== false;
      })
      .map(function (w) {
        return w.id;
      });
  }

  function defaultOrderAll(catalog) {
    return catalog
      .slice()
      .sort(function (a, b) {
        return (a.sortOrder || 0) - (b.sortOrder || 0);
      })
      .map(function (w) {
        return w.id;
      });
  }

  function sortedCatalog(catalog) {
    var orderIndex = {};
    order.forEach(function (id, idx) {
      orderIndex[id] = idx;
    });
    return catalog.slice().sort(function (a, b) {
      var ai = orderIndex[a.id];
      var bi = orderIndex[b.id];
      if (ai == null && bi == null) {
        return (a.sortOrder || 0) - (b.sortOrder || 0);
      }
      if (ai == null) {
        return 1;
      }
      if (bi == null) {
        return -1;
      }
      return ai - bi;
    });
  }

  function filterCatalog(catalog) {
    if (!categoryFilter) {
      return catalog;
    }
    return catalog.filter(function (w) {
      return (w.category || '') === categoryFilter;
    });
  }

  function escapeHtml(text) {
    var div = document.createElement('div');
    div.textContent = text == null ? '' : String(text);
    return div.innerHTML;
  }

  function escapeAttr(text) {
    return escapeHtml(text).replace(/"/g, '&quot;');
  }

  function isHidden(id) {
    return !!hiddenSet[id];
  }

  function updateSummary(catalog) {
    var el = $('dashboard-config-summary');
    if (!el) {
      return;
    }
    var hiddenCount = 0;
    catalog.forEach(function (w) {
      if (isHidden(w.id)) {
        hiddenCount += 1;
      }
    });
    el.textContent =
      '표시 ' +
      (catalog.length - hiddenCount) +
      '개 · 숨김 ' +
      hiddenCount +
      '개 · 전체 ' +
      catalog.length +
      '개';
  }

  function renderList(catalog) {
    var tbody = $('dashboard-config-body');
    if (!tbody) {
      return;
    }
    var filtered = filterCatalog(sortedCatalog(catalog));

    if (!filtered.length) {
      tbody.innerHTML =
        '<tr><td colspan="7" class="text-center text-600 py-4">선택한 카테고리에 위젯이 없습니다.</td></tr>';
      updateSummary(catalog);
      return;
    }

    tbody.innerHTML = filtered
      .map(function (w, index) {
        var checked = !isHidden(w.id);
        return (
          '<tr data-widget-id="' +
          escapeAttr(w.id) +
          '">' +
          '<td class="text-center ps-3"><span class="badge badge-soft-secondary">' +
          (index + 1) +
          '</span></td>' +
          '<td><span class="badge badge-soft-info fs--2">' +
          escapeHtml(w.category || '기타') +
          '</span></td>' +
          '<td><code class="fs--1">' +
          escapeHtml(w.id) +
          '</code></td>' +
          '<td class="fw-semi-bold">' +
          escapeHtml(w.label) +
          '</td>' +
          '<td class="text-600 d-none d-lg-table-cell">' +
          escapeHtml(w.description || '') +
          '</td>' +
          '<td class="text-center">' +
          '<div class="form-check form-switch d-inline-block mb-0">' +
          '<input class="form-check-input dashboard-widget-visible" type="checkbox" data-widget-id="' +
          escapeAttr(w.id) +
          '"' +
          (checked ? ' checked' : '') +
          ' aria-label="표시"/>' +
          '</div></td>' +
          '<td class="text-end pe-3 text-nowrap">' +
          '<button type="button" class="btn btn-link btn-sm p-0 me-1 btn-move-up" title="위로"><span class="fas fa-chevron-up"></span></button>' +
          '<button type="button" class="btn btn-link btn-sm p-0 btn-move-down" title="아래로"><span class="fas fa-chevron-down"></span></button>' +
          '</td></tr>'
        );
      })
      .join('');

    updateSummary(catalog);
  }

  function buildCategoryFilter(catalog) {
    var select = $('dashboard-config-category');
    if (!select) {
      return;
    }
    var categories = [];
    catalog.forEach(function (w) {
      var c = w.category || '기타';
      if (categories.indexOf(c) === -1) {
        categories.push(c);
      }
    });
    categories.sort();
    var current = select.value;
    select.innerHTML =
      '<option value="">전체 카테고리</option>' +
      categories
        .map(function (c) {
          return '<option value="' + escapeAttr(c) + '">' + escapeHtml(c) + '</option>';
        })
        .join('');
    select.value = current || '';
  }

  function loadStateFromStore(catalog) {
    var config = store.read();
    hiddenSet = {};
    config.hidden.forEach(function (id) {
      hiddenSet[id] = true;
    });
    order = config.order.length ? config.order.slice() : defaultOrderAll(catalog);
    catalog.forEach(function (w) {
      if (order.indexOf(w.id) === -1) {
        order.push(w.id);
      }
    });
  }

  function persist() {
    var hidden = Object.keys(hiddenSet).filter(function (id) {
      return hiddenSet[id];
    });
    store.write({ version: 1, hidden: hidden, order: order.slice() });
  }

  function moveRow(row, direction) {
    var id = row.getAttribute('data-widget-id');
    if (!id) {
      return;
    }
    var idx = order.indexOf(id);
    if (idx === -1) {
      return;
    }
    var swapIdx = direction < 0 ? idx - 1 : idx + 1;
    if (swapIdx < 0 || swapIdx >= order.length) {
      return;
    }
    var tmp = order[swapIdx];
    order[swapIdx] = order[idx];
    order[idx] = tmp;
  }

  function bind() {
    var catalog = getCatalog();
    loadStateFromStore(catalog);
    buildCategoryFilter(catalog);
    renderList(catalog);

    var categorySelect = $('dashboard-config-category');
    if (categorySelect) {
      categorySelect.addEventListener('change', function () {
        categoryFilter = categorySelect.value || '';
        renderList(catalog);
      });
    }

    var tbody = $('dashboard-config-body');
    if (tbody) {
      tbody.addEventListener('click', function (e) {
        var up = e.target.closest('.btn-move-up');
        var down = e.target.closest('.btn-move-down');
        var row = e.target.closest('tr[data-widget-id]');
        if (!row) {
          return;
        }
        if (up) {
          moveRow(row, -1);
          renderList(catalog);
        }
        if (down) {
          moveRow(row, 1);
          renderList(catalog);
        }
      });

      tbody.addEventListener('change', function (e) {
        var cb = e.target.closest('.dashboard-widget-visible');
        if (!cb) {
          return;
        }
        var id = cb.getAttribute('data-widget-id');
        if (!id) {
          return;
        }
        if (cb.checked) {
          delete hiddenSet[id];
        } else {
          hiddenSet[id] = true;
        }
        updateSummary(catalog);
      });
    }

    var saveBtn = $('btn-save-dashboard-config');
    if (saveBtn) {
      saveBtn.addEventListener('click', function () {
        persist();
        localStorage.removeItem('dashboardRemovedWidgets');
        loadStateFromStore(catalog);
        notify('저장되었습니다. 홈(/)을 Ctrl+F5로 새로고침하면 순서·표시가 반영됩니다.', 'success');
        renderList(catalog);
      });
    }

    var resetBtn = $('btn-reset-dashboard-config');
    if (resetBtn) {
      resetBtn.addEventListener('click', function () {
        if (!window.confirm('기본 위젯만 표시하고 순서를 카탈로그 기본값으로 초기화할까요?')) {
          return;
        }
        store.reset();
        var defaultIds = getDefaultIds(catalog);
        hiddenSet = {};
        catalog.forEach(function (w) {
          if (defaultIds.indexOf(w.id) === -1) {
            hiddenSet[w.id] = true;
          }
        });
        order = defaultOrderAll(catalog);
        persist();
        notify('기본 구성으로 초기화되었습니다.', 'info');
        renderList(catalog);
      });
    }

    var selectAll = $('btn-dashboard-select-all');
    if (selectAll) {
      selectAll.addEventListener('click', function () {
        catalog.forEach(function (w) {
          delete hiddenSet[w.id];
        });
        renderList(catalog);
      });
    }

    var selectNone = $('btn-dashboard-select-none');
    if (selectNone) {
      selectNone.addEventListener('click', function () {
        catalog.forEach(function (w) {
          hiddenSet[w.id] = true;
        });
        renderList(catalog);
      });
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bind);
  } else {
    bind();
  }
})();
