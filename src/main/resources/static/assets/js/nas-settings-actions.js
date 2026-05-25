/**
 * NAS 설정 화면 — /admin/nas
 * @module nas-settings-actions
 */
(function () {
    'use strict';

    var C = window.PrintMallCommon;
    var $ = C ? C.$ : function (id) { return document.getElementById(id); };
    var escapeHtml = C ? C.escapeHtml : function (s) { return String(s); };
    var fetchJson = C ? C.fetchJson : function (url) { return fetch(url).then(function (r) { return r.json(); }); };

    function formatMb(mb) {
        return (mb != null ? mb : 0).toFixed(2) + ' MB';
    }

    function renderUsage(data) {
        var summaryEl = $('nas-settings-usage-summary');
        var progressEl = $('nas-settings-usage-progress');
        var legendEl = $('nas-settings-usage-legend');
        if (!summaryEl) {
            return;
        }
        summaryEl.innerHTML = '<strong class="text-dark">' + formatMb(data.usedMb) + '</strong> / '
            + (data.quotaGb || 2) + ' GB 사용';
        if (progressEl && data.categories) {
            progressEl.innerHTML = '';
            data.categories.forEach(function (cat) {
                if (!cat.percentOfQuota || cat.percentOfQuota <= 0) {
                    return;
                }
                var bar = document.createElement('div');
                bar.className = 'progress-bar ' + (cat.barClass || 'bg-200');
                bar.setAttribute('role', 'progressbar');
                bar.style.width = cat.percentOfQuota + '%';
                progressEl.appendChild(bar);
            });
        }
        if (legendEl && data.categories) {
            legendEl.innerHTML = '';
            data.categories.forEach(function (cat) {
                var col = document.createElement('div');
                col.className = 'col-auto d-flex align-items-center pe-3';
                col.innerHTML = '<span class="dot ' + (cat.dotClass || 'bg-200') + '"></span>'
                    + '<span>' + escapeHtml(cat.label || cat.code) + '</span>'
                    + '<span class="d-none d-md-inline-block ms-1">(' + formatMb(cat.mb) + ')</span>';
                legendEl.appendChild(col);
            });
        }
    }

    function renderFolderTable(config) {
        var tbody = $('nas-folder-usage-body');
        if (!tbody || !config || !config.folders) {
            return;
        }
        tbody.innerHTML = config.folders.map(function (row) {
            var mb = row.mb != null ? row.mb : 0;
            return '<tr>'
                + '<td class="ps-3">' + escapeHtml(row.label) + '</td>'
                + '<td><code>' + escapeHtml(row.folder) + '</code></td>'
                + '<td class="text-break"><code class="fs--2">' + escapeHtml(row.path) + '</code></td>'
                + '<td class="text-end pe-3">' + formatMb(mb) + '</td>'
                + '</tr>';
        }).join('');
    }

    function load() {
        Promise.all([
            fetchJson('/api/storage/config'),
            fetchJson('/api/storage/usage')
        ]).then(function (results) {
            renderUsage(results[1]);
            renderFolderTable(results[0]);
        }).catch(function () {
            var tbody = $('nas-folder-usage-body');
            if (tbody) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center text-danger py-3">NAS 정보를 불러오지 못했습니다.</td></tr>';
            }
        });
    }

    function init() {
        load();
        var btn = $('btn-reload-nas-settings');
        if (btn) {
            btn.addEventListener('click', load);
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
