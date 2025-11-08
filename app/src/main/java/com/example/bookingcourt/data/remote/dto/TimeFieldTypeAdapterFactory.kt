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
 * khi backend trả về array thay vì string
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
                    // Đọc JSON thành string, parse thành JsonObject, fix time fields, rồi parse lại
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
                        // Nếu có lỗi syntax, thử parse lại với delegate để có error message rõ ràng
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
                        if (array.size() > 0) {
                            val firstElement = array[0]
                            if (firstElement.isJsonPrimitive && firstElement.asJsonPrimitive.isString) {
                                // Thay thế array bằng string
                                jsonObject.add(fieldName, firstElement)
                                Log.d("TimeFieldFactory", "✅ Fixed $fieldName: array -> string (${firstElement.asString})")
                            } else {
                                Log.e("TimeFieldFactory", "❌ First element of $fieldName array is not a string: $firstElement")
                            }
                        } else {
                            Log.e("TimeFieldFactory", "❌ Array for $fieldName is empty")
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

