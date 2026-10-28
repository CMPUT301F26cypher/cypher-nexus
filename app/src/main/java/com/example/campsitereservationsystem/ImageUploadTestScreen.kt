package com.example.campsitereservationsystem

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
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
fun ImageUploadTestScreen(onBack: ()-> Unit){
    var pickedImageURI by remember { mutableStateOf<Uri?>(null)} // holds image address

    val imagePickerLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null){
            pickedImageURI = uri
            Log.d("PhotoPicker", "Selected URI: $uri")
        } else{
            Log.d("PhotoPicker", "No Image Selected")
        }
    }

    Column(
        modifier = Modifier.padding(16.dp)
    ){
        Button(onClick = onBack){
            Text("Back")
        }
        Button(
            onClick = {
                imagePickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            }
        ){
            Text("Select Image")
        }

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