(function () {
    var validation = window.UserFormValidation;
    var form = document.getElementById('userRegisterForm');
    if (!form || !validation) {
        return;
    }

    var registrationFields = document.getElementById('registrationFields');
    var emailInput = document.getElementById('email');
    var emailCheckBtn = document.getElementById('btnCheckEmail');
    var emailMessageEl = document.getElementById('emailCheckMessage');
    var flashEl = document.getElementById('flashMessages');
    var pwInput = document.getElementById('pw');

    function setRegistrationEnabled(enabled) {
        if (registrationFields) {
            registrationFields.disabled = !enabled;
        }
    }

    if (flashEl) {
        var successMessage = flashEl.dataset.success;
        var errorMessage = flashEl.dataset.error;
        if (successMessage) {
            validation.showAlert('success', '등록 완료', successMessage);
        }
        if (errorMessage) {
            validation.showAlert('error', '등록 실패', errorMessage);
        }
    }

    setRegistrationEnabled(false);

    var emailCheck = validation.bindEmailDuplicateCheck({
        emailInput: emailInput,
        emailCheckBtn: emailCheckBtn,
        emailMessageEl: emailMessageEl,
        checkEmailUrl: form.getAttribute('data-check-email-url') || '/users/check-email',
        successSuffix: ' 나머지 항목을 입력해 주세요.',
        onAvailabilityChange: setRegistrationEnabled
    });

    var idCheck = validation.bindDuplicateCheck({
        input: document.getElementById('id'),
        button: document.getElementById('btnCheckId'),
        messageEl: document.getElementById('idCheckMessage'),
        checkUrl: form.getAttribute('data-check-id-url') || '/users/check-id',
        paramName: 'id',
        emptyTitle: '아이디 입력',
        emptyMessage: '아이디를 입력해 주세요.',
        submitTitle: '아이디 중복 확인',
        submitMessage: '등록 전에 아이디 중복 확인을 해 주세요.',
        unavailableTitle: '아이디 사용 불가',
        unavailableMessage: '사용할 수 없는 아이디입니다. 다른 아이디를 입력해 주세요.'
    });

    form.addEventListener('submit', function (e) {
        if (!emailCheck.validateOnSubmit()) {
            e.preventDefault();
            return;
        }
        if (!idCheck.validateOnSubmit(document.getElementById('id').value)) {
            e.preventDefault();
            return;
        }
        if (!validation.validatePasswordField(pwInput)) {
            e.preventDefault();
            return;
        }
        var zipcode = document.getElementById('zipcode').value.trim();
        var address = document.getElementById('address').value.trim();
        var addressDetail = document.getElementById('addressDetail').value.trim();
        if (!zipcode || !address) {
            e.preventDefault();
            validation.showAlert('warning', '주소 검색', '주소 검색 버튼으로 기본 주소를 입력해 주세요.');
            return;
        }
        if (!addressDetail) {
            e.preventDefault();
            document.getElementById('addressDetail').focus();
            validation.showAlert('warning', '상세주소', '상세주소를 입력해 주세요.');
        }
    });

    pwInput.addEventListener('input', function () {
        pwInput.classList.remove('is-invalid');
    });
})();
