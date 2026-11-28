package com.example.campsitereservationsystem.AdminPages

import com.example.campsitereservationsystem.UserRepository
import com.example.campsitereservationsystem.User
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.CheckboxDefaults.colors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color


/*
    AdminProfileReview.kt

    Admin list of every profile.
    Admins can remove a profile that violates app policy. (US 03.02.01)
    Admins do not have the option to delete their own profile.
 */

@Composable
fun AdminProfileReview(
    currentUserId: String = "",
    onViewProfile: (User) -> Unit,
    onBack: () -> Unit
) {

    val userRepo = remember { UserRepository() }

    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var toRemove by remember { mutableStateOf<User?>(null) }

    // Retrieve all users
    LaunchedEffect(userRepo) {
        userRepo.getAllUsers {
            users = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Review Profiles", fontSize = 30.sp)

        Spacer(modifier = Modifier.height(25.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            items(users, key = { it.id }) { user ->
                AdminProfileRow(
                    user = user,
                    canRemove = user.id != currentUserId,
                    onViewProfile = { onViewProfile(user) },
                    onRemove = { toRemove = user }
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        AdminMenuButtons("Back", onBack)
    }

    // Confirm before deleting (US 03.02.01)
    toRemove?.let { user ->
        AlertDialog(
            onDismissRequest = { toRemove = null },
            title = { Text("Remove profile") },
            text = { Text("Remove ${user.name}'s profile? They will also be banned from creating a new account.") },
            confirmButton = {
                TextButton(onClick = {
                    userRepo.banAndDelete(user)
                    users = users.filter { it.id != user.id }
                    toRemove = null
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { toRemove = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AdminProfileRow(
    user: User,
    canRemove: Boolean,
    onViewProfile: () -> Unit,
    onRemove: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(user.name, fontSize = 20.sp)
            Text("Role: ${user.roles.joinToString(", ")}", fontSize = 16.sp)

            Button(
                onClick = onViewProfile,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF819F5C)
                )
            ){
                Text("View Profile Details", color = Color.Black)
            }

            if (canRemove) {
                OutlinedButton(onClick = onRemove) {
                    Text("Remove Profile")
                }
            }
        }
    }
}