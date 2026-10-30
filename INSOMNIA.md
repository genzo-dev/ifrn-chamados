# Coleção do Insomnia — Sistema de Chamados IFRN

A coleção está versionada em um único arquivo:

```text
docs/insomnia/chamados.insomnia.yaml
```

Formato: **Insomnia v5 YAML** (`collection.insomnia.rest/5.0`, schema `5.1`).

O arquivo contém:

- A coleção completa, organizada em pastas.
- O ambiente `Base Environment`.
- Os bodies, path variables e query params.
- Scripts de after-response para capturar IDs e validar status HTTP.
- Casos de erro `400`, `404` e `409`.

Não existe arquivo separado de ambiente.

## 1. Pré-requisitos

- Insomnia 13 ou superior.
- Docker e Docker Compose, ou a aplicação rodando localmente.
- API disponível em `http://localhost:8080`.

Para subir a API:

```bash
make run-dev
```

Para recriar o banco do zero:

```bash
make down-dev-vol
make run-dev
```

Acessos úteis:

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/docs`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Não há autenticação na API atual.

## 2. Importar a coleção

1. Abra o Insomnia.
2. Clique em **Import**.
3. Escolha **File**.
4. Selecione `docs/insomnia/chamados.insomnia.yaml`.
5. Confirme a importação.
6. Verifique se a coleção `Sistema de Chamados IFRN` e o ambiente `Base Environment` foram criados.
7. Selecione o ambiente `Base Environment`.

O arquivo é autocontido: a coleção e o ambiente são importados juntos.

## 3. Ambiente

O ambiente `Base Environment` contém:

| Variável | Valor inicial | Uso |
|---|---:|---|
| `base_url` | `http://localhost:8080` | Base de todas as URLs |
| `page` | `0` | Paginação zero-based |
| `size` | `10` | Tamanho da página; máximo 50 |
| `sort_name` | `name,asc` | Ordenação por nome |
| `sort_opening` | `openingDate,desc` | Ordenação por abertura |
| `department_id` | `1` | Setor COTIC |
| `department_id_2` | `2` | Setor MANUT |
| `category_id` | `1` | Categoria Internet/Wi-Fi |
| `category_id_2` | `3` | Categoria Ar-condicionado |
| `requester_id` | `3` | José Solicitante |
| `other_requester_id` | `4` | Ana Professora |
| `technician_id` | `1` | João Técnico, setor 1 |
| `technician_id_2` | `2` | Maria Manutenção, setor 2 |
| `user_id` | `3` | Usuário usado nas buscas |
| `status` | `ABERTO` | Filtro de status |
| `priority` | `MEDIA` | Prioridade padrão |
| `user_type` | `TECNICO` | Filtro de tipo de usuário |
| `missing_id` | `999999` | Recursos inexistentes |

IDs criados em tempo de execução:

- `created_department_id`
- `created_category_id`
- `created_user_id`
- `created_ticket_id`
- `created_interaction_id`
- `ticket_id`
- `ticket_id_cancel`
- `ticket_id_error`

Os scripts de after-response atualizam essas variáveis.

## 4. Dados iniciais

O `V2__insert_initial_data.sql` cria os dados abaixo em um banco novo:

| Recurso | ID | Descrição |
|---|---:|---|
| Setor | 1 | Tecnologia da Informação (`COTIC`) |
| Setor | 2 | Manutenção de Infraestrutura (`MANUT`) |
| Categoria | 1 | Internet/Wi-Fi — setor 1 |
| Categoria | 2 | Projetor — setor 1 |
| Categoria | 3 | Ar-condicionado — setor 2 |
| Categoria | 4 | Mobiliário — setor 2 |
| Técnico | 1 | João Técnico — setor 1 |
| Técnico | 2 | Maria Manutenção — setor 2 |
| Solicitante | 3 | José Solicitante |
| Solicitante | 4 | Ana Professora |

Se o banco não estiver limpo, recrie os volumes antes de executar a coleção.

## 5. Estrutura da coleção

| Pasta | Requisições | Conteúdo |
|---|---:|---|
| `00 - Health` | 1 | Verificação da API |
| `01 - Departments` | 6 | CRUD e categorias do setor |
| `02 - Categories` | 5 | CRUD de categorias |
| `03 - Users` | 5 | CRUD e chamados do usuário |
| `04 - Tickets` | 7 | Criação, listagem, busca, assign e cancel |
| `05 - Interactions` | 2 | Interação e resolução do chamado |
| `99 - Error Cases` | 20 | Cenários `400`, `404` e `409` |

As rotas seguem o código atual, com nomes singulares:

- `/department`
- `/category`
- `/user`
- `/ticket`

## 6. Ordem recomendada

Execute primeiro o caminho feliz e deixe `99 - Error Cases` por último.

1. `GET /` — confirmar que a API está no ar.
2. `POST /department` — criar setor e capturar `created_department_id`.
3. `POST /category` — criar categoria e capturar `created_category_id`.
4. `POST /user` — criar usuário e capturar `created_user_id`.
5. `POST /ticket` — criar o chamado principal e capturar `ticket_id`.
6. `POST /ticket` — criar o chamado de cancelamento e capturar `ticket_id_cancel`.
7. `POST /ticket` — criar o chamado de erros e capturar `ticket_id_error`.
8. `PATCH /ticket/{id}/assign` — assumir o chamado principal.
9. `PATCH /ticket/{id}/cancel` — cancelar o chamado de cancelamento.
10. `POST /ticket/{id}/interaction` — registrar interação no chamado principal.
11. `PATCH /ticket/{id}/resolve` — resolver o chamado principal.
12. Executar `99 - Error Cases`.

A resolução está na pasta `05 - Interactions` para manter a ordem
`criar → assumir → interagir → resolver` quando a coleção for executada
de cima para baixo.

## 7. Scripts

As requisições de criação salvam o `id` retornado no ambiente:

- `POST /department` → `created_department_id`
- `POST /category` → `created_category_id`
- `POST /user` → `created_user_id`
- `POST /ticket` → `ticket_id`, `created_ticket_id`, `ticket_id_cancel` ou `ticket_id_error`
- `POST /ticket/{id}/interaction` → `created_interaction_id`

As requisições de mudança de estado validam o corpo:

- `assign` espera `EM_ANDAMENTO`.
- `resolve` espera `RESOLVIDO`.
- `cancel` espera `CANCELADO`.

As requisições de erro validam apenas o status HTTP, porque o corpo padrão de erro do Spring pode não incluir `message`.

## 8. Casos de erro incluídos

Validação `400`:

- Criar setor sem `name`.
- Criar categoria sem `description`.
- Criar usuário com matrícula fora de 7 caracteres.
- Criar chamado sem `title`.
- Criar interação com `text` vazio.

Recurso inexistente `404`:

- Buscar setor, categoria, usuário e chamado com `missing_id`.

Conflito `409`:

- Criar setor com sigla `COTIC`.
- Atualizar setor 1 com sigla `MANUT`.
- Criar categoria com nome `Internet/Wi-Fi`.
- Criar usuário com e-mail duplicado.
- Abrir chamado usando um técnico como solicitante.
- Excluir o setor 1, que possui vínculos.
- Assumir chamado com técnico de outro setor.
- Cancelar chamado por outro solicitante.
- Assumir chamado já resolvido.
- Cancelar chamado já cancelado.
- Interagir em chamado já resolvido.

Os casos `409` dependem do caminho feliz já ter sido executado.

## 9. Exportar ou atualizar o arquivo

Se alterar a coleção no Insomnia:

1. Abra o menu da **API Collection**.
2. Escolha **Export**.
3. Selecione **Insomnia v5 YAML**.
4. Salve como `docs/insomnia/chamados.insomnia.yaml`.
5. Revise o diff antes do commit.

Para manter um único arquivo, não use **Git Sync**: ele grava a coleção em vários arquivos.

O export de **API Collection** inclui requests, ambiente e scripts. Evite **Export all data**.

## 10. Decisões e limitações conhecidas

- As rotas permanecem singulares.
- `CategoryCreateDTO` usa `department_id`; `CategoryUpdateDTO` usa `departmentId`. A coleção segue o código atual.
- `TicketController` usa filtros em português: `setorId`, `categoriaId`, `solicitanteId`, `tecnicoId`, `dataInicio` e `dataFim`.
- `PATCH /ticket/{id}/assign`, `resolve` e `cancel` retornam `200`, embora a anotação Swagger declare `201`. A coleção valida `200`.
- Não existe `DELETE /user` nem `DELETE /ticket`.
- `GET /department/{id}/category` retorna uma lista, sem paginação.
- `CategoryService.updateById` tem um bug conhecido: busca o departamento usando o ID da categoria. A coleção usa o payload correto (`departmentId`); a correção do service fica fora do escopo.
- O OpenAPI JSON continua disponível em `/v3/api-docs`, mas não é versionado neste repositório.

## 11. Checklist

- [ ] A coleção importa sem erro no Insomnia.
- [ ] O ambiente `Base Environment` foi criado e selecionado.
- [ ] A pasta `99 - Error Cases` contém os cenários `400`, `404` e `409`.
- [ ] O fluxo criar → assumir → interagir → resolver funciona.
- [ ] O fluxo de cancelamento funciona.
- [ ] Os scripts capturam os IDs criados.
- [ ] O arquivo `docs/insomnia/chamados.insomnia.yaml` está versionado.
- [ ] O README aponta para a coleção.
