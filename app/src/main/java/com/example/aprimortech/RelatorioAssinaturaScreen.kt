package com.example.aprimortech

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.RelatorioViewModel
import com.example.aprimortech.ui.viewmodel.RelatorioViewModelFactory
import com.example.aprimortech.ui.viewmodel.RelatorioSharedViewModel
import com.example.aprimortech.model.Relatorio
import com.example.aprimortech.util.SignatureUtils
import android.widget.Toast
import androidx.compose.ui.layout.onSizeChanged
import org.json.JSONArray
import org.json.JSONObject
import android.util.Log

// Estado da assinatura: cada assinatura é composta por uma lista de linhas (paths).
data class SignatureState(
    val paths: SnapshotStateList<SnapshotStateList<Offset>> = mutableStateListOf()
) {
    fun clear() {
        paths.clear()
    }

    fun hasSignature(): Boolean {
        return paths.isNotEmpty() && paths.any { it.isNotEmpty() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioAssinaturaScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    relatorioInicial: Relatorio? = null,
    viewModel: RelatorioViewModel = viewModel(
        factory = RelatorioViewModelFactory(
            buscarRelatoriosUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarRelatoriosUseCase,
            salvarRelatorioUseCase = (LocalContext.current.applicationContext as AprimortechApplication).salvarRelatorioUseCase,
            excluirRelatorioUseCase = (LocalContext.current.applicationContext as AprimortechApplication).excluirRelatorioUseCase,
            sincronizarRelatoriosUseCase = (LocalContext.current.applicationContext as AprimortechApplication).sincronizarRelatoriosUseCase,
            buscarProximasManutencoesPreventivasUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarProximasManutencoesPreventivasUseCase,
            relatorioRepository = (LocalContext.current.applicationContext as AprimortechApplication).relatorioRepository
        )
    ),
    sharedViewModel: RelatorioSharedViewModel = viewModel()
) {
    val context = LocalContext.current
    val clienteSignature1 = remember { SignatureState() }
    val clienteSignature2 = remember { SignatureState() }
    val tecnicoSignature1 = remember { SignatureState() }
    val tecnicoSignature2 = remember { SignatureState() }
    var isLoading by remember { mutableStateOf(false) }

    // Usa o relatório passado como parâmetro ou tenta recuperar do savedStateHandle
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val previousBackStackEntry = navController.previousBackStackEntry

    // Tenta recuperar JSON salvo pela tela de pré-assinatura
    val relatorioJsonString = currentBackStackEntry?.savedStateHandle?.get<String>("relatorioFinalJson")
        ?: previousBackStackEntry?.savedStateHandle?.get<String>("relatorioFinalJson")

    // Se encontramos o JSON, desserializar para Relatorio
    val relatorioFromState: Relatorio? = try {
        if (!relatorioJsonString.isNullOrBlank()) {
            val jo = JSONObject(relatorioJsonString)
            val id = jo.optString("id", "")
            val clienteId = jo.optString("clienteId", "")
            val maquinaId = jo.optString("maquinaId", "")
            val descricaoServico = jo.optString("descricaoServico", "")
            val recomendacoes = if (jo.has("recomendacoes") && !jo.isNull("recomendacoes")) jo.optString("recomendacoes") else ""
            val numeroNotaFiscal = if (jo.has("numeroNotaFiscal") && !jo.isNull("numeroNotaFiscal")) jo.optString("numeroNotaFiscal") else null
            val dataRelatorio = jo.optString("dataRelatorio", "")
            val horarioEntrada = if (jo.has("horarioEntrada") && !jo.isNull("horarioEntrada")) jo.optString("horarioEntrada") else null
            val horarioSaida = if (jo.has("horarioSaida") && !jo.isNull("horarioSaida")) jo.optString("horarioSaida") else null
            val valorHoraTecnica = if (jo.has("valorHoraTecnica") && !jo.isNull("valorHoraTecnica")) jo.optDouble("valorHoraTecnica") else null
            val distanciaKm = if (jo.has("distanciaKm") && !jo.isNull("distanciaKm")) jo.optDouble("distanciaKm") else null
            val valorDeslocamentoPorKm = if (jo.has("valorDeslocamentoPorKm") && !jo.isNull("valorDeslocamentoPorKm")) jo.optDouble("valorDeslocamentoPorKm") else null
            val valorDeslocamentoTotal = if (jo.has("valorDeslocamentoTotal") && !jo.isNull("valorDeslocamentoTotal")) jo.optDouble("valorDeslocamentoTotal") else null
            val valorPedagios = if (jo.has("valorPedagios") && !jo.isNull("valorPedagios")) jo.optDouble("valorPedagios") else null
            val codigoTinta = if (jo.has("codigoTinta") && !jo.isNull("codigoTinta")) jo.optString("codigoTinta") else null
            val codigoSolvente = if (jo.has("codigoSolvente") && !jo.isNull("codigoSolvente")) jo.optString("codigoSolvente") else null
            val dataProximaPreventiva = if (jo.has("dataProximaPreventiva") && !jo.isNull("dataProximaPreventiva")) jo.optString("dataProximaPreventiva") else null
            val horasProximaPreventiva = if (jo.has("horasProximaPreventiva") && !jo.isNull("horasProximaPreventiva")) jo.optString("horasProximaPreventiva") else null
            val custoPecas = if (jo.has("custoPecas") && !jo.isNull("custoPecas")) jo.optDouble("custoPecas") else null
            val observacoes = if (jo.has("observacoes") && !jo.isNull("observacoes")) jo.optString("observacoes") else null

            val defeitosIdentificados = mutableListOf<String>()
            if (jo.has("defeitosIdentificados") && !jo.isNull("defeitosIdentificados")) {
                val arr = jo.getJSONArray("defeitosIdentificados")
                for (i in 0 until arr.length()) defeitosIdentificados.add(arr.optString(i))
            }

            val servicosRealizados = mutableListOf<String>()
            if (jo.has("servicosRealizados") && !jo.isNull("servicosRealizados")) {
                val arr = jo.getJSONArray("servicosRealizados")
                for (i in 0 until arr.length()) servicosRealizados.add(arr.optString(i))
            }

            val pecasUtilizadas = mutableListOf<Map<String, Any>>()
            if (jo.has("pecasUtilizadas") && !jo.isNull("pecasUtilizadas")) {
                val arr = jo.getJSONArray("pecasUtilizadas")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val map = mutableMapOf<String, Any>()
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val v = obj.opt(k)
                        if (v == JSONObject.NULL) map[k] = ""
                        else map[k] = v
                    }
                    pecasUtilizadas.add(map)
                }
            }

            val syncPending = if (jo.has("syncPending")) jo.optBoolean("syncPending") else true

            Relatorio(
                id = id,
                numeroRelatorio = "", // Será mantido ou gerado pelo repository
                clienteId = clienteId,
                maquinaId = maquinaId,
                pecaIds = emptyList(),
                descricaoServico = descricaoServico,
                recomendacoes = recomendacoes ?: "",
                numeroNotaFiscal = numeroNotaFiscal,
                dataRelatorio = dataRelatorio,
                horarioEntrada = horarioEntrada,
                horarioSaida = horarioSaida,
                valorHoraTecnica = valorHoraTecnica,
                distanciaKm = distanciaKm,
                valorDeslocamentoPorKm = valorDeslocamentoPorKm,
                valorDeslocamentoTotal = valorDeslocamentoTotal,
                valorPedagios = valorPedagios,
                codigoTinta = codigoTinta,
                codigoSolvente = codigoSolvente,
                dataProximaPreventiva = dataProximaPreventiva,
                horasProximaPreventiva = horasProximaPreventiva,
                custoPecas = custoPecas,
                observacoes = observacoes,
                assinaturaCliente1 = null,
                assinaturaCliente2 = null,
                assinaturaTecnico1 = null,
                assinaturaTecnico2 = null,
                tintaId = null,
                solventeId = null,
                defeitosIdentificados = defeitosIdentificados,
                servicosRealizados = servicosRealizados,
                observacoesDefeitosServicos = observacoes ?: "",
                pecasUtilizadas = pecasUtilizadas,
                syncPending = syncPending,
                equipamentoFotos = emptyList()
            )
        } else null
    } catch (e: Exception) {
        Log.e("RelatorioAssinatura", "Erro desserializando relatorioJson: ${e.message}")
        null
    }

    // Limpar a chave da entry anterior para evitar poluição
    previousBackStackEntry?.savedStateHandle?.remove<String>("relatorioFinalJson")

    val relatorioFinalState = remember {
        mutableStateOf(relatorioInicial ?: relatorioFromState)
    }
    val relatorioFinal = relatorioFinalState.value

    // Se relatorioFinal estiver nulo ou sem dados de cliente/maquina, buscar e preencher
    val clienteId = relatorioFinal?.clienteId ?: ""
    val maquinaId = relatorioFinal?.maquinaId ?: ""
    val clienteList by viewModel.buscarRelatoriosPorCliente(clienteId).collectAsState(initial = emptyList())
    val maquinaList by viewModel.buscarRelatoriosPorMaquina(maquinaId).collectAsState(initial = emptyList())
    LaunchedEffect(relatorioFinal, clienteList, maquinaList) {
        if (relatorioFinal == null || relatorioFinal.clienteId.isEmpty() || relatorioFinal.maquinaId.isEmpty()) {
            val cliente = clienteList.firstOrNull()
            val maquina = maquinaList.firstOrNull()
            if (cliente != null && maquina != null) {
                relatorioFinalState.value = relatorioFinal?.copy(
                    clienteId = cliente.clienteId,
                    maquinaId = maquina.maquinaId
                )
            }
        }
    }

    // Estados do ViewModel
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()
    val relatorioSalvoId by viewModel.relatorioSalvoId.collectAsState()

    // Feedback toast e navegação
    LaunchedEffect(mensagemOperacao, relatorioSalvoId) {
        mensagemOperacao?.let { msg ->
            isLoading = false
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.limparMensagem()
            if (msg.contains("sucesso") && relatorioSalvoId != null) {
                // Navegar para tela finalizada passando o ID do relatório
                navController.navigate("relatorioFinalizado?relatorioId=$relatorioSalvoId") {
                    popUpTo("relatorios") { inclusive = false }
                }
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5))
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header
                Text(
                    text = "Assinaturas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF1A4A5C)
                )

                // Assinatura do Cliente 1
                Text("Assinatura do Cliente 1", style = MaterialTheme.typography.titleMedium)
                var clienteCanvasWidth1 by remember { mutableStateOf(400) }
                var clienteCanvasHeight1 by remember { mutableStateOf(200) }
                SignaturePad(
                    state = clienteSignature1,
                    onClear = { clienteSignature1.clear() },
                    onSizeChanged = { w, h ->
                        clienteCanvasWidth1 = w
                        clienteCanvasHeight1 = h
                    }
                )

                // Assinatura do Cliente 2
                Text("Assinatura do Cliente 2", style = MaterialTheme.typography.titleMedium)
                var clienteCanvasWidth2 by remember { mutableStateOf(400) }
                var clienteCanvasHeight2 by remember { mutableStateOf(200) }
                SignaturePad(
                    state = clienteSignature2,
                    onClear = { clienteSignature2.clear() },
                    onSizeChanged = { w, h ->
                        clienteCanvasWidth2 = w
                        clienteCanvasHeight2 = h
                    }
                )

                // Assinatura do Técnico 1
                Text("Assinatura do Técnico 1", style = MaterialTheme.typography.titleMedium)
                var tecnicoCanvasWidth1 by remember { mutableStateOf(400) }
                var tecnicoCanvasHeight1 by remember { mutableStateOf(200) }
                SignaturePad(
                    state = tecnicoSignature1,
                    onClear = { tecnicoSignature1.clear() },
                    onSizeChanged = { w, h ->
                        tecnicoCanvasWidth1 = w
                        tecnicoCanvasHeight1 = h
                    }
                )

                // Assinatura do Técnico 2
                Text("Assinatura do Técnico 2", style = MaterialTheme.typography.titleMedium)
                var tecnicoCanvasWidth2 by remember { mutableStateOf(400) }
                var tecnicoCanvasHeight2 by remember { mutableStateOf(200) }
                SignaturePad(
                    state = tecnicoSignature2,
                    onClear = { tecnicoSignature2.clear() },
                    onSizeChanged = { w, h ->
                        tecnicoCanvasWidth2 = w
                        tecnicoCanvasHeight2 = h
                    }
                )

                Spacer(Modifier.height(24.dp))

                // Botão de finalização
                Button(
                    onClick = {
                        if (relatorioFinal != null) {
                            isLoading = true
                            val bitmapAssinaturaCliente1 = if (clienteSignature1.hasSignature()) {
                                SignatureUtils.convertSignatureToBitmap(clienteSignature1.paths, clienteCanvasWidth1, clienteCanvasHeight1)
                            } else null
                            val bitmapAssinaturaCliente2 = if (clienteSignature2.hasSignature()) {
                                SignatureUtils.convertSignatureToBitmap(clienteSignature2.paths, clienteCanvasWidth2, clienteCanvasHeight2)
                            } else null
                            val bitmapAssinaturaTecnico1 = if (tecnicoSignature1.hasSignature()) {
                                SignatureUtils.convertSignatureToBitmap(tecnicoSignature1.paths, tecnicoCanvasWidth1, tecnicoCanvasHeight1)
                            } else null
                            val bitmapAssinaturaTecnico2 = if (tecnicoSignature2.hasSignature()) {
                                SignatureUtils.convertSignatureToBitmap(tecnicoSignature2.paths, tecnicoCanvasWidth2, tecnicoCanvasHeight2)
                            } else null
                            val assinaturaClienteBase64_1 = bitmapAssinaturaCliente1?.let { SignatureUtils.bitmapToBase64(it) }
                            val assinaturaClienteBase64_2 = bitmapAssinaturaCliente2?.let { SignatureUtils.bitmapToBase64(it) }
                            val assinaturaTecnicoBase64_1 = bitmapAssinaturaTecnico1?.let { SignatureUtils.bitmapToBase64(it) }
                            val assinaturaTecnicoBase64_2 = bitmapAssinaturaTecnico2?.let { SignatureUtils.bitmapToBase64(it) }
                            sharedViewModel.setAssinaturas(
                                assinaturaTecnico1 = assinaturaTecnicoBase64_1,
                                assinaturaTecnico2 = assinaturaTecnicoBase64_2,
                                assinaturaCliente1 = assinaturaClienteBase64_1,
                                assinaturaCliente2 = assinaturaClienteBase64_2,
                                nomeTecnico = "Técnico Aprimortech"
                            )

                            android.util.Log.d("RelatorioAssinatura", "=== SALVANDO RELATÓRIO ===")
                            android.util.Log.d("RelatorioAssinatura", "RelatorioFinal recebido - Código Tinta: ${relatorioFinal.codigoTinta}")
                            android.util.Log.d("RelatorioAssinatura", "RelatorioFinal recebido - Código Solvente: ${relatorioFinal.codigoSolvente}")
                            android.util.Log.d("RelatorioAssinatura", "Cliente ID: ${relatorioFinal.clienteId}")
                            android.util.Log.d("RelatorioAssinatura", "Máquina ID: ${relatorioFinal.maquinaId}")
                            android.util.Log.d("RelatorioAssinatura", "Descrição Serviço: ${relatorioFinal.descricaoServico}")
                            android.util.Log.d("RelatorioAssinatura", "Observações: ${relatorioFinal.observacoes}")

                            // IMPORTANTE: Manter os códigos de tinta e solvente que o usuário preencheu manualmente
                            // Não sobrescrever com dados da máquina
                            val relatorioParaSalvar = relatorioFinal.copy(
                                syncPending = false,
                                codigoTinta = relatorioFinal.codigoTinta, // Mantém o que o usuário preencheu
                                codigoSolvente = relatorioFinal.codigoSolvente // Mantém o que o usuário preencheu
                            )

                            android.util.Log.d("RelatorioAssinatura", "=== RELATÓRIO PARA SALVAR ===")
                            android.util.Log.d("RelatorioAssinatura", "Código Tinta (final): ${relatorioParaSalvar.codigoTinta}")
                            android.util.Log.d("RelatorioAssinatura", "Código Solvente (final): ${relatorioParaSalvar.codigoSolvente}")

                            // O relatório já vem com os códigos do MainActivity, apenas salvar
                            viewModel.salvarRelatorioComAssinaturas(
                                relatorio = relatorioParaSalvar,
                                assinaturaCliente1 = bitmapAssinaturaCliente1,
                                assinaturaCliente2 = bitmapAssinaturaCliente2,
                                assinaturaTecnico1 = bitmapAssinaturaTecnico1,
                                assinaturaTecnico2 = bitmapAssinaturaTecnico2
                            )
                        } else {
                            android.util.Log.e("RelatorioAssinatura", "RelatorioFinal é NULL! Não é possível salvar.")
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A4A5C),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    // Exigir ao menos 1 assinatura do cliente (cliente1 OU cliente2)
                    // e ao menos 1 assinatura do técnico (tecnico1 OU tecnico2)
                    enabled = ( (clienteSignature1.hasSignature() || clienteSignature2.hasSignature())
                            && (tecnicoSignature1.hasSignature() || tecnicoSignature2.hasSignature())
                            && !isLoading )
                 ) {
                    if (isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                            Text("Salvando...")
                        }
                    } else {
                        Text("Finalizar Relatório")
                    }
                }
            }
        }
    }
}

@Composable
fun SignaturePad(
    state: SignatureState,
    onClear: () -> Unit,
    onSizeChanged: (width: Int, height: Int) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        onSizeChanged(size.width, size.height)
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                // Cria um novo traço
                                state.paths.add(mutableStateListOf(offset))
                            },
                            onDrag = { change, _ ->
                                // Adiciona novos pontos ao traço atual
                                state.paths.lastOrNull()?.add(change.position)
                            }
                        )
                    }
            ) {
                state.paths.forEach { pathPoints ->
                    val path = Path()
                    if (pathPoints.isNotEmpty()) {
                        path.moveTo(pathPoints[0].x, pathPoints[0].y)
                        for (i in 1 until pathPoints.size) {
                            path.lineTo(pathPoints[i].x, pathPoints[i].y)
                        }
                        drawPath(
                            path,
                            Color.Black,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onClear,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1A4A5C)
                ),
                border = BorderStroke(1.dp, Color.LightGray),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text("Limpar")
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatorioAssinaturaPreview() {
    AprimortechTheme {
        RelatorioAssinaturaScreen(navController = rememberNavController())
    }
}

private fun getCurrentDate(): String {
    val calendar = java.util.Calendar.getInstance()
    val year = calendar.get(java.util.Calendar.YEAR)
    val month = calendar.get(java.util.Calendar.MONTH) + 1
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    return String.format(java.util.Locale.getDefault(), "%02d/%02d/%04d", day, month, year)
}
