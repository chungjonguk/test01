/**
 * 인증 마법사 — 공통 카카오 주소 폼 (extended 레이아웃)
 */
(function () {
  'use strict';

  if (!document.getElementById('wizard-address-postal')) {
    return;
  }
  if (!window.PrintMallKakaoAddressForm) {
    return;
  }

  var addressForm = window.PrintMallKakaoAddressForm.create({
    idPrefix: 'wizard',
    layout: 'extended',
    notifyOnSelect: false
  });

  addressForm.bind();
  addressForm.attachTabShown('a[href="#bootstrap-wizard-tab3"]');
})();
