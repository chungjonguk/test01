/**
 * 알림 — 상단 벨 아이콘 ↔ 사이드바 알림 메뉴 연동 (미읽음 수·목록·현재 페이지)
 */
(function (global) {
  'use strict';

  var NOTIFICATION_PATH_KEYS = ['/app/social/notifications', '/app/social/notification-list'];
  var STORAGE_KEY = 'printmall.notifications.items';
  var API_URL = '/api/social/notifications';
  var DROPDOWN_LIMIT = 8;
  var PAGE_LIST_LIMIT = 100;
  var SIDEBAR_PREVIEW_LIMIT = 5;
  var useApi = true;
  var apiLoaded = false;
  /** 알림 목록 화면 조회 결과(검색란) — 전역 벨 캐시와 분리 */
  var pageListCache = null;

  var SECTION_LABELS = {
    NEW: '새 알림',
    EARLIER: '이전 알림'
  };

  /** Activity log 화면과 동일한 아이콘(이모지) 목록 */
  var ACTIVITY_LOG_ICONS = ['🔍', '📌', '🏷️', '💬', '😂', '🎁', '📋️', '📅️', '📢'];

  var DEMO_ITEMS = [
    {
      id: 'demo-n1',
      section: 'NEW',
      unread: true,
      body: '<strong>Emma Watson</strong> replied to your comment : "Hello world 😍"',
      timeLabel: 'Just now',
      timeIcon: '💬',
      avatarType: 'emoji'
    },
    {
      id: 'demo-n2',
      section: 'NEW',
      unread: true,
      body: '<strong>Albert Brooks</strong> reacted to <strong>Mia Khalifa\'s</strong> status',
      timeLabel: '9hr',
      timeIcon: '😂',
      avatarType: 'emoji'
    },
    {
      id: 'demo-n3',
      section: 'EARLIER',
      unread: false,
      body: 'The forecast today shows a low of 20℃ in California. See today\'s weather.',
      timeLabel: '1d',
      timeIcon: '🔍',
      avatarType: 'emoji'
    },
    {
      id: 'demo-n4',
      section: 'EARLIER',
      unread: true,
      body: '<strong>University of Oxford</strong> created an event : "Causal Inference Hilary 2019"',
      timeLabel: '1w',
      timeIcon: '📅️',
      avatarType: 'emoji'
    },
    {
      id: 'demo-n5',
      section: 'EARLIER',
      unread: false,
      body: '<strong>James Cameron</strong> invited to join the group: United Nations International Children\'s Fund',
      timeLabel: '2d',
      timeIcon: '📋️',
      avatarType: 'emoji'
    },
    {
      id: 'demo-n6',
      section: '',
      unread: true,
      body: 'Announcing the winners of the <strong>The only book awards</strong> decided by you, the readers.',
      timeLabel: 'Just Now',
      timeIcon: '📢',
      avatarType: 'emoji'
    },
    {
      id: 'demo-n7',
      section: '',
      unread: true,
      body: 'Last chance to vote in <strong>The 2018 PrintMall Choice Awards</strong>!',
      timeLabel: '15m',
      timeIcon: '🎁',
      avatarType: 'emoji'
    },
    {
      id: 'demo-n8',
      section: '',
      unread: false,
      body: '<strong>Jennifer Kent</strong> declared you as a <strong>President</strong> of Computer Science and Engineering Society',
      timeLabel: '1h',
      timeIcon: '📌',
      avatarType: 'emoji'
    }
  ];

  function pathKey(path) {
    if (!path) {
      return '';
    }
    var p = String(path).trim();
    var q = p.indexOf('?');
    if (q >= 0) {
      p = p.substring(0, q);
    }
    if (p.length > 3 && p.slice(-3) === '.do') {
      p = p.slice(0, -3);
    }
    if (p.length > 1 && p.charAt(p.length - 1) === '/') {
      p = p.slice(0, -1);
    }
    if (p === '/index') {
      return '/';
    }
    return p || '/';
  }

  function escapeHtml(value) {
    return String(value == null ? '' : value)
      .replace(/&/g, '&amp;')
      .replace(/"/g, '&quot;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  function migrateNotificationItem(item) {
    if (!item) {
      return null;
    }
    var migrated = Object.assign({}, item);
    migrated.avatarType = 'emoji';
    migrated.timeIcon = normalizeActivityIcon(migrated.timeIcon, migrated.timeIconClass);
    delete migrated.avatarInitials;
    delete migrated.avatarSrc;
    return migrated;
  }

  function migrateNotificationItems(items) {
    return (items || []).map(migrateNotificationItem).filter(Boolean);
  }

  function loadItems() {
    try {
      var raw = global.localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return [];
      }
      var parsed = JSON.parse(raw);
      return migrateNotificationItems(Array.isArray(parsed) ? parsed : []);
    } catch (e) {
      return [];
    }
  }

  function saveItems(items) {
    var list = migrateNotificationItems(Array.isArray(items) ? items : []);
    global.localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
    var unread = getUnreadCount(list);
    refreshBadges(unread);
    global.document.dispatchEvent(
      new CustomEvent('printmall-notification-updated', {
        detail: { unread: unread, items: list }
      })
    );
    return list;
  }

  function getUnreadCount(items) {
    var list = items || loadItems();
    return list.filter(function (item) {
      return item && item.unread !== false;
    }).length;
  }

  function mapApiRow(row) {
    if (!row) return null;
    return {
      id: row.id || (row.notificationId != null ? 'db-' + row.notificationId : ''),
      notificationId: row.notificationId,
      section: row.section || row.sectionCd || 'NEW',
      unread: row.unread !== false && row.readYn !== 'Y',
      body: row.body || row.message || '',
      timeLabel: row.timeLabel || '',
      timeIcon: normalizeActivityIcon(row.timeIcon, row.timeIconClass),
      timeIconClass: row.timeIconClass || '',
      avatarType: 'emoji'
    };
  }

  function normalizeActivityIcon(icon, timeIconClass) {
    if (timeIconClass) {
      return '📢';
    }
    var trimmed = String(icon == null ? '' : icon).trim();
    return trimmed || '📢';
  }

  function ensureItems() {
    var items = loadItems();
    if (useApi) {
      return items;
    }
    if (!items.length) {
      items = DEMO_ITEMS.map(function (item) {
        return Object.assign({}, item);
      });
      saveItems(items);
    }
    return items;
  }

  function sectionLabel(section) {
    var key = section || 'EARLIER';
    return SECTION_LABELS[key] || key;
  }

  function isNotificationListPage() {
    return !!global.document.getElementById('notification-icon-page-list');
  }

  function refreshNavFromApi() {
    if (!useApi || !global.fetch) {
      return Promise.resolve(ensureItems());
    }
    var url = API_URL + '?limit=' + DROPDOWN_LIMIT;
    return global
      .fetch(url, {
        method: 'GET',
        headers: { Accept: 'application/json' },
        credentials: 'same-origin'
      })
      .then(function (res) {
        return res.json();
      })
      .then(function (data) {
        if (!data || !data.success || !Array.isArray(data.items)) {
          apiLoaded = false;
          return ensureItems();
        }
        var items = data.items.map(mapApiRow).filter(Boolean);
        apiLoaded = true;
        if (items.length) {
          saveItems(items);
        } else {
          saveItems([]);
        }
        refreshNavOnly();
        return items;
      })
      .catch(function () {
        var cached = loadItems();
        apiLoaded = true;
        if (!cached.length) {
          saveItems([]);
        }
        refreshNavOnly();
        return cached;
      });
  }

  function refreshFromApi() {
    return refreshNavFromApi();
  }

  function refreshNavOnly() {
    refreshBadges(getUnreadCount());
    renderHeaderDropdown();
    renderSidebarIconList();
    syncHeaderActive();
    if (!isNotificationListPage()) {
      renderIconPageList();
      renderPageList();
    }
  }

  function formatBadge(n) {
    return n > 99 ? '99+' : String(n);
  }

  function refreshBadges(count) {
    var n = typeof count === 'number' ? count : getUnreadCount();
    global.document.querySelectorAll('[data-notification-badge]').forEach(function (badge) {
      var wrap = badge.closest('.notification-indicator') || badge.closest('a.nav-link');
      if (n > 0) {
        badge.textContent = formatBadge(n);
        badge.classList.remove('d-none');
        if (badge.classList.contains('notification-indicator-number')) {
          badge.style.display = '';
        }
        if (wrap && wrap.classList.contains('notification-indicator')) {
          wrap.classList.add('notification-indicator-fill');
        }
      } else {
        badge.textContent = '';
        badge.classList.add('d-none');
        if (badge.classList.contains('notification-indicator-number')) {
          badge.style.display = 'none';
        }
        if (wrap && wrap.classList.contains('notification-indicator')) {
          wrap.classList.remove('notification-indicator-fill');
        }
      }
    });
  }

  function activityLogEmoji(item) {
    return normalizeActivityIcon(item.timeIcon, item.timeIconClass);
  }

  /** Activity log — avatar-emoji rounded-circle */
  function renderActivityAvatar(item, sizeClass) {
    var emoji = activityLogEmoji(item);
    return (
      '<div class="avatar ' +
      (sizeClass || 'avatar-xl') +
      ' me-3"><div class="avatar-emoji rounded-circle "><span role="img" aria-label="Emoji">' +
      emoji +
      '</span></div></div>'
    );
  }

  function renderHeaderRow(item) {
    var unreadClass = item.unread !== false ? ' notification-unread' : '';
    return (
      '<div class="list-group-item">' +
      '<a class="notification notification-flush' +
      unreadClass +
      '" href="#!" data-notification-id="' +
      escapeHtml(item.id) +
      '" data-notification-mark-read="true">' +
      '<div class="notification-avatar">' +
      renderActivityAvatar(item, 'avatar-2xl') +
      '</div>' +
      '<div class="notification-body">' +
      '<p class="mb-1">' +
      item.body +
      '</p>' +
      '<span class="notification-time">' +
      escapeHtml(item.timeLabel || '') +
      '</span></div></a></div>'
    );
  }

  function renderActivityLogRow(item, opts) {
    opts = opts || {};
    var unreadClass = item.unread !== false ? ' notification-unread' : '';
    var rowClass = opts.isLast
      ? 'notification border-x-0 border-bottom-0 border-300 rounded-top-0'
      : 'border-bottom-0 notification rounded-0 border-x-0 border border-300';
    return (
      '<a class="' +
      rowClass +
      unreadClass +
      '" href="#!" data-notification-id="' +
      escapeHtml(item.id) +
      '" data-notification-mark-read="true">' +
      '<div class="notification-avatar">' +
      renderActivityAvatar(item, 'avatar-xl') +
      '</div>' +
      '<div class="notification-body">' +
      '<p class="mb-1">' +
      item.body +
      '</p>' +
      '<span class="notification-time">' +
      escapeHtml(item.timeLabel || '') +
      '</span></div></a>'
    );
  }

  function groupForList(items, max) {
    var cap = max > 0 ? max : items.length;
    var list = items.slice(0, cap);
    var sections = [];
    var seen = {};
    list.forEach(function (item) {
      var key = item.section || 'EARLIER';
      if (!seen[key]) {
        seen[key] = true;
        sections.push(key);
      }
    });
    if (!sections.length) {
      sections.push('NEW');
    }
    return { list: list, sections: sections };
  }

  function groupForDropdown(items) {
    return groupForList(items, DROPDOWN_LIMIT);
  }

  function renderIconPageListInto(container, items) {
    if (!container) return;
    if (!items.length) {
      container.innerHTML = '<div class="text-center text-600 py-4">알림이 없습니다.</div>';
      return;
    }
    container.innerHTML = items
      .map(function (item, index) {
        return renderActivityLogRow(item, { isLast: index === items.length - 1 });
      })
      .join('');
  }

  function renderGroupedIconList(container, items, maxItems) {
    if (!container) return;
    if (container.getAttribute('data-notification-icon-list') === 'page') {
      renderIconPageListInto(container, items);
      return;
    }
    if (!items.length) {
      container.innerHTML =
        '<div class="list-group-item text-center text-600 py-4">알림이 없습니다.</div>';
      return;
    }
    var grouped = groupForList(items, maxItems);
    var html = '';
    grouped.sections.forEach(function (section) {
      var sectionItems = grouped.list.filter(function (item) {
        var s = item.section || 'EARLIER';
        return s === section;
      });
      if (!sectionItems.length) return;
      if (section === 'NEW' || section === 'EARLIER' || section) {
        html += '<div class="list-group-title border-bottom">' + escapeHtml(sectionLabel(section)) + '</div>';
      }
      sectionItems.forEach(function (item) {
        html += renderHeaderRow(item);
      });
    });
    container.innerHTML = html;
  }

  function renderIconPageList() {
    var container = global.document.getElementById('notification-icon-page-list');
    if (!container) return;
    var items =
      pageListCache !== null
        ? pageListCache
        : migrateNotificationItems(ensureItems());
    renderIconPageListInto(container, items);
    var countEl = global.document.getElementById('notification-search-result-count');
    if (countEl) countEl.textContent = items.length + '건';
    var title = global.document.getElementById('notification-page-title');
    if (title) {
      var unread = getUnreadCount(items);
      title.textContent = unread ? '알림 (' + unread + '건 미읽음)' : '알림';
    }
  }

  function mapGridRow(row) {
    if (!row) return null;
    return {
      id: row.notificationId != null ? 'db-' + row.notificationId : row.id,
      notificationId: row.notificationId,
      section: row.sectionCd || row.section || 'NEW',
      unread: row.unread !== false && row.readYn !== 'Y',
      body: row.message || row.body || '',
      timeLabel: row.notifiedDtLabel || row.timeLabel || '',
      timeIcon: normalizeActivityIcon(row.timeIcon, row.timeIconClass),
      avatarType: 'emoji'
    };
  }

  function applySearchResults(rows) {
    var items = migrateNotificationItems((rows || []).map(mapGridRow).filter(Boolean));
    pageListCache = items;
    renderIconPageList();
    return items;
  }

  function clearPageListCache() {
    pageListCache = null;
  }

  function applyExternalItems(rows) {
    if (isNotificationListPage()) {
      return applySearchResults(rows);
    }
    var items = (rows || []).map(mapGridRow).filter(Boolean);
    apiLoaded = true;
    saveItems(items);
    return items;
  }

  function renderHeaderDropdown() {
    var container = global.document.getElementById('header-notification-dropdown-list');
    if (!container) {
      return;
    }
    var items = ensureItems();
    renderGroupedIconList(container, items, DROPDOWN_LIMIT);
    updateDropdownTitle(items);
  }

  function updateDropdownTitle(items) {
    var title = global.document.getElementById('header-notification-dropdown-title');
    if (!title) return;
    var list = items || loadItems();
    var unread = getUnreadCount(list);
    title.textContent = unread ? '알림 (' + unread + '건 미읽음)' : '알림';
  }

  function renderSidebarIconList() {
    var container = global.document.getElementById('sidebar-notification-icon-list');
    if (!container) return;
    var items = ensureItems().slice(0, SIDEBAR_PREVIEW_LIMIT);
    if (!items.length) {
      container.innerHTML =
        '<li class="nav-item"><span class="nav-link text-600 py-1">알림이 없습니다.</span></li>';
      return;
    }
    container.innerHTML = items
      .map(function (item) {
        var unreadClass = item.unread !== false ? ' fw-semi-bold' : ' text-600';
        var dot = item.unread !== false ? '<span class="fas fa-circle text-warning fs-11 me-1"></span>' : '';
        return (
          '<li class="nav-item">' +
          '<a class="nav-link py-1' +
          unreadClass +
          '" href="#!" data-notification-id="' +
          escapeHtml(item.id) +
          '" data-notification-mark-read="true" title="클릭 시 읽음">' +
          dot +
          '<span class="text-truncate d-inline-block" style="max-width:11rem">' +
          stripTags(item.body) +
          '</span>' +
          '<span class="ms-1 text-500">' +
          escapeHtml(item.timeLabel || '') +
          '</span></a></li>'
        );
      })
      .join('');
  }

  function stripTags(html) {
    var div = global.document.createElement('div');
    div.innerHTML = html || '';
    var text = (div.textContent || div.innerText || '').trim();
    return escapeHtml(text.length > 48 ? text.substring(0, 48) + '…' : text);
  }

  function renderPageList() {
    var container = global.document.getElementById('notification-page-list');
    if (!container) {
      return;
    }
    var items = ensureItems();
    if (!items.length) {
      container.innerHTML = '<div class="text-center text-600 py-5">알림이 없습니다.</div>';
      return;
    }
    container.innerHTML = items
      .map(function (item, index) {
        return renderActivityLogRow(item, { isLast: index === items.length - 1 });
      })
      .join('');
    var title = global.document.getElementById('notification-page-title');
    if (title) {
      var unread = getUnreadCount(items);
      title.textContent = unread
        ? '알림 (' + unread + '건 미읽음)'
        : '알림';
    }
  }

  function parseDbId(id) {
    if (!id || String(id).indexOf('db-') !== 0) return null;
    var n = parseInt(String(id).slice(3), 10);
    return isNaN(n) ? null : n;
  }

  function markReadInPageCache(id) {
    if (!pageListCache) {
      return false;
    }
    var changed = false;
    pageListCache.forEach(function (item) {
      if (item.id === id && item.unread !== false) {
        item.unread = false;
        changed = true;
      }
    });
    if (changed) {
      renderIconPageList();
    }
    return changed;
  }

  function markRead(id) {
    var dbId = parseDbId(id);
    if (useApi && dbId != null && global.fetch) {
      return global
        .fetch(API_URL + '/' + dbId + '/read', {
          method: 'PATCH',
          headers: { Accept: 'application/json' },
          credentials: 'same-origin'
        })
        .then(function () {
          if (isNotificationListPage()) {
            markReadInPageCache(id);
            return refreshNavFromApi();
          }
          return refreshFromApi();
        })
        .catch(function () {
          if (isNotificationListPage()) {
            markReadInPageCache(id);
            refreshNavOnly();
            return true;
          }
          return markReadLocal(id);
        });
    }
    if (isNotificationListPage()) {
      markReadInPageCache(id);
      return Promise.resolve(true);
    }
    return Promise.resolve(markReadLocal(id));
  }

  function markReadLocal(id) {
    var items = loadItems();
    var changed = false;
    items.forEach(function (item) {
      if (item.id === id && item.unread !== false) {
        item.unread = false;
        changed = true;
      }
    });
    if (changed) {
      saveItems(items);
    }
    return changed;
  }

  function markAllRead() {
    if (useApi && global.fetch) {
      return global
        .fetch(API_URL + '/read-all', {
          method: 'PATCH',
          headers: { Accept: 'application/json' },
          credentials: 'same-origin'
        })
        .then(function () {
          if (isNotificationListPage() && global.PrintMallNotificationList) {
            if (pageListCache) {
              pageListCache.forEach(function (item) {
                item.unread = false;
              });
              renderIconPageList();
            }
            return refreshNavFromApi().then(function () {
              if (global.PrintMallNotificationList.reload) {
                return global.PrintMallNotificationList.reload();
              }
            });
          }
          return refreshFromApi();
        })
        .catch(function () {
          markAllReadLocal();
        });
    }
    markAllReadLocal();
    return Promise.resolve();
  }

  function markAllReadLocal() {
    if (isNotificationListPage() && pageListCache) {
      pageListCache.forEach(function (item) {
        item.unread = false;
      });
      renderIconPageList();
      refreshNavOnly();
      return;
    }
    var items = loadItems();
    var changed = false;
    items.forEach(function (item) {
      if (item.unread !== false) {
        item.unread = false;
        changed = true;
      }
    });
    if (changed) {
      saveItems(items);
    }
    refresh();
  }

  function syncHeaderActive() {
    var link = global.document.getElementById('header-notification-link');
    if (!link) {
      return;
    }
    var ctx = global.__PAGE_MENU_CONTEXT__;
    var onPage = ctx && isNotificationPage(ctx.pathKey);
    link.classList.toggle('active', !!onPage);
    if (onPage) {
      link.setAttribute('aria-current', 'page');
      link.setAttribute('title', '알림 (현재 화면)');
    } else {
      link.removeAttribute('aria-current');
      link.setAttribute('title', '알림');
    }
  }

  function bind() {
    global.document.addEventListener('click', function (e) {
      var markReadBtn = e.target.closest('[data-notification-mark-read]');
      if (markReadBtn) {
        var id = markReadBtn.getAttribute('data-notification-id');
        if (id) {
          e.preventDefault();
          markRead(id);
        }
        return;
      }
      var markAll = e.target.closest('[data-notification-mark-all]');
      if (markAll) {
        e.preventDefault();
        markAllRead();
      }
    });
  }

  function refresh() {
    refreshBadges(getUnreadCount());
    renderHeaderDropdown();
    renderSidebarIconList();
    renderIconPageList();
    renderPageList();
    syncHeaderActive();
  }

  function isNotificationPage(path) {
    var key = pathKey(path);
    return NOTIFICATION_PATH_KEYS.indexOf(key) >= 0;
  }

  function bindDropdownRefresh() {
    var link = global.document.getElementById('header-notification-link');
    if (!link) return;
    link.addEventListener('show.bs.dropdown', function () {
      refreshFromApi();
    });
  }

  function init() {
    bind();
    bindDropdownRefresh();
    var cached = loadItems();
    if (cached.length) {
      saveItems(cached);
    }
    if (isNotificationListPage()) {
      refreshNavFromApi();
      return;
    }
    refreshNavFromApi().finally(function () {
      if (!apiLoaded) {
        refresh();
      }
    });
  }

  global.PrintMallNotificationNav = {
    ACTIVITY_LOG_ICONS: ACTIVITY_LOG_ICONS,
    NOTIFICATION_PATH_KEYS: NOTIFICATION_PATH_KEYS,
    loadItems: loadItems,
    saveItems: saveItems,
    getUnreadCount: getUnreadCount,
    markRead: markRead,
    markAllRead: markAllRead,
    refresh: refresh,
    refreshFromApi: refreshFromApi,
    applySearchResults: applySearchResults,
    clearPageListCache: clearPageListCache,
    applyExternalItems: applyExternalItems,
    pathKey: pathKey
  };

  global.addEventListener('printmall-notification-updated', function () {
    if (isNotificationListPage() && pageListCache !== null) {
      refreshNavOnly();
      renderIconPageList();
      return;
    }
    refresh();
  });

  if (global.document.readyState === 'loading') {
    global.document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }

  global.document.addEventListener('printmall-paths-ready', function () {
    syncHeaderActive();
  });
})(typeof window !== 'undefined' ? window : globalThis);
