package com.nexosolar.android.data.remote

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Adapter personalizado para serializar y deserializar [LocalDate] con Gson.
 *
 * Convierte fechas entre [LocalDate] y JSON usando el formato `dd/MM/yyyy`
 * (formato europeo esperado por la API).
 *
 * Se registra en el [com.google.gson.GsonBuilder] para conversión automática:
 * ```
 * GsonBuilder()
 *     .registerTypeAdapter(LocalDate::class.java, LocalDateTypeAdapter)
 *     .create()
 * ```
 */
object LocalDateTypeAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

    /**
     * Formato de fecha utilizado: dd/MM/yyyy (formato europeo).
     */
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    /**
     * Serializa un [LocalDate] a JSON.
     *
     * @param date Fecha a serializar
     * @param typeOfSrc Tipo del objeto fuente (ignorado)
     * @param context Contexto de serialización de Gson (ignorado)
     * @return [JsonPrimitive] con la fecha formateada como `dd/MM/yyyy`
     */
    override fun serialize(
        date: LocalDate,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        return JsonPrimitive(date.format(formatter))
    }

    /**
     * Deserializa una cadena JSON a [LocalDate].
     *
     * @param json Elemento JSON que contiene la fecha como cadena
     * @param typeOfT Tipo del objeto destino (ignorado)
     * @param context Contexto de deserialización de Gson (ignorado)
     * @return [LocalDate] parseado desde el formato `dd/MM/yyyy`
     * @throws JsonParseException Si la cadena no cumple el formato esperado
     */
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): LocalDate {
        return LocalDate.parse(json.getAsString(), formatter)
    }
}
