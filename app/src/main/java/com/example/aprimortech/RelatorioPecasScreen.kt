package com.example.aprimortech

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.model.Peca
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.PecaViewModel
import com.example.aprimortech.ui.viewmodel.PecaViewModelFactory
import kotlinx.coroutines.delay
import android.widget.Toast
import androidx.compose.material3.MenuAnchorType

data class PecaUiModel(
    var codigo: String = "",
    var descricao: String = "",
    var quantidade: Int = 0,
    var valorUnit: Double = 0.0
) {
    val valorTotal: Double
        get() = quantidade * valorUnit
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioPecasScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    defeitos: String = "",
    servicos: String = "",
    observacoes: String = "",
    clienteId: String = ""
) {
    val context = LocalContext.current

    // Usar repositório da Application
    val application = context.applicationContext as AprimortechApplication
    val pecaViewModel: PecaViewModel = viewModel(
        factory = PecaViewModelFactory(
            buscarPecasUseCase = application.buscarPecasUseCase,
            salvarPecaUseCase = application.salvarPecaUseCase,
            excluirPecaUseCase = application.excluirPecaUseCase,
            sincronizarPecasUseCase = application.sincronizarPecasUseCase,
            pecaRepository = application.pecaRepository
        )
    )

    // Estados dos ViewModels
    val pecasDisponiveis by pecaViewModel.pecas.collectAsState()
    val mensagemOperacao by pecaViewModel.mensagemOperacao.collectAsState()

    // Estados locais para UI
    var pecas by remember { mutableStateOf(mutableListOf<PecaUiModel>()) }
    var novaPeca by remember { mutableStateOf(PecaUiModel()) }
    var editIndex by remember { mutableStateOf<Int?>(null) }

    // Estados para busca e autocomplete
    var codigoBusca by remember { mutableStateOf("") }
    var debouncedCodigoBusca by remember { mutableStateOf("") }
    var expandedCodigos by remember { mutableStateOf(false) }
    var pecaSelecionada by remember { mutableStateOf<Peca?>(null) }

    // Decodificar as observações que vêm da tela anterior
    val observacoesDecodificadas = remember {
        try {
            java.net.URLDecoder.decode(observacoes, "UTF-8")
        } catch (e: Exception) {
            observacoes
        }
    }

    // Debounce para busca de código
    LaunchedEffect(codigoBusca) {
        delay(300)
        debouncedCodigoBusca = codigoBusca
    }

    // Filtrar peças por código ou nome
    val sugestoesCodigos = remember(debouncedCodigoBusca, pecasDisponiveis) {
        if (debouncedCodigoBusca.isBlank()) emptyList()
        else pecasDisponiveis.filter {
            it.codigo.contains(debouncedCodigoBusca, ignoreCase = true) ||
            it.nome.contains(debouncedCodigoBusca, ignoreCase = true)
        }.take(5)
    }

    // Mostrar mensagens via Toast
    LaunchedEffect(mensagemOperacao) {
        mensagemOperacao?.let { mensagem ->
            Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
            pecaViewModel.limparMensagem()
        }
    }

    // Atualizar campos quando uma peça é selecionada
    LaunchedEffect(pecaSelecionada) {
        pecaSelecionada?.let { peca ->
            novaPeca = novaPeca.copy(
                codigo = peca.codigo,
                descricao = peca.descricao.ifBlank { peca.nome },
                valorUnit = peca.preco
            )
        }
    }

    val totalGeral = pecas.sumOf { it.valorTotal }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Peças Utilizadas",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )

            // Campo Código com autocomplete
            ExposedDropdownMenuBox(
                expanded = expandedCodigos && sugestoesCodigos.isNotEmpty(),
                onExpandedChange = { expandedCodigos = it }
            ) {
                OutlinedTextField(
                    value = pecaSelecionada?.codigo ?: codigoBusca,
                    onValueChange = { newValue ->
                        if (pecaSelecionada == null) {
                            codigoBusca = newValue
                            novaPeca = novaPeca.copy(codigo = newValue)
                            expandedCodigos = newValue.isNotEmpty()
                        } else {
                            // Limpar seleção e começar nova busca
                            pecaSelecionada = null
                            novaPeca = PecaUiModel(codigo = newValue)
                            codigoBusca = newValue
                            expandedCodigos = newValue.isNotEmpty()
                        }
                    },
                    label = { Text("Código") },
                    placeholder = { Text("Digite o código da peça") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCodigos) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedCodigos && sugestoesCodigos.isNotEmpty(),
                    onDismissRequest = { expandedCodigos = false }
                ) {
                    sugestoesCodigos.forEach { peca ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = peca.codigo)
                                    Text(text = peca.nome, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            },
                            onClick = {
                                pecaSelecionada = peca
                                codigoBusca = ""
                                expandedCodigos = false
                            }
                        )
                    }
                }
            }

            // Mostrar peça selecionada
            if (pecaSelecionada != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("✓ Peça Selecionada", style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
                        Text("${pecaSelecionada!!.codigo} - ${pecaSelecionada!!.nome}", style = MaterialTheme.typography.bodyMedium)
                        if (pecaSelecionada!!.preco > 0) {
                            Text("Preço cadastrado: R$ %.2f".format(pecaSelecionada!!.preco), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = novaPeca.descricao,
                onValueChange = { novaPeca = novaPeca.copy(descricao = it) },
                label = { Text("Descrição") },
                placeholder = { Text("Descrição da peça") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            OutlinedTextField(
                value = if (novaPeca.quantidade == 0) "" else novaPeca.quantidade.toString(),
                onValueChange = { qtd ->
                    novaPeca = novaPeca.copy(quantidade = qtd.toIntOrNull() ?: 0)
                },
                label = { Text("Quantidade") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            OutlinedTextField(
                value = if (novaPeca.valorUnit == 0.0) "" else "%.2f".format(novaPeca.valorUnit),
                onValueChange = { valor ->
                    novaPeca = novaPeca.copy(valorUnit = valor.replace(",", ".").toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Valor Unitário (R$)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Button(
                onClick = {
                    if (novaPeca.codigo.isNotBlank() && novaPeca.descricao.isNotBlank()) {
                        // Se não existe uma peça selecionada (nova peça), salvar no Firebase
                        if (pecaSelecionada == null && novaPeca.codigo.isNotBlank()) {
                            val novaPecaFirebase = Peca(
                                codigo = novaPeca.codigo,
                                nome = novaPeca.descricao,
                                descricao = novaPeca.descricao,
                                preco = novaPeca.valorUnit
                            )
                            pecaViewModel.salvarPeca(novaPecaFirebase)
                        }

                        if (editIndex == null) {
                            // adiciona
                            pecas = (pecas + novaPeca).toMutableList()
                        } else {
                            // edita
                            val lista = pecas.toMutableList()
                            lista[editIndex!!] = novaPeca
                            pecas = lista
                            editIndex = null
                        }

                        // Limpar campos
                        novaPeca = PecaUiModel()
                        pecaSelecionada = null
                        codigoBusca = ""
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .height(46.dp),
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A4A5C),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                Text(if (editIndex == null) "Adicionar Peça" else "Salvar Alterações")
            }

            HorizontalDivider(Modifier.padding(vertical = 12.dp))

            // Lista de peças adicionadas
            if (pecas.isNotEmpty()) {
                Text("Peças Adicionadas:", style = MaterialTheme.typography.titleMedium)
                pecas.forEachIndexed { index, peca ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("${peca.codigo} - ${peca.descricao}")
                            Text("Qtd: ${peca.quantidade} • Unit: R$ %.2f".format(peca.valorUnit))
                            Text("Total: R$ %.2f".format(peca.valorTotal))
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                IconButton(onClick = {
                                    novaPeca = peca
                                    editIndex = index
                                    pecaSelecionada = null
                                    codigoBusca = ""
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color(0xFF1A4A5C))
                                }
                                IconButton(onClick = {
                                    pecas = pecas.toMutableList().also { it.removeAt(index) }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "TOTAL GERAL: R$ %.2f".format(totalGeral),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF1A4A5C)
                )
            }

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
                        // Passar dados para próxima etapa incluindo as peças
                        val defeitosString = defeitos
                        val servicosString = servicos
                        val observacoesEncoded = java.net.URLEncoder.encode(observacoesDecodificadas, "UTF-8")
                        val pecasJson = pecas.joinToString("|") { "${it.codigo};${it.descricao};${it.quantidade};${it.valorUnit}" }

                        navController.navigate("relatorioEtapa5?defeitos=$defeitosString&servicos=$servicosString&observacoes=$observacoesEncoded&pecas=$pecasJson&clienteId=$clienteId")
                    },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(46.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1A4A5C),
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text("Próximo")
                }
            }
        }
    }
}

@Composable
fun InputCardPeca(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.padding(4.dp)) {
            content()
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatorioPecasPreview() {
    AprimortechTheme {
        RelatorioPecasScreen(navController = rememberNavController())
    }
}
