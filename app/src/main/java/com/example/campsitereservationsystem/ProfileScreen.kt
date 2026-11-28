package com.example.campsitereservationsystem

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/*
    ProfileScreen.kt

    Lets a user edit their name, email, and phone number. US 01.07.03
    Also lets them delete their account. US 01.08.01
    Also lets them add, change or remove their profile photo. US 01.08.02
    Admins can also give themselves the owner role. US 03.11.01

    NOTE: Does not check if an edited email is unique.
    Deleting a profile might not remove it from the waitlist.
    ImageRepository does not report failed photo saves.
 */

@Composable
fun ProfileScreen(
    user: User,
    imageRepo: ImageRepository,
    onSave: (User) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    adminView: Boolean = false
) {

    val context = LocalContext.current

    var name by remember { mutableStateOf(user.name) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone) }

    var showDeleteDialog by remember { mutableStateOf(false) }

    val nameError = name.isBlank()
    val emailError = email.isBlank()
    val phoneError = phone.isBlank()
    val canSave = !nameError && !emailError && !phoneError

    // Photo state vars
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imageChanged by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    // Load the current photo ONCE
    LaunchedEffect(user.id) {
        imageRepo.getProfileImage(user.id) { bytes ->
            if (!imageChanged) imageBytes = bytes
        }
    }

    // Called by ImagePickerButton with the chosen image (null if cancelled)
    val onPicked: (Uri?) -> Unit = { uri ->
        if (uri != null) {
            val bytes = ProfileImage.compress(context, uri)
            if (bytes != null) {
                imageBytes = bytes
                imageChanged = true
                message = null
            } else {
                message = "Could not read that image."
            }
        }
    }

    val bitmap = remember(imageBytes) { ProfileImage.toBitmap(imageBytes) }

    // Admin ONLY choices US 03.11.01
    val isAdmin = Role.ADMIN in user.roles
    var wantsOwner by remember { mutableStateOf(Role.OWNER in user.roles) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Back") }
            Text("Profile", style = MaterialTheme.typography.titleLarge)
        }
        Spacer(Modifier.height(8.dp))

        // Profile Photo US 01.08.02
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // *** Might need to edit BOX size and background color
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Profile Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        user.name.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge
                    )
                }
            }
        }
        if (!adminView) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ImagePickerButton(onImageSelected = onPicked)
                if (imageBytes != null) {
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = {
                            imageBytes = null
                            imageChanged = true
                        }
                    ) {
                        Text("Remove Photo")
                    }
                }
            }
        }
        message?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            isError = nameError,
            readOnly = adminView,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            isError = emailError,
            readOnly = adminView,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            isError = phoneError,
            readOnly = adminView,
            modifier = Modifier.fillMaxWidth()
        )

        // Admin only can see this section US 03.11.01
        if (isAdmin) {
            Spacer(Modifier.height(16.dp))
            Text("Add another role?", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = wantsOwner,
                    onCheckedChange = { wantsOwner = it }
                )
                Text("Owner")
            }
        }

        Spacer(Modifier.height(16.dp))
        if (!adminView) {
            Button(
                onClick = {
                    // Write the photo only if the user picked or removed one
                    if (imageChanged) {
                        val bytes = imageBytes
                        if (bytes == null) {
                            imageRepo.deleteProfileImage(user.id)
                        } else {
                            imageRepo.saveProfileImage(user.id, bytes)
                        }
                    }
                    // Admins can turn the owner role on or off. The admin role is always kept.
                    val newRoles: List<Role> = if (isAdmin) {
                        if (wantsOwner) user.roles + Role.OWNER else user.roles - Role.OWNER
                    } else {
                        user.roles
                    }
                    onSave(user.copy(name = name, email = email, phone = phone, roles = newRoles))
                    onBack()
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Changes") }
        }
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Delete Profile") }
    }


    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Profile") },
            text = { Text("Are you sure you want to delete profile?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) { Text("Cancel") }
            }
        )
    }
}
