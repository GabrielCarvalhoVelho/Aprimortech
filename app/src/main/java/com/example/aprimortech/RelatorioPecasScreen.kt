package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.model.Peca
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.PecaViewModel
import com.example.aprimortech.ui.viewmodel.PecaViewModelFactory
import com.example.aprimortech.ui.viewmodel.RelatorioSharedViewModel
import kotlinx.coroutines.delay
import android.widget.Toast
import androidx.compose.material3.MenuAnchorType
import java.util.UUID

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
    clienteId: String = "",
    sharedViewModel: RelatorioSharedViewModel = viewModel()
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
    // Usar mutableStateListOf para evitar warnings ao criar MutableState com coleção mutável
    val pecas = remember { mutableStateListOf<PecaUiModel>() }
    var novaPeca by remember { mutableStateOf(PecaUiModel()) }
    var editIndex by remember { mutableStateOf<Int?>(null) }

    // Estados para busca e autocomplete
    var codigoBusca by remember { mutableStateOf("") }
    var debouncedCodigoBusca by remember { mutableStateOf("") }
    var expandedCodigos by remember { mutableStateOf(false) }
    var pecaSelecionada by remember { mutableStateOf<Peca?>(null) }

    // Estados para modais de gerenciamento de peças no catálogo
    var showAddEditCatalogo by remember { mutableStateOf(false) }
    var editingPecaCatalogo by remember { mutableStateOf<Peca?>(null) }
    var showDeleteCatalogo by remember { mutableStateOf(false) }
    var deletingPecaCatalogo by remember { mutableStateOf<Peca?>(null) }

    // Decodificar as observações que vêm da tela anterior
    val observacoesDecodificadas = remember {
        try {
            java.net.URLDecoder.decode(observacoes, "UTF-8")
        } catch (_: Exception) {
            observacoes
        }
    }

    // Debounce para busca de código
    LaunchedEffect(codigoBusca) {
        delay(300)
        debouncedCodigoBusca = codigoBusca
    }

    // Filtrar peças por código ou descrição
    val sugestoesCodigos = remember(debouncedCodigoBusca, pecasDisponiveis) {
        if (debouncedCodigoBusca.isBlank()) emptyList()
        else pecasDisponiveis.filter {
            it.codigo.contains(debouncedCodigoBusca, ignoreCase = true) ||
            it.descricao.contains(debouncedCodigoBusca, ignoreCase = true)
        }
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
                descricao = peca.descricao,
                valorUnit = peca.valorUnitario
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

            // Campo Código com autocomplete e botão + para adicionar nova peça ao catálogo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedCodigos && sugestoesCodigos.isNotEmpty(),
                    onExpandedChange = { expandedCodigos = it },
                    modifier = Modifier.weight(1f)
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
                                        Text(text = peca.descricao, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
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

                // Botão + discreto para adicionar nova peça ao catálogo
                IconButton(
                    onClick = {
                        editingPecaCatalogo = Peca(
                            id = UUID.randomUUID().toString(),
                            codigo = "",
                            descricao = "",
                            valorUnitario = 0.0
                        )
                        showAddEditCatalogo = true
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .offset(y = 8.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Adicionar nova peça ao catálogo",
                        tint = Color(0xFF1A4A5C)
                    )
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
                        Text("${pecaSelecionada!!.codigo} - ${pecaSelecionada!!.descricao}", style = MaterialTheme.typography.bodyMedium)
                        if (pecaSelecionada!!.valorUnitario > 0) {
                            Text("Valor cadastrado: R$ %.2f".format(pecaSelecionada!!.valorUnitario), style = MaterialTheme.typography.bodySmall)
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
                                descricao = novaPeca.descricao,
                                valorUnitario = novaPeca.valorUnit
                            )
                            pecaViewModel.salvarPeca(novaPecaFirebase)
                        }

                        if (editIndex == null) {
                            // adiciona
                            pecas.add(novaPeca)
                        } else {
                            // edita
                            pecas[editIndex!!] = novaPeca
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

                            // Mostrar quantidade e, se existir, o valor unitário
                            val unitText = if (peca.valorUnit > 0.0) " • Unit: R$ %.2f".format(peca.valorUnit) else ""
                            Text("Qtd: ${peca.quantidade}$unitText")

                            // Mostrar total apenas se houver valor unitário
                            if (peca.valorUnit > 0.0) {
                                Text("Total: R$ %.2f".format(peca.valorTotal))
                            } else {
                                Text("Total: -")
                            }

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
                                    pecas.removeAt(index)
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

            // Botão de navegação
            Button(
                onClick = {
                    // Salvar peças no ViewModel compartilhado
                    sharedViewModel.setPecas(
                        pecas.map { peca ->
                            com.example.aprimortech.ui.viewmodel.PecaData(
                                codigo = peca.codigo,
                                descricao = peca.descricao,
                                quantidade = peca.quantidade,
                                valorUnitario = if (peca.valorUnit == 0.0) null else peca.valorUnit
                            )
                        }
                    )

                    // Passar dados para próxima etapa incluindo as peças
                    val defeitosString = defeitos
                    val servicosString = servicos
                    val observacoesEncoded = java.net.URLEncoder.encode(observacoesDecodificadas, "UTF-8")
                    val pecasJson = pecas.joinToString("|") { "${it.codigo};${it.descricao};${it.quantidade};${if (it.valorUnit == 0.0) "" else it.valorUnit}" }

                    navController.navigate("relatorioEtapa5?defeitos=$defeitosString&servicos=$servicosString&observacoes=$observacoesEncoded&pecas=$pecasJson&clienteId=$clienteId")
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
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

        // Modal para adicionar/editar peça no catálogo
        if (showAddEditCatalogo && editingPecaCatalogo != null) {
            AddEditPecaCatalogoDialog(
                initial = editingPecaCatalogo!!,
                onDismiss = {
                    showAddEditCatalogo = false
                    editingPecaCatalogo = null
                },
                onConfirm = { updated ->
                    pecaViewModel.salvarPeca(updated)
                    // O método salvarPeca já atualiza a lista automaticamente
                    showAddEditCatalogo = false
                    editingPecaCatalogo = null
                }
            )
        }

        // Modal para confirmar exclusão de peça do catálogo
        if (showDeleteCatalogo && deletingPecaCatalogo != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteCatalogo = false
                    deletingPecaCatalogo = null
                },
                title = { Text("Excluir peça do catálogo") },
                text = { Text("Tem certeza que deseja excluir \"${deletingPecaCatalogo!!.codigo}\" do catálogo?") },
                confirmButton = {
                    Button(
                        onClick = {
                            pecaViewModel.excluirPeca(deletingPecaCatalogo!!.id)
                            showDeleteCatalogo = false
                            deletingPecaCatalogo = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A4A5C),
                            contentColor = Color.White
                        )
                    ) { Text("Excluir") }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        showDeleteCatalogo = false
                        deletingPecaCatalogo = null
                    }) {
                        Text("Cancelar", color = Color(0xFF1A4A5C))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPecaCatalogoDialog(
    initial: Peca,
    onDismiss: () -> Unit,
    onConfirm: (Peca) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var codigo by remember { mutableStateOf(initial.codigo) }
    var descricao by remember { mutableStateOf(initial.descricao) }
    var valorUnitarioText by remember { mutableStateOf(if (initial.valorUnitario > 0) initial.valorUnitario.toString() else "") }

    // Tornar o valor unitário opcional: apenas código e descrição são obrigatórios
    val salvarHabilitado = codigo.isNotBlank() && descricao.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.codigo.isBlank()) "Nova Peça no Catálogo" else "Editar Peça do Catálogo") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it.uppercase() },
                    label = { Text("Código *") },
                    placeholder = { Text("Ex: PEC001, FLT002") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true
                )

                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição *") },
                    placeholder = { Text("Ex: Filtro de ar, Correia dentada") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = valorUnitarioText,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                            .replace(',', '.')
                        valorUnitarioText = filtered
                    },
                    label = { Text("Valor Unitário (R$) (opcional)") },
                    placeholder = { Text("Ex: 45.90") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                Text(
                    "* Campos obrigatórios (Valor unitário é opcional)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    keyboardController?.hide()
                    val valorUnitario = valorUnitarioText.toDoubleOrNull() ?: 0.0

                    onConfirm(
                        initial.copy(
                            codigo = codigo.trim(),
                            descricao = descricao.trim(),
                            valorUnitario = valorUnitario
                        )
                    )
                },
                enabled = salvarHabilitado,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1A4A5C),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp)
            ) { Text("Salvar") }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    keyboardController?.hide()
                    onDismiss()
                },
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Cancelar", color = Color(0xFF1A4A5C))
            }
        }
    )
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
