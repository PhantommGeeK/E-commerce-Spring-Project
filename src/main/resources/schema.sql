CREATE TABLE IF NOT EXISTS authority (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    authority_id BIGINT NOT NULL,
    CONSTRAINT fk_users_authority FOREIGN KEY (authority_id) REFERENCES authority(id)
);

INSERT INTO authority (name)
VALUES ('ROLE_USER')
ON CONFLICT (name) DO NOTHING;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public' AND table_name = 'authorities'
    ) THEN
        INSERT INTO authority (name)
        SELECT old_auth.name
        FROM authorities old_auth
        ON CONFLICT (name) DO NOTHING;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'users' AND column_name = 'authority_id'
    ) THEN
        UPDATE users u
        SET authority_id = fallback_auth.id
        FROM authority fallback_auth
        WHERE fallback_auth.name = 'ROLE_USER'
          AND NOT EXISTS (
              SELECT 1 FROM authority a WHERE a.id = u.authority_id
          );
    END IF;
END $$;
