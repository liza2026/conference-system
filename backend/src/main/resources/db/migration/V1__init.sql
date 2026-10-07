-- Направления конкурса
CREATE TABLE direction (
    id   SMALLINT     PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

INSERT INTO direction (id, name) VALUES
    (1,  'Электронные устройства и системы'),
    (2,  'Прогрессивные технологии в машиностроении, устройства и средства автоматизации'),
    (3,  'Конструкционные материалы и технологии'),
    (4,  'Химия, химические процессы и технологии, проблемы экологии'),
    (5,  'Динамика и надежность машин, механизмов, конструкций'),
    (6,  'Дороги и автомобильный транспорт'),
    (7,  'Программно-информационное обеспечение'),
    (8,  'Экономика и управление'),
    (9,  'Социальные процессы и гуманитарные знания'),
    (10, 'Проблемы пищевой технологии'),
    (11, 'Архитектура и дизайн'),
    (12, 'Проблемы развития урбанизированных территорий'),
    (13, 'Проблемы жилищно-коммунального хозяйства'),
    (14, 'Технологии и материалы строительной индустрии'),
    (15, 'Надёжность строительных конструкций и инженерные изыскания'),
    (16, 'Роботы, мехатроника и робототехнические системы'),
    (17, 'Цифровые технологии в урбанистике, архитектуре и строительстве');

-- Заявка. Название, направление и файл тезисов хранятся один раз на работу.
CREATE TABLE application (
    id               BIGSERIAL    PRIMARY KEY,
    title            VARCHAR(500) NOT NULL,
    direction_id     SMALLINT     NOT NULL REFERENCES direction (id),
    status           VARCHAR(20)  NOT NULL CHECK (status IN ('SUBMITTED', 'WITHDRAWN')),
    thesis_file_name VARCHAR(255) NOT NULL,
    thesis_file      BYTEA        NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL
);

-- Авторы: не более двух на работу
CREATE TABLE author (
    id             BIGSERIAL    PRIMARY KEY,
    application_id BIGINT       NOT NULL REFERENCES application (id) ON DELETE CASCADE,
    position       SMALLINT     NOT NULL CHECK (position IN (1, 2)),
    full_name      VARCHAR(255) NOT NULL,
    group_name     VARCHAR(50)  NOT NULL,
    phone          VARCHAR(30)  NOT NULL,
    email          VARCHAR(255) NOT NULL,
    funding        VARCHAR(20)  NOT NULL CHECK (funding IN ('BUDGET', 'CONTRACT')),
    UNIQUE (application_id, position)
);

-- Научные руководители: не более двух на работу
CREATE TABLE supervisor (
    id             BIGSERIAL    PRIMARY KEY,
    application_id BIGINT       NOT NULL REFERENCES application (id) ON DELETE CASCADE,
    position       SMALLINT     NOT NULL CHECK (position IN (1, 2)),
    full_name      VARCHAR(255) NOT NULL,
    post           VARCHAR(255) NOT NULL,
    UNIQUE (application_id, position)
);