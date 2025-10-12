# Remoção dos Campos de Manutenção Preventiva das Máquinas

## 📋 Resumo da Implementação

Esta refatoração remove completamente os campos `dataProximaPreventiva` e `horasProximaPreventiva` das **Máquinas**, movendo-os para os **Relatórios**. Isso permite que cada relatório tenha sua própria programação de manutenção preventiva, ao invés de ser uma propriedade fixa da máquina.

## ✅ Arquivos Modificados

### 1. **Model Maquina** ✓
- **Arquivo**: `app/src/main/java/com/example/aprimortech/model/Maquina.kt`
- **Mudança**: Removidos campos `dataProximaPreventiva` e `horasProximaPreventiva`
- **Novo modelo**:
```kotlin
data class Maquina(
    val id: String = "",
    val clienteId: String = "",
    val fabricante: String = "",
    val numeroSerie: String = "",
    val modelo: String = "",
    val identificacao: String = "",
    val anoFabricacao: String = "",
    val codigoConfiguracao: String = ""
)
```

### 2. **MaquinaEntity** ✓
- **Arquivo**: `app/src/main/java/com/example/aprimortech/data/local/entity/MaquinaEntity.kt`
- **Mudança**: Removidos campos da entidade local do Room
- **Impacto**: Banco de dados local precisará de migração

### 3. **Relatorio Model** ✓
- **Arquivo**: `app/src/main/java/com/example/aprimortech/model/Relatorio.kt`
- **Mudança**: **ADICIONADOS** campos `dataProximaPreventiva` e `horasProximaPreventiva`
- **Novos campos**:
```kotlin
val dataProximaPreventiva: String? = null,
val horasProximaPreventiva: String? = null,
```

### 4. **RelatorioEntity** ✓
- **Arquivo**: `app/src/main/java/com/example/aprimortech/data/local/entity/RelatorioEntity.kt`
- **Mudança**: Adicionados campos na entidade local
- **Impacto**: Banco de dados local precisará de migração

### 5. **MaquinaRemoteDataSource** ✓
- **Arquivo**: `app/src/main/java/com/example/aprimortech/data/remote/MaquinaRemoteDataSource.kt`
- **Mudança**: Removidos campos dos métodos `insert()` e `update()` do Firebase
- **Impacto**: Os documentos no Firestore não terão mais esses campos

### 6. **MaquinasScreen** ⚠️ (Parcialmente Concluído)
- **Arquivo**: `app/src/main/java/com/example/aprimortech/MaquinasScreen.kt`
- **Mudanças necessárias**:
  - ✓ Remover campos do formulário de criação/edição
  - ✓ Remover exibição nos cards de listagem
  - ✓ Remover do dialog de visualização
  - ⚠️ **PENDENTE**: Remover completamente a função `ManutencaoPreventivaSection()`
  - ⚠️ **PENDENTE**: Remover função `mostrarDatePicker()`
  - ⚠️ **PENDENTE**: Ainda há variáveis `dataProximaPreventiva` e `horasProximaPreventiva` sendo inicializadas

## ⚠️ Arquivos que Precisam de Atenção

### 1. AppDatabase.kt
- **Necessário**: Adicionar migração do banco de dados local
- **Versão atual**: 8
- **Nova versão**: 9
- **Migração necessária**: Remover colunas `dataProximaPreventiva` e `horasProximaPreventiva` da tabela `maquinas` e adicionar à tabela `relatorios`

### 2. BuscarProximasManutencoesPreventivasUseCase.kt
- **Status**: Provavelmente **OBSOLETO**
- **Ação recomendada**: Remover ou adaptar para buscar por relatórios ao invés de máquinas

### 3. PreventiveMaintenanceNotificationWorker.kt
- **Status**: Precisa ser **ADAPTADO**
- **Ação recomendada**: Modificar para buscar manutenções preventivas dos relatórios

## 🔄 Próximos Passos Recomendados

1. **Limpar MaquinasScreen completamente**:
   - Remover todas as funções e variáveis relacionadas a manutenção preventiva
   - Remover a seção `ManutencaoPreventivaSection`
   - Remover imports não utilizados

2. **Criar migração do banco de dados**:
   ```kotlin
   private val MIGRATION_8_9 = object : Migration(8, 9) {
       override fun migrate(database: SupportSQLiteDatabase) {
           // Remover colunas da tabela maquinas
           database.execSQL("""
               CREATE TABLE maquinas_new (
                   id TEXT PRIMARY KEY NOT NULL,
                   clienteId TEXT NOT NULL,
                   fabricante TEXT NOT NULL,
                   numeroSerie TEXT NOT NULL,
                   modelo TEXT NOT NULL,
                   identificacao TEXT NOT NULL,
                   anoFabricacao TEXT NOT NULL,
                   codigoConfiguracao TEXT NOT NULL,
                   pendenteSincronizacao INTEGER NOT NULL,
                   timestampAtualizacao INTEGER NOT NULL
               )
           """)
           
           database.execSQL("""
               INSERT INTO maquinas_new SELECT 
                   id, clienteId, fabricante, numeroSerie, modelo, 
                   identificacao, anoFabricacao, codigoConfiguracao,
                   pendenteSincronizacao, timestampAtualizacao
               FROM maquinas
           """)
           
           database.execSQL("DROP TABLE maquinas")
           database.execSQL("ALTER TABLE maquinas_new RENAME TO maquinas")
           
           // Adicionar colunas na tabela relatorios
           database.execSQL("""
               ALTER TABLE relatorios 
               ADD COLUMN dataProximaPreventiva TEXT
           """)
           database.execSQL("""
               ALTER TABLE relatorios 
               ADD COLUMN horasProximaPreventiva TEXT
           """)
       }
   }
   ```

3. **Adicionar campos de manutenção preventiva nas telas de relatório**:
   - Criar seção na tela de criação de relatórios para preencher data e horas
   - Utilizar o mesmo componente `ManutencaoPreventivaSection` nas telas de relatório

4. **Limpar código Firebase**:
   - Verificar se há scripts de backup/migração do Firestore
   - Documentar que campos antigos nas máquinas existentes serão ignorados

## 📊 Impacto da Mudança

### Vantagens ✅
- Cada relatório pode ter sua própria programação de manutenção preventiva
- Histórico completo de manutenções preventivas programadas por relatório
- Modelo mais flexível e escalável
- Dados de preventiva vinculados ao contexto do atendimento

### Dados Existentes ⚠️
- **Firebase**: Máquinas existentes com campos `dataProximaPreventiva` e `horasProximaPreventiva` não causarão erro (campos serão ignorados)
- **Room (local)**: Migração automática removerá os campos
- **Recomendação**: Criar script de migração de dados se necessário preservar informações antigas

## 🧪 Testes Recomendados

1. ✅ Criar nova máquina sem campos de preventiva
2. ✅ Editar máquina existente
3. ✅ Sincronizar máquinas com Firebase
4. ⚠️ Criar relatório com campos de preventiva (após implementação)
5. ⚠️ Verificar compatibilidade com dados antigos

---

**Data da Implementação**: 12/10/2025
**Desenvolvedor**: AI Assistant
**Status**: 🟡 Parcialmente Implementado - Necessita finalização da MaquinasScreen e migração do banco

