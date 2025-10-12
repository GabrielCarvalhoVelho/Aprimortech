package com.example.aprimortech

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.model.RelatorioCompleto
import com.example.aprimortech.model.ContatoInfo
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.RelatorioSharedViewModel
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioFinalizadoScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    relatorioId: String? = null,
    sharedViewModel: RelatorioSharedViewModel = viewModel()
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val ptBrLocale = Locale.forLanguageTag("pt-BR")

    val relatorio by sharedViewModel.relatorioCompleto.collectAsState()
    var relatorioFromFirebase by remember { mutableStateOf<RelatorioCompleto?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(relatorioId) {
        android.util.Log.d("RelatorioFinalizado", "=== INICIANDO CARREGAMENTO ===")
        android.util.Log.d("RelatorioFinalizado", "relatorioId recebido: $relatorioId")
        android.util.Log.d("RelatorioFinalizado", "relatorio do ViewModel: $relatorio")

        if (relatorioId != null && relatorioId.isNotEmpty()) {
            isLoading = true
            android.util.Log.d("RelatorioFinalizado", "Buscando relatório no Firebase com ID: $relatorioId")
            try {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val doc = firestore.collection("relatorios").document(relatorioId).get().await()

                android.util.Log.d("RelatorioFinalizado", "Documento existe: ${doc.exists()}")

                if (doc.exists()) {
                    android.util.Log.d("RelatorioFinalizado", "Dados do documento: ${doc.data}")

                    val clienteId = doc.getString("clienteId") ?: ""
                    android.util.Log.d("RelatorioFinalizado", "Buscando cliente com ID: $clienteId")
                    val clienteDoc = firestore.collection("clientes").document(clienteId).get().await()
                    android.util.Log.d("RelatorioFinalizado", "Cliente existe: ${clienteDoc.exists()}")

                    val maquinaId = doc.getString("maquinaId") ?: ""
                    android.util.Log.d("RelatorioFinalizado", "Buscando máquina com ID: $maquinaId")
                    val maquinaDoc = if (maquinaId.isNotEmpty()) {
                        firestore.collection("maquinas").document(maquinaId).get().await()
                    } else null
                    android.util.Log.d("RelatorioFinalizado", "Máquina existe: ${maquinaDoc?.exists()}")

                    relatorioFromFirebase = RelatorioCompleto(
                        id = doc.id,
                        dataRelatorio = doc.getString("dataRelatorio") ?: "",
                        clienteNome = clienteDoc.getString("nome") ?: "",
                        clienteEndereco = clienteDoc.getString("endereco") ?: "",
                        clienteCidade = clienteDoc.getString("cidade") ?: "",
                        clienteEstado = clienteDoc.getString("estado") ?: "",
                        clienteTelefone = clienteDoc.getString("telefone") ?: "",
                        clienteCelular = clienteDoc.getString("celular") ?: "",
                        clienteContatos = (clienteDoc.get("contatos") as? List<*>)?.mapNotNull { item ->
                            (item as? Map<*, *>)?.let {
                                ContatoInfo(
                                    nome = it["nome"] as? String ?: "",
                                    setor = it["setor"] as? String ?: "",
                                    celular = it["celular"] as? String ?: ""
                                )
                            }
                        } ?: emptyList(),
                        equipamentoFabricante = maquinaDoc?.getString("fabricante") ?: "",
                        equipamentoNumeroSerie = maquinaDoc?.getString("numeroSerie") ?: "",
                        equipamentoCodigoConfiguracao = maquinaDoc?.getString("codigoConfiguracao") ?: "",
                        equipamentoModelo = maquinaDoc?.getString("modelo") ?: "",
                        equipamentoIdentificacao = maquinaDoc?.getString("identificacao") ?: "",
                        equipamentoAnoFabricacao = maquinaDoc?.getString("anoFabricacao") ?: "",
                        equipamentoCodigoTinta = doc.getString("codigoTinta") ?: maquinaDoc?.getString("codigoTinta") ?: "",
                        equipamentoCodigoSolvente = doc.getString("codigoSolvente") ?: maquinaDoc?.getString("codigoSolvente") ?: "",
                        equipamentoDataProximaPreventiva = maquinaDoc?.getString("dataProximaPreventiva") ?: "",
                        equipamentoHoraProximaPreventiva = maquinaDoc?.getString("horasProximaPreventiva") ?: "",
                        defeitos = (doc.getString("descricaoServico") ?: "").split(",").filter { it.isNotBlank() },
                        servicos = (doc.getString("descricaoServico") ?: "").split(";").filter { it.isNotBlank() },
                        pecas = emptyList(),
                        horarioEntrada = doc.getString("horarioEntrada") ?: "",
                        horarioSaida = doc.getString("horarioSaida") ?: "",
                        valorHoraTecnica = 150.0,
                        totalHorasTecnicas = 0.0,
                        quantidadeKm = doc.getDouble("distanciaKm") ?: 0.0,
                        valorPorKm = doc.getDouble("valorDeslocamentoPorKm") ?: 0.0,
                        valorPedagios = doc.getDouble("valorPedagios") ?: 0.0,
                        valorTotalDeslocamento = doc.getDouble("valorDeslocamentoTotal") ?: 0.0,
                        assinaturaTecnico = doc.getString("assinaturaTecnico"),
                        assinaturaCliente = doc.getString("assinaturaCliente"),
                        nomeTecnico = "Técnico Aprimortech",
                        observacoes = doc.getString("observacoes") ?: ""
                    )

                    android.util.Log.d("RelatorioFinalizado", "RelatorioCompleto construído com sucesso")
                    android.util.Log.d("RelatorioFinalizado", "Cliente: ${relatorioFromFirebase?.clienteNome}")
                } else {
                    android.util.Log.w("RelatorioFinalizado", "Documento não encontrado no Firebase")
                }
            } catch (e: Exception) {
                android.util.Log.e("RelatorioFinalizado", "Erro ao carregar relatório", e)
                Toast.makeText(context, "Erro ao carregar relatório: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
                android.util.Log.d("RelatorioFinalizado", "isLoading = false")
            }
        } else {
            android.util.Log.d("RelatorioFinalizado", "Nenhum relatorioId fornecido, usando ViewModel")
        }

        android.util.Log.d("RelatorioFinalizado", "relatorioFinal será: ${relatorioFromFirebase ?: relatorio}")
    }

    val relatorioFinal = relatorioFromFirebase ?: relatorio

    android.util.Log.d("RelatorioFinalizado", "Renderizando tela - relatorioFinal: $relatorioFinal, isLoading: $isLoading")

    if (relatorioFinal == null && !isLoading) {
        android.util.Log.d("RelatorioFinalizado", "Exibindo mensagem de 'Nenhum relatório encontrado'")
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Nenhum relatório encontrado",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Gray
                    )
                    Button(
                        onClick = { navController.navigate("novoRelatorio") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A4A5C)
                        )
                    ) {
                        Text("Criar Novo Relatório")
                    }
                }
            }
        }
        return
    }

    if (isLoading) {
        android.util.Log.d("RelatorioFinalizado", "Exibindo loading")
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Image(
                            painter = painterResource(id = R.drawable.logo_aprimortech),
                            contentDescription = "Logo Aprimortech",
                            modifier = Modifier.height(40.dp)
                        )
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    android.util.Log.d("RelatorioFinalizado", "Exibindo conteúdo do relatório")

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
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1A4A5C),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "Relatório Finalizado",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A4A5C)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Data: ${relatorioFinal?.dataRelatorio ?: ""}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            SecaoRelatorio(titulo = "Dados do Cliente") {
                relatorioFinal?.let { rel ->
                    CampoRelatorio("Nome", rel.clienteNome)
                    CampoRelatorio("Endereço", rel.clienteEndereco)
                    CampoRelatorio("Cidade", rel.clienteCidade)
                    CampoRelatorio("Estado", rel.clienteEstado)
                    CampoRelatorio("Telefone", rel.clienteTelefone)
                    CampoRelatorio("Celular", rel.clienteCelular)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SecaoRelatorio(titulo = "Dados do Equipamento") {
                relatorioFinal?.let { rel ->
                    CampoRelatorio("Fabricante", rel.equipamentoFabricante)
                    CampoRelatorio("Modelo", rel.equipamentoModelo)
                    CampoRelatorio("Número de Série", rel.equipamentoNumeroSerie)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("home") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Voltar ao Início")
            }
        }
    }
}

@Composable
fun SecaoRelatorio(
    titulo: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5F5)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A4A5C),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun CampoRelatorio(label: String, valor: String) {
    if (valor.isNotBlank()) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RelatorioFinalizadoPreview() {
    AprimortechTheme {
        RelatorioFinalizadoScreen(
            navController = rememberNavController()
        )
    }
}

