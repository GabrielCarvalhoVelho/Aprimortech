package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.aprimortech.model.Cliente
import com.example.aprimortech.model.ContatoCliente
import com.example.aprimortech.ui.viewmodel.ClienteViewModel
import com.example.aprimortech.ui.viewmodel.ClienteViewModelFactory
import com.example.aprimortech.ui.components.AutoCompleteEnderecoField
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.BorderStroke
import com.example.aprimortech.ui.theme.AprimortechTheme

private val Brand = Color(0xFF1A4A5C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: ClienteViewModel = viewModel(
        factory = ClienteViewModelFactory(
            buscarClientesUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarClientesUseCase,
            salvarClienteUseCase = (LocalContext.current.applicationContext as AprimortechApplication).salvarClienteUseCase,
            excluirClienteUseCase = (LocalContext.current.applicationContext as AprimortechApplication).excluirClienteUseCase,
            sincronizarClientesUseCase = (LocalContext.current.applicationContext as AprimortechApplication).sincronizarClientesUseCase
        )
    )
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val clientes by viewModel.clientes.collectAsState()
    val mensagemOperacao by viewModel.mensagemOperacao.collectAsState()

    val listaFiltrada = remember(clientes, query) {
        if (query.isBlank()) clientes else clientes.filter { it.nome.contains(query, ignoreCase = true) }
    }

    var showAddEdit by remember { mutableStateOf(false) }
    var editingCliente by remember { mutableStateOf<Cliente?>(null) }

    var showDelete by remember { mutableStateOf(false) }
    var deletingCliente by remember { mutableStateOf<Cliente?>(null) }

    var showView by remember { mutableStateOf(false) }
    var viewingCliente by remember { mutableStateOf<Cliente?>(null) }

    // Feedback de operações
    LaunchedEffect(mensagemOperacao) {
        mensagemOperacao?.let { mensagem ->
            android.widget.Toast.makeText(context, mensagem, android.widget.Toast.LENGTH_LONG).show()
            viewModel.limparMensagem()
        }
    }

    // Carregar clientes ao abrir a tela
    LaunchedEffect(Unit) {
        viewModel.carregarClientes()
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
            Text("Clientes", style = MaterialTheme.typography.headlineMedium, color = Brand)
            Spacer(Modifier.height(12.dp))

            ClientesSectionCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Buscar por nome") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors()
                    )

                    Button(
                        onClick = {
                            editingCliente = Cliente(
                                id = java.util.UUID.randomUUID().toString(),
                                nome = "",
                                cnpjCpf = "",
                                contatos = emptyList(),
                                endereco = "",
                                cidade = "",
                                estado = "",
                                telefone = "",
                                celular = "",
                                latitude = null,
                                longitude = null
                            )
                            showAddEdit = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White)
                    ) { Text("Adicionar Cliente") }
                }
            }

            Spacer(Modifier.height(16.dp))

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

                                    // Exibir contatos (múltiplos se houver)
                                    if (cli.contatos.isNotEmpty()) {
                                        Text("Contatos:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        cli.contatos.forEach { contato ->
                                            val setorCel = listOfNotNull(contato.setor, contato.celular).joinToString(" • ")
                                            Text(
                                                "• ${contato.nome}${if (setorCel.isNotBlank()) " ($setorCel)" else ""}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Exibir telefones apenas se não estiverem vazios
                                    val telefoneInfo = buildList {
                                        if (cli.telefone.isNotBlank()) add("Tel: ${cli.telefone}")
                                        if (cli.celular.isNotBlank()) add("Cel: ${cli.celular}")
                                    }.joinToString(" • ")

                                    if (telefoneInfo.isNotBlank()) {
                                        Text(telefoneInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Text("End.: ${cli.endereco}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row {
                                    IconButton(onClick = {
                                        editingCliente = cli
                                        showAddEdit = true
                                    }) {
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

                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    viewingCliente = cli
                                    showView = true
                                },
                                modifier = Modifier.height(40.dp),
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Brand),
                                border = BorderStroke(1.dp, Color.LightGray)
                            ) { Text("Visualizar") }
                        }
                    }
                }
            }
        }
    }

    if (showAddEdit && editingCliente != null) {
        AddEditClienteDialog(
            initial = editingCliente!!,
            onDismiss = { showAddEdit = false; editingCliente = null },
            onConfirm = { updated ->
                viewModel.salvarCliente(updated)
                showAddEdit = false
                editingCliente = null
            }
        )
    }

    if (showDelete && deletingCliente != null) {
        AlertDialog(
            onDismissRequest = { showDelete = false; deletingCliente = null },
            title = { Text("Excluir cliente") },
            text = { Text("Tem certeza que deseja excluir \"${deletingCliente!!.nome}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.excluirCliente(deletingCliente!!.id)
                        showDelete = false
                        deletingCliente = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                    shape = RoundedCornerShape(6.dp)
                ) { Text("Excluir") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDelete = false; deletingCliente = null }, shape = RoundedCornerShape(6.dp)) {
                    Text("Cancelar", color = Brand)
                }
            }
        )
    }

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
    ) { Column(modifier = Modifier.padding(12.dp), content = content) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditClienteDialog(
    initial: Cliente,
    onDismiss: () -> Unit,
    onConfirm: (Cliente) -> Unit
) {
    var nome by remember { mutableStateOf(initial.nome) }
    var telefone by remember { mutableStateOf(initial.telefone) }
    var celular by remember { mutableStateOf(initial.celular) }
    // agora contatos já é List<ContatoCliente>
    var contatos by remember { mutableStateOf(initial.contatos) }
    var endereco by remember { mutableStateOf(initial.endereco) }
    var cidade by remember { mutableStateOf(initial.cidade) }
    var estado by remember { mutableStateOf(initial.estado) }
    var latitude by remember { mutableStateOf(initial.latitude) }
    var longitude by remember { mutableStateOf(initial.longitude) }

    var enderecoPreenchido by remember { mutableStateOf(false) }
    val salvarHabilitado = nome.isNotBlank() && endereco.isNotBlank() && cidade.isNotBlank() && estado.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.nome.isBlank()) "Novo Cliente" else "Editar Cliente") },
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
                    label = { Text("Nome *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )
                AutoCompleteEnderecoField(
                    endereco = endereco,
                    onEnderecoChange = { enderecoCompleto ->
                        endereco = enderecoCompleto.endereco
                        cidade = enderecoCompleto.cidade
                        estado = enderecoCompleto.estado
                        latitude = enderecoCompleto.latitude
                        longitude = enderecoCompleto.longitude
                        enderecoPreenchido = enderecoCompleto.cidade.isNotBlank() && enderecoCompleto.estado.isNotBlank()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Digite o nome da rua ou avenida"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = cidade,
                        onValueChange = { cidade = it },
                        label = { Text("Cidade *") },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors(),
                        enabled = !enderecoPreenchido // Bloqueia se veio do Google Places
                    )
                    OutlinedTextField(
                        value = estado,
                        onValueChange = { estado = it },
                        label = { Text("Estado *") },
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors(),
                        enabled = !enderecoPreenchido // Bloqueia se veio do Google Places
                    )
                }
                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = { Text("Telefone do cliente (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = celular,
                    onValueChange = { celular = it },
                    label = { Text("Celular do cliente (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = textFieldColors()
                )
                // Nova seção de contatos
                ContatosSectionCompleta(
                    contatos = contatos,
                    onContatosChange = { contatos = it }
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
                    onConfirm(
                        initial.copy(
                            nome = nome.trim(),
                            telefone = telefone.trim(),
                            celular = celular.trim(),
                            contatos = contatos,
                            endereco = endereco.trim(),
                            cidade = cidade.trim(),
                            estado = estado.trim(),
                            latitude = latitude,
                            longitude = longitude
                        )
                    )
                },
                enabled = salvarHabilitado,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brand,
                    contentColor = Color.White,
                    disabledContainerColor = Brand.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                ),
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

// Nova seção de contatos completa
@Composable
private fun ContatosSectionCompleta(
    contatos: List<ContatoCliente>,
    onContatosChange: (List<ContatoCliente>) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var setor by remember { mutableStateOf("") }
    var celular by remember { mutableStateOf("") }
    val contatosMutable = remember(contatos) { contatos.toMutableList() }
    Column {
        Text("Contatos do cliente", style = MaterialTheme.typography.titleSmall, color = Brand)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome do contato *") },
                modifier = Modifier.weight(1f),
                colors = textFieldColors()
            )
            OutlinedTextField(
                value = setor,
                onValueChange = { setor = it },
                label = { Text("Setor (opcional)") },
                modifier = Modifier.weight(1f),
                colors = textFieldColors()
            )
            OutlinedTextField(
                value = celular,
                onValueChange = { celular = it },
                label = { Text("Celular (opcional)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = textFieldColors()
            )
            IconButton(
                onClick = {
                    if (nome.isNotBlank()) {
                        contatosMutable.add(ContatoCliente(nome.trim(), setor.ifBlank { null }, celular.ifBlank { null }))
                        onContatosChange(contatosMutable.toList())
                        nome = ""
                        setor = ""
                        celular = ""
                    }
                },
                enabled = nome.isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Contato", tint = if (nome.isNotBlank()) Brand else Color.Gray)
            }
        }
        if (contatosMutable.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Contatos adicionados:", style = MaterialTheme.typography.bodySmall, color = Brand, modifier = Modifier.padding(bottom = 4.dp))
                    contatosMutable.forEachIndexed { index, contato ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("• ${contato.nome}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2C3E50))
                                if (!contato.setor.isNullOrBlank()) Text("Setor: ${contato.setor}", style = MaterialTheme.typography.bodySmall)
                                if (!contato.celular.isNullOrBlank()) Text("Celular: ${contato.celular}", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(
                                onClick = {
                                    contatosMutable.removeAt(index)
                                    onContatosChange(contatosMutable.toList())
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Remover Contato", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor = Color.LightGray,
    unfocusedBorderColor = Color.LightGray
)

@Composable
private fun ViewClienteDialog(
    cliente: Cliente,
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
                if (cliente.contatos.isNotEmpty()) {
                    Text("Contatos:")
                    cliente.contatos.forEach { contato ->
                        val detalhes = listOfNotNull(contato.setor, contato.celular).joinToString(" • ")
                        Text("  • ${contato.nome}${if (detalhes.isNotBlank()) " ($detalhes)" else ""}")
                    }
                }
                if (cliente.telefone.isNotBlank()) Text("Telefone: ${cliente.telefone}")
                if (cliente.celular.isNotBlank()) Text("Celular: ${cliente.celular}")
                Text("Endereço: ${cliente.endereco}")
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
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(6.dp)) { Text("Fechar", color = Brand) }
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
fun ClientesScreenPreview() {
    AprimortechTheme {
        ClientesScreen(navController = rememberNavController())
    }
}
