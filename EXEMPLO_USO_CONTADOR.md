# Exemplo de Uso do RelatorioCounterInitializer

## Como Inicializar o Contador

### Opção 1: Na primeira vez que o app é aberto (recomendado)

Adicione este código no `onCreate()` da `MainActivity` ou em uma tela de configuração inicial:

```kotlin
// No MainActivity.onCreate() ou em algum init block
lifecycleScope.launch {
    try {
        val firestore = FirebaseFirestore.getInstance()
        
        // Inicializar o contador (só será criado se não existir)
        val sucesso = RelatorioCounterInitializer.inicializarContador(
            firestore = firestore,
            valorInicial = 0,
            forcarReinicializacao = false // Não sobrescreve se já existir
        )
        
        if (sucesso) {
            Log.d("App", "Sistema de numeração de relatórios pronto!")
        } else {
            Log.w("App", "Contador já estava inicializado")
        }
    } catch (e: Exception) {
        Log.e("App", "Erro ao configurar contador: ${e.message}")
    }
}
```

### Opção 2: Criar uma tela de Configuração/Admin

Você pode criar uma tela especial de administração onde o administrador pode:

```kotlin
@Composable
fun ConfiguracaoAdminScreen() {
    var valorInicial by remember { mutableStateOf("0") }
    var valorAtual by remember { mutableStateOf<Long?>(null) }
    var mensagem by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Configuração do Contador de Relatórios")
        
        // Mostrar valor atual
        Button(onClick = {
            scope.launch {
                valorAtual = RelatorioCounterInitializer.obterValorAtual()
                mensagem = if (valorAtual != null) {
                    "Valor atual: $valorAtual - Próximo: ${String.format("%04d", valorAtual!! + 1)}"
                } else {
                    "Contador não inicializado"
                }
            }
        }) {
            Text("Ver Valor Atual")
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Inicializar contador
        OutlinedTextField(
            value = valorInicial,
            onValueChange = { valorInicial = it },
            label = { Text("Valor Inicial") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        
        Button(onClick = {
            scope.launch {
                val valor = valorInicial.toLongOrNull() ?: 0
                val sucesso = RelatorioCounterInitializer.inicializarContador(
                    valorInicial = valor,
                    forcarReinicializacao = false
                )
                mensagem = if (sucesso) "Contador inicializado!" else "Erro ao inicializar"
            }
        }) {
            Text("Inicializar Contador")
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Mostrar mensagem
        if (mensagem.isNotEmpty()) {
            Text(mensagem, color = Color.Blue)
        }
    }
}
```

### Opção 3: Script One-Time (executar e remover)

Adicione temporariamente este código e execute o app uma vez:

```kotlin
// Em MainActivity.onCreate() - REMOVER APÓS EXECUTAR UMA VEZ
LaunchedEffect(Unit) {
    RelatorioCounterInitializer.inicializarContador()
}
```

## Verificar se o Contador Está Funcionando

Após inicializar, você pode testar:

```kotlin
lifecycleScope.launch {
    // Verificar valor atual
    val valorAtual = RelatorioCounterInitializer.obterValorAtual()
    Log.d("Test", "Contador em: $valorAtual")
    
    // Criar um relatório de teste e verificar o número gerado
    val novoRelatorio = Relatorio(
        id = "",
        numeroRelatorio = "", // Será gerado automaticamente
        clienteId = "teste",
        maquinaId = "teste"
        // ... outros campos
    )
    
    val relatorioId = relatorioRepository.salvarRelatorio(novoRelatorio)
    val relatorioSalvo = relatorioRepository.buscarRelatorioPorId(relatorioId)
    
    Log.d("Test", "Relatório criado com número: ${relatorioSalvo?.numeroRelatorio}")
}
```

## Migração de Relatórios Existentes (Opcional)

Se você já tem relatórios sem número, pode atribuir números retroativamente:

```kotlin
suspend fun migrarRelatoriosExistentes() {
    val firestore = FirebaseFirestore.getInstance()
    val relatorios = firestore.collection("relatorios")
        .whereEqualTo("numeroRelatorio", "")
        .get()
        .await()
    
    var contador = 1L
    
    relatorios.documents.forEach { doc ->
        val numeroFormatado = String.format("%04d", contador)
        doc.reference.update("numeroRelatorio", numeroFormatado).await()
        Log.d("Migracao", "Relatório ${doc.id} recebeu número $numeroFormatado")
        contador++
    }
    
    // Atualizar o contador global
    RelatorioCounterInitializer.definirValor(contador - 1)
    
    Log.d("Migracao", "Migração concluída! $contador relatórios atualizados")
}
```

## Troubleshooting

### "Transaction failed: Document not found"
**Causa**: O contador não foi inicializado.
**Solução**: Execute `RelatorioCounterInitializer.inicializarContador()`

### "currentNumber não encontrado"
**Causa**: Documento existe mas não tem o campo correto.
**Solução**: Execute `RelatorioCounterInitializer.inicializarContador(forcarReinicializacao = true)`

### Números estão duplicados
**Causa Improvável**: Se acontecer, pode ser problema de transação.
**Solução**: Verifique os logs do Firebase e considere executar a migração manual.

## Importante

- ✅ Execute a inicialização ANTES de criar qualquer relatório
- ✅ Não é necessário executar a inicialização toda vez que o app abre
- ✅ O contador é global para todos os usuários
- ⚠️ Use `forcarReinicializacao=true` com cuidado (pode causar duplicação)

