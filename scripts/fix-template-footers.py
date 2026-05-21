#!/usr/bin/env python3
"""Remove duplicate footer/settings/scripts from layout-based templates."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "src" / "main" / "resources" / "templates"
FOOTER_INLINE = '<footer class="footer">'
FRAGMENT = '        <div th:replace="~{fragments/footer :: footer}"></div>'
SUFFIX = FRAGMENT + "\n    </div>\n    </th:block>\n</body>\n</html>\n"
CALENDAR_MARKER = 'id="eventDetailsModal"'
END_MARKERS = [
    "    </main>",
    '    <div class="offcanvas offcanvas-end settings-panel',
    "    <!-- ===============================================-->",
]


def extract_calendar_modals(text: str, start_pos: int) -> str:
    idx = text.find(CALENDAR_MARKER, start_pos)
    if idx == -1:
        return ""
    mod_start = text.rfind('<div class="modal', start_pos, idx)
    if mod_start == -1:
        return ""
    end_pos = len(text)
    for marker in END_MARKERS:
        pos = text.find(marker, mod_start)
        if pos != -1 and pos < end_pos:
            end_pos = pos
    return text[mod_start:end_pos].rstrip()


def main() -> None:
    fixed: list[str] = []
    for path in sorted(ROOT.rglob("*.html")):
        text = path.read_text(encoding="utf-8")
        if FOOTER_INLINE not in text or "layout :: body" not in text:
            continue
        start = text.find(FOOTER_INLINE)
        preserved = extract_calendar_modals(text, start)
        tail = (preserved + "\n\n") if preserved else ""
        tail += SUFFIX
        new_text = text[:start] + tail
        if new_text != text:
            path.write_text(new_text, encoding="utf-8", newline="\n")
            fixed.append(str(path.relative_to(ROOT)).replace("\\", "/"))

    print(f"Fixed {len(fixed)} files:")
    for name in fixed:
        print(f" - {name}")


if __name__ == "__main__":
    main()
