#!/usr/bin/env python3
"""Falcon layout 페이지: 중복 main/settings 제거 + 상대 assets 경로 수정."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
TEMPLATES = ROOT / "src" / "main" / "resources" / "templates"

HEAD_RE = re.compile(
    r"(<!DOCTYPE html>.*?</head>\s*<body>\s*<th:block\s+th:replace=\"~\{layout\s*::\s*body\}\">\s*<div\s+th:fragment=\"page-content\">)",
    re.DOTALL | re.IGNORECASE,
)

MARKERS = [
    re.compile(r"<footer\s+class=\"footer\"", re.I),
    re.compile(r"<div\s+class=\"modal\s+fade\"\s+id=\"authentication-modal\"", re.I),
    re.compile(r"</main>", re.I),
    re.compile(r"<!--\s*End of Main Content\s*-->", re.I),
    re.compile(r"<div\s+class=\"offcanvas[^\"]*\"\s+id=\"settings-offcanvas\"", re.I),
    re.compile(r"<a\s+class=\"card\s+setting-toggle\"", re.I),
]

SUFFIX = """        <div th:replace="~{fragments/footer :: footer}"></div>
    </div>
    </th:block>
</body>
</html>
"""


def fix_asset_paths(text: str) -> str:
    text = re.sub(r"\.\./(?:\.\./)*assets/", "/assets/", text)
    text = re.sub(r"\.\./assets/", "/assets/", text)
    text = re.sub(r"url\((\.\./)+assets/", "url(/assets/", text, flags=re.I)
    return text


def find_cut_index(content: str, start: int) -> int | None:
    sub = content[start:]
    earliest = None
    for pattern in MARKERS:
        match = pattern.search(sub)
        if match:
            pos = match.start()
            if earliest is None or pos < earliest:
                earliest = pos
    if earliest is None:
        return None
    return start + earliest


def needs_fix(content: str) -> bool:
    if "layout :: body" not in content:
        return False
    return "</main>" in content or "settings-offcanvas" in content or "setting-toggle" in content


def process_file(path: Path) -> bool:
    original = path.read_text(encoding="utf-8")
    content = fix_asset_paths(original)
    changed = content != original

    if not needs_fix(content):
        if changed:
            path.write_text(content, encoding="utf-8")
            return True
        return False

    match = HEAD_RE.search(content)
    if not match:
        if changed:
            path.write_text(content, encoding="utf-8")
            return True
        return False

    head = match.group(1)
    start = match.end()
    cut = find_cut_index(content, start)
    if cut is None:
        if changed:
            path.write_text(content, encoding="utf-8")
            return True
        return False

    body = content[start:cut].rstrip()
    body = re.sub(
        r"<footer\s+class=\"footer\"[\s\S]*?</footer>\s*$",
        "",
        body,
        flags=re.I,
    ).rstrip()
    body = fix_asset_paths(body)

    if "fragments/footer" not in body:
        pass

    new_content = head + "\n" + body + "\n" + SUFFIX
    path.write_text(new_content, encoding="utf-8")
    return True


def main() -> None:
    fixed = 0
    for pattern in ("app/**/*.html", "pages/**/*.html", "modules/**/*.html"):
        for file_path in sorted(TEMPLATES.glob(pattern)):
            if process_file(file_path):
                fixed += 1
                print(file_path.relative_to(ROOT))
    print(f"\nDone. Updated {fixed} file(s).")


if __name__ == "__main__":
    main()
