-- Выполнить в lab3_db от владельца БД: psql -d lab3_db -f database/lab5.sql
-- Приложение использует схему public и сервисную роль lab_user.
BEGIN;
CREATE TABLE IF NOT EXISTS public.user_sessions (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    device_id VARCHAR(255),
    access_token TEXT NOT NULL,
    refresh_token TEXT NOT NULL UNIQUE,
    access_token_expiry TIMESTAMPTZ NOT NULL,
    refresh_token_expiry TIMESTAMPTZ NOT NULL,
    status VARCHAR(255) NOT NULL CHECK (status IN ('ACTIVE', 'USED', 'REVOKED'))
);
-- Эти ограничения добавляются и к таблице, которую раньше создал Hibernate.
CREATE UNIQUE INDEX IF NOT EXISTS user_sessions_refresh_unique ON public.user_sessions(refresh_token);
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint
                   WHERE conname = 'user_sessions_status_check'
                   AND conrelid = 'public.user_sessions'::regclass) THEN
        ALTER TABLE public.user_sessions ADD CONSTRAINT user_sessions_status_check
            CHECK (status IN ('ACTIVE', 'USED', 'REVOKED'));
    END IF;
    IF to_regclass('public.users') IS NOT NULL THEN
        CREATE UNIQUE INDEX IF NOT EXISTS users_email_unique ON public.users(email);
    END IF;
END $$;
GRANT USAGE ON SCHEMA public TO lab_user;
GRANT SELECT, INSERT, UPDATE ON public.user_sessions TO lab_user;
COMMIT;
