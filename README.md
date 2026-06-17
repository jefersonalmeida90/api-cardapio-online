# Cardapio Online Java

Backend REST do Cardapio Online em Java com Spring Boot. O projeto expõe APIs para autenticacao administrativa, produtos, pedidos, clientes, dados do estabelecimento, integracoes externas e upload de imagens.

## Stack

- Java 21
- Spring Boot 3.5.3
- Spring Web
- Spring Validation
- Spring Data JPA
- Spring Security com JWT
- Liquibase
- SQL Server em ambiente principal
- H2 em testes
- Maven
- JaCoCo

## Estrutura

- `src/main/java`: codigo-fonte da aplicacao
- `src/main/resources`: configuracoes e migrations do Liquibase
- `src/tests/java`: testes unitarios, web e de integracao
- `src/tests/resources`: configuracao usada pelos testes

Principais camadas:

- `presentation`: controllers e tratamento de excecoes HTTP
- `application`: casos de uso, DTOs, servicos e utilitarios
- `domain`: entidades e enums de negocio
- `infrastructure`: configuracao, seguranca e suporte tecnico

## Requisitos

- Java 21
- Maven 3.9+
- SQL Server acessivel para execucao local da aplicacao

## Variaveis de ambiente

O projeto nao versiona mais segredos sensiveis no `application.yml`. Para subir a aplicacao fora do perfil de teste, configure:

```bash
DB_PASSWORD=senha-do-banco
ADMIN_PASSWORD=senha-do-admin
JWT_SECRET=segredo-com-no-minimo-32-caracteres
```

Variaveis opcionais:

```bash
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=CARDAPIOONLINE_DB;encrypt=true;trustServerCertificate=true
DB_USERNAME=sa
ADMIN_EMAIL=admin@cardapioonline.local
JWT_ISSUER=CardapioOnline
JWT_AUDIENCE=CardapioOnlineAdmin
JWT_EXPIRATION_MINUTES=400
```

Configuracoes padrao relevantes:

- Porta HTTP: `8080`
- `api.base-url`: `http://localhost:8080`
- Upload de arquivos: maximo de `10MB`
- CORS liberado para `http://localhost:4200`

## Como executar

Subindo em desenvolvimento:

```bash
mvn spring-boot:run
```

Gerando o artefato:

```bash
mvn clean package
java -jar target/cardapio-online-java-1.0.0.jar
```

## Banco de dados e migrations

O schema e controlado por Liquibase:

- arquivo principal: `src/main/resources/db/changelog/db.changelog-master.yaml`
- changesets: `src/main/resources/db/changelog/changes`

Na execucao local padrao, a aplicacao aponta para SQL Server. Nos testes, o projeto usa H2 em memoria com perfil `test`.

## Testes

Executar toda a suite:

```bash
mvn test
```

O projeto possui testes de:

- servicos
- controllers
- seguranca JWT
- integracao com Spring Boot + Liquibase + H2

## Seguranca

Autenticacao administrativa via JWT:

- login em `POST /api/auth/login`
- endpoints nao publicos exigem header `Authorization: Bearer <token>`

Rotas publicas atualmente:

- `POST /api/auth/login`
- `GET /api/products`
- `POST /api/orders`
- `GET /api/estabelecimento`
- `POST /api/clients`
- `POST /api/clients/authenticate`
- `POST /api/uploads/image`
- arquivos estaticos em `/uploads/**`

Observacoes de seguranca implementadas:

- segredos fora do repositrio por variaveis de ambiente
- filtro JWT stateless
- upload restrito a `PNG`, `JPG/JPEG` e `GIF`
- validacao do conteudo real da imagem antes de salvar

## Uploads

Endpoint:

- `POST /api/uploads/image`

Comportamento:

- aceita multipart com campo `file`
- salva os arquivos em `./uploads/<yyyyMMdd>/`
- publica os arquivos via `/uploads/**`
- gera extensao segura a partir do `Content-Type`

## Endpoints

### Auth

- `POST /api/auth/login`

### Estabelecimento

- `GET /api/estabelecimento`
- `PUT /api/estabelecimento`

### Produtos

- `GET /api/products`
- `POST /api/products`
- `PUT /api/products/{id}`
- `DELETE /api/products/{id}`

### Pedidos

- `GET /api/orders`
- `POST /api/orders`
- `PUT /api/orders/{id}/advance`
- `PUT /api/orders/{id}/cancel`

### Clientes

- `GET /api/clients`
- `POST /api/clients`
- `POST /api/clients/authenticate`

### Integracoes

- `GET /api/integrations`
- `PUT /api/integrations/ifood`
- `PUT /api/integrations/anotai`
- `PUT /api/integrations/ubereats`
- `PUT /api/integrations/99food`
- `PUT /api/integrations/aiagents`
- `PUT /api/integrations/whatsapp`
- `PUT /api/integrations/takeblip`
- `PUT /api/integrations/zenvia`

### Upload

- `POST /api/uploads/image`

## Observacoes

- A pasta de testes do projeto e `src/tests/java`, configurada explicitamente no `pom.xml`.
- O projeto gera cobertura com JaCoCo no ciclo `verify`.
- O endpoint de listagem de clientes usa agregacao em lote para evitar `N+1 queries` no calculo de pedidos e valor gasto.
