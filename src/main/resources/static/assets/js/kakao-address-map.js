/**
 * 카카오맵 — 주소 검색 선택 좌표 표시 (공통)
 * @module kakao-address-map
 */
(function () {
  'use strict';

  var instances = {};
  var sdkLoading = false;
  var sdkStatusCache = null;

  function escapeHtml(text) {
    return String(text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function parseCoord(value) {
    if (value == null || value === '') {
      return null;
    }
    var num = Number(value);
    return Number.isFinite(num) ? num : null;
  }

  function getCoords(item) {
    return {
      lat: parseCoord(item.latitude),
      lng: parseCoord(item.longitude)
    };
  }

  function buildFormLine1(item) {
    if (window.PrintMallKakaoAddress && window.PrintMallKakaoAddress.buildFormLine1) {
      return window.PrintMallKakaoAddress.buildFormLine1(item);
    }
    return item.displayAddress || item.addressLine1 || '';
  }

  function isKakaoMapsReady() {
    return typeof window.kakao !== 'undefined' && window.kakao && window.kakao.maps;
  }

  function findKakaoSdkScript() {
    return document.querySelector(
      'script[src*="maps-sdk.js"],script[src*="dapi.kakao.com/v2/maps/sdk.js"]'
    );
  }

  function buildDomainHintMessage(extra) {
    var origin = window.location.origin || '';
    var port = window.location.port || '8081';
    var lines = [
      extra || '카카오맵 대화형 지도를 불러오지 못했습니다. (아래 미리보기·링크는 이용 가능)',
      '',
      '현재 접속: ' + (origin || '(알 수 없음)'),
      'developers.kakao.com → 내 애플리케이션',
      '  ① 제품 설정 → 지도(OPEN_MAP_AND_LOCAL) ON',
      '  ② 플랫폼 키 → Web → 사이트 도메인:',
      '     http://localhost:' + port + '  /  http://127.0.0.1:' + port,
      '  ③ JavaScript 키 → JavaScript SDK 도메인 (위와 동일)',
      '  ④ kakao.javascript-key = JavaScript 키 (REST 키 아님)',
      '저장 후 Ctrl+F5'
    ];
    if (origin && origin.indexOf('localhost') === -1 && origin.indexOf('127.0.0.1') === -1) {
      lines.push('  ※ LAN 접속 시: ' + origin + ' 도메인도 등록');
    }
    return lines.join('\n');
  }

  function buildKakaoMapLink(coords, item) {
    var lat = coords.lat;
    var lng = coords.lng;
    var name = encodeURIComponent((item && (item.displayAddress || item.addressLine1)) || '선택 위치');
    return 'https://map.kakao.com/link/map/' + name + ',' + lat + ',' + lng;
  }

  function fetchMapStatus() {
    if (sdkStatusCache) {
      return Promise.resolve(sdkStatusCache);
    }
    var origin = encodeURIComponent(window.location.origin || '');
    return fetch('/api/kakao/local/status?origin=' + origin, { credentials: 'same-origin' })
      .then(function (res) {
        return res.json();
      })
      .then(function (data) {
        sdkStatusCache = data || {};
        return sdkStatusCache;
      })
      .catch(function () {
        return {};
      });
  }

  function appendSdkScript(appKey, onReady, onError) {
    sdkLoading = true;
    var script = document.createElement('script');
    script.setAttribute('data-kakao-map-sdk', 'true');
    script.src = '/api/kakao/local/maps-sdk.js';
    script.onload = function () {
      sdkLoading = false;
      if (isKakaoMapsReady()) {
        window.kakao.maps.load(onReady);
      } else {
        waitForKakaoMaps(onReady, onError, 0);
      }
    };
    script.onerror = function () {
      sdkLoading = false;
      if (onError) {
        onError(buildDomainHintMessage());
      }
    };
    document.head.appendChild(script);
  }

  function waitForKakaoMaps(onReady, onError, attempt) {
    if (isKakaoMapsReady()) {
      window.kakao.maps.load(onReady);
      return;
    }
    if (attempt > 50) {
      if (onError) {
        onError(
          buildDomainHintMessage(
            'SDK 스크립트는 로드됐지만 kakao.maps 가 없습니다. 지도 API(OPEN_MAP_AND_LOCAL) 활성화와 도메인 등록을 확인하세요.'
          )
        );
      }
      return;
    }
    setTimeout(function () {
      waitForKakaoMaps(onReady, onError, attempt + 1);
    }, 150);
  }

  function loadSdk(onReady, onError) {
    if (isKakaoMapsReady()) {
      window.kakao.maps.load(onReady);
      return;
    }

    var existing = findKakaoSdkScript();
    if (existing) {
      waitForKakaoMaps(
        onReady,
        function () {
          if (onError) {
            onError(buildDomainHintMessage());
          }
        },
        0
      );
      return;
    }

    if (sdkLoading) {
      waitForKakaoMaps(onReady, onError, 0);
      return;
    }

    function startWithKey(appKey) {
      if (!appKey) {
        if (onError) {
          onError('카카오맵 JavaScript 키가 없습니다. application-local.properties 에 kakao.javascript-key 를 설정하세요.');
        }
        return;
      }
      window.__KAKAO_MAP_APP_KEY__ = appKey;
      appendSdkScript(appKey, onReady, onError);
    }

    if (window.__KAKAO_MAP_APP_KEY__) {
      startWithKey(window.__KAKAO_MAP_APP_KEY__);
      return;
    }

    fetchMapStatus().then(function (data) {
      if (data && data.mapConfigured && data.mapAppKey) {
        startWithKey(data.mapAppKey);
        return;
      }
      if (onError) {
        var msg =
          (data && data.mapMessage) ||
          'kakao.javascript-key 를 application-local.properties 에 설정한 뒤 서버를 재시작하세요.';
        onError(msg);
      }
    });
  }

  function renderMapPreview(mapEl, coords, item, kakaoLink) {
    var lat = coords.lat;
    var lng = coords.lng;
    var d = 0.008;
    var bbox = (lng - d) + '%2C' + (lat - d) + '%2C' + (lng + d) + '%2C' + (lat + d);
    var title = item.displayAddress || buildFormLine1(item);
    mapEl.innerHTML =
      '<iframe class="kakao-address-map-fallback" title="' +
      escapeHtml(title || '위치') +
      '" width="100%" height="220" style="border:0;border-radius:0.25rem;" loading="lazy" ' +
      'src="https://www.openstreetmap.org/export/embed.html?bbox=' +
      bbox +
      '&amp;layer=mapnik&amp;marker=' +
      lat +
      '%2C' +
      lng +
      '"></iframe>' +
      '<div class="fs--2 mt-1">' +
      '<a href="' +
      escapeHtml(kakaoLink) +
      '" target="_blank" rel="noopener noreferrer" class="text-primary">카카오맵에서 크게 보기</a>' +
      ' <span class="text-600">· 대화형 지도는 SDK 도메인 등록 후 표시됩니다</span></div>';
  }

  function whenKakaoMapsReady(callback, attempt, showStatus, onFallback) {
    var tries = attempt || 0;
    if (isKakaoMapsReady()) {
      window.kakao.maps.load(callback);
      return;
    }
    if (tries > 40) {
      var hint = buildDomainHintMessage();
      if (showStatus) {
        showStatus(hint, true);
      }
      if (onFallback) {
        onFallback();
      }
      return;
    }
    setTimeout(function () {
      whenKakaoMapsReady(callback, tries + 1, showStatus, onFallback);
    }, 150);
  }

  function createInstance(options) {
    var wrapEl = document.getElementById(options.wrapId);
    var mapEl = document.getElementById(options.mapId);
    var statusEl = options.statusId ? document.getElementById(options.statusId) : null;
    var infoEl = options.infoId ? document.getElementById(options.infoId) : null;
    if (!wrapEl || !mapEl) {
      return null;
    }

    function showStatus(message, isError) {
      if (!statusEl) {
        return;
      }
      if (!message) {
        statusEl.classList.add('d-none');
        statusEl.textContent = '';
        statusEl.classList.remove('alert-danger', 'alert-warning', 'alert-secondary');
        return;
      }
      statusEl.textContent = message;
      statusEl.classList.remove('d-none', 'alert-danger', 'alert-warning', 'alert-secondary');
      statusEl.classList.add(isError ? 'alert-danger' : 'alert-warning');
      statusEl.style.whiteSpace = 'pre-line';
    }

    function updateInfo(item, coords) {
      if (!infoEl) {
        return;
      }
      if (!item) {
        infoEl.textContent = '';
        return;
      }
      var lines = [];
      var title = item.displayAddress || buildFormLine1(item);
      if (title) {
        lines.push(title);
      }
      if (item.postalCode) {
        lines.push('우편번호 ' + item.postalCode);
      }
      if (coords.lat != null && coords.lng != null) {
        lines.push('좌표 ' + coords.lat.toFixed(6) + ', ' + coords.lng.toFixed(6));
      }
      infoEl.innerHTML = lines.map(function (line) {
        return '<div>' + escapeHtml(line) + '</div>';
      }).join('');
    }

    var state = {
      wrapEl: wrapEl,
      mapEl: mapEl,
      kakaoMap: null,
      kakaoMarker: null,
      kakaoInfoWindow: null,
      lastItem: null,
      usingFallback: false,
      showStatus: showStatus,
      updateInfo: updateInfo
    };

    function relayoutMap(pos) {
      if (!state.kakaoMap || !pos) {
        return;
      }
      var kakao = window.kakao;
      kakao.maps.event.trigger(state.kakaoMap, 'resize');
      state.kakaoMap.setCenter(pos);
      state.kakaoMap.setLevel(3);
    }

    function hide() {
      wrapEl.classList.add('d-none');
      wrapEl.setAttribute('aria-hidden', 'true');
      if (state.kakaoInfoWindow) {
        state.kakaoInfoWindow.close();
      }
      mapEl.innerHTML = '';
      state.usingFallback = false;
      state.kakaoMap = null;
      showStatus('');
      updateInfo(null, {});
      state.lastItem = null;
    }

    function showKakaoMap(item, coords) {
      whenKakaoMapsReady(
        function () {
          state.usingFallback = false;
          showStatus('');
          mapEl.innerHTML = '';
          state.kakaoMap = null;
          state.kakaoMarker = null;
          var kakao = window.kakao;
          var pos = new kakao.maps.LatLng(coords.lat, coords.lng);

          if (!state.kakaoMap) {
            state.kakaoMap = new kakao.maps.Map(mapEl, {
              center: pos,
              level: 3
            });
          }

          relayoutMap(pos);

          if (state.kakaoMarker) {
            state.kakaoMarker.setMap(null);
          }
          state.kakaoMarker = new kakao.maps.Marker({ position: pos, map: state.kakaoMap });

          if (state.kakaoInfoWindow) {
            state.kakaoInfoWindow.close();
          }
          var title = item.displayAddress || buildFormLine1(item);
          if (title) {
            state.kakaoInfoWindow = new kakao.maps.InfoWindow({
              content:
                '<div class="p-2 fs--1" style="max-width:260px;word-break:keep-all;">' +
                escapeHtml(title) +
                '</div>'
            });
            state.kakaoInfoWindow.open(state.kakaoMap, state.kakaoMarker);
          }

          setTimeout(function () {
            relayoutMap(pos);
          }, 100);
          setTimeout(function () {
            relayoutMap(pos);
          }, 400);
        },
        0,
        showStatus,
        function () {
          state.usingFallback = true;
          renderMapPreview(mapEl, coords, item, buildKakaoMapLink(coords, item));
        }
      );
    }

    function show(item) {
      if (!item) {
        hide();
        return;
      }
      var coords = getCoords(item);
      state.lastItem = item;
      if (coords.lat == null || coords.lng == null) {
        hide();
        showStatus('선택한 주소에 좌표 정보가 없어 지도를 표시할 수 없습니다.', true);
        return;
      }

      wrapEl.classList.remove('d-none');
      wrapEl.setAttribute('aria-hidden', 'false');
      showStatus('');
      updateInfo(item, coords);

      var kakaoLink = buildKakaoMapLink(coords, item);
      state.usingFallback = true;
      renderMapPreview(mapEl, coords, item, kakaoLink);

      loadSdk(
        function () {
          showKakaoMap(item, coords);
        },
        function (msg) {
          showStatus(msg, false);
        }
      );
    }

    function relayout() {
      if (!state.lastItem) {
        return;
      }
      var coords = getCoords(state.lastItem);
      if (coords.lat == null || coords.lng == null) {
        return;
      }
      if (state.usingFallback) {
        renderMapPreview(mapEl, coords, state.lastItem, buildKakaoMapLink(coords, state.lastItem));
        return;
      }
      if (!state.kakaoMap) {
        return;
      }
      var pos = new window.kakao.maps.LatLng(coords.lat, coords.lng);
      relayoutMap(pos);
    }

    return {
      show: show,
      hide: hide,
      relayout: relayout
    };
  }

  window.PrintMallKakaoAddressMap = {
    loadSdk: loadSdk,
    buildDomainHintMessage: buildDomainHintMessage,
    init: function (options) {
      if (!options || !options.wrapId) {
        return null;
      }
      if (!instances[options.wrapId]) {
        instances[options.wrapId] = createInstance(options);
      }
      return instances[options.wrapId];
    },
    get: function (wrapId) {
      return instances[wrapId] || null;
    }
  };
})();
