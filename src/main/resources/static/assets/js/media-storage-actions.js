/**
 * NAS 미디어 저장소 — /admin/media-storage
 * API: /api/storage/upload, /api/storage/files
 * @module media-storage-actions
 */

(function () {

  'use strict';



  var C = window.PrintMallCommon;

  var $ = C.$;

  var escapeHtml = C.escapeHtml;

  var fetchJson = C.fetchJson;



  function findFileInput(type) {

    return document.querySelector('input[type="file"][data-media-type="' + type + '"]');

  }



  function showResult(html, isError) {

    var box = $('media-upload-result');

    if (!box) {

      return;

    }

    box.innerHTML = html;

    box.className = 'border rounded p-3 fs--1 ' + (isError ? 'text-danger bg-white' : 'text-600 bg-light');

  }



  function renderFileList(files) {

    var tbody = $('media-file-list-body');

    if (!tbody) {

      return;

    }

    if (!files || !files.length) {

      tbody.innerHTML = '<tr><td colspan="6" class="text-center text-600 py-3">저장된 파일이 없습니다.</td></tr>';

      return;

    }

    tbody.innerHTML = files

      .map(function (row) {

        return (

          '<tr>' +

          '<td>' +

          escapeHtml(row.fileId) +

          '</td>' +

          '<td>' +

          escapeHtml(row.type) +

          '</td>' +

          '<td class="text-truncate" style="max-width:140px" title="' +

          escapeHtml(row.originalName) +

          '">' +

          escapeHtml(row.originalName || row.filename) +

          '</td>' +

          '<td class="text-truncate" style="max-width:160px"><a href="' +

          escapeHtml(row.url) +

          '" target="_blank" rel="noopener">' +

          escapeHtml(row.url) +

          '</a></td>' +

          '<td class="text-truncate" style="max-width:220px" title="' +

          escapeHtml(row.filePath) +

          '"><code class="fs--2">' +

          escapeHtml(row.filePath) +

          '</code></td>' +

          '<td class="text-nowrap">' +

          escapeHtml(row.regDt) +

          '</td>' +

          '</tr>'

        );

      })

      .join('');

  }



  /**
   * NAS 저장 파일 목록을 API에서 조회하여 테이블에 렌더링합니다.
   * @returns {void}
   */
  function loadFiles() {

    var typeSel = $('media-list-type');

    var type = typeSel ? typeSel.value : '';

    var params = new URLSearchParams({ limit: '50' });

    if (type) {

      params.set('type', type);

    }

    fetchJson('/api/storage/files?' + params.toString())

      .then(function (r) {

        if (!r.ok) {

          throw new Error('목록 조회 실패');

        }

        renderFileList(r.data.files || []);

      })

      .catch(function () {

        renderFileList([]);

      });

  }



  /**
   * 선택한 미디어 타입으로 파일을 NAS에 업로드합니다.
   * @param {string} type 미디어 타입 (image, product, video 등)
   * @returns {void}
   */
  function upload(type) {

    var input = findFileInput(type);

    if (!input || !input.files || !input.files.length) {

      showResult('파일을 선택해 주세요.', true);

      return;

    }

    var file = input.files[0];

    var form = new FormData();

    form.append('file', file);

    form.append('type', type);



    showResult('업로드 중...', false);



    fetchJson('/api/storage/upload', { method: 'POST', body: form })

      .then(function (result) {

        if (!result.ok || !result.data.success) {

          throw new Error((result.data && result.data.message) || '업로드 실패');

        }

        var d = result.data;

        var preview = '';

        if (type === 'image' || type === 'product') {

          preview =

            '<div class="mt-2"><img src="' +

            escapeHtml(d.url) +

            '" alt="" class="img-fluid rounded border" style="max-height:160px" /></div>';

        } else if (type === 'video') {

          preview =

            '<div class="mt-2"><video src="' +

            escapeHtml(d.url) +

            '" controls class="w-100 rounded border" style="max-height:200px"></video></div>';

        }

        showResult(

          '<div><strong>' +

            escapeHtml(d.message) +

            '</strong></div>' +

            '<div class="mt-1">fileId: <code>' +

            escapeHtml(d.fileId) +

            '</code></div>' +

            '<div>URL: <a href="' +

            escapeHtml(d.url) +

            '" target="_blank" rel="noopener">' +

            escapeHtml(d.url) +

            '</a></div>' +

            '<div>경로: <code class="fs--2">' +

            escapeHtml(d.filePath) +

            '</code></div>' +

            '<div>파일: ' +

            escapeHtml(d.originalName) +

            ' (' +

            escapeHtml(d.size) +

            ' bytes)</div>' +

            preview,

          false

        );

        input.value = '';

        loadFiles();

      })

      .catch(function (err) {

        showResult(escapeHtml(err.message || '업로드 중 오류'), true);

      });

  }



  /**
   * 미디어 저장소 화면 초기화 — 업로드·목록 갱신 이벤트 바인딩.
   * @returns {void}
   */
  window.MediaStorageInit = function () {

    document.querySelectorAll('.btn-media-upload').forEach(function (btn) {

      btn.addEventListener('click', function () {

        upload(btn.getAttribute('data-media-type'));

      });

    });

    var reloadBtn = $('btn-reload-media-list');

    if (reloadBtn) {

      reloadBtn.addEventListener('click', loadFiles);

    }

    var typeSel = $('media-list-type');

    if (typeSel) {

      typeSel.addEventListener('change', loadFiles);

    }

    loadFiles();

  };

})();


