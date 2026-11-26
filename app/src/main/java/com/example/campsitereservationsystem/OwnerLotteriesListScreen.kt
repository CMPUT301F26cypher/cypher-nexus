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
import com.example.campsitereservationsystem.OwnerCampsitesPath.Campsite
import com.example.campsitereservationsystem.OwnerCampsitesPath.CampsiteRepository

@Composable
fun OwnerLotteriesListScreen(
    user: User?,
    campsiteRepository: CampsiteRepository,
    lotteryRepository: LotteryRepository,
    onRegisterLottery: (List<Campsite>) -> Unit,
    onBack: () -> Unit
) {
    var campsites by remember {
        mutableStateOf(emptyList<Campsite>())
    }

    //owners campsites with a lottery
    var lotteryCampsites by remember {
        mutableStateOf(emptyList<Campsite>())
    }

    //owners campsites without a lottery
    var noLotteryCampsites by remember {
        mutableStateOf(emptyList<Campsite>())
    }

    //retrive the campsites of the owner
    LaunchedEffect(user?.id) {
        if (user != null) {
            campsites = campsiteRepository.getByOwner(user.id)
        } else {
            campsites = emptyList()
        }
    }

    LaunchedEffect(campsites) {
        //val matchingCampsites = mutableListOf<Campsite>()

        for (campsite in campsites) {
            lotteryRepository.getLotteryByCampsiteID(campsite.id) { lottery ->
                if (lottery != null) {
                    lotteryCampsites = lotteryCampsites + campsite
                }else{
                    noLotteryCampsites = noLotteryCampsites + campsite
                }
            }
        }
    }
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
                text = "My Lotteries",
                fontSize = 40.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                modifier = Modifier.size(
                    width = 275.dp,
                    height = 50.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF819F5C)
                ),
                onClick = {onRegisterLottery(noLotteryCampsites)}
                //onClick = {null}
            ){
                Text(
                    text = "Register Lottery",
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }

            //add campsite button goes here

            //LazyColumn for the list allows it to be scrollable
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(lotteryCampsites.size) { index ->
                    val campsite = lotteryCampsites[index]

                    //can reuse CampsiteListCards() for this as they both have the same format
                    CampsiteListCards(
                        campsite.name,
                        campsite.location
                    )
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