package com.example.campsitereservationsystem

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext


import com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite

// For testing the create profile screen and editing the profile details
// moved it here so i dont have to change the main activity too much
@Composable
fun AccountTest(
    user: User?,
    campsites: List<Campsite>,
    repo: UserRepository,
    onUserChange: (User?) -> Unit,
    onExit: () -> Unit,
    onEntrantHome: () -> Unit = {},
    onOwnerHome: () -> Unit = {},
    onAdminHome: () -> Unit = {}
) {
    val context = LocalContext.current

    //little helper function to direct to correct screen after user logs in
    fun navigateByRole(user: User) {
        when {
            Role.ADMIN in user.roles -> onAdminHome()
            Role.OWNER in user.roles -> onOwnerHome()
            Role.ENTRANT in user.roles -> onEntrantHome()
        }
    }

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
        if (!creatingProfile) {
            LoginScreen(
                error = error,
                onLogin = { email, password ->
                    error = null
                    repo.login(
                        email, password, DeviceId.get(context),
                        onResult = { u ->
                            if (u == null) {
                                error = "Incorrect email or password!"
                            } else {
                                onUserChange(u)
                                navigateByRole(u)
                            }
                        },
                        onError = {
                            error = "Something went wrong! Please try again."
                        }
                    )
                }, onDeviceLogin = {
                    error = null
                    repo.findByDeviceId(
                        deviceId = DeviceId.get(context),
                        onResult = { u ->
                            if (u == null) {
                                error = "No account found for this device!"
                            } else {
                                repo.startListening(u.id)
                                onUserChange(u)
                                navigateByRole(u)
                            }
                        },
                        onError = {
                            error = "Something went wrong! Please try again."
                        }
                    )
                },
                onCreateAccount = { creatingProfile = true; error = null }
            )
        }

        // US 01.11.01 (As an entrant, I want to see the app policy and guidelines.)
        // if there is no profile yet, read policy and show create profile screen
        else if (!policyRead) {
            PolicyScreen(onContinue = { policyRead = true })
        }
        else {
            CreateAccountScreen(
                error = error,
                onGoToLogin = { creatingProfile = false; error = null },
                onCreate = { name, email, phone, password, accRole ->
                    error = null
                    repo.register(
                        User(
                            name = name,
                            email = email,
                            phone = phone,
                            roles = listOf(accRole),
                            deviceId = DeviceId.get(context)
                        ),
                        password,
                        onSuccess = {
                            onUserChange(it)

                            when (accRole) {
                                Role.ENTRANT -> onEntrantHome()
                                Role.OWNER -> onOwnerHome()
                                Role.ADMIN -> onAdminHome()
                            }
                        },
                        onEmailTaken = { error = "Email already in use!" },
                        onBanned = { error = "This account has violated app policy and guidelines!" },
                        onError = { error = "Something went wrong! Please try again." }
                    )
                }


            )
        }
        return
}


