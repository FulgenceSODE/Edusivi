-- The lifecycle status is managed by the application (ACTIVE / TERMINEE).
-- Publication eligibility is enforced separately by statut_publication.
-- Remove the obsolete legacy check that rejects newly created evaluations.
BEGIN;

ALTER TABLE public.evaluation_cours
    DROP CONSTRAINT IF EXISTS ck_evaluation_statut;

COMMIT;
