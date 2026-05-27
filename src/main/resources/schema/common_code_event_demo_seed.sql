-- app/events, app/e-learning 데모 콤보박스

INSERT INTO common_code (code_id, code_nm, use_yn, reg_id, update_id) VALUES
('EVENT_TOPIC', '이벤트-주제', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_ORGANIZER', '이벤트-주최', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_SPONSOR', '이벤트-후원', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TAG', '이벤트-태그', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE code_nm = VALUES(code_nm), use_yn = VALUES(use_yn), update_id = VALUES(update_id);

INSERT INTO common_code_value (code_id, code_val, use_yn, reg_id, update_id) VALUES
('EVENT_TOPIC', '|Select a topic', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TOPIC', 'AUTO|Auto, Boat & Air', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TOPIC', 'BUSINESS|Business & Professional', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TOPIC', 'CHARITY|Charity & Causes', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TOPIC', 'COMMUNITY|Community & Culture', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TOPIC', 'FAMILY|Family & Education', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TOPIC', 'FASHION|Fashion & Beauty', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TOPIC', 'FILM|Film, Media & Entertainment', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TOPIC', 'FOOD|Food & Drink', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TOPIC', 'GOVERNMENT|Government & Politics', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_ORGANIZER', '|Select organizer...', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_ORGANIZER', 'MIT|Massachusetts Institute of Technology', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_ORGANIZER', 'UCHICAGO|University of Chicago', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_ORGANIZER', 'HARVARD|GSAS Open Labs At Harvard', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_ORGANIZER', 'CALTECH|California Institute of Technology', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_SPONSOR', '|Select sponsors...', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_SPONSOR', 'MSFT|Microsoft Corporation', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_SPONSOR', 'TECHNEXT|Technext Limited', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_SPONSOR', 'HP|Hewlett-Packard', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TAG', '|Select tags...', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TAG', 'CONCERT|Concert', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TAG', 'NEWYEAR|New Year', 'Y', 'SYSTEM', 'SYSTEM'),
('EVENT_TAG', 'PARTY|Party', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE use_yn = VALUES(use_yn), update_id = VALUES(update_id);
