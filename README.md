# FaaS Manager (Admin API)

O **FaaS Manager** é uma API REST gerencial desenvolvida em Java com Spring Boot, projetada para fornecer uma visão administrativa sobre infraestruturas distribuídas de *Function as a Service* (FaaS). 

Este projeto atua como um painel de controle (CRUD) para que administradores de sistemas e engenheiros de infraestrutura possam cadastrar nós de execução (Edge, Fog, Cloud), gerenciar as funções serverless alocadas e consultar pontualmente a integridade da rede. O objetivo principal do repositório é demonstrar a construção de uma API corporativa estruturada, aplicando padrões arquiteturais de mercado e boas práticas de desenvolvimento no ecossistema Spring.

## 🏗️ Objetivo e Casos de Uso

A aplicação foi desenhada para o uso humano (visão gerencial e administrativa), separando as responsabilidades de cadastro da complexidade do roteamento dinâmico de rede. Suas principais capacidades incluem:

* **Gestão de Inventário (CRUD):** Cadastro completo de dispositivos físicos, definindo suas restrições operacionais (limites de CPU e RAM) e categorização topológica.
* **Mapeamento de Funções:** Associação lógica de quais funções FaaS estão disponíveis em quais nós da infraestrutura.
* **Health Check Gerencial:** Integração direta com a API do **Prometheus** (via `PrometheusService`) para permitir que administradores consultem o estado atual dos dispositivos cadastrados.

## 🚀 Tecnologias e Padrões Aplicados

* **Java 17+** e **Spring Boot** (Web, Data JPA)
* **Padrão MVC / Camadas:** Separação estrita de responsabilidades (`controller`, `service`, `repository`, `model`, `exception`).
* **Hibernate:** Mapeamento Objeto-Relacional (ORM) para persistência consistente de dados.
* **Tratamento Global de Exceções:** Uso de `@ControllerAdvice` para interceptar erros e retornar respostas HTTP padronizadas e amigáveis para o cliente.
* **Integração REST externa:** Consumo de métricas da API do Prometheus.

## 📦 Estrutura dos Endpoints

A API expõe as seguintes rotas de gerenciamento:

* **`/api/devices`**: Permite criar, ler, atualizar e deletar os dispositivos de execução da rede, além de definir suas capacidades computacionais.
* **`/api/functions`**: Gerencia o catálogo de funções serverless e seus vínculos com os nós físicos.
* **`/api/logs`**: Registro e consulta do histórico de métricas ou eventos da infraestrutura.

## 🛠️ Como Executar Localmente

Certifique-se de ter o Java 17+ e o Maven instalados. Configure os dados de conexão do seu banco de dados relacional no arquivo `application.properties`.

1. Clone o repositório:
   ```bash
   git clone [https://github.com/RafaelPasCz/faas-manager.git](https://github.com/RafaelPasCz/faas-manager.git)