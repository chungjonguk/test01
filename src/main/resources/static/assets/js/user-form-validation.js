/**
 * 사용자 등록 폼 — 이메일 중복 확인·비밀번호 정책 공통 유틸
 * @module user-form-validation
 */
(function (global) {
    'use strict';

    var HAS_LETTER = /[A-Za-z]/;
    var HAS_DIGIT = /\d/;
    var HAS_SPECIAL = /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?`~]/;
    var EMAIL_PATTERN = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

    var swalDefaults = {
        confirmButtonText: '확인',
        confirmButtonColor: '#2c7be5'
    };

    function showAlert(icon, title, text) {
        if (typeof Swal === 'undefined') {
            window.alert(text);
            return;
        }
        return Swal.fire(Object.assign({}, swalDefaults, { icon: icon, title: title, text: text }));
    }

    function isValidPassword(password) {
        return HAS_LETTER.test(password) && HAS_DIGIT.test(password) && HAS_SPECIAL.test(password);
    }

    function passwordRequirementMessage() {
        return '비밀번호는 영문자, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.';
    }

    function bindEmailDuplicateCheck(options) {
        var emailInput = options.emailInput;
        var emailCheckBtn = options.emailCheckBtn;
        var emailMessageEl = options.emailMessageEl;
        var checkEmailUrl = options.checkEmailUrl || '/users/check-email';
        var onAvailabilityChange = options.onAvailabilityChange || function () {};

        var emailState = { checked: false, available: false, lastValue: '' };

        function showEmailMessage(available, message, isMuted) {
            emailMessageEl.textContent = message;
            if (isMuted) {
                emailMessageEl.className = 'form-text text-muted';
            } else {
                emailMessageEl.className = available ? 'form-text text-success' : 'form-text text-danger';
            }
            emailInput.classList.toggle('is-valid', available && !isMuted);
            emailInput.classList.toggle('is-invalid', !available && !isMuted);
        }

        function resetEmailCheck() {
            emailState.checked = false;
            emailState.available = false;
            emailState.lastValue = '';
            showEmailMessage(false, options.initialMessage || '이메일 중복 확인 후 나머지 항목을 입력할 수 있습니다.', true);
            onAvailabilityChange(false);
        }

        showEmailMessage(false, options.initialMessage || '이메일 중복 확인 후 나머지 항목을 입력할 수 있습니다.', true);

        emailInput.addEventListener('input', function () {
            if (emailInput.value.trim() !== emailState.lastValue) {
                resetEmailCheck();
            }
        });

        emailCheckBtn.addEventListener('click', async function () {
            var email = emailInput.value.trim();
            if (!email) {
                showEmailMessage(false, '이메일을 입력해 주세요.', false);
                showAlert('warning', '이메일 입력', '이메일을 입력해 주세요.');
                return;
            }
            if (!EMAIL_PATTERN.test(email)) {
                showEmailMessage(false, '올바른 이메일 형식이 아닙니다.', false);
                showAlert('warning', '이메일 형식', '올바른 이메일 형식이 아닙니다.');
                return;
            }

            emailCheckBtn.disabled = true;
            emailCheckBtn.textContent = '확인 중...';
            try {
                var response = await fetch(checkEmailUrl + '?email=' + encodeURIComponent(email));
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                var data = await response.json();
                emailState.checked = true;
                emailState.available = data.available;
                emailState.lastValue = email;
                showEmailMessage(data.available, data.message, false);
                onAvailabilityChange(data.available);

                if (data.available && typeof Swal !== 'undefined') {
                    Swal.fire(Object.assign({}, swalDefaults, {
                        icon: 'success',
                        title: '사용 가능',
                        text: data.message + (options.successSuffix || ''),
                        timer: 2500,
                        showConfirmButton: false
                    }));
                } else if (!data.available) {
                    showAlert('error', '사용 불가', data.message);
                }
            } catch (e) {
                resetEmailCheck();
                showEmailMessage(false, '중복 확인 중 오류가 발생했습니다.', false);
                showAlert('error', '오류', '중복 확인 중 오류가 발생했습니다.');
            } finally {
                emailCheckBtn.disabled = false;
                emailCheckBtn.textContent = '중복 확인';
            }
        });

        return {
            validateOnSubmit: function () {
                var email = emailInput.value.trim();
                if (!emailState.checked || emailState.lastValue !== email || !emailState.available) {
                    showAlert('warning', '이메일 중복 확인', '등록 전에 이메일 중복 확인을 완료해 주세요.');
                    emailInput.focus();
                    return false;
                }
                return true;
            },
            reset: resetEmailCheck
        };
    }

    function bindDuplicateCheck(options) {
        var state = { checked: false, available: false, lastValue: '' };

        function resetCheck() {
            state.checked = false;
            state.available = false;
            state.lastValue = '';
            options.messageEl.textContent = '';
            options.messageEl.className = 'form-text';
            options.input.classList.remove('is-valid', 'is-invalid');
        }

        function showMessage(available, message) {
            options.messageEl.textContent = message;
            options.messageEl.className = available ? 'form-text text-success' : 'form-text text-danger';
            options.input.classList.toggle('is-valid', available);
            options.input.classList.toggle('is-invalid', !available);
        }

        options.input.addEventListener('input', function () {
            if (options.input.value.trim() !== state.lastValue) {
                resetCheck();
            }
        });

        options.button.addEventListener('click', async function () {
            var value = options.input.value.trim();
            if (!value) {
                showMessage(false, options.emptyMessage);
                showAlert('warning', options.emptyTitle, options.emptyMessage);
                return;
            }

            options.button.disabled = true;
            options.button.textContent = '확인 중...';
            try {
                var response = await fetch(
                    options.checkUrl + '?' + options.paramName + '=' + encodeURIComponent(value)
                );
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                var data = await response.json();
                state.checked = true;
                state.available = data.available;
                state.lastValue = value;
                showMessage(data.available, data.message);
                if (data.available && typeof Swal !== 'undefined') {
                    Swal.fire(Object.assign({}, swalDefaults, {
                        icon: 'success',
                        title: '사용 가능',
                        text: data.message,
                        timer: 2000,
                        showConfirmButton: false
                    }));
                } else if (!data.available) {
                    showAlert('error', '사용 불가', data.message);
                }
            } catch (e) {
                showMessage(false, '중복 확인 중 오류가 발생했습니다.');
                showAlert('error', '오류', '중복 확인 중 오류가 발생했습니다.');
                resetCheck();
            } finally {
                options.button.disabled = false;
                options.button.textContent = '중복 확인';
            }
        });

        return {
            validateOnSubmit: function (value) {
                if (!state.checked || state.lastValue !== value.trim()) {
                    showAlert('warning', options.submitTitle, options.submitMessage);
                    return false;
                }
                if (!state.available) {
                    showAlert('error', options.unavailableTitle, options.unavailableMessage);
                    return false;
                }
                return true;
            }
        };
    }

    function validatePasswordField(pwInput, confirmInput) {
        var pw = pwInput.value;
        if (!isValidPassword(pw)) {
            pwInput.classList.add('is-invalid');
            pwInput.focus();
            showAlert('warning', '비밀번호 형식', passwordRequirementMessage());
            return false;
        }
        if (confirmInput && pw !== confirmInput.value) {
            confirmInput.classList.add('is-invalid');
            confirmInput.focus();
            showAlert('warning', '비밀번호 확인', '비밀번호와 확인 비밀번호가 일치하지 않습니다.');
            return false;
        }
        pwInput.classList.remove('is-invalid');
        if (confirmInput) {
            confirmInput.classList.remove('is-invalid');
        }
        return true;
    }

    global.UserFormValidation = {
        isValidPassword: isValidPassword,
        passwordRequirementMessage: passwordRequirementMessage,
        showAlert: showAlert,
        bindEmailDuplicateCheck: bindEmailDuplicateCheck,
        bindDuplicateCheck: bindDuplicateCheck,
        validatePasswordField: validatePasswordField
    };
})(window);
