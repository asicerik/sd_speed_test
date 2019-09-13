package com.asicerik.sdspeedtest

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.RandomAccessFile
import java.lang.Exception
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    val fileUtils = FileUtils()

    companion object {
        const val READ_STORAGE_PERMISSION_CODE = 0
        const val minTestIntervalMs = 5000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (hasPermissions()) {
            init()
        } else {
            requestPermission()
        }
    }

    private fun init() {
        textViewStatus.text = "Initializing..."
        runAll()
    }

    private fun runAll() {
        GlobalScope.async {
            runExternal()
            runInternall()
            runOnUiThread {
                textViewStatus.text = "Tests Complete"
            }
        }
    }

    private fun runExternal() {
        if (!fileUtils.isExternalStorageReadable() || !fileUtils.isExternalStorageWritable()) {
            Toast.makeText(this, "External storage is not accessible", Toast.LENGTH_LONG)
                .show()
            return
        }
        val table = tableLeft
        runLoops(table, FileUtils.getPath())
    }

    private fun runInternall() {
        val table = tableRight
        runLoops(table, filesDir.absolutePath)
    }

    private fun runLoops(table: TableLayout, rootFolder: String) {
        for (i in 1..3) {
            val row = (table[i] as TableRow)
            val size = when (i) {
                1 -> 10_000
                2 -> 100_000
                else -> 1_000_000
            }
            textViewStatus.text = "Running test ${size / 1000} KB Test"
            runOnUiThread {
                if (size < 1_000_000) {
                    (row[0] as TextView).text = "${size / 1000}KB"
                } else {
                    (row[0] as TextView).text = "${size / 1_000_000}MB"
                }
                (row[1] as TextView).text = "-"
                (row[2] as TextView).text = "-"
            }
            var res = runWriteTest(rootFolder, size)
            runOnUiThread {
                (row[1] as TextView).text = String.format("%3.1f", res / 1000)
            }
            res = runReadTest(rootFolder, size)
            runOnUiThread {
                (row[2] as TextView).text = String.format("%3.1f", res / 1000)
            }
        }
    }

    private fun runWriteTest(rootFolder: String, size: Int): Double {
        val file: File
        try {
            file = fileUtils.createTestFile(rootFolder)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG)
                .show()
            return 0.0
        }
        return executeWrite(size, file, false)
    }

    private fun runReadTest(rootFolder: String, size: Int): Double {
        val file: File
        try {
            file = fileUtils.createTestFile(rootFolder)
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG)
                .show()
            return 0.0
        }
        executeWrite(size, file, true)
        return executeRead(size, file)
    }

    // Returns measured speed in Bytes / sec
    private fun executeWrite(size: Int, file: File, readPrep: Boolean): Double {
        val data = ByteArray(size)
        var done = false
        var count = 0
        var time = 0L
        val startTime = System.currentTimeMillis()
        while (!done) {
            time += measureTimeMillis {
                file.writeBytes(data)
            }
            if (readPrep) {
                done = true
            } else if ((System.currentTimeMillis() - startTime) > minTestIntervalMs && count > 1) {
                done = true
            }
            count++
        }
        if (!readPrep) {
            file.delete()
        }
        val avgSpeed = (size.toDouble() * count) / (time.toDouble() / 1_000.0)
        return avgSpeed
    }

    // Returns measured speed in Bytes / sec
    private fun executeRead(size: Int, file: File): Double {
        val raf = RandomAccessFile(file, "rw")
        val data = ByteArray(size)
        var done = false
        var count = 0
        val startTime = System.currentTimeMillis()
        var time = 0L
        while (!done) {
            raf.seek(0L)
            time += measureTimeMillis {
                raf.read(data)
            }
            if ((System.currentTimeMillis() - startTime) > minTestIntervalMs && count > 1) {
                done = true
            }
            count++
        }
        file.delete()
        val avgSpeed = (size.toDouble() * count) / (time.toDouble() / 1_000.0)
        return avgSpeed
    }

    private fun hasPermissions(): Boolean {
        val permissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        val permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return permissionRead == PackageManager.PERMISSION_GRANTED
                && permissionWrite == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            READ_STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            for (res in grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "This program requires storage read/write permissions", Toast.LENGTH_LONG)
                        .show()
                } else {
                    init()
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

}
