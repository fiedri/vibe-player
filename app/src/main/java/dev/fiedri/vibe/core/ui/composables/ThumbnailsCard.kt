package dev.fiedri.vibe.core.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import dev.fiedri.vibe.core.ui.theme.VibeTheme
import kotlin.math.max

@Composable
fun ThumbnailCard(
    title: String,
    subtitle: String? = null,
    img: Any?,
    modifier: Modifier = Modifier
){

        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)){
                VibeImage(
                    resource = img,
                    contentDescription = "Cover de ${title}",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                title,
                maxLines = 1,
                style = VibeTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis
            )

            if (!subtitle.isNullOrEmpty()){
                Text(
                    subtitle,
                    maxLines = 1,
                    color = VibeTheme.colors.mutedForeground,
                    style = VibeTheme.typography.caption,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }

}
