/**
 * 메일 작성(데모): 버튼·전송·첨부 (이벤트 위임)
 */
(function () {
  var ROOT_SEL = '.email-compose-page';
  var INBOX_PATH = '/app/email/inbox';

  function notify(icon, title, text) {
    if (window.Swal) {
      Swal.fire({
        icon: icon,
        title: title,
        text: text || undefined,
        timer: text ? 2400 : 1800,
        showConfirmButton: !!text
      });
      return;
    }
    window.alert(title + (text ? '\n' + text : ''));
  }

  function escapeHtml(text) {
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  function formatFileSize(bytes) {
    if (!bytes && bytes !== 0) {
      return '';
    }
    if (bytes < 1024) {
      return bytes + 'b';
    }
    if (bytes < 1024 * 1024) {
      return Math.round(bytes / 1024) + 'kb';
    }
    return (bytes / (1024 * 1024)).toFixed(1) + 'MB';
  }

  function getFileIconClass(file) {
    var name = (file && file.name) || '';
    var type = (file && file.type) || '';
    if (type.indexOf('image/') === 0 || /\.(png|jpe?g|gif|webp|svg)$/i.test(name)) {
      return 'far fa-image text-danger';
    }
    if (/\.(zip|rar|7z|tar|gz)$/i.test(name) || type.indexOf('zip') >= 0) {
      return 'far fa-file-archive text-warning';
    }
    if (/\.pdf$/i.test(name) || type === 'application/pdf') {
      return 'far fa-file-pdf text-danger';
    }
    return 'far fa-file-alt text-primary';
  }

  function createAttachmentRow(file) {
    var row = document.createElement('div');
    row.className =
      'border px-2 rounded-3 d-flex flex-between-center align-items-center bg-white dark__bg-1000 my-1 fs--1';
    row.setAttribute('data-attachment-row', '');
    row.setAttribute('data-user-attachment', '');

    var icon = document.createElement('span');
    icon.className = 'fs-1 ' + getFileIconClass(file);

    var label = document.createElement('span');
    label.className = 'ms-2 flex-1 text-truncate';
    label.textContent = file.name + ' (' + formatFileSize(file.size) + ')';

    var remove = document.createElement('a');
    remove.className = 'text-300 p-1 ms-2 flex-shrink-0';
    remove.href = '#!';
    remove.setAttribute('data-detach-attachment', '');
    remove.title = '첨부 제거';
    remove.innerHTML = '<span class="fas fa-times"></span>';

    row.appendChild(icon);
    row.appendChild(label);
    row.appendChild(remove);
    return row;
  }

  function addFilesToList(fileList) {
    var container = document.getElementById('email-attachments');
    if (!container || !fileList || !fileList.length) {
      return;
    }
    Array.prototype.forEach.call(fileList, function (file) {
      container.appendChild(createAttachmentRow(file));
    });
  }

  function inboxUrl() {
    var form = document.getElementById('email-compose-form');
    var raw = form && form.getAttribute('data-inbox-url');
    if (!raw) {
      return INBOX_PATH;
    }
    return raw.split(';')[0];
  }

  function getBodyText() {
    if (window.tinymce) {
      window.tinymce.triggerSave();
      var editor = window.tinymce.get('email-content');
      if (editor) {
        var text = editor.getContent({ format: 'text' }).replace(/\u00a0/g, ' ').trim();
        var html = editor.getContent({ format: 'html' }).trim();
        if (text.length > 0) {
          return text;
        }
        if (html && !/^<p>(\s|&nbsp;|<br\s*\/?>)*<\/p>$/i.test(html)) {
          return html;
        }
        return '';
      }
    }
    var body = document.getElementById('email-content');
    return body ? body.value.trim() : '';
  }

  function sendMail() {
    var to = document.getElementById('email-to');
    var subject = document.getElementById('email-subject');
    var toVal = to && to.value.trim();
    var subjectVal = subject && subject.value.trim();
    var bodyVal = getBodyText();

    if (!toVal) {
      notify('warning', '받는 사람을 입력하세요.');
      if (to) {
        to.focus();
      }
      return;
    }
    if (!subjectVal) {
      notify('warning', '제목을 입력하세요.');
      if (subject) {
        subject.focus();
      }
      return;
    }
    if (!bodyVal) {
      notify('warning', '본문을 입력하세요.');
      return;
    }

    notify('success', '메일을 보냈습니다. (데모)', '실제 발송은 연동되지 않았습니다.');
    setTimeout(function () {
      window.location.href = inboxUrl();
    }, 2000);
  }

  function onClick(e) {
    var root = e.target.closest(ROOT_SEL);
    if (!root) {
      return;
    }

    var actionBtn = e.target.closest('[data-email-compose-action]');
    if (actionBtn) {
      var action = actionBtn.getAttribute('data-email-compose-action');
      if (action === 'send') {
        e.preventDefault();
        sendMail();
        return;
      }
      if (action === 'discard') {
        e.preventDefault();
        if (window.confirm('작성 중인 메일을 삭제할까요?')) {
          window.location.href = inboxUrl();
        }
        return;
      }
      if (action === 'menu') {
        e.preventDefault();
        notify('info', '메뉴 (데모)', actionBtn.textContent.trim() || '선택한 메뉴');
        return;
      }
    }

    var detach = e.target.closest('[data-detach-attachment]');
    if (detach) {
      e.preventDefault();
      var row = detach.closest('[data-attachment-row]');
      if (row) {
        row.remove();
      }
      return;
    }
  }

  function onChange(e) {
    if (!e.target.closest(ROOT_SEL)) {
      return;
    }
    if (
      (e.target.id === 'email-attachment' || e.target.id === 'email-image') &&
      e.target.files &&
      e.target.files.length
    ) {
      addFilesToList(e.target.files);
      e.target.value = '';
    }
  }

  function onSubmit(e) {
    if (e.target.id !== 'email-compose-form') {
      return;
    }
    e.preventDefault();
    sendMail();
  }

  function ensureEditor() {
    if (!window.tinymce || !document.querySelector(ROOT_SEL + ' .email-compose-editor')) {
      return;
    }
    if (window.tinymce.get('email-content')) {
      return;
    }
    window.tinymce.init({
      selector: ROOT_SEL + ' .email-compose-editor',
      height: 320,
      menubar: false,
      statusbar: false,
      plugins: 'link lists',
      toolbar: 'bold italic link bullist numlist undo redo',
      content_style: 'body { font-family: inherit; font-size: 14px; }',
      setup: function (editor) {
        editor.on('change', function () {
          window.tinymce.triggerSave();
        });
      }
    });
  }

  function init() {
    if (!document.querySelector(ROOT_SEL)) {
      return;
    }
    document.body.addEventListener('click', onClick);
    document.body.addEventListener('change', onChange);
    document.body.addEventListener('submit', onSubmit);
    setTimeout(ensureEditor, 300);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
