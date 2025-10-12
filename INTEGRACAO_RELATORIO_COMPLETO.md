# Guia de Integração do RelatorioSharedViewModel

## Visão Geral

O `RelatorioSharedViewModel` foi criado para gerenciar o estado completo do relatório durante todas as etapas de criação. Este documento explica como integrar o ViewModel em cada tela do fluxo.

## Como Funciona

O ViewModel armazena temporariamente os dados de cada etapa e, ao final, gera um `RelatorioCompleto` com todos os campos preenchidos que será exibido na tela `RelatorioFinalizadoScreen`.

## Integração por Etapa

### Etapa 1: NovoRelatorioScreen (Cliente e Contatos)

Ao finalizar esta etapa, chame:

```kotlin
@Composable
fun NovoRelatorioScreen(
    navController: NavController,
    sharedViewModel: RelatorioSharedViewModel = viewModel()
) {
    // ... seus estados existentes ...
    
    // Ao clicar em "Avançar"
    Button(onClick = {
        // Salvar dados no ViewModel
        sharedViewModel.setClienteData(
            nome = clienteSelecionado?.nome ?: "",
            endereco = clienteSelecionado?.endereco ?: "",
            cidade = clienteSelecionado?.cidade ?: "",
            estado = clienteSelecionado?.estado ?: "",
            telefone = clienteSelecionado?.telefone ?: "",
            celular = clienteSelecionado?.celular ?: "",
            contatos = clienteSelecionado?.contatos?.map { 
                ContatoInfo(it.nome, it.setor ?: "", it.celular ?: "") 
            } ?: emptyList()
        )
        
        // Navegar para próxima etapa
        navController.navigate("dadosEquipamento/${clienteId}/${contatoNome}")
    })
}
```

### Etapa 2: RelatorioEquipamentoScreen (Dados do Equipamento)

```kotlin
@Composable
fun RelatorioEquipamentoScreen(
    navController: NavController,
    clienteId: String,
    sharedViewModel: RelatorioSharedViewModel = viewModel()
) {
    // ... seus estados de equipamento ...
    
    // Ao clicar em "Avançar"
    Button(onClick = {
        sharedViewModel.setEquipamentoData(
            fabricante = fabricante,
            numeroSerie = numeroSerie,
            codigoConfiguracao = codigoConfiguracao,
            modelo = modelo,
            identificacao = identificacao,
            anoFabricacao = anoFabricacao,
            codigoTinta = codigoTinta,
            codigoSolvente = codigoSolvente,
            dataProximaPreventiva = dataProximaPreventiva,
            horaProximaPreventiva = horaProximaPreventiva
        )
        
        navController.navigate("relatorioEtapa3?clienteId=$clienteId")
    })
}
```

### Etapa 3: RelatorioDefeitoServicosScreen

```kotlin
@Composable
fun RelatorioDefeitoServicosScreen(
    navController: NavController,
    clienteId: String,
    sharedViewModel: RelatorioSharedViewModel = viewModel()
) {
    // ... seus estados de defeitos e serviços ...
    
    Button(onClick = {
        sharedViewModel.setDefeitosServicos(
            defeitos = defeitosSelecionados,
            servicos = servicosSelecionados,
            observacoes = observacoes
        )
        
        navController.navigate("relatorioEtapa4?clienteId=$clienteId")
    })
}
```

### Etapa 4: RelatorioPecasScreen

```kotlin
@Composable
fun RelatorioPecasScreen(
    navController: NavController,
    clienteId: String,
    sharedViewModel: RelatorioSharedViewModel = viewModel()
) {
    // ... seus estados de peças ...
    
    Button(onClick = {
        val pecasData = pecasSelecionadas.map { 
            PecaData(
                codigo = it.codigo,
                descricao = it.descricao,
                quantidade = it.quantidade
            )
        }
        
        sharedViewModel.setPecas(pecasData)
        
        navController.navigate("relatorioEtapa5?clienteId=$clienteId")
    })
}
```

### Etapa 5: RelatorioHorasDeslocamentoScreen

```kotlin
@Composable
fun RelatorioHorasDeslocamentoScreen(
    navController: NavController,
    clienteId: String,
    sharedViewModel: RelatorioSharedViewModel = viewModel()
) {
    // ... seus estados de horas e deslocamento ...
    
    Button(onClick = {
        sharedViewModel.setHorasDeslocamento(
            horarioEntrada = horarioEntrada,
            horarioSaida = horarioSaida,
            valorHoraTecnica = valorHoraTecnica.toDoubleOrNull() ?: 0.0,
            totalHoras = calcularTotalHoras(),
            quantidadeKm = distanciaKm.toDoubleOrNull() ?: 0.0,
            valorPorKm = valorPorKm.toDoubleOrNull() ?: 0.0,
            valorPedagios = valorPedagios.toDoubleOrNull() ?: 0.0,
            valorTotalDeslocamento = calcularTotalDeslocamento()
        )
        
        navController.navigate("relatorioEtapa6?clienteId=$clienteId")
    })
}
```

### Etapa 6: RelatorioAssinaturaScreen

```kotlin
@Composable
fun RelatorioAssinaturaScreen(
    navController: NavController,
    sharedViewModel: RelatorioSharedViewModel = viewModel()
) {
    // ... seus estados de assinaturas ...
    
    Button(onClick = {
        // Salvar assinaturas
        sharedViewModel.setAssinaturas(
            assinaturaTecnico = assinaturaTecnicoBase64,
            assinaturaCliente = assinaturaClienteBase64,
            nomeTecnico = nomeTecnico
        )
        
        // Construir relatório completo
        val relatorioCompleto = sharedViewModel.buildRelatorioCompleto(
            relatorioId = UUID.randomUUID().toString(),
            dataRelatorio = getCurrentDate()
        )
        
        // Aqui você pode salvar no Firebase se necessário
        // salvarRelatorioNoFirebase(relatorioCompleto)
        
        // Navegar para tela de relatório finalizado
        navController.navigate("relatorioFinalizado")
    })
}
```

## Tela Final: RelatorioFinalizadoScreen

Esta tela já está configurada para ler automaticamente os dados do `RelatorioSharedViewModel`:

```kotlin
@Composable
fun RelatorioFinalizadoScreen(
    navController: NavController,
    sharedViewModel: RelatorioSharedViewModel = viewModel()
) {
    val relatorio by sharedViewModel.relatorioCompleto.collectAsState()
    
    // Se não houver dados, mostra mensagem de erro
    // Se houver dados, exibe o relatório completo
}
```

## Importante

1. **Mesmo ViewModel em todas as telas**: Use `viewModel()` sem parâmetros para garantir que seja a mesma instância compartilhada
2. **Limpar dados**: Ao confirmar ou excluir o relatório, chame `sharedViewModel.limparDados()` para resetar o estado
3. **Navegação**: Sempre navegue de forma sequencial pelas etapas para garantir que todos os dados sejam coletados

## Exemplo Completo de Fluxo

```
NovoRelatorioScreen 
  → setClienteData() 
  → navigate("dadosEquipamento")

RelatorioEquipamentoScreen 
  → setEquipamentoData() 
  → navigate("relatorioEtapa3")

RelatorioDefeitoServicosScreen 
  → setDefeitosServicos() 
  → navigate("relatorioEtapa4")

RelatorioPecasScreen 
  → setPecas() 
  → navigate("relatorioEtapa5")

RelatorioHorasDeslocamentoScreen 
  → setHorasDeslocamento() 
  → navigate("relatorioEtapa6")

RelatorioAssinaturaScreen 
  → setAssinaturas() 
  → buildRelatorioCompleto() 
  → navigate("relatorioFinalizado")

RelatorioFinalizadoScreen 
  → Exibe todos os dados coletados
  → Gera PDF completo
  → Salva no Firebase
```

## Geração de PDF

A tela `RelatorioFinalizadoScreen` já possui integração com o `PdfExporter.exportRelatorioCompleto()` que gera um PDF profissional com todos os campos do relatório, incluindo as assinaturas.

## Próximos Passos

Você precisa integrar o `RelatorioSharedViewModel` em cada uma das telas do fluxo de criação de relatório, conforme exemplos acima. Isso garantirá que todos os dados preenchidos apareçam corretamente na tela final.

