package com.example.campsitereservationsystem.AdminPages

import com.example.campsitereservationsystem.UserRepository
import com.example.campsitereservationsystem.User
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color


@Composable
fun AdminProfileReview(onBack: () -> Unit) {

    val userRepo = remember { UserRepository() }

    var users by remember { mutableStateOf<List<User>>(emptyList()) }

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
            AdminProfileRow(user)
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        AdminMenuButtons("Back", onBack)
    }
}
@Composable
private fun AdminProfileRow(user: User) {
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
                onClick = {
                    // View selected user profile
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF819F5C)
                )
            ){
                Text("View Profile Details", color = Color.Black)
            }
        }
    }
}