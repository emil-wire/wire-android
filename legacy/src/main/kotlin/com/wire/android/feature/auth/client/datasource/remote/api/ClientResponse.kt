package com.wire.android.feature.auth.client.datasource.remote.api

import com.google.gson.annotations.SerializedName

data class ClientResponse(
    @SerializedName("id") val id: String,
    @SerializedName("cookie") val refreshToken: String,
    @SerializedName("time") val registrationTime: String,
    @SerializedName("location") val location: LocationResponse?,
    @SerializedName("address") val ipAddress: String?,
    @SerializedName("model") val model: String?,
    @SerializedName("type") val deviceType: String,
    @SerializedName("class") val deviceClass: String,
    @SerializedName("label") val label: String?
)

data class LocationResponse(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val latitude: String,
    @SerializedName("lon") val longitude: String
)

data class ClientsOfUsersResponse(
    @SerializedName("qualified_user_map") val qualifiedMap: Map<String, Map<String, List<SimpleClientResponse>>>
)

data class SimpleClientResponse(
    @SerializedName("id") val id: String,
    @SerializedName("class") val deviceClass: String
)

typealias RemainingPreKeysResponse = List<Int>