package com.galaxytv.app

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.URL
import java.nio.charset.StandardCharsets
import java.text.Normalizer
import java.util.Locale
import java.util.UUID

internal data class ActivationResult(
    val status: Int,
    val message: String,
    val expire: String,
    val osd: String,
    val userAgent: String,
)

internal data class ServerPackage(val id: String, val name: String, val iconUrl: String = "")

internal data class ServerChannel(val id: String, val name: String, val streamUrl: String)

internal enum class MediaKind { Movie, Series }

internal data class MediaCategory(val id: String, val name: String, val kind: MediaKind)

internal data class CatalogItem(
    val id: String,
    val title: String,
    val posterUrl: String,
    val streamUrl: String,
    val kind: MediaKind,
)

internal data class MediaEpisode(val id: String, val title: String, val streamUrl: String)

internal data class PlayableStream(val title: String, val url: String)

internal object ActivationClient {
    suspend fun activate(context: Context, code: String): ActivationResult {
        val response = JSONObject(post(context, request(context, code, "active")))
        return ActivationResult(
            status = response.optInt("status", -1),
            message = response.optString("message", ""),
            expire = response.optString("expire", ""),
            osd = response.optString("osd", ""),
            userAgent = response.optString("user_agent", ""),
        )
    }

    suspend fun fetchPackages(context: Context, code: String): List<ServerPackage> {
        val response = JSONArray(post(context, request(context, code, "lite_packages")))
        return buildList {
            for (index in 0 until response.length()) {
                val item = response.optJSONObject(index) ?: continue
                val name = item.optString("pkg_name", "")
                val id = item.optString("id", "")
                if (id.isNotBlank() && name.isNotBlank()) {
                    add(ServerPackage(id, name, item.optString("pkg_icon", "")))
                }
            }
        }
    }

    suspend fun fetchSportsPackages(context: Context, code: String): List<ServerPackage> =
        fetchPackages(context, code).filter { matchesSportsPackage(it.name) }

    suspend fun fetchChannels(context: Context, code: String, packageId: String): List<ServerChannel> {
        val request = request(context, code, "lite_channels").put("pkg_id", packageId)
        val response = JSONArray(post(context, request))
        return buildList {
            for (index in 0 until response.length()) {
                val item = response.optJSONObject(index) ?: continue
                val name = item.optString("stream_display_name", "")
                val streamUrl = item.optString("stream_url", "")
                if (name.isNotBlank() && streamUrl.startsWith("http")) {
                    add(ServerChannel(item.optString("id", ""), name, streamUrl))
                }
            }
        }
    }

    suspend fun fetchCategories(context: Context, code: String, kind: MediaKind): List<MediaCategory> {
        val mode = if (kind == MediaKind.Movie) "movies_cat" else "series_cat"
        val response = JSONArray(post(context, request(context, code, mode)))
        return buildList {
            for (index in 0 until response.length()) {
                val parent = response.optJSONObject(index) ?: continue
                val subcategories = parent.optJSONArray("sub_cats")
                if (subcategories == null || subcategories.length() == 0) {
                    val id = parent.optString("cat_id", "")
                    val name = parent.optString("cat_name", "")
                    if (id.isNotBlank() && name.isNotBlank()) add(MediaCategory(id, name, kind))
                } else {
                    for (subIndex in 0 until subcategories.length()) {
                        val child = subcategories.optJSONObject(subIndex) ?: continue
                        val id = child.optString("sub_id", "")
                        val name = child.optString("sub_name", "")
                        if (id.isNotBlank() && name.isNotBlank()) add(MediaCategory(id, name, kind))
                    }
                }
            }
        }
    }

    suspend fun fetchCatalogItems(context: Context, code: String, category: MediaCategory): List<CatalogItem> {
        val mode = if (category.kind == MediaKind.Movie) "movies_list" else "series_list"
        val request = request(context, code, mode).put("catid", category.id)
        val response = JSONArray(post(context, request))
        return buildList {
            for (index in 0 until response.length()) {
                val item = response.optJSONObject(index) ?: continue
                val id = item.optString("id", "")
                val title = item.optString("title", "")
                if (id.isBlank() || title.isBlank()) continue
                add(
                    CatalogItem(
                        id = id,
                        title = title,
                        posterUrl = item.optString("icon_big", "").ifBlank { item.optString("icon", "") },
                        streamUrl = item.optString("stream_url", ""),
                        kind = category.kind,
                    ),
                )
            }
        }
    }

    suspend fun fetchSeriesEpisodes(context: Context, code: String, seriesId: String): List<MediaEpisode> {
        val request = request(context, code, "series_info").put("series_id", seriesId)
        val response = JSONArray(post(context, request))
        return buildList {
            for (index in 0 until response.length()) {
                val item = response.optJSONObject(index) ?: continue
                val streamUrl = item.optString("stream_url", "")
                if (!streamUrl.startsWith("http", ignoreCase = true)) continue
                val season = item.optString("season_num", "")
                val episode = item.optString("episode_num", "")
                val title = item.optString("episode_name", "").ifBlank { "S$season E$episode" }
                add(MediaEpisode("$season-$episode", title, streamUrl))
            }
        }
    }

    internal fun matchesSportsPackage(name: String): Boolean {
        val normalized = normalizeName(name)
        return "sport" in normalized || "رياض" in normalized
    }

    internal fun containsArabic(text: String): Boolean = text.any {
        it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' || it in '\u08A0'..'\u08FF'
    }

    internal fun matchesFamilyCategory(name: String, section: GalaxySection): Boolean {
        val normalized = normalizeName(name)
        return when (section) {
            GalaxySection.Kids -> listOf("kid", "child", "enfant", "jeun", "cartoon", "dessin", "طفل", "اطفال", "كرتون", "صغار")
                .any(normalized::contains)
            GalaxySection.Ramadan -> "ramadan" in normalized || "رمضان" in normalized
            else -> false
        }
    }

    private fun normalizeName(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(Regex("[\\u064B-\\u065F\\u0670\\u0640\\p{M}]"), "")
        .lowercase(Locale.ROOT)

    private fun request(context: Context, code: String, mode: String): JSONObject {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID).orEmpty()
        return JSONObject()
            .put("mode", mode)
            .put("code", code)
            .put("mac", getMacAddress())
            .put("sn", androidId)
            .put("chipid", "")
            .put("model", Build.MODEL.orEmpty())
            .put("firmware_ver", Build.VERSION.RELEASE.orEmpty())
    }

    private fun getMacAddress(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return ""

        return listOf("wlan0", "eth0").firstNotNullOfOrNull { interfaceName ->
            runCatching {
                NetworkInterface.getByName(interfaceName)?.hardwareAddress
                    ?.takeIf { it.size == 6 }
                    ?.joinToString(":") { byte -> String.format(Locale.US, "%02X", byte.toInt() and 0xFF) }
                    ?.takeUnless { it == "02:00:00:00:00:00" }
            }.getOrNull()
        }.orEmpty()
    }

    private suspend fun post(context: Context, request: JSONObject): String = withContext(Dispatchers.IO) {
        val payload = request.toString().toByteArray(StandardCharsets.UTF_8)
        val xorKey = BuildConfig.ACTIVATION_XOR_KEY.toByteArray(StandardCharsets.UTF_8)
        require(xorKey.isNotEmpty())
        val obfuscated = ByteArray(payload.size) { index ->
            (payload[index].toInt() xor xorKey[index % xorKey.size].toInt()).toByte()
        }
        val encodedPayload = Base64.encodeToString(obfuscated, Base64.NO_WRAP)
        val boundary = "GalaxyTV-${UUID.randomUUID()}"
        val connection = (URL(BuildConfig.ACTIVATION_ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 30_000
            doOutput = true
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
            setRequestProperty("User-Agent", "REV ${Build.MODEL.orEmpty()} ${Build.VERSION.RELEASE.orEmpty()}")
        }

        try {
            val multipartBody = buildString {
                append("--$boundary\r\n")
                append("Content-Disposition: form-data; name=\"json\"\r\n\r\n")
                append(encodedPayload)
                append("\r\n--$boundary--\r\n")
            }
            connection.outputStream.use { output ->
                output.write(multipartBody.toByteArray(StandardCharsets.UTF_8))
            }
            val responseCode = connection.responseCode
            val responseStream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val encryptedResponse = responseStream?.use { it.readBytes() } ?: byteArrayOf()
            decodeResponse(encryptedResponse, xorKey)
        } finally {
            connection.disconnect()
        }
    }

    private fun decodeResponse(encryptedResponse: ByteArray, xorKey: ByteArray): String {
        require(encryptedResponse.isNotEmpty()) { "Empty response from activation server" }
        require(xorKey.isNotEmpty()) { "Empty XOR key" }
        val decodedBytes = ByteArray(encryptedResponse.size) { index ->
            (encryptedResponse[index].toInt() xor xorKey[index % xorKey.size].toInt()).toByte()
        }
        return String(decodedBytes, StandardCharsets.UTF_8)
    }

}
