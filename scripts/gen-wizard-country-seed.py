#!/usr/bin/env python3
"""Extract country options from wizard.html and emit common_code_wizard_seed.sql."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
WIZARD = ROOT / "src/main/resources/templates/modules/forms/wizard.html"
OUT = ROOT / "src/main/resources/schema/common_code_wizard_seed.sql"

text = WIZARD.read_text(encoding="utf-8")
m = re.search(
    r'id="bootstrap-wizard-card-holder-country">(.*?)</select>', text, re.S
)
if not m:
    raise SystemExit("country select block not found")
countries = [
    (v, lbl.strip())
    for v, lbl in re.findall(
        r'<option value="([^"]*)">([^<]*)</option>', m.group(1)
    )
    if v
]

lines = [
    "-- wizard / authentication wizard form-select 공통코드",
    "",
    "INSERT INTO common_code (code_id, code_nm, use_yn, reg_id, update_id) VALUES",
    "('WIZARD_GENDER', '위저드-성별', 'Y', 'SYSTEM', 'SYSTEM'),",
    "('COUNTRY_LIST', '국가목록', 'Y', 'SYSTEM', 'SYSTEM'),",
    "('BIRTH_MONTH', '생년월일-월', 'Y', 'SYSTEM', 'SYSTEM')",
    "ON DUPLICATE KEY UPDATE code_nm = VALUES(code_nm), use_yn = VALUES(use_yn), update_id = VALUES(update_id);",
    "",
    "INSERT INTO common_code_value (code_id, code_val, use_yn, reg_id, update_id) VALUES",
]

vals = [
    "('WIZARD_GENDER', '|Select your gender ...', 'Y', 'SYSTEM', 'SYSTEM')",
    "('WIZARD_GENDER', 'Male|Male', 'Y', 'SYSTEM', 'SYSTEM')",
    "('WIZARD_GENDER', 'Female|Female', 'Y', 'SYSTEM', 'SYSTEM')",
    "('WIZARD_GENDER', 'Other|Other', 'Y', 'SYSTEM', 'SYSTEM')",
    "('BIRTH_MONTH', '|월', 'Y', 'SYSTEM', 'SYSTEM')",
]
for i in range(1, 13):
    v = f"{i:02d}"
    vals.append(f"('BIRTH_MONTH', '{v}|{i}월', 'Y', 'SYSTEM', 'SYSTEM')")
vals.append("('COUNTRY_LIST', '|Select your country ...', 'Y', 'SYSTEM', 'SYSTEM')")
for v, _ in countries:
    esc = v.replace("'", "''")
    vals.append(f"('COUNTRY_LIST', '{esc}|{esc}', 'Y', 'SYSTEM', 'SYSTEM')")

lines.append(",\n".join(vals))
lines.append("ON DUPLICATE KEY UPDATE use_yn = VALUES(use_yn), update_id = VALUES(update_id);")
OUT.write_text("\n".join(lines) + "\n", encoding="utf-8")
print(f"wrote {OUT} ({len(countries)} countries)")
