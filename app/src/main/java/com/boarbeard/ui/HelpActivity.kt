package com.boarbeard.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class HelpActivity : ComponentActivity() {
    public override fun onCreate(icicle: Bundle?) {
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    HelpContent()
                }
            }
        }
        super.onCreate(icicle)
    }
}

class HelpItem(
    val name: String,
    val description: String,
)

private val messages = listOf(
    HelpItem(
        name = "Type",
        description = "Choose an already generated mission or to generate a random mission."
    ),
    HelpItem(name = "Reset", description = "Resets the current mission to beginning."),
    HelpItem(name = "New", description = "Generates a new mission with the current preferences."),
    HelpItem(name = "Preferences", description = "Preferences to set for the mission."),
    HelpItem(name = "About", description = "Information about who helped create this Application."),
    HelpItem(name = "Help", description = "This Page."),
)

@Composable
fun HelpContent() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(2.dp)
            .background(Color.White)
    ) {
        Text(
            text = "Help",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(top = 12.dp)
        )
        Spacer(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            text = "Clicking the play/pause button will generate and start a mission using the current preferences.\n\n" +
                    "If you change the preferences make sure to create a new mission under the menu to get the new settings.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 24.dp, 12.dp)
        )
        Spacer(modifier = Modifier.padding(vertical = 6.dp))

        val bullet = "\u2022"
        val paragraphStyle = ParagraphStyle(textIndent = TextIndent(restLine = 12.sp))
        Text(
            text = buildAnnotatedString {
                messages.forEach {
                    withStyle(style = paragraphStyle) {
                        append(bullet)
                        append("\t\t")
                        withStyle(style = SpanStyle(color = Color.Red)) {
                            append(it.name)
                        }
                        append(": ")
                        append(it.description)
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 24.dp, 12.dp),
        )
    }
}

@Preview
@Composable
private fun HelpPreview() {
    MaterialTheme {
        HelpContent()
    }
}