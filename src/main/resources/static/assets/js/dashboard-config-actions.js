/**
 * 대시보드 구성 — 표시 위젯 기준 자동 배치
 */
(function () {
  'use strict';

  var store = window.DashboardConfigStore;
  var Rules = window.DashboardLayoutRules;
  if (!store || !Rules) {
    return;
  }

  var hiddenSet = {};
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
        timer: 3200,
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

  function hiddenList() {
    return Object.keys(hiddenSet).filter(function (id) {
      return hiddenSet[id];
    });
  }

  function syncAutoOrder(catalog) {
    return Rules.autoArrangeOrder(catalog, hiddenList());
  }

  function validateCurrent(catalog) {
    return Rules.validate({
      hidden: hiddenList(),
      catalog: catalog
    });
  }

  function sortedCatalog(catalog) {
    var order = syncAutoOrder(catalog);
    var orderIndex = {};
    order.forEach(function (id, idx) {
      orderIndex[id] = idx;
    });
    return catalog.slice().sort(function (a, b) {
      var ai = orderIndex[a.id];
      var bi = orderIndex[b.id];
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
    var hiddenCount = hiddenList().length;
    var visibleCount = catalog.length - hiddenCount;
    el.textContent =
      '표시 ' + visibleCount + '개 · 숨김 ' + hiddenCount + '개 · 전체 ' + catalog.length + '개 (자동 배치)';
  }

  function updateLayoutMessage(catalog) {
    var box = $('dashboard-config-layout-message');
    var saveBtn = $('btn-save-dashboard-config');
    if (!box) {
      return;
    }

    var result = validateCurrent(catalog);
    var html = '';

    if (result.errors.length) {
      html +=
        '<div class="alert alert-danger py-2 mb-0" role="alert"><strong>저장할 수 없음</strong><ul class="mb-0 ps-3">';
      result.errors.forEach(function (msg) {
        html += '<li>' + escapeHtml(msg) + '</li>';
      });
      html += '</ul></div>';
    } else if (result.bands.length) {
      html =
        '<div class="alert alert-success py-2 mb-0" role="alert">표시된 ' +
        Rules.getVisibleIds(null, hiddenList(), catalog).length +
        '개 위젯이 크기별로 자동 배치됩니다. (홈 화면 ' +
        result.bands.length +
        '줄)</div>';
    } else {
      html =
        '<div class="alert alert-secondary py-2 mb-0" role="alert">표시할 위젯을 선택하면 자동으로 배치됩니다.</div>';
    }

    box.innerHTML = html;
    if (saveBtn) {
      saveBtn.disabled = !result.valid;
    }
  }

  function renderList(catalog) {
    var tbody = $('dashboard-config-body');
    if (!tbody) {
      return;
    }
    var filtered = filterCatalog(sortedCatalog(catalog));
    var visibleOrder = Rules.getVisibleIds(null, hiddenList(), catalog);
    var visibleRank = {};
    visibleOrder.forEach(function (id, idx) {
      visibleRank[id] = idx + 1;
    });

    if (!filtered.length) {
      tbody.innerHTML =
        '<tr><td colspan="6" class="text-center text-600 py-4">선택한 카테고리에 위젯이 없습니다.</td></tr>';
      updateSummary(catalog);
      updateLayoutMessage(catalog);
      return;
    }

    tbody.innerHTML = filtered
      .map(function (w) {
        var checked = !isHidden(w.id);
        var rank = visibleRank[w.id];
        return (
          '<tr data-widget-id="' +
          escapeAttr(w.id) +
          '">' +
          '<td class="text-center ps-3">' +
          (checked
            ? '<span class="badge badge-soft-primary" title="자동 배치 순서">' + rank + '</span>'
            : '<span class="text-400">-</span>') +
          '</td>' +
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
          '<td class="text-center pe-3">' +
          '<div class="form-check form-switch d-inline-block mb-0">' +
          '<input class="form-check-input dashboard-widget-visible" type="checkbox" data-widget-id="' +
          escapeAttr(w.id) +
          '"' +
          (checked ? ' checked' : '') +
          ' aria-label="표시"/>' +
          '</div></td>' +
          '</tr>'
        );
      })
      .join('');

    updateSummary(catalog);
    updateLayoutMessage(catalog);
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

  function loadHiddenFromStore() {
    var config = store.read();
    hiddenSet = {};
    (config.hidden || []).forEach(function (id) {
      hiddenSet[id] = true;
    });
  }

  function buildPayload(catalog) {
    return {
      version: store.VERSION || 2,
      hidden: hiddenList(),
      order: syncAutoOrder(catalog)
    };
  }

  function persist(catalog) {
    return store.write(buildPayload(catalog));
  }

  function switchCompany(catalog, nextCompanyId) {
    return persist(catalog)
      .then(function () {
        return store.loadFromServer(nextCompanyId);
      })
      .then(function () {
        loadHiddenFromStore();
        renderList(catalog);
      });
  }

  function bind() {
    var catalog = getCatalog();
    loadHiddenFromStore();
    buildCategoryFilter(catalog);
    renderList(catalog);

    var companySelect = $('dashboard-company-select');
    if (companySelect) {
      companySelect.addEventListener('change', function () {
        var nextId = companySelect.value ? parseInt(companySelect.value, 10) : null;
        if (!nextId) {
          return;
        }
        switchCompany(catalog, nextId).catch(function () {
          notify('업체 구성을 불러오지 못했습니다.', 'error');
        });
      });
    }

    var categorySelect = $('dashboard-config-category');
    if (categorySelect) {
      categorySelect.addEventListener('change', function () {
        categoryFilter = categorySelect.value || '';
        renderList(catalog);
      });
    }

    var tbody = $('dashboard-config-body');
    if (tbody) {
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
        renderList(catalog);
      });
    }

    var saveBtn = $('btn-save-dashboard-config');
    if (saveBtn) {
      saveBtn.addEventListener('click', function () {
        var check = validateCurrent(catalog);
        if (!check.valid) {
          notify(check.errors[0], 'error');
          updateLayoutMessage(catalog);
          return;
        }
        persist(catalog)
          .then(function () {
            localStorage.removeItem('dashboardRemovedWidgets');
            loadHiddenFromStore();
            notify(
              '저장되었습니다. 선택 업체 대시보드에 반영됩니다. 홈(/)을 Ctrl+F5로 새로고침하세요.',
              'success'
            );
            renderList(catalog);
          })
          .catch(function () {
            notify('저장에 실패했습니다.', 'error');
          });
      });
    }

    var resetBtn = $('btn-reset-dashboard-config');
    if (resetBtn) {
      resetBtn.addEventListener('click', function () {
        if (!window.confirm('이 업체를 기본 위젯 구성으로 초기화할까요?')) {
          return;
        }
        var defaultIds = getDefaultIds(catalog);
        hiddenSet = {};
        catalog.forEach(function (w) {
          if (defaultIds.indexOf(w.id) === -1) {
            hiddenSet[w.id] = true;
          }
        });
        persist(catalog)
          .then(function () {
            notify('기본 구성으로 초기화되었습니다.', 'info');
            renderList(catalog);
          })
          .catch(function () {
            notify('초기화 저장에 실패했습니다.', 'error');
          });
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
