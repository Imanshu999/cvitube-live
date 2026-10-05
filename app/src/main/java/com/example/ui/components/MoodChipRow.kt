package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TipsAndUpdates
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkChipBg
import com.example.ui.theme.DarkChipSelectedBg
import com.example.ui.theme.MoodChill
import com.example.ui.theme.MoodFocus
import com.example.ui.theme.MoodLearn
import com.example.ui.theme.MoodMusic
import com.example.ui.theme.MoodNews
import com.example.ui.theme.YouTubeRed

data class MoodItem(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color
)

val MOOD_ITEMS = listOf(
    MoodItem("all", "All", Icons.Rounded.AutoAwesome, YouTubeRed),
    MoodItem("learn", "Learn", Icons.Rounded.TipsAndUpdates, MoodLearn),
    MoodItem("music", "Music", Icons.Rounded.Headphones, MoodMusic),
    MoodItem("chill", "Chill", Icons.Rounded.Spa, MoodChill),
    MoodItem("news", "News", Icons.Rounded.Newspaper, MoodNews),
    MoodItem("focus", "Focus", Icons.Rounded.SelfImprovement, MoodFocus)
)

@Composable
fun MoodChipRow(
    selectedMood: String,
    onMoodSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MOOD_ITEMS.forEach { item ->
            val isSelected = selectedMood == item.id
            val bgColor = if (isSelected) DarkChipSelectedBg else DarkChipBg
            val textColor = if (isSelected) Color(0xFF0F0F0F) else Color(0xFFF1F1F1)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onMoodSelected(item.id) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("mood_chip_${item.id}"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.name,
                        tint = if (isSelected) Color(0xFF0F0F0F) else item.color,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = textColor
                        )
                    )
                }
            }
        }
    }
}
