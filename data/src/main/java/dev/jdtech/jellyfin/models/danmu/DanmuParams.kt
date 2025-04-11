package dev.jdtech.jellyfin.models.danmu

import kotlinx.serialization.Serializable

@Serializable
data class DanmuParams(
	val needSites:Collection<String>,
)
