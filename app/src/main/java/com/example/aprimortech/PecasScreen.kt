package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.model.Peca
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.example.aprimortech.ui.viewmodel.PecaViewModel
import com.example.aprimortech.ui.viewmodel.PecaViewModelFactory
import java.util.UUID
import java.text.NumberFormat
import java.util.Locale
import kotlinx.coroutines.delay

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
    val keyboardController = LocalSoftwareKeyboardController.current

    var query by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }

    LaunchedEffect(query) {
        delay(300)
        debouncedQuery = query
    }

    val pecas by viewModel.pecas.collectAsState()
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()

    val listaFiltrada = remember(pecas, debouncedQuery) {
        if (debouncedQuery.isBlank()) pecas else pecas.filter { peca ->
            peca.codigo.contains(debouncedQuery, ignoreCase = true) ||
            peca.descricao.contains(debouncedQuery, ignoreCase = true)
        }
    }

    var showAddEdit by remember { mutableStateOf(false) }
    var editingPeca by remember { mutableStateOf<Peca?>(null) }
    var showDelete by remember { mutableStateOf(false) }
    var deletingPeca by remember { mutableStateOf<Peca?>(null) }
    var showView by remember { mutableStateOf(false) }
    var viewingPeca by remember { mutableStateOf<Peca?>(null) }

    LaunchedEffect(mensagemOperacao) {
        mensagemOperacao?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.limparMensagem()
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
                        label = { Text("Buscar por código ou descrição") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            editingPeca = Peca(
                                id = UUID.randomUUID().toString(),
                                codigo = "",
                                descricao = "",
                                valorUnitario = 0.0
                            )
                            showAddEdit = true
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) { Text("Adicionar Peça") }
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
                            Text(peca.codigo, style = MaterialTheme.typography.titleMedium, color = Brand)
                            Spacer(Modifier.height(4.dp))
                            Text("Descrição: ${peca.descricao}", style = MaterialTheme.typography.bodySmall)
                            Text("Valor Unitário: ${currencyFormatter.format(peca.valorUnitario)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Brand)

                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = {
                                    viewingPeca = peca
                                    showView = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Visualizar", tint = Brand)
                                }
                                IconButton(onClick = {
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
            onDismiss = {
                showAddEdit = false
                editingPeca = null
            },
            onConfirm = { updated ->
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
            text = { Text("Tem certeza que deseja excluir \"${deletingPeca!!.codigo}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.excluirPeca(deletingPeca!!.id)
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
                editingPeca = viewingPeca
                showView = false
                showAddEdit = true
            },
            onDelete = {
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
    onDismiss: () -> Unit,
    onConfirm: (Peca) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var codigo by remember { mutableStateOf(initial.codigo) }
    var descricao by remember { mutableStateOf(initial.descricao) }
    var valorUnitarioText by remember { mutableStateOf(if (initial.valorUnitario > 0) initial.valorUnitario.toString() else "") }

    val salvarHabilitado = codigo.isNotBlank() && descricao.isNotBlank() && valorUnitarioText.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.codigo.isBlank()) "Nova Peça" else "Editar Peça") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Código
                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it.uppercase() },
                    label = { Text("Código *") },
                    placeholder = { Text("Ex: PEC001, FLT002") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true
                )

                // 2. Descrição
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição *") },
                    placeholder = { Text("Ex: Filtro de ar, Correia dentada") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = textFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                // 3. Valor Unitário
                OutlinedTextField(
                    value = valorUnitarioText,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                            .replace(',', '.')
                        valorUnitarioText = filtered
                    },
                    label = { Text("Valor Unitário (R$) *") },
                    placeholder = { Text("Ex: 45.90") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors(),
                    singleLine = true
                )

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
                colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
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
                Text("Código: ${peca.codigo}")
                Text("Descrição: ${peca.descricao}")
                Text("Valor Unitário: ${currencyFormatter.format(peca.valorUnitario)}")
            }
        },
        confirmButton = {
            Button(
                onClick = onEdit,
                colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                shape = RoundedCornerShape(6.dp)
            ) { Text("Editar") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(6.dp)) {
                    Text("Fechar", color = Brand)
                }
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
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
