package com.example.aprimortech.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    var searchText by remember { mutableStateOf(endereco) }
    var numeroEndereco by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var enderecoBase by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Inicializar Places API
    val placesClient = remember {
        if (!Places.isInitialized()) {
            Places.initialize(context, context.getString(com.example.aprimortech.R.string.google_maps_key))
        }
        Places.createClient(context)
    }

    // Token para a sessão de autocompletar
    val token = remember { AutocompleteSessionToken.newInstance() }

    // Função para buscar sugestões
    fun buscarSugestoes(query: String) {
        if (query.length < 3) {
            suggestions = emptyList()
            showSuggestions = false
            return
        }

        isLoading = true

        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(query)
            .setTypeFilter(TypeFilter.ADDRESS)
            .setCountries("BR") // Apenas Brasil
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                suggestions = response.autocompletePredictions
                showSuggestions = suggestions.isNotEmpty()
                isLoading = false
                Log.d("PlacesAPI", "Encontradas ${suggestions.size} sugestões para: $query")
            }
            .addOnFailureListener { exception ->
                Log.e("PlacesAPI", "Erro ao buscar sugestões", exception)
                suggestions = emptyList()
                showSuggestions = false
                isLoading = false
            }
    }

    // Função para obter detalhes do local selecionado
    fun obterDetalhesLocal(placeId: String, descricao: String) {
        isLoading = true

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

                // Extrair cidade e estado dos componentes do endereço
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

                // Usar a rua extraída ou o endereço completo se não conseguir extrair
                enderecoBase = if (rua.isNotEmpty()) rua else (place.address ?: descricao)
                searchText = enderecoBase

                // Criar endereço final com número se fornecido
                val enderecoFinal = if (numeroEndereco.isNotBlank()) {
                    "$enderecoBase, $numeroEndereco"
                } else {
                    enderecoBase
                }

                val enderecoCompleto = EnderecoCompleto(
                    endereco = enderecoFinal,
                    cidade = cidade,
                    estado = estado,
                    latitude = latLng?.latitude,
                    longitude = latLng?.longitude
                )

                Log.d("PlacesAPI", "Endereço completo: $enderecoCompleto")
                onEnderecoChange(enderecoCompleto)
                showSuggestions = false
                isLoading = false
            }
            .addOnFailureListener { exception ->
                Log.e("PlacesAPI", "Erro ao obter detalhes do local", exception)
                isLoading = false
            }
    }

    // Função para atualizar endereço quando número mudar
    fun atualizarEnderecoComNumero() {
        if (enderecoBase.isNotEmpty()) {
            val enderecoFinal = if (numeroEndereco.isNotBlank()) {
                "$enderecoBase, $numeroEndereco"
            } else {
                enderecoBase
            }

            // Manter os dados da cidade e estado originais quando atualizar o número
            val enderecoAtual = EnderecoCompleto(
                endereco = enderecoFinal,
                cidade = "", // Não sobrescrever cidade
                estado = "", // Não sobrescrever estado
                latitude = null, // Não sobrescrever coordenadas
                longitude = null
            )

            onEnderecoChange(enderecoAtual)
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { newValue ->
                searchText = newValue
                // Debounce para evitar muitas consultas
                scope.launch {
                    delay(300)
                    if (searchText == newValue) {
                        buscarSugestoes(newValue)
                    }
                }
            },
            label = { Text("Endereço") },
            leadingIcon = {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Localização",
                    tint = Color(0xFF1A4A5C)
                )
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFF1A4A5C)
                    )
                }
            },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray
            )
        )

        // Campo para número do endereço
        if (enderecoBase.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = numeroEndereco,
                onValueChange = {
                    numeroEndereco = it
                    atualizarEnderecoComNumero()
                },
                label = { Text("Número (opcional)") },
                placeholder = { Text("Ex: 123, 45-A, etc.") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray
                )
            )
        }

        // Lista de sugestões
        if (showSuggestions && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column {
                    suggestions.take(5).forEach { suggestion ->
                        TextButton(
                            onClick = {
                                obterDetalhesLocal(suggestion.placeId, suggestion.getFullText(null).toString())
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = suggestion.getPrimaryText(null).toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1A4A5C)
                                )
                                Text(
                                    text = suggestion.getSecondaryText(null).toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                        if (suggestion != suggestions.last()) {
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}
