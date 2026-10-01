<div align="center">

# FlowOps

### Sistema de operação e acompanhamento de fluxos empresariais

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Status](https://img.shields.io/badge/Status-Em_desenvolvimento-F4A261?style=for-the-badge)

</div>

## Sobre o projeto

FlowOps é uma API para organizar empresas, usuários, projetos, tarefas e auditorias em um fluxo centralizado.

O sistema permitirá controlar acessos por empresa, distribuir tarefas, acompanhar o andamento dos projetos e validar entregas antes da conclusão.

## Funcionalidades planejadas

- Cadastro e gerenciamento de empresas.
- Cadastro de usuários e solicitação de vínculo empresarial.
- Controle de acesso por papel e cargo.
- Gerenciamento de projetos e membros.
- Criação e atribuição de tarefas.
- Fluxo de envio, auditoria e correção de tarefas.
- Cálculo automático do progresso dos projetos.
- Notificações de acesso e auditoria.

## Tecnologias utilizadas

- **Java 21**
- **Spring Boot 3.5.6**
- **Spring Security** com JWT e OAuth2
- **PostgreSQL**
- **Hibernate/JPA**
- **Swagger/OpenAPI**
- **Maven**

## Estrutura do backend

```text
src/main/java/br/com/flowopsbackend
├── controller
├── dto
│   ├── request
│   └── response
├── model
├── repository
├── security
└── service
