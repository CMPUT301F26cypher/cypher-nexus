// TEMPORARY: lets an owner pick one of their campsites from the Test menu to open Lottery Details (US 02.04.03, 02.04.04).
// Delete this file once the team builds My Lotteries (Frame 20) and Pick a Campsite (Frame 21).

package com.example.campsitereservationsystem

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.campsitereservationsystem.campsite.Campsite

/**
 * TEMPORARY list of the owner's campsites. Pressing Select opens the Lottery Details form.
 *
 * @param campsites The owner's campsites.
 * @param onSelect Called with the campsite the owner picked.
 * @param onBack Called when the owner leaves the screen.
 */
@Composable
fun TempPickCampsiteScreen(
    campsites: List<Campsite>,
    onSelect: (Campsite) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back")
        }
        Text("Pick a Campsite (TEMP)", style = MaterialTheme.typography.headlineSmall)

        if (campsites.isEmpty()) {
            Text("No campsites yet. Add one in My Campsites first.")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(campsites) { site ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(site.name, style = MaterialTheme.typography.titleMedium)
                        Text(site.location)
                        Button(onClick = { onSelect(site) }) {
                            Text("Select")
                        }
                    }
                }
            }
        }
    }
}


