-- EduSuivi: prerequisites for secure registration and JAAS authentication.
-- Back up the database before applying this migration.
-- This migration does not rewrite existing passwords. Inspect their format
-- with 00_diagnostic.sql before enabling the bcrypt JDBC realm.
BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE utilisateur
    ADD COLUMN IF NOT EXISTS password_reset_required BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE demande_inscription_enseignant
    ADD COLUMN IF NOT EXISTS specialite VARCHAR(150);

CREATE UNIQUE INDEX IF NOT EXISTS uq_utilisateur_email_ci
    ON utilisateur (LOWER(email));

CREATE UNIQUE INDEX IF NOT EXISTS uq_demande_enseignant_email_ci
    ON demande_inscription_enseignant (LOWER(email));

CREATE UNIQUE INDEX IF NOT EXISTS uq_enseignant_matricule
    ON enseignant (matricule);

UPDATE annee_academique
SET statut = 'ACTIVE'
WHERE UPPER(BTRIM(statut)) IN ('ACTIVE', 'ACTIF', 'EN_COURS', 'EN COURS');

UPDATE annee_academique
SET statut = 'INACTIVE'
WHERE UPPER(BTRIM(statut)) IN ('INACTIVE', 'INACTIF');

CREATE UNIQUE INDEX IF NOT EXISTS uq_annee_academique_active
    ON annee_academique (UPPER(statut))
    WHERE UPPER(statut) = 'ACTIVE';

CREATE TABLE IF NOT EXISTS utilisateur_preference (
    id_utilisateur BIGINT PRIMARY KEY,
    theme VARCHAR(10) NOT NULL DEFAULT 'light',
    langue VARCHAR(5) NOT NULL DEFAULT 'fr',
    CONSTRAINT fk_utilisateur_preference_utilisateur
        FOREIGN KEY (id_utilisateur) REFERENCES utilisateur (id_utilisateur) ON DELETE CASCADE,
    CONSTRAINT ck_utilisateur_preference_theme CHECK (theme IN ('light', 'dark')),
    CONSTRAINT ck_utilisateur_preference_langue CHECK (langue IN ('fr', 'en'))
);

COMMIT;

-- New passwords are stored as PostgreSQL pgcrypto bcrypt values:
-- crypt(password, gen_salt('bf', 12)).
-- Legacy password values must be reset or migrated explicitly; never hash an
-- unknown digest again, as the original password cannot be recovered from it.
