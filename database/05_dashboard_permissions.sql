-- Grants required by the WildFly datasource configured as edusuivi_app.
-- Run as the database owner (or a role allowed to grant these privileges).
BEGIN;

GRANT SELECT, INSERT, UPDATE ON TABLE public.utilisateur_preference TO edusuivi_app;
GRANT SELECT, INSERT, UPDATE ON TABLE
    public.annee_academique,
    public.semestre
TO edusuivi_app;
GRANT SELECT, INSERT ON TABLE public.journal_audit TO edusuivi_app;

DO $$
DECLARE
    sequence_row record;
    sequence_name regclass;
BEGIN
    FOR sequence_row IN
        SELECT * FROM (VALUES
            ('public.annee_academique', 'id_annee'),
            ('public.semestre', 'id_semestre'),
            ('public.journal_audit', 'id_audit')
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
