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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
        composable("relatorios") {
            RelatoriosScreen(navController = navController)
        }
        composable("novoRelatorio") {
            // Agora não precisa mais de parâmetro extra
            NovoRelatorioScreen(navController = navController)
        }
        composable(
            "dadosEquipamento/{clienteId}/{contatoId}/{setorId}",
            arguments = listOf(
                navArgument("clienteId") { type = NavType.StringType },
                navArgument("contatoId") { type = NavType.StringType },
                navArgument("setorId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""
            val contatoId = backStackEntry.arguments?.getString("contatoId") ?: ""
            val setorId = backStackEntry.arguments?.getString("setorId") ?: ""
            DadosEquipamentoScreen(
                navController = navController,
                clienteId = clienteId,
                contatoId = contatoId,
                setorId = setorId
            )
        }
        composable(
            "relatorioEtapa2?clienteId={clienteId}",
            arguments = listOf(
                navArgument("clienteId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""
            RelatorioEquipamentoScreen(
                navController = navController,
                clienteId = clienteId
            )
        }
        composable(
            "relatorioEtapa3?clienteId={clienteId}",
            arguments = listOf(
                navArgument("clienteId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""
            RelatorioDefeitoServicosScreen(
                navController = navController,
                clienteId = clienteId
            )
        }
        composable(
            "relatorioEtapa4?defeitos={defeitos}&servicos={servicos}&observacoes={observacoes}&clienteId={clienteId}",
            arguments = listOf(
                navArgument("defeitos") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("servicos") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("observacoes") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("clienteId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val defeitos = backStackEntry.arguments?.getString("defeitos") ?: ""
            val servicos = backStackEntry.arguments?.getString("servicos") ?: ""
            val observacoes = backStackEntry.arguments?.getString("observacoes") ?: ""
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""

            RelatorioPecasScreen(
                navController = navController,
                defeitos = defeitos,
                servicos = servicos,
                observacoes = observacoes,
                clienteId = clienteId
            )
        }
        composable(
            "relatorioEtapa5?defeitos={defeitos}&servicos={servicos}&observacoes={observacoes}&pecas={pecas}&clienteId={clienteId}",
            arguments = listOf(
                navArgument("defeitos") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("servicos") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("observacoes") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("pecas") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("clienteId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val defeitos = backStackEntry.arguments?.getString("defeitos") ?: ""
            val servicos = backStackEntry.arguments?.getString("servicos") ?: ""
            val observacoes = backStackEntry.arguments?.getString("observacoes") ?: ""
            val pecas = backStackEntry.arguments?.getString("pecas") ?: ""
            val clienteId = backStackEntry.arguments?.getString("clienteId") ?: ""

            RelatorioHorasDeslocamentoScreen(
                navController = navController,
                defeitos = defeitos,
                servicos = servicos,
                observacoes = observacoes,
                pecas = pecas,
                clienteId = clienteId
            )
        }
        composable("relatorioEtapa6") {
            RelatorioAssinaturaScreen(navController = navController)
        }
        composable("relatorioFinalizado") {
            RelatorioFinalizadoScreen(navController = navController)
        }
        // 🚀 NOVAS ROTAS
        composable("clientes") {
            ClientesScreen(navController = navController)
        }
        composable("maquinas") {
            MaquinasScreen(navController = navController)
        }
        composable("pecas") {
            PecasScreen(navController = navController)
        }
        composable("defeitos") {
            DefeitosScreen(navController = navController)
        }
        composable("servicos") {
            ServicosScreen(navController = navController)
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
            painter = painterResource(id = R.drawable.logo_aprimortech),
            contentDescription = "Logo Aprimortech",
            modifier = Modifier
                .size(height = 160.dp, width = 360.dp)
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
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Ícone de Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Ícone de Senha") },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
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
            onClick = { /* TODO: recuperar senha */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Esqueceu a senha?")
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Por favor, preencha email e senha.", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                auth?.signInWithEmailAndPassword(email.trim(), password.trim())
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show()
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        } else {
                            val exception = task.exception
                            val errorMessage = exception?.message ?: "Erro desconhecido. Tente novamente."
                            Toast.makeText(context, "Falha no login: $errorMessage", Toast.LENGTH_LONG).show()
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1A4A5C),
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
