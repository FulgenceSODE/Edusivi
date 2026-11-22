-- =====================================================================
-- EduSuivi - 01_migration.sql : alignement du schema sur le DAT / UML
-- !! FAIRE UNE SAUVEGARDE AVANT :  pg_dump -Fc -U <user> <base> > sauvegarde_avant_migration.dump
-- !! Executer 00_diagnostic.sql d'abord ; corriger les lignes signalees.
-- Execution : psql -U <user> -d <base> -v ON_ERROR_STOP=1 -f 01_migration.sql
-- Tout est dans UNE transaction : a la moindre erreur, rien n'est applique.
-- Aucune donnee n'est supprimee ; seuls des types et des contraintes changent.
-- =====================================================================
BEGIN;

-- 1. enseignement.volume_horaire_prevu : NUMERIC -> INTEGER (DAT, decision du projet)
ALTER TABLE enseignement
    ALTER COLUMN volume_horaire_prevu TYPE INTEGER USING volume_horaire_prevu::INTEGER;
ALTER TABLE enseignement
    ADD CONSTRAINT ck_enseignement_volume_positif CHECK (volume_horaire_prevu IS NULL OR volume_horaire_prevu >= 0);

-- 2. Le coefficient appartient a composition_ue (RG35, RG37).
--    Copie de element_constitutif(id_ue, coefficient) vers composition_ue quand le couple n'existe pas,
--    puis element_constitutif.coefficient devient facultative. Les colonnes ne sont PAS supprimees
--    (a faire plus tard, apres validation) : l'application ne les utilise plus.
INSERT INTO composition_ue (id_ue, id_ec, coefficient, ordre)
SELECT ec.id_ue, ec.id_ec, ec.coefficient, NULL
FROM element_constitutif ec
WHERE ec.id_ue IS NOT NULL AND ec.coefficient > 0
ON CONFLICT (id_ue, id_ec) DO NOTHING;
ALTER TABLE element_constitutif ALTER COLUMN coefficient DROP NOT NULL;

-- 3. journal_audit : details OID -> TEXT ; id_entite NUMERIC -> VARCHAR(100) (DAT)
ALTER TABLE journal_audit
    ALTER COLUMN details TYPE TEXT
    USING CASE WHEN details IS NULL THEN NULL ELSE convert_from(lo_get(details), 'UTF8') END;
ALTER TABLE journal_audit
    ALTER COLUMN id_entite TYPE VARCHAR(100) USING id_entite::BIGINT::TEXT;
-- Les anciens objets larges eventuels restent a purger a la main : vacuumlo <base>  (optionnel)

-- 4. Contraintes d'unicite du DAT
CREATE UNIQUE INDEX uq_etudiant_matricule            ON etudiant (matricule);
CREATE UNIQUE INDEX uq_etudiant_email_ci             ON etudiant (LOWER(email)) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX uq_enseignant_matricule          ON enseignant (matricule);
CREATE UNIQUE INDEX uq_departement_code              ON departement (code);
CREATE UNIQUE INDEX uq_filiere_code                  ON filiere (code);
CREATE UNIQUE INDEX uq_unite_enseignement_code       ON unite_enseignement (code);
CREATE UNIQUE INDEX uq_element_constitutif_code      ON element_constitutif (code);

-- 5. Regles de gestion portees par la base
ALTER TABLE note ADD CONSTRAINT ck_note_valeur_0_20 CHECK (valeur IS NULL OR (valeur >= 0 AND valeur <= 20));            -- RG51
CREATE UNIQUE INDEX uq_note_evaluation_inscription ON note (id_evaluation, id_inscription);                              -- RG53
ALTER TABLE composition_ue ADD CONSTRAINT ck_composition_coefficient_positif CHECK (coefficient > 0);                    -- RG36
ALTER TABLE seance_cours ADD CONSTRAINT ck_seance_heures CHECK (heure_debut < heure_fin);                                -- RG45
ALTER TABLE annee_academique ADD CONSTRAINT ck_annee_dates CHECK (date_debut < date_fin);                                -- RG14
ALTER TABLE semestre ADD CONSTRAINT ck_semestre_dates CHECK (date_debut < date_fin);

COMMIT;
