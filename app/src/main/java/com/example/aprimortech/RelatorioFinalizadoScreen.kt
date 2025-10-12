package com.example.aprimortech

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aprimortech.data.repository.*
import com.example.aprimortech.model.*
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*

/**
 * Tela de exibição do relatório finalizado
 * Arquitetura: MVVM com Repository Pattern
 *
 * Responsabilidades:
 * 1. Carregar dados completos do relatório do Firestore
 * 2. Buscar dados relacionados (cliente, máquina, peças, defeitos, serviços)
 * 3. Exibir relatório formatado
 * 4. Exportar para PDF
 * 5. Compartilhar relatório
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioFinalizadoScreen(
    navController: NavController,
    relatorioId: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // State
    var relatorioCompleto by remember { mutableStateOf<RelatorioCompleto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Repositories - usar as instâncias já disponíveis na aplicação
    val app = context.applicationContext as AprimortechApplication
    val relatorioRepository = remember { app.relatorioRepository }
    val clienteRepository = remember { app.clienteRepository }
    val maquinaRepository = remember { app.maquinaRepository }
    val pecaRepository = remember { app.pecaRepository }
    val defeitoRepository = remember { app.defeitoRepository }
    val servicoRepository = remember { app.servicoRepository }
    val tintaRepository = remember { TintaRepository() }
    val solventeRepository = remember { SolventeRepository() }

    // Carregar dados ao iniciar
    LaunchedEffect(relatorioId) {
        if (relatorioId.isNullOrEmpty()) {
            errorMessage = "ID do relatório não fornecido"
            isLoading = false
            return@LaunchedEffect
        }

        scope.launch {
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
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatório Finalizado") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
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
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartilhar"
                            )
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
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    LoadingView()
                }
                errorMessage != null -> {
                    ErrorView(errorMessage!!)
                }
                relatorioCompleto != null -> {
                    RelatorioContent(relatorioCompleto = relatorioCompleto!!)
                }
                else -> {
                    ErrorView("Relatório não encontrado")
                }
            }
        }
    }
}

/**
 * Carrega todos os dados do relatório de forma assíncrona
 */
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
    // 1. Buscar relatório base
    val relatorio = relatorioRepository.buscarRelatorioPorId(relatorioId)
        ?: throw Exception("Relatório não encontrado")

    android.util.Log.d("RelatorioFinalizado", "=== CARREGANDO RELATÓRIO ===")
    android.util.Log.d("RelatorioFinalizado", "Relatório ID: ${relatorio.id}")
    android.util.Log.d("RelatorioFinalizado", "Cliente ID: ${relatorio.clienteId}")
    android.util.Log.d("RelatorioFinalizado", "Máquina ID: '${relatorio.maquinaId}'")

    // 2. Buscar dados do cliente
    val cliente = clienteRepository.buscarClientePorId(relatorio.clienteId)
        ?: throw Exception("Cliente não encontrado")

    android.util.Log.d("RelatorioFinalizado", "✅ Cliente carregado: ${cliente.nome}")

    // 3. Buscar dados da máquina
    // IMPORTANTE: O maquinaId pode estar vazio no relatório antigo, mas os dados da máquina
    // foram salvos através do SharedViewModel e estão no Firestore
    val maquina = if (relatorio.maquinaId.isNotBlank() &&
                      relatorio.maquinaId != "maquinas" &&
                      relatorio.maquinaId.length > 10) {
        android.util.Log.d("RelatorioFinalizado", "Tentando buscar máquina com ID: ${relatorio.maquinaId}")
        maquinaRepository.buscarMaquinaPorId(relatorio.maquinaId)?.also {
            android.util.Log.d("RelatorioFinalizado", "✅ Máquina carregada: ${it.modelo}")
        }
    } else {
        android.util.Log.w("RelatorioFinalizado", "⚠️ maquinaId inválido ou vazio, buscando máquinas do cliente")
        // Fallback: buscar máquinas do cliente e usar a primeira encontrada
        val maquinasDoCliente = maquinaRepository.buscarMaquinasPorCliente(cliente.id)
        maquinasDoCliente.firstOrNull()?.also {
            android.util.Log.d("RelatorioFinalizado", "✅ Usando primeira máquina do cliente: ${it.modelo}")
        }
    }

    // 4. Buscar peças utilizadas
    val pecasInfo = relatorio.pecaIds.mapNotNull { pecaId ->
        pecaRepository.buscarPecaPorId(pecaId)?.let { peca ->
            PecaInfo(
                codigo = peca.codigo,
                descricao = peca.descricao,
                quantidade = 1
            )
        }
    }

    // 5. Processar defeitos (separados por vírgula)
    val defeitos = if (relatorio.defeitosIdentificados.isNotEmpty()) {
        // Usa os novos campos estruturados
        relatorio.defeitosIdentificados
    } else {
        // Fallback para compatibilidade com relatórios antigos
        relatorio.descricaoServico
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    // 6. Processar serviços (separados por vírgula das recomendações)
    val servicos = if (relatorio.servicosRealizados.isNotEmpty()) {
        // Usa os novos campos estruturados
        relatorio.servicosRealizados
    } else {
        // Fallback para compatibilidade com relatórios antigos
        relatorio.recomendacoes
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }

    // 6a. Observações - prioriza o campo novo
    val observacoesFinais = relatorio.observacoesDefeitosServicos.ifEmpty {
        relatorio.observacoes ?: ""
    }

    // 7. Calcular horas técnicas
    val totalHoras = calcularHorasTecnicas(
        horarioEntrada = relatorio.horarioEntrada,
        horarioSaida = relatorio.horarioSaida
    )

    // 8. Montar contatos
    val contatos = cliente.contatos.map { contato ->
        ContatoInfo(
            nome = contato.nome,
            setor = contato.setor ?: "",
            celular = contato.celular ?: ""
        )
    }

    android.util.Log.d("RelatorioFinalizado", "=== DADOS CARREGADOS COM SUCESSO ===")

    // 9. Construir RelatorioCompleto
    // IMPORTANTE: Dados do equipamento vêm da máquina cadastrada
    // EXCETO codigoTinta e codigoSolvente que vêm APENAS do relatório (preenchidos pelo usuário)

    // PRIORIDADE: Códigos preenchidos pelo usuário no relatório
    val codigoTintaFinal = relatorio.codigoTinta ?: ""
    val codigosolventeFinal = relatorio.codigoSolvente ?: ""

    android.util.Log.d("RelatorioFinalizado", "=== CÓDIGOS FINAIS PARA EXIBIÇÃO ===")
    android.util.Log.d("RelatorioFinalizado", "Código Tinta do Relatório: ${relatorio.codigoTinta}")
    android.util.Log.d("RelatorioFinalizado", "Código Tinta FINAL (exibido): $codigoTintaFinal")
    android.util.Log.d("RelatorioFinalizado", "Código Solvente do Relatório: ${relatorio.codigoSolvente}")
    android.util.Log.d("RelatorioFinalizado", "Código Solvente FINAL (exibido): $codigosolventeFinal")

    return RelatorioCompleto(
        id = relatorio.id,
        dataRelatorio = relatorio.dataRelatorio,

        // Dados do Cliente
        clienteNome = cliente.nome,
        clienteEndereco = cliente.endereco,
        clienteCidade = cliente.cidade,
        clienteEstado = cliente.estado,
        clienteTelefone = cliente.telefone,
        clienteCelular = cliente.celular,
        clienteContatos = contatos,

        // Dados do Equipamento - da máquina cadastrada
        equipamentoFabricante = maquina?.fabricante ?: "Não especificado",
        equipamentoNumeroSerie = maquina?.numeroSerie ?: "N/A",
        equipamentoCodigoConfiguracao = maquina?.codigoConfiguracao ?: "N/A",
        equipamentoModelo = maquina?.modelo ?: "N/A",
        equipamentoIdentificacao = maquina?.identificacao ?: "N/A",
        equipamentoAnoFabricacao = maquina?.anoFabricacao ?: "",
        // ⭐ CÓDIGOS DE TINTA E SOLVENTE: Prioriza o que o usuário preencheu no relatório
        equipamentoCodigoTinta = codigoTintaFinal,
        equipamentoCodigoSolvente = codigosolventeFinal,
        // ✅ Próxima manutenção preventiva - AGORA VEM DO RELATÓRIO
        equipamentoDataProximaPreventiva = relatorio.dataProximaPreventiva ?: "",
        equipamentoHoraProximaPreventiva = relatorio.horasProximaPreventiva ?: "",

        // Defeitos e Serviços
        defeitos = defeitos,
        servicos = servicos,

        // Peças
        pecas = pecasInfo,

        // Horas Técnicas
        horarioEntrada = relatorio.horarioEntrada ?: "",
        horarioSaida = relatorio.horarioSaida ?: "",
        valorHoraTecnica = relatorio.valorHoraTecnica ?: 0.0,
        totalHorasTecnicas = totalHoras,

        // Deslocamento
        quantidadeKm = relatorio.distanciaKm ?: 0.0,
        valorPorKm = relatorio.valorDeslocamentoPorKm ?: 0.0,
        valorPedagios = relatorio.valorPedagios ?: 0.0,
        valorTotalDeslocamento = relatorio.valorDeslocamentoTotal ?: 0.0,

        // Assinaturas
        assinaturaTecnico = relatorio.assinaturaTecnico,
        assinaturaCliente = relatorio.assinaturaCliente,

        // Observações
        observacoes = observacoesFinais,
        nomeTecnico = "Técnico Aprimortech"
    )
}

/**
 * Calcula o total de horas entre entrada e saída
 */
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

/**
 * Componente de loading
 */
@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text("Carregando relatório...")
        }
    }
}

/**
 * Componente de erro
 */
@Composable
private fun ErrorView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 48.sp
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Conteúdo principal do relatório
 */
@Composable
private fun RelatorioContent(relatorioCompleto: RelatorioCompleto) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        HeaderSection(relatorioCompleto)

        Divider()

        // Dados do Cliente
        ClienteSection(relatorioCompleto)

        Divider()

        // Dados do Equipamento
        EquipamentoSection(relatorioCompleto)

        Divider()

        // Defeitos
        if (relatorioCompleto.defeitos.isNotEmpty()) {
            DefeitosSection(relatorioCompleto.defeitos)
            Divider()
        }

        // Serviços Realizados
        if (relatorioCompleto.servicos.isNotEmpty()) {
            ServicosSection(relatorioCompleto.servicos)
            Divider()
        }

        // Peças Utilizadas
        if (relatorioCompleto.pecas.isNotEmpty()) {
            PecasSection(relatorioCompleto.pecas)
            Divider()
        }

        // Horas Técnicas
        HorasTecnicasSection(relatorioCompleto)

        Divider()

        // Deslocamento
        DeslocamentoSection(relatorioCompleto)

        Divider()

        // Observações
        if (relatorioCompleto.observacoes.isNotEmpty()) {
            ObservacoesSection(relatorioCompleto.observacoes)
            Divider()
        }

        // Assinaturas
        AssinaturasSection(relatorioCompleto)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HeaderSection(relatorio: RelatorioCompleto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "RELATÓRIO DE MANUTENÇÃO",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Data: ${relatorio.dataRelatorio}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (relatorio.id.isNotEmpty()) {
                Text(
                    text = "ID: ${relatorio.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
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
            Text(
                text = "Contatos:",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
            relatorio.clienteContatos.forEach { contato ->
                Text(
                    text = "• ${contato.nome} - ${contato.setor} - ${contato.celular}",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                )
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

        if (relatorio.equipamentoDataProximaPreventiva.isNotEmpty()) {
            InfoRow(label = "Próxima Preventiva", value = relatorio.equipamentoDataProximaPreventiva)
        }
        if (relatorio.equipamentoHoraProximaPreventiva.isNotEmpty()) {
            InfoRow(label = "Hora Próxima Preventiva", value = relatorio.equipamentoHoraProximaPreventiva)
        }
    }
}

@Composable
private fun DefeitosSection(defeitos: List<String>) {
    SectionCard(title = "DEFEITOS ENCONTRADOS") {
        defeitos.forEachIndexed { index, defeito ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${index + 1}.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = defeito,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ServicosSection(servicos: List<String>) {
    SectionCard(title = "SERVIÇOS REALIZADOS") {
        servicos.forEachIndexed { index, servico ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${index + 1}.",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = servico,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PecasSection(pecas: List<PecaInfo>) {
    SectionCard(title = "PEÇAS UTILIZADAS") {
        // Header da tabela
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp)
        ) {
            Text(
                text = "Código",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "Descrição",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.weight(2f)
            )
            Text(
                text = "Qtd",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.width(40.dp)
            )
        }

        // Linhas da tabela
        pecas.forEach { peca ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = peca.codigo,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = peca.descricao,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = peca.quantidade.toString(),
                    fontSize = 12.sp,
                    modifier = Modifier.width(40.dp)
                )
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                InfoRow(label = "Entrada", value = relatorio.horarioEntrada)
            }
            Column(modifier = Modifier.weight(1f)) {
                InfoRow(label = "Saída", value = relatorio.horarioSaida)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        InfoRow(
            label = "Total de Horas",
            value = String.format("%.2f horas", relatorio.totalHorasTecnicas)
        )

        InfoRow(
            label = "Valor Hora Técnica",
            value = numberFormat.format(relatorio.valorHoraTecnica)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        InfoRow(
            label = "Total Hora Técnica",
            value = numberFormat.format(valorTotalHorasTecnicas),
            valueFontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DeslocamentoSection(relatorio: RelatorioCompleto) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    SectionCard(title = "DESLOCAMENTO") {
        InfoRow(label = "Distância (km)", value = String.format("%.2f km", relatorio.quantidadeKm))
        InfoRow(label = "Valor por km", value = numberFormat.format(relatorio.valorPorKm))
        InfoRow(label = "Pedágios", value = numberFormat.format(relatorio.valorPedagios))

        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))

        InfoRow(
            label = "Total Deslocamento",
            value = numberFormat.format(relatorio.valorTotalDeslocamento),
            valueFontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ObservacoesSection(observacoes: String) {
    SectionCard(title = "OBSERVAÇÕES") {
        Text(
            text = observacoes,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun AssinaturasSection(relatorio: RelatorioCompleto) {
    SectionCard(title = "ASSINATURAS") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Assinatura do Técnico
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Técnico",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                AssinaturaImage(base64 = relatorio.assinaturaTecnico)
                if (relatorio.nomeTecnico.isNotEmpty()) {
                    Text(
                        text = relatorio.nomeTecnico,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Assinatura do Cliente
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cliente",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                AssinaturaImage(base64 = relatorio.assinaturaCliente)
            }
        }
    }
}

/**
 * Componente para exibir assinatura base64
 */
@Composable
private fun AssinaturaImage(base64: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(4.dp)
            )
            .background(Color.White, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!base64.isNullOrEmpty()) {
            val bitmap = remember(base64) { base64ToBitmap(base64) }
            bitmap?.let {
                // Redimensionar para caber na área de exibição
                val resizedBitmap = remember(it) {
                    resizeBitmapToFit(it, maxWidth = 300, maxHeight = 120)
                }
                Image(
                    bitmap = resizedBitmap.asImageBitmap(),
                    contentDescription = "Assinatura",
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            } ?: Text(
                text = "Erro ao carregar assinatura",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                text = "Sem assinatura",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Converte Base64 para Bitmap
 */
private fun base64ToBitmap(base64: String): Bitmap? {
    return try {
        val cleanBase64 = base64.replace("data:image/png;base64,", "")
        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

/**
 * Redimensiona bitmap mantendo aspect ratio
 */
private fun resizeBitmapToFit(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    if (width <= maxWidth && height <= maxHeight) {
        return bitmap
    }

    val ratioWidth = maxWidth.toFloat() / width
    val ratioHeight = maxHeight.toFloat() / height
    val ratio = minOf(ratioWidth, ratioHeight)

    val newWidth = (width * ratio).toInt()
    val newHeight = (height * ratio).toInt()

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

/**
 * Card de seção reutilizável
 */
@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            content()
        }
    }
}

/**
 * Componente para exibir linha de informação
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueFontWeight: FontWeight = FontWeight.Normal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = valueFontWeight,
            modifier = Modifier.weight(1f)
        )
    }
}
