package com.example.aprimortech.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val filteredSuggestions = remember(value, suggestions) {
        if (value.isBlank()) {
            suggestions.take(5) // Mostrar apenas 5 sugestões quando vazio
        } else {
            suggestions.filter { it.contains(value, ignoreCase = true) }.take(5)
        }
    }

    Column(modifier = modifier) {
        // Se não há sugestões, usar campo simples; se há, usar dropdown
        if (suggestions.isEmpty()) {
            // Campo simples quando não há sugestões cadastradas
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                placeholder = { Text(placeholder.ifBlank { "Digite aqui..." }) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else Color.LightGray,
                    unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else Color.LightGray
                ),
                isError = isError,
                supportingText = if (isError && errorMessage != null) {
                    { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                } else null
            )
        } else {
            // Campo com autocomplete quando há sugestões
            ExposedDropdownMenuBox(
                expanded = expanded && filteredSuggestions.isNotEmpty(),
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { newValue ->
                        onValueChange(newValue)
                        expanded = newValue.isNotBlank() && filteredSuggestions.isNotEmpty()
                    },
                    label = { Text(label) },
                    placeholder = { Text(placeholder.ifBlank { "Digite ou selecione..." }) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else Color.LightGray,
                        unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else Color.LightGray
                    ),
                    isError = isError,
                    supportingText = if (isError && errorMessage != null) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                if (filteredSuggestions.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(filteredSuggestions) { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion) },
                                    onClick = {
                                        onValueChange(suggestion)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
