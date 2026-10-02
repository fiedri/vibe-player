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
import dev.fiedri.vibe.R


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
    // MediaStore artwork URIs are non-null even when no art exists, so the
    // caller ?: fallback never fires: a failed load must fall back here.
    val fallback = painterResource(R.drawable.default_cover)
    AsyncImage(
        model = imageRequest,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale ?: ContentScale.Crop,
        placeholder = placeholder ?: fallback,
        error = error ?: fallback,
        colorFilter = colorFilter
        )
}