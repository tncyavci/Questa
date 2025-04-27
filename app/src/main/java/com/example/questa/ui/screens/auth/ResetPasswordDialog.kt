package com.example.questa.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ResetPasswordDialog(
    email: String,
    onDismiss: () -> Unit,
    onResetPassword: (String) -> Unit
) {
    var resetEmail by remember { mutableStateOf(email) }
    var isEmailValid by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column {
                Text(
                    "Enter your email address and we'll send you a link to reset your password.",
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = resetEmail,
                    onValueChange = { 
                        resetEmail = it
                        isEmailValid = isValidEmail(resetEmail)
                    },
                    label = { Text("Email") },
                    isError = !isEmailValid,
                    supportingText = {
                        if (!isEmailValid) {
                            Text("Please enter a valid email address")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (isValidEmail(resetEmail)) {
                        onResetPassword(resetEmail)
                    } else {
                        isEmailValid = false
                    }
                },
                enabled = resetEmail.isNotBlank()
            ) {
                Text("Reset Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
} 