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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme

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
fun RelatorioPecasScreen(navController: NavController, modifier: Modifier = Modifier) {
    var pecas by remember { mutableStateOf(mutableListOf<PecaUiModel>()) }
    var novaPeca by remember { mutableStateOf(PecaUiModel()) }
    var editIndex by remember { mutableStateOf<Int?>(null) }

    // Exemplo de códigos pré-cadastrados
    val codigosExemplo = listOf("PC-1001", "PC-2002", "PC-3003", "PC-4004")
    var expandedCodigos by remember { mutableStateOf(false) }
    val sugestoesCodigos = codigosExemplo.filter { it.contains(novaPeca.codigo, ignoreCase = true) }

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
            InputCardPeca {
                ExposedDropdownMenuBox(
                    expanded = expandedCodigos && sugestoesCodigos.isNotEmpty(),
                    onExpandedChange = { expandedCodigos = it }
                ) {
                    OutlinedTextField(
                        value = novaPeca.codigo,
                        onValueChange = {
                            novaPeca = novaPeca.copy(codigo = it)
                            expandedCodigos = true
                        },
                        label = { Text("Código") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedCodigos) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCodigos && sugestoesCodigos.isNotEmpty(),
                        onDismissRequest = { expandedCodigos = false }
                    ) {
                        sugestoesCodigos.forEach { sugestao ->
                            DropdownMenuItem(
                                text = { Text(sugestao) },
                                onClick = {
                                    novaPeca = novaPeca.copy(codigo = sugestao)
                                    expandedCodigos = false
                                }
                            )
                        }
                    }
                }
            }

            InputCardPeca {
                OutlinedTextField(
                    value = novaPeca.descricao,
                    onValueChange = { novaPeca = novaPeca.copy(descricao = it) },
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            InputCardPeca {
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
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            InputCardPeca {
                OutlinedTextField(
                    value = if (novaPeca.valorUnit == 0.0) "" else novaPeca.valorUnit.toString(),
                    onValueChange = { valor ->
                        novaPeca = novaPeca.copy(valorUnit = valor.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Valor Unitário (R$)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            Button(
                onClick = {
                    if (novaPeca.codigo.isNotBlank() && novaPeca.descricao.isNotBlank()) {
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
                        novaPeca = PecaUiModel()
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

            Divider(Modifier.padding(vertical = 12.dp))

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
                    onClick = { navController.navigate("relatorioEtapa5") },
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
