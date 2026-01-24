/*
 * Copyright 2026 Proify, Tomakino
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package io.github.proify.qrckit.decrypt

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.InflaterInputStream

object QrcDecrypter {
    private val QQ_KEY = "!@#)(*$%123ZXC!@!@#)(NHL".toByteArray(Charsets.US_ASCII)

    private val decryptSchedule = Array(3) { Array(16) { ByteArray(6) } }.also {
        DESHelper.tripleDESKeySetup(QQ_KEY, it, DESHelper.DECRYPT)
    }

    fun decrypt(encrypted: String?): String? {
        if (encrypted.isNullOrBlank()) return null

        return runCatching {
            val encryptedBytes = hexStringToByteArray(encrypted)
            val decryptedData = ByteArray(encryptedBytes.size)

            val temp = ByteArray(8)
            val inputBlock = ByteArray(8)
            for (i in encryptedBytes.indices step 8) {
                System.arraycopy(encryptedBytes, i, inputBlock, 0, 8)
                DESHelper.tripleDESCrypt(inputBlock, temp, decryptSchedule)
                System.arraycopy(temp, 0, decryptedData, i, 8)
            }

            val unzippedData = decompress(decryptedData)
            unzippedData.toString(Charsets.UTF_8)
        }.getOrElse {
            it.printStackTrace()
            null
        }
    }

    private fun decompress(data: ByteArray): ByteArray {
        return ByteArrayInputStream(data).use { bis ->
            InflaterInputStream(bis).use { iis ->
                ByteArrayOutputStream(data.size * 2).use { bos ->
                    iis.copyTo(bos, bufferSize = 4096)
                    bos.toByteArray()
                }
            }
        }
    }

    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length
        require(len % 2 == 0) { "Hex string length must be even" }

        val data = ByteArray(len / 2)
        for (i in 0..<len step 2) {
            val h = Character.digit(hexString[i], 16)
            val l = Character.digit(hexString[i + 1], 16)
            data[i / 2] = ((h shl 4) or l).toByte()
        }
        return data
    }
}