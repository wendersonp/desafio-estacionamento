## Instruções para configurar o projeto

Existem duas maneiras de executar este projeto:

### Execução Local utilizando Docker
    1. Instale o Docker e Docker-Compose, caso estes não estejam instalados
    2. Execute o comando `docker-compose up -d` dentro do diretório do projeto:

OBS: Entre em contato para receber os arquivos necessarios para execução no Docker.

### Execução remota:
Realize requisições [Neste Endereço](https://desafio-estacionamento.herokuapp.com/).

### Outras opções:
Se preferivel, você pode compilar e utilizar este projeto utilizando o Maven, detalhes [Neste Link](https://www.baeldung.com/spring-boot-run-maven-vs-executable-jar).


## Requisições suportadas pelo servidor

Legenda para parâmetros:

- **A**: Parâmetro Obrigatório
- *B*: Parâmetro Opcional
- ***C***: Parâmetro de URL

**OBS:** Os parâmetros relacionados a instantes recebem um *int* representando os segundos desde o **epoch** como entrada, visite (Epoch Converter)[https://www.epochconverter.com/] para obter valores de exemplo.

### Consultar quantidade de vagas disponíveis


- Função: Verifica a quantidade de vagas disponíveis para estacionamento
- Tipo de Requisição HTTP: GET
- Endereço: /vagas/quantidade
- Retorno: *int* Com a quantidade de vagas disponíveis

### Consultar as vagas disponíveis

- Função: Obtem dados relacionados as vagas disponíveis
- Tipo de Requisição HTTP: GET
- Endereço: /vagas
- Retorno: *List<VagaDTO>* com ID, e coordenadas geométricas X e Y da vaga. 

### Realizar estacionamento de um veículo

- Função: Realiza o registro de estacionamento de um veículo
- Tipo de Requisição HTTP: POST
- Endereço: /estacionar/{idVaga}
- Parâmetros:
    - ***idVaga***: Vaga a ser registrada
    - **placa_veiculo**: Placa do veículo a ser estacionada, deve estar no formato `XXX-0000`, ex.: **MNI-3638**
    - *data_entrada*: O instante (data+tempo) de entrada do veículo, em formato de *int* após época, caso não preenchido, o sistema registra o instante atual
    - *data_saida*: O instante de saída de veículo, caso não registrado, nenhuma data de saída é registrada e o sistema só ira permitir pagamento quando a saída for registrada separadamente.
- Retorno: .JSON do usuário requisitado

### Consultar lista de pagamentos pendentes

- Função: Obtem a lista de pagamentos pendentes, **Detalhe:** se o instante de saída ainda não foi registrado, o sistema retorna o possivel valor do estacionamento para o instante atual.
- Tipo de Requisição HTTP: GET
- Endereço: /pagamentos/pendentes
- Retorno: Lista de Registros com pagamentos pendentes.

### Registrar instante de saída do veículo

- Função: Registra momento de saída do veículo
- Tipo de Requisição HTTP: PUT
- Endereço: /saida/{idVaga}
- Parâmetros:
    - ***idVaga***: Vaga que o registro de estacionamento relacionado ocupa
    - *data_saida*: Instante de saída do veículo, se não preenchido, o sistema assume como sendo o instante atual acrescido de **cinco** minutos.
- Retorno: Registro relacionado com valores atualizados, informando inclusive o valor do estacionamento a ser pago.

### Registrar pagamento 

- Função: Informa que o pagamento do estacionamento na vaga relacionada foi realizado, o estacionamento é liberado apenas quando o pagamento é realizado. Se o instante de saída acontece em um momento futuro ao pagamento, o sistema agenda a liberação do estacionamento em questão para este momento futuro.
- Tipo de Requisição HTTP: PUT
- Endereço: /pagamento/{idVaga}
- Parâmetros:
    - ***idVaga***: Identificação da vaga de estacionamento cujo valor foi pago
- Retorno: Registro relacionado com os valores atualizados.

### Relatorio de negócios por vaga

- Função: Gera relatório com a quantidade de carros estacionados e o lucro obtido, com agrupamento por vaga de estacionamento
- Tipo de Requisição HTTP: GET
- Endereço: /relatorio/vaga
- Retorno: RelatorioDTO com classificação por vaga.

### Relatorio de negócios por data

- Função: Gera relatório com a quantidade de carros estacionados e o lucro obtido, com agrupamento por data de saída, esta requisição é paginada.
- Tipo de Requisição HTTP: GET
- Endereço: /relatorio/data
- Parametros: 
    - *pagina*: Pagina do relatório, se nao inserido, assume a pagina zero como a inicial
- Retorno: RelatorioDTO com classificação por data.

### Relatorio de negócios sem agrupamento

- Função: Gera relatório informando a quantidade total de carros que foram estacionados e lucro obtido.
- Tipo de Requisição HTTP: GET
- Endereço: /relatorio
- Retorno: RelatorioDTO com quantidade de carros estacionados e lucro total obtido.