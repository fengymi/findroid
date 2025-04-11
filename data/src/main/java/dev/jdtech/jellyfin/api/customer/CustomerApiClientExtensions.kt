package dev.jdtech.jellyfin.api.customer

import org.jellyfin.sdk.api.client.ApiClient

val ApiClient.danmuApi: DanmuApi get() = getOrCreateApi { DanmuApi(it) }