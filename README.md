Crawler
=======

### Resumo

Este documento descreve os principais passos para utilização do Crawler desenvolvido no Labic. O Crawler e uma ferramenta computacional capaz de capturar imagens e páginas de sites da internet e armazená-las no computador. Junto com  os dados coletados da internet, são criados vários arquivos de controle e armazenamento de informação. As principais informações sobre estes arquivos também são descritas neste documento.

```
versão e instruções adaptadas para Ubuntu e OSX
```

### Funcionalidade do código

O Crawler apresentado neste documento foi desenvolvido para capturar imagens e sites postados no Twitter pelos usuários. A descrição realizada neste documento é de uma versão beta do código. Versões futuras do código incluíram extrair informações postadas por usuários do Facebook assim como vídeos da internet.

### Compilação

Para compilar o código fonte é necessário ter instalado no computador o Java Development Kit (JDK). Se estiver instalado, coloque os arquivos `leitura_twitter.java`, `Crawler_simples2.java`, `PatternsFile.java` e `jsoup-1.7.2.jar` dentro de uma pasta.

Abra o terminal, acesse a pasta onde estão os arquivos e execute os comandos:

```
javac -encoding ISO-8859-1 leitura_twitter.java 
javac -encoding ISO-8859-1 -cp jsoup-1.7.2.jar PatternsFile.java Crawler_simples2.java 
```

Ao executar esse comando são gerados os seguintes arquivos dentro da pasta:

```
leitura_twitter.class
PatternFile.class
Crawler_simples2.class
```

Uma vez que o código fonte foi compilado, não é necessário compilar novamente, basta usar os arquivos gerados. Os arquivos gerados irão rodar em qualquer computador que tenha o ambiente Java instalado.

### Execução


Para utilizar o Crawler é necessário realizar uma série de procedimentos. Inicialmente é necessário ter um arquivo CSV com os tuites capturados dos usuários, que deve ser nomeado para tweets.csv.

O primeiro passo é executar o código leitura_twitter sobre o arquivo tweets.csv, para poder extrair os links citados pelos usuários. Coloque os dois arquivos no mesmo diretório, e execute:

```
java leitura_twitter tweets.csv
```

Este procedimento irá gerar um arquivo chamado links.csv que servirá de entrada para o Crawler. O arquivo `links.csv` contém o nome dos usuários e os links postados por eles, que foram extraídos dos tuites.

O próximo passo é executar o Crawler. Para isso, os arquivos `tweets.csv` e `links.csv` devem estar no mesmo diretório (coloque eles dentro de pasta chamada teste), e os arquivos `Crawler_simples2.class`, `PatternFile.class`,  `jsoup-1.7.2.jar`, `configs.txt` e `patterns.txt` devem estar juntos, mas não necessariamente no mesmo diretório dos arquivos csv.

Abra o arquivo `configs.txt`. Este arquivo apresenta vários parâmetros que são passados para o Crawler. O primeiro deles é o diretório onde estão os arquivos csv. Coloque o caminho completo nele. No caso testado ficou assim:

Pasta onde armazenar dados e onde está o arquivo com os links :

```
C:\Repositories_LABIC\teste
```

Para evitar erros, o caminho completo na pode acento, cedilha (ç) ou espaço entre as palavras. Assim, esses caminhos dariam erro:

```
C:\Repositories LABIC\teste
C:\Faça\teste
C:\Ándrea\teste
```

Dentro da pasta onde está o arquivo Crawler_simples2.class, execute o comando:

```
# java [options] Crawler_simples2 <config file> <dir>
java -Xmx512m -cp .:jsoup-1.7.2.jar Crawler_simples2 configs.txt .
```

A partir daí as imagens e os links começam a ser baixados para dentro da pasta teste.

Tão logo comecem a serem baixadas as imagens e os sites, três pastas são criadas dentro da pasta teste:

* **`images`**: onde são armazenadas as imagens baixadas pelo Crawler;

* **`pages`**: onde são armazenadas as páginas de internet da onde foram capturadas as imagens;

* **`hist`**: onde é armazenado um arquivo de controle do Crawler, caso ele seja parado no meio da execução.

Alguns arquivos são criados na pasta teste ao longo do tempo:

* **`links_reais.csv`**: os links postados nos tuites são em sua maioria mini URLs para economia de caracteres. Esse arquivo cria um vínculo entre cada mini URL e o link real;

* **`user_fotos.csv`**: esse arquivo armazena o vínculo entre o nome do usuário, a imagem salva na pasta images, o link de origem da imagem e em qual o site a imagem foi baixada;

* **`rel_user_photos.csv`**: a mesma coisa do arquivo anterior, com a diferença que cada usuário recebe uma identificação numérica diferente;

* **`Est_Imagens.csv`**: Este arquivo é gerado após todos os links no arquivo links.csv terem sido processados. Ele fornece, de forma decrescente, quais foram as imagens mais frequentes nos tuites.

* **`Est_Sites.csv`**: Este arquivo é gerado após todos os links no arquivo links.csv terem sido processados. Ele fornece, de forma decrescente, quais foram as homepages com imagens mais frequentes nos tuites.

* **`Est_Usuarios.csv`**: Este arquivo é gerado após todos os links no arquivo links.csv terem sido processados. Ele fornece, de forma decrescente, quais foram os usuários que mais postaram imagens nos tuites.

### Parada Forçada

O processo de captura pode ser longo e demandar muito tempo, e talvez seja necessário parar o procedimento por algum tempo. Para forçar a parada da captura, basta criar o seguinte arquivo dentro da pasta `hist`: **`stop.dat`**

O arquivo não precisa possuir conteúdo nenhum, ou seja, pode estar vazio.
Após algum tempo o programa irá automaticamente fechar.

Caso queira retornar a executar o programa, delete o arquivo `stop.dat`, e volte a executar o comando:

```
java -Xmx512m -cp .:jsoup-1.7.2.jar Crawler_simples2 configs.txt .
```

### Baixar Dados de Vários Arquivos tweets.csv ao Mesmo Tempo

É possível baixar dados de vários arquivos tweets.csv ao mesmo tempo. Para isso, coloque cada arquivo tweets.csv em uma pasta, execute o comando:

```
java leitura_twitter tweets.csv
```

Para obter o arquivo links.csv para cada arquivo.

Em seguida, crie um arquivo configs.txt para cada tweets.csv (em outras palavras, faça cópias do arquivo existente). Por exemplo, se forem usados três arquivos tweets.csv, localizados nos diretórios:

```
C:\Repositories_LABIC\teste1
C:\Repositories_LABIC\teste2
C:\Repositories_LABIC\teste3
```

Crie três arquivos configs.txt (configs_1.txt, configs_2.txt e configs_3.txt), onde para cada um terá:

* Pasta onde armazenar dados e onde está o arquivo com os links:	`C:\Repositories_LABIC\teste1`

* Pasta onde armazenar dados e onde está o arquivo com os links:	`C:\Repositories_LABIC\teste2`

* Pasta onde armazenar dados e onde está o arquivo com os links:	`C:\Repositories_LABIC\teste3`

Em seguida, abra três terminais e, em cada terminal, digite:

```
java -Xmx512m -cp jsoup-1.7.2.jar; Crawler_simples2 configs_1.txt
java -Xmx512m -cp jsoup-1.7.2.jar; Crawler_simples2 configs_2.txt
java -Xmx512m -cp jsoup-1.7.2.jar; Crawler_simples2 configs_3.txt
```

### Outras Configurações

Outras configurações podem ser encontradas dentro do arquivo `configs.txt`.
