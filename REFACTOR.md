# REFACTOR.md — Análise de conformidade e plano de refatoração

> **Escopo deste documento:** análise completa do projeto, comparação com `docs/API.md` e com o enunciado da disciplina, identificação de defeitos funcionais e plano de refatoração.
>
> **Importante:** nenhum arquivo de código, configuração, migration, README ou `docs/API.md` foi alterado. O único arquivo criado por esta análise é este `REFACTOR.md`. As mudanças descritas abaixo são propostas; os commits citados como "sugerido" ainda não existem.

---

## 1. Resumo executivo

O projeto compila (`./mvnw -q -DskipTests compile` termina com sucesso), a aplicação sobe em Docker quando as variáveis de ambiente são injetadas manualmente, o Flyway aplica as duas migrations e o fluxo principal de chamados funciona: criar chamado, assumir, interagir, resolver e cancelar. Isso mostra que existe uma base funcional.

Entretanto, o projeto **não está integralmente de acordo** com o enunciado nem com `docs/API.md`. Os problemas mais graves são:

| Prioridade | Problema | Evidência |
| --- | --- | --- |
| P0 | Rotas divergentes do contrato: `/category`, `/user`, `/chamados/{id}/interacoes` e `/user/{id}/chamados`; o contrato exige `/categories`, `/users`, `/tickets/{id}/interactions` e `/users/{id}/tickets`. | Teste real: `GET /categories` → 404; `GET /category` → 200. `GET /users` → 404; `GET /user` → 200. `POST /tickets/1/interactions` → 404; `POST /chamados/1/interacoes` → 201. |
| P0 | `Status` usa `EM_ANDAMENTO`, mas o enunciado/API usam `EM_ATENDIMENTO`; `Priority` possui `URGENTE`, mas o domínio só permite `BAIXA`, `MEDIA` e `ALTA`. | `Status.java` e `Priority.java`. A resposta de `assign` devolve `"status":"EM_ANDAMENTO"`. |
| P0 | `PUT /categories/{id}` ignora o `departmentId` recebido e busca o departamento usando o **id da categoria**. | `CategoryService.java:64`. Teste real: atualizar categoria 1 para `departmentId=2` devolveu `department_id: 1`. |
| P0 | `DELETE /departments/{id}` só verifica chamados. Se o setor tiver categorias ou técnicos, o banco rejeita a FK e a API devolve 500. | Teste real: `DELETE /departments/2` (tem categorias, sem chamados) → 500. |
| P0 | Não existe `@RestControllerAdvice` nem JSON de erro padronizado com `timestamp`, `status`, `message` e `path`. As respostas de erro reais não trazem `message`. | `GET /departments/999` devolve `{"timestamp","status","error","path"}` sem `message`. |
| P0 | `docker compose up` na raiz não funciona. Não existe compose na raiz; o compose em `infra/` não injeta variáveis no backend; o volume de dados só existe no overlay de dev; o `.env.dev.example` usa host `postgres`, mas o serviço se chama `ifrn-chamados-postgres`. | `docker compose config` mostra o backend sem `environment`/`env_file` no compose base. O README manda copiar o exemplo com `DATABASE_HOST=postgres` e `JPA_DDL_AUTO=update`. |
| P0 | O último commit da `main` é o commit inicial. Todo o desenvolvimento está em `develop`/`refactor/application`. O enunciado diz que será avaliado o último commit da `main`. | `git log`: `main` em `54153e7 initial commit`; `develop` em `1cdc841`. |
| P1 | Paginação não limita `size` em 50. | `GET /tickets?size=100` devolve `"pageSize":100`. |
| P1 | `GET /departments/{id}/categories` não é paginado e devolve array, contrariando a regra de que toda listagem deve ser paginada. | Teste real devolveu `[{...},{...}]`. |
| P1 | Nomes de filtros misturam português e inglês e divergem de `docs/API.md`. | Código aceita `nome`, `tipo`, `setorId`, `prioridade`, `dataInicio`, `dataFim`; o contrato pede `name`, `userType`, `departmentId`, `priority`, `startDate`, `endDate`. |
| P1 | O único teste (`contextLoads`) falha sem banco/ambiente. | `./mvnw test` falha com `Failed to determine a suitable driver class`. |
| P1 | Não existe coleção Postman/Insomnia/`.http` com todas as requisições. | Busca no repositório não encontrou nenhum arquivo desse tipo. |
| P2 | O projeto usa package-by-feature, mas o enunciado pede pacotes separados de `controller`, `service`, `repository` e `model`. | Estrutura atual: `category`, `department`, `interaction`, `ticket`, `users`, cada um com controller/service/repository/entity juntos. |
| P2 | Services não usam `@Transactional`; o carregamento lazy de categorias depende do Open Session In View. | `DepartmentService.findCategoriesByDepartment` retorna `department.getCategories()`. Log do Spring mostra `spring.jpa.open-in-view is enabled by default`. |
| P2 | Validações e unicidade estão incompletas/inconsistentes. | `@Valid` ausente nos updates de departamento/categoria; update de categoria não valida duplicidade; `existsByDescriptionIgnoreCase(dto.name())` usa o campo errado; matrícula tem `@Size(min=7,max=7)` sem justificativa no enunciado. |

---

## 2. Como a análise foi feita

1. Leitura de todos os arquivos Java, `pom.xml`, `application.properties`, migrations, Dockerfiles, compose, Makefile, README e `docs/API.md`.
2. Comparação item a item com o enunciado da disciplina e com a tabela de endpoints de `docs/API.md`.
3. Compilação:

```bash
./mvnw -q -DskipTests compile
# exit 0
```

4. Execução dos testes:

```bash
./mvnw -q test
# falha: Failed to determine a suitable driver class
```

5. Subida real da stack com um override temporário em `/tmp` apenas para injetar as variáveis que o compose base não injeta:

```bash
docker compose \
  --env-file .envs/.env.dev \
  -f infra/docker-compose.yml \
  -f /tmp/chamados-override.yml \
  up -d --build
```

Resultado: Flyway aplicou `V1` e `V2`, a aplicação iniciou na porta 8080 e `GET /` retornou 200. Os containers e o volume temporário foram removidos ao final.

6. Testes manuais com `curl` dos endpoints principais, filtros, paginação, erros e fluxo completo de chamado. Os resultados estão citados na seção 1 e nas seções de cada mudança.

---

## 3. Estrutura atual do projeto

### 3.1. Pontos positivos que devem ser preservados

- Java 21, Spring Boot 4.1.1 e Maven.
- Spring Web MVC, Spring Data JPA, PostgreSQL, Flyway, Validation e Lombok.
- Injeção de dependência por construtor (`@RequiredArgsConstructor`), sem `@Autowired` em atributo.
- Entidades JPA com `@ManyToOne`; `Department` com `@OneToMany`; enums com `@Enumerated(EnumType.STRING)`.
- Regras de negócio de chamados concentradas, em sua maioria, em `TicketService` e `InteractionService`.
- Migrations versionadas e seed com 2 setores, 4 categorias e 4 usuários.
- Dockerfile multi-stage com build Maven e execução em JRE.
- Histórico de commits incremental, com branches e merges por feature.
- README com a explicação solicitada sobre Flyway e `ddl-auto`.
- Swagger/OpenAPI configurado em `/docs`.

### 3.2. Estrutura atual

```text
src/main/java/com/ifrn/chamados
├── category
│   ├── Category.java
│   ├── CategoryController.java
│   ├── CategoryRepository.java
│   ├── CategoryService.java
│   └── dto
├── common
│   ├── enums
│   │   ├── Priority.java
│   │   ├── Status.java
│   │   └── UserType.java
│   └── utils
│       └── EntityFinder.java
├── config
│   └── SwaggerConfig.java
├── department
│   ├── Department.java
│   ├── DepartmentController.java
│   ├── DepartmentRepository.java
│   ├── DepartmentService.java
│   └── dto
├── interaction
│   ├── Interaction.java
│   ├── InteractionController.java
│   ├── InteractionRepository.java
│   ├── InteractionService.java
│   └── dto
├── ticket
│   ├── Ticket.java
│   ├── TicketController.java
│   ├── TicketRepository.java
│   ├── TicketService.java
│   └── dto
├── users
│   ├── Users.java
│   ├── UsersController.java
│   ├── UsersRepository.java
│   ├── UsersService.java
│   └── dto
└── ChamadosApplication.java
```

### 3.3. Decisão de nomenclatura

O enunciado usa nomes em português, mas `docs/API.md` já adota rotas e filtros em inglês. Como o requisito do usuário é usar inglês e boas práticas Java, este plano adota:

- Rotas em inglês, conforme `docs/API.md`.
- Enums, classes, DTOs, pacotes, mensagens de erro e query params em inglês.
- Migrations e nomes de tabelas/colunas já existentes são predominantemente ingleses; a tradução dos valores dos enums será feita com nova migration, sem editar `V1`/`V2`.
- O documento `docs/API.md` precisará ser atualizado em uma etapa documental para refletir os valores dos enums em inglês (`OPEN`, `IN_PROGRESS`, etc.). Nesta tarefa ele não foi alterado.

---

## 4. Matriz de conformidade

Legenda: **OK** = atende; **PARCIAL** = atende em parte; **NÃO** = não atende.

| Requisito | Situação | Evidência / observação |
| --- | --- | --- |
| Java 21 + Spring Boot 4 + Maven | OK | `pom.xml` com `java.version=21` e parent 4.1.1. |
| Spring Web, Spring Data JPA, PostgreSQL | OK | Dependências presentes. |
| Lombok permitido | OK | Usado para getters/setters e construtores. |
| Pacotes separados de controller/service/repository/model | NÃO | Organização é package-by-feature; as camadas estão dentro de cada feature. |
| Controller sem regra de negócio | PARCIAL | As regras estão majoritariamente nos services, mas há mapeamentos e orquestração no controller; não há regra crítica no controller. |
| Service concentra regras e lança exceções próprias | PARCIAL | Regras estão nos services, mas as exceções são `ResponseStatusException` do Spring Web, não exceções próprias de domínio. |
| Injeção por construtor, sem `@Autowired` em atributo | OK | `@RequiredArgsConstructor` e construtor explícito. |
| Repository estende `JpaRepository`; filtros com derived queries ou `@Query` | OK | Implementado. |
| `@ManyToOne`, `@OneToMany` e `EnumType.STRING` | PARCIAL | Enums e `ManyToOne` corretos; `Ticket` não declara `@OneToMany` de interações; `Department.technicians` na verdade contém todos os usuários do setor. |
| Configuração de banco por variáveis de ambiente, sem senha fixa em `application.properties` | PARCIAL | `application.properties` usa placeholders, mas os exemplos de env e o compose base não garantem execução sem ajuste. |
| Flyway versionado em `db/migration` | OK | `V1__create_tables.sql` e `V2__insert_initial_data.sql`. |
| `ddl-auto=validate` e Hibernate não cria tabelas | PARCIAL | `.env.dev` usa `validate`, mas `.env.dev.example` e `.env.test` usam `update`; quem segue o README usa `update`. |
| Migration já aplicada não é editada | OK | Nenhuma evidência de edição; a estratégia deste plano é criar `V3` em diante. |
| Seed mínimo: 2 setores, 4 categorias, 4 usuários | OK | `V2__insert_initial_data.sql`. |
| Dockerfile multi-stage | OK | Stages base, development, test, build e production. |
| `docker compose up` sem passos manuais | NÃO | Não há compose na raiz; o compose base não injeta env; exemplos e host divergem. |
| Volume para dados do PostgreSQL | PARCIAL | Existe apenas no overlay `docker-compose.dev.yml`; o compose base não tem volume. |
| README com descrição, execução, exemplos e estudo de Flyway | PARCIAL | O estudo de Flyway está bom, mas os exemplos de endpoints não correspondem à implementação e o passo `cp .env` ainda é manual. |
| Coleção Postman/Insomnia/`.http` | NÃO | Não encontrada no repositório. |
| Paginação com `page`, `size`, `sort`; padrão 10, máximo 50 | PARCIAL | `page/size/sort` funcionam e o padrão é 10, mas o máximo de 50 não é aplicado. |
| Resposta de página com itens e metadados | OK | `Page` do Spring traz `content`, `size`, `totalElements`, `totalPages` etc. |
| Filtros combináveis; filtro ausente ignorado | PARCIAL | Funcionam, mas os nomes divergem do contrato em vários endpoints. |
| `GET /departments/{id}/categories` | PARCIAL | Existe, mas devolve lista não paginada e não usa repositório com `Pageable`. |
| `GET /users/{id}/tickets` | NÃO | Implementado como `/user/{id}/chamados`. |
| `POST /tickets/{id}/interactions` | NÃO | Implementado como `/chamados/{id}/interacoes`. |
| 400 para dados inválidos | PARCIAL | O status é 400, mas o corpo não contém `message` nem detalhes de validação. |
| 404 para recurso inexistente | PARCIAL | Status correto, corpo sem `message`. |
| 409 para violação de regra | PARCIAL | Status correto em vários casos, corpo sem `message`; alguns casos viram 500 (delete de setor). |
| JSON de erro padronizado com `timestamp`, `status`, `mensagem`, `caminho` via `@RestControllerAdvice` | NÃO | Não existe `@RestControllerAdvice`. |
| Regra 1: chamado nasce ABERTO, dataAbertura do sistema, prioridade MEDIA padrão | PARCIAL | Comportamento existe, mas o enum está em português e há `URGENTE` indevido. |
| Regra 2: apenas SOLICITANTE abre chamado | OK | `TicketService.create` valida e retorna 409. |
| Regra 3: técnico assume ABERTO da categoria do seu setor e status vira EM_ATENDIMENTO | PARCIAL | Validações corretas; status gravado como `EM_ANDAMENTO` em vez de `EM_ATENDIMENTO` (ou equivalente inglês). |
| Regra 4: somente técnico atribuído resolve, com solução obrigatória e dataFechamento | OK | Implementado; `@NotBlank` garante 400 sem solução. |
| Regra 5: somente solicitante cancela, apenas ABERTO | OK | Implementado. |
| Regra 6: RESOLVIDO/CANCELADO não aceitam alterações nem interações | PARCIAL | Interações bloqueadas; não há endpoint de alteração de chamado, mas a regra deve ser centralizada para o futuro. |
| Regra 7: somente solicitante e técnico atribuído registram interações | OK | Implementado. |
| Regra 8: não excluir setor/categoria com chamados | PARCIAL | Categoria OK; setor retorna 500 quando há categorias/técnicos, e 409 quando há chamados. |
| Nomenclatura em inglês | NÃO | Rotas e filtros principais estão parcialmente em inglês; enums, alguns parâmetros, DTO (`department_id`), classes (`Users`) e mensagens estão em português. |
| Testes automatizados | NÃO | Apenas `contextLoads`, que falha sem banco/ambiente. |
| Último commit na `main` | NÃO | `main` está no commit inicial. |

---

## 5. Mudanças propostas

Cada mudança abaixo segue o formato: problema, mudança, motivo, impacto, teste e commits relacionados.

### M01 — Alinhar as rotas HTTP ao contrato

**Severidade:** P0

**Problema:** As rotas implementadas não são as rotas documentadas em `docs/API.md`:

| Contrato | Implementação atual |
| --- | --- |
| `/categories` | `/category` |
| `/users` | `/user` |
| `/users/{id}/tickets` | `/user/{id}/chamados` |
| `/tickets/{id}/interactions` | `/chamados/{id}/interacoes` |
| `PUT /departments/{id}` | `PATCH /departments/{id}` |

**Mudança proposta:**

- `CategoryController`: `@RequestMapping("/categories")`.
- `UsersController`: renomear para `UserController` e usar `@RequestMapping("/users")`.
- `InteractionController`: `@RequestMapping("/tickets")` e `@PostMapping("/{id}/interactions")`.
- `UsersController.getUserTickets`: `@GetMapping("/{id}/tickets")`.
- `DepartmentController.update`: trocar `@PatchMapping` por `@PutMapping`.
- Padronizar todos os mapeamentos com barra inicial (`@GetMapping("/{id}")`).
- Atualizar README e a coleção de requisições.

**Motivo:** O contrato REST é a interface pública da API; qualquer divergência quebra clientes, coleção de testes e avaliação.

**Impacto:** Mudança incompatível para clientes que usam as rotas antigas. Como o projeto ainda não foi entregue, o custo é baixo. `docs/API.md` já está correto para as rotas.

**Como testar:**

```bash
curl -i http://localhost:8080/categories          # 200
curl -i http://localhost:8080/users               # 200
curl -i http://localhost:8080/users/3/tickets     # 200
curl -i -X POST http://localhost:8080/tickets/1/interactions \
  -H 'Content-Type: application/json' \
  -d '{"text":"Teste","authorId":3}'              # 201
curl -i -X PUT http://localhost:8080/departments/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"COTIC","abbreviation":"COTIC"}'    # 200
```

**Arquivos:** `CategoryController.java`, `UsersController.java`, `InteractionController.java`, `DepartmentController.java`, README, coleção Postman/Insomnia/`.http`.

**Commits existentes relacionados:** `8a9c18f` (criação de categoria), `6d39b02` (busca de categoria), `e62d0c9` (update de categoria), `1811af8` (delete de categoria), `403ee1e` (categorias por setor), `5ade3fb` (criação de usuário), `177976d` (busca de usuário), `5c6ce2a` (update de usuário), `839b7b0` (chamados por usuário), `e7dabe3` (interações), `2b10653` (CRUD de setores), `fa12355` (update de setor).

**Commit sugerido:** `refactor(api): align routes with API contract`.

---

### M02 — Traduzir enums e corrigir o domínio de status/prioridade

**Severidade:** P0

**Problema:** Os enums estão em português e não representam exatamente o domínio:

- `Status`: `ABERTO`, `EM_ANDAMENTO`, `RESOLVIDO`, `CANCELADO`. O enunciado usa `EM_ATENDIMENTO`.
- `Priority`: possui `URGENTE`, mas o domínio só permite `BAIXA`, `MEDIA`, `ALTA`.
- `UserType`: `SOLICITANTE`, `TECNICO`.

**Mudança proposta:**

```java
public enum Status {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CANCELED
}

public enum Priority {
    LOW,
    MEDIUM,
    HIGH
}

public enum UserType {
    REQUESTER,
    TECHNICIAN
}
```

- Criar `V3__translate_domain_enums.sql` para converter os valores já gravados:

```sql
UPDATE ticket
SET status = CASE status
    WHEN 'ABERTO' THEN 'OPEN'
    WHEN 'EM_ANDAMENTO' THEN 'IN_PROGRESS'
    WHEN 'RESOLVIDO' THEN 'RESOLVED'
    WHEN 'CANCELADO' THEN 'CANCELED'
    ELSE status
END;

UPDATE ticket
SET priority = CASE priority
    WHEN 'BAIXA' THEN 'LOW'
    WHEN 'MEDIA' THEN 'MEDIUM'
    WHEN 'ALTA' THEN 'HIGH'
    WHEN 'URGENTE' THEN 'HIGH'
    ELSE priority
END;

UPDATE users
SET user_type = CASE user_type
    WHEN 'SOLICITANTE' THEN 'REQUESTER'
    WHEN 'TECNICO' THEN 'TECHNICIAN'
    ELSE user_type
END;
```

- Atualizar `TicketService.create` para `Status.OPEN` e `Priority.MEDIUM`.
- Atualizar `TicketService.assignTicket` para `Status.IN_PROGRESS`.
- Atualizar `TicketService.resolveTicket` e `cancelTicket` para `RESOLVED`/`CANCELED`.
- Atualizar `InteractionService` e mensagens de erro.
- Centralizar as transições em métodos da entidade `Ticket` (por exemplo `assignTo`, `resolve`, `cancel`) ou em uma política de status, chamada pelo service. Isso evita que a regra fique espalhada em `if`s.

**Motivo:** O domínio deve refletir exatamente os valores permitidos. `URGENTE` não existe no enunciado e `EM_ANDAMENTO` diverge de `EM_ATENDIMENTO`. A tradução atende ao pedido de nomenclatura em inglês.

**Impacto:**

- Mudança incompatível no JSON (`"status":"OPEN"` em vez de `"ABERTO"`).
- Dados existentes precisam da migration `V3`; não editar `V1`/`V2`.
- Filtros e documentação devem usar os novos valores.
- Registros `URGENTE` serão convertidos para `HIGH` para não perder dados.

**Como testar:**

```bash
# 1. Subir com banco antigo em V2 e verificar que V3 é aplicada
docker compose up --build -d
docker compose exec postgres psql -U "$DATABASE_USER" -d "$DATABASE_DB" \
  -c "SELECT version, success FROM flyway_schema_history ORDER BY installed_rank;"
docker compose exec postgres psql -U "$DATABASE_USER" -d "$DATABASE_DB" \
  -c "SELECT DISTINCT status, priority FROM ticket;"
docker compose exec postgres psql -U "$DATABASE_USER" -d "$DATABASE_DB" \
  -c "SELECT DISTINCT user_type FROM users;"

# 2. Criar chamado e verificar OPEN/MEDIUM
curl -s -X POST http://localhost:8080/tickets \
  -H 'Content-Type: application/json' \
  -d '{"title":"Teste","description":"Teste","location":"Sala 1","requesterId":3,"categoryId":1}'

# 3. Valor antigo deve ser rejeitado com 400
curl -i 'http://localhost:8080/tickets?status=ABERTO'
# 4. Valor novo deve ser aceito
curl -i 'http://localhost:8080/tickets?status=OPEN'
```

**Arquivos:** `Status.java`, `Priority.java`, `UserType.java`, `TicketService.java`, `InteractionService.java`, `V3__translate_domain_enums.sql`, `docs/API.md`, README, coleção.

**Commits existentes relacionados:** `ed80468` (entidades e enums), `b6f0270` (migrations), `74eb909` (criação de chamado), `bd6311c` (filtros), `3ef70d5` (transições de status), `e7dabe3` (interações).

**Commit sugerido:** `refactor(domain)!: translate ticket enums to English`.

---

### M03 — Corrigir o update de categoria e a validação de descrição

**Severidade:** P0

**Problema 1:** Em `CategoryService.updateById`, o departamento é buscado com o id da categoria:

```java
Department department = EntityFinder.findByIdOrThrow(departmentRepository, id, "Departamento");
```

O correto é usar `dto.departmentId()`.

**Problema 2:** Em `CategoryService.create`, a checagem de descrição usa o nome:

```java
if (categoryRepository.existsByDescriptionIgnoreCase(dto.name())) {
```

Isso pode gerar 409 indevido e não valida a descrição recebida.

**Mudança proposta:**

```java
if (dto.departmentId() != null) {
    Department department = EntityFinder.findByIdOrThrow(
        departmentRepository,
        dto.departmentId(),
        "Department"
    );
    category.setDepartment(department);
}
```

E, na criação, decidir uma das duas opções:

1. Remover a unicidade de descrição, pois o enunciado não exige; ou
2. Corrigir para `existsByDescriptionIgnoreCase(dto.description())` e documentar a regra extra.

Recomendação: remover a unicidade de descrição e manter apenas validação de obrigatoriedade, alinhando ao enunciado. Se a regra for mantida, aplicar também no update.

**Motivo:** O bug atual impede mover uma categoria para outro setor e pode causar 404/409 incorretos. É um defeito funcional direto.

**Impacto:** Corrige o comportamento de atualização. Se a unicidade de descrição for removida, categorias com descrições iguais passam a ser aceitas; isso é aderente ao enunciado, que não exige unicidade.

**Como testar:**

```bash
# Pré-condição: categoria 1 pertence ao setor 1
curl -i -X PUT http://localhost:8080/categories/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Internet/Wi-Fi","description":"Problemas de conexão de rede","departmentId":2}'
# Esperado: 200 e departmentId=2

curl -s http://localhost:8080/categories/1
# Esperado: "departmentId":2

# Departamento inexistente deve dar 404
curl -i -X PUT http://localhost:8080/categories/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Internet/Wi-Fi","description":"Problemas de conexão de rede","departmentId":999}'
# Esperado: 404
```

**Arquivos:** `CategoryService.java`, `CategoryController.java` (adicionar `@Valid` no update), `CategoryUpdateDTO.java`.

**Commits existentes relacionados:** `e62d0c9` (update de categoria), `c0343f2` (refatoração de categoria/setor).

**Commit sugerido:** `fix(category): use departmentId when moving category`.

---

### M04 — Corrigir exclusão de setor e categoria

**Severidade:** P0

**Problema:** `DepartmentService.deleteById` só verifica se existe chamado vinculado:

```java
if (ticketRepository.existsByCategoryDepartmentId(id)) {
    throw new ResponseStatusException(HttpStatus.CONFLICT, ...);
}
```

Se o setor tiver categorias ou técnicos, mas nenhum chamado, o `DELETE` causa violação de FK e retorna 500. Teste real: `DELETE /departments/2` → 500.

**Mudança proposta:**

- Continuar verificando chamados vinculados (Regra 8).
- Verificar também se existem categorias vinculadas ao setor.
- Verificar também se existem usuários/técnicos vinculados ao setor.
- Se qualquer vínculo existir, retornar 409 com mensagem clara.
- Opcionalmente, definir política de exclusão em cascata apenas se o domínio permitir; a recomendação é bloquear, para não apagar histórico/categorias sem intenção.
- Para categoria, manter a checagem de chamados existente.
- Adicionar métodos de repositório:

```java
boolean existsByDepartmentId(Long departmentId); // CategoryRepository
boolean existsByDepartmentId(Long departmentId); // UserRepository
```

- Tratar `DataIntegrityViolationException` no handler global como 409, como rede de segurança.

**Motivo:** A API deve responder de forma previsível (409) e não vazar erro 500 do banco. A regra 8 fala de chamados, mas a integridade referencial exige tratar categorias e usuários.

**Impacto:** `DELETE /departments/{id}` passa a retornar 409 em mais casos; clientes precisam remover/mover categorias e usuários antes. Nenhum dado é apagado em cascata.

**Como testar:**

```bash
# Setor 2 tem categorias e nenhum chamado -> deve dar 409, não 500
curl -i -X DELETE http://localhost:8080/departments/2

# Setor sem categorias/usuários/chamados -> 200
curl -i -X POST http://localhost:8080/departments \
  -H 'Content-Type: application/json' \
  -d '{"name":"Setor Temporário","abbreviation":"TMP"}'
# usar o id retornado
curl -i -X DELETE http://localhost:8080/departments/{id}

# Categoria com chamado -> 409
curl -i -X DELETE http://localhost:8080/categories/1
```

**Arquivos:** `DepartmentService.java`, `DepartmentRepository.java`, `CategoryRepository.java`, `UsersRepository.java`, `GlobalExceptionHandler.java` (quando criado), `V4` se forem adicionadas constraints explícitas.

**Commits existentes relacionados:** `c0343f2` (checagens de exclusão), `1811af8` (delete de categoria), `2b10653` (CRUD de setores).

**Commit sugerido:** `fix(department): block deletion with related categories or users`.

---

### M05 — Criar exceções de domínio e `@RestControllerAdvice`

**Severidade:** P0

**Problema:** Não há tratamento global de erros. As respostas reais de 404/409/400 não possuem `message`, e alguns erros de banco viram 500. O enunciado exige JSON com `timestamp`, `status`, `mensagem` e `caminho`, gerado por `@RestControllerAdvice`.

**Mudança proposta:**

- Criar `exception/ApiError.java`:

```java
public record ApiError(
    Instant timestamp,
    int status,
    String message,
    String path
) {}
```

- Criar exceções próprias:

```java
public class ResourceNotFoundException extends RuntimeException { ... }
public class BusinessRuleException extends RuntimeException { ... }
```

- Criar `GlobalExceptionHandler` com `@RestControllerAdvice` e handlers para:
  - `ResourceNotFoundException` → 404.
  - `BusinessRuleException` → 409.
  - `MethodArgumentNotValidException` → 400, com lista de campos inválidos.
  - `ConstraintViolationException` → 400.
  - `MethodArgumentTypeMismatchException` → 400 (enum/query param inválido).
  - `HttpMessageNotReadableException` → 400 (JSON malformado).
  - `DataIntegrityViolationException` → 409 (unicidade/FK).
  - `Exception` → 500, sem expor stack trace.
- Substituir `ResponseStatusException` em services e em `EntityFinder` por `ResourceNotFoundException`/`BusinessRuleException`.
- Garantir `message` não vazio em todas as respostas de erro.
- Opcional: incluir `errors` (lista de {field, message}) apenas em erros de validação, mantendo os quatro campos obrigatórios.

**Motivo:** Padronização exigida pelo enunciado, melhor experiência para o cliente, segurança (não expor detalhes internos) e testabilidade.

**Impacto:** O corpo de erro muda. Clientes que dependiam do formato padrão do Spring (`error`) precisam se adaptar. O status HTTP continua o mesmo na maioria dos casos; erros de FK passam de 500 para 409.

**Como testar:**

```bash
curl -s http://localhost:8080/departments/999 | jq
# Esperado: {"timestamp":"...","status":404,"message":"Department 999 not found","path":"/departments/999"}

curl -s -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"","email":"invalido","registrationNumber":"1","userType":null}' | jq
# Esperado: 400 com message e, opcionalmente, errors por campo

curl -s -X POST http://localhost:8080/users \
  -H 'Content-Type: application/json' \
  -d '{"name":"X","email":"joao.ti@ifrn.edu.br","registrationNumber":"9999999","userType":"REQUESTER"}' | jq
# Esperado: 409 com message

curl -i 'http://localhost:8080/tickets?status=INVALIDO'
# Esperado: 400 com message
```

**Arquivos:** `exception/ApiError.java`, `exception/ResourceNotFoundException.java`, `exception/BusinessRuleException.java`, `exception/GlobalExceptionHandler.java`, `EntityFinder.java`, todos os services, controllers que capturam exceções (não devem capturar).

**Commits existentes relacionados:** `a5f8b4e` (EntityFinder), commits de cada feature que introduziram `ResponseStatusException`.

**Commit sugerido:** `feat(exception): add domain exceptions and global error handler`.

---

### M06 — Corrigir paginação, tamanho máximo e filtros

**Severidade:** P1

**Problema 1:** O tamanho máximo de 50 não é aplicado. `GET /tickets?size=100` devolve `pageSize: 100`.

**Problema 2:** `GET /departments/{id}/categories` devolve `List<Category>` e um array JSON, sem paginação.

**Problema 3:** Os nomes dos filtros divergem do contrato:

| Endpoint | Implementação atual | Contrato |
| --- | --- | --- |
| `GET /departments` | `nome` | `name` |
| `GET /users` | `nome`, `tipo`, `setorId` | `name`, `userType`, `departmentId` |
| `GET /tickets` | `prioridade`, `setorId`, `categoriaId`, `solicitanteId`, `tecnicoId`, `dataInicio`, `dataFim` | `priority`, `departmentId`, `categoryId`, `requesterId`, `technicianId`, `startDate`, `endDate` |

**Mudança proposta:**

- Em `application.properties`:

```properties
spring.data.web.pageable.default-page-size=10
spring.data.web.pageable.max-page-size=50
```

- Renomear os `@RequestParam` para os nomes do contrato.
- Alterar `DepartmentController.findCategoriesByDepartment` para aceitar `Pageable` e retornar `Page<CategoryResponseDTO>`.
- Adicionar `CategoryRepository.findByDepartmentId(Long departmentId, Pageable pageable)`.
- Ordenar interações por data no detalhe do chamado (`findByTicketIdOrderByDateTimeAsc`).
- Opcional recomendado: criar um `PageResponseDTO<T>` estável com `content`, `page`, `size`, `totalElements`, `totalPages`, `first`, `last`, em vez de serializar `Page` diretamente. Isso desacopla o contrato da implementação do Spring Data.
- Validar `sort` contra uma lista de campos permitidos para evitar 500 em propriedades inexistentes.

**Motivo:** O enunciado exige paginação em toda listagem, limite máximo de 50 e filtros com nomes específicos. A resposta em array quebra o contrato de metadados.

**Impacto:**

- `GET /departments/{id}/categories` muda de array para objeto paginado (breaking change).
- `size > 50` passa a retornar no máximo 50 itens.
- Filtros antigos deixam de funcionar; clientes e coleção devem ser atualizados.

**Como testar:**

```bash
# Limite máximo
curl -s 'http://localhost:8080/tickets?size=100' | jq '.pageable.pageSize'
# Esperado: 50

# Metadados
curl -s 'http://localhost:8080/tickets?page=0&size=10&sort=openingDate,desc' | jq
# Esperado: content, number, size, totalElements, totalPages

# Categorias paginadas
curl -s 'http://localhost:8080/departments/1/categories?page=0&size=10' | jq
# Esperado: objeto de página, não array

# Filtros combinados
curl -s 'http://localhost:8080/tickets?status=OPEN&priority=HIGH&departmentId=1&page=0&size=10&sort=openingDate,desc' | jq

# Filtro antigo deve ser ignorado ou rejeitado conforme decisão do contrato
curl -s 'http://localhost:8080/tickets?prioridade=ALTA' | jq
```

**Arquivos:** `application.properties`, `DepartmentController.java`, `CategoryRepository.java`, `TicketController.java`, `UsersController.java`, `DepartmentController.java`, `TicketRepository.java`, `InteractionRepository.java`, DTOs de página (se criados).

**Commits existentes relacionados:** `1e8ab87` (paginação de categorias), `bd6311c` (filtros de chamados), `cf233cf` (filtros de usuários), `403ee1e` (categorias por setor), `839b7b0` (chamados por usuário).

**Commit sugerido:** `feat(pagination): enforce page size limits and paginate department categories`.

---

### M07 — Reorganizar os pacotes por camada

**Severidade:** P2 (necessário para aderência literal ao enunciado)

**Problema:** O projeto adota package-by-feature. O enunciado pede "pacotes separados para controller, service, repository e model (entidades)". Embora package-by-feature seja uma prática válida, a estrutura atual não atende literalmente ao requisito.

**Mudança proposta:** Reestruturar para:

```text
com.ifrn.chamados
├── ChamadosApplication.java
├── config
│   └── OpenApiConfig.java
├── controller
│   ├── DepartmentController.java
│   ├── CategoryController.java
│   ├── UserController.java
│   ├── TicketController.java
│   ├── InteractionController.java
│   └── HomeController.java
├── dto
│   ├── department
│   ├── category
│   ├── user
│   ├── ticket
│   └── interaction
├── exception
│   ├── ApiError.java
│   ├── ResourceNotFoundException.java
│   ├── BusinessRuleException.java
│   └── GlobalExceptionHandler.java
├── model
│   ├── Department.java
│   ├── Category.java
│   ├── User.java
│   ├── Ticket.java
│   ├── Interaction.java
│   └── enums
│       ├── Priority.java
│       ├── Status.java
│       └── UserType.java
├── repository
│   ├── DepartmentRepository.java
│   ├── CategoryRepository.java
│   ├── UserRepository.java
│   ├── TicketRepository.java
│   └── InteractionRepository.java
└── service
    ├── DepartmentService.java
    ├── CategoryService.java
    ├── UserService.java
    ├── TicketService.java
    └── InteractionService.java
```

Alternativa: manter package-by-feature, mas com subpacotes `controller`, `service`, `repository`, `model`, `dto` dentro de cada feature. Essa alternativa também é defensável tecnicamente, mas deve ser alinhada com o professor para evitar perda de ponto.

**Motivo:** Aderência literal ao requisito de arquitetura em camadas e facilidade de correção por um avaliador que procure os pacotes pedidos.

**Impacto:** Mudança mecânica e grande em número de arquivos, sem alteração de comportamento. Imports, testes e referências precisam ser atualizados. O histórico Git pode ficar mais difícil de ler; por isso deve ser um commit isolado e sem mudanças funcionais.

**Como testar:**

```bash
./mvnw -q -DskipTests compile
./mvnw test
# Deve continuar verde, com comportamento idêntico
```

**Arquivos:** Todos os arquivos Java de `src/main/java` e `src/test/java`.

**Commits existentes relacionados:** `9ceb48f` (estrutura inicial de módulos), `ed80468` (entidades), commits de cada feature.

**Commit sugerido:** `refactor(layers): move code to controller, service, repository and model packages`.

---

### M08 — Padronizar validação, unicidade e semântica de PUT

**Severidade:** P1

**Problema:** Existem inconsistências:

- `DepartmentController.update` e `CategoryController.update` não usam `@Valid`.
- `DepartmentUpdateDTO` não tem validações; `CategoryUpdateDTO` tem, mas não é validado.
- `UserUpdateDTO` não permite alterar `registrationNumber`.
- `DepartmentService.updateById` não verifica duplicidade de nome/sigla; pode gerar 500 por constraint de sigla.
- `UsersService.create` valida matrícula única apenas na aplicação; não há constraint no banco.
- `UserCreateDTO.registrationNumber` exige exatamente 7 caracteres, regra não prevista no enunciado.
- `UserService` usa e-mail case-sensitive; e-mails normalmente devem ser normalizados.
- `DepartmentService` exige nome único, mas o banco não tem constraint e o enunciado só exige sigla única.
- `CategoryService` tem a checagem de descrição com o campo errado (ver M03).

**Mudança proposta:**

- Adicionar `@Valid` nos updates de departamento e categoria.
- Definir semântica de `PUT`: representação completa. Se for completo, `DepartmentUpdateDTO` e `CategoryUpdateDTO` devem ter `@NotBlank`/`@NotNull` e o service deve substituir todos os campos. Se for parcial, trocar para `PATCH` e documentar. Recomendação: manter `PUT` completo para aderir ao contrato.
- Incluir `registrationNumber` em `UserUpdateDTO` se a matrícula for editável; validar unicidade com `existsByRegistrationNumberAndIdNot`.
- Remover o `@Size(min=7,max=7)` ou justificar a regra no README. Se mantida, documentar e testar.
- Normalizar e-mail para lowercase e usar comparação case-insensitive.
- Decidir e documentar a unicidade de nome de setor e descrição de categoria. Recomendação: seguir o enunciado (somente sigla de setor e e-mail de usuário únicos) e remover regras extras não documentadas. Se mantiver regras extras, criar constraints no banco e tratar update.
- Adicionar constraints de banco via nova migration quando a regra for mantida (por exemplo, `UNIQUE` em `users.registration_number`, se a unicidade for desejada).
- Adicionar `@Size` nos campos com limite de coluna (`abbreviation` 50, `title` 255, etc.) para evitar 500 por truncamento.

**Motivo:** Evitar 400/409/500 inconsistentes, alinhar validação de entrada ao contrato e garantir invariantes também no banco (não só na aplicação).

**Impacto:** Requisições que antes passavam podem ser rejeitadas com 400; regras extras removidas podem permitir dados que antes eram bloqueados. Atualizar documentação e testes.

**Como testar:**

```bash
# Update sem campos obrigatórios -> 400
curl -i -X PUT http://localhost:8080/departments/1 \
  -H 'Content-Type: application/json' -d '{}'

# Update de categoria sem descrição -> 400
curl -i -X PUT http://localhost:8080/categories/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Internet","departmentId":1}'

# Sigla duplicada no update -> 409
curl -i -X PUT http://localhost:8080/departments/1 \
  -H 'Content-Type: application/json' \
  -d '{"name":"COTIC","abbreviation":"MANUT"}'

# E-mail duplicado no update -> 409
curl -i -X PUT http://localhost:8080/users/3 \
  -H 'Content-Type: application/json' \
  -d '{"name":"José","email":"joao.ti@ifrn.edu.br","userType":"REQUESTER"}'
```

**Arquivos:** DTOs de update, controllers, services, `V4`/`V5` para constraints, `UserRepository`, `DepartmentRepository`, `CategoryRepository`.

**Commits existentes relacionados:** `5ade3fb` (criação de usuário), `5c6ce2a` (update de usuário), `8a9c18f` (criação de categoria), `e62d0c9` (update de categoria), `2b10653` (CRUD de setores), `fa12355` (update de setor).

**Commit sugerido:** `fix(validation): align DTO validation and uniqueness rules`.

---

### M09 — Transações, Open Session In View e consultas

**Severidade:** P2

**Problema:**

- Services não usam `@Transactional`.
- `DepartmentService.findCategoriesByDepartment` retorna uma coleção lazy e o controller acessa os dados fora do service; isso depende de `spring.jpa.open-in-view=true`.
- As listagens de chamados podem gerar N+1 ao acessar `requester`, `category` e `technician` em cada DTO.
- `InteractionRepository.findByTicketId` não define ordenação.
- A entidade `Interaction` usa o campo `user`, enquanto o DTO usa `author`; a entidade `Department.technicians` contém todos os usuários, não só técnicos.

**Mudança proposta:**

- Anotar writes com `@Transactional` e reads com `@Transactional(readOnly = true)`.
- Configurar `spring.jpa.open-in-view=false`.
- Criar consultas com `@EntityGraph` ou `JOIN FETCH` para listagem/detalhe de chamados e categorias por setor.
- Ordenar interações por data: `findByTicketIdOrderByDateTimeAsc`.
- Renomear `Interaction.user` para `Interaction.author` (coluna `author_id` já existe) e `Department.technicians` para `Department.users` ou `Department.technicians` apenas se filtrar por tipo.
- Opcional: adicionar `@OneToMany(mappedBy = "ticket")` em `Ticket` para interações, com `@EntityGraph` no detalhe.
- Adicionar índices para filtros:

```sql
CREATE INDEX idx_ticket_status ON ticket(status);
CREATE INDEX idx_ticket_priority ON ticket(priority);
CREATE INDEX idx_ticket_opening_date ON ticket(opening_date);
CREATE INDEX idx_ticket_requester ON ticket(requester_id);
CREATE INDEX idx_ticket_technician ON ticket(technician_id);
CREATE INDEX idx_ticket_category ON ticket(category_id);
CREATE INDEX idx_category_department ON category(department_id);
CREATE INDEX idx_users_department ON users(department_id);
CREATE INDEX idx_interaction_ticket ON interaction(ticket_id);
```

**Motivo:** Garantir atomicidade das transições, evitar `LazyInitializationException` ao desligar OSIV, melhorar performance e deixar o modelo semanticamente correto.

**Impacto:** Se `open-in-view=false` for ativado sem ajustar as consultas, endpoints podem falhar com lazy loading; por isso a mudança deve ser acompanhada de fetch joins e testes de integração. Índices impactam pouco escrita e melhoram leitura.

**Como testar:**

```bash
# Fluxo completo após desligar OSIV
curl -s http://localhost:8080/tickets?size=10 | jq
curl -s http://localhost:8080/tickets/1 | jq
curl -s http://localhost:8080/departments/1/categories | jq

# Verificar ordenação das interações no detalhe
curl -s http://localhost:8080/tickets/1 | jq '.interactions[].dateTime'
# Esperado: datas em ordem crescente

# Rodar testes de integração com Testcontainers
./mvnw test
```

**Arquivos:** services, repositories, entidades `Ticket`, `Interaction`, `Department`, `application.properties`, nova migration de índices (`V4` ou `V5`).

**Commits existentes relacionados:** commits de `TicketService`, `InteractionService`, `DepartmentService`, `CategoryService`.

**Commit sugerido:** `refactor(persistence): add transactions, fetch strategies and indexes`.

---

### M10 — Tornar o Docker realmente reprodutível

**Severidade:** P0

**Problema:** O requisito é "clonar, rodar `docker compose up` e usar a API sem configurar nada além do Docker". Hoje:

- Não existe `docker-compose.yml`/`compose.yaml` na raiz.
- O compose em `infra/` não injeta `environment`/`env_file` no backend no perfil base.
- O volume do PostgreSQL só existe no overlay de dev.
- O `.env.dev.example` usa `DATABASE_HOST=postgres`, mas o serviço se chama `ifrn-chamados-postgres`.
- O `.env.dev.example` usa `JPA_DDL_AUTO=update`, contrariando o requisito de `validate`.
- O README exige `cp .envs/.env.dev.example .envs/.env.dev` e `make run-dev`, ou seja, passo manual.

**Mudança proposta:** Criar um `compose.yaml` (ou `docker-compose.yml`) na raiz, autossuficiente:

```yaml
name: ifrn-chamados

services:
  api:
    build:
      context: .
    ports:
      - "8080:8080"
    environment:
      ENVIRONMENT: docker
      DATABASE_TYPE: postgresql
      DATABASE_HOST: postgres
      DATABASE_PORT: 5432
      DATABASE_DB: ${POSTGRES_DB:-ifrn_chamados}
      DATABASE_USER: ${POSTGRES_USER:-ifrn_chamados}
      DATABASE_PASSWORD: ${POSTGRES_PASSWORD:-ifrn_chamados_dev}
      JPA_DDL_AUTO: validate
      JPA_SHOW_SQL: "false"
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped

  postgres:
    image: postgres:17
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-ifrn_chamados}
      POSTGRES_USER: ${POSTGRES_USER:-ifrn_chamados}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-ifrn_chamados_dev}
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-ifrn_chamados} -d ${POSTGRES_DB:-ifrn_chamados}"]
      interval: 5s
      timeout: 5s
      retries: 10
    restart: unless-stopped

volumes:
  pgdata:
```

- Os valores padrão são apenas para desenvolvimento local; produção deve sobrescrever via variáveis de ambiente/secrets.
- Não expor a porta do PostgreSQL por padrão, a menos que seja necessário para debug.
- Atualizar `.env.dev.example` para `DATABASE_HOST=postgres` se o compose da raiz for usado, e `JPA_DDL_AUTO=validate`.
- Atualizar `.env.test.example` para `validate` se os testes deixarem de usar `update`.
- Ajustar o Dockerfile para rodar como usuário não-root (opcional, mas recomendado).
- Garantir que o build de produção use o stage `production`.
- Manter os overlays de dev/test se forem úteis, mas o caminho principal deve ser o compose da raiz.
- README: trocar o passo manual por `docker compose up --build` e documentar `docker compose down`/`down -v`.

**Motivo:** Atender ao requisito de entrega e evitar falhas de rede/senha/volume em uma clonagem limpa.

**Impacto:** `docker compose up` passa a funcionar sem `.env` manual. O volume persistente passa a existir no fluxo padrão. A porta do banco deixa de ser exposta publicamente (se aplicado). Credenciais locais padrão passam a existir no compose; isso é aceitável para desenvolvimento, mas deve ser documentado e sobrescrito em produção.

**Como testar:**

```bash
# Em uma cópia limpa do repositório
docker compose up --build -d
curl -i http://localhost:8080/
curl -i http://localhost:8080/departments
curl -i http://localhost:8080/docs

# Persistência
docker compose down
docker compose up -d
curl -s http://localhost:8080/departments | jq '.totalElements'
# O total deve permanecer após o down/up sem -v

# Limpeza
docker compose down -v
```

**Arquivos:** novo `compose.yaml`/`docker-compose.yml` na raiz, `infra/docker-compose*.yml`, `.envs/.env.*.example`, `Dockerfile`, README, `.dockerignore` (corrigir escapes `\*.iml`, `.env.\*`, `\*.log`).

**Commits existentes relacionados:** `e36151a` (docker inicial), `046a489` (hot reload), `b7be5ca` (ambiente de teste), `1cdc841` (README).

**Commit sugerido:** `chore(docker): make docker compose up self-contained`.

---

### M11 — Testes automatizados

**Severidade:** P1

**Problema:** O único teste é `contextLoads`, e ele falha sem banco/ambiente:

```text
Failed to determine a suitable driver class
```

Não há testes das regras de negócio, dos códigos HTTP, do handler de erro nem da paginação.

**Mudança proposta:**

- Adicionar Testcontainers PostgreSQL e `spring-boot-testcontainers` no `pom.xml`.
- Substituir `contextLoads` por uma configuração de teste com Testcontainers:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ChamadosApplicationTests {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @Test
    void contextLoads() {}
}
```

- Criar testes de integração com `MockMvc`/`RestTestClient` para:
  - CRUD de setores, categorias e usuários.
  - Unicidade de e-mail (409).
  - Abertura de chamado: status OPEN, prioridade MEDIUM, openingDate preenchida.
  - Apenas requester abre chamado.
  - Assumir: ABERTO, técnico do setor, status IN_PROGRESS.
  - Assumir chamado de outro setor → 409.
  - Resolver: somente técnico atribuído, solução obrigatória, closingDate, status RESOLVED.
  - Cancelar: somente requester, somente OPEN, status CANCELED.
  - Interações: autorizados, bloqueio em RESOLVED/CANCELED.
  - Exclusão de setor/categoria com chamados → 409; sem vínculos → 200.
  - Paginação: default 10, max 50, metadados.
  - Filtros combinados.
  - Formato de erro: 400/404/409 com `timestamp`, `status`, `message`, `path`.
- Criar testes de repositório com `@DataJpaTest` para queries customizadas.
- Opcional: testes de contrato com `@WebMvcTest` e mocks de service.

**Motivo:** Garantir que as regras e o contrato não regridam, permitir CI sem banco externo e cobrir os bugs encontrados.

**Impacto:** Aumenta o tempo de build, mas elimina dependência de banco local e dá segurança para a refatoração. Requer Docker disponível no ambiente de teste (Testcontainers).

**Como testar:**

```bash
./mvnw test
# Deve rodar sem precisar exportar variáveis de banco
```

**Arquivos:** `pom.xml`, `src/test/java/**`, configuração de teste, possivelmente `application-test.properties`.

**Commits existentes relacionados:** `54153e7` (commit inicial, que criou o teste de contexto); demais commits de feature que não adicionaram testes.

**Commit sugerido:** `test: add Testcontainers integration tests for business rules`.

---

### M12 — Documentação e coleção de requisições

**Severidade:** P1

**Problema:**

- O README descreve endpoints que não correspondem à implementação.
- O README chama `docs/API.md` de "documentação do frontend", mas não há frontend.
- Não há coleção Postman/Insomnia/`.http`.
- As anotações Swagger têm códigos errados: `assign`, `resolve` e `cancel` estão documentados como 201, mas retornam 200; há descrições copiadas de departamento em endpoints de categoria.
- O endpoint raiz retorna mensagem em português e a classe principal acumula `@RestController`.

**Mudança proposta:**

- Atualizar README com:
  - Rotas reais em inglês.
  - Exemplos de requisição e resposta.
  - Formato padronizado de erro.
  - Como rodar com `docker compose up --build`.
  - Como rodar testes.
  - Estrutura de pacotes atualizada.
  - Explicação da migração `V3` e dos valores dos enums.
  - Remoção da menção a "frontend".
- Criar `docs/postman/ifrn-chamados.postman_collection.json` (ou `.http`) com:
  - Todos os endpoints da seção 4.
  - Variáveis `baseUrl`, `ticketId`, `userId`, etc.
  - Casos felizes e casos de erro (400/404/409).
- Corrigir as anotações `@ApiResponse`.
- Mover o endpoint `/` para um `HomeController` ou remover; a classe `ChamadosApplication` deve apenas inicializar a aplicação.
- Atualizar `docs/API.md` para os valores de enum em inglês (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CANCELED`; `LOW`, `MEDIUM`, `HIGH`; `REQUESTER`, `TECHNICIAN`).

**Motivo:** Entregar documentação coerente com o código e facilitar a avaliação manual. A coleção é exigência explícita do enunciado.

**Impacto:** Apenas documentação e Swagger, sem impacto funcional, exceto a mudança do endpoint raiz (se removido). A atualização de `docs/API.md` é necessária para refletir a decisão de nomenclatura em inglês.

**Como testar:**

```bash
# Importar a coleção no Postman/Insomnia e executar na ordem
# Verificar que todas as requisições retornam os status esperados
curl -s http://localhost:8080/v3/api-docs | jq
# Conferir que os códigos documentados batem com os reais
```

**Arquivos:** `README.md`, `docs/API.md`, nova coleção, `SwaggerConfig.java`, `ChamadosApplication.java`, `HomeController.java`, anotações nos controllers.

**Commits existentes relacionados:** `2388626` (API.md), `1cdc841` (README), `18e254d` (Swagger), commits de cada controller.

**Commit sugerido:** `docs: align README, OpenAPI and add request collection`.

---

### M13 — Ajustes menores de qualidade e nomenclatura

**Severidade:** P3

**Problema:** Pequenas inconsistências que não quebram o fluxo, mas prejudicam a leitura e as boas práticas:

- `Users` no plural como nome de entidade/classe; o padrão é `User`.
- Pacote `users` no plural; os demais são singulares.
- `ChamadosApplication` também é `@RestController`.
- Mensagens de erro e título do Swagger em português.
- `@RequestMapping("{id}")` sem barra inicial.
- Import wildcard em `InteractionController`.
- `artifactId` `chamados` e nome do projeto em português.
- `EntityFinder` é uma classe utilitária estática; poderia ser um componente ou, melhor, cada service usar seu repositório com exceção própria. A recomendação mínima é trocar a exceção de web por exceção de domínio.
- `Department.abbreviation` poderia ser `acronym`, se o time preferir; não é obrigatório.
- `Ticket.openingDate`/`closingDate` poderiam ser `openedAt`/`closedAt`; exigiria nova migration de rename. Não é necessário para atender ao enunciado.

**Mudança proposta:** Aplicar as renomeações e ajustes em um commit separado, sem misturar com correções funcionais.

**Motivo:** Consistência, legibilidade e aderência ao pedido de projeto em inglês.

**Impacto:** Baixo, mas renomear classes/pacotes gera diff grande. Renomear colunas exigiria migration e não é recomendado neste momento; o foco deve ser nomes de classes, pacotes, DTOs e mensagens.

**Como testar:**

```bash
./mvnw -q -DskipTests compile
./mvnw test
curl -s http://localhost:8080/ | jq
```

**Arquivos:** `Users.java`, `UsersController.java`, `UsersService.java`, `UsersRepository.java`, `ChamadosApplication.java`, `SwaggerConfig.java`, `InteractionController.java`, controllers em geral.

**Commits existentes relacionados:** `54153e7` (commit inicial), `18e254d` (Swagger), `9ceb48f` (módulos), commits de usuários.

**Commit sugerido:** `refactor(naming): standardize English class and package names`.

---

## 6. Mapa de commits existentes por área

Esta tabela ajuda a rastrear qual commit introduziu cada comportamento que precisa ser refatorado.

| Área | Commits existentes | O que foi introduzido |
| --- | --- | --- |
| Infra Docker inicial | `e36151a`, `046a489`, `b7be5ca` | Compose base, hot reload e ambiente de teste. |
| Entidades e enums | `ed80468` | `Department`, `Category`, `Users`, `Ticket`, `Interaction`, enums em português, incluindo `URGENTE` e `EM_ANDAMENTO`. |
| Migrations | `b6f0270` | `V1` e `V2`; seed inicial. |
| Swagger | `18e254d` | Configuração OpenAPI. |
| Estrutura de módulos | `9ceb48f` | Controllers/services/repositories vazios por feature. |
| API.md | `2388626` | Contrato REST em inglês. |
| Setores | `2b10653`, `fa12355` | CRUD e update; update via `PATCH`. |
| Categorias | `8a9c18f`, `6d39b02`, `e62d0c9`, `1811af8`, `403ee1e`, `1e8ab87`, `a5f8b4e` | CRUD, listagem por setor, paginação, EntityFinder. |
| Usuários | `5ade3fb`, `cf233cf`, `177976d`, `5c6ce2a`, `839b7b0` | CRUD, filtros, `/user/{id}/chamados`. |
| Chamados | `74eb909`, `bd6311c`, `6cfc023`, `3ef70d5`, `c0343f2` | Criação, filtros, detalhe com interações, assign/resolve/cancel, checagens de exclusão. |
| Interações | `e7dabe3` | `POST /chamados/{id}/interacoes`. |
| README | `1cdc841` | Documentação, estudo de Flyway. |
| Teste de contexto | `54153e7` | `ChamadosApplicationTests`. |

---

## 7. Sequência de commits sugerida

A ordem abaixo minimiza estados intermediários quebrados. O commit de migration deve entrar junto com a mudança de enum, para que o Flyway aplique antes do `validate` do Hibernate.

| Ordem | Commit sugerido | Escopo |
| --- | --- | --- |
| 1 | `refactor(domain)!: translate ticket enums to English` | Enums, `V3`, services, mensagens. |
| 2 | `refactor(api): align routes with API contract` | Rotas, `PUT` de setor, Swagger. |
| 3 | `fix(category): use departmentId when moving category` | Bug de update de categoria e validação de descrição. |
| 4 | `fix(department): block deletion with related categories or users` | Exclusão segura de setor/categoria. |
| 5 | `feat(exception): add domain exceptions and global error handler` | `ApiError`, exceptions, advice, remoção de `ResponseStatusException`. |
| 6 | `feat(pagination): enforce page size limits and paginate department categories` | Pageable, filtros em inglês, categorias paginadas. |
| 7 | `refactor(layers): move code to controller, service, repository and model packages` | Reorganização de pacotes. |
| 8 | `fix(validation): align DTO validation and uniqueness rules` | `@Valid`, PUT completo, constraints. |
| 9 | `refactor(persistence): add transactions, fetch strategies and indexes` | Transações, OSIV, N+1, índices. |
| 10 | `chore(docker): make docker compose up self-contained` | Compose raiz, env, volume, README de execução. |
| 11 | `test: add Testcontainers integration tests for business rules` | Testes de integração e repositório. |
| 12 | `docs: align README, OpenAPI and add request collection` | README, API.md, Postman/Insomnia/`.http`. |
| 13 | `refactor(naming): standardize English class and package names` | Renomeações menores. |

Observação: os commits acima são **propostas**. Nenhum deles foi criado nesta análise. O `REFACTOR.md` é o único artefato novo.

---

## 8. Plano de testes funcionais

### 8.1. Pré-condições

- `docker compose up --build -d` na raiz.
- Base URL `http://localhost:8080`.
- Seed aplicado: setores 1 (COTIC) e 2 (MANUT); categorias 1 e 2 no setor 1, 3 e 4 no setor 2; usuários 1 (técnico do setor 1), 2 (técnico do setor 2), 3 e 4 (solicitantes).
- Após M02, usar valores em inglês.

### 8.2. Fluxo feliz completo

```bash
BASE=http://localhost:8080

# 1. Listar setores com filtro e paginação
curl -s "$BASE/departments?name=cotic&page=0&size=10&sort=name" | jq

# 2. Criar chamado sem prioridade (deve assumir MEDIUM)
curl -s -X POST "$BASE/tickets" \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Projetor não liga",
    "description": "Projetor da sala 1 não liga",
    "location": "Sala 1",
    "requesterId": 3,
    "categoryId": 1
  }' | jq
# Esperado: 201, status OPEN, priority MEDIUM, openingDate preenchida, closingDate null

# Guardar o id retornado em TICKET_ID

# 3. Registrar interação do solicitante
curl -s -X POST "$BASE/tickets/$TICKET_ID/interactions" \
  -H 'Content-Type: application/json' \
  -d '{"text":"O projetor segue sem ligar","authorId":3}' | jq
# Esperado: 201

# 4. Técnico do setor 1 assume
curl -s -X PATCH "$BASE/tickets/$TICKET_ID/assign" \
  -H 'Content-Type: application/json' \
  -d '{"technicianId":1}' | jq
# Esperado: 200, status IN_PROGRESS, technicianId 1

# 5. Técnico registra interação
curl -s -X POST "$BASE/tickets/$TICKET_ID/interactions" \
  -H 'Content-Type: application/json' \
  -d '{"text":"Vou verificar o cabo","authorId":1}' | jq
# Esperado: 201

# 6. Técnico resolve
curl -s -X PATCH "$BASE/tickets/$TICKET_ID/resolve" \
  -H 'Content-Type: application/json' \
  -d '{"technicianId":1,"solution":"Cabo substituído"}' | jq
# Esperado: 200, status RESOLVED, solution preenchida, closingDate preenchida

# 7. Detalhe com interações
curl -s "$BASE/tickets/$TICKET_ID" | jq
# Esperado: interactions em ordem cronológica

# 8. Chamados do solicitante
curl -s "$BASE/users/3/tickets?status=RESOLVED&page=0&size=10" | jq
# Esperado: o chamado aparece
```

### 8.3. Casos negativos

| Cenário | Requisição | Resultado esperado |
| --- | --- | --- |
| Abrir chamado como técnico | `POST /tickets` com `requesterId=1` | 409 |
| Abrir chamado com categoria inexistente | `POST /tickets` com `categoryId=999` | 404 |
| Assumir chamado de outro setor | `PATCH /tickets/{id}/assign` com `technicianId=2` para categoria do setor 1 | 409 |
| Assumir chamado já assumido | repetir assign | 409 |
| Resolver por técnico não atribuído | `PATCH /tickets/{id}/resolve` com outro técnico | 409 |
| Resolver sem solução | `PATCH /tickets/{id}/resolve` com `solution=""` | 400 |
| Cancelar por outro solicitante | `PATCH /tickets/{id}/cancel` com `requesterId=4` | 409 |
| Cancelar chamado em atendimento | cancelar após assign | 409 |
| Interagir em chamado resolvido | `POST /tickets/{id}/interactions` | 409 |
| Interagir em chamado cancelado | `POST /tickets/{id}/interactions` | 409 |
| Interagir como usuário não envolvido | `authorId=4` | 409 |
| Excluir setor com chamado | `DELETE /departments/1` | 409 |
| Excluir setor com categorias | `DELETE /departments/2` | 409 (antes: 500) |
| Excluir categoria com chamado | `DELETE /categories/1` | 409 |
| E-mail duplicado | `POST /users` com e-mail existente | 409 |
| E-mail inválido | `POST /users` com `email=invalido` | 400 |
| Enum inválido | `GET /tickets?status=ABERTO` | 400 |
| Recurso inexistente | `GET /tickets/999` | 404 |
| Tamanho de página acima do máximo | `GET /tickets?size=100` | 200 com `pageSize=50` |

### 8.4. Verificação do formato de erro

```bash
curl -s "$BASE/tickets/999" | jq 'keys'
# Esperado: ["message","path","status","timestamp"]
# (ou incluir "errors" apenas em erros de validação)
```

### 8.5. Verificação de migrations

```bash
docker compose exec postgres psql -U ifrn_chamados -d ifrn_chamados \
  -c "SELECT version, description, checksum, success FROM flyway_schema_history ORDER BY installed_rank;"

# Deve mostrar V1, V2, V3... com success = true.
# Não editar V1/V2. Para testar checksum mismatch, nunca altere uma migration aplicada:
# o Flyway deve abortar com "Migration checksum mismatch".
```

### 8.6. Verificação de Docker

```bash
# Clonar em diretório limpo, sem .env manual
docker compose up --build -d
curl -i http://localhost:8080/
curl -i http://localhost:8080/docs

# Persistência
docker compose down          # sem -v
docker compose up -d
curl -s http://localhost:8080/departments | jq '.totalElements'

# Limpeza
docker compose down -v
```

### 8.7. Verificação de testes automatizados

```bash
./mvnw test
# Todos os testes verdes, sem exportar DATABASE_* manualmente.
```

---

## 9. Riscos e mudanças incompatíveis

| Mudança | Tipo de impacto | Mitigação |
| --- | --- | --- |
| Tradução dos enums | Breaking change no JSON e no banco | Migration `V3`, atualização de docs/coleção, testes de contrato. |
| Renomeação de query params | Breaking change em clientes | Documentar e atualizar Postman; versão de API se necessário. |
| `/departments/{id}/categories` de array para página | Breaking change | Atualizar clientes e testes; documentar. |
| `PUT` completo em setor/categoria | Requisições parciais passam a 400 | Documentar payload completo; se necessário, usar `PATCH`. |
| Exclusão de setor com vínculos passa a 409 | Fluxo de exclusão muda | Cliente deve mover/remover categorias e usuários antes. |
| `open-in-view=false` | Pode revelar lazy loading | Fetch joins/EntityGraph e testes de integração antes de ativar. |
| Remoção de `URGENTE` | Dados existentes com `URGENTE` | Converter para `HIGH` na `V3`. |
| Reorganização de pacotes | Diff grande e conflitos de merge | Commit isolado, sem mudanças funcionais. |
| Compose com credenciais padrão locais | Risco se usado em produção | Documentar que são apenas dev; produção deve sobrescrever com secrets. |

---

## 10. Critérios de aceite

O projeto pode ser considerado aderente quando:

- [ ] `docker compose up --build` na raiz sobe API e PostgreSQL sem passos manuais.
- [ ] `http://localhost:8080/` responde e `http://localhost:8080/docs` abre o Swagger.
- [ ] Todas as rotas de `docs/API.md` existem com os métodos HTTP corretos.
- [ ] Os enums e filtros estão em inglês e consistentes com a documentação.
- [ ] As 8 regras de negócio passam em testes automatizados.
- [ ] Transições inválidas retornam 409; dados inválidos retornam 400; inexistentes retornam 404.
- [ ] Todo erro retorna `timestamp`, `status`, `message` e `path` via `@RestControllerAdvice`.
- [ ] Toda listagem é paginada, com padrão 10 e máximo 50.
- [ ] `GET /departments/{id}/categories` e `GET /users/{id}/tickets` estão paginados e no caminho correto.
- [ ] `ddl-auto` é `validate` em todos os ambientes; Flyway é a única fonte de schema.
- [ ] `V1` e `V2` não foram editadas; mudanças estão em `V3+`.
- [ ] `./mvnw test` passa sem banco externo/configuração manual.
- [ ] Existe coleção Postman/Insomnia/`.http` com todos os endpoints.
- [ ] README e `docs/API.md` refletem o código real.
- [ ] O último commit avaliado está na `main`.
- [ ] Não há senha real/segredo versionado.
- [ ] Nenhum controller contém regra de negócio; services lançam exceções próprias.
- [ ] Injeção de dependência é por construtor.

---

## 11. Conclusão

O projeto tem uma base boa e um fluxo principal viável, mas ainda não está pronto para entrega conforme o enunciado. As correções de maior prioridade são:

1. Alinhar rotas e nomes de filtros ao contrato (`M01`, `M06`).
2. Corrigir o bug de update de categoria (`M03`).
3. Corrigir a exclusão de setor para não retornar 500 (`M04`).
4. Ajustar o domínio de status/prioridade e traduzir os enums (`M02`).
5. Implementar o handler global de erros (`M05`).
6. Tornar o `docker compose up` autossuficiente (`M10`).
7. Fazer o merge final na `main` e adicionar testes/coleção (`M11`, `M12`).

Depois dessas etapas, a reorganização por camadas (`M07`), as melhorias de persistência (`M09`) e os ajustes de nomenclatura (`M13`) elevam a qualidade e a aderência às boas práticas Java.
