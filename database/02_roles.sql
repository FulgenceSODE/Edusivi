-- =====================================================================
-- EduSuivi - 02_roles.sql : roles de l'application (idempotent, sans effet s'ils existent deja)
-- =====================================================================
INSERT INTO role (code_role, libelle) VALUES
    ('ADMIN', 'Administrateur'),
    ('DE', 'Directeur des etudes'),
    ('PROFESSEUR', 'Professeur')
ON CONFLICT (code_role) DO NOTHING;
