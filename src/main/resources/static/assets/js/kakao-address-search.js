/**
 * 카카오 로컬 API 주소 검색 공통 모듈 — /api/kakao/local/search/address
 * @module kakao-address-search
 */
(function () {
  'use strict';

  function escapeHtml(text) {
    return String(text)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
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

  function renderResults(container, items, onSelect) {
    container.innerHTML = '';
    if (!items.length) {
      container.classList.add('d-none');
      return;
    }
    items.forEach(function (item) {
      var btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'list-group-item list-group-item-action text-start py-2';
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
        '<div class="fw-semi-bold fs--1">' +
        escapeHtml(item.displayAddress || buildFormLine1(item)) +
        '</div>' +
        '<div class="fs--2 text-600">' +
        escapeHtml(subParts.join(' · ')) +
        '</div>';
      btn.addEventListener('click', function () {
        onSelect(item);
        container.classList.add('d-none');
      });
      container.appendChild(btn);
    });
    container.classList.remove('d-none');
  }

  /**
   * 주소 검색 UI를 바인딩합니다.
   * @param {object} options
   * @param {string} options.queryId 검색어 input id
   * @param {string} options.btnId 검색 버튼 id
   * @param {string} options.resultsId 결과 목록 container id
   * @param {string} [options.hintId] 안내 alert id
   * @param {function(object):void} options.onSelect 주소 선택 콜백
   * @param {function(string,string):void} [options.onError] 오류 콜백
   */
  function bindSearch(options) {
    var queryEl = document.getElementById(options.queryId);
    var btnEl = document.getElementById(options.btnId);
    var resultsEl = document.getElementById(options.resultsId);
    var hintEl = options.hintId ? document.getElementById(options.hintId) : null;
    if (!queryEl || !resultsEl) {
      return;
    }

    function runSearch() {
      var query = queryEl.value.trim();
      if (query.length < 2) {
        showHint(hintEl, '검색어를 2글자 이상 입력해 주세요.', false);
        return;
      }
      showHint(hintEl, '검색 중…', false);
      resultsEl.classList.add('d-none');
      if (btnEl) {
        btnEl.disabled = true;
      }

      fetch('/api/kakao/local/search/address?query=' + encodeURIComponent(query) + '&size=10', {
        credentials: 'same-origin'
      })
        .then(function (res) {
          return res.json().then(function (data) {
            return { ok: res.ok, status: res.status, data: data };
          });
        })
        .then(function (result) {
          if (!result.ok) {
            var msg =
              (result.data && result.data.message) ||
              (result.status === 403
                ? '주소 검색 API 접근 권한이 없습니다. 로그인 상태를 확인하거나 관리자에게 문의하세요.'
                : '주소 검색에 실패했습니다. (HTTP ' + result.status + ')');
            showHint(hintEl, msg, true);
            if (options.onError) {
              options.onError(msg, String(result.status));
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
            return;
          }
          renderResults(resultsEl, items, function (item) {
            showHint(hintEl, '주소가 입력되었습니다. 상세 주소를 입력해 주세요.', false);
            options.onSelect(item);
          });
        })
        .catch(function () {
          showHint(hintEl, '주소 검색 요청 중 오류가 발생했습니다.', true);
          if (options.onError) {
            options.onError('주소 검색 요청 중 오류가 발생했습니다.', 'network');
          }
        })
        .finally(function () {
          if (btnEl) {
            btnEl.disabled = false;
          }
        });
    }

    if (btnEl) {
      btnEl.addEventListener('click', runSearch);
    }
    queryEl.addEventListener('keydown', function (e) {
      if (e.key === 'Enter') {
        e.preventDefault();
        runSearch();
      }
    });
  }

  window.PrintMallKakaoAddress = {
    buildFormLine1: buildFormLine1,
    bindSearch: bindSearch
  };
})();
