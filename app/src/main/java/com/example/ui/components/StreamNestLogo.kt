package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.YouTubeRed

@Composable
fun StreamNestLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    showText: Boolean = true
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val w = this.size.width
                val h = this.size.height
                val left = w * 0.06f
                val top = h * 0.18f
                val right = w * 0.94f
                val bottom = h * 0.82f
                drawRoundRect(
                    color = YouTubeRed,
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.18f, w * 0.18f)
                )
                val play = Path().apply {
                    moveTo(w * 0.44f, h * 0.35f)
                    lineTo(w * 0.44f, h * 0.65f)
                    lineTo(w * 0.69f, h * 0.50f)
                    close()
                }
                drawPath(play, color = Color.White, style = Fill)
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                Text(
                    text = "Cvi",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "Tube",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        color = YouTubeRed
                    )
                )
            }
        }
    }
}
