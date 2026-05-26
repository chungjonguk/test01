/**
 * 알림 — 상단 벨 아이콘 ↔ 사이드바 알림 메뉴 연동 (미읽음 수·목록·현재 페이지)
 */
(function (global) {
  'use strict';

  var NOTIFICATION_PATH_KEY = '/app/social/notifications';
  var STORAGE_KEY = 'printmall.notifications.items';
  var DROPDOWN_LIMIT = 8;

  var DEMO_ITEMS = [
    {
      id: 'demo-n1',
      section: 'NEW',
      unread: true,
      body: '<strong>Emma Watson</strong> replied to your comment : "Hello world 😍"',
      timeLabel: 'Just now',
      timeIcon: '💬',
      avatarSrc: '/assets/img/team/1-thumb.png',
      avatarType: 'img'
    },
    {
      id: 'demo-n2',
      section: 'NEW',
      unread: true,
      body: '<strong>Albert Brooks</strong> reacted to <strong>Mia Khalifa\'s</strong> status',
      timeLabel: '9hr',
      timeIcon: '',
      timeIconClass: 'fab fa-gratipay text-danger',
      avatarType: 'initials',
      avatarInitials: 'AB'
    },
    {
      id: 'demo-n3',
      section: 'EARLIER',
      unread: false,
      body: 'The forecast today shows a low of 20℃ in California. See today\'s weather.',
      timeLabel: '1d',
      timeIcon: '🌤️',
      avatarSrc: '/assets/img/icons/weather-sm.jpg',
      avatarType: 'img'
    },
    {
      id: 'demo-n4',
      section: 'EARLIER',
      unread: true,
      body: '<strong>University of Oxford</strong> created an event : "Causal Inference Hilary 2019"',
      timeLabel: '1w',
      timeIcon: '✌️',
      avatarSrc: '/assets/img/logos/oxford.png',
      avatarType: 'img'
    },
    {
      id: 'demo-n5',
      section: 'EARLIER',
      unread: false,
      body: '<strong>James Cameron</strong> invited to join the group: United Nations International Children\'s Fund',
      timeLabel: '2d',
      timeIcon: '🙋‍',
      avatarSrc: '/assets/img/team/10.jpg',
      avatarType: 'img'
    },
    {
      id: 'demo-n6',
      section: '',
      unread: true,
      body: 'Announcing the winners of the <strong>The only book awards</strong> decided by you, the readers.',
      timeLabel: 'Just Now',
      timeIcon: '📢',
      avatarSrc: '/assets/img/team/1.jpg',
      avatarType: 'img'
    },
    {
      id: 'demo-n7',
      section: '',
      unread: true,
      body: 'Last chance to vote in <strong>The 2018 PrintMall Choice Awards</strong>!',
      timeLabel: '15m',
      timeIcon: '🏆',
      avatarSrc: '/assets/img/team/2.jpg',
      avatarType: 'img'
    },
    {
      id: 'demo-n8',
      section: '',
      unread: false,
      body: '<strong>Jennifer Kent</strong> declared you as a <strong>President</strong> of Computer Science and Engineering Society',
      timeLabel: '1h',
      timeIcon: '📢',
      avatarSrc: '/assets/img/team/3.jpg',
      avatarType: 'img'
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

  function loadItems() {
    try {
      var raw = global.localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return [];
      }
      var parsed = JSON.parse(raw);
      return Array.isArray(parsed) ? parsed : [];
    } catch (e) {
      return [];
    }
  }

  function saveItems(items) {
    var list = Array.isArray(items) ? items : [];
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

  function ensureItems() {
    var items = loadItems();
    if (!items.length) {
      items = DEMO_ITEMS.map(function (item) {
        return Object.assign({}, item);
      });
      saveItems(items);
    }
    return items;
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

  function renderAvatar(item) {
    if (item.avatarType === 'initials') {
      var initials = escapeHtml(item.avatarInitials || '?');
      return (
        '<div class="avatar avatar-2xl me-3"><div class="avatar-name rounded-circle"><span>' +
        initials +
        '</span></div></div>'
      );
    }
    var src = escapeHtml(item.avatarSrc || '/assets/img/team/1-thumb.png');
    return (
      '<div class="avatar avatar-2xl me-3"><img class="rounded-circle" src="' +
      src +
      '" alt="" onerror="this.src=\'/assets/img/team/1-thumb.png\'"/></div>'
    );
  }

  function renderTimeIcon(item) {
    if (item.timeIconClass) {
      return '<span class="me-2 ' + escapeHtml(item.timeIconClass) + '"></span>';
    }
    if (item.timeIcon) {
      return '<span class="me-2" role="img" aria-label="">' + item.timeIcon + '</span>';
    }
    return '';
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
      renderAvatar(item) +
      '</div>' +
      '<div class="notification-body">' +
      '<p class="mb-1">' +
      item.body +
      '</p>' +
      '<span class="notification-time">' +
      renderTimeIcon(item) +
      escapeHtml(item.timeLabel || '') +
      '</span></div></a></div>'
    );
  }

  function renderPageRow(item) {
    var unreadClass = item.unread !== false ? ' notification-unread' : '';
    var avatarSize = item.avatarType === 'initials' ? 'avatar-2xl' : 'avatar-xl';
    var avatarHtml =
      item.avatarType === 'initials'
        ? '<div class="avatar ' +
          avatarSize +
          ' me-3"><div class="avatar-name rounded-circle"><span>' +
          escapeHtml(item.avatarInitials || '?') +
          '</span></div></div>'
        : '<div class="avatar ' +
          avatarSize +
          ' me-3"><img class="rounded-circle" src="' +
          escapeHtml(item.avatarSrc || '/assets/img/team/1.jpg') +
          '" alt="" onerror="this.src=\'/assets/img/team/1.jpg\'"/></div>';
    return (
      '<a class="border-bottom-0 notification rounded-0 border-x-0 border-300' +
      unreadClass +
      '" href="#!" data-notification-id="' +
      escapeHtml(item.id) +
      '" data-notification-mark-read="true">' +
      '<div class="notification-avatar">' +
      avatarHtml +
      '</div>' +
      '<div class="notification-body">' +
      '<p class="mb-1">' +
      item.body +
      '</p>' +
      '<span class="notification-time">' +
      renderTimeIcon(item) +
      escapeHtml(item.timeLabel || '') +
      '</span></div></a>'
    );
  }

  function groupForDropdown(items) {
    var list = items.slice(0, DROPDOWN_LIMIT);
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

  function renderHeaderDropdown() {
    var container = global.document.getElementById('header-notification-dropdown-list');
    if (!container) {
      return;
    }
    var items = ensureItems();
    if (!items.length) {
      container.innerHTML =
        '<div class="list-group-item text-center text-600 py-4">알림이 없습니다.</div>';
      return;
    }
    var grouped = groupForDropdown(items);
    var html = '';
    grouped.sections.forEach(function (section) {
      var sectionItems = grouped.list.filter(function (item) {
        var s = item.section || 'EARLIER';
        return s === section;
      });
      if (!sectionItems.length) {
        return;
      }
      if (section === 'NEW' || section === 'EARLIER') {
        html += '<div class="list-group-title border-bottom">' + escapeHtml(section) + '</div>';
      }
      sectionItems.forEach(function (item) {
        html += renderHeaderRow(item);
      });
    });
    container.innerHTML = html;
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
    container.innerHTML = items.map(renderPageRow).join('');
    var title = global.document.getElementById('notification-page-title');
    if (title) {
      var unread = getUnreadCount(items);
      title.textContent = unread
        ? '알림 (' + unread + '건 미읽음)'
        : '알림';
    }
  }

  function markRead(id) {
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
    renderHeaderDropdown();
    renderPageList();
    return changed;
  }

  function syncHeaderActive() {
    var link = global.document.getElementById('header-notification-link');
    if (!link) {
      return;
    }
    var ctx = global.__PAGE_MENU_CONTEXT__;
    var onPage = ctx && pathKey(ctx.pathKey) === NOTIFICATION_PATH_KEY;
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
          renderHeaderDropdown();
          renderPageList();
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
    renderPageList();
    syncHeaderActive();
  }

  function init() {
    bind();
    refresh();
  }

  global.PrintMallNotificationNav = {
    NOTIFICATION_PATH_KEY: NOTIFICATION_PATH_KEY,
    loadItems: loadItems,
    saveItems: saveItems,
    getUnreadCount: getUnreadCount,
    markRead: markRead,
    markAllRead: markAllRead,
    refresh: refresh,
    pathKey: pathKey
  };

  global.addEventListener('printmall-notification-updated', function () {
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
