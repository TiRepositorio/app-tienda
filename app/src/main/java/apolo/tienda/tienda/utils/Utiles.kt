// apolo.tienda.tienda.utils.Utils.kt
package apolo.tienda.tienda.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.OffsetDateTime

fun formatDateTime(dateTimeString: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm 'hs'")
        val dateTime = LocalDateTime.parse(dateTimeString, inputFormatter)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        dateTimeString
    }
}


object DateUtils {

    private val ZONA_APP: ZoneId = ZoneId.of("America/Asuncion")

    fun parseDateFromApi(dateString: String): ZonedDateTime {
        val offsetDateTime = OffsetDateTime.parse(dateString)
        return offsetDateTime.atZoneSameInstant(ZONA_APP)
    }

    fun parseDateFromLocal(dateString: String): ZonedDateTime {
        val localDateTime = LocalDateTime.parse(dateString) // porque es un ISO_LOCAL_DATE_TIME
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZONA_APP)
    }

    fun formatDateToShow(date: ZonedDateTime, pattern: String = "dd/MM/yyyy HH:mm"): String {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return date.format(formatter)
    }
}