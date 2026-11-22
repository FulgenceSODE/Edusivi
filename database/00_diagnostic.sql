-- =====================================================================
-- EduSuivi - 00_diagnostic.sql : DIAGNOSTIC EN LECTURE SEULE
-- A executer AVANT 01_migration.sql :  psql -U <user> -d <base> -f 00_diagnostic.sql
-- Ce script ne modifie rien. Chaque requete signale un probleme si elle
-- retourne des lignes (sauf les requetes "inventaire" qui sont informatives).
-- =====================================================================
\echo '=== 1. Doublons de matricule etudiant (doit etre vide)'
SELECT matricule, COUNT(*) FROM etudiant GROUP BY matricule HAVING COUNT(*) > 1;
\echo '=== 2. Doublons de matricule enseignant (doit etre vide)'
SELECT matricule, COUNT(*) FROM enseignant GROUP BY matricule HAVING COUNT(*) > 1;
\echo '=== 2b. Doublons de matricule dossier etudiant, insensible a la casse (doit etre vide)'
SELECT UPPER(matricule), COUNT(*) FROM dossier_etudiant
GROUP BY UPPER(matricule) HAVING COUNT(*) > 1;
\echo '=== 3. Doublons de code insensibles a la casse : departement, filiere, UE, EC (doit etre vide)'
SELECT 'departement' AS table_, UPPER(code), COUNT(*) FROM departement GROUP BY UPPER(code) HAVING COUNT(*) > 1
UNION ALL SELECT 'filiere', UPPER(code), COUNT(*) FROM filiere GROUP BY UPPER(code) HAVING COUNT(*) > 1
UNION ALL SELECT 'unite_enseignement', UPPER(code), COUNT(*) FROM unite_enseignement GROUP BY UPPER(code) HAVING COUNT(*) > 1
UNION ALL SELECT 'element_constitutif', UPPER(code), COUNT(*) FROM element_constitutif GROUP BY UPPER(code) HAVING COUNT(*) > 1;
\echo '=== 3b. Doublons de code de niveau par filiere et de groupe par annee/filiere/niveau (doit etre vide)'
SELECT 'niveau' AS table_, id_filiere::text || ':' || UPPER(code), COUNT(*)
FROM niveau WHERE id_filiere IS NOT NULL GROUP BY id_filiere, UPPER(code) HAVING COUNT(*) > 1
UNION ALL
SELECT 'groupe', id_annee::text || ':' || id_filiere::text || ':' || id_niveau::text || ':' || UPPER(code), COUNT(*)
FROM groupe GROUP BY id_annee, id_filiere, id_niveau, UPPER(code) HAVING COUNT(*) > 1;
\echo '=== 3c. Groupes dont la filiere ne correspond pas au niveau (doit etre vide)'
SELECT g.id_groupe, g.id_filiere, g.id_niveau, n.id_filiere AS filiere_du_niveau
FROM groupe g JOIN niveau n ON n.id_niveau = g.id_niveau
WHERE n.id_filiere IS NULL OR n.id_filiere <> g.id_filiere;
\echo '=== 3d. Inscriptions dont l''annee ne correspond pas au groupe (doit etre vide)'
SELECT i.id_inscription, i.id_annee, g.id_annee AS annee_du_groupe
FROM inscription_etudiant i JOIN groupe g ON g.id_groupe = i.id_groupe
WHERE i.id_annee <> g.id_annee;
\echo '=== 3e. Inscriptions dupliquees pour le meme etudiant/groupe/annee (doit etre vide)'
SELECT id_etudiant, id_groupe, id_annee, COUNT(*)
FROM inscription_etudiant GROUP BY id_etudiant, id_groupe, id_annee HAVING COUNT(*) > 1;
\echo '=== 3f. Notes sans inscription unique correspondant au groupe/annee de l''evaluation (doit etre vide)'
SELECT ne.id_evaluation, ne.id_etudiant, COUNT(ie.id_inscription) AS inscriptions_correspondantes
FROM note_evaluation ne
JOIN evaluation_cours ec ON ec.id_evaluation = ne.id_evaluation
JOIN affectation_enseignement ae ON ae.id_affectation = ec.id_affectation
LEFT JOIN inscription_etudiant ie
    ON ie.id_etudiant = ne.id_etudiant
   AND ie.id_annee = ae.id_annee
   AND ie.id_groupe = ae.id_groupe
GROUP BY ne.id_evaluation, ne.id_etudiant
HAVING COUNT(ie.id_inscription) <> 1;
\echo '=== 3g. Affectations avec 0 ou plusieurs semestres EC/UE candidats (peuvent etre resolues par enseignement historique)'
SELECT a.id_affectation, a.id_annee, a.id_ec, COUNT(DISTINCT s.id_semestre) AS semestres_correspondants
FROM affectation_enseignement a
LEFT JOIN composition_ue c ON c.id_ec = a.id_ec
LEFT JOIN unite_enseignement u ON u.id_ue = c.id_ue
LEFT JOIN semestre s ON s.id_semestre = u.id_semestre AND s.id_annee = a.id_annee
GROUP BY a.id_affectation, a.id_annee, a.id_ec
HAVING COUNT(DISTINCT s.id_semestre) <> 1;
\echo '=== 3i. Affectations que la migration 09 ne peut pas resoudre sans arbitrage (doit etre vide)'
WITH semester_candidates AS (
    SELECT a.id_affectation, a.id_annee, a.id_ec, a.id_groupe,
           COUNT(DISTINCT legacy_s.id_semestre) AS legacy_count,
           COUNT(DISTINCT curriculum_s.id_semestre) AS curriculum_count,
           COUNT(DISTINCT curriculum_s.id_semestre)
               FILTER (WHERE curriculum_s.id_semestre = legacy_s.id_semestre) AS matching_count,
           ARRAY_AGG(DISTINCT legacy_s.id_semestre)
               FILTER (WHERE legacy_s.id_semestre IS NOT NULL) AS legacy_semesters,
           ARRAY_AGG(DISTINCT curriculum_s.id_semestre)
               FILTER (WHERE curriculum_s.id_semestre IS NOT NULL) AS curriculum_semesters
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
    GROUP BY a.id_affectation, a.id_annee, a.id_ec, a.id_groupe
)
SELECT id_affectation, id_annee, id_ec, id_groupe,
       legacy_semesters, curriculum_semesters,
       CASE
           WHEN legacy_count > 1 THEN 'PLUSIEURS_SEMESTRES_DANS_ENSEIGNEMENT'
           WHEN legacy_count = 1 AND curriculum_count = 0 THEN 'EC_SANS_UE_DANS_CE_SEMESTRE'
           WHEN legacy_count = 1 AND matching_count = 0 THEN 'ENSEIGNEMENT_ET_UE_EN_CONFLIT'
           WHEN legacy_count = 0 AND curriculum_count = 0 THEN 'AUCUN_SEMESTRE_TROUVE'
           WHEN legacy_count = 0 AND curriculum_count > 1 THEN 'PLUSIEURS_SEMESTRES_UE'
       END AS motif
FROM semester_candidates
WHERE legacy_count > 1
   OR (legacy_count = 1 AND (curriculum_count = 0 OR matching_count = 0))
   OR (legacy_count = 0 AND curriculum_count <> 1);
\echo '=== 3j. Doublons d''absence par etudiant/seance (doit etre vide avant migration 11)'
SELECT id_etudiant, id_seance, COUNT(*)
FROM suivi_pedagogique_absence
GROUP BY id_etudiant, id_seance
HAVING COUNT(*) > 1;
\echo '=== 3h. Affectations dont l''annee ne correspond pas au groupe (doit etre vide)'
SELECT a.id_affectation, a.id_annee, g.id_annee AS annee_du_groupe
FROM affectation_enseignement a JOIN groupe g ON g.id_groupe = a.id_groupe
WHERE a.id_annee <> g.id_annee;
\echo '=== 4. Doublons d''email etudiant, insensible a la casse (doit etre vide)'
SELECT LOWER(email), COUNT(*) FROM etudiant WHERE email IS NOT NULL GROUP BY LOWER(email) HAVING COUNT(*) > 1;
\echo '=== 5. Notes hors de [0 ; 20] (RG51, doit etre vide)'
SELECT id_note, valeur FROM note WHERE valeur IS NOT NULL AND (valeur < 0 OR valeur > 20);
\echo '=== 6. Plusieurs notes pour la meme evaluation et la meme inscription (RG53, doit etre vide)'
SELECT id_evaluation, id_inscription, COUNT(*) FROM note GROUP BY id_evaluation, id_inscription HAVING COUNT(*) > 1;
\echo '=== 6b. Notes d''evaluation_cours hors de [0 ; 20] (RG51, doit etre vide)'
SELECT id_evaluation, id_etudiant, valeur
FROM note_evaluation WHERE valeur < 0 OR valeur > 20;
\echo '=== 7. Coefficients <= 0 dans composition_ue (RG36, doit etre vide)'
SELECT id_ue, id_ec, coefficient FROM composition_ue WHERE coefficient <= 0;
\echo '=== 8. Seances dont heure_debut >= heure_fin (RG45, doit etre vide)'
SELECT id_seance, heure_debut, heure_fin FROM seance_cours WHERE heure_debut >= heure_fin;
\echo '=== 9. volume_horaire_prevu non entier ou negatif (bloque la conversion en INTEGER, doit etre vide)'
SELECT id_enseignement, volume_horaire_prevu FROM enseignement
 WHERE volume_horaire_prevu IS NOT NULL AND (volume_horaire_prevu <> TRUNC(volume_horaire_prevu) OR volume_horaire_prevu < 0);
\echo '=== 10. journal_audit : lignes avec details (OID) ou id_entite non entier (doit etre vide ou verifiee a la main)'
SELECT COUNT(*) FILTER (WHERE details IS NOT NULL) AS lignes_avec_details,
       COUNT(*) FILTER (WHERE id_entite IS NOT NULL AND id_entite <> TRUNC(id_entite)) AS id_entite_non_entier
FROM journal_audit;
\echo '=== 11. EC dont le couple (id_ue, coefficient) n''est pas encore dans composition_ue (sera copie par la migration)'
SELECT ec.id_ec, ec.code, ec.id_ue, ec.coefficient
FROM element_constitutif ec
LEFT JOIN composition_ue cu ON cu.id_ue = ec.id_ue AND cu.id_ec = ec.id_ec
WHERE ec.id_ue IS NOT NULL AND cu.id_ec IS NULL;
\echo '=== 12. EC dont element_constitutif.coefficient differe de composition_ue.coefficient (conflit a arbitrer)'
SELECT ec.id_ec, ec.code, ec.coefficient AS coef_ec, cu.coefficient AS coef_composition
FROM element_constitutif ec JOIN composition_ue cu ON cu.id_ue = ec.id_ue AND cu.id_ec = ec.id_ec
WHERE ec.coefficient <> cu.coefficient;
\echo '=== 13. Plusieurs annees academiques ''actives'' (RG15) - valeurs de statut observees'
SELECT statut, COUNT(*) FROM annee_academique GROUP BY statut ORDER BY statut;
\echo '=== 14. Inventaire des valeurs de statut / categorie (pour definir les enumerations)'
SELECT 'utilisateur.statut' AS colonne, statut AS valeur, COUNT(*) FROM utilisateur GROUP BY statut
UNION ALL SELECT 'inscription.statut', statut, COUNT(*) FROM inscription GROUP BY statut
UNION ALL SELECT 'inscription_etudiant.statut', statut, COUNT(*) FROM inscription_etudiant GROUP BY statut
UNION ALL SELECT 'enseignement.statut', statut, COUNT(*) FROM enseignement GROUP BY statut
UNION ALL SELECT 'evaluation.categorie', categorie, COUNT(*) FROM evaluation GROUP BY categorie
UNION ALL SELECT 'evaluation.statut_publication', statut_publication, COUNT(*) FROM evaluation GROUP BY statut_publication
UNION ALL SELECT 'note.statut_presence', statut_presence, COUNT(*) FROM note GROUP BY statut_presence
UNION ALL SELECT 'etudiant.sexe', sexe, COUNT(*) FROM etudiant GROUP BY sexe
ORDER BY 1, 2;
\echo '=== 15. Inventaire : nombre de lignes par table (tables du second modele incluses)'
SELECT 'utilisateur' AS table_, COUNT(*) FROM utilisateur UNION ALL SELECT 'role', COUNT(*) FROM role
UNION ALL SELECT 'utilisateur_role', COUNT(*) FROM utilisateur_role
UNION ALL SELECT 'etudiant', COUNT(*) FROM etudiant UNION ALL SELECT 'inscription', COUNT(*) FROM inscription
UNION ALL SELECT 'note', COUNT(*) FROM note UNION ALL SELECT 'historique_note', COUNT(*) FROM historique_note
UNION ALL SELECT 'enseignement', COUNT(*) FROM enseignement
UNION ALL SELECT 'dossier_etudiant (2e modele)', COUNT(*) FROM dossier_etudiant
UNION ALL SELECT 'inscription_etudiant (2e modele)', COUNT(*) FROM inscription_etudiant
UNION ALL SELECT 'affectation_enseignement (2e modele)', COUNT(*) FROM affectation_enseignement
UNION ALL SELECT 'evaluation_cours (2e modele)', COUNT(*) FROM evaluation_cours
UNION ALL SELECT 'note_evaluation (2e modele)', COUNT(*) FROM note_evaluation
UNION ALL SELECT 'chapitre_cours (2e modele)', COUNT(*) FROM chapitre_cours
UNION ALL SELECT 'suivi_pedagogique_seance (2e modele)', COUNT(*) FROM suivi_pedagogique_seance
UNION ALL SELECT 'suivi_pedagogique_absence (2e modele)', COUNT(*) FROM suivi_pedagogique_absence
UNION ALL SELECT 'demande_inscription_enseignant', COUNT(*) FROM demande_inscription_enseignant
UNION ALL SELECT 'journal_audit', COUNT(*) FROM journal_audit;
\echo '=== 16. Mots de passe : longueur/format des hachages (ne doit PAS ressembler a un mot de passe en clair)'
SELECT LEFT(mot_de_passe_hash, 7) AS prefixe, LENGTH(mot_de_passe_hash) AS longueur, COUNT(*) FROM utilisateur GROUP BY 1, 2;
