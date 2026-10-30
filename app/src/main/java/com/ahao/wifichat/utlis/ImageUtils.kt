package com.ahao.wifichat.utlis

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import java.io.File
import java.io.FileOutputStream

/**
 * 图片/视频/文件路径工具。
 * 仅保留实际使用的方法；Android 10+ 分区存储下无法直接拿到真实路径时，会复制到应用目录。
 */
object ImageUtils {

    const val REQUEST_CODE_GETIMAGE_BYSDCARD = 0
    const val REQUEST_CODE_GETIMAGE_BYCAMERA = 1
    const val REQUEST_CODE_GETIMAGE_BYCROP = 2

    /** 将 content/file Uri 转为本地路径；无法直接获取时复制一份到应用目录 */
    fun getRealFilePath(context: Context, uri: Uri?): String? {
        uri ?: return null
        return when (uri.scheme) {
            null, ContentResolver.SCHEME_FILE -> uri.path
            ContentResolver.SCHEME_CONTENT -> queryDataColumn(context, uri, MediaStore.Images.ImageColumns.DATA)
                ?: copyToAppStorage(context, uri)
            else -> null
        }
    }

    /** 处理 Document Uri、下载目录、媒体库、content 与 file 等多种来源 */
    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            when {
                isExternalStorageDocument(uri) -> {
                    val split = DocumentsContract.getDocumentId(uri).split(":")
                    if ("primary".equals(split[0], ignoreCase = true)) {
                        return "${Environment.getExternalStorageDirectory()}/${split[1]}"
                    }
                }
                isDownloadsDocument(uri) -> {
                    val id = DocumentsContract.getDocumentId(uri).toLong()
                    val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), id)
                    return getDataColumn(context, contentUri, null, null) ?: copyToAppStorage(context, uri)
                }
                isMediaDocument(uri) -> {
                    val split = DocumentsContract.getDocumentId(uri).split(":")
                    val contentUri = when (split[0]) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }
                    return getDataColumn(context, contentUri, "_id=?", arrayOf(split[1])) ?: copyToAppStorage(context, uri)
                }
            }
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null) ?: copyToAppStorage(context, uri)
        } else if (ContentResolver.SCHEME_FILE.equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /** 取视频第一帧并缩放到指定尺寸 */
    fun getVideoBitmap(data: String?, width: Int, height: Int): Bitmap? {
        if (TextUtils.isEmpty(data)) return null
        return runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(data)
                val frame = retriever.getFrameAtTime(-1) ?: return null
                Bitmap.createScaledBitmap(frame, width, height, false)
            } finally {
                retriever.release() // AutoCloseable.close() 仅 API 29+，这里用 release()
            }
        }.onFailure { it.printStackTrace() }.getOrNull()
    }

    /** 将 content Uri 的内容复制到应用私有目录，返回新文件路径 */
    fun copyToAppStorage(context: Context, uri: Uri): String? = runCatching {
        val name = queryDisplayName(context, uri) ?: "file_${System.currentTimeMillis()}"
        val target = File(context.getExternalFilesDir("chat_import") ?: context.filesDir, name)
        target.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(target).use { output -> input.copyTo(output) }
        } ?: return null
        target.absolutePath
    }.onFailure { it.printStackTrace() }.getOrNull()

    private fun queryDisplayName(context: Context, uri: Uri): String? =
        context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null)?.use {
            if (it.moveToFirst()) it.getString(0) else null
        }

    private fun queryDataColumn(context: Context, uri: Uri, column: String): String? =
        context.contentResolver.query(uri, arrayOf(column), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getColumnIndex(column).takeIf { it > -1 }?.let(cursor::getString) else null
        }

    private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String? {
        uri ?: return null
        return runCatching {
            context.contentResolver.query(uri, arrayOf("_data"), selection, selectionArgs, null)?.use { cursor: Cursor ->
                if (cursor.moveToFirst()) cursor.getString(cursor.getColumnIndexOrThrow("_data")) else null
            }
        }.getOrNull()
    }

    private fun isExternalStorageDocument(uri: Uri) = "com.android.externalstorage.documents" == uri.authority
    private fun isDownloadsDocument(uri: Uri) = "com.android.providers.downloads.documents" == uri.authority
    private fun isMediaDocument(uri: Uri) = "com.android.providers.media.documents" == uri.authority
}
