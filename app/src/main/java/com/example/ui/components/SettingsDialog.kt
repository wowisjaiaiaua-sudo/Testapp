package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GameViewModel

@Composable
fun SettingsDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val stats by viewModel.userStats.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(360.dp)
                .background(Color(0xFF0F1528), RoundedCornerShape(20.dp))
                .border(2.dp, Color(0xFF00F0FF), RoundedCornerShape(20.dp))
                .padding(24.dp)
                .testTag("settings_dialog"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SETTINGS",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            SettingToggleRow(
                label = "SOUND EFFECTS",
                checked = stats.sfxEnabled,
                onCheckedChange = { viewModel.toggleSfx() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingToggleRow(
                label = "MUSIC / SYNTH",
                checked = stats.musicEnabled,
                onCheckedChange = { viewModel.toggleMusic() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingToggleRow(
                label = "SCREEN SHAKE",
                checked = stats.screenShakeEnabled,
                onCheckedChange = { viewModel.toggleScreenShake() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingToggleRow(
                label = "HAPTIC FEEDBACK",
                checked = stats.hapticsEnabled,
                onCheckedChange = { viewModel.toggleHaptics() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF66)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("close_settings_button")
            ) {
                Text(
                    text = "CLOSE",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xFFE0E0E0),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF00FF66),
                uncheckedThumbColor = Color(0xFF888888),
                uncheckedTrackColor = Color(0xFF222838)
            )
        )
    }
}
