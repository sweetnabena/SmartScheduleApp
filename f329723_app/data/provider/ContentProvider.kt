package com.example.f329723_app.data.provider

import android.content.*
import android.database.Cursor
import android.net.Uri
import androidx.room.Room
import com.example.f329723_app.data.database.AppDatabase
import com.example.f329723_app.data.model.PrivateNote

// ContentProvider that enables other apps or components to access private notes
class PrivateNoteProvider : ContentProvider() {

    companion object {
        // Content authority and URI to access notes via the provider
        const val AUTHORITY = "com.example.f329723_app.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/notes")
    }

    // Reference to the Room database
    private lateinit var db: AppDatabase

    // Called when the provider is first created. Initializes the database instance.
    override fun onCreate(): Boolean {
        db = Room.databaseBuilder(
            context!!,
            AppDatabase::class.java,
            "app_db"
        ).build()
        return true
    }

    // Handles query requests from clients. Returns a Cursor over the result set.
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?,
                       selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        val cursor = db.privateNoteDao().getAllNotesCursor()
        cursor.setNotificationUri(context?.contentResolver, uri) // Notify observers of data changes
        return cursor
    }

    // Handles requests to insert a new row. Returns the URI for the newly inserted item.
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val content = values?.getAsString("content") ?: return null // Retrieve note content
        val id = db.privateNoteDao().insertRaw(PrivateNote(content = content)) // Insert note into database
        context?.contentResolver?.notifyChange(uri, null) // Notify observers
        return ContentUris.withAppendedId(uri, id) // Return URI with appended ID
    }

    // Handles requests to delete one or more rows. Returns the number of rows deleted.
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val id = ContentUris.parseId(uri) // Extract note ID from URI
        return db.privateNoteDao().deleteById(id) // Delete note by ID
    }

    // Update method is not implemented. Returns 0.
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0 // optional
    }

    // Returns the MIME type of the data at the given URI.
    override fun getType(uri: Uri): String? = "vnd.android.cursor.dir/vnd.$AUTHORITY.notes"
}
