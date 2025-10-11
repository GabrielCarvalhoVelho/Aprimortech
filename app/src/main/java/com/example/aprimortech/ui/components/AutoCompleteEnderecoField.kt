package com.example.aprimortech.ui.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import kotlinx.coroutines.delay

data class EnderecoCompleto(
    val endereco: String,
    val cidade: String,
    val estado: String,
    val latitude: Double?,
    val longitude: Double?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteEnderecoField(
    endereco: String,
    onEnderecoChange: (EnderecoCompleto) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Digite o endereço..."
) {
    val context = LocalContext.current
    var searchText by remember { mutableStateOf("") }
    var numeroEndereco by remember { mutableStateOf("") }
    var complemento by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Dados do endereço selecionado (mantidos após seleção)
    var enderecoSelecionado by remember { mutableStateOf<EnderecoCompleto?>(null) }
    var ruaSelecionada by remember { mutableStateOf("") }

    // Inicializar Places API
    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, context.getString(com.example.aprimortech.R.string.google_maps_key))
        }
        Places.createClient(context)
    }

    // Token para a sessão de autocompletar (renovado a cada sessão)
    var token by remember { mutableStateOf(AutocompleteSessionToken.newInstance()) }

    // Inicializar com o endereço passado (para edição)
    LaunchedEffect(endereco) {
        if (endereco.isNotBlank() && ruaSelecionada.isEmpty()) {
            // Tentar extrair número do endereço existente
            val partes = endereco.split(",").map { it.trim() }
            if (partes.size >= 2) {
                searchText = partes[0]
                numeroEndereco = partes.getOrNull(1) ?: ""
                complemento = partes.drop(2).joinToString(", ")
            } else {
                searchText = endereco
            }
        }
    }

    // Função para buscar sugestões com debounce
    LaunchedEffect(searchText) {
        if (searchText.length < 3 || ruaSelecionada.isNotEmpty()) {
            suggestions = emptyList()
            showSuggestions = false
            return@LaunchedEffect
        }

        isLoading = true
        delay(500) // Debounce de 500ms

        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(searchText)
            .setCountries("BR")
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                suggestions = response.autocompletePredictions
                showSuggestions = suggestions.isNotEmpty()
                isLoading = false
                Log.d("AutoCompleteEndereco", "Encontradas ${suggestions.size} sugestões")
            }
            .addOnFailureListener { exception ->
                Log.e("AutoCompleteEndereco", "Erro ao buscar sugestões", exception)
                suggestions = emptyList()
                showSuggestions = false
                isLoading = false
            }
    }

    // Função para atualizar endereço completo mantendo cidade/estado
    fun atualizarEnderecoCompleto() {
        enderecoSelecionado?.let { selecionado ->
            val partesEndereco = mutableListOf(ruaSelecionada)

            if (numeroEndereco.isNotBlank()) {
                partesEndereco.add(numeroEndereco)
            }

            if (complemento.isNotBlank()) {
                partesEndereco.add(complemento)
            }

            val enderecoFinal = partesEndereco.joinToString(", ")

            // IMPORTANTE: Manter cidade, estado e coordenadas do endereço selecionado
            onEnderecoChange(
                EnderecoCompleto(
                    endereco = enderecoFinal,
                    cidade = selecionado.cidade,
                    estado = selecionado.estado,
                    latitude = selecionado.latitude,
                    longitude = selecionado.longitude
                )
            )
        }
    }

    // Função para obter detalhes do local selecionado
    fun obterDetalhesLocal(placeId: String, descricao: String) {
        isLoading = true
        showSuggestions = false

        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS_COMPONENTS
        )

        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                val place = response.place
                val latLng = place.latLng

                var cidade = ""
                var estado = ""
                var rua = ""

                place.addressComponents?.asList()?.forEach { component ->
                    when {
                        component.types.contains("route") -> {
                            rua = component.name
                        }
                        component.types.contains("administrative_area_level_2") ||
                        component.types.contains("locality") -> {
                            cidade = component.name
                        }
                        component.types.contains("administrative_area_level_1") -> {
                            estado = component.shortName ?: component.name
                        }
                    }
                }

                // Priorizar a rua extraída, caso contrário usar o nome do lugar
                ruaSelecionada = if (rua.isNotEmpty()) {
                    rua
                } else {
                    place.name ?: descricao.split(",").firstOrNull()?.trim() ?: descricao
                }

                searchText = ruaSelecionada

                // Criar objeto com os dados completos
                enderecoSelecionado = EnderecoCompleto(
                    endereco = ruaSelecionada,
                    cidade = cidade,
                    estado = estado,
                    latitude = latLng?.latitude,
                    longitude = latLng?.longitude
                )

                // Notificar mudança
                atualizarEnderecoCompleto()

                // Renovar token para próxima sessão
                token = AutocompleteSessionToken.newInstance()
                isLoading = false

                Log.d("AutoCompleteEndereco", "Endereço selecionado: $ruaSelecionada, $cidade - $estado")
            }
            .addOnFailureListener { exception ->
                Log.e("AutoCompleteEndereco", "Erro ao obter detalhes", exception)
                isLoading = false
            }
    }

    // Função para limpar seleção
    fun limparSelecao() {
        ruaSelecionada = ""
        numeroEndereco = ""
        complemento = ""
        searchText = ""
        enderecoSelecionado = null
        suggestions = emptyList()
        showSuggestions = false
    }

    Column(modifier = modifier) {
        // Campo de busca de rua/avenida
        OutlinedTextField(
            value = searchText,
            onValueChange = { newValue ->
                if (ruaSelecionada.isEmpty()) {
                    searchText = newValue
                }
            },
            label = { Text("Rua/Avenida *") },
            leadingIcon = {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Localização",
                    tint = Color(0xFF1A4A5C)
                )
            },
            trailingIcon = {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF1A4A5C)
                        )
                    }
                    ruaSelecionada.isNotEmpty() -> {
                        IconButton(onClick = { limparSelecao() }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Limpar",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            enabled = ruaSelecionada.isEmpty(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF5F5F5),
                focusedBorderColor = Color(0xFF1A4A5C),
                unfocusedBorderColor = Color.LightGray,
                disabledBorderColor = Color.LightGray
            ),
            singleLine = true
        )

        // Lista de sugestões
        if (showSuggestions && suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(suggestions.take(8)) { suggestion ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    obterDetalhesLocal(
                                        suggestion.placeId,
                                        suggestion.getFullText(null).toString()
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = suggestion.getPrimaryText(null).toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1A4A5C)
                            )
                            Text(
                                text = suggestion.getSecondaryText(null).toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        if (suggestion != suggestions.last()) {
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = Color.LightGray.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // Campos de número e complemento (aparecem após selecionar a rua)
        if (ruaSelecionada.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Campo de número
                OutlinedTextField(
                    value = numeroEndereco,
                    onValueChange = {
                        numeroEndereco = it
                        atualizarEnderecoCompleto()
                    },
                    label = { Text("Número") },
                    placeholder = { Text("123") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF1A4A5C),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )

                // Campo de complemento
                OutlinedTextField(
                    value = complemento,
                    onValueChange = {
                        complemento = it
                        atualizarEnderecoCompleto()
                    },
                    label = { Text("Complemento") },
                    placeholder = { Text("Apto 45") },
                    modifier = Modifier.weight(1.5f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF1A4A5C),
                        unfocusedBorderColor = Color.LightGray
                    ),
                    singleLine = true
                )
            }

            // Informação visual do endereço selecionado
            enderecoSelecionado?.let { selecionado ->
                if (selecionado.cidade.isNotBlank() && selecionado.estado.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A4A5C).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF1A4A5C),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${selecionado.cidade} - ${selecionado.estado}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1A4A5C),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
