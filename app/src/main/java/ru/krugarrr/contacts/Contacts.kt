package ru.krugarrr.contacts

import android.content.Context
import android.provider.ContactsContract
import java.io.Serializable

data class Contact(val name: String, val phone: String) : Serializable

fun loadContacts(context: Context): ArrayList<Contact> {
    val result = ArrayList<Contact>()
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )
    if (cursor == null) return result

    val nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
    val phoneIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
    while (cursor.moveToNext()) {
        val name = cursor.getString(nameIndex) ?: ""
        val phone = cursor.getString(phoneIndex) ?: ""
        result.add(Contact(name, phone))
    }
    cursor.close()
    return result
}
