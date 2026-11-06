package com.example.campsitereservationsystem

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import coil3.compose.AsyncImage // coil version 3.3.0 and newer
//import coil.compose.AsyncImage - coil version 2.7 import library

/**
 * AsyncImage() - function to display image on screen
 *  -needs model/uri
 *  -needs content description
 * rememberLauncherForActivityResult() - Photo picker launcher for compose function
 *
 */
@Composable
fun ImageUploadTestScreen(
    onBack: ()-> Unit,
    notificationController: NotificationController,
    user: User
){
    var pickedImageURI by remember { mutableStateOf<Uri?>(null)} // holds image address


    Column(
        modifier = Modifier.padding(30.dp)
    ){
        Button(onClick = onBack){
            Text("Back")
        }

        Button(
            onClick = {
                notificationController.sendNotification(
                    receiverID = user.id,
                    message = "Test Notification"
                )
            }
        ) {
            Text("Send Notification")
        }

        ImagePickerButton(
            onImageSelected = {uri ->
                pickedImageURI = uri
                Log.d("PhotoPicker", "Selected URI: $uri")
            }
        )

        if (pickedImageURI != null) {
            Text("Image Selected")

            //Display selected image
            AsyncImage(
                model = pickedImageURI,
                contentDescription = "Selected Image"
            )
        }else{
            Text("No Image Selected")
        }
    }
}