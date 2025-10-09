package com.example.aprimortech

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.RelatorioViewModel
import com.example.aprimortech.ui.viewmodel.RelatorioViewModelFactory
import com.example.aprimortech.model.Relatorio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatoriosScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: RelatorioViewModel = viewModel(
        factory = RelatorioViewModelFactory(
            buscarRelatoriosUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarRelatoriosUseCase,
            salvarRelatorioUseCase = (LocalContext.current.applicationContext as AprimortechApplication).salvarRelatorioUseCase,
            excluirRelatorioUseCase = (LocalContext.current.applicationContext as AprimortechApplication).excluirRelatorioUseCase,
            sincronizarRelatoriosUseCase = (LocalContext.current.applicationContext as AprimortechApplication).sincronizarRelatoriosUseCase,
            buscarProximasManutencoesPreventivasUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarProximasManutencoesPreventivasUseCase,
            relatorioRepository = (LocalContext.current.applicationContext as AprimortechApplication).relatorioRepository
        )
    )
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showDeleteDialog by remember { mutableStateOf<Relatorio?>(null) }
    var showProximasManutencoes by remember { mutableStateOf(false) }

    // Estados do ViewModel
    val relatorios by viewModel.relatorios.collectAsState()
    val proximasManutencoes by viewModel.proximasManutencoes.collectAsState()
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()

    // Feedback toast
    LaunchedEffect(mensagemOperacao) {
        mensagemOperacao?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.limparMensagem()
        }
    }

    // Carregar dados ao entrar
    LaunchedEffect(Unit) {
        // Removendo chamada para método privado carregarRelatorios()
        viewModel.sincronizarRelatorios()
    }

    val filteredRelatorios = remember(searchQuery.text, relatorios) {
        if (searchQuery.text.isBlank()) {
            relatorios
        } else {
            relatorios.filter {
                it.descricaoServico.contains(searchQuery.text, ignoreCase = true) ||
                it.clienteId.contains(searchQuery.text, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Relatórios") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    // Botão de próximas manutenções
                    IconButton(onClick = { showProximasManutencoes = true }) {
                        Badge(
                            containerColor = if (proximasManutencoes.isNotEmpty()) MaterialTheme.colorScheme.error else Color.Transparent
                        ) {
                            if (proximasManutencoes.isNotEmpty()) {
                                Text("${proximasManutencoes.size}")
                            }
                        }
                        Icon(Icons.Default.NotificationImportant, contentDescription = "Próximas Manutenções")
                    }
                    // Botão de sincronizar
                    IconButton(onClick = { viewModel.sincronizarRelatorios() }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sincronizar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("novoRelatorio") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Novo Relatório")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por descrição ou cliente") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            if (relatorios.isEmpty()) {
                // Estado vazio
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Nenhum relatório encontrado")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { navController.navigate("novoRelatorio") }) {
                            Text("Criar Primeiro Relatório")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRelatorios) { relatorio ->
                        RelatorioCard(
                            relatorio = relatorio,
                            onEdit = {
                                // Passar dados para edição
                                navController.currentBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("relatorioEdit", relatorio)
                                navController.navigate("novoRelatorio")
                            },
                            onDelete = { showDeleteDialog = relatorio },
                            onExportPdf = {
                                // TODO: Implementar exportação PDF
                                Toast.makeText(context, "Exportação PDF em desenvolvimento", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog de confirmação de exclusão
    showDeleteDialog?.let { relatorio ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Excluir relatório") },
            text = { Text("Tem certeza que deseja excluir este relatório?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.excluirRelatorio(relatorio.id)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog de próximas manutenções
    if (showProximasManutencoes) {
        AlertDialog(
            onDismissRequest = { showProximasManutencoes = false },
            title = { Text("Próximas Manutenções Preventivas") },
            text = {
                if (proximasManutencoes.isEmpty()) {
                    Text("Não há manutenções preventivas programadas para os próximos 30 dias.")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(proximasManutencoes) { maquina ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        maquina.nomeMaquina,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        "Cliente: ${maquina.clienteId}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        "Data: ${maquina.dataProximaPreventiva}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showProximasManutencoes = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}

@Composable
private fun RelatorioCard(
    relatorio: Relatorio,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onExportPdf: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(relatorio.descricaoServico, style = MaterialTheme.typography.titleMedium)
            Text("Cliente: ${relatorio.clienteId}", style = MaterialTheme.typography.bodyMedium)
            Text("Data: ${relatorio.dataRelatorio}", style = MaterialTheme.typography.bodySmall)
            if (relatorio.numeroNotaFiscal != null) {
                Text("NF: ${relatorio.numeroNotaFiscal}", style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // EDITAR
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                    Spacer(Modifier.width(4.dp))
                    Text("Editar")
                }

                Row {
                    // PDF
                    IconButton(onClick = onExportPdf) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF")
                    }
                    // EXCLUIR
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Excluir",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatoriosScreenPreview() {
    AprimortechTheme {
        RelatoriosScreen(navController = rememberNavController())
    }
}
