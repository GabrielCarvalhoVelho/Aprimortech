package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.BorderStroke
import com.example.aprimortech.model.Peca
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.PecaViewModel
import com.example.aprimortech.ui.viewmodel.PecaViewModelFactory
import java.util.UUID
import java.text.NumberFormat
import java.util.Locale

private val Brand = Color(0xFF1A4A5C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PecasScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PecaViewModel = viewModel(
        factory = PecaViewModelFactory(
            buscarPecasUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarPecasUseCase,
            salvarPecaUseCase = (LocalContext.current.applicationContext as AprimortechApplication).salvarPecaUseCase,
            excluirPecaUseCase = (LocalContext.current.applicationContext as AprimortechApplication).excluirPecaUseCase,
            sincronizarPecasUseCase = (LocalContext.current.applicationContext as AprimortechApplication).sincronizarPecasUseCase,
            pecaRepository = (LocalContext.current.applicationContext as AprimortechApplication).pecaRepository
        )
    )
) {
    val context = LocalContext.current

    android.util.Log.w("PECA_FIREBASE_TEST", "=== PECAS SCREEN INICIADA ===")

    var query by remember { mutableStateOf("") }
    val pecas by viewModel.pecas.collectAsState()
    val fabricantesDisponiveis by viewModel.fabricantesDisponiveis.collectAsState()
    val categoriasDisponiveis by viewModel.categoriasDisponiveis.collectAsState()
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()

    val listaFiltrada = remember(pecas, query) {
        android.util.Log.d("PecasScreen", "Filtrando ${pecas.size} peças com query: '$query'")
        if (query.isBlank()) pecas else pecas.filter { peca ->
            peca.nome.contains(query, ignoreCase = true) ||
            peca.codigo.contains(query, ignoreCase = true) ||
            peca.fabricante.contains(query, ignoreCase = true) ||
            peca.categoria.contains(query, ignoreCase = true)
        }
    }

    var showAddEdit by remember { mutableStateOf(false) }
    var editingPeca by remember { mutableStateOf<Peca?>(null) }

    var showDelete by remember { mutableStateOf(false) }
    var deletingPeca by remember { mutableStateOf<Peca?>(null) }

    var showView by remember { mutableStateOf(false) }
    var viewingPeca by remember { mutableStateOf<Peca?>(null) }

    // Feedback toast
    LaunchedEffect(mensagemOperacao) {
        mensagemOperacao?.let { msg ->
            android.util.Log.d("PecasScreen", "Mostrando mensagem: $msg")
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.limparMensagem()
        }
    }

    // Carregar ao entrar
    LaunchedEffect(Unit) {
        android.util.Log.d("PecasScreen", "Carregando dados ao entrar na tela")
        viewModel.carregarTodosDados()
        // Sincronização automática ao entrar na tela
        android.util.Log.d("PecasScreen", "Iniciando sincronização automática")
        viewModel.sincronizarDadosExistentes()
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Brand)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Peças", style = MaterialTheme.typography.headlineMedium, color = Brand)
            Spacer(Modifier.height(12.dp))

            SectionCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Buscar por nome, código, fabricante ou categoria") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                android.util.Log.w("PECA_FIREBASE_TEST", "=== USUÁRIO CLICOU EM ADICIONAR PEÇA ===")
                                editingPeca = Peca(
                                    id = UUID.randomUUID().toString(),
                                    nome = "",
                                    codigo = "",
                                    descricao = "",
                                    fabricante = "",
                                    categoria = "",
                                    preco = 0.0
                                )
                                showAddEdit = true
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                        ) { Text("Adicionar Peça") }

                        IconButton(
                            onClick = {
                                android.util.Log.w("PECA_FIREBASE_TEST", "=== USUÁRIO CLICOU EM SINCRONIZAR ===")
                                viewModel.sincronizarDadosExistentes()
                            },
                            modifier = Modifier.size(46.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Sync,
                                        contentDescription = "Sincronizar",
                                        tint = Brand,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(listaFiltrada, key = { it.id }) { peca ->
                    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${peca.nome} (${peca.codigo})", style = MaterialTheme.typography.titleMedium, color = Brand)
                            Spacer(Modifier.height(4.dp))
                            Text("Fabricante: ${peca.fabricante}", style = MaterialTheme.typography.bodySmall)
                            Text("Categoria: ${peca.categoria}", style = MaterialTheme.typography.bodySmall)
                            Text("Preço: ${currencyFormatter.format(peca.preco)}", style = MaterialTheme.typography.bodySmall)

                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = {
                                    android.util.Log.d("PecasScreen", "Visualizando peça: ${peca.nome}")
                                    viewingPeca = peca
                                    showView = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Visualizar", tint = Brand)
                                }
                                IconButton(onClick = {
                                    android.util.Log.d("PecasScreen", "Solicitando exclusão da peça: ${peca.nome}")
                                    deletingPeca = peca
                                    showDelete = true
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddEdit && editingPeca != null) {
        AddEditPecaDialog(
            initial = editingPeca!!,
            fabricantesDisponiveis = fabricantesDisponiveis,
            categoriasDisponiveis = categoriasDisponiveis,
            onDismiss = {
                android.util.Log.d("PecasScreen", "Dialog de adicionar/editar cancelado")
                showAddEdit = false; editingPeca = null
            },
            onConfirm = { updated ->
                android.util.Log.w("PECA_FIREBASE_TEST", "=== USUÁRIO CONFIRMOU SALVAMENTO ===")
                android.util.Log.w("PECA_FIREBASE_TEST", "Peça a ser salva: ${updated.nome}")
                viewModel.salvarPeca(updated)
                showAddEdit = false
                editingPeca = null
            }
        )
    }

    if (showDelete && deletingPeca != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false; deletingPeca = null },
            title = { Text("Excluir peça") },
            text = { Text("Tem certeza que deseja excluir \"${deletingPeca!!.nome}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        android.util.Log.w("PECA_FIREBASE_TEST", "=== USUÁRIO CONFIRMOU EXCLUSÃO ===")
                        android.util.Log.w("PECA_FIREBASE_TEST", "Peça a ser excluída: ${deletingPeca!!.nome}")
                        viewModel.excluirPeca(deletingPeca!!)
                        showDelete = false
                        deletingPeca = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White)
                ) { Text("Excluir") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDelete = false; deletingPeca = null }) {
                    Text("Cancelar", color = Brand)
                }
            }
        )
    }

    if (showView && viewingPeca != null) {
        ViewPecaDialog(
            peca = viewingPeca!!,
            onDismiss = { showView = false; viewingPeca = null },
            onEdit = {
                android.util.Log.d("PecasScreen", "Editando peça: ${viewingPeca!!.nome}")
                editingPeca = viewingPeca
                showView = false
                showAddEdit = true
            },
            onDelete = {
                android.util.Log.d("PecasScreen", "Solicitando exclusão da peça: ${viewingPeca!!.nome}")
                deletingPeca = viewingPeca
                showView = false
                showDelete = true
            }
        )
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) { Column(modifier = Modifier.padding(12.dp), content = content) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditPecaDialog(
    initial: Peca,
    fabricantesDisponiveis: List<String>,
    categoriasDisponiveis: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (Peca) -> Unit
) {
    var nome by remember { mutableStateOf(initial.nome) }
    var codigo by remember { mutableStateOf(initial.codigo) }
    var descricao by remember { mutableStateOf(initial.descricao) }
    var fabricante by remember { mutableStateOf(initial.fabricante) }
    var categoria by remember { mutableStateOf(initial.categoria) }
    var precoText by remember { mutableStateOf(if (initial.preco > 0) initial.preco.toString() else "") }

    val salvarHabilitado = nome.isNotBlank() && codigo.isNotBlank() && fabricante.isNotBlank() &&
            categoria.isNotBlank() && precoText.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.nome.isBlank()) "Nova Peça" else "Editar Peça") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome da Peça *") },
                    placeholder = { Text("Ex: Filtro de Ar, Correia") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it.uppercase() },
                    label = { Text("Código *") },
                    placeholder = { Text("Ex: FLT001, PC002") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    placeholder = { Text("Descrição detalhada da peça") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = textFieldColors()
                )

                // Campo Fabricante com dropdown autocomplete
                var fabricanteExpanded by remember { mutableStateOf(false) }
                val fabricantesFiltrados = remember(fabricante, fabricantesDisponiveis) {
                    if (fabricante.isBlank()) fabricantesDisponiveis.take(5)
                    else fabricantesDisponiveis.filter { it.contains(fabricante, ignoreCase = true) }.take(5)
                }

                ExposedDropdownMenuBox(
                    expanded = fabricanteExpanded && fabricantesFiltrados.isNotEmpty(),
                    onExpandedChange = { fabricanteExpanded = it }
                ) {
                    OutlinedTextField(
                        value = fabricante,
                        onValueChange = {
                            fabricante = it
                            fabricanteExpanded = it.isNotEmpty() && fabricantesFiltrados.isNotEmpty()
                        },
                        label = { Text("Fabricante *") },
                        placeholder = { Text("Ex: Hitachi, Videojet, Domino") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )
                    if (fabricantesFiltrados.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = fabricanteExpanded,
                            onDismissRequest = { fabricanteExpanded = false }
                        ) {
                            fabricantesFiltrados.forEach { sugestao ->
                                DropdownMenuItem(
                                    text = { Text(sugestao) },
                                    onClick = {
                                        fabricante = sugestao
                                        fabricanteExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Campo Categoria com dropdown autocomplete
                var categoriaExpanded by remember { mutableStateOf(false) }
                val categoriasFiltradas = remember(categoria, categoriasDisponiveis) {
                    if (categoria.isBlank()) categoriasDisponiveis.take(5)
                    else categoriasDisponiveis.filter { it.contains(categoria, ignoreCase = true) }.take(5)
                }

                ExposedDropdownMenuBox(
                    expanded = categoriaExpanded && categoriasFiltradas.isNotEmpty(),
                    onExpandedChange = { categoriaExpanded = it }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {
                            categoria = it
                            categoriaExpanded = it.isNotEmpty() && categoriasFiltradas.isNotEmpty()
                        },
                        label = { Text("Categoria *") },
                        placeholder = { Text("Ex: Filtros, Cabeças, Tintas") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )
                    if (categoriasFiltradas.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = categoriaExpanded,
                            onDismissRequest = { categoriaExpanded = false }
                        ) {
                            categoriasFiltradas.forEach { sugestao ->
                                DropdownMenuItem(
                                    text = { Text(sugestao) },
                                    onClick = {
                                        categoria = sugestao
                                        categoriaExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = precoText,
                        onValueChange = { newValue ->
                            // Permitir apenas números e vírgula/ponto decimal
                            val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                                .replace(',', '.')
                            precoText = filtered
                        },
                        label = { Text("Preço Unitário *") },
                        placeholder = { Text("Ex: 45.90") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors()
                    )
                }

                // Informação sobre campos obrigatórios
                Text(
                    "* Campos obrigatórios",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val preco = precoText.toDoubleOrNull() ?: 0.0

                    android.util.Log.d("AddEditPecaDialog", "Convertendo dados: preco=$preco")

                    onConfirm(
                        initial.copy(
                            nome = nome.trim(),
                            codigo = codigo.trim(),
                            descricao = descricao.trim(),
                            fabricante = fabricante.trim(),
                            categoria = categoria.trim(),
                            preco = preco
                        )
                    )
                },
                enabled = salvarHabilitado,
                colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                shape = RoundedCornerShape(6.dp)
            ) { Text("Salvar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(6.dp)) {
                Text("Cancelar", color = Brand)
            }
        }
    )
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Color.LightGray,
    unfocusedBorderColor = Color.LightGray
)

@Composable
private fun ViewPecaDialog(
    peca: Peca,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Peça") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nome: ${peca.nome}")
                Text("Código: ${peca.codigo}")
                Text("Descrição: ${peca.descricao}")
                Text("Fabricante: ${peca.fabricante}")
                Text("Categoria: ${peca.categoria}")
                Text("Preço: ${currencyFormatter.format(peca.preco)}")
            }
        },
        confirmButton = {
            Button(onClick = onEdit, colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White)) {
                Text("Editar")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss) { Text("Fechar", color = Brand) }
                OutlinedButton(onClick = onDelete, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Excluir")
                }
            }
        }
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PecasScreenPreview() {
    AprimortechTheme {
        PecasScreen(navController = rememberNavController())
    }
}
