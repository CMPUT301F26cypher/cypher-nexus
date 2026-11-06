package com.example.campsitereservationsystem

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

import com.example.campsitereservationsystem.campsite.Campsite

// For testing the create profile screen and editing the profile details
// moved it here so i dont have to change the main activity too much
@Composable
fun AccountTest(
    user: User?,
    campsites: List<Campsite>,
    repo: UserRepository,
    onUserChange: (User?) -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current

    var showProfile by remember { mutableStateOf(false) }
    var policyRead by remember { mutableStateOf(false) }
    var newScreen by remember { mutableStateOf("home") }
    var creatingProfile by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }


    BackHandler {
        when {
            newScreen != "home" -> newScreen = "home"
            showProfile -> showProfile = false
            user == null && policyRead && creatingProfile -> policyRead = false
            user == null && creatingProfile -> creatingProfile = false
            else -> onExit()
        }
    }

    // Login if they already have an account
    if (user == null) {
        if (!creatingProfile) {
            LoginScreen(
                error = error,
                onLogin = { email, password ->
                    error = null
                    repo.login(
                        email, password, DeviceId.get(context),
                        onResult = { u ->
                            if (user == null) error = "Inccorect email or password!"
                            else onUserChange(user)
                        },
                        onError = { error = "Something went wrong! Please try again." }
                    )
                }, onCreateAccount = { creatingProfile = true; error = null }
            )
        }

        // US 01.11.01 (As an entrant, I want to see the app policy and guidelines.)
        // if there is no profile yet, read policy and show create profile screen
        else if (!policyRead) {
            PolicyScreen(onContinue = { policyRead = true })
        }
        else {
            CreateAccountScreen(
                erorr = error,
                onGoToLogin = { creatingProfile = false; error = null },
                onCreate = { name, email, phone, password, accRole ->
                    error = null
                    repo.register(
                        User(
                            name = name,
                            email = email,
                            phone = phone,
                            roles = setOf(accRole),
                            deviceId = DeviceId.get(context)
                        ),
                        password,
                        onSuccess = { onUserChange(it) },
                        onEmailTaken = { error = "Email already in use!" }
                        onError = { error = "Something went wrong! Please try again." }
                    )
                }
            )
        }
        return
    }

    when {
        newScreen == "policy" -> PolicyScreen(onBack = { newScreen = "home" })
        newScreen == "admin" -> AdminCampsiteBrowseScreen(
            campsites = campsites,
            onBack = { newScreen = "home" }
        )

        showProfile -> ProfileScreen(
            user = user,
            onSave = { updated ->
                repo.updateUser(updated)
                onUserChange(updated)
            },
            onDelete = {
                repo.delete(user)
                onUserChange(null)
                showProfile = false
                creatingProfile = false
                policyRead = false
            },
            onBack = {
                showProfile = false
            }
        )
        else -> Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome, ${user.name}")
            Text("Email: ${user.email.ifBlank { "(none)" }}")
            Text("Phone: ${user.phone.ifBlank { "(none)" }}")

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { showProfile = true }) { Text("Profile Settings") }
            Button(onClick = { newScreen = "policy" }) { Text("App Policy & Guidelines") }

            // US 03.04.01
            // As an admin, I want to browse campsites.
            if (Role.ADMIN in user.roles) {
                Button(onClick = { newScreen = "admin" }) { Text("Browse Campsites") }
            }
            // temp button for testing if admin functionality works
            // Updated the button so it works with user repo + firestore
            Button(
                onClick = {
                    val updated = user.copy(roles = user.roles + Role.ADMIN)
                    repo.updateUser(updated)
                    onUserChange(updated)
                }
            ) { Text("Become an Admin") }

            // New logout button
            // Forgets device and logs out
            Button(
                onClick = {
                    repo.logout(user)
                    onUserChange(null)
                }
            ) {
                Text("Logout")
            }

            // Exit button
            Button(onClick = onExit) { Text("Exit") }
        }
    }
}
