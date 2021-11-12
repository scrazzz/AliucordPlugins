package com.scruzism.plugins

import android.content.Context

import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.Logger
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.Plugin
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.SettingsAPI

import com.discord.api.commands.ApplicationCommandType

import java.io.File
import java.util.UUID
import org.json.JSONObject

private fun uploadTo(host: String, f: File, settings: SettingsAPI): String{
    return when (host) {
        "0x0" -> {
            val multipart = MultipartUtility("https://0x0.st", "UTF-8")
            multipart.addFilePart("file", f)
            multipart.finish()[0]
        }
        "imgbb" -> {
            val url = Http.QueryBuilder("https://api.imgbb.com/1/upload")
                    .append("key", "93dd460db91461bbffa0ffcf1f6c475c") // it's fake mail acc, idc
                    .append("name", UUID.randomUUID().toString().subSequence(0, 8).toString())
                    .toString()
            val multipart = MultipartUtility(url, "UTF-8")
            multipart.addFilePart("image", f)
            return JSONObject(multipart.finish()[0].toString()).getJSONObject("data").getString("url")
        }
        "sxcu" -> {
            val subdomain = settings.getString("sxcuSubdomain", null)
            if (subdomain == null) {
                return "You have not set an sxcu subdomain in settings"
            }
            val url = "https://$subdomain/api/files/create"
            val multipart = MultipartUtility(url, "UTF-8")
            multipart.addFilePart("file", f)
            multipart.addFormField("noembed", "")
            multipart.addFormField("og_properties", "{\"discord_hide_url\": true}")
            return JSONObject("{${multipart.finish()[2].toString().trim().removeSuffix(",")}}").getString("url")
        }
        else -> "kekw"
    }
}

@AliucordPlugin
class UITH : Plugin() {

    private val LOG = Logger("UITH")
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun start(ctx: Context) {
        val hostChoices = listOf(
                Utils.createCommandChoice("0x0.st", "0x0"),
                Utils.createCommandChoice("imgbb", "imgbb"),
                Utils.createCommandChoice("sxcu", "sxcu")
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
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.BOOLEAN,
                        "send",
                        "send to chat"
                )
        )

        commands.registerCommand(
                "uith",
                "Upload Image To Host",
                args
        ) {
            val host = it.getStringOrDefault("host", "imgbb")
            val message = it.getStringOrDefault("message", "")
            val send = it.getBoolOrDefault("send", false)

            if (it.attachments.size > 1) { return@registerCommand CommandResult(
                    "Plugin does not support multiple file uploads. Please select ONE file.",
                    null, false
            )}
            val file = File(it.attachments[0].data.toString())
            val resp = uploadTo(host, file, settings)
            LOG.info(resp)

            val fmt = if (message.isEmpty()) { resp }
                      else if (resp == "You have not set an sxcu subdomain in settings") {
                          return@registerCommand CommandResult(resp, null, false)
                      }
                      else { message + "\n" + resp }
            it.attachments.clear()
            CommandResult(fmt, null, send)
        }
    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}