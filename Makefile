run-dev:
	docker compose --env-file .envs/.env.dev -f infra/docker-compose.yml -f infra/docker-compose.dev.yml up

down-dev: 
	docker compose --env-file .envs/.env.dev -f infra/docker-compose.yml -f infra/docker-compose.dev.yml down

down-dev-vol:
	docker compose --env-file .envs/.env.dev -f infra/docker-compose.yml -f infra/docker-compose.dev.yml down -v

run-test:
	docker compose --env-file .envs/.env.test -f infra/docker-compose.yml -f infra/docker-compose.test.yml up

down-test: 
	docker compose --env-file .envs/.env.test -f infra/docker-compose.yml -f infra/docker-compose.test.yml down

