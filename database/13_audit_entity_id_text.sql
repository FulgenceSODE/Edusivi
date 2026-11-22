-- Align journal_audit.id_entite with JournalAudit and all audit producers.
-- Existing numeric identifiers are preserved as their decimal text.
BEGIN;

ALTER TABLE journal_audit
    ALTER COLUMN id_entite TYPE VARCHAR(100)
    USING id_entite::TEXT;

COMMIT;
