/**
 * 인보이스 화면: Download (.pdf) — html2pdf.js + 전역 AppLoading
 */
(function () {
  function getOrderId() {
    var area = document.getElementById('invoice-pdf-area');
    if (!area) {
      return 'invoice';
    }
    var id = area.getAttribute('data-order-id');
    if (id) {
      return id;
    }
    var heading = area.querySelector('h5');
    if (heading && heading.textContent) {
      var match = heading.textContent.match(/#?([A-Z0-9-]+)/i);
      if (match) {
        return match[1];
      }
    }
    return 'invoice';
  }

  function downloadInvoicePdf() {
    if (typeof html2pdf === 'undefined') {
      if (window.AppLoading) {
        AppLoading.alert('error', 'PDF 생성 실패', 'PDF 라이브러리를 불러오지 못했습니다. 페이지를 새로고침한 뒤 다시 시도해 주세요.');
      } else {
        window.alert('PDF 라이브러리를 불러오지 못했습니다.');
      }
      return;
    }

    var source = document.getElementById('invoice-pdf-area');
    if (!source) {
      return;
    }

    var btn = document.getElementById('btn-invoice-download-pdf');
    if (btn && btn.disabled) {
      return;
    }

    var filename = 'invoice-' + getOrderId() + '.pdf';
    var opt = {
      margin: [10, 10, 10, 10],
      filename: filename,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: {
        scale: 2,
        useCORS: true,
        logging: false,
        ignoreElements: function (el) {
          return el && el.classList && el.classList.contains('invoice-pdf-ignore');
        }
      },
      jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
    };

    var loadingOpts = {
      modal: true,
      message: '인보이스를 PDF로 변환하고 있습니다.<br/>잠시만 기다려 주세요.',
      button: btn,
      buttonHtml:
        '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>PDF 생성 중...'
    };

    var task = html2pdf().set(opt).from(source).save();

    if (window.AppLoading) {
      AppLoading.show(loadingOpts);
      task
        .then(function () {
          AppLoading.hide();
          AppLoading.alert('success', '다운로드 완료', filename + ' 파일이 저장되었습니다.');
        })
        .catch(function () {
          AppLoading.hide(true);
          AppLoading.alert('error', 'PDF 생성 실패', 'PDF 생성에 실패했습니다. 잠시 후 다시 시도해 주세요.');
        });
      return;
    }

    task.catch(function () {
      window.alert('PDF 생성에 실패했습니다.');
    });
  }

  function init() {
    var btn = document.getElementById('btn-invoice-download-pdf');
    if (!btn) {
      return;
    }
    btn.addEventListener('click', downloadInvoicePdf);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
