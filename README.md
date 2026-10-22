<div align="center">

# FlowOps API

Backend para gestão de empresas, projetos, tarefas e auditorias.

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.6-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-4169E1?style=flat-square&logo=postgresql&logoColor=white)

</div>

## Recursos

- Autenticação stateless com JWT.
- Cadastro de usuários e empresas.
- Aprovação de vínculos empresariais.
- Controle de acesso por papel e projeto.
- Gestão de projetos, membros e tarefas.
- Fluxo de auditoria, aprovação e correção.
- Progresso calculado por tarefas aprovadas.
- Notificações por e-mail configuráveis.
- Documentação OpenAPI e Swagger.

## Tecnologias

- Java 21
- Spring Boot 3.5.6
- Spring Security e OAuth2 Resource Server
- Spring Data JPA e Hibernate
- PostgreSQL
- Maven
- JUnit, Mockito e H2

## Configuração local

```powershell
$env:FLOWOPS_DB_PASSWORD="sua-senha"
.\mvnw.cmd spring-boot:run
```

A API utiliza `http://localhost:8080`. O Swagger fica disponível em `http://localhost:8080/swagger-ui.html`.

## Variáveis principais

| Variável | Finalidade |
|---|---|
| `FLOWOPS_DB_URL` | URL JDBC do PostgreSQL |
| `FLOWOPS_DB_USERNAME` | Usuário do banco |
| `FLOWOPS_DB_PASSWORD` | Senha do banco |
| `FLOWOPS_JWT_SECRET` | Chave JWT com pelo menos 32 bytes |
| `FLOWOPS_CORS_ORIGINS` | Origens permitidas |
| `FLOWOPS_MAIL_ENABLED` | Ativa o envio de e-mails |

## Validação

```powershell
.\mvnw.cmd test
```
