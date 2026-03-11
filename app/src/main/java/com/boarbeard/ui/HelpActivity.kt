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
import androidx.compose.ui.res.stringResource
import com.boarbeard.R
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
                            title = { Text(stringResource(R.string.mission_help)) },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.navigate_back),
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
            text = stringResource(R.string.help_getting_started),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = stringResource(R.string.help_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
        )

        HelpCard(
            icon = Icons.Filled.PlayArrow,
            iconTint = Color(0xFF81C784),
            title = stringResource(R.string.help_play_pause_title),
            description = stringResource(R.string.help_play_pause_description)
        )

        HelpCard(
            icon = Icons.Filled.Add,
            iconTint = Color(0xFF64B5F6),
            title = stringResource(R.string.help_new_mission_title),
            description = stringResource(R.string.help_new_mission_description)
        )

        HelpCard(
            icon = Icons.Filled.Refresh,
            iconTint = Color(0xFFFFB74D),
            title = stringResource(R.string.help_restart_title),
            description = stringResource(R.string.help_restart_description)
        )

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.help_options_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        HelpCard(
            icon = Icons.Filled.Settings,
            iconTint = Color(0xFFCE93D8),
            title = stringResource(R.string.help_unconfirmed_title),
            description = stringResource(R.string.help_unconfirmed_description)
        )

        HelpCard(
            icon = Icons.Filled.Settings,
            iconTint = Color(0xFFCE93D8),
            title = stringResource(R.string.help_compress_title),
            description = stringResource(R.string.help_compress_description)
        )

        HelpCard(
            icon = Icons.Filled.Info,
            iconTint = Color(0xFFCE93D8),
            title = stringResource(R.string.help_random_params_title),
            description = stringResource(R.string.help_random_params_description)
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
