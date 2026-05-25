/**
 * 사용자 등록 — 공통 카카오 주소 폼 (split: zipcode / address / addressDetail)
 */
(function () {
  'use strict';

  if (!document.getElementById('zipcode')) {
    return;
  }
  if (!window.PrintMallKakaoAddressForm) {
    return;
  }

  var addressForm = window.PrintMallKakaoAddressForm.create({
    idPrefix: 'user',
    layout: 'split',
    zipId: 'zipcode',
    baseId: 'address',
    detailId: 'addressDetail'
  });

  addressForm.bind();
  addressForm.onContainerShown();
})();
