package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme

private val Brand = Color(0xFF1A4A5C)

// ✅ Data class declarada apenas aqui
data class PecasUIModel(
    val id: Int,
    var codigo: String,
    var descricao: String,
    var quantidade: String,
    var valorUnitario: String,
    var valorTotal: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PecasScreen(navController: NavController, modifier: Modifier = Modifier) {
    var pecas by remember {
        mutableStateOf(
            listOf(
                PecasUIModel(1, "PC001", "Correia Transportadora", "5", "120.00", "600.00"),
                PecasUIModel(2, "PC002", "Rolamento Esférico", "10", "45.00", "450.00"),
                PecasUIModel(3, "PC003", "Motor Elétrico 5CV", "2", "1500.00", "3000.00")
            )
        )
    }
    var idCounter by remember { mutableIntStateOf(4) }

    var query by remember { mutableStateOf("") }
    val listaFiltrada = remember(pecas, query) {
        if (query.isBlank()) pecas else pecas.filter { it.descricao.contains(query, ignoreCase = true) }
    }

    var showAddEdit by remember { mutableStateOf(false) }
    var editingPeca by remember { mutableStateOf<PecasUIModel?>(null) }

    var showDelete by remember { mutableStateOf(false) }
    var deletingPeca by remember { mutableStateOf<PecasUIModel?>(null) }

    var showView by remember { mutableStateOf(false) }
    var viewingPeca by remember { mutableStateOf<PecasUIModel?>(null) }

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

            // Busca + Botão
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Buscar por descrição") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )
                    Button(
                        onClick = {
                            editingPeca = PecasUIModel(idCounter, "", "", "", "", "")
                            showAddEdit = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(peca.descricao, style = MaterialTheme.typography.titleMedium, color = Brand)
                            Spacer(Modifier.height(4.dp))
                            Text("Código: ${peca.codigo}", style = MaterialTheme.typography.bodySmall)
                            Text("Qtd: ${peca.quantidade} • V. Unit: ${peca.valorUnitario} • V. Total: ${peca.valorTotal}", style = MaterialTheme.typography.bodySmall)

                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                IconButton(onClick = {
                                    viewingPeca = peca
                                    showView = true
                                }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Brand)
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

    // Modal Adicionar/Editar
    if (showAddEdit && editingPeca != null) {
        AddEditPecaDialog(
            initial = editingPeca!!,
            onDismiss = { showAddEdit = false; editingPeca = null },
            onConfirm = { updated ->
                pecas = if (pecas.any { it.id == updated.id }) {
                    pecas.map { if (it.id == updated.id) updated else it }
                } else {
                    pecas + updated.also { idCounter += 1 }
                }
                showAddEdit = false
                editingPeca = null
            }
        )
    }

    // Modal Deletar
    if (showDelete && deletingPeca != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false; deletingPeca = null },
            title = { Text("Excluir peça") },
            text = { Text("Tem certeza que deseja excluir \"${deletingPeca!!.descricao}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        pecas = pecas.filterNot { it.id == deletingPeca!!.id }
                        showDelete = false
                        deletingPeca = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp)
                ) { Text("Excluir") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDelete = false; deletingPeca = null }) {
                    Text("Cancelar", color = Brand)
                }
            }
        )
    }

    // Modal Visualizar
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
private fun AddEditPecaDialog(
    initial: PecasUIModel,
    onDismiss: () -> Unit,
    onConfirm: (PecasUIModel) -> Unit
) {
    var codigo by remember { mutableStateOf(initial.codigo) }
    var descricao by remember { mutableStateOf(initial.descricao) }
    var quantidade by remember { mutableStateOf(initial.quantidade) }
    var valorUnitario by remember { mutableStateOf(initial.valorUnitario) }
    var valorTotal by remember { mutableStateOf(initial.valorTotal) }

    val salvarHabilitado = codigo.isNotBlank() && descricao.isNotBlank() && quantidade.isNotBlank() && valorUnitario.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.descricao.isBlank()) "Nova Peça" else "Editar Peça") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = codigo, onValueChange = { codigo = it }, label = { Text("Código") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())
                OutlinedTextField(value = descricao, onValueChange = { descricao = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth(), colors = textFieldColors())
                OutlinedTextField(value = quantidade, onValueChange = { quantidade = it }, label = { Text("Quantidade") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = textFieldColors())
                OutlinedTextField(value = valorUnitario, onValueChange = { valorUnitario = it }, label = { Text("Valor Unitário") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), colors = textFieldColors())
                OutlinedTextField(value = valorTotal, onValueChange = { valorTotal = it }, label = { Text("Valor Total") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), colors = textFieldColors())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        initial.copy(
                            codigo = codigo.trim(),
                            descricao = descricao.trim(),
                            quantidade = quantidade.trim(),
                            valorUnitario = valorUnitario.trim(),
                            valorTotal = valorTotal.trim()
                        )
                    )
                },
                enabled = salvarHabilitado,
                colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                shape = RoundedCornerShape(6.dp)
            ) { Text("Salvar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancelar", color = Brand) }
        }
    )
}

@Composable
private fun ViewPecaDialog(
    peca: PecasUIModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Peça") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Código: ${peca.codigo}")
                Text("Descrição: ${peca.descricao}")
                Text("Quantidade: ${peca.quantidade}")
                Text("Valor Unitário: ${peca.valorUnitario}")
                Text("Valor Total: ${peca.valorTotal}")
            }
        },
        confirmButton = {
            Button(onClick = onEdit, colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White)) { Text("Editar") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss) { Text("Fechar", color = Brand) }
                OutlinedButton(onClick = onDelete, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Excluir") }
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

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PecasScreenPreview() {
    AprimortechTheme {
        PecasScreen(navController = rememberNavController())
    }
}
