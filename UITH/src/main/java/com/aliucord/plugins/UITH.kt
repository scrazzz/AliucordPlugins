package com.aliucord.plugins

import android.content.Context

import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.Logger
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.Plugin
import com.aliucord.annotations.AliucordPlugin

import com.discord.api.commands.ApplicationCommandType
import java.io.File
import org.json.JSONObject

private fun uploadTo(host: String, f: File): String{
    return when (host) {
        "0x0" -> {
            val multipart = MultipartUtility("https://0x0.st", "UTF-8")
            multipart.addFilePart("file", f)
            multipart.finish()[0]
        }
        "imgbb" -> {
            val url = Http.QueryBuilder("https://api.imgbb.com/1/upload")
                    .append("key", "93dd460db91461bbffa0ffcf1f6c475c") // it's fake mail acc, idc
                    .toString()
            val multipart = MultipartUtility(url, "UTF-8")
            multipart.addFilePart("image", f)
            return JSONObject(multipart.finish()[0].toString()).getJSONObject("data").getString("url")
        }
        else -> "kekw"
    }
}

@AliucordPlugin
class UITH : Plugin() {

    private val LOG = Logger("UITH")

    override fun start(ctx: Context) {
        val hostChoices = listOf(
                Utils.createCommandChoice("0x0.st", "0x0"),
                Utils.createCommandChoice("imgbb", "imgbb")
        )
        val args = listOf(
                Utils.createCommandOption(
                        ApplicationCommandType.STRING,
                        "host",
                        "Choose an image upload host",
                        choices = hostChoices
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.STRING,
                        "message",
                        "An optional message to send"
                )
        )

        commands.registerCommand(
                "uith",
                "Upload Image To Host",
                args
        ) {
            val host = it.getStringOrDefault("host", "imgbb")
            val message = it.getStringOrDefault("message", "")

            val file = File(it.attachments[0].data.toString())
            val resp = uploadTo(host, file)
            LOG.info(resp)

            CommandResult(message + "\n" + resp, null, false)
        }
    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}