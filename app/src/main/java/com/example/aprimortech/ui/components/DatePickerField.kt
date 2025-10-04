package com.example.aprimortech.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    isYearOnly: Boolean = false
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = if (isYearOnly) {
        SimpleDateFormat("yyyy", Locale.getDefault())
    } else {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { /* Read only */ },
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Selecionar data",
                    modifier = Modifier.clickable { showDatePicker = true }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            readOnly = true,
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

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = try {
                    if (value.isNotBlank()) {
                        if (isYearOnly) {
                            // Para ano, criar data do 1º de janeiro do ano
                            Calendar.getInstance().apply {
                                set(Calendar.YEAR, value.toInt())
                                set(Calendar.MONTH, 0)
                                set(Calendar.DAY_OF_MONTH, 1)
                            }.timeInMillis
                        } else {
                            dateFormatter.parse(value)?.time
                        }
                    } else {
                        System.currentTimeMillis()
                    }
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val selectedDate = Date(millis)
                                val formattedValue = if (isYearOnly) {
                                    SimpleDateFormat("yyyy", Locale.getDefault()).format(selectedDate)
                                } else {
                                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate)
                                }
                                onValueChange(formattedValue)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
