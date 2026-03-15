package com.christelldev.easyreferplus.util

import android.content.Context
import android.provider.ContactsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DeviceContact(
    val name: String,
    val phone: String // normalized 10-digit Ecuadorian format
)

object ContactsHelper {

    suspend fun loadContacts(context: Context): List<DeviceContact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<DeviceContact>()
        val seen = mutableSetOf<String>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null, null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val name = cursor.getString(nameIdx)?.trim() ?: continue
                val raw = cursor.getString(phoneIdx) ?: continue
                val normalized = normalizePhone(raw) ?: continue
                if (seen.add(normalized)) {
                    contacts.add(DeviceContact(name = name, phone = normalized))
                }
            }
        }
        contacts
    }

    fun filter(contacts: List<DeviceContact>, query: String): List<DeviceContact> {
        if (query.isBlank()) return contacts
        val q = query.trim().lowercase()
        return contacts.filter {
            it.name.lowercase().contains(q) || it.phone.contains(q)
        }
    }

    fun normalizePhone(raw: String): String? {
        val digits = raw.filter { it.isDigit() }
        return when {
            digits.startsWith("593") && digits.length == 12 -> "0${digits.substring(3)}"
            digits.startsWith("0") && digits.length == 10 -> digits
            digits.length == 9 && digits.startsWith("9") -> "0$digits"
            else -> null
        }
    }
}
