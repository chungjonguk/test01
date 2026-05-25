/**
 * CRM 대시보드 — 지도·차트 리사이즈 (world.js 로드 후 레이아웃 보정)
 */
(function () {
  'use strict';

  function relayoutCrmCharts() {
    if (!window.echarts) {
      return;
    }
    document.querySelectorAll('.echart-location-by-session-map, .echart-crm-revenue').forEach(function (el) {
      var chart = window.echarts.getInstanceByDom(el);
      if (chart) {
        chart.resize();
      }
    });
  }

  function onReady() {
    relayoutCrmCharts();
    setTimeout(relayoutCrmCharts, 300);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', onReady);
  } else {
    onReady();
  }
  window.addEventListener('load', onReady);
})();
