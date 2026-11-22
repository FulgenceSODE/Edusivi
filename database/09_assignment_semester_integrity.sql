-- Add the semester required by the canonical teaching-assignment model.
-- Prefer a unique legacy teaching record, when it agrees with the EC/UE
-- curriculum. Otherwise infer only when the EC maps to one semester.
BEGIN;

ALTER TABLE public.affectation_enseignement
    ADD COLUMN IF NOT EXISTS id_semestre BIGINT;

DO $$
BEGIN
    IF EXISTS (
        WITH semester_candidates AS (
            SELECT a.id_affectation,
                   COUNT(DISTINCT legacy_s.id_semestre) AS legacy_count,
                   COUNT(DISTINCT curriculum_s.id_semestre) AS curriculum_count,
                   COUNT(DISTINCT curriculum_s.id_semestre)
                       FILTER (WHERE curriculum_s.id_semestre = legacy_s.id_semestre) AS matching_count
            FROM affectation_enseignement a
            LEFT JOIN enseignement e
                ON e.id_ec = a.id_ec AND e.id_groupe = a.id_groupe
            LEFT JOIN semestre legacy_s
                ON legacy_s.id_semestre = e.id_semestre
               AND legacy_s.id_annee = a.id_annee
            LEFT JOIN composition_ue c ON c.id_ec = a.id_ec
            LEFT JOIN unite_enseignement u ON u.id_ue = c.id_ue
            LEFT JOIN semestre curriculum_s
                ON curriculum_s.id_semestre = u.id_semestre
               AND curriculum_s.id_annee = a.id_annee
            WHERE a.id_semestre IS NULL
            GROUP BY a.id_affectation
        )
        SELECT 1
        FROM semester_candidates
        WHERE legacy_count > 1
           OR (legacy_count = 1 AND (curriculum_count = 0 OR matching_count = 0))
           OR (legacy_count = 0 AND curriculum_count <> 1)
    ) THEN
        RAISE EXCEPTION
            'Some assignments have missing, conflicting, or ambiguous semester data; inspect diagnostic 3i before migration 09.';
    END IF;
    IF EXISTS (
        SELECT 1
        FROM affectation_enseignement a
        JOIN groupe g ON g.id_groupe = a.id_groupe
        WHERE a.id_annee <> g.id_annee
    ) THEN
        RAISE EXCEPTION 'Assignments whose year differs from their group must be corrected before migration 09.';
    END IF;
    IF EXISTS (
        SELECT 1 FROM affectation_enseignement
        GROUP BY id_annee, id_ec, id_groupe HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Duplicate assignments for the same year, EC and group must be reconciled before migration 09.';
    END IF;
END
$$;

-- A unique legacy teaching record is the best evidence when an EC belongs
-- to multiple UEs/semesters. The guard above ensures it agrees with the
-- curriculum association before it is used.
UPDATE affectation_enseignement a
SET id_semestre = candidates.id_semestre
FROM (
    SELECT a2.id_affectation, MIN(legacy_s.id_semestre) AS id_semestre
    FROM affectation_enseignement a2
    JOIN enseignement e ON e.id_ec = a2.id_ec AND e.id_groupe = a2.id_groupe
    JOIN semestre legacy_s
        ON legacy_s.id_semestre = e.id_semestre
       AND legacy_s.id_annee = a2.id_annee
    WHERE a2.id_semestre IS NULL
    GROUP BY a2.id_affectation
    HAVING COUNT(DISTINCT legacy_s.id_semestre) = 1
) candidates
WHERE a.id_affectation = candidates.id_affectation;

-- For assignments without matching legacy teaching rows, fall back only
-- when the EC has exactly one curriculum semester in the assignment year.
UPDATE affectation_enseignement a
SET id_semestre = candidates.id_semestre
FROM (
    SELECT a2.id_affectation, MIN(s.id_semestre) AS id_semestre
    FROM affectation_enseignement a2
    JOIN composition_ue c ON c.id_ec = a2.id_ec
    JOIN unite_enseignement u ON u.id_ue = c.id_ue
    JOIN semestre s ON s.id_semestre = u.id_semestre AND s.id_annee = a2.id_annee
    WHERE a2.id_semestre IS NULL
      AND NOT EXISTS (
          SELECT 1
          FROM enseignement e
          JOIN semestre legacy_s ON legacy_s.id_semestre = e.id_semestre
          WHERE e.id_ec = a2.id_ec
            AND e.id_groupe = a2.id_groupe
            AND legacy_s.id_annee = a2.id_annee
      )
    GROUP BY a2.id_affectation
    HAVING COUNT(DISTINCT s.id_semestre) = 1
) candidates
WHERE a.id_affectation = candidates.id_affectation;

ALTER TABLE affectation_enseignement ALTER COLUMN id_semestre SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_semestre_id_annee
    ON semestre (id_semestre, id_annee);
CREATE UNIQUE INDEX IF NOT EXISTS uq_groupe_id_annee
    ON groupe (id_groupe, id_annee);
CREATE UNIQUE INDEX IF NOT EXISTS uq_affectation_scope
    ON affectation_enseignement (id_annee, id_ec, id_groupe);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_affectation_groupe_annee'
          AND conrelid = 'public.affectation_enseignement'::regclass
    ) THEN
        ALTER TABLE affectation_enseignement
            ADD CONSTRAINT fk_affectation_groupe_annee
            FOREIGN KEY (id_groupe, id_annee)
            REFERENCES groupe (id_groupe, id_annee);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_affectation_semestre_annee'
          AND conrelid = 'public.affectation_enseignement'::regclass
    ) THEN
        ALTER TABLE affectation_enseignement
            ADD CONSTRAINT fk_affectation_semestre_annee
            FOREIGN KEY (id_semestre, id_annee)
            REFERENCES semestre (id_semestre, id_annee);
    END IF;
END
$$;

COMMIT;
