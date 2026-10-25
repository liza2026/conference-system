# Система подачи и приёма заявок на конференцию

Стек: Java 21, Spring Boot, PostgreSQL, Flyway, React, TypeScript, Docker Compose.

## Суть

Один участник подаёт, изменяет и отзывает заявку (название, направление, 1-2 автора,
1-2 руководителя, файл тезисов .doc/.docx). Все действия доступны только до времени Т1
(настройка `DEADLINE_T1` в `.env`), после него сервер отвечает `409`.
Данные вводятся один раз, имя скачиваемого файла система собирает сама по шаблону из письма.

## Запуск

Нужен Docker Desktop.

```
copy .env.example .env
docker compose up -d --build
```

Сайт: http://localhost:8081. Остановить: `docker compose down`.

Для разработки: `docker compose up -d db`, бэкенд запускается из IDE (порт 8080),
фронтенд командами `cd frontend`, `npm install`, `npm run dev` (порт 5173).

## Тесты

`cd backend`, затем `./mvnw test` (нужен запущенный Docker: тесты поднимают временную PostgreSQL).

## Откат к итерации

```
git checkout iteration-1
docker compose down -v
```