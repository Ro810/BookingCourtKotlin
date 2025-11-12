package com.example.bookingcourt.data.remote.dto

import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * Custom deserializer để xử lý trường hợp backend trả về time field là string hoặc array
 * Nếu là array, lấy phần tử đầu tiên
 * Nếu là string, dùng trực tiếp
 */
class TimeStringDeserializer : JsonDeserializer<String?> {
    companion object {
        private const val TAG = "TimeStringDeserializer"
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): String? {
        if (json == null || json.isJsonNull) {
            Log.d(TAG, "⚠️ JSON element is null")
            return null
        }

        return when {
            // Nếu là string, trả về trực tiếp
            json.isJsonPrimitive && json.asJsonPrimitive.isString -> {
                val result = json.asString
                Log.d(TAG, "✅ Parsed as string: $result")
                result
            }
            // ✅ FIX: Nếu là array, xử lý cả array string và array số
            json.isJsonArray -> {
                val array = json.asJsonArray
                Log.d(TAG, "⚠️ Found array with size: ${array.size()}")

                if (array.size() == 0) {
                    Log.e(TAG, "❌ Array is empty")
                    return null
                }

                val firstElement = array[0]

                // ✅ Nếu phần tử đầu là string, lấy trực tiếp
                if (firstElement.isJsonPrimitive && firstElement.asJsonPrimitive.isString) {
                    val result = firstElement.asString
                    Log.d(TAG, "✅ Extracted string from array: $result")
                    return result
                }

                // ✅ Nếu phần tử đầu là số, đây là array format [year,month,day,hour,minute] hoặc [year,month,day,hour,minute,second,nano]
                if (firstElement.isJsonPrimitive && firstElement.asJsonPrimitive.isNumber) {
                    try {
                        when (array.size()) {
                            5 -> {
                                // Format: [year, month, day, hour, minute]
                                val year = array[0].asInt
                                val month = array[1].asInt
                                val day = array[2].asInt
                                val hour = array[3].asInt
                                val minute = array[4].asInt
                                val result = String.format("%04d-%02d-%02dT%02d:%02d:00", year, month, day, hour, minute)
                                Log.d(TAG, "✅ Converted array [5] to ISO string: $result")
                                return result
                            }
                            6 -> {
                                // Format: [year, month, day, hour, minute, second]
                                val year = array[0].asInt
                                val month = array[1].asInt
                                val day = array[2].asInt
                                val hour = array[3].asInt
                                val minute = array[4].asInt
                                val second = array[5].asInt
                                val result = String.format("%04d-%02d-%02dT%02d:%02d:%02d", year, month, day, hour, minute, second)
                                Log.d(TAG, "✅ Converted array [6] to ISO string: $result")
                                return result
                            }
                            7 -> {
                                // Format: [year, month, day, hour, minute, second, nano]
                                val year = array[0].asInt
                                val month = array[1].asInt
                                val day = array[2].asInt
                                val hour = array[3].asInt
                                val minute = array[4].asInt
                                val second = array[5].asInt
                                // Bỏ qua nano seconds
                                val result = String.format("%04d-%02d-%02dT%02d:%02d:%02d", year, month, day, hour, minute, second)
                                Log.d(TAG, "✅ Converted array [7] to ISO string: $result")
                                return result
                            }
                            else -> {
                                Log.e(TAG, "❌ Unexpected array size: ${array.size()}")
                                return null
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error converting array to ISO string: ${e.message}")
                        return null
                    }
                }

                Log.e(TAG, "❌ First element is neither string nor number: $firstElement")
                null
            }
            // Các trường hợp khác
            else -> {
                Log.e(TAG, "❌ Unexpected JSON type: ${json.javaClass.simpleName}, value: $json")
                null
            }
        }
    }
}
