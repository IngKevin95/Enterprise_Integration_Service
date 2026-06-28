.DEFAULT_GOAL := help

.PHONY: help build test verify run docker-up docker-down clean

help:
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Compila el proyecto (sin tests)
	mvn package -DskipTests

test: ## Corre los tests unitarios
	mvn test

verify: ## Tests + Checkstyle + JaCoCo (perfil CI)
	mvn verify -P ci

run: ## Inicia la app en modo desarrollo (:8080)
	mvn spring-boot:run -Dspring-boot.run.profiles=dev

docker-up: ## Levanta el stack completo (app + PostgreSQL)
	docker compose up --build

docker-down: ## Detiene y elimina volumenes
	docker compose down -v

clean: ## Limpia artefactos de build
	mvn clean
