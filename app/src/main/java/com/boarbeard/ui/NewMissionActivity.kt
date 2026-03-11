package com.boarbeard.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import android.preference.PreferenceManager
import com.boarbeard.R
import kotlin.math.roundToInt

// App color constants matching the XML theme (style.xml / colors.xml)
internal val SpaceAlertRed = Color(0xFFF44336)
internal val SpaceAlertRedDark = Color(0xFFB71C1C)
internal val SpaceAlertSurface = Color.Black
internal val SpaceAlertBackground = Color(0xFF424242)

internal val SpaceAlertColorScheme = darkColorScheme(
    primary = SpaceAlertRed,
    onPrimary = Color.White,
    primaryContainer = SpaceAlertRedDark,
    secondary = SpaceAlertRed,
    onSecondary = Color.White,
    surface = SpaceAlertSurface,
    onSurface = Color.White,
    background = SpaceAlertBackground,
    onBackground = Color.White,
    surfaceVariant = SpaceAlertBackground,
    onSurfaceVariant = Color.White,
)

class NewMissionActivity : ComponentActivity() {

    companion object {
        const val RESULT_MISSION_TYPE_ORDINAL = "mission_type_ordinal"
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val currentOrdinal = intent.getIntExtra(RESULT_MISSION_TYPE_ORDINAL, MissionType.Random.ordinal)

        setContent {
            MaterialTheme(colorScheme = SpaceAlertColorScheme) {
                var selectedType by remember { mutableStateOf(MissionType.entries[currentOrdinal]) }

                // Preference state
                var playerCount by remember { mutableIntStateOf(prefs.getInt("playerCount", 5)) }
                var stompUnconfirmed by remember { mutableStateOf(prefs.getBoolean("stompUnconfirmedReportsPreference", true)) }
                var compressTime by remember { mutableStateOf(prefs.getBoolean("compressTimePreference", false)) }
                var missionLength by remember { mutableIntStateOf(prefs.getInt("missionLengthPreference", 540)) }
                var threatDifficulty by remember { mutableIntStateOf(prefs.getInt("threatDifficultyPreference", 8)) }
                var incomingDataMin by remember { mutableIntStateOf(prefs.getInt("numberIncomingData", 2)) }
                var incomingDataMax by remember { mutableIntStateOf(prefs.getInt("numberIncomingDataRightValue", 4)) }
                var enableDoubleThreats by remember { mutableStateOf(prefs.getBoolean("enable_double_threats", false)) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.mission_new_mission)) },
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        // Start button pinned at top
                        Button(
                            onClick = {
                                // Save preferences
                                prefs.edit().apply {
                                    putInt("playerCount", playerCount)
                                    putBoolean("stompUnconfirmedReportsPreference", stompUnconfirmed)
                                    putBoolean("compressTimePreference", compressTime)
                                    if (selectedType.group.isRandom) {
                                        putInt("missionLengthPreference", missionLength)
                                        putInt("threatDifficultyPreference", threatDifficulty)
                                        putInt("numberIncomingData", incomingDataMin)
                                        putInt("numberIncomingDataRightValue", incomingDataMax)
                                        putBoolean("enable_double_threats", enableDoubleThreats)
                                    }
                                    apply()
                                }

                                val resultIntent = Intent().apply {
                                    putExtra(RESULT_MISSION_TYPE_ORDINAL, selectedType.ordinal)
                                }
                                setResult(Activity.RESULT_OK, resultIntent)
                                finish()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpaceAlertRed,
                                contentColor = Color.White
                            )
                        ) {
                            Text(stringResource(R.string.start_new_mission))
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                        // Scrollable options below
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            // Mission type dropdown
                            MissionTypeDropdown(selectedType) { selectedType = it }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Options section
                            Text(
                                text = stringResource(R.string.mission_options),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Common options (all mission types)
                            CheckboxRow(
                                label = stringResource(R.string.pref_stomp_unconfirmed_reports_title),
                                checked = stompUnconfirmed,
                                onCheckedChange = { stompUnconfirmed = it }
                            )

                            if (stompUnconfirmed) {
                                SliderRow(
                                    label = stringResource(R.string.pref_player_count_dialog_title),
                                    value = playerCount,
                                    valueRange = 1..5,
                                    formatValue = { "$it" },
                                    onValueChange = { playerCount = it }
                                )
                            }

                            CheckboxRow(
                                label = stringResource(R.string.pref_compress_time_title),
                                checked = compressTime,
                                onCheckedChange = { compressTime = it }
                            )

                            // Random-only options
                            if (selectedType.group.isRandom) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.pref_mission),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                SliderRow(
                                    label = stringResource(R.string.pref_mission_length_dialog_title),
                                    value = missionLength,
                                    valueRange = 540..840,
                                    step = 30,
                                    formatValue = { "${it}s" },
                                    onValueChange = { missionLength = it }
                                )

                                SliderRow(
                                    label = stringResource(R.string.pref_threat_difficulty_dialog_title),
                                    value = threatDifficulty,
                                    valueRange = 1..14,
                                    formatValue = { "$it" },
                                    onValueChange = { threatDifficulty = it }
                                )

                                RangeSliderRow(
                                    label = stringResource(R.string.pref_incoming_data_dialog_title),
                                    minValue = incomingDataMin,
                                    maxValue = incomingDataMax,
                                    valueRange = 1..6,
                                    formatValue = { "$it" },
                                    onValueChange = { min, max ->
                                        incomingDataMin = min
                                        incomingDataMax = max
                                    }
                                )

                                CheckboxRow(
                                    label = stringResource(R.string.pref_enableDoubleThreats_title),
                                    checked = enableDoubleThreats,
                                    onCheckedChange = { enableDoubleThreats = it }
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                TextButton(
                                    onClick = {
                                        missionLength = 540
                                        threatDifficulty = 8
                                        incomingDataMin = 2
                                        incomingDataMax = 4
                                        enableDoubleThreats = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        "Reset to defaults",
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MissionTypeDropdown(
    selectedType: MissionType,
    onTypeSelected: (MissionType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Build flat list with group headers for the dropdown menu
    val groupedTypes = MissionType.entries.groupBy { it.group }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = stringResource(selectedType.resId),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.mission_type)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = SpaceAlertRed,
                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                focusedLabelColor = SpaceAlertRed,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                focusedTrailingIconColor = SpaceAlertRed,
                unfocusedTrailingIconColor = Color.White.copy(alpha = 0.7f),
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = SpaceAlertBackground
        ) {
            MissionGroup.entries.forEach { group ->
                val types = groupedTypes[group] ?: return@forEach

                // Group header
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(group.displayNameResId),
                            fontWeight = FontWeight.Bold,
                            color = SpaceAlertRed
                        )
                    },
                    onClick = {},
                    enabled = false
                )

                // Mission types within this group
                types.forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(type.resId),
                                color = if (type == selectedType) SpaceAlertRed else Color.White
                            )
                        },
                        onClick = {
                            onTypeSelected(type)
                            expanded = false
                        },
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = SpaceAlertRed,
                uncheckedColor = Color.White.copy(alpha = 0.7f),
                checkmarkColor = Color.White
            )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RangeSliderRow(
    label: String,
    minValue: Int,
    maxValue: Int,
    valueRange: IntRange,
    formatValue: (Int) -> String,
    onValueChange: (min: Int, max: Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text("${formatValue(minValue)} – ${formatValue(maxValue)}", fontWeight = FontWeight.Bold)
        }
        val steps = valueRange.last - valueRange.first - 1
        RangeSlider(
            value = minValue.toFloat()..maxValue.toFloat(),
            onValueChange = { range ->
                onValueChange(range.start.roundToInt(), range.endInclusive.roundToInt())
            },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = if (steps > 0) steps else 0,
            colors = SliderDefaults.colors(
                thumbColor = SpaceAlertRed,
                activeTrackColor = SpaceAlertRed,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun SliderRow(
    label: String,
    value: Int,
    valueRange: IntRange,
    step: Int = 1,
    formatValue: (Int) -> String,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label)
            Text(formatValue(value), fontWeight = FontWeight.Bold)
        }
        val steps = ((valueRange.last - valueRange.first) / step) - 1
        Slider(
            value = value.toFloat(),
            onValueChange = {
                val rounded = (it / step).roundToInt() * step
                onValueChange(rounded.coerceIn(valueRange))
            },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = if (steps > 0) steps else 0,
            colors = SliderDefaults.colors(
                thumbColor = SpaceAlertRed,
                activeTrackColor = SpaceAlertRed,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}
