(function () {
    const form = document.getElementById('userRegisterForm');
    if (!form) {
        return;
    }

    const registrationFields = document.getElementById('registrationFields');
    const emailInput = document.getElementById('email');
    const emailCheckBtn = document.getElementById('btnCheckEmail');
    const emailMessageEl = document.getElementById('emailCheckMessage');
    const flashEl = document.getElementById('flashMessages');
    const pwInput = document.getElementById('pw');
    const hasLetter = /[A-Za-z]/;
    const hasDigit = /\d/;
    const hasSpecial = /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?`~]/;
    const emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;

    const swalDefaults = {
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
        return hasLetter.test(password) && hasDigit.test(password) && hasSpecial.test(password);
    }

    function setRegistrationEnabled(enabled) {
        if (registrationFields) {
            registrationFields.disabled = !enabled;
        }
    }

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

    if (flashEl) {
        const successMessage = flashEl.dataset.success;
        const errorMessage = flashEl.dataset.error;
        if (successMessage) {
            showAlert('success', '등록 완료', successMessage);
        }
        if (errorMessage) {
            showAlert('error', '등록 실패', errorMessage);
        }
    }

    setRegistrationEnabled(false);
    showEmailMessage(false, '이메일 중복 확인 후 나머지 항목을 입력할 수 있습니다.', true);

    const emailState = { checked: false, available: false, lastValue: '' };

    function resetEmailCheck() {
        emailState.checked = false;
        emailState.available = false;
        emailState.lastValue = '';
        setRegistrationEnabled(false);
        showEmailMessage(false, '이메일 중복 확인 후 나머지 항목을 입력할 수 있습니다.', true);
    }

    emailInput.addEventListener('input', function () {
        if (emailInput.value.trim() !== emailState.lastValue) {
            resetEmailCheck();
        }
    });

    emailCheckBtn.addEventListener('click', async function () {
        const email = emailInput.value.trim();
        if (!email) {
            showEmailMessage(false, '이메일을 입력해 주세요.', false);
            showAlert('warning', '이메일 입력', '이메일을 입력해 주세요.');
            return;
        }
        if (!emailPattern.test(email)) {
            showEmailMessage(false, '올바른 이메일 형식이 아닙니다.', false);
            showAlert('warning', '이메일 형식', '올바른 이메일 형식이 아닙니다.');
            return;
        }

        const checkEmailUrl = form.getAttribute('data-check-email-url') || '/users/check-email';
        emailCheckBtn.disabled = true;
        emailCheckBtn.textContent = '확인 중...';
        try {
            const response = await fetch(checkEmailUrl + '?email=' + encodeURIComponent(email));
            if (!response.ok) {
                throw new Error('HTTP ' + response.status);
            }
            const data = await response.json();
            emailState.checked = true;
            emailState.available = data.available;
            emailState.lastValue = email;
            showEmailMessage(data.available, data.message, false);

            if (data.available) {
                setRegistrationEnabled(true);
                if (typeof Swal !== 'undefined') {
                    Swal.fire(Object.assign({}, swalDefaults, {
                        icon: 'success',
                        title: '사용 가능',
                        text: data.message + ' 나머지 항목을 입력해 주세요.',
                        timer: 2500,
                        showConfirmButton: false
                    }));
                }
            } else {
                setRegistrationEnabled(false);
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

    function setupDuplicateCheck(options) {
        const state = { checked: false, available: false, lastValue: '' };

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
            const value = options.input.value.trim();
            if (!value) {
                showMessage(false, options.emptyMessage);
                showAlert('warning', options.emptyTitle, options.emptyMessage);
                return;
            }

            options.button.disabled = true;
            options.button.textContent = '확인 중...';
            try {
                const response = await fetch(
                    options.checkUrl + '?' + options.paramName + '=' + encodeURIComponent(value)
                );
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                const data = await response.json();
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
            validateOnSubmit(value) {
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

    const idCheck = setupDuplicateCheck({
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
        const email = emailInput.value.trim();
        const id = document.getElementById('id').value;
        const pw = pwInput.value;

        if (!emailState.checked || emailState.lastValue !== email || !emailState.available) {
            e.preventDefault();
            showAlert('warning', '이메일 중복 확인', '등록 전에 이메일 중복 확인을 완료해 주세요.');
            emailInput.focus();
            return;
        }
        if (!idCheck.validateOnSubmit(id)) {
            e.preventDefault();
            return;
        }
        if (!isValidPassword(pw)) {
            e.preventDefault();
            pwInput.classList.add('is-invalid');
            pwInput.focus();
            showAlert('warning', '비밀번호 형식', '비밀번호는 영문자, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다.');
            return;
        }
        const zipcode = document.getElementById('zipcode').value.trim();
        const address = document.getElementById('address').value.trim();
        const addressDetail = document.getElementById('addressDetail').value.trim();
        if (!zipcode || !address) {
            e.preventDefault();
            showAlert('warning', '주소 검색', '주소 검색 버튼으로 기본 주소를 입력해 주세요.');
            return;
        }
        if (!addressDetail) {
            e.preventDefault();
            document.getElementById('addressDetail').focus();
            showAlert('warning', '상세주소', '상세주소를 입력해 주세요.');
            return;
        }
        pwInput.classList.remove('is-invalid');
    });

    pwInput.addEventListener('input', function () {
        pwInput.classList.remove('is-invalid');
    });
})();
