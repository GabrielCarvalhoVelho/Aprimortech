package com.example.aprimortech

import android.Manifest
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.aprimortech.ui.viewmodel.MaquinaViewModel
import com.example.aprimortech.ui.viewmodel.MaquinaViewModelFactory
import com.example.aprimortech.ui.viewmodel.RelatorioSharedViewModel
import com.example.aprimortech.data.local.entity.MaquinaEntity
import com.example.aprimortech.model.Tinta
import com.example.aprimortech.model.Solvente
import com.example.aprimortech.model.Cliente
import androidx.compose.material3.MenuAnchorType
import android.widget.Toast
import android.app.DatePickerDialog
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import android.graphics.BitmapFactory
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.foundation.horizontalScroll
import com.example.aprimortech.data.repository.StorageUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.net.URL
import com.google.firebase.storage.FirebaseStorage

private val Brand = Color(0xFF1A4A5C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelatorioEquipamentoScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    clienteId: String = "",
    // Tornar o ViewModel opcional para evitar usar APIs composable em parâmetros padrão
    maquinaViewModelParam: MaquinaViewModel? = null,
    sharedViewModel: RelatorioSharedViewModel
) {
    // Instanciar o ViewModel aqui (dentro do contexto @Composable)
    val maquinaVM: MaquinaViewModel = maquinaViewModelParam ?: viewModel(
        factory = MaquinaViewModelFactory(
            buscarMaquinasUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarMaquinasUseCase,
            salvarMaquinaUseCase = (LocalContext.current.applicationContext as AprimortechApplication).salvarMaquinaUseCase,
            excluirMaquinaUseCase = (LocalContext.current.applicationContext as AprimortechApplication).excluirMaquinaUseCase,
            sincronizarMaquinasUseCase = (LocalContext.current.applicationContext as AprimortechApplication).sincronizarMaquinasUseCase,
            buscarClientesUseCase = (LocalContext.current.applicationContext as AprimortechApplication).buscarClientesUseCase
        )
    )

    val context = LocalContext.current
    val app = context.applicationContext as AprimortechApplication
    val scope = rememberCoroutineScope()

    // Repositórios
    val tintaRepository = remember { app.tintaRepository }
    val solventeRepository = remember { app.solventeRepository }

    // Estados do formulário
    var maquinaSelecionada by remember { mutableStateOf<MaquinaEntity?>(null) }
    var codigoTintaSelecionado by remember { mutableStateOf("") }
    var codigoSolventeSelecionado by remember { mutableStateOf("") }
    var dataProximaPreventiva by remember { mutableStateOf("") }
    var horasProximaPreventiva by remember { mutableStateOf("") }
    // Fotos do equipamento (armazenadas como Base64 para enviar/armazenar no sharedViewModel)
    var equipamentoFotos by remember { mutableStateOf<List<String>>(emptyList()) }

    // Launchers para câmera e galeria
    val cameraUriState = remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraUriState.value?.let { uri ->
                val base64 = uriToBase64(context, uri)
                base64?.let { newImage ->
                    // Tentativa assíncrona de upload para Storage; se falhar, manter Base64 localmente
                    scope.launch {
                        val folder = "relatorios/drafts" // manter padrão simples; o RelatorioRepository fará o re-upload definitivo ao salvar
                        val uploadedUrl = withContext(Dispatchers.IO) { StorageUploader.uploadBase64Image(newImage, folder) }
                        if (uploadedUrl != null) {
                            if (equipamentoFotos.size < 4) equipamentoFotos = equipamentoFotos + uploadedUrl
                            else Toast.makeText(context, "Máximo de 4 fotos", Toast.LENGTH_SHORT).show()
                        } else {
                            // upload falhou, manter base64 local
                            if (equipamentoFotos.size < 4) equipamentoFotos = equipamentoFotos + newImage
                            else Toast.makeText(context, "Máximo de 4 fotos", Toast.LENGTH_SHORT).show()
                            Toast.makeText(context, "Foto adicionada localmente (sem upload). Será enviada ao salvar.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    // Launcher para solicitar permissão de câmera (runtime)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            // Se a permissão foi concedida, lançar a câmera usando a uri previamente criada
            cameraUriState.value?.let { uri ->
                takePictureLauncher.launch(uri)
            }
        } else {
            Toast.makeText(context, "Permissão de câmera necessária para tirar fotos", Toast.LENGTH_SHORT).show()
        }
    }

    val pickImagesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val ctx = context
            // Converter e tentar upload em série (limitado ao número disponível)
            scope.launch {
                val converted = uris.mapNotNull { uri -> uriToBase64(ctx, uri) }
                var available = 4 - equipamentoFotos.size
                for (base64 in converted) {
                    if (available <= 0) break
                    val uploadedUrl = withContext(Dispatchers.IO) { StorageUploader.uploadBase64Image(base64, "relatorios/drafts") }
                    if (uploadedUrl != null) {
                        equipamentoFotos = equipamentoFotos + uploadedUrl
                    } else {
                        equipamentoFotos = equipamentoFotos + base64
                        Toast.makeText(ctx, "Algumas imagens foram adicionadas localmente e serão enviadas ao salvar.", Toast.LENGTH_LONG).show()
                    }
                    available--
                }
                if (converted.size > (4 - available)) {
                    Toast.makeText(ctx, "Apenas ${4 - equipamentoFotos.size} fotos adicionadas (limite 4)", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Estados auxiliares ausentes anteriormente
    var pendingMaquinaId by remember { mutableStateOf<String?>(null) }
    var showNovoMaquinaDialog by remember { mutableStateOf(false) }

    // Listas de tintas e solventes disponíveis
    var tintasDisponiveis by remember { mutableStateOf<List<Tinta>>(emptyList()) }
    var solventesDisponiveis by remember { mutableStateOf<List<Solvente>>(emptyList()) }

    // Estados do ViewModel (usando a instância local `maquinaVM`)
    val maquinas by maquinaVM.maquinas.collectAsState()
    val clientes by maquinaVM.clientes.collectAsState()
    val fabricantesDisponiveis by maquinaVM.fabricantesDisponiveis.collectAsState()
    val modelosDisponiveis by maquinaVM.modelosDisponiveis.collectAsState()

    val operacaoEmAndamento by maquinaVM.operacaoEmAndamento.collectAsState()
    val mensagemOperacao by maquinaVM.mensagemOperacao.collectAsState()

    // Carregar dados do ViewModel ao entrar (para clientes, máquinas e autocompletes)
    LaunchedEffect(Unit) { maquinaVM.carregarTodosDados() }

    // Carregar tintas e solventes ao iniciar
    LaunchedEffect(Unit) {
        scope.launch {
            tintasDisponiveis = tintaRepository.buscarTodas()
            solventesDisponiveis = solventeRepository.buscarTodos()
        }
    }

    // Filtrar máquinas do cliente atual
    val maquinasDoCliente = remember(maquinas, clienteId) {
        maquinas.filter { it.clienteId == clienteId }
    }

    // Quando um id de máquina foi salvo/pendente, aguardar até que ViewModel retorne essa máquina e selecioná-la
    LaunchedEffect(pendingMaquinaId, maquinas) {
        val pid = pendingMaquinaId
        if (!pid.isNullOrBlank()) {
            val encontrada = maquinas.find { it.id == pid }
            if (encontrada != null) {
                maquinaSelecionada = encontrada
                pendingMaquinaId = null
            }
        }
    }

    // Feedback toast
    LaunchedEffect(mensagemOperacao) {
        mensagemOperacao?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            maquinaVM.limparMensagem()
        }
    }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Brand)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Dados do Equipamento", style = MaterialTheme.typography.headlineMedium, color = Brand)
            Spacer(Modifier.height(12.dp))

            if (operacaoEmAndamento) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Brand)
                }
                Spacer(Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // SEÇÃO MÁQUINA
                SectionCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Máquina", style = MaterialTheme.typography.titleMedium, color = Brand)
                        IconButton(onClick = { showNovoMaquinaDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Nova Máquina", tint = Brand)
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    if (maquinasDoCliente.isEmpty()) {
                        Text("Nenhuma máquina cadastrada para este cliente",
                             style = MaterialTheme.typography.bodyMedium,
                             color = Color.Gray)
                    } else {
                        var maquinaExpanded by remember { mutableStateOf(false) }

                        ExposedDropdownMenuBox(
                            expanded = maquinaExpanded,
                            onExpandedChange = { maquinaExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = maquinaSelecionada?.identificacao ?: "",
                                onValueChange = { },
                                label = { Text("Selecionar Máquina *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                colors = textFieldColors(),
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = maquinaExpanded) }
                            )

                            ExposedDropdownMenu(
                                expanded = maquinaExpanded,
                                onDismissRequest = { maquinaExpanded = false }
                            ) {
                                maquinasDoCliente.forEach { maquina ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(maquina.identificacao, style = MaterialTheme.typography.bodyMedium)
                                                Text("${maquina.fabricante} - ${maquina.modelo}",
                                                     style = MaterialTheme.typography.bodySmall,
                                                     color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            maquinaSelecionada = maquina
                                            // ✅ Removido: campos dataProximaPreventiva e horasProximaPreventiva não existem mais na máquina
                                            // Os valores serão preenchidos manualmente pelo usuário
                                            maquinaExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (maquinaSelecionada != null) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("✓ Máquina Selecionada", style = MaterialTheme.typography.labelMedium, color = Color(0xFF2E7D32))
                                Text(maquinaSelecionada!!.identificacao, style = MaterialTheme.typography.bodyMedium)
                                Text("${maquinaSelecionada!!.fabricante} - ${maquinaSelecionada!!.modelo} (N/S: ${maquinaSelecionada!!.numeroSerie})",
                                     style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }

                // SEÇÃO CÓDIGO DA TINTA com Autocomplete
                SectionCard {
                    Text("Código da Tinta", style = MaterialTheme.typography.titleMedium, color = Brand)
                    Spacer(Modifier.height(8.dp))

                    AutocompleteTextField(
                        value = codigoTintaSelecionado,
                        onValueChange = { codigoTintaSelecionado = it.uppercase() },
                        suggestions = tintasDisponiveis.map { it.codigo },
                        label = "Código da Tinta *",
                        placeholder = "Digite ou selecione",
                        onAddNew = {
                            // Salvar novo código E garantir que o campo receba o valor
                            scope.launch {
                                val novo = codigoTintaSelecionado.trim().uppercase()
                                if (novo.isNotBlank()) {
                                    // Comparação case-insensitive para evitar duplicatas
                                    val existe = tintasDisponiveis.any { it.codigo.equals(novo, ignoreCase = true) }
                                    if (!existe) {
                                        tintaRepository.salvarTinta(novo)
                                        tintasDisponiveis = tintaRepository.buscarTodas()
                                        Toast.makeText(context, "Código de tinta salvo!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Código de tinta já existe", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }

                // SEÇÃO CÓDIGO DO SOLVENTE com Autocomplete
                SectionCard {
                    Text("Código do Solvente", style = MaterialTheme.typography.titleMedium, color = Brand)
                    Spacer(Modifier.height(8.dp))

                    AutocompleteTextField(
                        value = codigoSolventeSelecionado,
                        onValueChange = { codigoSolventeSelecionado = it.uppercase() },
                        suggestions = solventesDisponiveis.map { it.codigo },
                        label = "Código do Solvente *",
                        placeholder = "Digite ou selecione",
                        onAddNew = {
                            scope.launch {
                                val novo = codigoSolventeSelecionado.trim().uppercase()
                                if (novo.isNotBlank()) {
                                    val existe = solventesDisponiveis.any { it.codigo.equals(novo, ignoreCase = true) }
                                    if (!existe) {
                                        solventeRepository.salvarSolvente(novo)
                                        solventesDisponiveis = solventeRepository.buscarTodos()
                                        Toast.makeText(context, "Código de solvente salvo!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Código de solvente já existe", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    )
                }

                // SEÇÃO PRÓXIMA MANUTENÇÃO PREVENTIVA
                SectionCard {
                    Text("Próxima Manutenção Preventiva", style = MaterialTheme.typography.titleMedium, color = Brand)
                    Spacer(Modifier.height(8.dp))

                    // Campo de data
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val calendar = Calendar.getInstance()
                                if (dataProximaPreventiva.isNotBlank()) {
                                    try {
                                        val parts = dataProximaPreventiva.split("/")
                                        if (parts.size == 3) {
                                            calendar.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
                                        }
                                    } catch (_: Exception) { }
                                }

                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        dataProximaPreventiva = String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            day, month + 1, year
                                        )
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    ) {
                        OutlinedTextField(
                            value = dataProximaPreventiva,
                            onValueChange = { },
                            label = { Text("Data") },
                            placeholder = { Text("DD/MM/AAAA") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors(),
                            trailingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = "Selecionar Data", tint = Brand)
                            },
                            readOnly = true,
                            enabled = false
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = horasProximaPreventiva,
                        onValueChange = { horasProximaPreventiva = it.filter { char -> char.isDigit() } },
                        label = { Text("Horas *") },
                        placeholder = { Text("Ex: 500") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors(),
                        trailingIcon = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        val atual = horasProximaPreventiva.toIntOrNull() ?: 0
                                        horasProximaPreventiva = (atual + 100).toString()
                                    },
                                    modifier = Modifier.size(32.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Brand.copy(alpha = 0.1f),
                                        contentColor = Brand
                                    )
                                ) {
                                    Text("+100", fontSize = 9.sp)
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        val atual = horasProximaPreventiva.toIntOrNull() ?: 0
                                        horasProximaPreventiva = (atual + 500).toString()
                                    },
                                    modifier = Modifier.size(32.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = Brand.copy(alpha = 0.15f),
                                        contentColor = Brand
                                    )
                                ) {
                                    Text("+500", fontSize = 9.sp)
                                }
                            }
                        }
                    )
                }

                // SEÇÃO FOTOS DO EQUIPAMENTO
                SectionCard {
                    Text("Fotos do Equipamento", style = MaterialTheme.typography.titleMedium, color = Brand)
                    Spacer(Modifier.height(8.dp))

                    // Miniaturas em uma única linha, lado a lado (rolável horizontalmente se necessário)
                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Mostrar até 4 slots (preencher com Spacer quando vazio)
                            for (i in 0 until 4) {
                                if (i < equipamentoFotos.size) {
                                    val item = equipamentoFotos[i]
                                    Box(modifier = Modifier.size(72.dp)) {
                                        // Bitmap state por imagem (suporta URLs e base64)
                                        var bitmapState by remember(item) { mutableStateOf<android.graphics.Bitmap?>(null) }

                                        LaunchedEffect(item) {
                                            try {
                                                // http/https URL -> buscar bytes via rede
                                                if (item.startsWith("http://") || item.startsWith("https://")) {
                                                    val bytes = withContext(Dispatchers.IO) {
                                                        URL(item).openStream().use { it.readBytes() }
                                                    }
                                                    bitmapState = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                    return@LaunchedEffect
                                                }

                                                // gs:// reference -> usar Firebase Storage
                                                if (item.startsWith("gs://")) {
                                                    try {
                                                        val storage = FirebaseStorage.getInstance("gs://aprimortech-30cad.firebasestorage.app")
                                                        val ref = storage.getReferenceFromUrl(item)
                                                        val maxBytes: Long = 10L * 1024L * 1024L
                                                        val bytes = ref.getBytes(maxBytes).await()
                                                        bitmapState = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                        return@LaunchedEffect
                                                    } catch (_: Exception) {
                                                        // fallback: tentar resolver downloadUrl -> buscar via HTTP
                                                        try {
                                                            val storage = FirebaseStorage.getInstance("gs://aprimortech-30cad.firebasestorage.app")
                                                            val ref = storage.getReferenceFromUrl(item)
                                                            val downloadUrl = ref.downloadUrl.await().toString()
                                                            val bytes = withContext(Dispatchers.IO) { URL(downloadUrl).openStream().use { it.readBytes() } }
                                                            bitmapState = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                            return@LaunchedEffect
                                                        } catch (_: Exception) {
                                                            bitmapState = null
                                                        }
                                                    }
                                                }

                                                // Caso padrão: tratar como Base64 (captura exceção se inválido)
                                                try {
                                                    val bytes = Base64.decode(item, Base64.DEFAULT)
                                                    bitmapState = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                } catch (_: Exception) {
                                                    // se for inválido, deixar null (mostra placeholder)
                                                    bitmapState = null
                                                }
                                            } catch (_: Exception) {
                                                android.util.Log.d("RelatorioEquipamento", "Falha ao carregar bitmap item")
                                                bitmapState = null
                                            }
                                        }

                                        if (bitmapState != null) {
                                            Image(
                                                bitmap = bitmapState!!.asImageBitmap(),
                                                contentDescription = "Foto ${i + 1}",
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            // Placeholder simples quando não há bitmap
                                            Box(
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFECECEC))
                                            )
                                        }

                                        Surface(
                                            shape = CircleShape,
                                            color = Color.White,
                                            tonalElevation = 2.dp,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .align(Alignment.TopEnd)
                                                .clickable {
                                                    equipamentoFotos = equipamentoFotos.toMutableList().also { list ->
                                                        if (i in list.indices) list.removeAt(i)
                                                    }
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(Icons.Default.Close, contentDescription = "Remover foto", tint = Color(0xFFB00020), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(72.dp))
                                }
                            }
                        }

                        // Botões centralizados abaixo das miniaturas (evita sobreposição)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            FilledTonalButton(onClick = {
                                val ctx = context
                                val photoFile = File(ctx.cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(ctx, ctx.packageName + ".fileprovider", photoFile)
                                cameraUriState.value = uri

                                val cameraPermission = Manifest.permission.CAMERA
                                if (ContextCompat.checkSelfPermission(ctx, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
                                    takePictureLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(cameraPermission)
                                }
                            }, colors = ButtonDefaults.filledTonalButtonColors(containerColor = Brand.copy(alpha = 0.08f)), modifier = Modifier.padding(end = 8.dp)) {
                                Icon(Icons.Default.Add, contentDescription = "Tirar foto", tint = Brand)
                                Spacer(Modifier.width(6.dp))
                                Text("Câmera", color = Brand)
                            }

                            FilledTonalButton(onClick = { pickImagesLauncher.launch("image/*") }, colors = ButtonDefaults.filledTonalButtonColors(containerColor = Brand.copy(alpha = 0.08f))) {
                                Text("Galeria", color = Brand)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("Até 4 fotos. Você pode tirar novas fotos ou selecionar da galeria.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                // BOTÃO CONTINUAR
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Executar salvamento das collections e salvar no SharedViewModel
                        scope.launch {
                            val novoTinta = codigoTintaSelecionado.trim().uppercase()
                            if (novoTinta.isNotBlank()) {
                                val existeTinta = tintasDisponiveis.any { it.codigo.equals(novoTinta, ignoreCase = true) }
                                if (!existeTinta) {
                                    tintaRepository.salvarTinta(novoTinta)
                                    tintasDisponiveis = tintaRepository.buscarTodas()
                                }
                            }
                            val novoSolvente = codigoSolventeSelecionado.trim().uppercase()
                            if (novoSolvente.isNotBlank()) {
                                val existeSolvente = solventesDisponiveis.any { it.codigo.equals(novoSolvente, ignoreCase = true) }
                                if (!existeSolvente) {
                                    solventeRepository.salvarSolvente(novoSolvente)
                                    solventesDisponiveis = solventeRepository.buscarTodos()
                                }
                            }

                            // Agora salvar os dados no SharedViewModel
                            maquinaSelecionada?.let { maquina ->
                                android.util.Log.d("RelatorioEquipamentoScreen", "[DEBUG] Salvando dados do equipamento (dentro do coroutine)")
                                android.util.Log.d("RelatorioEquipamentoScreen", "[DEBUG] codigoTintaSelecionado: $codigoTintaSelecionado")
                                android.util.Log.d("RelatorioEquipamentoScreen", "[DEBUG] codigoSolventeSelecionado: $codigoSolventeSelecionado")
                                android.util.Log.d("RelatorioEquipamentoScreen", "[DEBUG] dataProximaPreventiva: $dataProximaPreventiva")
                                android.util.Log.d("RelatorioEquipamentoScreen", "[DEBUG] horasProximaPreventiva: $horasProximaPreventiva")
                                sharedViewModel.setEquipamentoData(
                                    fabricante = maquina.fabricante,
                                    numeroSerie = maquina.numeroSerie,
                                    codigoConfiguracao = maquina.codigoConfiguracao,
                                    modelo = maquina.modelo,
                                    identificacao = maquina.identificacao,
                                    anoFabricacao = maquina.anoFabricacao,
                                    codigoTinta = codigoTintaSelecionado,
                                    codigoSolvente = codigoSolventeSelecionado,
                                    dataProximaPreventiva = dataProximaPreventiva,
                                    horaProximaPreventiva = horasProximaPreventiva,
                                    equipamentoFotos = equipamentoFotos
                                )
                            }

                            // Navegar após salvar
                            navController.navigate("relatorioEtapa3?clienteId=$clienteId")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = maquinaSelecionada != null &&
                              codigoTintaSelecionado.isNotBlank() &&
                              codigoSolventeSelecionado.isNotBlank() &&
                              horasProximaPreventiva.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand, contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continuar", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // Dialog para nova máquina (usa implementação idêntica à de MaquinasScreen)
    if (showNovoMaquinaDialog) {
        AddEditMaquinaDialog(
            initial = MaquinaEntity(
                id = UUID.randomUUID().toString(),
                clienteId = clienteId,
                fabricante = "",
                numeroSerie = "",
                modelo = "",
                identificacao = "",
                anoFabricacao = "",
                codigoConfiguracao = ""
            ),
            clientes = clientes,
            fabricantesDisponiveis = fabricantesDisponiveis,
            modelosDisponiveis = modelosDisponiveis,
            onDismiss = { showNovoMaquinaDialog = false },
            onConfirm = { nova ->
                // Marcar pending id e solicitar salvamento via ViewModel
                pendingMaquinaId = nova.id
                maquinaVM.salvarMaquina(nova)
                showNovoMaquinaDialog = false
            }
        )
    }
}

/**
 * Campo de texto com autocomplete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    label: String,
    placeholder: String,
    onAddNew: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredSuggestions = remember(value, suggestions) {
        if (value.isBlank()) suggestions
        else suggestions.filter { it.contains(value, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded && (filteredSuggestions.isNotEmpty() || value.isNotBlank()),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
            colors = textFieldColors(),
            trailingIcon = {
                if (filteredSuggestions.isNotEmpty()) {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            }
        )

        // Mostrar o menu sempre que estiver expandido e houver sugestão OU valor não vazio
        if (expanded && (filteredSuggestions.isNotEmpty() || value.isNotBlank())) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredSuggestions.take(10).forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            onValueChange(suggestion)
                            expanded = false
                        }
                    )
                }

                // Se o valor atual não está presente nas sugestões e não é vazio,
                // mostrar opção para adicionar novo código
                val alreadyExists = filteredSuggestions.any { it.equals(value, ignoreCase = true) }
                if (value.isNotBlank() && !alreadyExists) {
                    DropdownMenuItem(
                        text = { Text("Adicionar '" + value + "'") },
                        leadingIcon = { Icon(Icons.Filled.Add, contentDescription = "Adicionar") },
                        onClick = {
                            // Chama callback do chamador para salvar/usar o novo valor
                            onAddNew()
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    focusedBorderColor = Brand,
    unfocusedBorderColor = Color.LightGray
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditMaquinaDialog(
    initial: MaquinaEntity,
    clientes: List<Cliente>,
    fabricantesDisponiveis: List<String>,
    modelosDisponiveis: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (MaquinaEntity) -> Unit
) {
    var clienteId by remember { mutableStateOf(initial.clienteId) }
    var fabricante by remember { mutableStateOf(initial.fabricante) }
    var numeroSerie by remember { mutableStateOf(initial.numeroSerie) }
    var modelo by remember { mutableStateOf(initial.modelo) }
    var identificacao by remember { mutableStateOf(initial.identificacao) }
    var anoFabricacao by remember { mutableStateOf(initial.anoFabricacao) }
    var codigoConfiguracao by remember { mutableStateOf(initial.codigoConfiguracao) }

    val salvarHabilitado = clienteId.isNotBlank() && fabricante.isNotBlank() &&
            numeroSerie.isNotBlank() && modelo.isNotBlank() &&
            anoFabricacao.isNotBlank() && identificacao.isNotBlank() &&
            codigoConfiguracao.isNotBlank()
    // ⚠️ REMOVIDOS das validações: codigoTinta e codigoSolvente

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.fabricante.isBlank()) "Nova Máquina" else "Editar Máquina") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dropdown de Cliente
                var expanded by remember { mutableStateOf(false) }
                val clienteSelecionado = clientes.find { it.id == clienteId }?.nome ?: ""

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = clienteSelecionado,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Cliente *") },
                        placeholder = { Text("Selecione um cliente") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        clientes.forEach { cliente ->
                            DropdownMenuItem(
                                text = { Text(cliente.nome) },
                                onClick = {
                                    clienteId = cliente.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Campo Fabricante com dropdown autocomplete
                var fabricanteExpanded by remember { mutableStateOf(false) }
                val fabricantesFiltrados = remember(fabricante, fabricantesDisponiveis) {
                    if (fabricante.isBlank()) fabricantesDisponiveis.take(5)
                    else fabricantesDisponiveis.filter { it.contains(fabricante, ignoreCase = true) }.take(5)
                }

                ExposedDropdownMenuBox(
                    expanded = fabricanteExpanded && fabricantesFiltrados.isNotEmpty(),
                    onExpandedChange = { fabricanteExpanded = it }
                ) {
                    OutlinedTextField(
                        value = fabricante,
                        onValueChange = {
                            fabricante = it.uppercase()
                            fabricanteExpanded = it.isNotEmpty() && fabricantesFiltrados.isNotEmpty()
                        },
                        label = { Text("Fabricante *") },
                        placeholder = { Text("Ex: Hitachi, Videojet, Domino") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable),
                        colors = textFieldColors()
                    )
                    if (fabricantesFiltrados.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = fabricanteExpanded,
                            onDismissRequest = { fabricanteExpanded = false }
                        ) {
                            fabricantesFiltrados.forEach { sugestao ->
                                DropdownMenuItem(
                                    text = { Text(sugestao) },
                                    onClick = {
                                        fabricante = sugestao
                                        fabricanteExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = numeroSerie,
                    onValueChange = { numeroSerie = it.uppercase() },
                    label = { Text("Número de Série *") },
                    placeholder = { Text("Ex: SN001234") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                // Campo Modelo com dropdown autocomplete
                var modeloExpanded by remember { mutableStateOf(false) }
                val modelosFiltrados = remember(modelo, modelosDisponiveis) {
                    if (modelo.isBlank()) modelosDisponiveis.take(5)
                    else modelosDisponiveis.filter { it.contains(modelo, ignoreCase = true) }.take(5)
                }

                ExposedDropdownMenuBox(
                    expanded = modeloExpanded && modelosFiltrados.isNotEmpty(),
                    onExpandedChange = { modeloExpanded = it }
                ) {
                    OutlinedTextField(
                        value = modelo,
                        onValueChange = {
                            modelo = it.uppercase()
                            modeloExpanded = it.isNotEmpty() && modelosFiltrados.isNotEmpty()
                        },
                        label = { Text("Modelo *") },
                        placeholder = { Text("Ex: UX-D160W, 1550") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable),
                        colors = textFieldColors()
                    )
                    if (modelosFiltrados.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = modeloExpanded,
                            onDismissRequest = { modeloExpanded = false }
                        ) {
                            modelosFiltrados.forEach { sugestao ->
                                DropdownMenuItem(
                                    text = { Text(sugestao) },
                                    onClick = {
                                        modelo = sugestao.uppercase()
                                        modeloExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = identificacao,
                    onValueChange = { identificacao = it.uppercase() },
                    label = { Text("Identificação *") },
                    placeholder = { Text("Ex: Máquina Principal") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = anoFabricacao,
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }.take(4)
                        anoFabricacao = filtered
                    },
                    label = { Text("Ano de Fabricação *") },
                    placeholder = { Text("Ex: 2020") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                OutlinedTextField(
                    value = codigoConfiguracao,
                    onValueChange = { codigoConfiguracao = it.uppercase() },
                    label = { Text("Código de Configuração *") },
                    placeholder = { Text("Ex: CFG001, CONFIG-A") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors()
                )

                Text(
                    "* Campos obrigatórios",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        initial.copy(
                            clienteId = clienteId.trim(),
                            fabricante = fabricante.trim(),
                            numeroSerie = numeroSerie.trim(),
                            modelo = modelo.trim(),
                            anoFabricacao = anoFabricacao.trim(),
                            identificacao = identificacao.trim(),
                            codigoConfiguracao = codigoConfiguracao.trim()
                        )
                    )
                },
                enabled = salvarHabilitado,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brand,
                    contentColor = Color.White,
                    disabledContainerColor = Brand.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(6.dp)
            ) { Text("Salvar") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(6.dp)) {
                Text("Cancelar", color = Brand)
            }
        }
    )
}

// Helper: converte Uri para Base64 (JPEG comprimido)
private fun uriToBase64(context: android.content.Context, uri: Uri): String? {
    return try {
        val input: InputStream? = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(input)
        input?.close()
        val baos = ByteArrayOutputStream()
        // Comprimir em JPEG com qualidade 80
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, baos)
        val bytes = baos.toByteArray()
        Base64.encodeToString(bytes, Base64.DEFAULT)
    } catch (e: Exception) {
        android.util.Log.e("RelatorioEquipamentoScreen", "Erro convertendo uri para base64", e)
        null
    }
}
