/**
 * 마법사 주소 탭 — 카카오 로컬 API 주소 검색 + 카카오맵 표시
 */
(function () {
  'use strict';

  var mapWrap = null;
  var mapEl = null;
  var mapStatusEl = null;
  var kakaoMap = null;
  var kakaoMarker = null;
  var kakaoInfoWindow = null;
  var lastMapItem = null;

  function notify(message, icon) {
    if (window.Swal) {
      window.Swal.fire({
        toast: true,
        position: 'top-end',
        icon: icon || 'info',
        title: message,
        showConfirmButton: false,
        timer: 2800,
        timerProgressBar: true
      });
      return;
    }
    window.alert(message);
  }

  function isKoreaSelected() {
    var country = document.getElementById('wizard-address-country');
    return !country || country.value === 'KR';
  }

  function buildFormLine1(item) {
    var line = item.addressLine1 || '';
    if (line) {
      var stateShort = (item.state || '').replace('특별시', '').replace('광역시', '').replace('특별자치시', '');
      if (
        (item.state && line.indexOf(item.state) !== -1) ||
        (stateShort && line.indexOf(stateShort) !== -1) ||
        (item.city && line.indexOf(item.city) !== -1)
      ) {
        return line;
      }
    }
    var parts = [item.state, item.city, item.region3, line].filter(Boolean);
    if (parts.length) {
      return parts.join(' ');
    }
    return item.displayAddress || '';
  }

  function escapeHtml(text) {
    return String(text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function showMapStatus(message) {
    if (!mapStatusEl) {
      return;
    }
    if (!message) {
      mapStatusEl.classList.add('d-none');
      mapStatusEl.textContent = '';
      return;
    }
    mapStatusEl.textContent = message;
    mapStatusEl.classList.remove('d-none');
  }

  function hideAddressMap() {
    if (mapWrap) {
      mapWrap.classList.add('d-none');
      mapWrap.setAttribute('aria-hidden', 'true');
    }
    if (kakaoInfoWindow) {
      kakaoInfoWindow.close();
    }
    showMapStatus('');
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

  function isKakaoMapsReady() {
    return typeof window.kakao !== 'undefined' && window.kakao && window.kakao.maps;
  }

  function whenKakaoMapsReady(callback, attempt) {
    var tries = attempt || 0;
    if (isKakaoMapsReady()) {
      window.kakao.maps.load(callback);
      return;
    }
    if (tries > 40) {
      var hint =
        '카카오맵 JavaScript SDK를 불러오지 못했습니다. '
        + '① application-local.properties 의 kakao.javascript-key 가 REST 키(kakao.client-id)와 다른지 확인 '
        + '② developers.kakao.com → 플랫폼 키 → JavaScript 키 → JavaScript SDK 도메인에 http://localhost:8081 등록 '
        + '③ run-server.bat 으로 서버 완전 재시작(Ctrl+F5).';
      if (window.__KAKAO_MAP_APP_KEY__ && window.__KAKAO_REST_API_KEY__
          && window.__KAKAO_MAP_APP_KEY__ === window.__KAKAO_REST_API_KEY__) {
        hint += ' (현재 페이지에 REST API 키가 맵 키로 들어가 있습니다. mvn clean 후 재시작하세요.)';
      }
      showMapStatus(hint);
      return;
    }
    setTimeout(function () {
      whenKakaoMapsReady(callback, tries + 1);
    }, 150);
  }

  function relayoutMap(pos) {
    if (!kakaoMap || !pos) {
      return;
    }
    var kakao = window.kakao;
    kakao.maps.event.trigger(kakaoMap, 'resize');
    kakaoMap.setCenter(pos);
    kakaoMap.setLevel(3);
  }

  function renderMap(item) {
    if (!mapWrap || !mapEl) {
      return;
    }
    var coords = getCoords(item);
    if (coords.lat == null || coords.lng == null) {
      hideAddressMap();
      showMapStatus('선택한 주소에 좌표 정보가 없어 지도를 표시할 수 없습니다.');
      return;
    }
    if (!window.__KAKAO_MAP_APP_KEY__) {
      hideAddressMap();
      showMapStatus(
        '카카오맵 JavaScript 키가 없습니다. REST API 키(kakao.client-id)와 별도로 kakao.javascript-key 를 설정해 주세요.'
      );
      return;
    }

    mapWrap.classList.remove('d-none');
    mapWrap.setAttribute('aria-hidden', 'false');
    showMapStatus('');

    whenKakaoMapsReady(function () {
      var kakao = window.kakao;
      var pos = new kakao.maps.LatLng(coords.lat, coords.lng);

      if (!kakaoMap) {
        kakaoMap = new kakao.maps.Map(mapEl, {
          center: pos,
          level: 3
        });
      }

      relayoutMap(pos);

      if (kakaoMarker) {
        kakaoMarker.setMap(null);
      }
      kakaoMarker = new kakao.maps.Marker({ position: pos, map: kakaoMap });

      if (kakaoInfoWindow) {
        kakaoInfoWindow.close();
      }
      var title = item.displayAddress || buildFormLine1(item);
      if (title) {
        kakaoInfoWindow = new kakao.maps.InfoWindow({
          content:
            '<div class="p-2 fs--1" style="max-width:260px;word-break:keep-all;">' +
            escapeHtml(title) +
            '</div>'
        });
        kakaoInfoWindow.open(kakaoMap, kakaoMarker);
      }

      setTimeout(function () {
        relayoutMap(pos);
      }, 100);
      setTimeout(function () {
        relayoutMap(pos);
      }, 400);
    });
  }

  function showAddressMap(item) {
    if (!item) {
      hideAddressMap();
      return;
    }
    lastMapItem = item;
    renderMap(item);
  }

  function applyAddressItem(item) {
    var postal = document.getElementById('wizard-address-postal');
    var line1 = document.getElementById('wizard-address-line1');
    var city = document.getElementById('wizard-address-city');
    var state = document.getElementById('wizard-address-state');
    var line2 = document.getElementById('wizard-address-line2');

    if (postal) {
      postal.value = item.postalCode || '';
    }
    if (line1) {
      line1.value = buildFormLine1(item);
    }
    if (state) {
      state.value = item.state || '';
    }
    if (city) {
      city.value = item.city || '';
    }
    if (line2) {
      line2.value = '';
    }

    [postal, line1, city, state, line2].forEach(function (el) {
      if (el) {
        el.dispatchEvent(new Event('change', { bubbles: true }));
      }
    });

    showAddressMap(item);

    if (line2) {
      line2.focus();
    }

    if (mapWrap && !mapWrap.classList.contains('d-none')) {
      mapWrap.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }
  }

  function renderResults(container, items) {
    container.innerHTML = '';
    if (!items.length) {
      container.classList.add('d-none');
      hideAddressMap();
      return;
    }
    items.forEach(function (item) {
      var btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'list-group-item list-group-item-action text-start';
      btn.setAttribute('role', 'option');
      var typeLabel =
        item.addressType === 'ROAD_ADDR'
          ? '도로명'
          : item.addressType === 'REGION_ADDR'
            ? '지번'
            : '주소';
      var subParts = [typeLabel];
      if (item.postalCode) {
        subParts.push('우편번호 ' + item.postalCode);
      }
      if (item.buildingName) {
        subParts.push(item.buildingName);
      }
      btn.innerHTML =
        '<div class="fw-semi-bold">' +
        (item.displayAddress || buildFormLine1(item)) +
        '</div>' +
        '<div class="fs--2 text-600">' +
        subParts.join(' · ') +
        '</div>';
      btn.addEventListener('click', function () {
        applyAddressItem(item);
        container.classList.add('d-none');
        notify('주소가 입력되었습니다.', 'success');
      });
      container.appendChild(btn);
    });
    container.classList.remove('d-none');
  }

  function showHint(el, message, isError) {
    if (!el) {
      return;
    }
    if (!message) {
      el.classList.add('d-none');
      el.textContent = '';
      el.classList.remove('alert-danger', 'alert-warning');
      return;
    }
    el.textContent = message;
    el.classList.remove('d-none', 'alert-danger', 'alert-warning');
    el.classList.add(isError ? 'alert-danger' : 'alert-warning');
  }

  function searchAddress() {
    var queryEl = document.getElementById('wizard-address-search-query');
    var resultsEl = document.getElementById('wizard-address-search-results');
    var hintEl = document.getElementById('wizard-address-search-hint');
    var btn = document.getElementById('wizard-address-search-btn');
    if (!queryEl || !resultsEl) {
      return;
    }

    if (!isKoreaSelected()) {
      showHint(hintEl, '대한민국을 선택한 경우에만 카카오 주소 검색을 사용할 수 있습니다.', false);
      resultsEl.classList.add('d-none');
      return;
    }

    var query = queryEl.value.trim();
    if (query.length < 2) {
      showHint(hintEl, '검색어를 2글자 이상 입력해 주세요.', false);
      return;
    }

    showHint(hintEl, '검색 중…', false);
    hideAddressMap();
    if (btn) {
      btn.disabled = true;
    }

    fetch('/api/kakao/local/search/address?query=' + encodeURIComponent(query) + '&size=10')
      .then(function (res) {
        return res.json().then(function (data) {
          return { ok: res.ok, status: res.status, data: data };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          var msg =
            (result.data && result.data.message) ||
            '주소 검색에 실패했습니다. (HTTP ' + result.status + ')';
          showHint(hintEl, msg, true);
          resultsEl.classList.add('d-none');
          hideAddressMap();
          if (result.status === 503) {
            notify('카카오 REST API 키를 application-local.properties 에 설정해 주세요.', 'warning');
          }
          return;
        }
        var data = result.data || {};
        if (data.warning) {
          showHint(hintEl, data.warning, false);
        } else if (data.mock) {
          showHint(
            hintEl,
            '데모 주소입니다. 실제 카카오 검색은 application-local.properties 에 REST API 키를 넣어 주세요.',
            false
          );
        } else {
          showHint(hintEl, '', false);
        }
        var items = data.items || [];
        if (!items.length) {
          showHint(hintEl, '검색 결과가 없습니다. 다른 키워드로 시도해 주세요.', false);
          resultsEl.classList.add('d-none');
          hideAddressMap();
          return;
        }
        renderResults(resultsEl, items);
        showHint(hintEl, '목록에서 주소를 선택하면 아래 지도에 위치가 표시됩니다.', false);
      })
      .catch(function () {
        showHint(hintEl, '주소 검색 요청 중 오류가 발생했습니다.', true);
        resultsEl.classList.add('d-none');
        hideAddressMap();
      })
      .finally(function () {
        if (btn) {
          btn.disabled = false;
        }
      });
  }

  function bindTabResize() {
    var tabLink = document.querySelector('a[href="#bootstrap-wizard-tab3"]');
    if (!tabLink) {
      return;
    }
    tabLink.addEventListener('shown.bs.tab', function () {
      if (lastMapItem) {
        setTimeout(function () {
          renderMap(lastMapItem);
        }, 200);
      }
    });
  }

  function bind() {
    mapWrap = document.getElementById('wizard-kakao-map-wrap');
    mapEl = document.getElementById('wizard-kakao-map');
    mapStatusEl = document.getElementById('wizard-kakao-map-status');

    if (!window.__KAKAO_MAP_APP_KEY__) {
      showMapStatus(
        '카카오맵용 JavaScript 키가 없습니다. application-local.properties 에 kakao.javascript-key=JavaScript키 를 추가하세요. (REST API 키와 다릅니다)'
      );
    }

    var searchBtn = document.getElementById('wizard-address-search-btn');
    var queryEl = document.getElementById('wizard-address-search-query');
    if (searchBtn) {
      searchBtn.addEventListener('click', searchAddress);
    }
    if (queryEl) {
      queryEl.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
          e.preventDefault();
          searchAddress();
        }
      });
    }
    bindTabResize();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bind);
  } else {
    bind();
  }
})();
