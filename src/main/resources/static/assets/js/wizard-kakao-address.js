/**
 * 마법사 주소 탭 — 카카오 로컬 API 주소 검색 (/api/kakao/local/search/address)
 */
(function () {
  'use strict';

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
    return !country || country.value === 'KR' || country.value === '';
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
      line1.value = item.addressLine1 || item.displayAddress || '';
    }
    if (state) {
      state.value = item.state || '';
    }
    if (city) {
      city.value = item.city || item.region3 || '';
    }
    if (line2 && item.buildingName) {
      line2.value = item.buildingName;
    }

    [postal, line1, city, state].forEach(function (el) {
      if (el) {
        el.dispatchEvent(new Event('change', { bubbles: true }));
      }
    });
  }

  function renderResults(container, items) {
    container.innerHTML = '';
    if (!items.length) {
      container.classList.add('d-none');
      return;
    }
    items.forEach(function (item, index) {
      var btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'list-group-item list-group-item-action text-start';
      btn.setAttribute('role', 'option');
      btn.dataset.index = String(index);
      var typeLabel =
        item.addressType === 'ROAD_ADDR'
          ? '도로명'
          : item.addressType === 'REGION_ADDR'
            ? '지번'
            : '주소';
      var meta = [];
      if (item.postalCode) {
        meta.push(item.postalCode);
      }
      if (item.city || item.state) {
        meta.push([item.state, item.city].filter(Boolean).join(' '));
      }
      btn.innerHTML =
        '<div class="fw-semi-bold">' +
        (item.displayAddress || '') +
        '</div>' +
        '<div class="fs--2 text-600">' +
        typeLabel +
        (meta.length ? ' · ' + meta.join(' · ') : '') +
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
          if (result.status === 503) {
            notify('카카오 REST API 키를 application.properties 에 설정해 주세요.', 'warning');
          }
          return;
        }
        var data = result.data || {};
        if (data.mock) {
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
          return;
        }
        renderResults(resultsEl, items);
      })
      .catch(function () {
        showHint(hintEl, '주소 검색 요청 중 오류가 발생했습니다.', true);
        resultsEl.classList.add('d-none');
      })
      .finally(function () {
        if (btn) {
          btn.disabled = false;
        }
      });
  }

  function bind() {
    var searchBtn = document.getElementById('wizard-address-search-btn');
    var queryEl = document.getElementById('wizard-address-search-query');
    var countryEl = document.getElementById('wizard-address-country');

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
    if (countryEl) {
      countryEl.addEventListener('change', function () {
        var hintEl = document.getElementById('wizard-address-search-hint');
        var resultsEl = document.getElementById('wizard-address-search-results');
        if (!isKoreaSelected()) {
          showHint(
            hintEl,
            '해외 주소는 직접 입력해 주세요. (카카오 검색은 국내 주소만 지원)',
            false
          );
          if (resultsEl) {
            resultsEl.classList.add('d-none');
          }
        } else {
          showHint(hintEl, '', false);
        }
      });
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', bind);
  } else {
    bind();
  }
})();
