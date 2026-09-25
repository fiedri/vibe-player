package dev.fiedri.vibe.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.fiedri.vibe.ui.theme.VibeTheme

fun Modifier.borderBotton(
    color: Color,
    width: Dp = 4.dp
): Modifier = this.drawBehind {
    val strokeWidth = width.toPx()
    drawLine(
        color = color,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = strokeWidth
    )
}

@Composable
fun VibeToBar(
    tabs: List<String>,
    activeTab: String,
    onTabSelected: (String) -> Unit
){
    val borderColor = VibeTheme.colors.muted
    val primaryColor = VibeTheme.colors.primary

    val tabPositions = remember { mutableStateMapOf<String, Float>() }

    val tabWidths = remember { mutableStateMapOf<String, Float>() }

    val currentX = tabPositions[activeTab] ?: 0f
    val currentWidth = tabWidths[activeTab] ?: 0f
    val animatedX by animateFloatAsState(targetValue = currentX, label = "animatedX")
    val animatedWidth by animateFloatAsState(targetValue = currentWidth, label = "animatedWidth")
    Column(
        modifier = Modifier.fillMaxWidth().borderBotton(borderColor, 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 5.dp, end = 5.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(Icons.Default.Menu, contentDescription = "Abrir menú", tint = VibeTheme.colors.foreground,
                    modifier = Modifier.size(30.dp))
            }
            Text(text = "VIBE", color = VibeTheme.colors.foreground, fontSize = 24.sp)
            Row() {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = VibeTheme.colors.foreground,
                        modifier = Modifier.size(30.dp))
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Buscar", tint = VibeTheme.colors.foreground,
                        modifier = Modifier.size(25.dp))
                }
            }
        }
    Box(modifier = Modifier.fillMaxWidth().drawBehind {

        val strokeWidth = 2.dp.toPx()
        drawLine(
            color = borderColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth
        )
    }){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()), // Permite hacer scroll horizontal si no caben
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isActive = tab == activeTab

                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 8.dp)
                        .onGloballyPositioned { coordinates ->
                            tabPositions[tab] = coordinates.positionInParent().x
                            tabWidths[tab] = coordinates.size.width.toFloat()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        color = if (isActive) VibeTheme.colors.foreground else VibeTheme.colors.muted,
                        style = VibeTheme.typography.bodyLarge
                    )

                }
            }
        }
        if (animatedWidth > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = with(androidx.compose.ui.platform.LocalDensity.current) { animatedX.toDp() })
                    .width(with(androidx.compose.ui.platform.LocalDensity.current) { animatedWidth.toDp() })
                    .height(2.dp)
                    .background(primaryColor)
            )
        }
    }

    }
}
