-- Заменить email на значение runEmail из Postman.
SELECT id, username, device_id, status,
       access_token_expiry, refresh_token_expiry
FROM public.user_sessions
WHERE username = 'EMAIL_FROM_POSTMAN'
ORDER BY refresh_token_expiry, id;

-- После логина: ACTIVE = 1. После refresh: USED = 1, ACTIVE = 1.
-- После повтора старого refresh: REVOKED = 1, ACTIVE = 1, всего две строки.
SELECT status, count(*)
FROM public.user_sessions
WHERE username = 'EMAIL_FROM_POSTMAN'
GROUP BY status;
