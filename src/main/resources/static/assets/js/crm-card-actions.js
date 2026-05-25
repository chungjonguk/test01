/**
 * CRM 대시보드 — 카드/행 ⋮ 메뉴 View · Export · Remove
 */
(function () {
  'use strict';

  var ROOT_ID = 'crm-dashboard-page';
  var HIGHLIGHT_CLASS = 'crm-widget-highlight';

  function $(id) {
    return document.getElementById(id);
  }

  function root() {
    return $(ROOT_ID);
  }

  function slugify(text) {
    return (text || 'crm-export')
      .trim()
      .replace(/[^\w\u3131-\uD79D\s-]+/g, '')
      .replace(/\s+/g, '-')
      .toLowerCase() || 'crm-export';
  }

  function downloadBlob(filename, mime, content) {
    var blob = new Blob(['\uFEFF' + content], { type: mime });
    var url = URL.createObjectURL(blob);
    var a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.style.display = 'none';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }

  function downloadDataUrl(dataUrl, filename) {
    var a = document.createElement('a');
    a.href = dataUrl;
    a.download = filename;
    a.style.display = 'none';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  }

  function resolveContext(trigger) {
    var tr = trigger.closest('tr');
    if (tr && tr.closest('table')) {
      return { type: 'row', el: tr, scope: tr.closest('.card') || tr };
    }
    var todoRow = trigger.closest('.hover-actions-trigger.btn-reveal-trigger');
    if (todoRow && todoRow.querySelector('label.form-check-label')) {
      return { type: 'todo', el: todoRow, scope: todoRow.closest('.card') || todoRow };
    }
    var col = trigger.closest('[class*="col-"]');
    if (col) {
      var row = col.parentElement;
      if (row && row.classList.contains('row')) {
        var cols = row.querySelectorAll(':scope > [class*="col-"]');
        if (cols.length > 1) {
          return { type: 'column', el: col, scope: col };
        }
      }
    }
    var card = trigger.closest('.card');
    if (card) {
      return { type: 'card', el: card, scope: card };
    }
    return null;
  }

  function getTitle(ctx) {
    if (!ctx) {
      return 'CRM';
    }
    if (ctx.type === 'row') {
      var name = ctx.el.querySelector('.name');
      return name ? name.textContent.trim() : '리드';
    }
    if (ctx.type === 'todo') {
      var label = ctx.el.querySelector('label.form-check-label');
      return label ? label.textContent.trim() : '할 일';
    }
    if (ctx.type === 'column') {
      var h6 = ctx.el.querySelector('h6');
      return h6 ? h6.textContent.trim() : '통계';
    }
    var card = ctx.el;
    var heading = card.querySelector(
      '.card-header .h6, .card-header h6, .card-header h5, .card-title, .card-body > h6'
    );
    return heading ? heading.textContent.trim() : 'CRM 위젯';
  }

  function escapeHtml(s) {
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function buildViewHtml(ctx) {
    var title = getTitle(ctx);
    if (ctx.type === 'row') {
      var name = ctx.el.querySelector('.name');
      var email = ctx.el.querySelector('.email');
      var status = ctx.el.querySelector('.status');
      return (
        '<dl class="row mb-0">' +
        '<dt class="col-sm-3">이름</dt><dd class="col-sm-9">' +
        escapeHtml(name ? name.textContent.trim() : '-') +
        '</dd>' +
        '<dt class="col-sm-3">이메일</dt><dd class="col-sm-9">' +
        escapeHtml(email ? email.textContent.trim() : '-') +
        '</dd>' +
        '<dt class="col-sm-3">상태</dt><dd class="col-sm-9">' +
        escapeHtml(status ? status.textContent.trim() : '-') +
        '</dd></dl>'
      );
    }
    if (ctx.type === 'todo') {
      var label = ctx.el.querySelector('label.form-check-label');
      var checkbox = ctx.el.querySelector('input[type="checkbox"]');
      var done = checkbox && checkbox.checked;
      return (
        '<p class="mb-2"><strong>' +
        escapeHtml(title) +
        '</strong></p>' +
        '<p class="mb-0 text-600">상태: ' +
        (done ? '완료' : '진행 중') +
        '</p>'
      );
    }
    if (ctx.type === 'column') {
      var pct = ctx.el.querySelector('.fs-4, .fs-3, h6.text-primary');
      var note = ctx.el.querySelector('.fs--2');
      return (
        '<p class="mb-2 fs-2 fw-bold text-primary">' +
        escapeHtml(pct ? pct.textContent.trim() : '') +
        '</p>' +
        '<p class="mb-0 text-600">' +
        escapeHtml(note ? note.textContent.trim() : '') +
        '</p>'
      );
    }
    var table = ctx.el.querySelector('table');
    if (table) {
      var clone = table.cloneNode(true);
      clone.querySelectorAll('.dropdown, .hover-actions, .btn-reveal-trigger').forEach(function (n) {
        n.remove();
      });
      return '<div class="table-responsive">' + clone.outerHTML + '</div>';
    }
    var chart = ctx.el.querySelector('[class*="echart"]');
    if (chart) {
      return (
        '<p class="mb-2">' +
        escapeHtml(title) +
        ' 차트가 표시됩니다.</p>' +
        '<p class="mb-0 text-600 fs--1">Export 메뉴로 PNG 이미지를 저장할 수 있습니다.</p>'
      );
    }
    var body = ctx.el.querySelector('.card-body');
    if (body) {
      var text = body.innerText.replace(/\s+/g, ' ').trim();
      if (text.length > 1200) {
        text = text.slice(0, 1200) + '…';
      }
      return '<p class="mb-0 text-700" style="white-space:pre-wrap;">' + escapeHtml(text) + '</p>';
    }
    return '<p class="mb-0 text-600">표시할 내용이 없습니다.</p>';
  }

  function showDetailModal(ctx) {
    var modalEl = $('crmDetailModal');
    var titleEl = $('crmDetailModalTitle');
    var bodyEl = $('crmDetailModalBody');
    if (!modalEl || !titleEl || !bodyEl) {
      return;
    }
    var title = getTitle(ctx);
    titleEl.textContent = title;
    bodyEl.innerHTML = buildViewHtml(ctx);
    if (window.bootstrap && window.bootstrap.Modal) {
      var instance = window.bootstrap.Modal.getOrCreateInstance(modalEl);
      instance.show();
    } else {
      modalEl.classList.add('show');
      modalEl.style.display = 'block';
    }
  }

  function highlightScope(ctx) {
    var target = ctx.type === 'column' ? ctx.el : ctx.el;
    target.classList.add(HIGHLIGHT_CLASS);
    target.style.transition = 'box-shadow 0.3s ease';
    target.style.boxShadow = '0 0 0 3px rgba(44, 123, 229, 0.45)';
    target.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    setTimeout(function () {
      target.classList.remove(HIGHLIGHT_CLASS);
      target.style.boxShadow = '';
    }, 1600);
  }

  function findChartInScope(scope) {
    if (!window.echarts) {
      return null;
    }
    var nodes = scope.querySelectorAll('[class*="echart"]');
    for (var i = 0; i < nodes.length; i++) {
      var inst = window.echarts.getInstanceByDom(nodes[i]);
      if (inst) {
        return { el: nodes[i], chart: inst };
      }
    }
    return null;
  }

  function tableToCsv(table) {
    var lines = [];
    table.querySelectorAll('tr').forEach(function (tr) {
      var cells = [];
      tr.querySelectorAll('th, td').forEach(function (td) {
        if (td.querySelector('.dropdown, .hover-actions')) {
          return;
        }
        cells.push('"' + (td.innerText || '').trim().replace(/"/g, '""') + '"');
      });
      if (cells.length) {
        lines.push(cells.join(','));
      }
    });
    return lines.join('\r\n');
  }

  function collectTextLines(ctx) {
    var lines = [];
    var title = getTitle(ctx);
    lines.push('항목,' + title);
    if (ctx.type === 'row') {
      var nameEl = ctx.el.querySelector('.name');
      var emailEl = ctx.el.querySelector('.email');
      var statusEl = ctx.el.querySelector('.status');
      lines.push('이름,"' + (nameEl ? nameEl.textContent : '').trim().replace(/"/g, '""') + '"');
      lines.push('이메일,"' + (emailEl ? emailEl.textContent : '').trim().replace(/"/g, '""') + '"');
      lines.push('상태,"' + (statusEl ? statusEl.textContent : '').trim().replace(/"/g, '""') + '"');
      return lines.join('\r\n');
    }
    if (ctx.type === 'todo') {
      var checkbox = ctx.el.querySelector('input[type="checkbox"]');
      lines.push('상태,' + (checkbox && checkbox.checked ? '완료' : '진행 중'));
      return lines.join('\r\n');
    }
    if (ctx.type === 'column') {
      ctx.el.querySelectorAll('p, h6, .fs--2').forEach(function (p) {
        var t = (p.textContent || '').trim();
        if (t) {
          lines.push('"' + t.replace(/"/g, '""') + '"');
        }
      });
      return lines.join('\r\n');
    }
    var table = ctx.el.querySelector('table');
    if (table) {
      return tableToCsv(table);
    }
    var body = ctx.el.querySelector('.card-body');
    if (body) {
      lines.push('내용,"' + body.innerText.trim().replace(/\s+/g, ' ').replace(/"/g, '""') + '"');
    }
    return lines.join('\r\n');
  }

  function exportScope(ctx) {
    var title = getTitle(ctx);
    var stamp = new Date().toISOString().slice(0, 10);
    var base = slugify(title) + '-' + stamp;
    var chartInfo = findChartInScope(ctx.scope);
    if (chartInfo) {
      try {
        var url = chartInfo.chart.getDataURL({
          type: 'png',
          pixelRatio: 2,
          backgroundColor: '#fff'
        });
        downloadDataUrl(url, base + '.png');
        return;
      } catch (e) {
        /* fall through to CSV */
      }
    }
    var csv = collectTextLines(ctx);
    if (!csv) {
      window.alert('보낼 데이터가 없습니다.');
      return;
    }
    downloadBlob(base + '.csv', 'text/csv;charset=utf-8;', csv);
  }

  function hideElement(el) {
    el.style.transition = 'opacity 0.25s ease';
    el.style.opacity = '0';
    setTimeout(function () {
      el.classList.add('d-none');
      el.setAttribute('aria-hidden', 'true');
    }, 260);
  }

  function removeScope(ctx) {
    var title = getTitle(ctx);
    var msg =
      ctx.type === 'row' || ctx.type === 'todo'
        ? '"' + title + '" 항목을 목록에서 제거할까요?'
        : '"' + title + '" 영역을 화면에서 숨길까요? (새로고침 시 다시 표시됩니다)';
    if (!window.confirm(msg)) {
      return;
    }
    if (ctx.type === 'row' || ctx.type === 'todo') {
      hideElement(ctx.el);
      return;
    }
    if (ctx.type === 'column') {
      hideElement(ctx.el);
      return;
    }
    var colWrap = ctx.el.closest('[class*="col-"]');
    if (colWrap && colWrap.parentElement && colWrap.parentElement.classList.contains('row')) {
      hideElement(colWrap);
    } else {
      hideElement(ctx.el);
    }
  }

  function closeDropdown(trigger) {
    var dropdown = trigger.closest('.dropdown');
    var toggle = dropdown ? dropdown.querySelector('[data-bs-toggle="dropdown"]') : null;
    if (!toggle || !window.bootstrap) {
      return;
    }
    var dd = window.bootstrap.Dropdown.getInstance(toggle);
    if (dd) {
      dd.hide();
    }
  }

  function onMenuClick(e) {
    var actionEl = e.target.closest('[data-crm-action]');
    if (!actionEl) {
      return;
    }
    var page = root();
    if (!page || !page.contains(actionEl)) {
      return;
    }
    e.preventDefault();
    e.stopPropagation();
    closeDropdown(actionEl);
    var action = actionEl.getAttribute('data-crm-action');
    var ctx = resolveContext(actionEl);
    if (!ctx) {
      return;
    }
    if (action === 'view') {
      highlightScope(ctx);
      showDetailModal(ctx);
    } else if (action === 'export') {
      exportScope(ctx);
    } else if (action === 'remove') {
      removeScope(ctx);
    }
  }

  function injectHighlightStyle() {
    if (document.getElementById('crm-card-actions-style')) {
      return;
    }
    var style = document.createElement('style');
    style.id = 'crm-card-actions-style';
    style.textContent = '.crm-widget-highlight{position:relative;z-index:1;}';
    document.head.appendChild(style);
  }

  function initCardActions() {
    if (!root()) {
      return;
    }
    injectHighlightStyle();
    document.body.addEventListener('click', onMenuClick, true);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initCardActions);
  } else {
    initCardActions();
  }
})();
