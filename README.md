# Biblioteca Java

Sistema de gerenciamento de biblioteca desenvolvido em **Java**, com **Programação Orientada a Objetos (POO)** e persistência em **banco SQL H2**.

A aplicação funciona pelo terminal e permite gerenciar livros, leitores, empréstimos e devoluções. O código está organizado em camadas para facilitar o estudo e a manutenção.

## Funcionalidades

| Área | Operações |
| --- | --- |
| Livros | Cadastrar, listar, editar título e autor, excluir |
| Leitores | Cadastrar, listar, editar nome e e-mail, excluir |
| Empréstimos | Registrar retirada e definir prazo de devolução |
| Devoluções | Encerrar empréstimos e repor exemplares no estoque |
| Consultas | Visualizar histórico, empréstimos abertos e atrasos |

Os dados permanecem salvos após o encerramento do programa.

## Tecnologias

- **Java 17 ou superior:** linguagem da aplicação.
- **JDBC:** comunicação com o banco de dados.
- **H2 2.3.232:** banco SQL local, sem servidor separado.
- **Maven:** alternativa para compilação e empacotamento.
- **PowerShell:** scripts para executar, compilar e testar no Windows.

## Como executar

### Versão pronta no Windows

1. Instale o **Java 17 ou superior**.
2. Extraia o ZIP do projeto.
3. Abra o PowerShell dentro da pasta `biblioteca-java`.
4. Execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

O ZIP inclui o programa compilado e o driver H2. Escolha uma opção do menu e siga as perguntas exibidas.

Se esses arquivos estiverem ausentes, o script tentará compilar o projeto. Nesse caso, será necessário ter o **JDK 17+** instalado; baixar o driver também exige internet.

### Compilar o código

Para desenvolver ou recompilar, use um **JDK 17 ou superior**, com `java`, `javac` e `jar` disponíveis no terminal.

```powershell
java -version
javac -version
powershell -ExecutionPolicy Bypass -File .\build.ps1
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

A compilação gera `build/biblioteca.jar`. O driver fica em `build/lib/h2-2.3.232.jar` e é baixado do Maven Central quando estiver ausente.

Sempre recompile depois de alterar o código-fonte.

### Alternativa com Maven

Com JDK 17+ e Maven instalados, execute na raiz do projeto:

```sh
mvn clean package
java -jar target/biblioteca-1.0.0.jar
```

O JAR gerado pelo Maven inclui o driver H2. A primeira compilação precisa de internet para baixar as dependências.

**Os testes de integração possuem um executor próprio:** `mvn clean package` não executa essas verificações. Consulte a seção de testes abaixo.

## Exemplo de uso

1. Escolha **1 — Cadastrar livro** e informe título, autor, ISBN e quantidade de exemplares.
2. Escolha **5 — Cadastrar leitor** e informe nome e e-mail.
3. Anote os IDs apresentados. As opções **2** e **6** também permitem consultá-los.
4. Escolha **9 — Emprestar livro**, informe os IDs do livro e do leitor e um prazo, como `7` dias.
5. Consulte o ID do empréstimo na opção **12 — Empréstimos abertos**.
6. Escolha **10 — Devolver livro** e informe o ID do empréstimo.
7. Consulte o registro na opção **11 — Histórico**.
8. Escolha **0 — Sair** para encerrar.

## Estrutura do projeto

```text
biblioteca-java/
├── src/
│   ├── main/
│   │   ├── java/br/com/biblioteca/
│   │   │   ├── Main.java
│   │   │   ├── db/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/schema.sql
│   └── test/java/br/com/biblioteca/IntegrationTest.java
├── sql/schema.sql
├── build.ps1
├── run.ps1
├── pom.xml
└── README.md
```

| Componente | Responsabilidade |
| --- | --- |
| `Main` | Menu, leitura das entradas e apresentação dos resultados |
| `model` | Entidades `Entidade`, `Livro`, `Leitor` e `Emprestimo` |
| `repository` | Interface genérica e operações de persistência JDBC |
| `service` | Validações, regras de negócio e transações |
| `db` | Conexões e inicialização das tabelas |

Os repositórios cuidam dos cadastros. O serviço coordena empréstimos e devoluções porque essas operações alteram várias tabelas na mesma transação.

## POO aplicada

| Conceito | Exemplo no código |
| --- | --- |
| Encapsulamento | Atributos privados e métodos de acesso em `Livro` e `Leitor` |
| Herança | `Livro` e `Leitor` estendem a classe abstrata `Entidade` |
| Abstração | `Entidade` define uma base comum e `Repositorio<T>` define um contrato de persistência |
| Polimorfismo | As entidades sobrescrevem `resumo()` e os repositórios implementam a mesma interface |
| Composição | `BibliotecaService` utiliza `Banco` e reúne os repositórios |

`Emprestimo` é um `record` imutável usado para representar os resultados das consultas.

## Banco de dados

O H2 funciona dentro da aplicação. As tabelas são criadas automaticamente na inicialização.

| Tabela | Dados principais |
| --- | --- |
| `livros` | ID, título, autor, ISBN, total e exemplares disponíveis |
| `leitores` | ID, nome e e-mail |
| `emprestimos` | ID, livro, leitor, retirada, prazo e devolução |

Cada empréstimo referencia um livro e um leitor por chaves estrangeiras. Um livro e um leitor podem aparecer em vários empréstimos.

O esquema inclui chaves primárias, ISBN e e-mail únicos, restrições de quantidade e datas e índices nas referências de empréstimos.

### Persistência e backup

Na configuração padrão, os dados ficam em:

```text
data/biblioteca.mv.db
```

O caminho é relativo à pasta de execução. Os scripts iniciam o programa na raiz do projeto. Ao executar pelo Maven, use essa mesma pasta para acessar o mesmo banco.

Para fazer backup, feche a aplicação e copie o arquivo do banco. Na configuração padrão, mantenha apenas uma instância do programa aberta por vez.

### Configuração opcional

Para escolher outro arquivo de banco no PowerShell:

```powershell
$env:BIBLIOTECA_DB_URL = "jdbc:h2:./data/minha-biblioteca"
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

Para restaurar o padrão:

```powershell
Remove-Item Env:\BIBLIOTECA_DB_URL
```

O usuário da conexão é `sa`, sem senha, em uma configuração voltada ao estudo local.

### Consulta SQL de exemplo

```sql
SELECT
    e.id,
    l.titulo,
    u.nome AS leitor,
    e.realizado_em,
    e.previsto_em
FROM emprestimos e
JOIN livros l ON l.id = e.livro_id
JOIN leitores u ON u.id = e.leitor_id
WHERE e.devolvido_em IS NULL
ORDER BY e.previsto_em;
```

Para acessar o console SQL, feche a aplicação e execute na raiz do projeto:

```powershell
java -cp build/lib/h2-2.3.232.jar org.h2.tools.Shell
```

Informe a URL `jdbc:h2:./data/biblioteca`, o driver `org.h2.Driver`, o usuário `sa` e deixe a senha vazia. Esse comando exige o driver na pasta `build/lib/`.

### Manutenção do esquema

Mantenha estes arquivos sincronizados ao alterar a estrutura do banco:

- `sql/schema.sql`: usado pelo script de compilação.
- `src/main/resources/schema.sql`: usado pelo Maven.

A inicialização cria tabelas ausentes, mas não migra tabelas existentes.

## Regras de negócio

- Os campos obrigatórios não podem ficar em branco e devem respeitar os limites de tamanho.
- E-mails passam por validação básica de formato e são convertidos para minúsculas.
- ISBN e e-mail devem ser únicos. O ISBN é tratado como texto, sem validação de dígito verificador.
- A quantidade inicial de exemplares deve ser positiva.
- O prazo de empréstimo deve estar entre **1 e 60 dias**.
- O empréstimo exige leitor cadastrado e exemplar disponível.
- A retirada reduz o estoque disponível; a devolução o repõe.
- Um empréstimo pode ser devolvido apenas uma vez.
- Livros e leitores com histórico não podem ser excluídos, mesmo após a devolução.
- Um empréstimo aberto é marcado como atrasado quando o prazo é anterior à data atual.

Empréstimos e devoluções usam transações: se uma etapa falhar, a operação é desfeita. A atualização condicional do estoque impede que duas retiradas simultâneas consumam o mesmo último exemplar.

Os valores recebidos do usuário são vinculados às consultas por `PreparedStatement`.

## Testes

Com JDK 17+ instalado, execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1 -Test
```

O comando compila e executa **23 verificações de integração** em um H2 em memória, isolado dos dados do usuário.

A cobertura inclui validação, unicidade, edição, exclusão, integridade referencial, estoque, devolução repetida, histórico e disputa simultânea pelo último exemplar.

Resultado esperado:

```text
OK: 23 verificações de integração.
```

## Solução de problemas

| Situação | Como resolver |
| --- | --- |
| `java` não é reconhecido | Confira a instalação do Java e sua inclusão no PATH |
| `javac` ou `jar` não é reconhecido | Instale um JDK 17+ e confira o PATH |
| Falha ao baixar o H2 | Verifique o acesso à internet e ao Maven Central e compile novamente |
| Banco em uso | Encerre outras instâncias da aplicação e o console SQL |
| ISBN ou e-mail duplicado | Use outro valor ou edite o registro existente |
| Exclusão bloqueada | O cadastro possui histórico e precisa ser preservado |
| Alterações no código não aparecem | Execute `build.ps1` novamente |

## Escopo e evolução

O projeto é uma base de estudo de **Java, POO, JDBC e SQL**, com interface de terminal.

A versão atual não inclui autenticação, interface web, API, multas, renovação, edição da quantidade total após o cadastro ou migrações automáticas. Esses recursos podem ser implementados como extensões do projeto.
