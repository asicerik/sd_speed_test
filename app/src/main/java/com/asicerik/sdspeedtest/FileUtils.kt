package com.asicerik.sdspeedtest

import android.os.Environment
import java.io.File
import java.nio.file.Paths

const val DATA_FOLDER_NAME = "SdSpeedTest"

class FileUtils {
    companion object Root {
        fun getPath(): String {
            return File(Environment.getExternalStorageDirectory(), DATA_FOLDER_NAME).path
        }
    }

    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun createTestFile(rootFolder: String): File {
        val folder = File(rootFolder)
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                throw Exception("Could not create test folder")
            }
        }
        val path = Paths.get(rootFolder, "test.dat")
        val file = File(path.toString())
        if (file.exists()) {
            file.delete()
        }
        if (!file.createNewFile()) {
            throw Exception("Could not create test folder")
        }
        return file
    }
}
