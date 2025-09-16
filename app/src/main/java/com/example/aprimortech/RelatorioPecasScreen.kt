package com.example.aprimortech

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun RelatorioPecasScreen(navController: NavController, modifier: Modifier = Modifier) {
    var pecas by remember { mutableStateOf(mutableListOf<PecaUiModel>()) }
    var novaPeca by remember { mutableStateOf(PecaUiModel()) }

    val totalGeral = pecas.sumOf { it.valorTotal }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Peças Utilizadas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // Formulário para adicionar peça
        OutlinedTextField(
            value = novaPeca.codigo,
            onValueChange = { novaPeca = novaPeca.copy(codigo = it) },
            label = { Text("Código") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = novaPeca.descricao,
            onValueChange = { novaPeca = novaPeca.copy(descricao = it) },
            label = { Text("Descrição") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = if (novaPeca.quantidade == 0) "" else novaPeca.quantidade.toString(),
            onValueChange = { qtd ->
                novaPeca = novaPeca.copy(quantidade = qtd.toIntOrNull() ?: 0)
            },
            label = { Text("Quantidade") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = if (novaPeca.valorUnit == 0.0) "" else novaPeca.valorUnit.toString(),
            onValueChange = { valor ->
                novaPeca = novaPeca.copy(valorUnit = valor.toDoubleOrNull() ?: 0.0)
            },
            label = { Text("Valor Unitário (R$)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Button(
            onClick = {
                if (novaPeca.codigo.isNotBlank() && novaPeca.descricao.isNotBlank()) {
                    pecas = (pecas + novaPeca).toMutableList()
                    novaPeca = PecaUiModel()
                }
            },
            modifier = Modifier.align(Alignment.End)
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
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(Modifier.height(24.dp))

        // Botões de navegação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = { navController.popBackStack() }) {
                Text("Anterior")
            }
            Button(onClick = {
                // TODO: salvar lista de peças e ir para a próxima etapa
                // navController.navigate("relatorioEtapa5")
            }) {
                Text("Próximo")
            }
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
