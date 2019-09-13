package com.asicerik.sdspeedtest

import android.os.Environment
import android.os.StatFs
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

    val internalStorageSizeGB: Double
        get() {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return (totalBlocks * blockSize)/1_000_000_000.0
    }

    val externalStorageSizeGB: Double
        get() {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return (totalBlocks * blockSize)/1_000_000_000.0
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
