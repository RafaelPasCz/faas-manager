# FaaS Manager API

API RESTful desenvolvida para orquestração, monitoramento e gerenciamento de infraestrutura FaaS (Function as a Service) em ambientes distribuídos (Edge e Fog Computing). O sistema gerencia o ciclo de vida de dispositivos (nós de processamento), funções serverless e centraliza a telemetria através de logs de execução imutáveis e integração com Prometheus.

## Tecnologias Utilizadas
* **Java 17+**
* **Spring Boot 3** (Web, Data JPA)
* **PostgreSQL**
* **Hibernate** (Auditoria de dados via `AuditingEntityListener`)
* **Maven**

## Pré-requisitos
* [JDK 17](https://jdk.java.net/17/) ou superior instalado.
* [PostgreSQL](https://www.postgresql.org/download/) rodando localmente (nativamente ou via Docker).
* [Maven](https://maven.apache.org/) instalado (ou utilize o wrapper `mvnw` incluso no projeto).

## Como executar localmente

### 1. Configuração do Banco de Dados
Acesse o seu PostgreSQL pelo terminal ou via interface gráfica (como pgAdmin/DBeaver) e crie o banco de dados e as credenciais utilizadas pelo projeto:

```sql
-- Criar o banco de dados
CREATE DATABASE faas_manager;

-- Criar o usuário administrador (caso ainda não exista)
CREATE USER faas_admin WITH ENCRYPTED PASSWORD '1234';

-- Conceder privilégios ao usuário
GRANT ALL PRIVILEGES ON DATABASE faas_manager TO faas_admin;
```

### 2. Configurações de Ambiente (`application.properties`)
Certifique-se de que o arquivo `src/main/resources/application.properties` está configurado corretamente com a string de conexão e o controle de migração do Hibernate. O arquivo deve conter:

```properties
# Conexão com o PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/faas_manager
spring.datasource.username=faas_admin
spring.datasource.password=1234
spring.datasource.driver-class-name=org.postgresql.Driver

# Configurações do JPA / Hibernate
# "update" criará/atualizará as tabelas automaticamente sem apagar os dados existentes
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# (Opcional) Porta do servidor
server.port=8080
```

### 3. Rodando o Projeto
Abra o terminal na pasta raiz do projeto (onde está o arquivo `pom.xml`) e execute o comando abaixo para compilar e iniciar a aplicação:

```bash
# Se tiver o Maven instalado globalmente:
mvn spring-boot:run

# Se for utilizar o Maven Wrapper do projeto:
./mvnw spring-boot:run     # (Linux/macOS)
mvnw.cmd spring-boot:run   # (Windows)
```

O servidor iniciará localmente e a API estará disponível no endereço: `http://localhost:8080/api/`

---

## Estrutura de Endpoints (Visão Geral)

* **`/api/devices`**: Cadastro e edição de hardware de borda (incluindo validação de IPv4 e URL do Prometheus).
* **`/api/functions`**: Gerenciamento das funções serverless disponíveis no cluster.
* **`/api/execution-logs`**: Ingestão de telemetria. Logs possuem datas de registro automáticas (`savedAt`) e são estritamente imutáveis (append-only) para garantir a integridade da auditoria de falhas e timeouts.
