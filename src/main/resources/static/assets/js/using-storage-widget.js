/**
 * Using Storage 위젯 — NAS 폴더 실사용량 (/api/storage/usage)
 */
(function () {
    'use strict';

    var card = document.querySelector('[data-dashboard-widget="using-storage"]');
    if (!card) {
        return;
    }

    var summaryEl = card.querySelector('[data-nas-usage-summary]');
    var progressEl = card.querySelector('[data-nas-usage-progress]');
    var legendEl = card.querySelector('[data-nas-usage-legend]');

    function formatMb(mb) {
        return mb.toFixed(2) + ' MB';
    }

    function render(data) {
        if (!data || summaryEl == null) {
            return;
        }
        var usedMb = data.usedMb != null ? data.usedMb : 0;
        var quotaMb = data.quotaMb != null ? data.quotaMb : 2048;
        summaryEl.innerHTML = '<strong class="text-dark">' + formatMb(usedMb) + '</strong> of '
            + (data.quotaGb || 2) + ' GB used';
        card.setAttribute('title', 'NAS: ' + (data.uploadRoot || ''));

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
                bar.setAttribute('aria-valuenow', String(cat.percentOfQuota));
                bar.setAttribute('aria-valuemin', '0');
                bar.setAttribute('aria-valuemax', '100');
                progressEl.appendChild(bar);
            });
        }

        if (legendEl && data.categories) {
            legendEl.innerHTML = '';
            data.categories.forEach(function (cat) {
                var col = document.createElement('div');
                col.className = 'col-auto d-flex align-items-center pe-3';
                var mb = cat.mb != null ? cat.mb : 0;
                col.innerHTML = '<span class="dot ' + (cat.dotClass || 'bg-200') + '"></span>'
                    + '<span>' + (cat.label || cat.code) + '</span>'
                    + '<span class="d-none d-md-inline-block d-lg-none d-xxl-inline-block">('
                    + formatMb(mb) + ')</span>';
                legendEl.appendChild(col);
            });
        }
    }

    fetch('/api/storage/usage')
        .then(function (res) {
            if (!res.ok) {
                throw new Error('HTTP ' + res.status);
            }
            return res.json();
        })
        .then(render)
        .catch(function () {
            if (summaryEl) {
                summaryEl.textContent = 'NAS 사용량을 불러오지 못했습니다.';
            }
        });
})();
