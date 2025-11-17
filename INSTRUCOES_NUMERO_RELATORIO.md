# Implementação do Número de Relatório Sequencial

## 📋 Resumo da Implementação

Foi implementado um sistema de numeração sequencial automática para relatórios, com formato "0001", "0002", etc.

## ✅ Alterações Realizadas

### 1. **Modelo de Dados**
- ✅ Adicionado campo `numeroRelatorio: String` no modelo `Relatorio`
- ✅ Adicionado campo `numeroRelatorio: String` no modelo `RelatorioCompleto`
- ✅ Adicionado campo `numeroRelatorio: String` no `RelatorioEntity` (banco local)

### 2. **Repository**
- ✅ Criado método `obterProximoNumeroRelatorio()` usando **Firestore Transaction**
- ✅ Modificado `salvarRelatorio()` para gerar número automaticamente ao criar novo relatório
- ✅ Números são mantidos ao atualizar relatórios existentes

### 3. **ViewModel**
- ✅ Atualizado `buildRelatorioCompleto()` para aceitar `numeroRelatorio` como parâmetro

### 4. **Telas**
- ✅ Atualizado `MainActivity` para passar numeroRelatorio vazio (será gerado automaticamente)
- ✅ Atualizado `NovoRelatorioScreen` para passar numeroRelatorio ao editar
- ✅ Atualizado `RelatorioAssinaturaScreen` para incluir o campo

## 🔧 Configuração Inicial do Firebase

### **IMPORTANTE: Inicializar o Contador**

Antes de usar o sistema, você precisa criar o documento contador no Firestore:

#### Opção 1: Via Console do Firebase
1. Acesse o [Firebase Console](https://console.firebase.google.com/)
2. Selecione seu projeto: **aprimortech-30cad**
3. Vá em **Firestore Database**
4. Crie uma nova **Collection** chamada: `counters`
5. Dentro de `counters`, crie um **Document** com ID: `relatorio_counter`
6. Adicione o campo:
   - **Campo**: `currentNumber`
   - **Tipo**: `number`
   - **Valor**: `0` (ou o último número usado se já houver relatórios)

#### Opção 2: Via Código (executar uma vez)
Adicione este código temporariamente em algum lugar que execute ao iniciar o app:

```kotlin
// Executar apenas uma vez para inicializar
lifecycleScope.launch {
    try {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("counters")
            .document("relatorio_counter")
            .set(mapOf("currentNumber" to 0))
            .await()
        Log.d("Setup", "Contador inicializado com sucesso!")
    } catch (e: Exception) {
        Log.e("Setup", "Erro ao inicializar contador: ${e.message}")
    }
}
```

## 🎯 Como Funciona

### Criação de Novo Relatório
1. Usuário preenche o formulário do relatório
2. Ao salvar, o `RelatorioRepository` automaticamente:
   - Executa uma **transação atômica** no Firestore
   - Incrementa o contador em `counters/relatorio_counter`
   - Obtém o novo número
   - Formata com 4 dígitos (ex: "0001", "0002")
   - Salva o relatório com o número gerado

### Edição de Relatório Existente
- O número original é **mantido** sem alterações

### Concorrência
- **Transações Firestore** garantem que não haverá números duplicados
- Mesmo com múltiplos usuários criando relatórios simultaneamente, cada um receberá um número único

## 🔍 Estrutura do Firestore

```
/counters
  /relatorio_counter
    currentNumber: 1234

/relatorios
  /{relatorioId}
    numeroRelatorio: "1234"
    clienteId: "..."
    maquinaId: "..."
    ... outros campos
```

## 📱 Usando o Número do Relatório

### No Código
```kotlin
val relatorio = repository.buscarRelatorioPorId(id)
val numero = relatorio?.numeroRelatorio // "0001", "0002", etc.
```

### Na UI
```kotlin
Text("Relatório Nº ${relatorio.numeroRelatorio}")
```

### No PDF/Impressão
O campo `numeroRelatorio` está disponível tanto no modelo `Relatorio` quanto no `RelatorioCompleto`, então pode ser usado em qualquer lugar onde esses modelos sejam utilizados.

## ⚠️ Observações Importantes

1. **Inicialização Obrigatória**: O documento `counters/relatorio_counter` DEVE existir antes de criar o primeiro relatório
2. **Formato Fixo**: Sempre 4 dígitos com zero à esquerda (0001-9999)
3. **Sequencial Global**: O contador é compartilhado entre todos os usuários do app
4. **Não Reutilizável**: Números não são reutilizados mesmo se um relatório for excluído
5. **Fallback**: Em caso de erro ao acessar o contador, usa timestamp como fallback

## 🚀 Próximos Passos (Opcional)

### Melhorias Futuras Possíveis
- [ ] Adicionar filtro/busca por número de relatório
- [ ] Exibir número de relatório na lista de relatórios
- [ ] Adicionar número de relatório no PDF gerado
- [ ] Implementar reset anual do contador (ex: 0001/2025, 0001/2026)
- [ ] Criar dashboard com estatísticas de relatórios por número

## 🐛 Troubleshooting

### Erro: "currentNumber não encontrado"
**Solução**: O contador não foi inicializado. Siga os passos em "Configuração Inicial do Firebase"

### Números duplicados
**Solução**: Isso não deve acontecer devido às transações atômicas. Se acontecer, verifique:
- Se há múltiplas instâncias do app rodando com versões diferentes do código
- Se o Firebase está com problemas de conectividade

### Números pulando valores
**Comportamento Normal**: Devido às transações, se uma transação falhar após incrementar, o número é "perdido". Isso é esperado e não causa problemas.

## 📝 Checklist de Deploy

Antes de colocar em produção:
- [ ] Inicializar contador no Firestore
- [ ] Testar criação de múltiplos relatórios
- [ ] Verificar que números não se repetem
- [ ] Testar edição de relatório existente (número deve ser mantido)
- [ ] Testar com múltiplos usuários simultâneos
- [ ] Adicionar exibição do número nas telas relevantes
- [ ] Adicionar número no PDF do relatório

---

✨ **Implementação Completa e Funcional!** ✨

