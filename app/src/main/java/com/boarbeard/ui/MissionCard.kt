package com.boarbeard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boarbeard.audio.MissionLog

@Composable
fun MissionCard(data: MissionLog, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .wrapContentHeight()
            .padding(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black,
        ),
    ) {
        Row(
            modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(4.dp)
        ) {
            if (data.clockText != null) {
                Text(
                    text = "${data.clockText}",
                    fontSize = 20.sp,
                    color = data.clockColor,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .wrapContentHeight(),
                )
            }

            Text(
                text = "${data.actionText}",
                fontSize = 20.sp,
                color = data.actionColor,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .weight(1f)
            )
        }
    }
}

@Preview
@Composable
private fun MissionCardPreview() {
    val data = listOf(
        MissionLog("Enemy activity detected. Please begin 1st Phase.", "0:00"),
        MissionLog("Hello", "0:00"),
        MissionLog("Begin First Phase", "0:00")
    )
    MaterialTheme {
        LazyColumn(modifier = Modifier.padding(8.dp)) {
            itemsIndexed(data) { _, item ->
                MissionCard(data = item)
            }
        }

    }
}