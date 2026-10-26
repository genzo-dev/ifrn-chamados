INSERT INTO department (name, abbreviation) VALUES ('Tecnologia da Informação', 'COTIC');
INSERT INTO department (name, abbreviation) VALUES ('Manutenção de Infraestrutura', 'MANUT');

INSERT INTO category (name, description, department_id) VALUES ('Internet/Wi-Fi', 'Problemas de conexão de rede', 1);
INSERT INTO category (name, description, department_id) VALUES ('Projetor', 'Problema em projetores de sala', 1);
INSERT INTO category (name, description, department_id) VALUES ('Ar-condicionado', 'Manutenção em ar-condicionado', 2);
INSERT INTO category (name, description, department_id) VALUES ('Mobiliário', 'Cadeiras ou mesas quebradas', 2);

INSERT INTO users (name, email, registration_number, user_type, department_id) VALUES ('João Técnico', 'joao.ti@ifrn.edu.br', '2020101', 'TECNICO', 1);
INSERT INTO users (name, email, registration_number, user_type, department_id) VALUES ('Maria Manutenção', 'maria.manut@ifrn.edu.br', '2020102', 'TECNICO', 2);
INSERT INTO users (name, email, registration_number, user_type, department_id) VALUES ('José Solicitante', 'jose.aluno@escolar.ifrn.edu.br', '2023101', 'SOLICITANTE', NULL);
INSERT INTO users (name, email, registration_number, user_type, department_id) VALUES ('Ana Professora', 'ana.prof@ifrn.edu.br', '2010101', 'SOLICITANTE', NULL);

