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
import com.example.aprimortech.data.repository.DefeitoRepository
import com.example.aprimortech.model.Defeito
import com.example.aprimortech.ui.viewmodel.DefeitoViewModel
import com.example.aprimortech.ui.viewmodel.DefeitoViewModelFactory
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefeitosScreen(navController: NavController) {
    // Criação direta dos componentes necessários
    val firestore = FirebaseFirestore.getInstance()
    val defeitoRepository = DefeitoRepository(firestore)
    val viewModelFactory = DefeitoViewModelFactory(defeitoRepository)
    val viewModel: DefeitoViewModel = viewModel(factory = viewModelFactory)

    val defeitos by viewModel.defeitos.collectAsState()
    val top5Defeitos by viewModel.top5Defeitos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var novoDefeitoNome by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Defeitos") },
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
                Icon(Icons.Default.Add, contentDescription = "Adicionar defeito")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Top 5 Defeitos Mais Usados
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
                        text = "Top 5 Defeitos Mais Usados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (top5Defeitos.isEmpty()) {
                        Text(
                            text = "Nenhum defeito cadastrado ainda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        top5Defeitos.forEachIndexed { index, defeito ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${index + 1}. ${defeito.nome}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${defeito.vezesUsado}x",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Lista Completa de Defeitos
            Text(
                text = "Todos os Defeitos",
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
                    items(defeitos) { defeito ->
                        DefeitoItem(defeito = defeito)
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

    // Dialog para adicionar novo defeito
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                novoDefeitoNome = ""
            },
            title = { Text("Adicionar Novo Defeito") },
            text = {
                OutlinedTextField(
                    value = novoDefeitoNome,
                    onValueChange = { novoDefeitoNome = it },
                    label = { Text("Nome do defeito") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (novoDefeitoNome.isNotBlank()) {
                            viewModel.salvarDefeito(novoDefeitoNome.trim())
                            showAddDialog = false
                            novoDefeitoNome = ""
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
                        novoDefeitoNome = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DefeitoItem(defeito: Defeito) {
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
                    text = defeito.nome,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Último uso: ${defeito.ultimoUso}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "${defeito.vezesUsado}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
