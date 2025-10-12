package com.example.aprimortech

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aprimortech.data.repository.*
import com.example.aprimortech.model.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioFinalizadoScreen(
    navController: NavController,
    relatorioId: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var relatorioCompleto by remember { mutableStateOf<RelatorioCompleto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val app = remember { context.applicationContext as AprimortechApplication }
    val relatorioRepository = remember { app.relatorioRepository }
    val clienteRepository = remember { app.clienteRepository }
    val maquinaRepository = remember { app.maquinaRepository }
    val pecaRepository = remember { app.pecaRepository }
    val defeitoRepository = remember { app.defeitoRepository }
    val servicoRepository = remember { app.servicoRepository }
    val tintaRepository = remember { TintaRepository() }
    val solventeRepository = remember { SolventeRepository() }

    LaunchedEffect(relatorioId) {
        if (relatorioId.isNullOrEmpty()) {
            errorMessage = "ID do relatório não fornecido"
            isLoading = false
            return@LaunchedEffect
        }

        try {
            isLoading = true
            relatorioCompleto = carregarRelatorioCompleto(
                relatorioId = relatorioId,
                relatorioRepository = relatorioRepository,
                clienteRepository = clienteRepository,
                maquinaRepository = maquinaRepository,
                pecaRepository = pecaRepository,
                defeitoRepository = defeitoRepository,
                servicoRepository = servicoRepository,
                tintaRepository = tintaRepository,
                solventeRepository = solventeRepository
            )
            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Erro ao carregar relatório: ${e.message}"
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatório Finalizado") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (relatorioCompleto != null) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val uri = PdfExporter.exportRelatorioCompleto(context, relatorioCompleto!!)
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
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Compartilhar")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading -> LoadingView()
                errorMessage != null -> ErrorView(errorMessage!!)
                relatorioCompleto != null -> RelatorioContent(relatorioCompleto = relatorioCompleto!!)
                else -> ErrorView("Relatório não encontrado")
            }
        }
    }
}

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
                PecaInfo(
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
            } catch (e: Exception) {
                null
            }
        }
    } else if (relatorio.pecaIds.isNotEmpty()) {
        relatorio.pecaIds.mapNotNull { pecaId ->
            pecaRepository.buscarPecaPorId(pecaId)?.let { peca ->
                PecaInfo(codigo = peca.codigo, descricao = peca.descricao, quantidade = 1)
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
        ContatoInfo(nome = contato.nome, setor = contato.setor ?: "", celular = contato.celular ?: "")
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
    } catch (e: Exception) {
        0.0
    }
}

@Composable
private fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator()
            Text("Carregando relatório...")
        }
    }
}

@Composable
private fun ErrorView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
            Text(text = "⚠️", fontSize = 48.sp)
            Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun RelatorioContent(relatorioCompleto: RelatorioCompleto) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection(relatorioCompleto)
        Divider()
        ClienteSection(relatorioCompleto)
        Divider()
        EquipamentoSection(relatorioCompleto)
        Divider()
        if (relatorioCompleto.pecas.isNotEmpty()) {
            PecasSection(relatorioCompleto.pecas)
            Divider()
        }
        if (relatorioCompleto.defeitos.isNotEmpty()) {
            DefeitosSection(relatorioCompleto.defeitos)
            Divider()
        }
        if (relatorioCompleto.servicos.isNotEmpty()) {
            ServicosSection(relatorioCompleto.servicos)
            Divider()
        }
        HorasTecnicasSection(relatorioCompleto)
        Divider()
        DeslocamentoSection(relatorioCompleto)
        Divider()
        AssinaturasSection(relatorioCompleto)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HeaderSection(relatorio: RelatorioCompleto) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "RELATÓRIO DE MANUTENÇÃO", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(text = "Data: ${relatorio.dataRelatorio}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            if (relatorio.id.isNotEmpty()) {
                Text(text = "ID: ${relatorio.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun ClienteSection(relatorio: RelatorioCompleto) {
    SectionCard(title = "DADOS DO CLIENTE") {
        InfoRow(label = "Nome", value = relatorio.clienteNome)
        InfoRow(label = "Endereço", value = relatorio.clienteEndereco)
        InfoRow(label = "Cidade/Estado", value = "${relatorio.clienteCidade} - ${relatorio.clienteEstado}")
        InfoRow(label = "Telefone", value = relatorio.clienteTelefone)
        InfoRow(label = "Celular", value = relatorio.clienteCelular)
        if (relatorio.clienteContatos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Contatos:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            relatorio.clienteContatos.forEach { contato ->
                Text(text = "• ${contato.nome} - ${contato.setor} - ${contato.celular}", fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp))
            }
        }
    }
}

@Composable
private fun EquipamentoSection(relatorio: RelatorioCompleto) {
    SectionCard(title = "DADOS DO EQUIPAMENTO") {
        InfoRow(label = "Fabricante", value = relatorio.equipamentoFabricante)
        InfoRow(label = "Modelo", value = relatorio.equipamentoModelo)
        InfoRow(label = "Número de Série", value = relatorio.equipamentoNumeroSerie)
        InfoRow(label = "Identificação", value = relatorio.equipamentoIdentificacao)
        InfoRow(label = "Código Configuração", value = relatorio.equipamentoCodigoConfiguracao)
        InfoRow(label = "Ano Fabricação", value = relatorio.equipamentoAnoFabricacao)
        InfoRow(label = "Código Tinta", value = relatorio.equipamentoCodigoTinta)
        InfoRow(label = "Código Solvente", value = relatorio.equipamentoCodigoSolvente)
        InfoRow(label = "Data Próxima Preventiva", value = relatorio.equipamentoDataProximaPreventiva)
        InfoRow(label = "Horas até Próxima Preventiva", value = relatorio.equipamentoHoraProximaPreventiva)
    }
}

@Composable
private fun DefeitosSection(defeitos: List<String>) {
    SectionCard(title = "DEFEITOS ENCONTRADOS") {
        defeitos.forEachIndexed { index, defeito ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                Text(text = "${index + 1}.", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
                Text(text = defeito, fontSize = 14.sp, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ServicosSection(servicos: List<String>) {
    SectionCard(title = "SERVIÇOS REALIZADOS") {
        servicos.forEachIndexed { index, servico ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                Text(text = "${index + 1}.", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
                Text(text = servico, fontSize = 14.sp, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PecasSection(pecas: List<PecaInfo>) {
    SectionCard(title = "PEÇAS UTILIZADAS") {
        Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp)) {
            Text(text = "Código", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
            Text(text = "Descrição", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(2f))
            Text(text = "Qtd", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(40.dp))
        }
        pecas.forEach { peca ->
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text(text = peca.codigo, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(text = peca.descricao, fontSize = 12.sp, modifier = Modifier.weight(2f))
                Text(text = peca.quantidade.toString(), fontSize = 12.sp, modifier = Modifier.width(40.dp))
            }
            Divider()
        }
    }
}

@Composable
private fun HorasTecnicasSection(relatorio: RelatorioCompleto) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    val valorTotalHorasTecnicas = relatorio.totalHorasTecnicas * relatorio.valorHoraTecnica
    SectionCard(title = "HORAS TÉCNICAS") {
        Column(modifier = Modifier.fillMaxWidth()) {
            InfoRow(label = "Entrada", value = relatorio.horarioEntrada)
            InfoRow(label = "Saída", value = relatorio.horarioSaida)
        }
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow(label = "Total de Horas", value = String.format("%.2f h", relatorio.totalHorasTecnicas))
        InfoRow(label = "Valor por Hora", value = numberFormat.format(relatorio.valorHoraTecnica))
        InfoRow(label = "Valor Total", value = numberFormat.format(valorTotalHorasTecnicas), highlight = true)
    }
}

@Composable
private fun DeslocamentoSection(relatorio: RelatorioCompleto) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    SectionCard(title = "DESLOCAMENTO") {
        InfoRow(label = "Distância (KM)", value = String.format("%.2f km", relatorio.quantidadeKm))
        InfoRow(label = "Valor por KM", value = numberFormat.format(relatorio.valorPorKm))
        InfoRow(label = "Pedágios", value = numberFormat.format(relatorio.valorPedagios))
        InfoRow(label = "Total Deslocamento", value = numberFormat.format(relatorio.valorTotalDeslocamento), highlight = true)
    }
}

@Composable
private fun ObservacoesSection(observacoes: String) {
    SectionCard(title = "OBSERVAÇÕES") {
        Text(text = observacoes, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun AssinaturasSection(relatorio: RelatorioCompleto) {
    SectionCard(title = "ASSINATURAS") {
        // Exibir assinaturas dos técnicos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Assinatura Técnico 1", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val bitmapTecnico1 = remember(relatorio.assinaturaTecnico1) {
                    relatorio.assinaturaTecnico1?.let { assinatura ->
                        val imageBytes = Base64.decode(assinatura, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
                    }
                }
                if (bitmapTecnico1 != null) {
                    Image(bitmapTecnico1, contentDescription = "Assinatura Técnico 1", modifier = Modifier.size(120.dp), contentScale = ContentScale.Fit)
                } else {
                    Text("Sem assinatura", fontSize = 10.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Assinatura Técnico 2", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val bitmapTecnico2 = remember(relatorio.assinaturaTecnico2) {
                    relatorio.assinaturaTecnico2?.let { assinatura ->
                        val imageBytes = Base64.decode(assinatura, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
                    }
                }
                if (bitmapTecnico2 != null) {
                    Image(bitmapTecnico2, contentDescription = "Assinatura Técnico 2", modifier = Modifier.size(120.dp), contentScale = ContentScale.Fit)
                } else {
                    Text("Sem assinatura", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        // Exibir assinaturas dos clientes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Assinatura Cliente 1", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val bitmapCliente1 = remember(relatorio.assinaturaCliente1) {
                    relatorio.assinaturaCliente1?.let { assinatura ->
                        val imageBytes = Base64.decode(assinatura, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
                    }
                }
                if (bitmapCliente1 != null) {
                    Image(bitmapCliente1, contentDescription = "Assinatura Cliente 1", modifier = Modifier.size(120.dp), contentScale = ContentScale.Fit)
                } else {
                    Text("Sem assinatura", fontSize = 10.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Assinatura Cliente 2", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val bitmapCliente2 = remember(relatorio.assinaturaCliente2) {
                    relatorio.assinaturaCliente2?.let { assinatura ->
                        val imageBytes = Base64.decode(assinatura, Base64.DEFAULT)
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
                    }
                }
                if (bitmapCliente2 != null) {
                    Image(bitmapCliente2, contentDescription = "Assinatura Cliente 2", modifier = Modifier.size(120.dp), contentScale = ContentScale.Fit)
                } else {
                    Text("Sem assinatura", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Divider()
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, highlight: Boolean = false) {
    val displayValue = if (value.isBlank()) "—" else value
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "$label:", fontSize = 12.sp, fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(text = displayValue, fontSize = 12.sp, fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}
