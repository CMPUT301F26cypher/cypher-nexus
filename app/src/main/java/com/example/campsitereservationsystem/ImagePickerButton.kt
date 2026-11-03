package com.example.campsitereservationsystem
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.runtime.Composable
import androidx.compose.material3.Button
import androidx.compose.material3.Text
/**
 * API's/LIBRARIES NEEDED:
 *  -Android Studio Picker
 *
 *
 * Utilize uri - address of selected photo
 * Photopicker reuses same lines just going to make
 * a helper function to open photo picker and return uri address of photo
 *
 * Can add parameters in future if I need to add different button design
 * for specific pages
 */
@Composable
fun ImagePickerButton(
    onImageSelected: (Uri) -> Unit //Returns selected image URI
){
    val imagePickerLauncher = rememberLauncherForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null){
            onImageSelected(uri)
            Log.d("PhotoPicker", "Selected URI: $uri")
        }
    }

    Button(
        onClick = {
            imagePickerLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
        }
    ){
        Text("Select Image")
    }
}