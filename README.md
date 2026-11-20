# FaaS Manager

O FaaS Manager é um plano de controle (Control Plane) desenvolvido para orquestração, implantação e monitoramento de infraestrutura Function as a Service (FaaS) em ambientes distribuídos, com foco em Edge e Fog Computing.

Este repositório integra o ecossistema **FaaSwitch** e segue uma arquitetura desacoplada (*Stateless Control Plane* + banco de dados isolado), preparando a camada de controle para evoluir ou ser substituída de forma independente da camada de persistência.

---

## Estrutura do Repositório

```text
faas-manager/
├── backend/                 # API RESTful (Spring Boot) + Docker Compose do PostgreSQL
└── frontend/
    └── faas-dashboard/      # Dashboard web (React + Tremor + Tailwind CSS)
```

---

## Arquitetura do Sistema

O projeto adota uma arquitetura cliente-servidor com estrita separação de responsabilidades entre **Persistência (PostgreSQL isolado)**, **Plano de Controle (Back-end *stateless*)** e **Interface de Operação (Front-end)**.

### Back-end (`backend/`)
Desenvolvido em Java 21 com Spring Boot 4 e PostgreSQL, o back-end atua como a camada de controle *stateless* conectada ao banco exclusivamente via TCP/IP (configurável por variáveis de ambiente). Ele é dividido nos seguintes módulos principais:

* **Gerenciamento de Dispositivos (Devices):** Responsável por cadastrar e manter o estado dos nós físicos de hardware na borda, validando unicidade de nome e IP apenas entre registros ativos (*soft delete*). O sistema diferencia a topologia dos nós, identificando quais são hospedeiros de funções (Function Hosts) e quais atuam como concentradores de métricas (Prometheus Hosts), além de consultar métricas de CPU e RAM no Prometheus (`PrometheusService`) com normalização automática do esquema `http://`.
* **Catálogo de Funções (Functions):** Mantém a abstração das funções serverless disponíveis para o sistema, validando unicidade de nome entre funções ativas. Este módulo armazena metadados de execução, como a imagem Docker correspondente e o ambiente de execução (runtime), desvinculados da infraestrutura física.
* **Instâncias de Função (Function Instances):** Gerencia o ciclo de vida completo dos *deployments* integrando diretamente com o **`faas-agent`** (`FaasAgentService`) via HTTP (`POST /deploy` e `DELETE /function/{name}`). Sincroniza o estado real das funções implantadas (`DEPLOYING`, `IDLE`, `EXECUTING`, `STOPPED` e `MISSING` quando ausente no agente) e expõe `POST /api/function-instances/{id}/redeploy` para re-implantação.
* **Observabilidade (Execution Logs):** Módulo de coleta incremental e ingestão de telemetria focado em auditoria. Sincroniza periodicamente os logs gerados pelos nós `faas-agent` (`GET /logs?afterId=...`) de forma idempotente via `agentLogId` e persiste duração em milissegundos, status code HTTP, sucesso e data/hora exata. Os logs são estritamente imutáveis (*append-only*), garantidos pelo uso de `AuditingEntityListener` do Hibernate.

### Front-end (`frontend/faas-dashboard/`)
A interface de usuário foi construída com React, Vite e a biblioteca de componentes Tremor, operando como uma Single Page Application (SPA) focada na consistência visual e na rápida tomada de decisão. O funcionamento baseia-se em três pilares:

* **Visão de Dispositivos:** Interface baseada em cards expansíveis que exibe a infraestrutura física. Ao interagir com um nó, o operador visualiza métricas de hardware, a tabela de instâncias de funções com atualização automática de status, opção de remover implantações, aviso com botão **Re-implantar** quando uma função estiver `MISSING` no agente, um *feed* com as últimas execuções e modais para provisionamento de novas instâncias.
* **Gestão do Catálogo:** Aba dedicada à listagem e pesquisa das funções abstratas, permitindo a adição e remoção de definições de software com feedback de validação e conflito diretamente nos modais.
* **Auditoria de Logs:** Interface de observabilidade com atualização automática a cada 10s quando ativa e botão manual de atualização, centralizando o histórico global de execuções com filtros combinados em memória (por nomenclatura, janelas de tempo, códigos HTTP e resultados).

---

## Stack Tecnológico

**Back-end & Infraestrutura:**
* Java 21
* Spring Boot 4 (WebMVC, Data JPA / Hibernate)
* PostgreSQL 16 (containerizado via Docker Compose)
* Maven
* Docker & Docker Compose

**Front-end:**
* React 19 + Vite
* Tremor (`@tremor/react`)
* Tailwind CSS

---

## Como executar localmente

### 1. Variáveis de Ambiente (`backend/.env`)

O back-end lê suas configurações a partir de variáveis de ambiente (com valores padrão para desenvolvimento local em `backend/src/main/resources/application.properties`). Caso queira customizá-las, crie um arquivo `.env` dentro de `backend/` a partir do modelo:

```bash
cd backend
cp .env.example .env
```

Principais variáveis disponíveis em `backend/.env.example`:
* `SERVER_PORT`: Porta HTTP do back-end (padrão `8080`).
* `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`: Conexão TCP/IP com o PostgreSQL isolado (padrão `localhost:5432/faas_manager`, usuário `faas_admin`).
* `JPA_DDL_AUTO`, `JPA_SHOW_SQL`: Comportamento do Hibernate (`update` / `true`).
* `CORS_ALLOWED_ORIGINS`: Lista de origens permitidas no CORS separadas por vírgula (padrão `http://localhost:5173,http://localhost:4200`).
* `HOST_LOOPBACK_ALIAS`: Alias/IP usado quando o back-end roda em container e precisa alcançar o Prometheus ou um `faas-agent` rodando em `localhost`/`127.0.0.1` na máquina hospedeira (no Docker Compose é preenchido por padrão com `host.docker.internal`).
* `TZ`: Fuso horário utilizado pelos containers do PostgreSQL e do FaaS Manager (padrão `America/Sao_Paulo`, sincronizado também com `/etc/localtime` do host).

---

### 2. Executando o Back-end e o Banco Isolado (`backend/`)

Certifique-se de que não haja outro PostgreSQL ocupando a porta `5432` no host (ou ajuste `DB_PORT` no `.env`).

#### Modo 1 — Apenas PostgreSQL em container + Back-end via Maven no host (desenvolvimento)
1. Suba apenas a instância isolada do PostgreSQL com volume persistente (`faaswitch_pgdata`):
   ```bash
   cd backend
   docker compose up -d
   ```
2. Inicie a aplicação Spring Boot no host conectando via TCP/IP:
   ```bash
   ./mvnw spring-boot:run
   ```

#### Modo 2 — Stack completa em containers (`postgres` + `faas-manager`)
Para construir e executar também o back-end em container (`Dockerfile` multi-stage), utilize o profile `full`:

```bash
cd backend
docker compose --profile full up -d --build
```

> **Nota para ambientes com Docker Rootless (Linux):**
> No modo *Rootless*, o `host.docker.internal` aponta para o namespace do RootlessKit e não alcança o `127.0.0.1` do host. Se o seu Prometheus estiver rodando diretamente na máquina física (ouvindo em `*:9090` / `0.0.0.0:9090`), defina `HOST_LOOPBACK_ALIAS` no `backend/.env` com o IP local da sua placa de rede antes de subir o container:
> ```bash
> echo "HOST_LOOPBACK_ALIAS=$(hostname -I | awk '{print $1}')" >> backend/.env
> docker compose --profile full up -d
> ```

A API estará disponível em: `http://localhost:8080/api/`

---

### 3. Executando o Front-end (`frontend/faas-dashboard/`)

Em outro terminal, instale as dependências e inicie o servidor de desenvolvimento do Vite:

```bash
cd frontend/faas-dashboard
npm install
npm run dev
```

O dashboard ficará disponível em `http://localhost:5173`.
