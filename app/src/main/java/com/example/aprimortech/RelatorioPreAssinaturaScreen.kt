package com.example.aprimortech

import android.content.Intent
import android.util.Log
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.aprimortech.data.repository.*
import com.example.aprimortech.model.*
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

private val Brand = Color(0xFF1A4A5C)
private val appBackground = Color(0xFFF5F5F5)
private val displayDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioPreAssinaturaScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    relatorioId: String? = null,
    relatorioCompleto: RelatorioCompleto? = null,
    relatorioFinal: Relatorio? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var relatorioCompletoState by remember { mutableStateOf<RelatorioCompleto?>(null) }
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

    LaunchedEffect(relatorioId, relatorioCompleto) {
        // Se o relatorioCompleto foi passado diretamente, usar imediatamente
        if (relatorioCompleto != null) {
            relatorioCompletoState = relatorioCompleto
            isLoading = false
            return@LaunchedEffect
        }

        if (relatorioId.isNullOrEmpty()) {
            errorMessage = "ID do relatório não fornecido"
            isLoading = false
            return@LaunchedEffect
        }

        try {
            isLoading = true
            try {
                val auth = FirebaseAuth.getInstance()
                if (auth.currentUser == null) {
                    auth.signInAnonymously().await()
                }
            } catch (_: Exception) {}

            relatorioCompletoState = carregarRelatorioCompleto(
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
                    if (relatorioCompletoState != null) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    try {
                                        val uri = PdfExporter.exportRelatorioCompleto(context, relatorioCompletoState!!)
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
                relatorioCompletoState != null -> RelatorioContentSemAssinaturas(relatorioCompleto = relatorioCompletoState!!, modifier = Modifier.weight(1f))
                else -> ErrorView("Relatório não encontrado")
            }

             // Botão para prosseguir à tela de assinaturas
             Button(
                 onClick = {
                     if (relatorioFinal == null) {
                         Toast.makeText(context, "Relatório não disponível para assinatura", Toast.LENGTH_LONG).show()
                         return@Button
                     }

                     Log.d("RelatorioPreAssinatura", "Prosseguindo para assinatura com relatorio id=${relatorioFinal.id}")
                     Toast.makeText(context, "Abrindo tela de assinatura...", Toast.LENGTH_SHORT).show()

                     // Serializar Relatorio para JSON (SavedStateHandle não aceita tipos não-parcelables)
                     try {
                         val jo = JSONObject()
                         jo.put("id", relatorioFinal.id)
                         jo.put("clienteId", relatorioFinal.clienteId)
                         jo.put("maquinaId", relatorioFinal.maquinaId)
                         jo.put("descricaoServico", relatorioFinal.descricaoServico)
                         jo.put("recomendacoes", relatorioFinal.recomendacoes ?: JSONObject.NULL)
                         jo.put("numeroNotaFiscal", relatorioFinal.numeroNotaFiscal ?: JSONObject.NULL)
                         jo.put("dataRelatorio", relatorioFinal.dataRelatorio)
                         jo.put("horarioEntrada", relatorioFinal.horarioEntrada ?: JSONObject.NULL)
                         jo.put("horarioSaida", relatorioFinal.horarioSaida ?: JSONObject.NULL)
                         jo.put("valorHoraTecnica", relatorioFinal.valorHoraTecnica ?: JSONObject.NULL)
                         jo.put("distanciaKm", relatorioFinal.distanciaKm ?: JSONObject.NULL)
                         jo.put("valorDeslocamentoPorKm", relatorioFinal.valorDeslocamentoPorKm ?: JSONObject.NULL)
                         jo.put("valorDeslocamentoTotal", relatorioFinal.valorDeslocamentoTotal ?: JSONObject.NULL)
                         jo.put("valorPedagios", relatorioFinal.valorPedagios ?: JSONObject.NULL)
                         jo.put("codigoTinta", relatorioFinal.codigoTinta ?: JSONObject.NULL)
                         jo.put("codigoSolvente", relatorioFinal.codigoSolvente ?: JSONObject.NULL)
                         jo.put("dataProximaPreventiva", relatorioFinal.dataProximaPreventiva ?: JSONObject.NULL)
                         jo.put("horasProximaPreventiva", relatorioFinal.horasProximaPreventiva ?: JSONObject.NULL)
                         jo.put("custoPecas", relatorioFinal.custoPecas ?: JSONObject.NULL)
                         jo.put("observacoes", relatorioFinal.observacoes ?: JSONObject.NULL)

                         // listas simples
                         val arrDefeitos = JSONArray()
                         relatorioFinal.defeitosIdentificados.forEach { arrDefeitos.put(it) }
                         jo.put("defeitosIdentificados", arrDefeitos)

                         val arrServicos = JSONArray()
                         relatorioFinal.servicosRealizados.forEach { arrServicos.put(it) }
                         jo.put("servicosRealizados", arrServicos)

                         // pecasUtilizadas: List<Map<String, Any>>
                         val arrPecas = JSONArray()
                         relatorioFinal.pecasUtilizadas.forEach { map ->
                             val p = JSONObject()
                             map.forEach { (k, v) ->
                                 when (v) {
                                     is Number -> p.put(k, v)
                                     is Boolean -> p.put(k, v)
                                     else -> p.put(k, v?.toString() ?: JSONObject.NULL)
                                 }
                             }
                             arrPecas.put(p)
                         }
                         jo.put("pecasUtilizadas", arrPecas)

                         jo.put("syncPending", relatorioFinal.syncPending)

                         val jsonString = jo.toString()

                         // Salva o JSON no savedStateHandle
                         navController.currentBackStackEntry?.savedStateHandle?.set("relatorioFinalJson", jsonString)
                     } catch (e: Exception) {
                         Log.e("RelatorioPreAssinatura", "Erro serializando relatorio: ${e.message}")
                         Toast.makeText(context, "Erro preparando relatório para assinatura", Toast.LENGTH_LONG).show()
                         return@Button
                     }

                     // Salva o objeto Relatorio no savedStateHandle para a próxima tela
                     navController.navigate("relatorioAssinatura") {
                         // não limpar a pilha para permitir voltar
                     }
                 },
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(50.dp),
                // Só habilita quando o Relatorio estiver disponível
                enabled = relatorioFinal != null && !isLoading,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A4A5C),
                    contentColor = Color.White
                )
            ) {
                Text("Prosseguir para Assinatura", style = MaterialTheme.typography.titleMedium)
            }

        }
    }
}

@Composable
private fun RelatorioContentSemAssinaturas(relatorioCompleto: RelatorioCompleto, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
        Spacer(modifier = Modifier.height(16.dp))
    }
}

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
