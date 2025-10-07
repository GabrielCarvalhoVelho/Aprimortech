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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioEquipamentoScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    clienteId: String = ""
) {
    var fabricante by remember { mutableStateOf("") }
    var codigoConfiguracao by remember { mutableStateOf("") }
    var numeroSerie by remember { mutableStateOf("") }
    var modelo by remember { mutableStateOf("") }
    var codigoTinta by remember { mutableStateOf("") }
    var anoFabricacao by remember { mutableStateOf("") }
    var identificacao by remember { mutableStateOf("") }
    var codigoSolvente by remember { mutableStateOf("") }

    // Data próxima manutenção = hoje + 12 meses
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val proximaManutencao = remember {
        LocalDate.now().plusMonths(12).format(formatter)
    }

    // Banco fictício de fabricantes e dados
    val equipamentos = mapOf(
        "Epson" to mapOf(
            "codigoConfiguracao" to listOf("CFG-EP-100", "CFG-EP-200", "CFG-EP-300"),
            "numeroSerie" to listOf("EP12345", "EP67890", "EP54321"),
            "modelo" to listOf("Epson TX100", "Epson L3150", "Epson XP300"),
            "codigoTinta" to listOf("T664", "T673", "003"),
            "anoFabricacao" to listOf("2022", "2023", "2024"),
            "identificacao" to listOf("EQP-EP-01", "EQP-EP-02", "EQP-EP-03"),
            "codigoSolvente" to listOf("SOL-EP-10", "SOL-EP-20", "SOL-EP-30")
        ),
        "HP" to mapOf(
            "codigoConfiguracao" to listOf("CFG-HP-110", "CFG-HP-220", "CFG-HP-330"),
            "numeroSerie" to listOf("HP11223", "HP44556", "HP77889"),
            "modelo" to listOf("HP DeskJet 2136", "HP LaserJet 1020", "HP InkTank 319"),
            "codigoTinta" to listOf("664", "662", "678"),
            "anoFabricacao" to listOf("2021", "2022", "2023"),
            "identificacao" to listOf("EQP-HP-01", "EQP-HP-02", "EQP-HP-03"),
            "codigoSolvente" to listOf("SOL-HP-10", "SOL-HP-20", "SOL-HP-30")
        ),
        "Canon" to mapOf(
            "codigoConfiguracao" to listOf("CFG-CN-210", "CFG-CN-320", "CFG-CN-430"),
            "numeroSerie" to listOf("CN99887", "CN77665", "CN55443"),
            "modelo" to listOf("Canon G3110", "Canon iX6810", "Canon LBP6030"),
            "codigoTinta" to listOf("GI-490", "PG-245", "CL-246"),
            "anoFabricacao" to listOf("2020", "2021", "2022"),
            "identificacao" to listOf("EQP-CN-01", "EQP-CN-02", "EQP-CN-03"),
            "codigoSolvente" to listOf("SOL-CN-10", "SOL-CN-20", "SOL-CN-30")
        )
    )

    var expandedFabricante by remember { mutableStateOf(false) }
    var expandedConfig by remember { mutableStateOf(false) }
    var expandedSerie by remember { mutableStateOf(false) }
    var expandedModelo by remember { mutableStateOf(false) }
    var expandedTinta by remember { mutableStateOf(false) }
    var expandedAno by remember { mutableStateOf(false) }
    var expandedIdent by remember { mutableStateOf(false) }
    var expandedSolvente by remember { mutableStateOf(false) }

    val fabricantes = equipamentos.keys.toList()

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
                text = "Dados do Equipamento",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1A4A5C)
            )

            DropdownInputCard(
                label = "Fabricante",
                value = fabricante,
                options = fabricantes,
                expanded = expandedFabricante,
                onExpandedChange = { expandedFabricante = it },
                onSelect = { fabricante = it; expandedFabricante = false }
            )

            if (fabricante.isNotBlank()) {
                val dados = equipamentos[fabricante]!!

                DropdownInputCard("Código de Configuração", codigoConfiguracao, dados["codigoConfiguracao"]!!, expandedConfig, { expandedConfig = it }) {
                    codigoConfiguracao = it; expandedConfig = false
                }

                DropdownInputCard("Número de Série", numeroSerie, dados["numeroSerie"]!!, expandedSerie, { expandedSerie = it }) {
                    numeroSerie = it; expandedSerie = false
                }

                DropdownInputCard("Modelo", modelo, dados["modelo"]!!, expandedModelo, { expandedModelo = it }) {
                    modelo = it; expandedModelo = false
                }

                DropdownInputCard("Código da Tinta", codigoTinta, dados["codigoTinta"]!!, expandedTinta, { expandedTinta = it }) {
                    codigoTinta = it; expandedTinta = false
                }

                DropdownInputCard("Ano de Fabricação", anoFabricacao, dados["anoFabricacao"]!!, expandedAno, { expandedAno = it }) {
                    anoFabricacao = it; expandedAno = false
                }

                DropdownInputCard("Identificação", identificacao, dados["identificacao"]!!, expandedIdent, { expandedIdent = it }) {
                    identificacao = it; expandedIdent = false
                }

                DropdownInputCard("Código do Solvente", codigoSolvente, dados["codigoSolvente"]!!, expandedSolvente, { expandedSolvente = it }) {
                    codigoSolvente = it; expandedSolvente = false
                }

                // Campo Data Próxima Manutenção preenchido automaticamente
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    OutlinedTextField(
                        value = proximaManutencao,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Data Próxima Manutenção Preventiva") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

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
                    onClick = { navController.navigate("relatorioEtapa3?clienteId=$clienteId") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownInputCard(
    label: String,
    value: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .padding(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = { onSelect(option) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun RelatorioEquipamentoPreview() {
    AprimortechTheme {
        RelatorioEquipamentoScreen(navController = rememberNavController())
    }
}
