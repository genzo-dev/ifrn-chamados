# Documentação da API REST — Sistema de Chamados

## 1. Visão geral

Esta documentação descreve as rotas da API REST do Sistema de Chamados, incluindo os métodos HTTP disponíveis, os códigos de resposta esperados e as principais regras de negócio aplicadas a cada operação.

### Convenções

| Código HTTP       | Descrição                                                           |
| ----------------- | ------------------------------------------------------------------- |
| `200 OK`          | Requisição executada com sucesso.                                   |
| `201 Created`     | Recurso criado com sucesso.                                         |
| `400 Bad Request` | Requisição inválida ou dados fornecidos incorretamente.             |
| `404 Not Found`   | Recurso não encontrado.                                             |
| `409 Conflict`    | Conflito com uma regra de negócio ou com o estado atual do recurso. |

---

## 2. Endpoints da API

### 2.1. Departments

| Rota                        | Método HTTP | Sucesso | Erros        | Observações |
| --------------------------- | ----------- | ------- | ------------ | ----------- |
| `/department`               | `POST`      | `201`   | `400`        | —           |
| `/department`               | `GET`       | `200`   | —            | Obs. 1      |
| `/department/{id}`          | `GET`       | `200`   | `404`        | —           |
| `/department/{id}`          | `PUT`       | `200`   | `404`        | —           |
| `/department/{id}`          | `DELETE`    | `200`   | `404`, `409` | Obs. 2      |
| `/department/{id}/category` | `GET`       | `200`   | `404`        | Obs. 4      |

### 2.2. Categories

| Rota             | Método HTTP | Sucesso | Erros        | Observações |
| ---------------- | ----------- | ------- | ------------ | ----------- |
| `/category`      | `POST`      | `201`   | `400`, `404` | —           |
| `/category`      | `GET`       | `200`   | —            | Obs. 3      |
| `/category/{id}` | `GET`       | `200`   | `404`        | —           |
| `/category/{id}` | `PUT`       | `200`   | `404`        | —           |
| `/category/{id}` | `DELETE`    | `200`   | `404`, `409` | Obs. 2      |

### 2.3. Users

| Rota                | Método HTTP | Sucesso | Erros        | Observações |
| ------------------- | ----------- | ------- | ------------ | ----------- |
| `/user`             | `POST`      | `201`   | `400`, `409` | Obs. 5      |
| `/user`             | `GET`       | `200`   | —            | Obs. 6      |
| `/user/{id}`        | `GET`       | `200`   | `404`        | —           |
| `/user/{id}`        | `PUT`       | `200`   | `404`, `409` | Obs. 5      |
| `/user/{id}/ticket` | `GET`       | `200`   | `404`        | Obs. 10     |

### 2.4. Tickets

| Rota                   | Método HTTP | Sucesso | Erros               | Observações |
| ---------------------- | ----------- | ------- | ------------------- | ----------- |
| `/ticket`              | `POST`      | `201`   | `400`, `404`        | Obs. 7      |
| `/ticket`              | `GET`       | `200`   | —                   | Obs. 8      |
| `/ticket/{id}`         | `GET`       | `200`   | `404`               | Obs. 9      |
| `/ticket/{id}/assign`  | `PATCH`     | `200`   | `404`, `409`        | Obs. 11     |
| `/ticket/{id}/resolve` | `PATCH`     | `200`   | `400`, `404`, `409` | Obs. 12     |
| `/ticket/{id}/cancel`  | `PATCH`     | `200`   | `404`, `409`        | Obs. 13     |

### 2.5. Interactions

| Rota                       | Método HTTP | Sucesso | Erros               | Observações |
| -------------------------- | ----------- | ------- | ------------------- | ----------- |
| `/ticket/{id}/interaction` | `POST`      | `201`   | `400`, `404`, `409` | Obs. 14     |

---

## 3. Regras gerais

### 3.1. Identificação do usuário responsável pela ação

Como a API não possui autenticação, o identificador do usuário que executa a operação deve ser enviado no corpo da requisição, quando aplicável.

Exemplos:

- `requesterId`: identifica o solicitante responsável pela abertura ou pelo cancelamento de um chamado.
- `technicianId`: identifica o técnico responsável por assumir ou resolver um chamado.

A identificação do usuário deve ser utilizada para aplicar as regras de autorização de negócio descritas nesta documentação.

### 3.2. Paginação

Todas as rotas que retornam coleções devem implementar paginação com os seguintes parâmetros:

| Parâmetro                | Regra                                           |
| ------------------------ | ----------------------------------------------- |
| Tamanho padrão da página | 10 itens                                        |
| Tamanho máximo da página | 50 itens                                        |
| Metadados                | A resposta deve incluir os metadados da página. |

A paginação deve permitir consultar os resultados em partes, evitando o retorno de grandes volumes de dados em uma única requisição.

---

## 4. Observações e regras de negócio por endpoint

### Obs. 1 — Listagem de setores

A listagem de setores deve aceitar um filtro parcial pelo nome, sem diferenciar letras maiúsculas de minúsculas.

### Obs. 2 — Exclusão de setores e categorias

Conforme a Regra 8, é proibida a exclusão de setores ou categorias que possuam chamados vinculados.

Nesses casos, a API deve retornar:

- `409 Conflict`: quando a exclusão violar essa regra de negócio.
- `404 Not Found`: quando o setor ou a categoria não existir.

### Obs. 3 — Listagem de categorias

A listagem de categorias deve permitir filtros pelos seguintes campos:

- `name`
- `departmentId`

### Obs. 4 — Categorias de um setor

A rota `GET /department/{id}/category` deve retornar exclusivamente as categorias pertencentes ao setor informado.

Caso o setor não exista, a API deve retornar `404 Not Found`.

### Obs. 5 — Unicidade do e-mail de usuários

No cadastro ou na atualização de um usuário, o e-mail informado não pode estar associado a outro usuário.

Quando houver conflito de e-mail, a API deve retornar `409 Conflict`.

### Obs. 6 — Listagem de usuários

A listagem de usuários deve permitir filtros pelos seguintes campos:

- `name`
- `userType`
- `departmentId`

### Obs. 7 — Abertura de chamados

A abertura de chamados deve obedecer às seguintes regras:

- Apenas usuários do tipo `SOLICITANTE` podem abrir chamados.
- O status inicial do chamado deve ser obrigatoriamente `ABERTO`.
- A prioridade padrão deve ser `MEDIA`, caso não seja informada.
- Referências inválidas a recursos relacionados devem resultar em `404 Not Found`, conforme aplicável.

### Obs. 8 — Listagem geral de chamados

A rota `GET /tickets` deve permitir filtrar os resultados pelos seguintes campos:

- `status`
- `priority`
- `departmentId`
- `categoryId`
- `requesterId`
- `technicianId`
- `startDate`
- `endDate`

Os campos `startDate` e `endDate` devem permitir filtrar os chamados por período.

### Obs. 9 — Detalhamento de um chamado

A rota `GET /tickets/{id}` deve retornar os dados completos do chamado, incluindo suas interações.

Caso o chamado não exista, a API deve retornar `404 Not Found`.

### Obs. 10 — Chamados por usuário

A rota `GET /user/{id}/ticket` deve retornar:

- Os chamados abertos pelo solicitante informado; ou
- Os chamados atribuídos ao técnico informado.

A listagem deve aceitar um filtro por `status`.

### Obs. 11 — Assumir um chamado

A operação `PATCH /tickets/{id}/assign` deve obedecer às seguintes regras:

- Apenas um técnico pode assumir um chamado.
- O chamado deve estar no status `ABERTO`.
- A categoria do chamado deve pertencer ao setor do técnico.
- Após a operação, o status do chamado deve mudar para `EM_ANDAMENTO`.

A operação deve retornar `409 Conflict` quando o chamado não puder ser assumido devido ao seu estado ou à violação de uma regra de negócio.

### Obs. 12 — Resolver um chamado

A operação `PATCH /tickets/{id}/resolve` deve obedecer às seguintes regras:

- Apenas o técnico já atribuído ao chamado pode resolvê-lo.
- O envio da solução é obrigatório.
- O status do chamado deve mudar para `RESOLVIDO`.
- O campo `closingDate` deve ser preenchido.

Se a solução não for informada, a API deve retornar `400 Bad Request`.

Se a operação violar uma regra de negócio, a API deve retornar `409 Conflict`.

### Obs. 13 — Cancelar um chamado

A operação `PATCH /tickets/{id}/cancel` deve obedecer às seguintes regras:

- Apenas o solicitante que criou o chamado pode cancelá-lo.
- O chamado deve estar no status `ABERTO`.
- Após o cancelamento, o status deve mudar para `CANCELADO`.

Caso o chamado não possa ser cancelado devido ao seu estado ou à violação de uma regra de negócio, a API deve retornar `409 Conflict`.

### Obs. 14 — Registrar interações

A operação `POST /tickets/{id}/interactions` deve obedecer às seguintes regras:

- Apenas o solicitante que abriu o chamado ou o técnico responsável pode registrar interações.
- Chamados com status `RESOLVIDO` não podem receber novas interações.
- Chamados com status `CANCELADO` não podem receber novas interações.
- Tentativas de registrar interações em chamados encerrados devem retornar `409 Conflict`.

---

## 5. Resumo dos códigos de resposta

| Código HTTP       | Aplicação na API                                                                                             |
| ----------------- | ------------------------------------------------------------------------------------------------------------ |
| `200 OK`          | Consultas, atualizações, exclusões e operações de alteração de estado executadas com sucesso.                |
| `201 Created`     | Criação de setores, categorias, usuários, chamados e interações.                                             |
| `400 Bad Request` | Dados inválidos ou ausência de informações obrigatórias.                                                     |
| `404 Not Found`   | Recurso solicitado ou referência necessária não encontrada.                                                  |
| `409 Conflict`    | Violação de regras de negócio, conflito de unicidade ou operação incompatível com o estado atual do recurso. |

### 5.1 Formato dos erros

Os erros são retornados no formato `application/problem+json` (`ProblemDetail`), sem stack trace:

```json
{
  "type": "about:blank",
  "title": "Regra de negócio violada",
  "status": 409,
  "detail": "Apenas chamados com status ABERTO podem ser assumidos.",
  "instance": "/ticket/1/assign",
  "code": "BUSINESS_RULE_VIOLATION"
}
```

Erros de validação incluem o campo `errors`:

```json
{
  "type": "about:blank",
  "title": "Erro de validação",
  "status": 400,
  "detail": "Dados inválidos ou fornecidos incorretamente.",
  "instance": "/department",
  "code": "VALIDATION_ERROR",
  "errors": {
    "name": ["O nome é obrigatório"]
  }
}
```

| `code`                     | Status | Aplicação                                                   |
| -------------------------- | -----: | ----------------------------------------------------------- |
| `RESOURCE_NOT_FOUND`       |  `404` | Recurso ou referência não encontrada.                       |
| `INVALID_REQUEST`          |  `400` | Corpo, parâmetro ou tipo de dados inválido.                 |
| `VALIDATION_ERROR`         |  `400` | Falha de validação de campos.                               |
| `DUPLICATE_RESOURCE`       |  `409` | Violação de unicidade.                                      |
| `RESOURCE_IN_USE`          |  `409` | Recurso com vínculos que impedem a exclusão.                |
| `INVALID_STATE`            |  `409` | Operação incompatível com o estado atual do recurso.        |
| `BUSINESS_RULE_VIOLATION`  |  `409` | Violação de regra de negócio.                               |
| `DATA_INTEGRITY_VIOLATION` |  `409` | Violação de integridade detectada pelo banco.               |
| `UNSUPPORTED_MEDIA_TYPE`   |  `415` | `Content-Type` não suportado.                               |
| `METHOD_NOT_ALLOWED`       |  `405` | Método HTTP não suportado para o recurso.                   |
| `INTERNAL_ERROR`           |  `500` | Erro inesperado; detalhes são registrados apenas no servidor. |

---

## 6. Considerações finais

A implementação dos endpoints deve respeitar os códigos HTTP definidos nesta documentação, garantindo respostas consistentes para operações bem-sucedidas, erros de validação, recursos inexistentes e conflitos de regras de negócio.

As regras descritas também devem ser consideradas na implementação dos serviços, dos controladores e das validações de domínio do backend.
