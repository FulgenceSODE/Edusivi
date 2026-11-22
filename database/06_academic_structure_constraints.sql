-- Uniqueness constraints for academic structure codes. Resolve duplicate rows
-- reported by database/00_diagnostic.sql before applying this migration.
BEGIN;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM departement GROUP BY UPPER(code) HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate department codes (case-insensitive); clean them before migration 06.';
    END IF;
    IF EXISTS (
        SELECT 1 FROM filiere GROUP BY UPPER(code) HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate program codes (case-insensitive); clean them before migration 06.';
    END IF;
    IF EXISTS (
        SELECT 1 FROM unite_enseignement GROUP BY UPPER(code) HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate teaching-unit codes (case-insensitive); clean them before migration 06.';
    END IF;
    IF EXISTS (
        SELECT 1 FROM element_constitutif GROUP BY UPPER(code) HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate course-element codes (case-insensitive); clean them before migration 06.';
    END IF;
    IF EXISTS (
        SELECT 1 FROM niveau
        WHERE id_filiere IS NOT NULL
        GROUP BY id_filiere, UPPER(code)
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate level codes within a program; clean them before migration 06.';
    END IF;
    IF EXISTS (
        SELECT 1 FROM groupe
        GROUP BY id_annee, id_filiere, id_niveau, UPPER(code)
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate group codes within an academic scope; clean them before migration 06.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM groupe g
        JOIN niveau n ON n.id_niveau = g.id_niveau
        WHERE n.id_filiere IS NULL OR n.id_filiere <> g.id_filiere
    ) THEN
        RAISE EXCEPTION 'Groups whose program differs from their level must be corrected before migration 06.';
    END IF;
END
$$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_departement_code_ci
    ON departement (UPPER(code));
CREATE UNIQUE INDEX IF NOT EXISTS uq_filiere_code_ci
    ON filiere (UPPER(code));
CREATE UNIQUE INDEX IF NOT EXISTS uq_unite_enseignement_code_ci
    ON unite_enseignement (UPPER(code));
CREATE UNIQUE INDEX IF NOT EXISTS uq_element_constitutif_code_ci
    ON element_constitutif (UPPER(code));
CREATE UNIQUE INDEX IF NOT EXISTS uq_niveau_filiere_code_ci
    ON niveau (id_filiere, UPPER(code));
CREATE UNIQUE INDEX IF NOT EXISTS uq_niveau_id_filiere
    ON niveau (id_niveau, id_filiere);
CREATE UNIQUE INDEX IF NOT EXISTS uq_groupe_scope_code_ci
    ON groupe (id_annee, id_filiere, id_niveau, UPPER(code));

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_groupe_niveau_filiere' AND conrelid = 'public.groupe'::regclass
    ) THEN
        ALTER TABLE groupe
            ADD CONSTRAINT fk_groupe_niveau_filiere
            FOREIGN KEY (id_niveau, id_filiere)
            REFERENCES niveau (id_niveau, id_filiere);
    END IF;
END
$$;

GRANT SELECT ON TABLE public.semestre TO edusuivi_app;
GRANT SELECT, INSERT, UPDATE ON TABLE
    public.departement,
    public.filiere,
    public.niveau,
    public.groupe,
    public.unite_enseignement,
    public.element_constitutif,
    public.composition_ue,
    public.affectation_enseignement
TO edusuivi_app;

DO $$
DECLARE
    sequence_row record;
    sequence_name regclass;
BEGIN
    FOR sequence_row IN
        SELECT * FROM (VALUES
            ('public.departement', 'id_departement'),
            ('public.filiere', 'id_filiere'),
            ('public.niveau', 'id_niveau'),
            ('public.groupe', 'id_groupe'),
            ('public.unite_enseignement', 'id_ue'),
            ('public.element_constitutif', 'id_ec'),
            ('public.affectation_enseignement', 'id_affectation')
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
