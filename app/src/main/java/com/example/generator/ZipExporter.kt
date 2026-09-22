package com.example.generator

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipExporter {

    fun createZip(projectDir: File, zipFileName: String): File {
        val zipFile = File(projectDir, zipFileName)
        if (zipFile.exists()) {
            zipFile.delete()
        }

        val filesToZip = listOf("index.html", "admin.html", "firestore.rules", "SETUP.md")
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            for (name in filesToZip) {
                val file = File(projectDir, name)
                if (file.exists()) {
                    val entry = ZipEntry(name)
                    zos.putNextEntry(entry)
                    FileInputStream(file).use { fis ->
                        fis.copyTo(zos)
                    }
                    zos.closeEntry()
                }
            }
        }
        return zipFile
    }
}
