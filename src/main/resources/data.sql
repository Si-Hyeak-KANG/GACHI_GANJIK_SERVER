-- TB_CATEGORY 초기 데이터 (서버 시작 시 자동 삽입)
INSERT IGNORE INTO TB_CATEGORY (category_name, display_order, status, created_dt)
VALUES
    ('결혼',     1, 'ACTIVE', NOW()),
    ('여행',     2, 'ACTIVE', NOW()),
    ('모임',     3, 'ACTIVE', NOW()),
    ('생일',     4, 'ACTIVE', NOW()),
    ('기념일',   5, 'ACTIVE', NOW()),
    ('연인',     6, 'ACTIVE', NOW()),
    ('반려동물', 7, 'ACTIVE', NOW()),
    ('취미',     8, 'ACTIVE', NOW()),
    ('일상',     9, 'ACTIVE', NOW()),
    ('기록',    10, 'ACTIVE', NOW()),
    ('친구',    11, 'ACTIVE', NOW()),
    ('독서',    12, 'ACTIVE', NOW()),
    ('공부',    13, 'ACTIVE', NOW()),
    ('기타',    14, 'ACTIVE', NOW());