package com.example.campsitereservationsystem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun CreateAccountScreen(onCreate: (name: String, email: String, phone: String, password: String, role: Role) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Added option to choose different role when you create an account
    var accRole by remember { mutableStateOf(Role.ENTRANT) }


    // Added password fields
    // When the user types their password, it will be hidden and they can toggle to check their password
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val mismatch = confirmPassword.isNotEmpty() && confirmPassword != password
    val passwordTooShort = password.isNotEmpty() && password.length < 8


    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Text("Welcome to Campsite")
        Spacer(modifier = Modifier.height(24.dp))
        Text("Create Account")
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()

        )

        // Password
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            isError = passwordTooShort,
            // Auto completed code by android studio's built in gemini
            supportingText = {
                if (passwordTooShort) {
                    Text("Password must be at least 8 characters long")
                }
            },
            visualTransformation =
                if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "Hide" else "Show")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            isError = mismatch,
            // Auto completed code by android studio's built in gemini
            supportingText = {
                if (mismatch) {
                    Text("Passwords do not match")
                }
            },
            visualTransformation =
                if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        // Role selction
        Spacer(modifier = Modifier.height(12.dp))
        Text("Select Role:")
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = { accRole = Role.ENTRANT },
                modifier = Modifier.weight(1f)
            ) {
                Text("Entrant")
                }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = { accRole = Role.OWNER },
                modifier = Modifier.weight(1f)
            ) {
                Text("Owner")
            }
        }

        // Confirm create account
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onCreate(name, email, phone, password, accRole) },
            enabled = name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()
                    && password.isNotBlank() && confirmPassword.isNotBlank()
                    && !passwordTooShort && !mismatch && password == confirmPassword,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Account")
        }
    }
}