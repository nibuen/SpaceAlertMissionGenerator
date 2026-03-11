package com.boarbeard.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class HelpActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    public override fun onCreate(icicle: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(icicle)
        setContent {
            MaterialTheme(colorScheme = SpaceAlertColorScheme) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Help") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = SpaceAlertRedDark,
                                titleContentColor = Color.White
                            )
                        )
                    },
                    containerColor = SpaceAlertBackground
                ) { padding ->
                    HelpContent(modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
fun HelpContent(modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Getting started
        Text(
            text = "Getting Started",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "This app generates and plays audio missions for the Space Alert board game. " +
                    "It narrates threats, data transfers, and phase changes in real time so you can focus on playing.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
        )

        HelpCard(
            icon = Icons.Filled.PlayArrow,
            iconTint = Color(0xFF81C784),
            title = "Play / Pause",
            description = "Tap play to start. If no mission is loaded yet, you'll be taken to the " +
                    "New Mission screen first to pick a mission type and configure options. " +
                    "Tap again to pause mid-mission."
        )

        HelpCard(
            icon = Icons.Filled.Add,
            iconTint = Color(0xFF64B5F6),
            title = "New Mission",
            description = "Open from the menu (\u22EE) to pick a mission type and adjust settings. " +
                    "Choose Random to generate a unique mission each time, or select a pre-built " +
                    "mission from the book \u2014 test runs, simulations, real missions, and Double Action variants are all included."
        )

        HelpCard(
            icon = Icons.Filled.Refresh,
            iconTint = Color(0xFFFFB74D),
            title = "Restart Mission",
            description = "Resets the current mission back to the beginning without generating a new one. " +
                    "Useful if you want to replay the same mission."
        )

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Mission Options",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        HelpCard(
            icon = Icons.Filled.Settings,
            iconTint = Color(0xFFCE93D8),
            title = "Unconfirmed Reports",
            description = "When enabled, unconfirmed threat reports are automatically resolved based on your player count. " +
                    "With 5 players they are discarded; with fewer players they become normal threats. " +
                    "Turn this off to handle unconfirmed reports manually with the threat cards."
        )

        HelpCard(
            icon = Icons.Filled.Settings,
            iconTint = Color(0xFFCE93D8),
            title = "Compress Time",
            description = "Speeds up the silence between events, making missions shorter and more intense. " +
                    "Great for experienced crews who want less downtime."
        )

        HelpCard(
            icon = Icons.Filled.Info,
            iconTint = Color(0xFFCE93D8),
            title = "Random Mission Parameters",
            description = "When playing random missions, you can tune mission length, threat difficulty, " +
                    "incoming data frequency, and enable double threats. If you're unsure, the defaults are a good starting point \u2014 " +
                    "use \"Reset to defaults\" at the bottom of the options to restore them."
        )
    }
}

@Composable
private fun HelpCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    description: String,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun HelpPreview() {
    MaterialTheme(colorScheme = SpaceAlertColorScheme) {
        HelpContent()
    }
}
