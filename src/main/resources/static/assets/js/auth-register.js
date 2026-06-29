/**
 * 인증 레이아웃 회원가입(simple/card/split) — 이메일 중복 확인·비밀번호 정책
 */
(function () {
    'use strict';

    var validation = window.UserFormValidation;
    if (!validation) {
        return;
    }

    document.querySelectorAll('[data-auth-register]').forEach(function (form) {
        var emailInput = form.querySelector('[data-auth-email]');
        var emailCheckBtn = form.querySelector('[data-auth-check-email]');
        var emailMessageEl = form.querySelector('[data-auth-email-message]');
        var pwInput = form.querySelector('[data-auth-password]');
        var confirmInput = form.querySelector('[data-auth-confirm-password]');
        var lockFields = form.querySelector('[data-auth-register-fields]');
        var redirectUrl = form.getAttribute('data-redirect-url') || '/users';

        if (!emailInput || !emailCheckBtn || !emailMessageEl || !pwInput) {
            return;
        }

        function setFieldsEnabled(enabled) {
            if (lockFields) {
                lockFields.disabled = !enabled;
            }
        }

        setFieldsEnabled(false);

        var emailCheck = validation.bindEmailDuplicateCheck({
            emailInput: emailInput,
            emailCheckBtn: emailCheckBtn,
            emailMessageEl: emailMessageEl,
            checkEmailUrl: form.getAttribute('data-check-email-url') || '/users/check-email',
            successSuffix: ' 나머지 항목을 입력해 주세요.',
            onAvailabilityChange: setFieldsEnabled
        });

        pwInput.addEventListener('input', function () {
            pwInput.classList.remove('is-invalid');
        });
        if (confirmInput) {
            confirmInput.addEventListener('input', function () {
                confirmInput.classList.remove('is-invalid');
            });
        }

        form.addEventListener('submit', function (e) {
            e.preventDefault();
            if (!emailCheck.validateOnSubmit()) {
                return;
            }
            if (!validation.validatePasswordField(pwInput, confirmInput)) {
                return;
            }
            var email = encodeURIComponent(emailInput.value.trim());
            window.location.href = redirectUrl + (redirectUrl.indexOf('?') >= 0 ? '&' : '?') + 'email=' + email;
        });
    });
})();
