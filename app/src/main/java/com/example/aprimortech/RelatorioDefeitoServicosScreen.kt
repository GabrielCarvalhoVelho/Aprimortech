package com.example.aprimortech

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioDefeitoServicosScreen(navController: NavController, modifier: Modifier = Modifier) {
    // listas base
    val defeitosComuns = listOf("Falha Elétrica", "Vazamento de Óleo", "Ruído Anormal", "Aquecimento Excessivo")
    val servicosComuns = listOf("Troca de Peça", "Lubrificação", "Ajuste de Configuração", "Limpeza Geral")

    // estado de selecionados
    val defeitosSelecionados = remember { mutableStateListOf<String>() }
    val servicosSelecionados = remember { mutableStateListOf<String>() }

    // extras adicionados pelo usuário
    val defeitosExtras = remember { mutableStateListOf<String>() }
    val servicosExtras = remember { mutableStateListOf<String>() }

    var novoDefeito by remember { mutableStateOf("") }
    var novoServico by remember { mutableStateOf("") }
    var observacoes by remember { mutableStateOf("") }

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
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        (defeitosComuns + defeitosExtras).forEach { defeito ->
                            FilterChip(
                                selected = defeitosSelecionados.contains(defeito),
                                onClick = {
                                    if (defeitosSelecionados.contains(defeito)) {
                                        defeitosSelecionados.remove(defeito)
                                    } else {
                                        defeitosSelecionados.add(defeito)
                                    }
                                },
                                label = { Text(defeito) }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = novoDefeito,
                        onValueChange = { novoDefeito = it },
                        label = { Text("Adicionar novo defeito") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (novoDefeito.isNotBlank()) {
                                defeitosExtras.add(novoDefeito)
                                defeitosSelecionados.add(novoDefeito)
                                novoDefeito = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A4A5C),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Adicionar")
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
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        (servicosComuns + servicosExtras).forEach { servico ->
                            FilterChip(
                                selected = servicosSelecionados.contains(servico),
                                onClick = {
                                    if (servicosSelecionados.contains(servico)) {
                                        servicosSelecionados.remove(servico)
                                    } else {
                                        servicosSelecionados.add(servico)
                                    }
                                },
                                label = { Text(servico) }
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = novoServico,
                        onValueChange = { novoServico = it },
                        label = { Text("Adicionar novo serviço") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (novoServico.isNotBlank()) {
                                servicosExtras.add(novoServico)
                                servicosSelecionados.add(novoServico)
                                novoServico = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A4A5C),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Adicionar")
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
                    onValueChange = { observacoes = it },
                    placeholder = { Text("Digite observações adicionais...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
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
                    onClick = { navController.navigate("relatorioEtapa4") },
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

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatorioDefeitoServicosPreview() {
    AprimortechTheme {
        RelatorioDefeitoServicosScreen(navController = rememberNavController())
    }
}
