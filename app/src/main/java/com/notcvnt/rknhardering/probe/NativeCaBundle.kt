package com.notcvnt.rknhardering.probe

import android.content.Context
import java.io.File

internal object NativeCaBundle {
    private const val ASSET_DIR = "native-curl"
    private const val ASSET_NAME = "cacert.pem"
    private const val HASH_ASSET_NAME = "cacert.pem.sha256"

    data class Info(
        val absolutePath: String,
        val versionHash: String,
    )

    fun ensureInstalled(context: Context): Info {
        val versionHash = context.assets
            .open("$ASSET_DIR/$HASH_ASSET_NAME")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }
            .trim()
        val targetDir = File(context.filesDir, "native-ca")
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        val targetFile = File(targetDir, "cacert-$versionHash.pem")
        if (!targetFile.exists()) {
            context.assets.open("$ASSET_DIR/$ASSET_NAME").use { input ->
                targetFile.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return Info(
            absolutePath = targetFile.absolutePath,
            versionHash = versionHash,
        )
    }
}

