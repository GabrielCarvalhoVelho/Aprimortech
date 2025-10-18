package com.example.aprimortech

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.RelatorioViewModel
import com.example.aprimortech.ui.viewmodel.RelatorioViewModelFactory
import com.example.aprimortech.ui.viewmodel.ClienteViewModel
import com.example.aprimortech.ui.viewmodel.ClienteViewModelFactory
import com.example.aprimortech.model.Relatorio
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

private val Brand = Color(0xFF1A4A5C)
private val appBackground = Color(0xFFF5F5F5)
private val displayDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))

private fun parseDateSafe(dateStr: String?): Date? {
    if (dateStr.isNullOrBlank()) return null
    val patterns = listOf("yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd")
    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.isLenient = false
            val parsed = sdf.parse(dateStr)
            if (parsed != null) return parsed
        } catch (_: Exception) { }
    }
    return null
}

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

    // ViewModel para clientes (resolver nome a partir do clienteId)
    val clienteViewModel: ClienteViewModel = viewModel(
        factory = ClienteViewModelFactory(
            buscarClientesUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarClientesUseCase,
            salvarClienteUseCase = (LocalContext.current.applicationContext as AprimortechApplication).salvarClienteUseCase,
            excluirClienteUseCase = (LocalContext.current.applicationContext as AprimortechApplication).excluirClienteUseCase,
            sincronizarClientesUseCase = (LocalContext.current.applicationContext as AprimortechApplication).sincronizarClientesUseCase
        )
    )
    val clientes by clienteViewModel.clientes.collectAsState()
    val clienteNameById = remember(clientes) { clientes.associate { it.id to it.nome } }

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
        clienteViewModel.sincronizarDados()
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
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logo_aprimortech),
                        contentDescription = "Logo Aprimortech",
                        modifier = Modifier.height(40.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Brand)
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
                        Icon(Icons.Default.NotificationImportant, contentDescription = "Próximas Manutenções", tint = Brand)
                    }
                    // Botão de sincronizar
                    IconButton(onClick = { viewModel.sincronizarRelatorios() }) {
                        Icon(Icons.Default.Sync, contentDescription = "Sincronizar", tint = Brand)
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
                .background(appBackground)
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
                        val clientName = clienteNameById[relatorio.clienteId]
                            ?: relatorio.clienteId.takeIf { it.isNotBlank() } ?: "Cliente ${relatorio.id.take(6)}"
                        val parsed = parseDateSafe(relatorio.dataRelatorio)
                        val formattedDate = parsed?.let { displayDateFormatter.format(it) } ?: relatorio.dataRelatorio

                        RelatorioCard(
                            relatorio = relatorio,
                            clientName = clientName,
                            formattedDate = formattedDate,
                            onDetails = {
                                // Navegar para detalhes do relatório (id devidamente codificado)
                                val encodedId = URLEncoder.encode(relatorio.id, "UTF-8")
                                navController.navigate("relatorioFinalizado?relatorioId=$encodedId")
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
                // ✅ ATUALIZADO: Agora busca preventivas dos RELATÓRIOS
                Text("Funcionalidade temporariamente desabilitada. As manutenções preventivas agora são registradas nos relatórios.")
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
    clientName: String,
    formattedDate: String,
    onDetails: () -> Unit,
    onDelete: () -> Unit,
    onExportPdf: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(clientName, style = MaterialTheme.typography.titleMedium, color = Brand, modifier = Modifier.weight(1f))
                Text(formattedDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (relatorio.descricaoServico.isNotBlank()) {
                Text(relatorio.descricaoServico, style = MaterialTheme.typography.bodyMedium)
            }

            if (relatorio.numeroNotaFiscal != null) {
                Text("NF: ${relatorio.numeroNotaFiscal}", style = MaterialTheme.typography.bodySmall)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão Detalhes (substitui o antigo Editar)
                TextButton(onClick = onDetails) {
                    Text("Detalhes")
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
