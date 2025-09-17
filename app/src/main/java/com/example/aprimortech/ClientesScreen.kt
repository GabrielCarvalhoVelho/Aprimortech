package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

data class ClienteUiModel(
    val id: Int,
    var nome: String,
    var telefone: String,
    var email: String,
    var cidade: String,
    var estado: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(navController: NavController, modifier: Modifier = Modifier) {
    // Estado da lista (mock local)
    var clientes by remember {
        mutableStateOf(
            listOf(
                ClienteUiModel(1, "Indústrias TechFlow", "(11) 99999-1234", "contato@techflow.com", "São Paulo", "SP"),
                ClienteUiModel(2, "Corporação Acme", "(21) 98888-4321", "suporte@acme.com", "Rio de Janeiro", "RJ"),
                ClienteUiModel(3, "Metalúrgica Alfa", "(31) 97777-5678", "comercial@alfa.com", "Belo Horizonte", "MG")
            )
        )
    }
    var idCounter by remember { mutableIntStateOf(4) }

    // Busca
    var query by remember { mutableStateOf("") }
    val listaFiltrada = remember(clientes, query) {
        if (query.isBlank()) clientes
        else clientes.filter { it.nome.contains(query, ignoreCase = true) }
    }

    // Diálogos
    var showAddEdit by remember { mutableStateOf(false) }
    var editingCliente by remember { mutableStateOf<ClienteUiModel?>(null) }

    var showDelete by remember { mutableStateOf(false) }
    var deletingCliente by remember { mutableStateOf<ClienteUiModel?>(null) }

    var showView by remember { mutableStateOf(false) }
    var viewingCliente by remember { mutableStateOf<ClienteUiModel?>(null) }

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
            // Header
            Text(
                text = "Clientes",
                style = MaterialTheme.typography.headlineMedium,
                color = Brand
            )
            Spacer(Modifier.height(12.dp))

            // Busca + Adicionar
            ClientesSectionCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Buscar por nome") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.LightGray,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )

                    Button(
                        onClick = {
                            editingCliente = ClienteUiModel(
                                id = idCounter,
                                nome = "",
                                telefone = "",
                                email = "",
                                cidade = "",
                                estado = ""
                            )
                            showAddEdit = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brand,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Text("Adicionar Cliente")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Lista
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(listaFiltrada, key = { it.id }) { cli ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 12.dp)
                                ) {
                                    Text(cli.nome, style = MaterialTheme.typography.titleMedium, color = Brand)
                                    Spacer(Modifier.height(2.dp))
                                    Text("${cli.cidade} • ${cli.estado}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(cli.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(cli.telefone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row {
                                    IconButton(onClick = {
                                        viewingCliente = cli
                                        showView = true
                                    }) {
                                        // Reaproveitando o ícone de editar como "ver" seria confuso; então vamos abrir o dialog ao clicar no card também:
                                        Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Brand)
                                    }
                                    IconButton(onClick = {
                                        deletingCliente = cli
                                        showDelete = true
                                    }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }

                            // Toque no card para visualizar
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    viewingCliente = cli
                                    showView = true
                                },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = Brand
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                            ) {
                                Text("Visualizar")
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Adicionar / Editar
    if (showAddEdit && editingCliente != null) {
        AddEditClienteDialog(
            initial = editingCliente!!,
            onDismiss = { showAddEdit = false; editingCliente = null },
            onConfirm = { updated ->
                clientes = if (clientes.any { it.id == updated.id }) {
                    clientes.map { if (it.id == updated.id) updated else it }
                } else {
                    clientes + updated.also { idCounter += 1 }
                }
                showAddEdit = false
                editingCliente = null
            }
        )
    }

    // Dialog: Deletar
    if (showDelete && deletingCliente != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false; deletingCliente = null },
            title = { Text("Excluir cliente") },
            text = { Text("Tem certeza que deseja excluir \"${deletingCliente!!.nome}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        clientes = clientes.filterNot { it.id == deletingCliente!!.id }
                        showDelete = false
                        deletingCliente = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp)
                ) { Text("Excluir") }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDelete = false; deletingCliente = null },
                    shape = RoundedCornerShape(6.dp)
                ) { Text("Cancelar", color = Brand) }
            }
        )
    }

    // Dialog: Visualizar
    if (showView && viewingCliente != null) {
        ViewClienteDialog(
            cliente = viewingCliente!!,
            onDismiss = { showView = false; viewingCliente = null },
            onEdit = {
                editingCliente = viewingCliente
                showView = false
                showAddEdit = true
            },
            onDelete = {
                deletingCliente = viewingCliente
                showView = false
                showDelete = true
            }
        )
    }
}

@Composable
private fun ClientesSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), content = content)
    }
}

@Composable
private fun AddEditClienteDialog(
    initial: ClienteUiModel,
    onDismiss: () -> Unit,
    onConfirm: (ClienteUiModel) -> Unit
) {
    var nome by remember { mutableStateOf(initial.nome) }
    var telefone by remember { mutableStateOf(initial.telefone) }
    var email by remember { mutableStateOf(initial.email) }
    var cidade by remember { mutableStateOf(initial.cidade) }
    var estado by remember { mutableStateOf(initial.estado) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.nome.isBlank()) "Novo Cliente" else "Editar Cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nome, onValueChange = { nome = it }, label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = telefone, onValueChange = { telefone = it }, label = { Text("Telefone") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = email, onValueChange = { email = it }, label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = cidade, onValueChange = { cidade = it }, label = { Text("Cidade") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                OutlinedTextField(
                    value = estado, onValueChange = { estado = it }, label = { Text("Estado") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        initial.copy(
                            nome = nome.trim(),
                            telefone = telefone.trim(),
                            email = email.trim(),
                            cidade = cidade.trim(),
                            estado = estado.trim()
                        )
                    )
                },
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
private fun ViewClienteDialog(
    cliente: ClienteUiModel,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cliente") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nome: ${cliente.nome}")
                Text("Telefone: ${cliente.telefone}")
                Text("Email: ${cliente.email}")
                Text("Cidade: ${cliente.cidade}")
                Text("Estado: ${cliente.estado}")
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
                ) {
                    Text("Excluir")
                }
            }
        }
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ClientesScreenPreview() {
    AprimortechTheme {
        ClientesScreen(navController = rememberNavController())
    }
}
