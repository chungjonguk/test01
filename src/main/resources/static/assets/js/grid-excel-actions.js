/**
 * 그리드 엑셀 공통 — {@code window.PrintMallGridExcel.bind(config)}
 */
(function () {
  'use strict';

  var C = window.PrintMallCommon;

  /**
   * @param {object} data in: API import 응답
   * @param {boolean} ok in: HTTP 성공 여부
   * @returns {string} out: alert용 메시지
   */
  function formatImportMessage(data, ok) {
    var msg = (data && data.message) || (ok ? '업로드 완료' : '업로드 실패');
    if (data && data.errors && data.errors.length) {
      msg += '\n\n' + data.errors.slice(0, 10).join('\n');
      if (data.errors.length > 10) {
        msg += '\n... 외 ' + (data.errors.length - 10) + '건';
      }
    }
    return msg;
  }

  /**
   * 엑셀 다운로드·양식·업로드 버튼 이벤트 연결.
   *
   * @param {object} config in:
   *   exportBtnId, templateBtnId, uploadInputId,
   *   exportUrl, templateUrl, importUrl,
   *   buildExportQuery, confirmMessage, onSuccess(data, result)
   */
  function bind(config) {
    if (!C) {
      console.warn('PrintMallCommon is required for PrintMallGridExcel');
      return;
    }
    config = config || {};

    var exportBtn = C.$(config.exportBtnId);
    if (exportBtn && config.exportUrl) {
      exportBtn.addEventListener('click', function () {
        window.location.href = config.exportUrl + C.toQuerySuffix(config.buildExportQuery);
      });
    }

    var templateBtn = C.$(config.templateBtnId);
    if (templateBtn && config.templateUrl) {
      templateBtn.addEventListener('click', function () {
        window.location.href = config.templateUrl;
      });
    }

    var uploadInput = C.$(config.uploadInputId);
    if (!uploadInput || !config.importUrl) {
      return;
    }

    var fileField = config.fileFieldName || 'file';
    var confirmMsg =
      config.confirmMessage || '선택한 엑셀 파일로 데이터를 일괄 등록·수정하시겠습니까?';
    var invalidExtMsg = config.invalidExtensionMessage || 'xlsx 형식의 엑셀 파일만 업로드할 수 있습니다.';
    var uploadErrorMsg = config.uploadErrorMessage || '엑셀 업로드 중 오류가 발생했습니다.';

    uploadInput.addEventListener('change', function () {
      var file = uploadInput.files && uploadInput.files[0];
      uploadInput.value = '';
      if (!file) {
        return;
      }
      if (!/\.xlsx$/i.test(file.name)) {
        window.alert(invalidExtMsg);
        return;
      }
      if (!window.confirm(confirmMsg)) {
        return;
      }

      var formData = new FormData();
      formData.append(fileField, file);

      C.fetchJson(config.importUrl, { method: 'POST', body: formData })
        .then(function (result) {
          var data = result.data || {};
          window.alert(formatImportMessage(data, result.ok));
          if (result.ok || data.created > 0 || data.updated > 0) {
            if (typeof config.onSuccess === 'function') {
              config.onSuccess(data, result);
            }
          }
        })
        .catch(function () {
          window.alert(uploadErrorMsg);
        });
    });
  }

  window.PrintMallGridExcel = { bind: bind };
})();
