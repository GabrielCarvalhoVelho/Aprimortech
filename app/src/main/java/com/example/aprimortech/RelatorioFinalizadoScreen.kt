package com.example.aprimortech

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aprimortech.data.repository.*
import com.example.aprimortech.model.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import java.net.URL
import java.text.NumberFormat
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

            // Garantir autenticação anônima (se regras exigirem auth para leitura do Storage)
            try {
                val auth = FirebaseAuth.getInstance()
                if (auth.currentUser == null) {
                    Log.d("RelatorioFinalizado", "Usuário não autenticado. Tentando signInAnonymously()")
                    try {
                        auth.signInAnonymously().await()
                        Log.d("RelatorioFinalizado", "Autenticação anônima bem sucedida: uid=${auth.currentUser?.uid}")
                    } catch (_: Exception) {
                        Log.w("RelatorioFinalizado", "Falha ao autenticar anonimamente")
                    }
                } else {
                    Log.d("RelatorioFinalizado", "Usuário já autenticado: uid=${auth.currentUser?.uid}")
                }
            } catch (_: Exception) {
                Log.w("RelatorioFinalizado", "Erro ao garantir autenticação anon")
            }

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
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.logo_aprimortech),
                        contentDescription = "Logo Aprimortech",
                        modifier = Modifier.height(40.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Brand)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("dashboard") {
                                launchSingleTop = true
                                popUpTo("dashboard") { inclusive = false }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Home, contentDescription = "Ir para Dashboard", tint = Brand)
                    }

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
                            Icon(Icons.Default.Share, contentDescription = "Compartilhar", tint = Brand)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(appBackground)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                isLoading -> LoadingView()
                errorMessage != null -> ErrorView(errorMessage!!)
                relatorioCompleto != null -> RelatorioContent(relatorioCompleto = relatorioCompleto!!)
                else -> ErrorView("Relatório não encontrado")
            }
        }
    }
}

// Torna pública a função carregarRelatorioCompleto

@OptIn(ExperimentalMaterial3Api::class)
suspend fun carregarRelatorioCompleto(
    relatorioId: String,
    relatorioRepository: RelatorioRepository,
    clienteRepository: ClienteRepository,
    maquinaRepository: MaquinaRepository,
    pecaRepository: PecaRepository,
    defeitoRepository: DefeitoRepository,
    servicoRepository: ServicoRepository,
    tintaRepository: TintaRepository,
    solventeRepository: SolventeRepository
): RelatorioCompleto = carregarRelatorioCompletoImpl(
    relatorioId,
    relatorioRepository,
    clienteRepository,
    maquinaRepository,
    pecaRepository,
    defeitoRepository,
    servicoRepository,
    tintaRepository,
    solventeRepository
)

private suspend fun carregarRelatorioCompletoImpl(
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
            } catch (_: Exception) {
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

    // Equipamento fotos: priorizar fotos diretamente salvas no relatório (relatorio.equipamentoFotos)
    var equipamentoFotosList = if (relatorio.equipamentoFotos.isNotEmpty()) {
        relatorio.equipamentoFotos.toMutableList()
    } else {
        // Caso não existam no relatório, tentar recuperar da máquina (se houver campo de fotos)
        // Atualmente Maquina model doesn't store fotos; placeholder for future retrieval from maquina
        mutableListOf<String>()
    }

    // Resolver entradas `gs://` ou listagens e converter para URLs HTTP (downloadUrl) ou data URIs antes de retornar
    try {
        val bucketOnly = equipamentoFotosList.any { it.startsWith("gs://") && !it.substringAfter("gs://").contains("/") }
        if (bucketOnly) {
            val resolved = mutableListOf<String>()
            val storage = FirebaseStorage.getInstance("gs://aprimortech-30cad.firebasestorage.app")
             equipamentoFotosList.forEach { item ->
                 if (item.startsWith("gs://") && !item.substringAfter("gs://").contains("/")) {
                     try {
                         val folderRef = try {
                             val bucketRef = storage.getReferenceFromUrl(item)
                             bucketRef.child("relatorios/${relatorio.id}/fotos")
                         } catch (_: Exception) {
                             // Fallback to default app bucket path if getReferenceFromUrl fails
                             storage.reference.child("relatorios/${relatorio.id}/fotos")
                         }
                         val listResult = folderRef.listAll().await()
                         if (listResult.items.isEmpty()) {
                             // Try alternative bucket name when original uses `.firebasestorage.app` (some projects use .appspot.com)
                             if (item.contains(".firebasestorage.app")) {
                                try {
                                    val altBucket = item.replace(".firebasestorage.app", ".appspot.com")
                                    val altStorage = FirebaseStorage.getInstance("gs://aprimortech-30cad.firebasestorage.app")
                                    val altRef = try {
                                        altStorage.getReferenceFromUrl(altBucket).child("relatorios/${relatorio.id}/fotos")
                                    } catch (_: Exception) {
                                        altStorage.reference.child("relatorios/${relatorio.id}/fotos")
                                    }
                                    val altList = altRef.listAll().await()
                                    altList.items.forEach { fileRef ->
                                        try {
                                            val url = fileRef.downloadUrl.await().toString()
                                            resolved.add(url)
                                        } catch (_: Exception) { }
                                    }
                                    // if altList had items, continue
                                    if (altList.items.isNotEmpty()) return@forEach
                                } catch (_: Exception) { /* ignore alt attempt */ }
                            }
                         } else {
                            listResult.items.forEach { fileRef ->
                                try {
                                    val url = fileRef.downloadUrl.await().toString()
                                    resolved.add(url)
                                } catch (_: Exception) { /* ignore single file */ }
                            }
                         }
                     } catch (_: Exception) {
                         // fallback: keep original bucket string for debugging
                         resolved.add(item)
                     }
                 } else {
                     resolved.add(item)
                 }
             }
             equipamentoFotosList = resolved
         }
     } catch (_: Exception) {
         // ignore resolution errors, we'll try to render what we have
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
        // Passar lista de fotos para o modelo completo
        equipamentoFotos = equipamentoFotosList,
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
        // Se o backend não tiver calculado, calcular aqui como fallback
        valorTotalDeslocamento = relatorio.valorDeslocamentoTotal ?: calcularValorDeslocamentoTotal(relatorio.distanciaKm, relatorio.valorDeslocamentoPorKm, relatorio.valorPedagios),
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

// Função utilitária: calcular valor total do deslocamento (fallback UI)
private fun calcularValorDeslocamentoTotal(distanciaKm: Double?, valorPorKm: Double?, pedagios: Double?): Double {
    val d = distanciaKm ?: 0.0
    val v = valorPorKm ?: 0.0
    val p = pedagios ?: 0.0
    return d * v + p
}

@Composable
fun LoadingView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator()
            Text("Carregando relatório...")
        }
    }
}

@Composable
fun ErrorView(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(32.dp)) {
            Text(text = "⚠️", fontSize = 48.sp)
            Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun RelatorioContent(relatorioCompleto: RelatorioCompleto) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabeçalho: nome do cliente (em destaque) e data formatada
        SectionCard {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = "Relatório", tint = Brand, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = relatorioCompleto.clienteNome,
                    style = MaterialTheme.typography.titleMedium,
                    color = Brand,
                    modifier = Modifier.weight(1f)
                )
                val parsed = parseDateSafe(relatorioCompleto.dataRelatorio)
                val formattedDate = if (parsed != null) displayDateFormatter.format(parsed) else relatorioCompleto.dataRelatorio
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ClienteSection(relatorioCompleto)
        HorizontalDivider()
        EquipamentoSection(relatorioCompleto)
        // Nova seção: fotos do equipamento (se existirem)
        if (relatorioCompleto.equipamentoFotos.isNotEmpty()) {
            HorizontalDivider()
            FotosSection(relatorioCompleto)
        }
        HorizontalDivider()
        if (relatorioCompleto.pecas.isNotEmpty()) {
            PecasSection(relatorioCompleto.pecas)
            HorizontalDivider()
        }
        if (relatorioCompleto.defeitos.isNotEmpty()) {
            DefeitosSection(relatorioCompleto.defeitos)
            HorizontalDivider()
        }
        if (relatorioCompleto.servicos.isNotEmpty()) {
            ServicosSection(servicos = relatorioCompleto.servicos)
            HorizontalDivider()
        }
        HorasTecnicasSection(relatorioCompleto)
        HorizontalDivider()
        DeslocamentoSection(relatorioCompleto)
        HorizontalDivider()
        if (relatorioCompleto.observacoes.isNotBlank()) {
            ObservacoesSection(relatorioCompleto.observacoes)
            HorizontalDivider()
        }
        AssinaturasSection(relatorioCompleto)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ClienteSection(relatorio: RelatorioCompleto) {
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
fun EquipamentoSection(relatorio: RelatorioCompleto) {
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

        // Mostrar thumbnails das fotos do equipamento dentro desta seção
        if (relatorio.equipamentoFotos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Fotos do Equipamento", style = MaterialTheme.typography.titleSmall, color = Brand)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                relatorio.equipamentoFotos.forEachIndexed { index, imgStr ->
                    // Determine what to pass to Coil: HTTP URL, data URI (for base64), or try to resolve gs:// to downloadUrl
                    val resolvedSource by produceState<String?>(initialValue = null, key1 = imgStr) {
                        value = try {
                            when {
                                imgStr.startsWith("http://") || imgStr.startsWith("https://") -> imgStr
                                imgStr.startsWith("gs://") -> {
                                    // Try to resolve gs:// to a downloadUrl
                                    try {
                                        val storage = FirebaseStorage.getInstance("gs://aprimortech-30cad.firebasestorage.app")
                                        val ref = storage.getReferenceFromUrl(imgStr)
                                        ref.downloadUrl.await().toString()
                                    } catch (_: Exception) {
                                        // Fallback to default app bucket path if getReferenceFromUrl fails
                                        null
                                    }
                                }
                                else -> {
                                    // Assume base64 - convert to data URI so Coil can load it
                                    try {
                                        val cleaned = imgStr.substringAfter("base64,", imgStr)
                                        "data:image/jpeg;base64,$cleaned"
                                    } catch (_: Exception) {
                                        null
                                    }
                                }
                            }
                        } catch (_: Exception) {
                            null
                        }
                    }

                    // Carregamento manual compatível (sem Coil). resolvedSource pode ser:
                    // - URL http(s)
                    // - data URI (data:image/...;base64,....)
                    // - null (fallback)
                    val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = resolvedSource) {
                        value = try {
                            val src = resolvedSource
                            when {
                                src == null -> null
                                src.startsWith("data:") -> {
                                    try {
                                        val b64 = src.substringAfter(",")
                                        val bytes = Base64.decode(b64, Base64.DEFAULT)
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    } catch (_: Exception) { null }
                                }
                                src.startsWith("http://") || src.startsWith("https://") -> {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            val bytes = URL(src).openStream().use { it.readBytes() }
                                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        } catch (_: Exception) { null }
                                    }
                                }
                                else -> null
                            }
                        } catch (_: Exception) { null }
                    }

                    if (bitmap != null) {
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.size(120.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                        ) {
                            Image(
                                bitmap = bitmap!!.asImageBitmap(),
                                contentDescription = "Foto ${index + 1}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        // Fallback: placeholder and debug log
                        LaunchedEffect(imgStr) { Log.d("RelatorioFinalizado", "Falha ao carregar imagem (index=$index). valor='$imgStr'") }
                        Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.size(120.dp)) {
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray), contentAlignment = Alignment.Center) {
                                val debugText = when {
                                    imgStr.startsWith("gs://") && !imgStr.contains("/") -> "gs://... (parece só bucket)"
                                    imgStr.length > 32 -> imgStr.take(32) + "..."
                                    else -> imgStr
                                }
                                Text("Erro ao carregar\n$debugText", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                 }
             }
         }
     }
}

@Composable
fun DefeitosSection(defeitos: List<String>) {
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
fun ServicosSection(servicos: List<String>) {
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
fun PecasSection(pecas: List<PecaInfo>) {
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
            HorizontalDivider()
        }
    }
}

@Composable
fun HorasTecnicasSection(relatorio: RelatorioCompleto) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    val valorTotalHorasTecnicas = relatorio.totalHorasTecnicas * relatorio.valorHoraTecnica
    SectionCard(title = "HORAS TÉCNICAS") {
        Column(modifier = Modifier.fillMaxWidth()) {
            InfoRow(label = "Entrada", value = relatorio.horarioEntrada)
            InfoRow(label = "Saída", value = relatorio.horarioSaida)
        }
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow(label = "Total de Horas", value = String.format(Locale.getDefault(), "%.2f h", relatorio.totalHorasTecnicas))
        InfoRow(label = "Valor por Hora", value = numberFormat.format(relatorio.valorHoraTecnica))
        InfoRow(label = "Valor Total", value = numberFormat.format(valorTotalHorasTecnicas), highlight = true)
    }
}

@Composable
fun DeslocamentoSection(relatorio: RelatorioCompleto) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
    SectionCard(title = "DESLOCAMENTO") {
        InfoRow(label = "Distância (KM)", value = String.format(Locale.getDefault(), "%.2f km", relatorio.quantidadeKm))
        InfoRow(label = "Valor por KM", value = numberFormat.format(relatorio.valorPorKm))
        InfoRow(label = "Pedágios", value = numberFormat.format(relatorio.valorPedagios))
        InfoRow(label = "Total Deslocamento", value = numberFormat.format(relatorio.valorTotalDeslocamento), highlight = true)
    }
}

@Composable
fun ObservacoesSection(observacoes: String) {
    SectionCard(title = "OBSERVAÇÕES") {
        Text(text = observacoes, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun AssinaturasSection(relatorio: RelatorioCompleto) {
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
fun SectionCard(title: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            title?.let {
                Text(text = it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Brand)
                HorizontalDivider()
            }
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String, highlight: Boolean = false) {
    val displayValue = if (value.isBlank()) "—" else value
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = "$label:", fontSize = 12.sp, fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(text = displayValue, fontSize = 12.sp, fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun FotosSection(relatorio: RelatorioCompleto) {
    SectionCard(title = "FOTOS DO EQUIPAMENTO") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            relatorio.equipamentoFotos.forEachIndexed { index, imgStr ->
                // Determine what to pass to Coil: HTTP URL, data URI (for base64), or try to resolve gs:// to downloadUrl
                val resolvedSource by produceState<String?>(initialValue = null, key1 = imgStr) {
                    value = try {
                        when {
                            imgStr.startsWith("http://") || imgStr.startsWith("https://") -> imgStr
                            imgStr.startsWith("gs://") -> {
                                // Try to resolve gs:// to a downloadUrl
                                try {
                                    val storage = FirebaseStorage.getInstance("gs://aprimortech-30cad.firebasestorage.app")
                                    val ref = storage.getReferenceFromUrl(imgStr)
                                    ref.downloadUrl.await().toString()
                                } catch (_: Exception) {
                                    // Fallback to default app bucket path if getReferenceFromUrl fails
                                    null
                                }
                            }
                            else -> {
                                // Assume base64 - convert to data URI so Coil can load it
                                try {
                                    val cleaned = imgStr.substringAfter("base64,", imgStr)
                                    "data:image/jpeg;base64,$cleaned"
                                } catch (_: Exception) {
                                    null
                                }
                            }
                        }
                    } catch (_: Exception) {
                        null
                    }
                }

                // Carregamento manual compatível (sem Coil). resolvedSource pode ser:
                // - URL http(s)
                // - data URI (data:image/...;base64,....)
                // - null (fallback)
                val bitmap by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = resolvedSource) {
                    value = try {
                        val src = resolvedSource
                        when {
                            src == null -> null
                            src.startsWith("data:") -> {
                                try {
                                    val b64 = src.substringAfter(",")
                                    val bytes = Base64.decode(b64, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                } catch (_: Exception) { null }
                            }
                            src.startsWith("http://") || src.startsWith("https://") -> {
                                withContext(Dispatchers.IO) {
                                    try {
                                        val bytes = URL(src).openStream().use { it.readBytes() }
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    } catch (_: Exception) { null }
                                }
                            }
                            else -> null
                        }
                    } catch (_: Exception) { null }
                }

                if (bitmap != null) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(120.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Foto ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Card(shape = RoundedCornerShape(8.dp), modifier = Modifier.size(120.dp)) {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray), contentAlignment = Alignment.Center) {
                            LaunchedEffect(imgStr) {
                                Log.d("RelatorioFinalizado", "Falha ao carregar imagem (index=$index). valor='$imgStr'")
                            }
                            val debugText = when {
                                imgStr.startsWith("gs://") && !imgStr.contains("/") -> "gs://... (parece só bucket)"
                                imgStr.length > 32 -> imgStr.take(32) + "..."
                                else -> imgStr
                            }
                            Text("Erro ao carregar\n$debugText", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // DEBUG PANEL: mostra as strings das imagens e o tipo (apenas em builds DEBUG)
        val ctx = LocalContext.current
        val isDebug = (ctx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug && relatorio.equipamentoFotos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "DEBUG: imagens (tipo / prefixo)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Column(modifier = Modifier.fillMaxWidth()) {
                relatorio.equipamentoFotos.forEachIndexed { i, s ->
                    val type = when {
                        s.startsWith("gs://") -> "gs://"
                        s.startsWith("http://") || s.startsWith("https://") -> "http(s)"
                        s.length > 100 -> "base64 (long)"
                        else -> "base64/other"
                    }
                    Text(text = "${i + 1}. [$type] ${s.take(60)}${if (s.length > 60) "..." else ""}", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }

    }
}
