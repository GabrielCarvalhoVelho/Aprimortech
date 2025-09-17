package com.example.aprimortech

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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

private val Brand = Color(0xFF1A4A5C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdicionarClienteScreen(navController: NavController, modifier: Modifier = Modifier) {
    var nome by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }

    val salvarHabilitado = nome.isNotBlank()

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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Brand
                        )
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
                text = "Adicionar Cliente",
                color = Brand,
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )

            FieldCard {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            FieldCard {
                OutlinedTextField(
                    value = telefone,
                    onValueChange = { telefone = it },
                    label = { Text("Telefone") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            FieldCard {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            FieldCard {
                OutlinedTextField(
                    value = cidade,
                    onValueChange = { cidade = it },
                    label = { Text("Cidade") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            FieldCard {
                OutlinedTextField(
                    value = estado,
                    onValueChange = { estado = it },
                    label = { Text("Estado") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Brand
                    ),
                    border = androidx.compose.foundation.BorderStroke(0.dp, Color.Transparent),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text("Cancelar")
                }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = {
                        val novo = ClienteUiModel(
                            id = (System.currentTimeMillis() % Int.MAX_VALUE).toInt(),
                            nome = nome.trim(),
                            telefone = telefone.trim(),
                            email = email.trim(),
                            cidade = cidade.trim(),
                            estado = estado.trim()
                        )
                        // Devolve o resultado para a tela anterior (ClientesScreen)
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("novoCliente", novo)
                        navController.popBackStack()
                    },
                    enabled = salvarHabilitado,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brand,
                        contentColor = Color.White,
                        disabledContainerColor = Brand.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.8f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text("Salvar")
                }
            }
        }
    }
}

@Composable
private fun FieldCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(modifier = Modifier.padding(8.dp)) { content() }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun AdicionarClienteScreenPreview() {
    AprimortechTheme {
        AdicionarClienteScreen(navController = rememberNavController())
    }
}
