# INE 5421

## Sobre o trabalho
Esta atividade foi desenvolvida na disciplina de `Linguagens Formais e Compiladores` com o 
propósito de implementação de um analisador léxico (T1) e um analisador sintático (T2)

### Requisitos
Para a execução deste trabalho, é necessária utilização de uma versão Java $\ge 25$, além da
instalação do gerenciados de pacotes `maven` com apontamento para `$JAVA_HOME` na versão 25.

### Como instalar o projeto?

No diretório raiz deste projeto se encontra um arquivo `pom.xml` responsável por direcionar todas
dependencias utilizadas ao longo da execução dos analisadores criado. Através disso, para dar-se a
inicialização e execução do projeto é necessário o seguinte comando no diretório raiz deste trabalho

```bash
~$: mvn clean install [-D skipTests]
```

Agora, ambos diretórios `T1` e `T2` possuem um subdiretório `/target`. O executável alvo se localiza
em `/target/<T1 | T2>-1.-SNAPSHOT-fat.jar`

### Como executar

A execução se dá por linha de comando utilizando da biblioteca picocli. Sua execução consta através da
seguinte estrutura

```bash
~$: java -jar <T1|T2>/target/<T1|T2>-1.0-SNAPSHOT-fat.jar <args>
```

#### Argumentos T1

O trabalho 1 possui 2 parâmetros de entrada

```bash
~$: java -jar T1/target/T1-1.0-SNAPSHOT-fat.jar path_to_regex path_to_source 
```
onde 
- *path_to_regex*: caminho para o arquivo com os regexes a serem utilizados
- *path_to_source*: caminho para o arquivo com o texto de entrada
- 
#### Exemplo de execução T1

O seguinte comando executa o trabalho 1, gerando seus resultados em T1/output
    
```bash
~$: java -jar T1/target/T1-1.0-SNAPSHOT-fat.jar T1/src/test/resources/test1/regexes.txt T1/src/test/resources/test1/input.txt 
```

#### Argumentos T2

O trabalho 2 possui 3 parâmetros de entrada

```bash
~$: java -jar T1/target/T1-1.0-SNAPSHOT-fat.jar path_to_grammar path_to_reserved path_to_input 
```
onde
- *path_to_grammar*: caminho para o arquivo com a gramática a ser utilizada
- *path_to_reserved*: caminho para o arquivo com as palavras reservadas
- *path_to_input*: caminho para o arquivo com o texto de entrada

#### Exemplo de execução T2

O seguinte comando executa o trabalho 2, gerando seus resultados em T2/output

```bash
~$: java -jar T2/target/T2-1.0-SNAPSHOT-fat.jar T2/src/test/resources/test3/grammar.txt T2/src/test/resources/test3/reserved.txt T2/src/test/resources/test3/input.txt
```


#### Créditos
Este trabalho foi desenvolvido pelos alunos
- Bruno Bianchi Pagani
- João Gabriel Feres
- Hélcio Valentim Neto

