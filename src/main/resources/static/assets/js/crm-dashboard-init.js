/**
 * CRM 대시보드 — 날짜 선택·지도 줌/리셋·차트 리사이즈
 */
(function () {
  'use strict';

  var ROOT_ID = 'crm-dashboard-page';
  var MAP_SEL = '.echart-location-by-session-map';
  var MAP_HEIGHT = 302;
  var CRM_CHART_SEL =
    MAP_SEL +
    ', .echart-crm-revenue, .echart-crm-users, .echart-crm-deals, .echart-crm-profit, .echart-crm-statistics';
  var datePickerReady = false;

  function pageRoot() {
    return document.getElementById(ROOT_ID);
  }

  function removeAllFlatpickrCalendars() {
    document.querySelectorAll('.flatpickr-calendar').forEach(function (cal) {
      if (cal.parentNode) {
        cal.parentNode.removeChild(cal);
      }
    });
  }

  function initDateRangePicker() {
    if (datePickerReady) {
      return;
    }
    var input = document.getElementById('CRMDateRange');
    if (!input || !window.flatpickr) {
      return;
    }

    if (input._flatpickr) {
      input._flatpickr.destroy();
    }
    removeAllFlatpickrCalendars();

    var options = {
      mode: 'range',
      dateFormat: 'M d',
      disableMobile: true,
      defaultDate: [new Date(2024, 8, 13), new Date(2024, 8, 20)],
      appendTo: document.body,
      position: 'below left',
      static: false,
      clickOpens: true,
      allowInput: false
    };

    var fp;
    try {
      fp = window.flatpickr(input, options);
    } catch (err) {
      console.warn('CRM date range picker init failed:', err);
      removeAllFlatpickrCalendars();
      return;
    }

    if (fp && typeof fp.close === 'function') {
      fp.close();
    }
    datePickerReady = true;
  }

  function getMapChart() {
    if (!window.echarts) {
      return null;
    }
    var el = document.querySelector(MAP_SEL);
    if (!el) {
      return null;
    }
    return window.echarts.getInstanceByDom(el) || null;
  }

  function isMapSeriesValid(chart) {
    var opt = chart.getOption();
    var series = opt && opt.series;
    return !!(series && series[0] && series[0].type === 'map' && series[0].map === 'world');
  }

  function repairMapChart() {
    if (!window.echarts || !window.echarts.getMap('world')) {
      return;
    }
    var el = document.querySelector(MAP_SEL);
    if (!el) {
      return;
    }
    var chart = window.echarts.getInstanceByDom(el);
    if (chart && !isMapSeriesValid(chart)) {
      window.echarts.dispose(el);
    }
  }

  function setMapZoom(chart, zoomLevel) {
    chart.setOption({
      series: [
        {
          zoom: zoomLevel
        }
      ]
    });
  }

  function rebindMapControls() {
    var chart = getMapChart();
    if (!chart || !isMapSeriesValid(chart)) {
      return;
    }

    var selectors = [
      '.location-by-session-map-reset',
      '.location-by-session-map-zoom',
      '.location-by-session-map-zoomOut'
    ];
    var maxZoom = 5;
    var minZoom = 1;
    var zoomLevel = 1;

    selectors.forEach(function (selector) {
      document.querySelectorAll(selector).forEach(function (btn) {
        if (!pageRoot() || !pageRoot().contains(btn)) {
          return;
        }
        var clone = btn.cloneNode(true);
        btn.parentNode.replaceChild(clone, btn);
      });
    });

    var resetBtn = document.querySelector('.location-by-session-map-reset');
    var zoomInBtn = document.querySelector('.location-by-session-map-zoom');
    var zoomOutBtn = document.querySelector('.location-by-session-map-zoomOut');

    if (resetBtn) {
      resetBtn.addEventListener('click', function () {
        zoomLevel = 1;
        chart.dispatchAction({ type: 'restore' });
        setMapZoom(chart, 1);
        resizeMapChart();
      });
    }
    if (zoomInBtn) {
      zoomInBtn.addEventListener('click', function () {
        if (zoomLevel < maxZoom) {
          zoomLevel += 1;
        }
        setMapZoom(chart, zoomLevel);
      });
    }
    if (zoomOutBtn) {
      zoomOutBtn.addEventListener('click', function () {
        if (zoomLevel > minZoom) {
          zoomLevel -= 1;
        }
        setMapZoom(chart, zoomLevel);
      });
    }
  }

  function resizeMapChart() {
    var el = document.querySelector(MAP_SEL);
    var chart = getMapChart();
    if (!el || !chart) {
      return;
    }
    if (el.offsetWidth < 1) {
      return;
    }
    chart.resize({ width: el.clientWidth, height: MAP_HEIGHT });
  }

  function relayoutCrmCharts() {
    if (!window.echarts || !pageRoot()) {
      return;
    }
    pageRoot().querySelectorAll(CRM_CHART_SEL).forEach(function (el) {
      if (el.closest('.tab-pane') && window.getComputedStyle(el.closest('.tab-pane')).display === 'none') {
        return;
      }
      var chart = window.echarts.getInstanceByDom(el);
      if (chart && el.offsetWidth > 0) {
        chart.resize();
      }
    });
    resizeMapChart();
  }

  function patchEchartsResize() {
    if (!pageRoot() || !window.echarts) {
      return;
    }
    window.addEventListener(
      'resize',
      function () {
        relayoutCrmCharts();
      },
      { passive: true }
    );
  }

  function onReady() {
    if (!pageRoot()) {
      return;
    }
    initDateRangePicker();
    repairMapChart();
    relayoutCrmCharts();
    rebindMapControls();
    patchEchartsResize();
    setTimeout(function () {
      repairMapChart();
      relayoutCrmCharts();
      rebindMapControls();
    }, 500);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', onReady);
  } else {
    onReady();
  }
})();
