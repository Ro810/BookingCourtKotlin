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
            // Nếu là array, lấy phần tử đầu tiên
            json.isJsonArray -> {
                val array = json.asJsonArray
                Log.d(TAG, "⚠️ Found array with size: ${array.size()}")
                if (array.size() > 0) {
                    val firstElement = array[0]
                    Log.d(TAG, "  First element type: ${firstElement.javaClass.simpleName}")
                    if (firstElement.isJsonPrimitive && firstElement.asJsonPrimitive.isString) {
                        val result = firstElement.asString
                        Log.d(TAG, "✅ Extracted string from array: $result")
                        result
                    } else {
                        Log.e(TAG, "❌ First element is not a string: $firstElement")
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

