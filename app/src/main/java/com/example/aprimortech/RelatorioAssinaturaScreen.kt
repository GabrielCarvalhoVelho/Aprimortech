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
    val clienteSignature = remember { SignatureState() }
    val tecnicoSignature = remember { SignatureState() }
    var isLoading by remember { mutableStateOf(false) }

    // Usa o relatório passado como parâmetro ou tenta recuperar do savedStateHandle
    val relatorioFinal = relatorioInicial ?: navController.currentBackStackEntryAsState().value
        ?.savedStateHandle
        ?.get<Relatorio>("relatorioFinal")

    // Buscar dados do equipamento do SharedViewModel
    val relatorioCompleto by sharedViewModel.relatorioCompleto.collectAsState()

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
                // Construir e salvar o RelatorioCompleto no ViewModel compartilhado
                val relatorioCompletoFinal = sharedViewModel.buildRelatorioCompleto(
                    relatorioId = relatorioSalvoId!!,
                    dataRelatorio = getCurrentDate()
                )

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

                // Assinatura do Cliente
                Text("Assinatura do Cliente", style = MaterialTheme.typography.titleMedium)
                SignaturePad(
                    state = clienteSignature,
                    onClear = { clienteSignature.clear() }
                )

                // Assinatura do Técnico
                Text("Assinatura do Técnico", style = MaterialTheme.typography.titleMedium)
                SignaturePad(
                    state = tecnicoSignature,
                    onClear = { tecnicoSignature.clear() }
                )

                Spacer(Modifier.height(24.dp))

                // Botão de finalização
                Button(
                    onClick = {
                        if (relatorioFinal != null) {
                            isLoading = true

                            // Converte assinaturas para Bitmap
                            val bitmapAssinaturaCliente = if (clienteSignature.hasSignature()) {
                                SignatureUtils.signatureToBitmap(clienteSignature.paths)
                            } else null

                            val bitmapAssinaturaTecnico = if (tecnicoSignature.hasSignature()) {
                                SignatureUtils.signatureToBitmap(tecnicoSignature.paths)
                            } else null

                            // Salvar assinaturas no ViewModel compartilhado
                            val assinaturaClienteBase64 = bitmapAssinaturaCliente?.let {
                                SignatureUtils.bitmapToBase64(it)
                            }
                            val assinaturaTecnicoBase64 = bitmapAssinaturaTecnico?.let {
                                SignatureUtils.bitmapToBase64(it)
                            }

                            sharedViewModel.setAssinaturas(
                                assinaturaTecnico = assinaturaTecnicoBase64,
                                assinaturaCliente = assinaturaClienteBase64,
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
                                assinaturaCliente = bitmapAssinaturaCliente,
                                assinaturaTecnico = bitmapAssinaturaTecnico
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
                    enabled = clienteSignature.hasSignature() && tecnicoSignature.hasSignature() && !isLoading
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
fun SignaturePad(state: SignatureState, onClear: () -> Unit) {
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
    return String.format("%02d/%02d/%04d", day, month, year)
}
