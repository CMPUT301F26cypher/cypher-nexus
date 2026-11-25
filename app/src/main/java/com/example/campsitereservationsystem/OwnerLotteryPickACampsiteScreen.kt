package com.example.campsitereservationsystem

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsitereservationsystem.campsite.Campsite
import com.example.campsitereservationsystem.campsite.CampsiteRepository

@Composable
fun OwnerLotteryPickACampsiteScreen(
    user: User?,
    campsiteRepository: CampsiteRepository,
    campsitesWithoutLotteries: MutableList<Campsite>,
    //onRegisterCampsite: (MutableList<Campsite>) -> Unit,
    onSelect: (Campsite) -> Unit,
    onBack: () -> Unit
) {
    //A box is used to allow the back button to be in the bottom left at all times
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp) // Adds spacing from the screen edges
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "Pick a Campsite",
                fontSize = 40.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            //add campsite button goes here

            //LazyColumn for the list allows it to be scrollable
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                item {
                    for (campsite in campsitesWithoutLotteries){
                        PickACampsiteCards(campsite.name, campsite.location, onSelect = { onSelect(campsite)})
                    }
                }
            }
        }

        //Back button in the bottom left
        Button(
            onClick = { onBack() },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .scale(1.2f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF819F5C)
            )
        ) {
            Text(
                text = "Back",
                color = Color.Black
            )
        }
    }

}

//the function that defines what the entries for a campsite on the list looks like
@Composable
fun PickACampsiteCards(
    name: String,
    location: String,
    onSelect: () -> Unit
){
    Card(
        modifier = Modifier
            .width(350.dp)
            .wrapContentHeight()
            .padding(
                top = 20.dp
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF676564)
        )
    ){
        Text(
            modifier = Modifier.padding(
                start = 15.dp,
                top = 15.dp,
            ),
            text = name,
            fontSize = 26.sp
        )
        //Spacer(modifier = Modifier.height(5.dp))
        Text(
            modifier = Modifier.padding(
                start = 15.dp,
                top = 5.dp,
            ),
            text = location,
            fontSize = 16.sp
        )

        //da buttons
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 18.dp,
                    bottom = 12.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ){
            PickACampsiteButtons("Select", onClick = onSelect)
        }
    }
}

@Composable
fun PickACampsiteButtons(
    text: String,
    onClick: () -> Unit
){
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF819F5C)
        ),
        modifier = Modifier
            .wrapContentWidth()
            .height(40.dp)
            .padding(
                top = 5.dp
            ),
        shape = RoundedCornerShape(10.dp)
    ){
        Text(
            text = text,
            color = Color.Black
        )
    }
}