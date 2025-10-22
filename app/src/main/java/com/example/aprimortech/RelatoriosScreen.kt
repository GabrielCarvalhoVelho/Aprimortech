package com.example.aprimortech

import android.content.Intent
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
import androidx.compose.foundation.Image
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.RelatorioViewModel
import com.example.aprimortech.ui.viewmodel.RelatorioViewModelFactory
import com.example.aprimortech.ui.viewmodel.ClienteViewModel
import com.example.aprimortech.ui.viewmodel.ClienteViewModelFactory
import com.example.aprimortech.model.Relatorio
import com.example.aprimortech.model.RelatorioCompleto
import com.example.aprimortech.data.repository.RelatorioRepository
import com.example.aprimortech.data.repository.ClienteRepository
import com.example.aprimortech.data.repository.MaquinaRepository
import com.example.aprimortech.data.repository.PecaRepository
import com.example.aprimortech.data.repository.DefeitoRepository
import com.example.aprimortech.data.repository.ServicoRepository
import com.example.aprimortech.data.repository.TintaRepository
import com.example.aprimortech.data.repository.SolventeRepository
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
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
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showDeleteDialog by remember { mutableStateOf<Relatorio?>(null) }
    var showProximasManutencoes by remember { mutableStateOf(false) }

    // Estados do ViewModel
    val relatorios by viewModel.relatorios.collectAsState()
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

    // Repositories & application references (used to build RelatorioCompleto for export)
    val app = remember { context.applicationContext as AprimortechApplication }
    val relatorioRepository = remember { app.relatorioRepository }
    val clienteRepository = remember { app.clienteRepository }
    val maquinaRepository = remember { app.maquinaRepository }
    val pecaRepository = remember { app.pecaRepository }
    val defeitoRepository = remember { app.defeitoRepository }
    val servicoRepository = remember { app.servicoRepository }
    val tintaRepository = remember { app.tintaRepository }
    val solventeRepository = remember { app.solventeRepository }

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

    // Computa manutenções preventivas a partir dos relatórios (próximos 30 dias)
    val proximasManutencoesLocal = remember(relatorios) {
        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val today = todayCal.time
        val millisIn30Days = 30L * 24 * 60 * 60 * 1000
        relatorios.mapNotNull { rel ->
            val parsed = parseDateSafe(rel.dataProximaPreventiva)
            if (parsed != null && parsed.time >= today.time && (parsed.time - today.time) <= millisIn30Days) {
                rel to parsed
            } else null
        }.sortedBy { it.second.time }
            .map { it.first }
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
                            containerColor = if (proximasManutencoesLocal.isNotEmpty()) MaterialTheme.colorScheme.error else Color.Transparent
                        ) {
                            if (proximasManutencoesLocal.isNotEmpty()) {
                                Text("${proximasManutencoesLocal.size}")
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
                                scope.launch {
                                    try {
                                        val relComp = carregarRelatorioCompleto(
                                            relatorioId = relatorio.id,
                                            relatorioRepository = relatorioRepository,
                                            clienteRepository = clienteRepository,
                                            maquinaRepository = maquinaRepository,
                                            pecaRepository = pecaRepository,
                                            defeitoRepository = defeitoRepository,
                                            servicoRepository = servicoRepository,
                                            tintaRepository = tintaRepository,
                                            solventeRepository = solventeRepository
                                        )
                                        val uri = PdfExporter.exportRelatorioCompleto(context, relComp)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/pdf"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Erro ao exportar PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
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
                if (proximasManutencoesLocal.isEmpty()) {
                    Text("Nenhuma manutenção preventiva nos próximos 30 dias.")
                } else {
                    Column {
                        proximasManutencoesLocal.forEach { rel ->
                            val name = clienteNameById[rel.clienteId]
                                ?: rel.clienteId.takeIf { it.isNotBlank() } ?: "Cliente ${rel.id.take(6)}"
                            val dateStr = parseDateSafe(rel.dataProximaPreventiva)
                                ?.let { displayDateFormatter.format(it) }
                                ?: rel.dataProximaPreventiva ?: "N/A"
                            // calcular dias restantes quando possível
                            val diasRestantes = parseDateSafe(rel.dataProximaPreventiva)?.let { date ->
                                val diff = (date.time - Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                                }.time.time) / (24 * 60 * 60 * 1000)
                                diff.toInt()
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, style = MaterialTheme.typography.bodyMedium)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    diasRestantes?.let { d ->
                                        Text(if (d == 0) "Hoje" else "Em $d dias", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
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

// --- helper to build RelatorioCompleto (adapted from RelatorioFinalizadoScreen)
@Suppress("UNUSED_PARAMETER")
private suspend fun carregarRelatorioCompleto(
    relatorioId: String,
    relatorioRepository: RelatorioRepository,
    clienteRepository: ClienteRepository,
    maquinaRepository: MaquinaRepository,
    pecaRepository: PecaRepository,
    defeitoRepository: DefeitoRepository,
    servicoRepository: ServicoRepository,
    tintaRepository: TintaRepository,
    solventeRepository: SolventeRepository
): RelatorioCompleto {
    val relatorio = relatorioRepository.buscarRelatorioPorId(relatorioId)
        ?: throw Exception("Relatório não encontrado")

    val cliente = clienteRepository.buscarClientePorId(relatorio.clienteId)
        ?: throw Exception("Cliente não encontrado")

    val maquina = if (relatorio.maquinaId.isNotBlank() && relatorio.maquinaId != "maquinas" && relatorio.maquinaId.length > 10) {
        maquinaRepository.buscarMaquinaPorId(relatorio.maquinaId)
    } else {
        maquinaRepository.buscarMaquinasPorCliente(cliente.id).firstOrNull()
    }

    val pecasInfo = if (relatorio.pecasUtilizadas.isNotEmpty()) {
        relatorio.pecasUtilizadas.mapNotNull { pecaMap ->
            try {
                com.example.aprimortech.model.PecaInfo(
                    codigo = pecaMap["codigo"] as? String ?: "",
                    descricao = pecaMap["descricao"] as? String ?: "",
                    quantidade = when (val qtd = pecaMap["quantidade"]) {
                        is Int -> qtd
                        is Long -> qtd.toInt()
                        is Double -> qtd.toInt()
                        is String -> qtd.toIntOrNull() ?: 0
                        else -> 0
                    }
                )
            } catch (_: Exception) {
                null
            }
        }
    } else if (relatorio.pecaIds.isNotEmpty()) {
        relatorio.pecaIds.mapNotNull { pecaId ->
            pecaRepository.buscarPecaPorId(pecaId)?.let { peca ->
                com.example.aprimortech.model.PecaInfo(codigo = peca.codigo, descricao = peca.descricao, quantidade = 1)
            }
        }
    } else emptyList()

    val defeitos = if (relatorio.defeitosIdentificados.isNotEmpty()) {
        relatorio.defeitosIdentificados
    } else {
        relatorio.descricaoServico.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    val servicos = if (relatorio.servicosRealizados.isNotEmpty()) {
        relatorio.servicosRealizados
    } else {
        relatorio.recomendacoes.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    val observacoesFinais = relatorio.observacoesDefeitosServicos.ifEmpty {
        relatorio.observacoes ?: ""
    }

    val totalHoras = calcularHorasTecnicas(relatorio.horarioEntrada, relatorio.horarioSaida)

    val contatos = cliente.contatos.map { contato ->
        com.example.aprimortech.model.ContatoInfo(nome = contato.nome, setor = contato.setor ?: "", celular = contato.celular ?: "")
    }

    return RelatorioCompleto(
        id = relatorio.id,
        dataRelatorio = relatorio.dataRelatorio,
        clienteNome = cliente.nome,
        clienteEndereco = cliente.endereco,
        clienteCidade = cliente.cidade,
        clienteEstado = cliente.estado,
        clienteTelefone = cliente.telefone,
        clienteCelular = cliente.celular,
        clienteContatos = contatos,
        equipamentoFabricante = maquina?.fabricante ?: "Não especificado",
        equipamentoNumeroSerie = maquina?.numeroSerie ?: "N/A",
        equipamentoCodigoConfiguracao = maquina?.codigoConfiguracao ?: "N/A",
        equipamentoModelo = maquina?.modelo ?: "N/A",
        equipamentoIdentificacao = maquina?.identificacao ?: "N/A",
        equipamentoAnoFabricacao = maquina?.anoFabricacao ?: "",
        equipamentoCodigoTinta = relatorio.codigoTinta ?: "",
        equipamentoCodigoSolvente = relatorio.codigoSolvente ?: "",
        equipamentoDataProximaPreventiva = relatorio.dataProximaPreventiva ?: "",
        equipamentoHoraProximaPreventiva = relatorio.horasProximaPreventiva ?: "",
        defeitos = defeitos,
        servicos = servicos,
        pecas = pecasInfo,
        horarioEntrada = relatorio.horarioEntrada ?: "",
        horarioSaida = relatorio.horarioSaida ?: "",
        valorHoraTecnica = relatorio.valorHoraTecnica ?: 0.0,
        totalHorasTecnicas = totalHoras,
        quantidadeKm = relatorio.distanciaKm ?: 0.0,
        valorPorKm = relatorio.valorDeslocamentoPorKm ?: 0.0,
        valorPedagios = relatorio.valorPedagios ?: 0.0,
        valorTotalDeslocamento = relatorio.valorDeslocamentoTotal ?: 0.0,
        assinaturaTecnico1 = relatorio.assinaturaTecnico1,
        assinaturaCliente1 = relatorio.assinaturaCliente1,
        assinaturaTecnico2 = relatorio.assinaturaTecnico2,
        assinaturaCliente2 = relatorio.assinaturaCliente2,
        observacoes = observacoesFinais,
        nomeTecnico = "Técnico Aprimortech"
    )
}

private fun calcularHorasTecnicas(horarioEntrada: String?, horarioSaida: String?): Double {
    if (horarioEntrada.isNullOrEmpty() || horarioSaida.isNullOrEmpty()) return 0.0
    return try {
        val (horaEntrada, minutoEntrada) = horarioEntrada.split(":").map { it.toInt() }
        val (horaSaida, minutoSaida) = horarioSaida.split(":").map { it.toInt() }
        val minutosEntrada = horaEntrada * 60 + minutoEntrada
        val minutosSaida = horaSaida * 60 + minutoSaida
        val diferencaMinutos = if (minutosSaida >= minutosEntrada) {
            minutosSaida - minutosEntrada
        } else {
            (24 * 60 - minutosEntrada) + minutosSaida
        }
        diferencaMinutos / 60.0
    } catch (_: Exception) {
        0.0
    }
}
