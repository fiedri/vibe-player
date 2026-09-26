package dev.fiedri.vibe.core.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.fiedri.vibe.R
import dev.fiedri.vibe.core.ui.theme.VibeTheme

@Composable
fun ThumbnailCard(title: String, subtitle: String, img: Int = R.drawable.default_artist){

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)){
                Image(
                    painter = painterResource(id = img),
                    contentDescription = "Cover de ${title}",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                title,
                style = VibeTheme.typography.titleLarge
            )

            if(!subtitle.isEmpty()){
                Text(
                    subtitle,
                    color = VibeTheme.colors.mutedForeground,
                    style = VibeTheme.typography.caption
                )
            }

        }

}