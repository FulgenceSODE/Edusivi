-- Align journal_audit.details with the application's String mapping.
-- Existing JSON values are preserved as readable text; JSON strings are
-- unwrapped, while objects and arrays retain their JSON representation.
BEGIN;

DO $$
DECLARE
    details_type TEXT;
BEGIN
    SELECT data_type
      INTO details_type
      FROM information_schema.columns
     WHERE table_schema = 'public'
       AND table_name = 'journal_audit'
       AND column_name = 'details';

    IF details_type IS NULL THEN
        RAISE EXCEPTION 'public.journal_audit.details does not exist.';
    ELSIF details_type = 'jsonb' THEN
        ALTER TABLE public.journal_audit
            ALTER COLUMN details TYPE TEXT
            USING CASE
                WHEN details IS NULL THEN NULL
                WHEN jsonb_typeof(details) = 'string' THEN details #>> '{}'
                ELSE details::TEXT
            END;
    ELSIF details_type = 'json' THEN
        ALTER TABLE public.journal_audit
            ALTER COLUMN details TYPE TEXT
            USING CASE
                WHEN details IS NULL THEN NULL
                WHEN json_typeof(details) = 'string' THEN details #>> '{}'
                ELSE details::TEXT
            END;
    ELSIF details_type = 'oid' THEN
        ALTER TABLE public.journal_audit
            ALTER COLUMN details TYPE TEXT
            USING CASE
                WHEN details IS NULL THEN NULL
                ELSE convert_from(lo_get(details), 'UTF8')
            END;
    ELSIF details_type NOT IN ('text', 'character varying') THEN
        ALTER TABLE public.journal_audit
            ALTER COLUMN details TYPE TEXT
            USING details::TEXT;
    END IF;
END
$$;

COMMIT;
