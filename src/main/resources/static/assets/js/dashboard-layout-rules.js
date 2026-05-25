/**
 * 홈 대시보드 위젯 배치 규칙 (구성 화면 검증 · 렌더러 공용)
 */
(function (global) {
  'use strict';

  var COL_COMPACT = 'col-6 col-md-4 col-xl-3';
  var COL_HALF = 'col-12 col-md-6';
  var COL_WIDE = 'col-12';
  var COL_FULL = 'col-12';

  var TYPE_LABELS = {
    compact: '작은 카드',
    half: '중간 카드',
    wide: '넓은 카드',
    full: '전체 너비'
  };

  function gridSpanFromMeta(meta) {
    var colClass = (meta && meta.colClass) || '';
    if (/\bcol-12\b/.test(colClass) && !/\bcol-(sm|md|lg|xl|xxl)-\d/.test(colClass)) {
      return 12;
    }
    var prefixes = ['xxl', 'xl', 'lg', 'md', 'sm'];
    var i;
    for (i = 0; i < prefixes.length; i++) {
      var re = new RegExp('\\bcol-' + prefixes[i] + '-(\\d{1,2})\\b');
      var m = colClass.match(re);
      if (m) {
        return Math.min(12, Math.max(3, parseInt(m[1], 10)));
      }
    }
    return 3;
  }

  function bandTypeFromSpan(span) {
    if (span >= 12) {
      return 'full';
    }
    if (span >= 8) {
      return 'wide';
    }
    if (span >= 5) {
      return 'half';
    }
    return 'compact';
  }

  function bandLimit(type) {
    if (type === 'compact') {
      return 4;
    }
    if (type === 'half') {
      return 2;
    }
    return 1;
  }

  function bandTypeLabel(type) {
    return TYPE_LABELS[type] || type;
  }

  function colClassForBandType(type) {
    if (type === 'compact') {
      return COL_COMPACT;
    }
    if (type === 'half') {
      return COL_HALF;
    }
    if (type === 'wide') {
      return COL_WIDE;
    }
    return COL_FULL;
  }

  function catalogById(catalog) {
    var map = {};
    catalog.forEach(function (w) {
      map[w.id] = w;
    });
    return map;
  }

  function widgetBandType(widgetId, byId) {
    var meta = byId[widgetId];
    if (!meta) {
      return 'compact';
    }
    return bandTypeFromSpan(gridSpanFromMeta(meta));
  }

  function widgetLabel(byId, widgetId) {
    var meta = byId[widgetId];
    return (meta && meta.label) || widgetId;
  }

  var BAND_TYPE_SEQUENCE = ['compact', 'half', 'wide', 'full'];

  function hiddenToSet(hidden) {
    var hiddenSet = {};
    (hidden || []).forEach(function (id) {
      hiddenSet[id] = true;
    });
    return hiddenSet;
  }

  /**
   * 표시(체크)된 위젯만 크기별·sortOrder 기준으로 자동 정렬한 전체 order
   */
  function autoArrangeOrder(catalog, hidden) {
    var hiddenSet = hiddenToSet(hidden);
    var byId = catalogById(catalog);
    var buckets = { compact: [], half: [], wide: [], full: [] };
    var hiddenList = [];

    catalog.forEach(function (w) {
      if (hiddenSet[w.id]) {
        hiddenList.push(w);
        return;
      }
      var type = widgetBandType(w.id, byId);
      buckets[type].push(w);
    });

    BAND_TYPE_SEQUENCE.forEach(function (type) {
      buckets[type].sort(function (a, b) {
        return (a.sortOrder || 0) - (b.sortOrder || 0);
      });
    });

    hiddenList.sort(function (a, b) {
      return (a.sortOrder || 0) - (b.sortOrder || 0);
    });

    var arranged = [];
    BAND_TYPE_SEQUENCE.forEach(function (type) {
      buckets[type].forEach(function (w) {
        arranged.push(w.id);
      });
    });
    hiddenList.forEach(function (w) {
      arranged.push(w.id);
    });
    return arranged;
  }

  /** 표시 설정된 위젯 ID 목록 (자동 배치 순서) */
  function getVisibleIds(order, hidden, catalog) {
    var hiddenSet = hiddenToSet(hidden);
    return autoArrangeOrder(catalog, hidden).filter(function (id) {
      return !hiddenSet[id];
    });
  }

  /** 렌더·검증 공용 밴드 시뮬레이션 */
  function simulateBands(visibleIds, catalog) {
    var byId = catalogById(catalog);
    var bands = [];
    var current = null;

    function flush() {
      current = null;
    }

    function pushSolo(widgetId, type) {
      flush();
      bands.push({ type: type, widgetIds: [widgetId] });
    }

    visibleIds.forEach(function (widgetId) {
      var type = widgetBandType(widgetId, byId);
      if (type === 'wide' || type === 'full') {
        pushSolo(widgetId, type);
        return;
      }
      if (!current || current.type !== type || current.widgetIds.length >= bandLimit(type)) {
        flush();
        current = { type: type, widgetIds: [] };
        bands.push(current);
      }
      current.widgetIds.push(widgetId);
    });

    return bands;
  }

  /**
   * @param {{ order: string[], hidden: string[], catalog: object[] }} input
   * @returns {{ valid: boolean, errors: string[], warnings: string[], bands: object[] }}
   */
  function validate(input) {
    var hidden = input.hidden || [];
    var catalog = input.catalog || [];
    var errors = [];
    var visibleIds = getVisibleIds(null, hidden, catalog);

    if (!visibleIds.length) {
      errors.push('표시할 위젯을 1개 이상 선택하세요.');
      return { valid: false, errors: errors, warnings: [], bands: [] };
    }

    return {
      valid: true,
      errors: errors,
      warnings: [],
      bands: simulateBands(visibleIds, catalog)
    };
  }

  global.DashboardLayoutRules = {
    gridSpanFromMeta: gridSpanFromMeta,
    bandTypeFromSpan: bandTypeFromSpan,
    bandLimit: bandLimit,
    bandTypeLabel: bandTypeLabel,
    colClassForBandType: colClassForBandType,
    autoArrangeOrder: autoArrangeOrder,
    getVisibleIds: getVisibleIds,
    simulateBands: simulateBands,
    validate: validate
  };
})(typeof window !== 'undefined' ? window : globalThis);
