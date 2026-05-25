/**
 * 카카오 주소 검색 + 지도 + 우편번호/기본/상세 주소 — 화면 공통 폼
 * @module kakao-address-form
 *
 * HTML: th:replace="~{fragments/kakao-address-search :: addressFields('company')}"
 * JS:   var form = PrintMallKakaoAddressForm.create({ idPrefix: 'company', onNotify: fn });
 */
(function () {
  'use strict';

  var instances = {};

  function el(id) {
    return document.getElementById(id);
  }

  function digitsOnly(value) {
    return String(value == null ? '' : value).replace(/\D/g, '');
  }

  function resolveIds(options) {
    var p = options.idPrefix || 'address';
    return {
      idPrefix: p,
      queryId: options.queryId || p + '-address-search-query',
      btnId: options.btnId || p + '-address-search-btn',
      resultsId: options.resultsId || p + '-address-search-results',
      hintId: options.hintId || p + '-address-search-hint',
      mapWrapId: options.mapWrapId || p + '-kakao-map-wrap',
      mapId: options.mapId || p + '-kakao-map',
      mapStatusId: options.mapStatusId || p + '-kakao-map-status',
      mapInfoId: options.mapInfoId || p + '-kakao-map-info',
      zipId: options.zipId || p + '-address-zip',
      baseId: options.baseId || p + '-address-base',
      detailId: options.detailId || p + '-address-detail'
    };
  }

  function formatStoredAddress(postal, base, detail) {
    var b = (base || '').trim();
    var zip = digitsOnly(postal).slice(0, 5);
    var d = (detail || '').trim();
    if (!b && !zip && !d) {
      return '';
    }
    var line = zip ? '[' + zip + '] ' + b : b;
    if (d) {
      line += (line ? ' / ' : '') + d;
    }
    return line.trim();
  }

  function parseStoredAddress(value) {
    var raw = (value || '').trim();
    if (!raw) {
      return { postal: '', base: '', detail: '' };
    }
    var bracket = raw.match(/^\[(\d{5})\]\s*(.*)$/);
    if (bracket) {
      var rest = bracket[2];
      var slashIdx = rest.indexOf(' / ');
      if (slashIdx >= 0) {
        return {
          postal: bracket[1],
          base: rest.slice(0, slashIdx).trim(),
          detail: rest.slice(slashIdx + 3).trim()
        };
      }
      return { postal: bracket[1], base: rest.trim(), detail: '' };
    }
    return { postal: '', base: raw, detail: '' };
  }

  function createInstance(options) {
    var ids = resolveIds(options || {});
    var bound = false;
    var mapApi = null;
    var onNotify = options.onNotify || function () {};
    var KakaoAddress = window.PrintMallKakaoAddress;

    function notify(msg, icon) {
      onNotify(msg, icon);
    }

    function initMap() {
      if (!window.PrintMallKakaoAddressMap) {
        return null;
      }
      if (!mapApi) {
        mapApi = window.PrintMallKakaoAddressMap.init({
          wrapId: ids.mapWrapId,
          mapId: ids.mapId,
          statusId: ids.mapStatusId,
          infoId: ids.mapInfoId
        });
      }
      return mapApi;
    }

    function clear() {
      [ids.queryId, ids.zipId, ids.baseId, ids.detailId].forEach(function (id) {
        var node = el(id);
        if (node) {
          node.value = '';
        }
      });
      var results = el(ids.resultsId);
      if (results) {
        results.innerHTML = '';
        results.classList.add('d-none');
      }
      var hint = el(ids.hintId);
      if (hint) {
        hint.classList.add('d-none');
        hint.textContent = '';
      }
      if (mapApi) {
        mapApi.hide();
      }
    }

    function fill(postal, base, detail) {
      if (el(ids.zipId)) {
        el(ids.zipId).value = postal || '';
      }
      if (el(ids.baseId)) {
        el(ids.baseId).value = base || '';
      }
      if (el(ids.detailId)) {
        el(ids.detailId).value = detail || '';
      }
    }

    function fillFromStored(stored) {
      var parsed = parseStoredAddress(stored);
      fill(parsed.postal, parsed.base, parsed.detail);
    }

    function getValues() {
      return {
        postal: (el(ids.zipId) || {}).value || '',
        base: (el(ids.baseId) || {}).value || '',
        detail: (el(ids.detailId) || {}).value || ''
      };
    }

    function getStored() {
      var v = getValues();
      return formatStoredAddress(v.postal, v.base, v.detail);
    }

    function applyItem(item) {
      if (!item || !KakaoAddress) {
        return;
      }
      var detail = (item.buildingName || '').trim();
      fill(item.postalCode || '', KakaoAddress.buildFormLine1(item), detail);
      var map = initMap();
      if (map) {
        map.show(item);
      }
      var detailEl = el(ids.detailId);
      if (detailEl) {
        detailEl.focus();
      }
      if (options.notifyOnSelect !== false) {
        notify('주소가 입력되었습니다.', 'success');
      }
      if (options.onSelect) {
        options.onSelect(item, getValues());
      }
    }

    function showSetupHint() {
      var hint = el(ids.hintId);
      if (!hint) {
        return;
      }
      if (!KakaoAddress || typeof KakaoAddress.bindSearch !== 'function') {
        hint.textContent =
          '주소 검색 스크립트를 불러오지 못했습니다. 페이지를 새로고침(Ctrl+F5)해 주세요.';
        hint.classList.remove('d-none');
        hint.classList.add('alert-warning');
        return;
      }
      fetch('/api/kakao/local/status?origin=' + encodeURIComponent(window.location.origin || ''), {
        credentials: 'same-origin'
      })
        .then(function (res) {
          return res.json();
        })
        .then(function (data) {
          if (data && data.configured) {
            hint.classList.add('d-none');
            return;
          }
          hint.textContent =
            (data && data.message) ||
            'kakao.client-id(REST API 키)를 application-local.properties 에 설정한 뒤 서버를 재시작하세요.';
          hint.classList.remove('d-none');
          hint.classList.add('alert-warning');
        })
        .catch(function () {
          hint.classList.add('d-none');
        });
    }

    function bind() {
      if (bound) {
        return;
      }
      KakaoAddress = window.PrintMallKakaoAddress;
      if (!KakaoAddress || typeof KakaoAddress.bindSearch !== 'function') {
        showSetupHint();
        return;
      }
      KakaoAddress.bindSearch({
        queryId: ids.queryId,
        btnId: ids.btnId,
        resultsId: ids.resultsId,
        hintId: ids.hintId,
        onSelect: applyItem,
        onError: function (msg) {
          notify(msg, 'warning');
        }
      });
      bound = true;
      initMap();
    }

    function onContainerShown() {
      bind();
      showSetupHint();
      if (window.PrintMallKakaoAddressMap && window.PrintMallKakaoAddressMap.loadSdk) {
        window.PrintMallKakaoAddressMap.loadSdk(
          function () {
            if (mapApi) {
              setTimeout(function () {
                mapApi.relayout();
              }, 200);
            }
          },
          function () {}
        );
      }
    }

    function attachModalShown(modalId) {
      var modalEl = el(modalId);
      if (!modalEl) {
        return;
      }
      modalEl.addEventListener('shown.bs.modal', onContainerShown);
    }

    return {
      ids: ids,
      clear: clear,
      fill: fill,
      fillFromStored: fillFromStored,
      getValues: getValues,
      getStored: getStored,
      applyItem: applyItem,
      bind: bind,
      onContainerShown: onContainerShown,
      attachModalShown: attachModalShown,
      relayoutMap: function () {
        if (mapApi) {
          mapApi.relayout();
        }
      }
    };
  }

  window.PrintMallKakaoAddressForm = {
    formatStoredAddress: formatStoredAddress,
    parseStoredAddress: parseStoredAddress,
    resolveIds: resolveIds,
    /**
     * @param {object} options idPrefix, onNotify(msg, icon), onSelect(item, values), notifyOnSelect, modalId
     */
    create: function (options) {
      var key = (options && options.idPrefix) || 'address';
      if (!instances[key]) {
        instances[key] = createInstance(options || {});
      }
      if (options && options.modalId) {
        instances[key].attachModalShown(options.modalId);
      }
      return instances[key];
    },
    get: function (idPrefix) {
      return instances[idPrefix || 'address'] || null;
    }
  };
})();
