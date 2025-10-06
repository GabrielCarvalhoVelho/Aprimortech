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
import com.example.aprimortech.model.Relatorio
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
    val clienteSignature = remember { SignatureState() }
    val tecnicoSignature = remember { SignatureState() }

    // Recupera relatório em progresso
    val relatorioFinal = navController.currentBackStackEntryAsState().value
        ?.savedStateHandle
        ?.get<Relatorio>("relatorioFinal")

    // Estados do ViewModel
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()

    // Feedback toast
    LaunchedEffect(mensagemOperacao) {
        mensagemOperacao?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.limparMensagem()
            if (msg.contains("sucesso")) {
                navController.navigate("relatorioFinalizado") {
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
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
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

            // Botões de navegação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1A4A5C)
                    ),
                    border = BorderStroke(0.dp, Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text("Anterior")
                }

                Button(
                    onClick = {
                        // Finaliza e salva o relatório completo
                        if (relatorioFinal != null) {
                            val relatorioCompleto = relatorioFinal.copy(
                                // Converter assinaturas para base64 (implementação simplificada)
                                assinaturaCliente = if (clienteSignature.hasSignature()) "ASSINATURA_CLIENTE_BASE64" else null,
                                assinaturaTecnico = if (tecnicoSignature.hasSignature()) "ASSINATURA_TECNICO_BASE64" else null,
                                syncPending = false // Relatório finalizado
                            )
                            viewModel.salvarRelatorio(relatorioCompleto)
                        }
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A4A5C),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    enabled = clienteSignature.hasSignature() && tecnicoSignature.hasSignature()
                ) {
                    Text("Finalizar Relatório")
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
