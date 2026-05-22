/**
 * 캘린더 — DB 연동 (FullCalendar + /api/calendar/events)
 */
(function () {
  'use strict';

  if (!window.__CALENDAR_USE_DB__) {
    return;
  }

  var API_BASE = window.__CALENDAR_API_BASE__ || '/api/calendar/events';

  var Selectors = {
    CALENDAR: '#appCalendar',
    CALENDAR_TITLE: '.calendar-title',
    ADD_EVENT_FORM: '#addEventForm',
    ADD_EVENT_MODAL: '#addEventModal',
    ADD_EVENT_MODAL_TITLE: '#addEventModalTitle',
    EVENT_DETAILS_MODAL: '#eventDetailsModal',
    EVENT_DETAILS_MODAL_CONTENT: '#eventDetailsModal .modal-content',
    DATA_CALENDAR_VIEW: '[data-fc-view]',
    DATA_EVENT: '[data-event]',
    DATA_VIEW_TITLE: '[data-view-title]',
    EVENT_START_DATE: '#addEventModal [name="startDate"]',
    EVENT_DELETE_BTN: '#eventDeleteBtn',
    ACTIVE: '.active'
  };

  var state = {
    calendar: null,
    editingEventId: null
  };

  function $(sel) {
    return document.querySelector(sel);
  }

  function apiUrl(path) {
    var base = API_BASE.replace(/\/$/, '');
    return path ? base + path : base;
  }

  function toast(icon, title, text) {
    if (window.Swal) {
      window.Swal.fire({ icon: icon, title: title, text: text || '', timer: icon === 'success' ? 2000 : undefined });
      return;
    }
    window.alert(title + (text ? '\n' + text : ''));
  }

  function fetchJson(url, options) {
    options = options || {};
    options.headers = Object.assign(
      { Accept: 'application/json' },
      options.body ? { 'Content-Type': 'application/json' } : {},
      options.headers || {}
    );
    return fetch(url, options).then(function (res) {
      return res.json().then(function (data) {
        return { ok: res.ok, status: res.status, data: data };
      });
    });
  }

  function normalizeDateTime(value) {
    if (!value || !String(value).trim()) {
      return null;
    }
    return String(value).trim().replace('T', ' ');
  }

  function formatDayjs(dt) {
    if (!window.dayjs || !dt) {
      return '';
    }
    return window.dayjs(dt).format('YYYY-MM-DD HH:mm');
  }

  function eventToPayload(fcEvent) {
    var ext = fcEvent.extendedProps || {};
    var allDay = !!fcEvent.allDay;
    var start = fcEvent.start;
    var end = fcEvent.end;
    var startDt;
    var endDt;
    if (allDay) {
      startDt = start ? window.dayjs(start).format('YYYY-MM-DD') + ' 00:00' : null;
      endDt = end ? window.dayjs(end).subtract(1, 'day').format('YYYY-MM-DD') + ' 23:59' : startDt;
    } else {
      startDt = start ? formatDayjs(start) : null;
      endDt = end ? formatDayjs(end) : null;
    }
    var labelCd = ext.labelCd;
    if (!labelCd && fcEvent.classNames && fcEvent.classNames.length) {
      var m = String(fcEvent.classNames[0]).match(/bg-soft-(\w+)/);
      if (m) {
        labelCd = m[1];
      }
    }
    return {
      eventId: ext.eventId || (fcEvent.id ? parseInt(fcEvent.id, 10) : null),
      title: fcEvent.title,
      categoryCd: ext.categoryCd || null,
      labelCd: labelCd || null,
      startDt: startDt,
      endDt: endDt,
      location: ext.location || null,
      description: ext.description || null,
      allDay: allDay
    };
  }

  function formToPayload(form) {
    var fd = new FormData(form);
    var allDay = form.allDay && form.allDay.checked;
    return {
      eventId: fd.get('eventId') ? parseInt(fd.get('eventId'), 10) : null,
      title: fd.get('title'),
      categoryCd: fd.get('category') || null,
      labelCd: fd.get('label') || null,
      startDt: normalizeDateTime(fd.get('startDate')),
      endDt: normalizeDateTime(fd.get('endDate')),
      location: fd.get('location') || null,
      description: fd.get('description') || null,
      allDay: allDay
    };
  }

  function toRangeParam(value) {
    if (!value) {
      return '';
    }
    if (window.dayjs) {
      return window.dayjs(value).format('YYYY-MM-DD HH:mm:ss');
    }
    var s = typeof value === 'string' ? value : value.toISOString();
    return s.substring(0, 19).replace('T', ' ');
  }

  function loadEvents(info, successCallback, failureCallback) {
    var start = toRangeParam(info.startStr || info.start);
    var end = toRangeParam(info.endStr || info.end);
    var url = apiUrl('?start=' + encodeURIComponent(start) + '&end=' + encodeURIComponent(end));
    fetchJson(url)
      .then(function (r) {
        if (!r.ok) {
          throw new Error((r.data && r.data.message) || '일정 조회에 실패했습니다.');
        }
        successCallback((r.data && r.data.events) || []);
      })
      .catch(function (err) {
        toast('error', '일정 조회 실패', err.message);
        if (failureCallback) {
          failureCallback(err);
        }
      });
  }

  function savePayload(payload) {
    var isEdit = payload.eventId != null && !isNaN(payload.eventId);
    var url = isEdit ? apiUrl('/' + payload.eventId) : apiUrl('');
    var method = isEdit ? 'PUT' : 'POST';
    var body = Object.assign({}, payload);
    delete body.eventId;
    return fetchJson(url, { method: method, body: JSON.stringify(body) }).then(function (r) {
      if (!r.ok) {
        throw new Error((r.data && r.data.message) || '저장에 실패했습니다.');
      }
      return r.data;
    });
  }

  function deleteEvent(eventId) {
    return fetchJson(apiUrl('/' + eventId), { method: 'DELETE' }).then(function (r) {
      if (!r.ok) {
        throw new Error((r.data && r.data.message) || '삭제에 실패했습니다.');
      }
      return r.data;
    });
  }

  function updateTitle(calendar) {
    var el = $(Selectors.CALENDAR_TITLE);
    if (el && calendar) {
      el.textContent = calendar.currentData.viewTitle;
    }
  }

  function resetForm(form) {
    form.reset();
    var idInput = form.querySelector('[name="eventId"]');
    if (idInput) {
      idInput.value = '';
    }
    state.editingEventId = null;
    var titleEl = $(Selectors.ADD_EVENT_MODAL_TITLE);
    if (titleEl) {
      titleEl.textContent = '일정 만들기';
    }
    var delBtn = $(Selectors.EVENT_DELETE_BTN);
    if (delBtn) {
      delBtn.classList.add('d-none');
    }
  }

  function fillFormFromEvent(fcEvent) {
    var form = $(Selectors.ADD_EVENT_FORM);
    if (!form) {
      return;
    }
    var ext = fcEvent.extendedProps || {};
    var allDay = !!fcEvent.allDay;
    form.eventId.value = ext.eventId || fcEvent.id || '';
    form.title.value = fcEvent.title || '';
    form.allDay.checked = allDay;
    if (form.category) {
      form.category.value = ext.categoryCd || '';
    }
    if (form.label) {
      form.label.value = ext.labelCd || '';
    }
    form.location.value = ext.location || '';
    form.description.value = ext.description || '';

    var startFp = form.startDate && form.startDate._flatpickr;
    var endFp = form.endDate && form.endDate._flatpickr;
    if (startFp && fcEvent.start) {
      startFp.setDate(fcEvent.start, true);
    }
    if (endFp) {
      if (fcEvent.end) {
        var endDate = allDay && fcEvent.end ? window.dayjs(fcEvent.end).subtract(1, 'day').toDate() : fcEvent.end;
        endFp.setDate(endDate, true);
      } else {
        endFp.clear();
      }
    }

    state.editingEventId = form.eventId.value || null;
    var titleEl = $(Selectors.ADD_EVENT_MODAL_TITLE);
    if (titleEl) {
      titleEl.textContent = '일정 수정';
    }
    var delBtn = $(Selectors.EVENT_DELETE_BTN);
    if (delBtn) {
      delBtn.classList.remove('d-none');
    }
  }

  function buildDetailHtml(fcEvent) {
    var ext = fcEvent.extendedProps || {};
    var dateLine = '';
    if (window.dayjs && fcEvent.start) {
      dateLine = window.dayjs(fcEvent.start).format('YYYY-MM-DD HH:mm');
      if (fcEvent.end && !fcEvent.allDay) {
        dateLine += ' – ' + window.dayjs(fcEvent.end).format('YYYY-MM-DD HH:mm');
      } else if (fcEvent.end && fcEvent.allDay) {
        dateLine += ' – ' + window.dayjs(fcEvent.end).subtract(1, 'day').format('YYYY-MM-DD');
      }
    }
    var desc = ext.description ? '<p class="mb-0">' + escapeHtml(ext.description) + '</p>' : '';
    var loc = ext.location
      ? '<div class="d-flex mt-3"><div class="flex-1"><h6>장소</h6><p class="mb-0">' + escapeHtml(ext.location) + '</p></div></div>'
      : '';
    return (
      '<div class="modal-header bg-light ps-card pe-5 border-bottom-0">' +
      '<div><h5 class="modal-title mb-0">' +
      escapeHtml(fcEvent.title) +
      '</h5></div>' +
      '<button type="button" class="btn-close position-absolute end-0 top-0 mt-3 me-3" data-bs-dismiss="modal" aria-label="Close"></button>' +
      '</div>' +
      '<div class="modal-body px-card pb-card pt-1 fs--1">' +
      (desc ? '<div class="mt-2"><h6>설명</h6>' + desc + '</div>' : '') +
      '<div class="d-flex mt-3"><div class="flex-1"><h6>일시</h6><p class="mb-0">' +
      escapeHtml(dateLine) +
      '</p></div></div>' +
      loc +
      '</div>' +
      '<div class="modal-footer d-flex justify-content-end bg-light px-card border-top-0 gap-2">' +
      '<button type="button" class="btn btn-falcon-danger btn-sm" data-calendar-action="delete">삭제</button>' +
      '<button type="button" class="btn btn-falcon-default btn-sm" data-calendar-action="edit">수정</button>' +
      '<button type="button" class="btn btn-falcon-primary btn-sm" data-bs-dismiss="modal">닫기</button>' +
      '</div>'
    );
  }

  function escapeHtml(v) {
    return String(v == null ? '' : v)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function init() {
    var appCalendarEl = $(Selectors.CALENDAR);
    if (!appCalendarEl || typeof window.renderCalendar !== 'function') {
      return;
    }

    var addEventForm = $(Selectors.ADD_EVENT_FORM);
    var addEventModal = $(Selectors.ADD_EVENT_MODAL);
    var eventDetailsModal = $(Selectors.EVENT_DETAILS_MODAL);
    var selectedEvent = null;

    var calendar = window.renderCalendar(appCalendarEl, {
      headerToolbar: false,
      dayMaxEvents: 2,
      height: 800,
      stickyHeaderDates: false,
      views: {
        week: { eventLimit: 3 }
      },
      eventTimeFormat: {
        hour: 'numeric',
        minute: '2-digit',
        omitZeroMinute: true,
        meridiem: true
      },
      events: loadEvents,
      editable: true,
      eventClick: function (info) {
        if (info.event.url) {
          window.open(info.event.url, '_blank');
          info.jsEvent.preventDefault();
          return;
        }
        selectedEvent = info.event;
        var content = $(Selectors.EVENT_DETAILS_MODAL_CONTENT);
        if (content) {
          content.innerHTML = buildDetailHtml(info.event);
          var modal = new window.bootstrap.Modal(eventDetailsModal);
          modal.show();
        }
      },
      dateClick: function (info) {
        resetForm(addEventForm);
        var modal = new window.bootstrap.Modal(addEventModal);
        modal.show();
        var startInput = $(Selectors.EVENT_START_DATE);
        var fp = startInput && startInput._flatpickr;
        if (fp) {
          fp.setDate([info.dateStr]);
        }
      },
      eventDrop: function (info) {
        persistDragResize(info);
      },
      eventResize: function (info) {
        persistDragResize(info);
      }
    });

    state.calendar = calendar;
    updateTitle(calendar);

    function persistDragResize(info) {
      var payload = eventToPayload(info.event);
      savePayload(payload)
        .then(function () {
          toast('success', '일정이 변경되었습니다.');
        })
        .catch(function (err) {
          info.revert();
          toast('error', '변경 실패', err.message);
        });
    }

    document.querySelectorAll(Selectors.DATA_EVENT).forEach(function (button) {
      button.addEventListener('click', function (e) {
        var el = e.currentTarget;
        var type = el.getAttribute('data-event');
        switch (type) {
          case 'prev':
            calendar.prev();
            break;
          case 'next':
            calendar.next();
            break;
          case 'today':
          default:
            calendar.today();
            break;
        }
        updateTitle(calendar);
      });
    });

    document.querySelectorAll(Selectors.DATA_CALENDAR_VIEW).forEach(function (link) {
      link.addEventListener('click', function (e) {
        e.preventDefault();
        var el = e.currentTarget;
        var text = el.textContent.trim();
        el.parentElement.querySelector(Selectors.ACTIVE).classList.remove('active');
        el.classList.add('active');
        var viewTitle = $(Selectors.DATA_VIEW_TITLE);
        if (viewTitle) {
          viewTitle.textContent = text;
        }
        var view = el.getAttribute('data-fc-view');
        if (view) {
          calendar.changeView(view);
        }
        updateTitle(calendar);
      });
    });

    if (addEventForm) {
      addEventForm.addEventListener('submit', function (e) {
        e.preventDefault();
        var payload = formToPayload(addEventForm);
        savePayload(payload)
          .then(function () {
            toast('success', payload.eventId ? '일정이 수정되었습니다.' : '일정이 등록되었습니다.');
            calendar.refetchEvents();
            resetForm(addEventForm);
            var inst = window.bootstrap.Modal.getInstance(addEventModal);
            if (inst) {
              inst.hide();
            }
          })
          .catch(function (err) {
            toast('error', '저장 실패', err.message);
          });
      });
    }

    var deleteBtn = $(Selectors.EVENT_DELETE_BTN);
    if (deleteBtn) {
      deleteBtn.addEventListener('click', function () {
        var id = state.editingEventId || (addEventForm && addEventForm.eventId.value);
        if (!id) {
          return;
        }
        confirmDelete(id, function () {
          calendar.refetchEvents();
          resetForm(addEventForm);
          var inst = window.bootstrap.Modal.getInstance(addEventModal);
          if (inst) {
            inst.hide();
          }
        });
      });
    }

    if (eventDetailsModal) {
      eventDetailsModal.addEventListener('click', function (e) {
        var btn = e.target.closest('[data-calendar-action]');
        if (!btn || !selectedEvent) {
          return;
        }
        var action = btn.getAttribute('data-calendar-action');
        if (action === 'edit') {
          fillFormFromEvent(selectedEvent);
          window.bootstrap.Modal.getInstance(eventDetailsModal).hide();
          new window.bootstrap.Modal(addEventModal).show();
        } else if (action === 'delete') {
          var ext = selectedEvent.extendedProps || {};
          var id = ext.eventId || selectedEvent.id;
          confirmDelete(id, function () {
            calendar.refetchEvents();
            window.bootstrap.Modal.getInstance(eventDetailsModal).hide();
            selectedEvent = null;
          });
        }
      });
    }

    if (addEventModal) {
      addEventModal.addEventListener('shown.bs.modal', function () {
        var input = addEventForm && addEventForm.querySelector('[name="title"]');
        if (input) {
          input.focus();
        }
      });
      addEventModal.addEventListener('hidden.bs.modal', function () {
        if (!state.editingEventId) {
          resetForm(addEventForm);
        }
      });
    }

    document.querySelector('[data-bs-target="#addEventModal"]') &&
      document.querySelector('[data-bs-target="#addEventModal"]').addEventListener('click', function () {
        resetForm(addEventForm);
      });
  }

  function confirmDelete(eventId, onDone) {
    var run = function () {
      deleteEvent(eventId)
        .then(function () {
          toast('success', '일정이 삭제되었습니다.');
          if (onDone) {
            onDone();
          }
          if (state.calendar) {
            state.calendar.refetchEvents();
          }
        })
        .catch(function (err) {
          toast('error', '삭제 실패', err.message);
        });
    };
    if (window.Swal) {
      window.Swal.fire({
        icon: 'warning',
        title: '일정 삭제',
        text: '이 일정을 삭제할까요?',
        showCancelButton: true,
        confirmButtonText: '삭제',
        cancelButtonText: '취소'
      }).then(function (r) {
        if (r.isConfirmed) {
          run();
        }
      });
      return;
    }
    if (window.confirm('이 일정을 삭제할까요?')) {
      run();
    }
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
