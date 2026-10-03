package com.example.f329723_app.data.provider

import android.net.Uri

object PrivateNoteContract {
    const val AUTHORITY = "com.example.f329723_app.privatenotes"
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/notes")
}
