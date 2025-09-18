package com.example.aprimortech

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aprimortech.ui.theme.AprimortechTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AprimortechTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = navController)
        }
        composable("dashboard") {
            DashboardScreen(navController = navController)
        }
        composable("novoRelatorio") {
            NovoRelatorioScreen(navController = navController)
        }
        composable("relatorioEtapa2") {
            RelatorioEquipamentoScreen(navController = navController)
        }
        composable("relatorioEtapa3") {
            RelatorioDefeitoServicosScreen(navController = navController)
        }
        composable("relatorioEtapa4") {
            RelatorioPecasScreen(navController = navController)
        }
        composable("relatorioEtapa5") {
            RelatorioHorasDeslocamentoScreen(navController = navController)
        }
        composable("relatorioEtapa6") {
            RelatorioAssinaturaScreen(navController = navController)
        }
        composable("relatorioFinalizado") {
            RelatorioFinalizadoScreen(
                navController = navController,
                relatorio = RelatorioUiModel(
                    cliente = "Cliente Exemplo",
                    data = "13/09/2025",
                    endereco = "Rua Teste, 123",
                    tecnico = "Alan Silva",
                    descricao = "Relatório de teste finalizado."
                )
            )
        }
        // 🚀 NOVA ROTA CLIENTES
        composable("clientes") {
            ClientesScreen(navController = navController)
        }
        // 🚀 NOVA ROTA MÁQUINAS
        composable("maquinas") {
            MaquinasScreen(navController = navController)
        }
        // 🚀 NOVA ROTA PEÇAS
        composable("pecas") {
            PecasScreen(navController = navController)
        }

    }
}

@Composable
fun LoginScreen(navController: NavController, modifier: Modifier = Modifier) {

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current

    val auth = if (!isInPreview) FirebaseAuth.getInstance() else null

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_aprimortech), // nome do seu arquivo em drawable
            contentDescription = "Logo Aprimortech",
            modifier = Modifier
                .size(height = 160.dp, width = 360.dp) // ajuste conforme ficar melhor
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Excelência em codificação industrial",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "Ícone de Email")
            },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "Ícone de Senha")
            },
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else
                    Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, contentDescription = "Mostrar/Esconder Senha")
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        TextButton(
            onClick = { /* Lógica futura aqui */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Esqueceu a senha?")
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(
                        context,
                        "Por favor, preencha email e senha.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }

                auth?.signInWithEmailAndPassword(email.trim(), password.trim())
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                context,
                                "Login realizado com sucesso!",
                                Toast.LENGTH_SHORT
                            ).show()
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            val exception = task.exception
                            val errorMessage =
                                exception?.message ?: "Erro desconhecido. Tente novamente."
                            Toast.makeText(
                                context,
                                "Falha no login: $errorMessage",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp), // mesma borda dos inputs
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A4A5C), // cor principal Aprimortech
                contentColor = Color.White
            )
        ) {
            Text("ENTRAR")
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun LoginScreenPreview() {
    AprimortechTheme {
        LoginScreen(navController = rememberNavController())
    }
}
