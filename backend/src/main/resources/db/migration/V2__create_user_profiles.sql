
CREATE TABLE app_user (
    id          BIGSERIAL PRIMARY KEY,
    last_name   VARCHAR(100) NOT NULL,
    first_name  VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    email       VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE direction_user_role (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    direction_id SMALLINT NOT NULL REFERENCES direction(id),
    role         VARCHAR(20) NOT NULL
                 CHECK (role IN ('ORGANIZER', 'EXPERT')),
                 CONSTRAINT uq_direction_user_role
                 UNIQUE (user_id, direction_id, role)
);

-- Связываем авторов заявок с пользователями
ALTER TABLE author
    ADD COLUMN user_id BIGINT NOT NULL
        REFERENCES app_user(id);

-- Убираем личные данные, которые теперь хранятся в app_user
ALTER TABLE author
DROP COLUMN last_name,
    DROP COLUMN first_name,
    DROP COLUMN middle_name,
    DROP COLUMN group_name,
    DROP COLUMN phone,
    DROP COLUMN email,
    DROP COLUMN funding;
