/**
 * 사용자 관리 — 비밀번호 초기화 확인
 */
(function () {
    'use strict';

    document.querySelectorAll('.user-reset-password-form').forEach(function (form) {
        form.addEventListener('submit', function (e) {
            var button = form.querySelector('button[type="submit"]');
            var userId = button && button.getAttribute('data-user-id') ? button.getAttribute('data-user-id') : '';

            if (!userId) {
                return;
            }

            e.preventDefault();

            var message = '비밀번호를 아이디(' + userId + ')와 동일하게 초기화합니다.\n계속하시겠습니까?';

            if (typeof Swal === 'undefined') {
                if (window.confirm(message)) {
                    form.submit();
                }
                return;
            }

            Swal.fire({
                icon: 'warning',
                title: '비밀번호 초기화',
                text: '초기 비밀번호는 아이디(' + userId + ')와 동일하게 설정됩니다.',
                showCancelButton: true,
                confirmButtonText: '초기화',
                cancelButtonText: '취소',
                confirmButtonColor: '#f5803e'
            }).then(function (result) {
                if (result.isConfirmed) {
                    form.submit();
                }
            });
        });
    });
})();
