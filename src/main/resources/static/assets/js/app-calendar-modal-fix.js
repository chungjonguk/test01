(function () {
  function normalizeEventPath(href) {
    if (!href) {
      return null;
    }
    var match = href.match(/\/app\/events\/[a-z0-9-]+/i);
    if (match) {
      return match[0].replace(/\.html$/i, '');
    }
    return null;
  }

  document.addEventListener('click', function (e) {
    var link = e.target.closest('#eventDetailsModal a[href]');
    if (!link) {
      return;
    }
    var target = normalizeEventPath(link.getAttribute('href'));
    if (!target) {
      return;
    }
    e.preventDefault();
    window.location.assign(target);
  });
})();
