package com.example.aprimortech

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.aprimortech.data.repository.ServicoRepository
import com.example.aprimortech.model.Servico
import com.example.aprimortech.ui.viewmodel.ServicoViewModel
import com.example.aprimortech.ui.viewmodel.ServicoViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicosScreen(navController: NavController) {
    // Criação direta dos componentes necessários
    val firestore = FirebaseFirestore.getInstance()
    val servicoRepository = ServicoRepository(firestore)
    val viewModelFactory = ServicoViewModelFactory(servicoRepository)
    val viewModel: ServicoViewModel = viewModel(factory = viewModelFactory)

    val servicos by viewModel.servicos.collectAsState()
    val top5Servicos by viewModel.top5Servicos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var novoServicoNome by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Serviços") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar serviço")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Top 5 Serviços Mais Usados
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Top 5 Serviços Mais Usados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (top5Servicos.isEmpty()) {
                        Text(
                            text = "Nenhum serviço cadastrado ainda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        top5Servicos.forEachIndexed { index, servico ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${index + 1}. ${servico.nome}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${servico.vezesUsado}x",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Lista Completa de Serviços
            Text(
                text = "Todos os Serviços",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn {
                    items(servicos) { servico ->
                        ServicoItem(servico = servico)
                    }
                }
            }

            // Mostrar erro se houver
            errorMessage?.let { error ->
                LaunchedEffect(error) {
                    // Aqui você pode mostrar um Snackbar ou Toast
                    viewModel.limparErro()
                }
            }
        }
    }

    // Dialog para adicionar novo serviço
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                novoServicoNome = ""
            },
            title = { Text("Adicionar Novo Serviço") },
            text = {
                OutlinedTextField(
                    value = novoServicoNome,
                    onValueChange = { novoServicoNome = it },
                    label = { Text("Nome do serviço") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (novoServicoNome.isNotBlank()) {
                            viewModel.salvarServico(novoServicoNome.trim())
                            showAddDialog = false
                            novoServicoNome = ""
                        }
                    }
                ) {
                    Text("Adicionar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        novoServicoNome = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ServicoItem(servico: Servico) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = servico.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Último uso: ${servico.ultimoUso}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${servico.vezesUsado}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
