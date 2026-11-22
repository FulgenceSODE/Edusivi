-- Migrate legacy note_evaluation rows to their exact student enrollment.
-- The transaction stops without changing data if a note cannot be mapped
-- uniquely to the evaluation's assigned group and academic year.
BEGIN;

ALTER TABLE public.note_evaluation
    ADD COLUMN IF NOT EXISTS id_inscription BIGINT;

DO $$
DECLARE
    primary_key_name TEXT;
    foreign_key_row RECORD;
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'note_evaluation'
          AND column_name = 'id_etudiant'
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM note_evaluation ne
            JOIN evaluation_cours ec ON ec.id_evaluation = ne.id_evaluation
            JOIN affectation_enseignement ae ON ae.id_affectation = ec.id_affectation
            LEFT JOIN inscription_etudiant ie
                ON ie.id_etudiant = ne.id_etudiant
               AND ie.id_annee = ae.id_annee
               AND ie.id_groupe = ae.id_groupe
            WHERE ne.id_inscription IS NULL
            GROUP BY ne.id_evaluation, ne.id_etudiant
            HAVING COUNT(ie.id_inscription) <> 1
        ) THEN
            RAISE EXCEPTION
                'Some notes have zero or multiple matching enrollments; reconcile them before migration 08.';
        END IF;

        UPDATE note_evaluation ne
        SET id_inscription = ie.id_inscription
        FROM evaluation_cours ec
        JOIN affectation_enseignement ae ON ae.id_affectation = ec.id_affectation
        JOIN inscription_etudiant ie
            ON ie.id_annee = ae.id_annee
           AND ie.id_groupe = ae.id_groupe
        WHERE ec.id_evaluation = ne.id_evaluation
          AND ie.id_etudiant = ne.id_etudiant
          AND ne.id_inscription IS NULL;

        IF EXISTS (SELECT 1 FROM note_evaluation WHERE id_inscription IS NULL) THEN
            RAISE EXCEPTION 'At least one note remains without an enrollment; no data was changed.';
        END IF;

        ALTER TABLE note_evaluation ALTER COLUMN id_inscription SET NOT NULL;

        FOR foreign_key_row IN
            SELECT conname
            FROM pg_constraint
            WHERE conrelid = 'public.note_evaluation'::regclass
              AND confrelid = 'public.dossier_etudiant'::regclass
              AND contype = 'f'
        LOOP
            EXECUTE format(
                'ALTER TABLE public.note_evaluation DROP CONSTRAINT %I',
                foreign_key_row.conname);
        END LOOP;

        SELECT conname INTO primary_key_name
        FROM pg_constraint
        WHERE conrelid = 'public.note_evaluation'::regclass
          AND contype = 'p';
        IF primary_key_name IS NOT NULL THEN
            EXECUTE format(
                'ALTER TABLE public.note_evaluation DROP CONSTRAINT %I',
                primary_key_name);
        END IF;

        ALTER TABLE note_evaluation DROP COLUMN id_etudiant;
        ALTER TABLE note_evaluation
            ADD CONSTRAINT pk_note_evaluation_inscription
            PRIMARY KEY (id_evaluation, id_inscription);
        ALTER TABLE note_evaluation
            ADD CONSTRAINT fk_note_evaluation_inscription
            FOREIGN KEY (id_inscription)
            REFERENCES inscription_etudiant (id_inscription);
    END IF;
END
$$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM note_evaluation
        WHERE valeur < 0 OR valeur > 20
    ) THEN
        RAISE EXCEPTION 'Notes outside [0,20] must be corrected before migration 08.';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_note_evaluation_valeur'
          AND conrelid = 'public.note_evaluation'::regclass
    ) THEN
        ALTER TABLE note_evaluation
            ADD CONSTRAINT ck_note_evaluation_valeur CHECK (valeur >= 0 AND valeur <= 20);
    END IF;
END
$$;

GRANT SELECT, INSERT, UPDATE ON TABLE public.note_evaluation TO edusuivi_app;

COMMIT;
