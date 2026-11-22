# EduSuivi universitaire

Plateforme de suivi pédagogique construite avec Jakarta EE 11, JSP/CSS, JAAS/Elytron, WildFly 42, PostgreSQL et JDK 21. La base attendue est `edusuivi_db`.

## Préparation de la base

Sur une base existante, effectuer une sauvegarde et suivre cet ordre dans `database/` :

1. `00_diagnostic.sql` (lecture seule), puis corriger les anomalies signalées.
2. `01_migration.sql`, `02_roles.sql`, `03_jaas_security.sql` et `04_auth_workflow.sql`.
3. La migration 04 crée les préférences persistantes, active `pgcrypto`, ajoute le marqueur de changement obligatoire du mot de passe et normalise quelques libellés usuels d'année active. Elle échoue volontairement si plusieurs années restent actives après normalisation : résoudre ce cas avant de la relancer.
4. Exécuter `05_dashboard_permissions.sql` en tant que propriétaire de la base (ou rôle autorisé à accorder ces droits). Le script accorde au rôle PostgreSQL `edusuivi_app`, configuré dans le datasource WildFly actuel, les droits nécessaires aux préférences, à la gestion des années/semestres et au journal d'audit.
5. Exécuter `06_academic_structure_constraints.sql`. Il vérifie les doublons de codes et la cohérence filière/niveau des groupes, ajoute les index uniques insensibles à la casse, protège cette relation par une clé étrangère composite et accorde au rôle `edusuivi_app` les droits de lecture/écriture nécessaires (sans suppression).
6. Exécuter `07_student_enrollment_integrity.sql`. Il protège l’unicité des matricules et des inscriptions par étudiant/groupe/année et vérifie que l’année d’une inscription correspond à celle de son groupe.
7. Exécuter `08_note_enrollment_link.sql` avant de déployer le modèle mis à jour des notes. Le script rattache chaque ancienne note à l’inscription unique correspondant au groupe et à l’année de son évaluation; il s’arrête sans supprimer de données si une note n’a aucune correspondance ou plusieurs correspondances.
8. Exécuter `09_assignment_semester_integrity.sql`. Il ajoute le semestre aux affectations; il utilise d’abord une affectation `enseignement` historique unique si elle concorde avec le rattachement EC/UE, puis ne déduit le semestre que si un seul choix reste possible. Si la migration s’arrête, consulter les lignes de diagnostic 3i, corriger ou arbitrer ces données, puis la relancer. Les scripts 05 à 08 ciblent le rôle PostgreSQL `edusuivi_app`; le remplacer si le datasource utilise un autre rôle.
9. Exécuter `10_audit_details_text.sql` sur une base où `journal_audit.details` est en `jsonb` ou `oid`. Il convertit cette colonne en `TEXT`, conformément au modèle Java, et conserve les données existantes.
10. Exécuter `11_professor_assessment_workflows.sql`. Il ajoute la catégorie, le barème standard et l’état de publication des évaluations, empêche les absences en double et accorde au rôle PostgreSQL `edusuivi_app` les droits nécessaires aux workflows professeur. Le script s’arrête si les anciennes données contredisent ces règles.
11. Exécuter `12_professor_chapter_workflow.sql` pour accorder au rôle PostgreSQL `edusuivi_app` les droits de lecture/écriture des chapitres et l’accès à leur séquence.
12. Exécuter `13_audit_entity_id_text.sql` pour aligner le type de l’identifiant d’entité du journal d’audit sur le modèle Java.
13. Exécuter `14_remove_legacy_evaluation_status_check.sql` pour retirer la contrainte de statut devenue incompatible avec le cycle de vie des évaluations.

`schema_reference.sql` est destiné à une base vide ; ne pas le rejouer sur une base existante. Les mots de passe historiques ne sont pas convertis automatiquement. Vérifier leur format et préparer une réinitialisation sûre avant d'activer le realm Elytron.

## Configuration et lancement

1. Arrêter WildFly. Les scripts ciblent `standalone-full.xml`, le profil utilisé par l’instance actuelle.
2. Si le profil ne possède pas encore de datasource PostgreSQL, définir `PG_DRIVER_JAR`, `EDUSUIVI_DB_URL`, `EDUSUIVI_DB_USER` et `EDUSUIVI_DB_PASSWORD`, puis exécuter `wildfly/configure-wildfly.cli`. Dans le profil actuel, le datasource existe sous le nom `EdusuiviDS` et le JNDI `java:/EdusuiviDS` : ne pas le recréer.
3. Depuis la racine du projet, serveur arrêté, exécuter avec PowerShell `& "$env:WILDFLY_HOME\bin\jboss-cli.bat" --file="wildfly\configure-elytron-jaas.cli"`, puis redémarrer WildFly avec le profil `standalone-full.xml`.
4. Déployer depuis NetBeans ou lancer `mvn clean package`, puis déployer `target/EduSuivi.war` sur WildFly.

La connexion des comptes DE/professeur passe par `/login.jsp`; le formulaire d'inscription professeur est public et sa validation est réservée à l'administration. L'accès administrateur commence à `/admin/login.jsp`. Les pages de paramètres permettent à chaque compte de choisir son thème et sa langue. Le DE gère les années et semestres académiques, y compris le changement d'année active, les départements, filières, niveaux, groupes, UE, EC et coefficients depuis `/de/structure`, les dossiers et inscriptions étudiants, ainsi que les affectations depuis `/de/affectations`. Une affectation lie un seul professeur responsable, un EC, un groupe et un semestre de la même année; les changements sont audités et une réaffectation est bloquée après la première activité pédagogique. Les entités académiques ne sont pas supprimées afin de préserver les liens et l'historique.

Après avoir appliqué les migrations 04 et 05 et déployé le WAR, exécuter une seule fois `psql -U <user> -d edusuivi_db -v ON_ERROR_STOP=1 -f database/reset_all_accounts.psql` pour convertir les anciens comptes vers des hashes BCrypt et générer un mot de passe temporaire unique par compte. Le script affiche ces accès une seule fois : les remettre individuellement par un canal privé, ne pas rediriger la sortie vers un fichier ou un journal, et ne jamais la partager dans le dépôt ou le chat. Chaque compte devra changer ce mot de passe avant d’accéder aux autres pages. Ne pas relancer le script après distribution, car il invaliderait tous les codes déjà remis.

## Déploiement GitHub et Render

Le `Dockerfile` construit le WAR avec Java 21 et l’exécute sous WildFly 42. Render peut créer le service Web à partir du dépôt et de `render.yaml`. Le contrôle de santé utilise `/login.jsp`.

Le service utilise la base PostgreSQL existante via les variables Render `EDUSUIVI_DB_URL`, `EDUSUIVI_DB_USER` et `EDUSUIVI_DB_PASSWORD`. Saisir leurs valeurs uniquement dans les paramètres privés du service Render, jamais dans le dépôt, le Dockerfile ou ce fichier. L’URL doit être une URL JDBC vers `edusuivi_db` et le serveur PostgreSQL doit accepter les connexions réseau depuis Render; appliquer les exigences TLS du fournisseur.

Le conteneur configure le datasource JNDI `java:/EdusuiviDS` et le domaine Elytron au démarrage. Il n’exécute aucune migration SQL et Hibernate est configuré pour ne pas modifier le schéma. Sauvegarder la base puis appliquer manuellement uniquement les migrations encore nécessaires avant le premier déploiement. Ne pas importer de sauvegarde ou de données personnelles dans le dépôt public. Les fichiers `.env`, sauvegardes, WAR générés et sorties de build sont exclus par `.gitignore`.

## État et validation

Les contrôleurs de connexion, d'inscription, d'approbation, de paramètres, du calendrier académique, de la structure (départements, filières, niveaux, groupes, UE, EC et coefficients), des dossiers étudiants, des inscriptions et des affectations sont en place. Les tableaux de bord et les vues professeur s'appuient sur les données enregistrées. Les séances, évaluations, notes, calculs de moyennes et relevés PDF Jasper sont gérés par leurs contrôleurs dédiés. Vérifier les migrations 08 à 14 et les appliquer uniquement si elles sont encore nécessaires à la base cible. `mvn clean package` vérifie la compilation Java et les tests métier; les JSP et migrations SQL ne sont pas compilées/exécutées par Maven.
