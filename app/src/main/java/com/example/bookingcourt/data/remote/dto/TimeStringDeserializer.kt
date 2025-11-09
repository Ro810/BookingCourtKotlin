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
            // Nếu là array
            json.isJsonArray -> {
                val array = json.asJsonArray
                Log.d(TAG, "⚠️ Found array with size: ${array.size()}")
                if (array.size() > 0) {
                    val firstElement = array[0]
                    Log.d(TAG, "  First element type: ${firstElement.javaClass.simpleName}")

                    // Case 1: Array có phần tử đầu là string (format cũ: ["2025-11-06", "12:30:00"])
                    if (firstElement.isJsonPrimitive && firstElement.asJsonPrimitive.isString) {
                        val result = firstElement.asString
                        Log.d(TAG, "✅ Extracted string from array: $result")
                        result
                    }
                    // Case 2: Array toàn số (format mới: [2025, 11, 6, 12, 30] hoặc [2025, 11, 6, 12, 30, 0])
                    else if (firstElement.isJsonPrimitive && firstElement.asJsonPrimitive.isNumber) {
                        try {
                            if (array.size() < 3) {
                                Log.e(TAG, "❌ Array too short, need at least 3 elements (year, month, day)")
                                return null
                            }

                            val year = array[0].asInt
                            val month = array[1].asInt
                            val day = array[2].asInt
                            val hour = if (array.size() > 3) array[3].asInt else 0
                            val minute = if (array.size() > 4) array[4].asInt else 0
                            val second = if (array.size() > 5) array[5].asInt else 0

                            val result = String.format("%04d-%02d-%02dT%02d:%02d:%02d", year, month, day, hour, minute, second)
                            Log.d(TAG, "✅ Converted number array to datetime: $result")
                            result
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Failed to convert number array: ${e.message}")
                            null
                        }
                    } else {
                        Log.e(TAG, "❌ First element is not a string or number: $firstElement")
                        null
                    }
                } else {
                    Log.e(TAG, "❌ Array is empty")
                    null
                }
            }
            // Các trường hợp khác
            else -> {
                Log.e(TAG, "❌ Unexpected JSON type: ${json.javaClass.simpleName}, value: $json")
                null
            }
        }
    }
}

