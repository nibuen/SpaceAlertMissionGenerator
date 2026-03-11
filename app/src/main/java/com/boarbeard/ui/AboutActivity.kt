package com.boarbeard.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.boarbeard.R

class AboutActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    public override fun onCreate(icicle: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(icicle)
        setContent {
            MaterialTheme(colorScheme = SpaceAlertColorScheme) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("About") },
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
                    AboutContent(modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

private val contributors = listOf(
    "Leif Norcott",
    "Thomas Arnold",
    "Christoph König",
    "Torbjörn Eklund",
    "kuhrusty",
    "Marcus Zuber",
)

@Composable
fun AboutContent(modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Links",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        LinkCard(
            painter = rememberVectorPainter(Icons.Filled.Info),
            iconTint = Color(0xFF64B5F6),
            title = "Iterary Blog",
            description = "Dev updates, project write-ups, and behind-the-scenes on all my apps",
            onClick = { uriHandler.openUri("https://blog.iterary.com") }
        )

        LinkCard(
            painter = painterResource(R.drawable.iterary_logo),
            iconTint = Color(0xFF8D6E63),
            title = "Iterary",
            description = "Plan your next adventure with smart travel itineraries and trip tools",
            onClick = { uriHandler.openUri("https://iterary.com") }
        )

        LinkCard(
            painter = rememberVectorPainter(Icons.Filled.Star),
            iconTint = Color(0xFFFFB74D),
            title = "Gamers Paper",
            description = "Board game news, reviews, and tips from the tabletop community",
            onClick = { uriHandler.openUri("https://gamerspaper.com") }
        )

        Spacer(modifier = Modifier.height(4.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Designed and developed by",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        contributors.forEach { name ->
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Special thanks to Maximilian Kalus for the Java version that is the basis for the original algorithm.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LinkCard(
    painter: Painter,
    iconTint: Color,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                colorFilter = ColorFilter.tint(iconTint),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Preview
@Composable
private fun AboutPreview() {
    MaterialTheme(colorScheme = SpaceAlertColorScheme) {
        AboutContent()
    }
}
