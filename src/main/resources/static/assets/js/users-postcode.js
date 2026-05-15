document.addEventListener('DOMContentLoaded', function () {
    const searchBtn = document.getElementById('btnSearchAddress');
    const zipcodeInput = document.getElementById('zipcode');
    const addressInput = document.getElementById('address');
    const addressDetailInput = document.getElementById('addressDetail');
    const registrationFields = document.getElementById('registrationFields');
    const postcodeModalEl = document.getElementById('postcodeModal');
    const postcodeEmbedEl = document.getElementById('postcodeEmbed');

    if (!searchBtn || !zipcodeInput || !addressInput || !addressDetailInput || !postcodeModalEl || !postcodeEmbedEl) {
        return;
    }

    const DAUM_POSTCODE_URL = 'https://t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';

    function showMessage(icon, title, text) {
        if (typeof Swal !== 'undefined') {
            Swal.fire({ icon: icon, title: title, text: text, confirmButtonText: '확인' });
        } else {
            window.alert(text);
        }
    }

    function isRegistrationEnabled() {
        return !registrationFields || !registrationFields.disabled;
    }

    function loadDaumPostcode(callback) {
        if (window.daum && window.daum.Postcode) {
            callback();
            return;
        }
        const existing = document.getElementById('daum-postcode-script');
        if (existing) {
            existing.addEventListener('load', function onLoad() {
                existing.removeEventListener('load', onLoad);
                callback();
            });
            existing.addEventListener('error', function onError() {
                existing.removeEventListener('error', onError);
                showMessage('error', '로드 실패', '카카오 우편번호 서비스를 불러오지 못했습니다. 네트워크 연결을 확인해 주세요.');
            });
            return;
        }
        const script = document.createElement('script');
        script.id = 'daum-postcode-script';
        script.src = DAUM_POSTCODE_URL;
        script.async = true;
        script.onload = callback;
        script.onerror = function () {
            showMessage('error', '로드 실패', '카카오 우편번호 서비스를 불러오지 못했습니다. 네트워크 연결을 확인해 주세요.');
        };
        document.body.appendChild(script);
    }

    function applyAddress(data) {
        let fullAddress = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
        if (data.buildingName) {
            fullAddress += (fullAddress ? ', ' : '') + data.buildingName;
        }
        zipcodeInput.value = data.zonecode;
        addressInput.value = fullAddress;
        zipcodeInput.dispatchEvent(new Event('input', { bubbles: true }));
        addressInput.dispatchEvent(new Event('input', { bubbles: true }));
    }

    function openPostcodeSearch() {
        loadDaumPostcode(function () {
            if (!window.daum || !window.daum.Postcode) {
                showMessage('error', '오류', '우편번호 서비스를 초기화하지 못했습니다.');
                return;
            }

            postcodeEmbedEl.innerHTML = '';

            const modal = typeof bootstrap !== 'undefined'
                ? bootstrap.Modal.getOrCreateInstance(postcodeModalEl)
                : null;

            if (modal) {
                modal.show();
            } else {
                postcodeModalEl.classList.add('show');
                postcodeModalEl.style.display = 'block';
                postcodeModalEl.removeAttribute('aria-hidden');
            }

            new daum.Postcode({
                oncomplete: function (data) {
                    applyAddress(data);
                    if (modal) {
                        modal.hide();
                    } else {
                        postcodeModalEl.classList.remove('show');
                        postcodeModalEl.style.display = 'none';
                    }
                    addressDetailInput.focus();
                },
                onclose: function () {
                    if (modal) {
                        modal.hide();
                    }
                },
                width: '100%',
                height: '100%'
            }).embed(postcodeEmbedEl);
        });
    }

    searchBtn.addEventListener('click', function (e) {
        e.preventDefault();
        e.stopPropagation();

        if (!isRegistrationEnabled()) {
            showMessage('warning', '이메일 확인 필요', '이메일 중복 확인을 완료한 후 주소 검색을 이용할 수 있습니다.');
            return;
        }

        openPostcodeSearch();
    });
});
