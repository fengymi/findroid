package dev.jdtech.jellyfin.presentation.film.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.jdtech.jellyfin.core.presentation.dummy.dummyPerson
import dev.jdtech.jellyfin.models.FindroidPerson
import dev.jdtech.jellyfin.presentation.theme.FindroidTheme
import dev.jdtech.jellyfin.presentation.theme.spacings

@Composable
fun PersonItem(
    person: FindroidPerson,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(110.dp),
    ) {
        Box {
        }
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(person.image.uri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(
                    MaterialTheme.colorScheme.surfaceContainer,
                )
                .height(160.dp),
            contentScale = ContentScale.Crop,
        )
        Spacer(Modifier.height(MaterialTheme.spacings.extraSmall))
        Text(
            text = person.name,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = person.role,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun PersonItemPreview() {
    FindroidTheme {
        PersonItem(
            person = dummyPerson,
        )
    }
}
