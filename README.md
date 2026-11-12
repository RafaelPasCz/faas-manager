# FaaS Manager

O FaaS Manager é um plano de controle (Control Plane) desenvolvido para orquestração, implantação e monitoramento de infraestrutura Function as a Service (FaaS) em ambientes distribuídos, com foco em Edge e Fog Computing. 

Este repositório representa a primeira fase de um projeto arquitetural maior integrado ao **Switchfaas**, com o objetivo de estabelecer uma camada de execução adaptativa ao longo do continuum de computação.

## Arquitetura do Sistema

O projeto adota uma arquitetura cliente-servidor, separando as responsabilidades de gerenciamento de estado (Back-end) e interface de operação (Front-end).

### Back-end (API RESTful)
Desenvolvido em Java 17 com Spring Boot 3 e PostgreSQL, o back-end atua como a fonte de verdade do estado do cluster. Ele é dividido nos seguintes módulos principais:

* **Gerenciamento de Dispositivos (Devices):** Responsável por cadastrar e manter o estado dos nós físicos de hardware na borda. O sistema diferencia a topologia dos nós, identificando quais são hospedeiros de funções (Function Hosts) e quais atuam como concentradores de métricas (Prometheus Hosts), além de gerenciar endereçamentos IP.
* **Catálogo de Funções (Functions):** Mantém a abstração das funções serverless disponíveis para o sistema. Este módulo armazena metadados de execução, como a imagem Docker correspondente e o ambiente de execução (runtime), desvinculados da infraestrutura física.
* **Instâncias de Função (Function Instances):** Gerencia o ciclo de vida dos *deployments*. Este componente é responsável por fazer o vínculo (binding) entre uma Função Abstrata e um Dispositivo Físico, controlando os status transicionais (ex: DEPLOYING, IDLE, EXECUTING).
* **Observabilidade (Execution Logs):** Módulo de ingestão de telemetria focado em auditoria. Registra dados vitais de cada requisição (duração em milissegundos, status code HTTP e data exata). Os logs são estritamente imutáveis (append-only), garantidos pelo uso de `AuditingEntityListener` do Hibernate, impossibilitando a alteração do histórico de execuções.

### Front-end (Dashboard de Controle)
A interface de usuário foi construída com React e a biblioteca de componentes Tremor, operando como uma Single Page Application (SPA) focada na consistência visual e na rápida tomada de decisão. O funcionamento baseia-se em três pilares:

* **Visão de Dispositivos:** Uma interface baseada em cards expansíveis que exibe a infraestrutura física. Ao interagir com um nó, o operador visualiza métricas de hardware, a tabela de instâncias de funções provisionadas naquele recurso e um *feed* em tempo real com as últimas execuções processadas por aquele dispositivo. A partir desta tela, é possível invocar modais para provisionamento imediato de novas instâncias.
* **Gestão do Catálogo:** Uma aba dedicada à listagem e pesquisa das funções abstratas, permitindo a adição e remoção de definições de software que poderão ser implantadas na topologia.
* **Auditoria de Logs:** Uma interface de observabilidade avançada que centraliza o histórico global de execuções. O front-end aplica filtros combinados reativos (por nomenclatura, janelas de tempo, códigos HTTP e resultados binários) cruzando os dados em memória para garantir respostas imediatas na interface.

---

## Stack Tecnológico

**Back-end:**
* Java 17+
* Spring Boot 3 (Web, Data JPA)
* PostgreSQL
* Hibernate 
* Maven

**Front-end:**
* React
* Tremor (Componentes de UI e visualização de dados)
* Tailwind CSS

---

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

### 2. Configurações de Ambiente
Certifique-se de que o arquivo `src/main/resources/application.properties` está configurado corretamente com a string de conexão e o controle de migração do Hibernate. O arquivo deve conter:

```properties
# Conexão com o PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/faas_manager
spring.datasource.username=faas_admin
spring.datasource.password=1234
spring.datasource.driver-class-name=org.postgresql.Driver

# Configurações do JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# (Opcional) Porta do servidor
server.port=8080
```

### 3. Execução do Servidor
Abra o terminal na pasta raiz do projeto (onde está o arquivo `pom.xml`) e execute o comando abaixo para compilar e iniciar a aplicação:

```bash
# Utilizando Maven instalado globalmente:
mvn spring-boot:run

# Utilizando Maven Wrapper (Linux/macOS):
./mvnw spring-boot:run

# Utilizando Maven Wrapper (Windows):
mvnw.cmd spring-boot:run
```

A API estará disponível no endereço: `http://localhost:8080/api/`
