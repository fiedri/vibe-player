package dev.fiedri.vibe.core.ui.composables

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest


@Composable
fun VibeImage(
    resource: Any?,
    contentDescription: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    contentScale: ContentScale? = null,
    placeholder: Painter? = null,
    error: Painter? = null,
    colorFilter: ColorFilter? = null
){
    val context = LocalContext.current


    val imageRequest = ImageRequest.Builder(context)
        .data(resource)
        .crossfade(true)
        .build()
    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale ?: ContentScale.Crop,
        placeholder = placeholder,
        error = error,
        colorFilter = colorFilter
        )
}