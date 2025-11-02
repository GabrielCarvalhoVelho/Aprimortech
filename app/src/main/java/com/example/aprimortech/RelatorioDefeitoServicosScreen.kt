package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.DefeitoViewModel
import com.example.aprimortech.ui.viewmodel.DefeitoViewModelFactory
import com.example.aprimortech.ui.viewmodel.ServicoViewModel
import com.example.aprimortech.ui.viewmodel.ServicoViewModelFactory
import com.example.aprimortech.ui.viewmodel.RelatorioSharedViewModel
import android.widget.Toast
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioDefeitoServicosScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    clienteId: String = "",
    sharedViewModel: RelatorioSharedViewModel = viewModel()
) {
    val context = LocalContext.current

    // Usar repositórios da Application em vez de criar novas instâncias
    val application = context.applicationContext as AprimortechApplication

    val defeitoViewModel: DefeitoViewModel = viewModel(
        factory = DefeitoViewModelFactory(application.defeitoRepository)
    )
    val servicoViewModel: ServicoViewModel = viewModel(
        factory = ServicoViewModelFactory(application.servicoRepository)
    )

    // Estados dos ViewModels
    val defeitos by defeitoViewModel.defeitos.collectAsState()
    val servicos by servicoViewModel.servicos.collectAsState()
    val isLoadingDefeitos by defeitoViewModel.isLoading.collectAsState()
    val isLoadingServicos by servicoViewModel.isLoading.collectAsState()
    val errorMessageDefeitos by defeitoViewModel.errorMessage.collectAsState()
    val errorMessageServicos by servicoViewModel.errorMessage.collectAsState()

    // Estados locais para seleção
    val defeitosSelecionados = remember { mutableStateListOf<String>() }
    val servicosSelecionados = remember { mutableStateListOf<String>() }

    var novoDefeito by remember { mutableStateOf("") }
    var novoServico by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }

    // Mostrar erros via Toast
    LaunchedEffect(errorMessageDefeitos) {
        errorMessageDefeitos?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            defeitoViewModel.limparErro()
        }
    }

    LaunchedEffect(errorMessageServicos) {
        errorMessageServicos?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            servicoViewModel.limparErro()
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ====== Defeitos ======
            Text(
                text = "Defeitos Identificados",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    if (isLoadingDefeitos) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF1A4A5C))
                        }
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            defeitos.forEach { defeito ->
                                FilterChip(
                                    // normaliza comparação usando uppercase para consistência
                                    selected = defeitosSelecionados.contains(defeito.nome.uppercase(Locale.getDefault())),
                                    onClick = {
                                        val key = defeito.nome.uppercase(Locale.getDefault())
                                        if (defeitosSelecionados.contains(key)) {
                                            defeitosSelecionados.remove(key)
                                        } else {
                                            defeitosSelecionados.add(key)
                                            // Incrementar uso no banco de dados usando versão maiúscula
                                            defeitoViewModel.salvarDefeito(key)
                                        }
                                    },
                                    label = { Text(defeito.nome) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = novoDefeito,
                        // força tudo em MAIÚSCULAS enquanto o usuário digita
                        onValueChange = { novoDefeito = it.uppercase(Locale.getDefault()) },
                        label = { Text("Adicionar novo defeito") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (novoDefeito.isNotBlank()) {
                                // novoDefeito já está em uppercase
                                val key = novoDefeito.uppercase(Locale.getDefault())
                                defeitoViewModel.salvarDefeito(key) { defeitoId ->
                                    defeitosSelecionados.add(key)
                                    novoDefeito = ""
                                    Toast.makeText(context, "Defeito adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A4A5C),
                            contentColor = Color.White
                        ),
                        enabled = novoDefeito.isNotBlank() && !isLoadingDefeitos
                    ) {
                        if (isLoadingDefeitos) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Adicionar")
                        }
                    }
                }
            }

            // ====== Serviços ======
            Text(
                text = "Serviços Realizados",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    if (isLoadingServicos) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF1A4A5C))
                        }
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            servicos.forEach { servico ->
                                FilterChip(
                                    selected = servicosSelecionados.contains(servico.nome.uppercase(Locale.getDefault())),
                                    onClick = {
                                        val key = servico.nome.uppercase(Locale.getDefault())
                                        if (servicosSelecionados.contains(key)) {
                                            servicosSelecionados.remove(key)
                                        } else {
                                            servicosSelecionados.add(key)
                                            // Incrementar uso no banco de dados usando versão maiúscula
                                            servicoViewModel.salvarServico(key)
                                        }
                                    },
                                    label = { Text(servico.nome) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = novoServico,
                        // força tudo em MAIÚSCULAS enquanto o usuário digita
                        onValueChange = { novoServico = it.uppercase(Locale.getDefault()) },
                        label = { Text("Adicionar novo serviço") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (novoServico.isNotBlank()) {
                                val key = novoServico.uppercase(Locale.getDefault())
                                servicoViewModel.salvarServico(key) { servicoId ->
                                    servicosSelecionados.add(key)
                                    novoServico = ""
                                    Toast.makeText(context, "Serviço adicionado com sucesso!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A4A5C),
                            contentColor = Color.White
                        ),
                        enabled = novoServico.isNotBlank() && !isLoadingServicos
                    ) {
                        if (isLoadingServicos) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White
                            )
                        } else {
                            Text("Adicionar")
                        }
                    }
                }
            }

            // ====== Observações ======
            Text(
                text = "Observações",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                OutlinedTextField(
                    value = observacoes,
                    // força tudo em MAIÚSCULAS enquanto o usuário digita
                    onValueChange = { observacoes = it.uppercase(Locale.getDefault()) },
                    placeholder = { Text("Digite observações adicionais...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .padding(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            // ====== Navegação ======
            Spacer(Modifier.height(16.dp))

            // Botão Continuar com validação
            Button(
                onClick = {
                    // Validação: verificar se há pelo menos 1 defeito e 1 serviço
                    if (defeitosSelecionados.isEmpty()) {
                        Toast.makeText(context, "Selecione pelo menos 1 defeito identificado", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    if (servicosSelecionados.isEmpty()) {
                        Toast.makeText(context, "Selecione pelo menos 1 serviço realizado", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // Salvar dados de defeitos e serviços no ViewModel compartilhado
                    sharedViewModel.setDefeitosServicos(
                        defeitos = defeitosSelecionados.toList(),
                        servicos = servicosSelecionados.toList(),
                        observacoes = observacoes
                    )

                    // Passar dados para a próxima etapa
                    val defeitosString = defeitosSelecionados.joinToString(",")
                    val servicosString = servicosSelecionados.joinToString(",")
                    val observacoesEncoded = java.net.URLEncoder.encode(observacoes, "UTF-8")

                    navController.navigate("relatorioEtapa4?defeitos=$defeitosString&servicos=$servicosString&observacoes=$observacoesEncoded&clienteId=$clienteId")
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = defeitosSelecionados.isNotEmpty() && servicosSelecionados.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A4A5C),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Continuar", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatorioDefeitoServicosScreenPreview() {
    AprimortechTheme {
        RelatorioDefeitoServicosScreen(navController = rememberNavController())
    }
}
