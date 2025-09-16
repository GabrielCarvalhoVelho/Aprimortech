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
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
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
            // Header
            Text(
                text = "Peças Utilizadas",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )

            // Formulário para adicionar peça
            InputCardPeca {
                OutlinedTextField(
                    value = novaPeca.codigo,
                    onValueChange = { novaPeca = novaPeca.copy(codigo = it) },
                    label = { Text("Código") },
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
                        pecas = (pecas + novaPeca).toMutableList()
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
                Text("Adicionar Peça")
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

            // Lista de peças adicionadas
            if (pecas.isNotEmpty()) {
                Text("Peças Adicionadas:", style = MaterialTheme.typography.titleMedium)
                pecas.forEach { peca ->
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
                    onClick = {
                        navController.navigate("relatorioEtapa5")
                    },
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
