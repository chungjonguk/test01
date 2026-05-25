/**
 * PrintMall 그리드·목록 공통 페이징
 *
 * 설정: application.properties `app.grid.page-size` → layout `__PRINTMALL_GRID_PAGE_SIZE__`
 *
 * JS API:
 *   PrintMallGridPager.getPageSize()
 *   PrintMallGridPager.attach({ rootId, onPageChange, pageSize? })
 *   PrintMallGridPager.createGridViewPager({ pagerRootId, containerId, renderPage, ... })
 *
 * PrintMallCommon (grid-pager 로드 후):
 *   createGridViewPager, createGridPager, gridPageSize, gridRowNo, listJsPageSize
 *
 * HTML: fragments/grid-pager :: pager('my-grid-pager')
 *
 * List.js: data-list JSON `"page":${gridPageSize}` (Thymeleaf)
 *
 * @global PrintMallGridPager
 */
(function () {
  'use strict';

  function resolvePageSize(override) {
    if (override != null && override > 0) {
      return override;
    }
    var raw = window.__PRINTMALL_GRID_PAGE_SIZE__;
    var n = parseInt(raw, 10);
    return n > 0 ? n : 30;
  }

  var DEFAULT_PAGE_SIZE = resolvePageSize();

  function getPageSize() {
    return DEFAULT_PAGE_SIZE;
  }

  function configure(options) {
    options = options || {};
    if (options.pageSize != null) {
      DEFAULT_PAGE_SIZE = resolvePageSize(options.pageSize);
      window.__PRINTMALL_GRID_PAGE_SIZE__ = DEFAULT_PAGE_SIZE;
    }
  }

  function rowNo(meta, index) {
    return (meta && meta.start ? meta.start - 1 : 0) + index + 1;
  }

  function emptyTableRow(colspan, message) {
    return (
      '<tr><td colspan="' +
      (colspan || 1) +
      '" class="text-center text-600 py-4">' +
      (message || '조회 결과가 없습니다.') +
      '</td></tr>'
    );
  }

  function totalPages(state) {
    return Math.max(1, Math.ceil(state.items.length / state.pageSize));
  }

  function pageItems(state) {
    var tp = totalPages(state);
    var page = Math.min(Math.max(1, state.page), tp);
    var start = (page - 1) * state.pageSize;
    return state.items.slice(start, start + state.pageSize);
  }

  function buildMeta(state) {
    var total = state.items.length;
    var tp = totalPages(state);
    var page = Math.min(Math.max(1, state.page), tp);
    var start = total === 0 ? 0 : (page - 1) * state.pageSize + 1;
    var end = Math.min(page * state.pageSize, total);
    return {
      page: page,
      totalPages: tp,
      totalItems: total,
      start: start,
      end: end,
      pageSize: state.pageSize
    };
  }

  function renderControls(root, state, onGo) {
    if (!root) {
      return;
    }
    var m = buildMeta(state);
    var info = root.querySelector('[data-grid-pager-info]');
    var ul = root.querySelector('[data-grid-pager-pages]');
    var showNav = m.totalItems > m.pageSize;

    root.classList.toggle('d-none', !showNav);

    if (info) {
      if (m.totalItems === 0) {
        info.textContent = '';
      } else {
        info.textContent =
          m.start + '-' + m.end + ' / ' + m.totalItems + '건 · ' + m.page + '/' + m.totalPages + '페이지';
      }
    }

    if (!ul) {
      return;
    }

    ul.innerHTML = '';
    if (!showNav) {
      return;
    }

    for (var p = 1; p <= m.totalPages; p += 1) {
      var li = document.createElement('li');
      li.className = 'page-item' + (p === m.page ? ' active' : '');
      var btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'page-link';
      btn.textContent = String(p);
      btn.setAttribute('data-grid-pager-page', String(p));
      btn.addEventListener('click', function (pageNum) {
        return function (e) {
          e.preventDefault();
          onGo(pageNum);
        };
      }(p));
      li.appendChild(btn);
      ul.appendChild(li);
    }
  }

  /**
   * @param {Object} config
   * @param {string} [config.rootId]
   * @param {HTMLElement} [config.rootEl]
   * @param {number} [config.pageSize]
   * @param {function(Array, Object):void} config.onPageChange
   */
  function attach(config) {
    config = config || {};
    var root =
      config.rootEl ||
      (typeof config.rootId === 'string' ? document.getElementById(config.rootId) : null);
    var state = {
      items: [],
      page: 1,
      pageSize: resolvePageSize(config.pageSize)
    };

    function emit() {
      var m = buildMeta(state);
      renderControls(root, state, go);
      if (typeof config.onPageChange === 'function') {
        config.onPageChange(pageItems(state), m);
      }
    }

    function go(page) {
      state.page = Math.min(Math.max(1, page), totalPages(state));
      emit();
    }

    function setItems(items) {
      state.items = items || [];
      state.page = 1;
      emit();
    }

    function refresh() {
      emit();
    }

    return {
      setItems: setItems,
      go: go,
      refresh: refresh,
      getMeta: function () {
        return buildMeta(state);
      },
      getAllItems: function () {
        return state.items.slice();
      }
    };
  }

  /**
   * 테이블 tbody·카드 그리드 등에 공통으로 쓰는 뷰 페이저 팩토리
   * @param {Object} options
   * @param {string} options.pagerRootId
   * @param {string} options.containerId
   * @param {function(Array, Object):void} options.renderPage
   * @param {string} [options.countElId]
   * @param {number} [options.pageSize]
   * @param {number} [options.emptyColspan]
   * @param {string} [options.emptyMessage]
   * @param {string} [options.emptyHtml] colspan 대신 전체 HTML (카드 그리드)
   * @param {function(Array, Object):void} [options.onBeforeRender]
   * @param {function(number):void} [options.onCountChange]
   */
  function createGridViewPager(options) {
    options = options || {};
    var holder = { pager: null };

    function container() {
      return options.containerId ? document.getElementById(options.containerId) : null;
    }

    function ensurePager() {
      if (!holder.pager) {
        holder.pager = attach({
          rootId: options.pagerRootId,
          pageSize: options.pageSize,
          onPageChange: function (pageItems, meta) {
            if (typeof options.onBeforeRender === 'function') {
              options.onBeforeRender(pageItems, meta);
            }
            options.renderPage(pageItems, meta);
          }
        });
      }
      return holder.pager;
    }

    function setCount(total) {
      if (typeof options.onCountChange === 'function') {
        options.onCountChange(total);
        return;
      }
      if (options.countElId) {
        var el = document.getElementById(options.countElId);
        if (el) {
          el.textContent = total + '건';
        }
      }
    }

    function defaultEmptyHtml() {
      if (options.emptyHtml) {
        return options.emptyHtml;
      }
      return emptyTableRow(options.emptyColspan, options.emptyMessage);
    }

    return {
      setData: function (items, emptyOverride) {
        items = items || [];
        setCount(items.length);
        var el = container();
        if (!items.length) {
          if (holder.pager) {
            holder.pager.setItems([]);
          }
          if (el) {
            el.innerHTML =
              emptyOverride && emptyOverride.html != null ? emptyOverride.html : defaultEmptyHtml();
          }
          return;
        }
        var pager = ensurePager();
        if (pager) {
          pager.setItems(items);
          return;
        }
        if (typeof options.onBeforeRender === 'function') {
          options.onBeforeRender(items, { start: 1, totalItems: items.length, pageSize: getPageSize() });
        }
        options.renderPage(items, { start: 1, totalItems: items.length, pageSize: getPageSize() });
      },
      refresh: function () {
        if (holder.pager) {
          holder.pager.refresh();
        }
      },
      getPager: function () {
        return holder.pager;
      }
    };
  }

  function wirePrintMallCommon() {
    if (!window.PrintMallCommon) {
      return;
    }
    var C = window.PrintMallCommon;
    C.gridPageSize = getPageSize;
    C.listJsPageSize = getPageSize;
    C.gridRowNo = rowNo;
    C.createGridPager = attach;
    C.createGridViewPager = createGridViewPager;
    C.configureGridPaging = configure;
  }

  window.PrintMallGridPager = {
    PAGE_SIZE: DEFAULT_PAGE_SIZE,
    getPageSize: getPageSize,
    configure: configure,
    rowNo: rowNo,
    emptyTableRow: emptyTableRow,
    attach: attach,
    createGridViewPager: createGridViewPager
  };

  wirePrintMallCommon();
})();
