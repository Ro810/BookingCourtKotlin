package com.example.bookingcourt.data.remote.dto

import android.util.Log
import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.google.gson.reflect.TypeToken
import java.io.IOException

/**
 * TypeAdapterFactory để tự động xử lý các field time (startTime, endTime, expireTime)
 * khi backend trả về array số nguyên thay vì string
 */
class TimeFieldTypeAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        // Chỉ xử lý các class có chứa time fields
        val rawType = type.rawType
        if (rawType == CreateBookingResponseDto::class.java ||
            rawType == BookingDetailResponseDto::class.java) {

            // Lấy default adapter
            val delegate = gson.getDelegateAdapter(this, type)

            return object : TypeAdapter<T>() {
                @Throws(IOException::class)
                override fun write(out: JsonWriter, value: T?) {
                    delegate.write(out, value)
                }

                @Throws(IOException::class)
                override fun read(`in`: JsonReader): T? {
                    return try {
                        // Đọc toàn bộ JSON thành JsonElement
                        val jsonElement = JsonParser.parseReader(`in`)

                        if (jsonElement.isJsonObject) {
                            val jsonObject = jsonElement.asJsonObject

                            // Xử lý các time fields
                            fixTimeField(jsonObject, "startTime")
                            fixTimeField(jsonObject, "endTime")
                            fixTimeField(jsonObject, "expireTime")

                            // Parse lại với JsonObject đã được fix
                            delegate.fromJsonTree(jsonObject)
                        } else {
                            delegate.fromJsonTree(jsonElement)
                        }
                    } catch (e: JsonIOException) {
                        Log.e("TimeFieldFactory", "❌ JSON IO Error", e)
                        throw e
                    } catch (e: JsonSyntaxException) {
                        Log.e("TimeFieldFactory", "❌ JSON Syntax Error", e)
                        throw e
                    } catch (e: Exception) {
                        Log.e("TimeFieldFactory", "❌ Unexpected error parsing JSON", e)
                        throw e
                    }
                }

                private fun fixTimeField(jsonObject: JsonObject, fieldName: String) {
                    val field = jsonObject.get(fieldName) ?: return

                    if (field.isJsonArray) {
                        val array = field.asJsonArray
                        Log.d("TimeFieldFactory", "⚠️ Found array for $fieldName with size: ${array.size()}")

                        if (array.size() >= 3) {
                            try {
                                // ✅ Chuyển đổi array số nguyên thành ISO-8601 string
                                // Array format: [year, month, day, hour?, minute?, second?, nano?]
                                val year = array[0].asInt
                                val month = array[1].asInt
                                val day = array[2].asInt
                                val hour = if (array.size() > 3) array[3].asInt else 0
                                val minute = if (array.size() > 4) array[4].asInt else 0
                                val second = if (array.size() > 5) array[5].asInt else 0

                                val isoString = String.format(
                                    "%04d-%02d-%02dT%02d:%02d:%02d",
                                    year, month, day, hour, minute, second
                                )

                                // Thay thế array bằng string
                                jsonObject.addProperty(fieldName, isoString)

                                Log.d("TimeFieldFactory", "✅ Fixed $fieldName: array $array -> string '$isoString'")
                            } catch (e: Exception) {
                                Log.e("TimeFieldFactory", "❌ Error converting $fieldName array to string", e)
                                jsonObject.add(fieldName, JsonNull.INSTANCE)
                            }
                        } else {
                            Log.e("TimeFieldFactory", "❌ Array for $fieldName too short (size: ${array.size()})")
                            jsonObject.add(fieldName, JsonNull.INSTANCE)
                        }
                    } else if (field.isJsonPrimitive && field.asJsonPrimitive.isString) {
                        Log.d("TimeFieldFactory", "✅ $fieldName is already a string: ${field.asString}")
                    }
                }
            } as TypeAdapter<T>
        }
        return null
    }
}
