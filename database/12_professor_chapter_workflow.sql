BEGIN;

GRANT SELECT, INSERT, UPDATE ON TABLE public.chapitre_cours TO edusuivi_app;

DO $$
DECLARE
    sequence_name REGCLASS;
BEGIN
    sequence_name := pg_get_serial_sequence(
        'public.chapitre_cours', 'id_chapitre')::REGCLASS;
    IF sequence_name IS NOT NULL THEN
        EXECUTE format(
            'GRANT USAGE, SELECT ON SEQUENCE %s TO %I',
            sequence_name, 'edusuivi_app');
    END IF;
END
$$;

COMMIT;
