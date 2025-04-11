package dev.jdtech.jellyfin.api.customer

import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import dev.jdtech.jellyfin.models.danmu.DanmuParams
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.Response
import org.jellyfin.sdk.api.client.extensions.get
import org.jellyfin.sdk.api.operations.Api
import org.jellyfin.sdk.model.UUID

//import org.jellyfin.apiclient.interaction.ApiClient;
// 注入
// import static org.koin.java.KoinJavaComponent.inject;
// org.jellyfin.sdk.api.operations.VideosApi

class DanmuApi(private val api: ApiClient) : Api {

	fun getDanmuXmlFileById(itemId: UUID, sites: Collection<String>): Response<ByteReadChannel> {
		return runBlocking{getSDanmuXmlFileById(itemId, sites)}
	}

	suspend fun getSDanmuXmlFileById(itemId: UUID, sites: Collection<String>): Response<ByteReadChannel> {
		// org.jellyfin.sdk.api.operations.VideosApi
		val body = DanmuParams(needSites = sites)

		val response = api.get<ByteReadChannel>("/api/danmu/$itemId/raw", requestBody = body)
		return response;
	}
}
