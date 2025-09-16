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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioEquipamentoScreen(navController: NavController, modifier: Modifier = Modifier) {
    var fabricante by remember { mutableStateOf("") }
    var codigoConfiguracao by remember { mutableStateOf("") }
    var numeroSerie by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var codigoTinta by remember { mutableStateOf("") }
    var anoFabricacao by remember { mutableStateOf("") }
    var identificacao by remember { mutableStateOf("") }
    var codigoSolvente by remember { mutableStateOf("") }
    var dataProximaManutencao by remember { mutableStateOf("") }

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
                .background(Color(0xFFF5F5F5)) // fundo cinza claro
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Dados do Equipamento",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )

            // Campos em cards brancos com sombra
            EquipamentoInputField("Fabricante", fabricante) { fabricante = it }
            EquipamentoInputField("Código de Configuração", codigoConfiguracao) { codigoConfiguracao = it }
            EquipamentoInputField("Número de Série", numeroSerie) { numeroSerie = it }
            EquipamentoInputField("Modelo", modelo) { modelo = it }
            EquipamentoInputField("Código da Tinta", codigoTinta) { codigoTinta = it }
            EquipamentoInputField("Ano de Fabricação", anoFabricacao, isNumber = true) { anoFabricacao = it }
            EquipamentoInputField("Identificação", identificacao) { identificacao = it }
            EquipamentoInputField("Código do Solvente", codigoSolvente) { codigoSolvente = it }
            EquipamentoInputField("Data Próxima Manutenção Preventiva", dataProximaManutencao) { dataProximaManutencao = it }

            Spacer(Modifier.height(16.dp))

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
                        // salvar dados e ir para próxima etapa
                        navController.navigate("relatorioEtapa3")
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
private fun EquipamentoInputField(
    label: String,
    value: String,
    isNumber: Boolean = false,
    onValueChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            keyboardOptions = if (isNumber) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatorioEquipamentoPreview() {
    AprimortechTheme {
        RelatorioEquipamentoScreen(navController = rememberNavController())
    }
}
