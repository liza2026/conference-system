-- Добавляем поле подающего заявку.
-- Пока nullable, чтобы сначала заполнить его для существующих заявок.
ALTER TABLE application
    ADD COLUMN submitter_id BIGINT;

-- Для старых заявок подающим назначаем первого автора.
UPDATE application a
SET submitter_id = au.user_id
    FROM author au
WHERE au.application_id = a.id
  AND au.position = 1;

-- После заполнения делаем поле обязательным.
ALTER TABLE application
    ALTER COLUMN submitter_id SET NOT NULL;

-- Связываем подающего с зарегистрированным пользователем.
ALTER TABLE application
    ADD CONSTRAINT fk_application_submitter
        FOREIGN KEY (submitter_id)
            REFERENCES app_user(id);

-- Индекс для поиска заявок по подающему.
CREATE INDEX idx_application_submitter_id
    ON application(submitter_id);