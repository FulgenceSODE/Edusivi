-- Data needed by the professor assessment and attendance workflows.
BEGIN;

ALTER TABLE public.evaluation_cours
    ADD COLUMN IF NOT EXISTS categorie VARCHAR(20);
ALTER TABLE public.evaluation_cours
    ADD COLUMN IF NOT EXISTS bareme NUMERIC;
ALTER TABLE public.evaluation_cours
    ADD COLUMN IF NOT EXISTS statut_publication VARCHAR(20);

UPDATE public.evaluation_cours
   SET categorie = UPPER(BTRIM(categorie))
 WHERE categorie IS NOT NULL;
UPDATE public.evaluation_cours
   SET categorie = 'DEVOIR'
 WHERE categorie IS NULL OR BTRIM(categorie) = '';
UPDATE public.evaluation_cours
   SET bareme = 20
 WHERE bareme IS NULL;
UPDATE public.evaluation_cours
   SET statut_publication = CASE
       WHEN UPPER(BTRIM(statut)) IN ('PUBLIEE', 'PUBLISHED') THEN 'PUBLIEE'
       ELSE 'BROUILLON'
   END
 WHERE statut_publication IS NULL OR BTRIM(statut_publication) = '';
UPDATE public.evaluation_cours
   SET statut_publication = UPPER(BTRIM(statut_publication))
 WHERE statut_publication IS NOT NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM public.evaluation_cours
        WHERE categorie NOT IN ('INTERROGATION', 'DEVOIR', 'EXAMEN')
    ) THEN
        RAISE EXCEPTION 'Invalid evaluation categories; use INTERROGATION, DEVOIR or EXAMEN before migration 11.';
    END IF;
    IF EXISTS (
        SELECT 1 FROM public.evaluation_cours
        WHERE bareme <> 20
    ) THEN
        RAISE EXCEPTION 'Evaluation scales other than 20 must be reconciled before migration 11.';
    END IF;
    IF EXISTS (
        SELECT 1 FROM public.evaluation_cours
        WHERE statut_publication NOT IN ('BROUILLON', 'PUBLIEE')
    ) THEN
        RAISE EXCEPTION 'Invalid evaluation publication statuses before migration 11.';
    END IF;
    IF EXISTS (
        SELECT 1 FROM public.suivi_pedagogique_absence
        GROUP BY id_etudiant, id_seance
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate absences for the same student and session must be reconciled before migration 11.';
    END IF;
END
$$;

ALTER TABLE public.evaluation_cours
    ALTER COLUMN categorie SET DEFAULT 'DEVOIR',
    ALTER COLUMN categorie SET NOT NULL,
    ALTER COLUMN bareme SET DEFAULT 20,
    ALTER COLUMN bareme SET NOT NULL,
    ALTER COLUMN statut_publication SET DEFAULT 'BROUILLON',
    ALTER COLUMN statut_publication SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_evaluation_categorie'
          AND conrelid = 'public.evaluation_cours'::regclass
    ) THEN
        ALTER TABLE public.evaluation_cours
            ADD CONSTRAINT ck_evaluation_categorie
            CHECK (categorie IN ('INTERROGATION', 'DEVOIR', 'EXAMEN'));
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_evaluation_bareme_20'
          AND conrelid = 'public.evaluation_cours'::regclass
    ) THEN
        ALTER TABLE public.evaluation_cours
            ADD CONSTRAINT ck_evaluation_bareme_20 CHECK (bareme = 20);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_evaluation_statut_publication'
          AND conrelid = 'public.evaluation_cours'::regclass
    ) THEN
        ALTER TABLE public.evaluation_cours
            ADD CONSTRAINT ck_evaluation_statut_publication
            CHECK (statut_publication IN ('BROUILLON', 'PUBLIEE'));
    END IF;
END
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_absence_student_session
    ON public.suivi_pedagogique_absence (id_etudiant, id_seance);

GRANT SELECT, INSERT, UPDATE ON TABLE
    public.evaluation_cours,
    public.note_evaluation,
    public.suivi_pedagogique_seance,
    public.suivi_pedagogique_absence
TO edusuivi_app;

DO $$
DECLARE
    sequence_row RECORD;
    sequence_name REGCLASS;
BEGIN
    FOR sequence_row IN
        SELECT * FROM (VALUES
            ('public.evaluation_cours', 'id_evaluation'),
            ('public.suivi_pedagogique_seance', 'id_seance'),
            ('public.suivi_pedagogique_absence', 'id_absence')
        ) AS identity_columns(table_name, column_name)
    LOOP
        sequence_name := pg_get_serial_sequence(
            sequence_row.table_name, sequence_row.column_name)::REGCLASS;
        IF sequence_name IS NOT NULL THEN
            EXECUTE format(
                'GRANT USAGE, SELECT ON SEQUENCE %s TO %I',
                sequence_name, 'edusuivi_app');
        END IF;
    END LOOP;
END
$$;

COMMIT;
