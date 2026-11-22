-- Uniqueness and year/group consistency for the canonical student records.
-- Resolve duplicate or inconsistent rows reported by 00_diagnostic.sql first.
BEGIN;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM dossier_etudiant
        GROUP BY UPPER(matricule) HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate student matricules (case-insensitive); clean them before migration 07.';
    END IF;
    IF EXISTS (
        SELECT 1 FROM inscription_etudiant
        GROUP BY id_etudiant, id_groupe, id_annee HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate student/group/year enrollments; reconcile them before migration 07.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM inscription_etudiant i
        JOIN groupe g ON g.id_groupe = i.id_groupe
        WHERE i.id_annee <> g.id_annee
    ) THEN
        RAISE EXCEPTION 'Enrollments whose academic year differs from their group must be corrected before migration 07.';
    END IF;
END
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_dossier_etudiant_matricule_ci
    ON dossier_etudiant (UPPER(matricule));
CREATE UNIQUE INDEX IF NOT EXISTS uq_groupe_id_annee
    ON groupe (id_groupe, id_annee);
CREATE UNIQUE INDEX IF NOT EXISTS uq_inscription_etudiant_groupe_annee
    ON inscription_etudiant (id_etudiant, id_groupe, id_annee);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_inscription_etudiant_groupe_annee'
          AND conrelid = 'public.inscription_etudiant'::regclass
    ) THEN
        ALTER TABLE inscription_etudiant
            ADD CONSTRAINT fk_inscription_etudiant_groupe_annee
            FOREIGN KEY (id_groupe, id_annee)
            REFERENCES groupe (id_groupe, id_annee);
    END IF;
END
$$;

GRANT SELECT, INSERT, UPDATE ON TABLE
    public.dossier_etudiant,
    public.inscription_etudiant
TO edusuivi_app;

DO $$
DECLARE
    sequence_row record;
    sequence_name regclass;
BEGIN
    FOR sequence_row IN
        SELECT * FROM (VALUES
            ('public.dossier_etudiant', 'id_etudiant'),
            ('public.inscription_etudiant', 'id_inscription')
        ) AS identity_columns(table_name, column_name)
    LOOP
        sequence_name := pg_get_serial_sequence(
            sequence_row.table_name, sequence_row.column_name)::regclass;
        IF sequence_name IS NOT NULL THEN
            EXECUTE format(
                'GRANT USAGE, SELECT ON SEQUENCE %s TO %I',
                sequence_name, 'edusuivi_app');
        END IF;
    END LOOP;
END
$$;

COMMIT;
