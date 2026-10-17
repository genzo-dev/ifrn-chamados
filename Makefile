run-dev:
	docker compose --env-file .envs/.env.dev -f infra/docker-compose.yml -f infra/docker-compose.dev.yml up