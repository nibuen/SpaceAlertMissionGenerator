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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class AboutActivity : ComponentActivity() {
    public override fun onCreate(icicle: Bundle?) {
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    AboutContent()
                }
            }
        }
        super.onCreate(icicle)
    }
}

class Person(
    val name: String,
)

private val attirbutions = listOf(
    Person(
        name = "Leif Norcott",
    ),
    Person(
        name = "Thomas Arnold",
    ),
    Person(
        name = "Christoph König",
    ),
    Person(
        name = "Torbjörn Eklund",
    ),
    Person(
        name = "kuhrusty",
    ),
    Person(
        name = "Marcus Zuber",
    ),
)

@Composable
fun AboutContent() {
    Column(
        Modifier
            .fillMaxSize()
            .padding(2.dp)
            .background(Color.White)
    ) {
        // UriHandler parse and opens URI inside AnnotatedString Item in Browse
        val uriHandler = LocalUriHandler.current

        Text(
            text = "About",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(top = 12.dp)
        )
        Spacer(modifier = Modifier.padding(vertical = 6.dp))

        val iteraryAnnotatedString = buildAnnotatedString {
            val text = "Find all my apps on my Iterary Blog"
            val startIndex = text.indexOf("Iterary")
            val endIndex = text.indexOf("Blog") + 4
            append(text)
            addStyle(
                style = SpanStyle(
                    color = Color(0xff64B5F6),
                    //fontSize = 18.sp,
                    textDecoration = TextDecoration.Underline
                ), start = startIndex, end = endIndex
            )
            addStringAnnotation(
                tag = "URL",
                annotation = "https://blog.iterary.com",
                start = startIndex,
                end = endIndex
            )
        }

        ClickableText(
            text = iteraryAnnotatedString,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp).align(alignment = Alignment.CenterHorizontally),
            onClick = {
                iteraryAnnotatedString
                    .getStringAnnotations("URL", it, it)
                    .firstOrNull()?.let { stringAnnotation ->
                        uriHandler.openUri(stringAnnotation.item)
                    }
            }
        )
        Spacer(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            text = "Designed and developed by",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp).align(alignment = Alignment.CenterHorizontally),
        )
        val bullet = "\u2022"
        val paragraphStyle = ParagraphStyle(textIndent = TextIndent(restLine = 12.sp))
        Text(
            text = buildAnnotatedString {
                attirbutions.forEach {
                    withStyle(style = paragraphStyle) {
                        //append(bullet)
                        //append("\t\t")
                        append(it.name)
                    }
                }
            },
            modifier = Modifier.padding(horizontal = 24.dp, 12.dp).align(alignment = Alignment.CenterHorizontally),
        )
        Text(
            text = "Special Thanks to Maximilian Kalus for Java version that is the new basis for the original algorithm.",
            modifier = Modifier.padding(horizontal = 12.dp, 12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview
@Composable
private fun HelpPreview() {
    MaterialTheme {
        AboutContent()
    }
}