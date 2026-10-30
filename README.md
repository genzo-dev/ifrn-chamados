# Projeto API REST — Sistema de Chamados do IFRN

API REST desenvolvida com Spring Boot para o gerenciamento e centralização de chamados de suporte do IFRN (Campus Pau dos Ferros). O sistema permite que solicitantes abram chamados de suporte, acompanhem interações e que técnicos dos setores responsáveis assumam, registrem andamento e encerrem os atendimentos.

## Stack

| Ferramenta                                          | Versão             |
| --------------------------------------------------- | ------------------ |
| [Java](https://docs.oracle.com/en/java/)            | 21                 |
| [Spring Boot](https://spring.io/)                   | 4.1.1              |
| [PostgreSQL](https://www.postgresql.org/docs/17/)   | 17 (imagem Docker) |
| [Flyway](https://documentation.red-gate.com/flyway) | 12.4.0             |
| [Lombok](https://projectlombok.org/)                | —                  |
| [Docker/DockerCompose](https://docs.docker.com/)    | —                  |

## Como Executar

> [!NOTE]
>
> Pré-requisitos:
>
> - Docker
> - Docker Compose

#### 1. Copie as variáveis de ambiente

```bash
cp .envs/.env.dev.example .envs/.env.dev
```

#### 2. Execute o projeto

```bash
make run-dev
```

#### 3. Acesse a API

A API estará disponível e pronta para receber requisições em:

- `http://localhost:8080`

Além disso, a documentação de endpoints via Swagger está disponível em:

- `http://localhost:8080/docs`

### Scripts

| Comando             | Descrição                                                      |
| ------------------- | -------------------------------------------------------------- |
| `make run-dev`      | Inicia servidor de desenvolvimento                             |
| `make run-test`     | Inicia servidor de testes                                      |
| `make down-dev`     | Derruba os containers de desenvolvimento sem limpar os volumes |
| `make down-dev-vol` | Derruba os containers de desenvolvimento e limpa os volumes    |
| `make down-test`    | Derruba os containers de testes                                |

## Estrutura de pastas (`/src`)

```bash
.
├── .envs
│   ├── .env.dev.example              # Template das variáveis de desenvolvimento
│   ├── .env.dev                      # Variáveis locais de desenvolvimento (não versionado)
│   ├── .env.test.example             # Template das variáveis de teste
│   └── .env.test                     # Variáveis locais de teste (não versionado)
├── .mvn
│   └── wrapper
│       └── maven-wrapper.properties  # Configuração do Maven Wrapper
├── Dockerfile                        # Build multi-stage: development, test, build e production
├── docs
│   └── API.md                        # Contrato REST: rotas, códigos HTTP e regras de negócio
├── infra
│   ├── docker-compose.yml            # Serviços base: backend Spring Boot + PostgreSQL 17
│   ├── docker-compose.dev.yml        # Overlay de desenvolvimento (hot reload e volume persistente)
│   └── docker-compose.test.yml       # Overlay de testes (banco efêmero em tmpfs)
├── Makefile                          # Atalhos para orquestrar os ambientes Docker
├── mvnw
├── mvnw.cmd
├── pom.xml                           # Dependências, versão do Java e plugins de build
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── ifrn
│   │   │           └── chamados
│   │   │               ├── category              # Feature: categorias vinculadas a setores
│   │   │               │   ├── CategoryController.java   # Endpoints HTTP da feature
│   │   │               │   ├── Category.java             # Entidade JPA
│   │   │               │   ├── CategoryRepository.java   # Consultas e persistência
│   │   │               │   ├── CategoryService.java      # Regras de negócio
│   │   │               │   └── dto
│   │   │               │       ├── CategoryCreateDTO.java
│   │   │               │       ├── CategoryResponseDTO.java
│   │   │               │       └── CategoryUpdateDTO.java
│   │   │               ├── ChamadosApplication.java      # Bootstrap da aplicação Spring Boot
│   │   │               ├── common                        # Artefatos transversais, sem regra de domínio
│   │   │               │   ├── enums
│   │   │               │   │   ├── Priority.java         # Prioridade do chamado
│   │   │               │   │   ├── Status.java           # Ciclo de vida do chamado
│   │   │               │   │   └── UserType.java         # Perfil do usuário
│   │   │               │   └── utils
│   │   │               │       └── EntityFinder.java     # Busca genérica com 404 padronizado
│   │   │               ├── config
│   │   │               │   └── SwaggerConfig.java        # Metadados OpenAPI/Swagger
│   │   │               ├── department            # Feature: setores responsáveis
│   │   │               │   ├── DepartmentController.java
│   │   │               │   ├── Department.java
│   │   │               │   ├── DepartmentRepository.java
│   │   │               │   ├── DepartmentService.java
│   │   │               │   └── dto
│   │   │               │       ├── DepartmentRequestDTO.java
│   │   │               │       ├── DepartmentResponseDTO.java
│   │   │               │       └── DepartmentUpdateDTO.java
│   │   │               ├── interaction           # Feature: histórico de mensagens do chamado
│   │   │               │   ├── dto
│   │   │               │   │   ├── InteractionCreateDTO.java
│   │   │               │   │   └── InteractionResponseDTO.java
│   │   │               │   ├── InteractionController.java
│   │   │               │   ├── Interaction.java
│   │   │               │   ├── InteractionRepository.java
│   │   │               │   └── InteractionService.java
│   │   │               ├── ticket                # Feature: núcleo do domínio de chamados
│   │   │               │   ├── dto
│   │   │               │   │   ├── TicketAssignDTO.java
│   │   │               │   │   ├── TicketCancelDTO.java
│   │   │               │   │   ├── TicketCreateDTO.java
│   │   │               │   │   ├── TicketDetailResponseDTO.java
│   │   │               │   │   ├── TicketResolveDTO.java
│   │   │               │   │   └── TicketResponseDTO.java
│   │   │               │   ├── TicketController.java
│   │   │               │   ├── Ticket.java
│   │   │               │   ├── TicketRepository.java
│   │   │               │   └── TicketService.java
│   │   │               └── users                 # Feature: usuários (solicitantes e técnicos)
│   │   │                   ├── dto
│   │   │                   │   ├── UserCreateDTO.java
│   │   │                   │   ├── UserResponseDTO.java
│   │   │                   │   └── UserUpdateDTO.java
│   │   │                   ├── UsersController.java
│   │   │                   ├── Users.java
│   │   │                   ├── UsersRepository.java
│   │   │                   └── UsersService.java
│   │   └── resources
│   │       ├── application.properties            # Configuração externalizada via variáveis de ambiente
│   │       ├── db
│   │       │   └── migration
│   │       │       ├── V1__create_tables.sql     # Schema inicial versionado
│   │       │       └── V2__insert_initial_data.sql # Seed de setores, categorias e usuários
│   │       ├── static
│   │       └── templates
│   └── test
│       └── java
│           └── com
│               └── ifrn
│                   └── chamados
│                       └── ChamadosApplicationTests.java # Teste de carregamento do contexto
```

### Observação

A estrutura adota **package by feature** (organização por funcionalidade) em vez de **package by layer** (organização por camada técnica). Cada pacote de domínio — `category`, `department`, `interaction`, `ticket` e `users` — é um módulo coeso que reúne entidade JPA, repositório, serviço, controlador e DTOs. O limite do pacote coincide com o limite da funcionalidade, o que reduz o acoplamento acidental e torna a evolução localizada: uma mudança em chamados tende a permanecer em `ticket/`, sem espalhar arquivos por diretórios globais de `controllers`, `services` e `repositories`.

Essa abordagem favorece:

- **Coesão e encapsulamento**: persistência, validação e transporte de uma feature ficam próximos.
- **Baixo acoplamento**: dependências entre features passam por contratos explícitos, não por vizinhança de pastas.
- **Navegabilidade**: o código é localizado pelo domínio, não pela tecnologia.
- **Escalabilidade do time**: features podem evoluir em paralelo com menor risco de conflito.
- **Testabilidade**: o escopo de cada módulo é claro, facilitando testes unitários e de integração por funcionalidade.

O pacote `common` é reservado a artefatos verdadeiramente transversais e sem regra de negócio específica (enums compartilhados e utilitários genéricos, como `EntityFinder`). Já `config` concentra infraestrutura de aplicação, como a configuração do OpenAPI/Swagger. Essa separação mantém o domínio explícito e evita que detalhes técnicos contaminem as features.

Em `resources/db/migration`, as migrations Flyway mantêm o schema versionado e auditável, enquanto `application.properties` externaliza a configuração por ambiente.

## Endpoints e Exemplos de Requisição

### 1. `/department`

| Rota                        | Método HTTP | Sucesso | Erros        | Funcionalidade                       |
| --------------------------- | ----------- | ------- | ------------ | ------------------------------------ |
| `/department`               | `POST`      | `201`   | `400`        | Criar setor                          |
| `/department`               | `GET`       | `200`   | —            | Listar setores                       |
| `/department/{id}`          | `GET`       | `200`   | `404`        | Buscar um único setor                |
| `/department/{id}`          | `PUT`       | `200`   | `404`        | Atualizar setor                      |
| `/department/{id}`          | `DELETE`    | `200`   | `404`, `409` | Deletar setor                        |
| `/department/{id}/category` | `GET`       | `200`   | `404`        | Listar categorias de um departamento |

### 2. `/category`

| Rota             | Método HTTP | Sucesso | Erros        | Funcionalidade             |
| ---------------- | ----------- | ------- | ------------ | -------------------------- |
| `/category`      | `POST`      | `201`   | `400`, `404` | Criar categoria            |
| `/category`      | `GET`       | `200`   | —            | Listar categorias          |
| `/category/{id}` | `GET`       | `200`   | `404`        | Buscar uma única categoria |
| `/category/{id}` | `PUT`       | `200`   | `404`        | Atualizar categoria        |
| `/category/{id}` | `DELETE`    | `200`   | `404`, `409` | Deletar categoria          |

### 3. `/user`

| Rota                | Método HTTP | Sucesso | Erros        | Funcionalidade                                                    |
| ------------------- | ----------- | ------- | ------------ | ----------------------------------------------------------------- |
| `/user`             | `POST`      | `201`   | `400`, `409` | Cadastrar usuário                                                 |
| `/user`             | `GET`       | `200`   | —            | Listar usuários                                                   |
| `/user/{id}`        | `GET`       | `200`   | `404`        | Buscar um único usuário                                           |
| `/user/{id}`        | `PUT`       | `200`   | `404`, `409` | Atualizar usuário                                                 |
| `/user/{id}/ticket` | `GET`       | `200`   | `404`        | Listar chamados associados ao usuário como solicitante ou técnico |

### 4. Ticket

| Rota                   | Método HTTP | Sucesso | Erros               | Funcionalidade                                   |
| ---------------------- | ----------- | ------- | ------------------- | ------------------------------------------------ |
| `/ticket`              | `POST`      | `201`   | `400`, `404`        | Abrir novo chamado                               |
| `/ticket`              | `GET`       | `200`   | —                   | Listar chamados                                  |
| `/ticket/{id}`         | `GET`       | `200`   | `404`               | Buscar um único chamado                          |
| `/ticket/{id}/assign`  | `PATCH`     | `200`   | `404`, `409`        | Técnico assume chamado                           |
| `/ticket/{id}/resolve` | `PATCH`     | `200`   | `400`, `404`, `409` | Técnico resolve chamado fornececendo uma solução |
| `/ticket/{id}/cancel`  | `PATCH`     | `200`   | `404`, `409`        | Solicitante cancela o chamado aberto             |

### 5. Interaction

| Rota                       | Método HTTP | Sucesso | Erros               | Funcionalidade                                |
| -------------------------- | ----------- | ------- | ------------------- | --------------------------------------------- |
| `/ticket/{id}/interaction` | `POST`      | `201`   | `400`, `404`, `409` | Adicionar uma mensagem/interação no histórico |

> [!NOTE]
>
> Para mais detalhes acesse [a documentação da API](/docs/API.md).

## Estudo e Controle de Versão do Banco de Dados com Flyway

### 1. Como o Flyway registra as versões na tabela `flyway_schema_history`

O Flyway é uma ferramenta de migração de banco de dados baseada em versionamento explícito. Ao ser executado na inicialização da aplicação, ele cria e gerencia automaticamente a tabela `flyway_schema_history` no banco de dados. Cada arquivo de migração localizado na pasta `src/main/resources/db/migration` (ex: `V1__criar_tabelas.sql`) é registrado nessa tabela com dados como:

- `installed_rank`: A ordem de execução da migração.

- `version`: A versão extraída do nome do arquivo (ex: 1, 2).

- `description`: A descrição textual da migração.

- `type`: O tipo de migração (ex: SQL).

- `script`: O nome exato do arquivo executado.

- `checksum`: Um código hash calculado a partir do conteúdo do arquivo SQL.

- `installed_on`: A data e hora em que o script foi aplicado.

- `success`: Booleano indicando se a execução do script foi concluída com sucesso.

### 2. O que acontece quando o checksum de uma migration aplicada muda

Quando uma migração é aplicada, seu `checksum` (hash digital do arquivo) fica gravado na tabela `flyway_schema_history`. Caso o desenvolvedor modifique o conteúdo de um arquivo de migração que já foi executado no banco (ex: editando o `V1__criar_tabelas.sql` em vez de criar uma nova versão V3), na próxima inicialização o Flyway recalculará o hash local e o comparará com o hash armazenado no banco.

Ao identificar a divergência, a inicialização da aplicação é abortada com o erro: `FlywayValidateException: Validate failed: Migration checksum mismatch for migration version X`.

Isso ocorre por segurança: a ferramenta impede que o sistema rode em um banco cujo estado real divirja do histórico declarado de alterações no código. A regra estrita é: migrations já aplicadas nunca devem ser editadas; correções ou evoluções de esquema devem ser feitas em novos arquivos de migração sequenciais (ex: `V3__alterar_tabela.sql`).

### 3. Por que `spring.jpa.hibernate.ddl-auto=update` não é utilizado em produção

A propriedade `spring.jpa.hibernate.ddl-auto=update` faz com que o Hibernate compare o modelo de entidades Java com a estrutura do banco e tente aplicar alterações automaticamente na inicialização. No entanto, ela é fortemente contraindicada para ambientes de produção pelos seguintes motivos:

- **Falta de controle e auditabilidade**: Não há registro histórico do que foi alterado, quando foi alterado nem quem alterou a estrutura do banco.

- **Risco de destruição de dados**: O Hibernate pode reinterpretar tipos de dados, renomear ou remover colunas de forma inesperada durante refatorações de código Java, resultando em perda irreversível de dados sensíveis.

- **Incompatibilidade com locks e concorrência**: Se múltiplas instâncias da aplicação subirem em paralelo num cluster em produção, todas tentarão alterar a estrutura do banco ao mesmo tempo, causando _deadlocks_ ou corrupção no esquema.

- **Erros silenciosos em scripts complexos**: Operações estruturais complexas (como criação de índices parciais, triggers, FKs personalizadas ou dados iniciais) não são suportadas adequadamente pelo gerador automático do Hibernate.

Em ambientes profissionais e de produção, adota-se o bloqueio de criação automática via JPA (`spring.jpa.hibernate.ddl-auto=validate`), delegando a evolução do esquema exclusivamente a ferramentas de migração explícitas e versionadas como o Flyway.

## Documentação

| Documento             | Descrição                                                             |
| --------------------- | --------------------------------------------------------------------- |
| [API.md](docs/API.md) | Tabelas de endpoints com suas respectivas funções e regras de negócio |

## Autor

<div align="center">
  <img src="https://github.com/genzo-dev.png" alt="Gabriel Enzo (genzo-dev)" width="160"/>
</div>

<div align="center">
  <strong>Gabriel Enzo (genzo-dev)</strong>
</div>

<div align="center">
  <sub>Fullstack web developer</sub>
</div>

&nbsp;

<div align="center">
  <a href="https://www.linkedin.com/in/genzo-dev/">💼 LinkedIn</a>
  |
  <a href="https://github.com/genzo-dev">🐙 GitHub</a>
</div>
