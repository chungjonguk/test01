/**
 * 사용자 설정 — 프로필 이미지·기본 정보 저장
 */
(function () {
  'use strict';

  var API = '/api/user/profile-images';
  var PROFILE_API = '/api/user/profile-settings';
  var DEFAULT_COVER = '/assets/img/generic/4.jpg';
  var PROFILE_MAX_BYTES = 2 * 1024 * 1024;
  var COVER_MAX_BYTES = 5 * 1024 * 1024;
  var addressForms = {};

  function $(id) {
    return document.getElementById(id);
  }

  function getAddressForm(prefix) {
    if (!$(prefix + '-address-zip') || !window.PrintMallKakaoAddressForm) {
      return null;
    }
    if (!addressForms[prefix]) {
      addressForms[prefix] = window.PrintMallKakaoAddressForm.create({ idPrefix: prefix });
    }
    if (addressForms[prefix]) {
      addressForms[prefix].bind();
      addressForms[prefix].onContainerShown();
    }
    return addressForms[prefix] || null;
  }

  function initAddressForms() {
    getAddressForm('home');
    getAddressForm('work');
    bindPrimaryAddressCheckboxes();
  }

  function bindPrimaryAddressCheckboxes() {
    var boxes = document.querySelectorAll('.settings-primary-address');
    if (!boxes.length) {
      return;
    }
    boxes.forEach(function (box) {
      box.addEventListener('change', function () {
        if (!box.checked) {
          var anyChecked = false;
          boxes.forEach(function (b) {
            if (b.checked) {
              anyChecked = true;
            }
          });
          if (!anyChecked) {
            box.checked = true;
          }
          return;
        }
        boxes.forEach(function (other) {
          if (other !== box) {
            other.checked = false;
          }
        });
      });
    });
  }

  function getPrimaryAddressType() {
    var checked = document.querySelector('.settings-primary-address:checked');
    if (checked && checked.getAttribute('data-address-type') === 'WORK') {
      return 'WORK';
    }
    return 'HOME';
  }

  function setPrimaryAddressType(type) {
    var target = type === 'WORK' ? 'WORK' : 'HOME';
    document.querySelectorAll('.settings-primary-address').forEach(function (box) {
      box.checked = box.getAttribute('data-address-type') === target;
    });
  }

  function readAddressFields(prefix) {
    return {
      zipcode: (($(prefix + '-address-zip') || {}).value || '').trim(),
      address: (($(prefix + '-address-base') || {}).value || '').trim(),
      addressDetail: (($(prefix + '-address-detail') || {}).value || '').trim()
    };
  }

  function collectAddressByPrefix(prefix) {
    return readAddressFields(prefix);
  }

  function validatePhoneInput(el, label) {
    if (!el) {
      return true;
    }
    var raw = (el.value || '').trim();
    if (!raw) {
      el.classList.remove('is-invalid');
      return true;
    }
    var fmt = window.PrintMallPhoneFormat;
    if (fmt) {
      el.value = fmt.format(raw);
      if (!fmt.isValid(el.value)) {
        el.classList.add('is-invalid');
        var phoneMsg = label + ' 형식이 올바르지 않습니다. 예: 010-1234-5678, 02-1234-5678';
        if (window.Swal) {
          window.Swal.fire({
            icon: 'warning',
            title: '입력 확인',
            text: phoneMsg,
            confirmButtonText: '확인'
          });
        } else {
          window.alert(phoneMsg);
        }
        el.focus();
        return false;
      }
      el.classList.remove('is-invalid');
      return true;
    }
    return true;
  }

  function bindPhoneInputs() {
    var fmt = window.PrintMallPhoneFormat;
    if (!fmt) {
      return;
    }
    fmt.bindInput($('home-phone'), { label: '자택 전화번호' });
    fmt.bindInput($('work-phone'), { label: '직장 전화번호' });
  }

  function fillAddressByPrefix(prefix, zipcode, address, addressDetail) {
    var af = getAddressForm(prefix);
    if (af) {
      af.fill(zipcode || '', address || '', addressDetail || '');
      return;
    }
    var zipEl = $(prefix + '-address-zip');
    var baseEl = $(prefix + '-address-base');
    var detailEl = $(prefix + '-address-detail');
    if (zipEl) {
      zipEl.value = zipcode || '';
    }
    if (baseEl) {
      baseEl.value = address || '';
    }
    if (detailEl) {
      detailEl.value = addressDetail || '';
    }
  }

  function collectProfilePayload() {
    var home = collectAddressByPrefix('home');
    var work = collectAddressByPrefix('work');
    return {
      name: (($('settings-user-name') || {}).value || '').trim(),
      email: (($('settings-user-email') || {}).value || '').trim(),
      homeZipcode: home.zipcode,
      homeAddress: home.address,
      homeAddressDetail: home.addressDetail,
      homePhone: (($('home-phone') || {}).value || '').trim(),
      workZipcode: work.zipcode,
      workAddress: work.address,
      workAddressDetail: work.addressDetail,
      workPhone: (($('work-phone') || {}).value || '').trim(),
      workCompanyName: (($('work-company-name') || {}).value || '').trim(),
      primaryAddressType: getPrimaryAddressType()
    };
  }

  function showMessage(text, isError) {
    var box = $('user-settings-message');
    if (!box) {
      return;
    }
    box.innerHTML =
      '<div class="alert alert-' +
      (isError ? 'danger' : 'success') +
      ' py-2 fs--1 mb-0" role="alert">' +
      text +
      '</div>';
  }

  function showRequiredAlert(missingLabels) {
    var list = missingLabels && missingLabels.length ? missingLabels.join(', ') : '';
    var text = list
      ? '필수 정보를 입력해 주세요.\n\n미입력: ' + list
      : '필수 정보를 입력해 주세요.';
    if (window.Swal) {
      window.Swal.fire({
        icon: 'warning',
        title: '필수 정보',
        text: text,
        confirmButtonText: '확인'
      });
      return;
    }
    window.alert(text);
  }

  function showSuccessAlert(message) {
    var text = message || '프로필이 저장되었습니다.';
    if (window.Swal) {
      window.Swal.fire({
        icon: 'success',
        title: '저장 완료',
        text: text,
        confirmButtonText: '확인'
      });
      return;
    }
    window.alert(text);
  }

  function focusField(id) {
    var el = $(id);
    if (el && typeof el.focus === 'function') {
      el.focus();
    }
  }

  function validateRequiredFields() {
    var checks = [
      { id: 'settings-user-id', label: '아이디', value: (($('settings-user-id') || {}).value || '').trim() },
      { id: 'settings-user-name', label: '이름', value: (($('settings-user-name') || {}).value || '').trim() },
      { id: 'settings-user-email', label: '이메일', value: (($('settings-user-email') || {}).value || '').trim() }
    ];
    var missing = [];
    checks.forEach(function (c) {
      if (!c.value) {
        missing.push(c.label);
      }
    });
    if (missing.length) {
      showRequiredAlert(missing);
      for (var i = 0; i < checks.length; i++) {
        if (!checks[i].value) {
          focusField(checks[i].id);
          break;
        }
      }
      return false;
    }
    var email = checks[2].value;
    if (email.indexOf('@') < 0 || email.indexOf('.') < 0) {
      if (window.Swal) {
        window.Swal.fire({
          icon: 'warning',
          title: '필수 정보',
          text: '올바른 이메일 형식을 입력해 주세요.',
          confirmButtonText: '확인'
        });
      } else {
        window.alert('올바른 이메일 형식을 입력해 주세요.');
      }
      focusField('settings-user-email');
      return false;
    }
    return true;
  }

  function parseResponse(res) {
    var ct = res.headers.get('content-type') || '';
    if (ct.indexOf('application/json') >= 0) {
      return res.json().then(function (data) {
        return { ok: res.ok, status: res.status, data: data };
      });
    }
    return Promise.resolve({
      ok: res.ok,
      status: res.status,
      data: { success: false, message: '서버 오류 (' + res.status + ')' }
    });
  }

  function formatMaxMb(bytes) {
    return Math.round(bytes / 1024 / 1024) + 'MB';
  }

  function validateSize(file, maxBytes, label) {
    if (file.size > maxBytes) {
      showMessage(label + '은(는) ' + formatMaxMb(maxBytes) + ' 이하만 등록할 수 있습니다.', true);
      return false;
    }
    return true;
  }

  function syncHeaderAvatar(url) {
    if (window.PrintMallUserNavAvatar && typeof window.PrintMallUserNavAvatar.apply === 'function') {
      window.PrintMallUserNavAvatar.apply(url);
    }
  }

  function refreshHeaderDisplayName(name) {
    var trimmed = name && String(name).trim();
    if (!trimmed) {
      return;
    }
    var label = trimmed + '님';
    var initial = trimmed.substring(0, 1);
    document.querySelectorAll('.user-nav-name').forEach(function (el) {
      el.textContent = label;
    });
    document.querySelectorAll('.user-nav-avatar-initial').forEach(function (el) {
      if (!el.classList.contains('d-none')) {
        el.textContent = initial;
      }
    });
    var settingsInitial = $('user-settings-profile-initial');
    if (settingsInitial && !settingsInitial.classList.contains('d-none')) {
      settingsInitial.textContent = initial;
    }
  }

  function fillProfileForm(data) {
    if (!data) {
      return;
    }
    var nameEl = $('settings-user-name');
    var emailEl = $('settings-user-email');
    var idEl = $('settings-user-id');
    if (idEl && data.userId) {
      idEl.value = data.userId;
    }
    if (nameEl && data.name != null) {
      nameEl.value = data.name;
    }
    if (emailEl && data.email != null) {
      emailEl.value = data.email;
    }
    fillAddressByPrefix('home', data.homeZipcode, data.homeAddress, data.homeAddressDetail);
    fillAddressByPrefix('work', data.workZipcode, data.workAddress, data.workAddressDetail);
    var homePhone = $('home-phone');
    var workPhone = $('work-phone');
    var workCompany = $('work-company-name');
    if (homePhone && data.homePhone != null) {
      homePhone.value =
        window.PrintMallPhoneFormat && window.PrintMallPhoneFormat.format
          ? window.PrintMallPhoneFormat.format(data.homePhone)
          : data.homePhone;
      homePhone.classList.remove('is-invalid');
    }
    if (workPhone && data.workPhone != null) {
      workPhone.value =
        window.PrintMallPhoneFormat && window.PrintMallPhoneFormat.format
          ? window.PrintMallPhoneFormat.format(data.workPhone)
          : data.workPhone;
      workPhone.classList.remove('is-invalid');
    }
    if (workCompany && data.workCompanyName != null) {
      workCompany.value = data.workCompanyName;
    }
    setPrimaryAddressType(data.primaryAddressType);
  }

  function upload(file, endpoint, maxBytes, sizeLabel, onSuccess) {
    if (!file || !file.type || file.type.indexOf('image/') !== 0) {
      showMessage('이미지 파일만 업로드할 수 있습니다.', true);
      return;
    }
    if (!validateSize(file, maxBytes, sizeLabel)) {
      return;
    }
    var form = new FormData();
    form.append('file', file);
    showMessage('업로드 중…', false);
    fetch(API + endpoint, { method: 'POST', body: form, credentials: 'same-origin' })
      .then(parseResponse)
      .then(function (result) {
        if (result.status === 401) {
          window.location.href =
            '/pages/authentication/simple/login?returnUrl=' +
            encodeURIComponent(window.location.pathname + window.location.search);
          return;
        }
        if (!result.ok || !result.data || !result.data.success) {
          throw new Error((result.data && result.data.message) || '업로드 실패 (' + result.status + ')');
        }
        onSuccess(result.data);
        showMessage(result.data.message || '등록되었습니다.', false);
      })
      .catch(function (err) {
        showMessage(err.message || '업로드 실패', true);
      });
  }

  function applyProfileUrl(url) {
    var img = $('user-settings-profile-img');
    var initial = $('user-settings-profile-initial');
    if (!img) {
      return;
    }
    if (url) {
      img.src = url + (url.indexOf('?') >= 0 ? '&' : '?') + 't=' + Date.now();
      img.classList.remove('d-none');
      if (initial) {
        initial.classList.add('d-none');
        initial.classList.remove('d-flex');
      }
    } else {
      img.classList.add('d-none');
      if (initial) {
        initial.classList.remove('d-none');
        initial.classList.add('d-flex');
      }
    }
    syncHeaderAvatar(url);
  }

  function applyCoverUrl(url) {
    var bg = document.querySelector('.user-settings-cover-bg');
    if (!bg) {
      return;
    }
    bg.style.backgroundImage = 'url(' + (url || DEFAULT_COVER) + ')';
  }

  function updateHint(profileMax, coverMax) {
    var hint = $('user-settings-profile-hint');
    if (!hint) {
      return;
    }
    if (profileMax && coverMax) {
      hint.textContent =
        '프로필 사진: JPG·PNG·GIF·WebP, 최대 ' +
        formatMaxMb(profileMax) +
        ' · 커버: 최대 ' +
        formatMaxMb(coverMax);
    }
  }

  function bindUploads() {
    var profileInput = $('profile-image');
    var coverInput = $('upload-cover-image');

    if (profileInput) {
      profileInput.addEventListener('change', function () {
        var file = profileInput.files && profileInput.files[0];
        if (!file) {
          return;
        }
        upload(file, '/profile', PROFILE_MAX_BYTES, '프로필 사진', function (data) {
          var url = data.image && data.image.url;
          applyProfileUrl(url);
          profileInput.value = '';
        });
      });
    }

    if (coverInput) {
      coverInput.addEventListener('change', function () {
        var file = coverInput.files && coverInput.files[0];
        if (!file) {
          return;
        }
        upload(file, '/cover', COVER_MAX_BYTES, '커버 이미지', function (data) {
          var url = data.image && data.image.url;
          applyCoverUrl(url);
          coverInput.value = '';
        });
      });
    }
  }

  function bindProfileForm() {
    var form = $('user-settings-profile-form');
    if (!form) {
      return;
    }
    form.addEventListener('submit', function (e) {
      e.preventDefault();
      if (!validateRequiredFields()) {
        return;
      }
      var payload = collectProfilePayload();
      if (!validatePhoneInput($('home-phone'), '자택 전화번호')) {
        return;
      }
      if (!validatePhoneInput($('work-phone'), '직장 전화번호')) {
        return;
      }
      payload.homePhone = (($('home-phone') || {}).value || '').trim();
      payload.workPhone = (($('work-phone') || {}).value || '').trim();
      var btn = $('settings-save-btn');
      if (btn) {
        btn.disabled = true;
      }
      showMessage('저장 중…', false);
      fetch(PROFILE_API, {
        method: 'PUT',
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        body: JSON.stringify(payload)
      })
        .then(parseResponse)
        .then(function (result) {
          if (result.status === 401) {
            window.location.href =
              '/pages/authentication/simple/login?returnUrl=' +
              encodeURIComponent(window.location.pathname + window.location.search);
            return;
          }
          if (!result.ok || !result.data || !result.data.success) {
            throw new Error((result.data && result.data.message) || '저장 실패 (' + result.status + ')');
          }
          fillProfileForm(result.data);
          refreshHeaderDisplayName(result.data.name);
          var successMsg = result.data.message || '프로필이 저장되었습니다.';
          showMessage(successMsg, false);
          showSuccessAlert(successMsg);
        })
        .catch(function (err) {
          var msg = err.message || '저장 실패';
          if (window.Swal) {
            window.Swal.fire({
              icon: 'error',
              title: '저장 실패',
              text: msg,
              confirmButtonText: '확인'
            });
          } else {
            window.alert(msg);
          }
          showMessage(msg, true);
        })
        .finally(function () {
          if (btn) {
            btn.disabled = false;
          }
        });
    });
  }

  function loadProfileSettings() {
    fetch(PROFILE_API, { credentials: 'same-origin', headers: { Accept: 'application/json' } })
      .then(parseResponse)
      .then(function (result) {
        if (!result.ok || !result.data || !result.data.success) {
          return;
        }
        fillProfileForm(result.data);
      })
      .catch(function () {
        /* ignore */
      });
  }

  function loadExisting() {
    fetch(API, { credentials: 'same-origin', headers: { Accept: 'application/json' } })
      .then(parseResponse)
      .then(function (result) {
        if (!result.ok || !result.data || !result.data.success) {
          return;
        }
        if (result.data.profileMaxBytes) {
          PROFILE_MAX_BYTES = result.data.profileMaxBytes;
        }
        if (result.data.coverMaxBytes) {
          COVER_MAX_BYTES = result.data.coverMaxBytes;
        }
        updateHint(PROFILE_MAX_BYTES, COVER_MAX_BYTES);
        if (result.data.profileImageUrl) {
          applyProfileUrl(result.data.profileImageUrl);
        }
        if (result.data.coverImageUrl) {
          applyCoverUrl(result.data.coverImageUrl);
        }
      })
      .catch(function () {
        /* ignore */
      });
  }

  function init() {
    if (!$('user-settings-profile-card') && !$('user-settings-form-card')) {
      return;
    }
    initAddressForms();
    bindPhoneInputs();
    bindUploads();
    bindProfileForm();
    loadExisting();
    loadProfileSettings();
  }

  window.UserSettingsInit = init;
})();
