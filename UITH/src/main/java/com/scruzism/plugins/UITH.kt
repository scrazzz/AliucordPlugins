package com.scruzism.plugins

import android.content.Context
import com.aliucord.Constants

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

private fun makeReq(host: String, name: String, file: File): MutableList<String> {
    val mp = MultipartUtility(host, "UTF-8")
    mp.addFilePart(name, file)
    return mp.finish()
}

private fun uploadTo(host: String, f: File, settings: SettingsAPI): String{
    return when (host) {
        "0x0.st" -> {
            return makeReq("https://0x0.st", "file", f)[0]
        }
        "imgbb" -> {
            val url = Http.QueryBuilder("https://api.imgbb.com/1/upload")
                    .append("key", "93dd460db91461bbffa0ffcf1f6c475c")
                    .append("name", UUID.randomUUID().toString().subSequence(0, 8).toString())
                    .toString()
            return JSONObject(makeReq(url, "image", f)[0]).getJSONObject("data").getString("url")
        }
        "sxcu" -> {
            val subdomain = settings.getString("sxcuSubdomain", null)
                    ?: return "You have not set an sxcu subdomain in settings"
            val url = "https://$subdomain/api/files/create"
            val multipart = MultipartUtility(url, "UTF-8")
            multipart.addFilePart("file", f)
            multipart.addFormField("noembed", "")
            multipart.addFormField("og_properties", "{\"discord_hide_url\": true}")
            return JSONObject("{${multipart.finish()[2].toString().trim().removeSuffix(",")}}").getString("url")
        }
        else -> "" // unreachable
    }
}

@AliucordPlugin
class UITH : Plugin() {

    private val LOG = Logger("UITH")
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    /*
    override fun load(context: Context?)
    {
        commands.unregisterAll()
        Utils.showToast("Deleting UITH plugin because it's deprecated.\nInstall UTH", true)
        LOG.info(Constants.PLUGINS_PATH)
        File(Constants.PLUGINS_PATH + "/UITH.zip").delete()
        File(Constants.BASE_PATH + "/UITH.json").delete()
    }
     */

    override fun load(context: Context?) {
        Utils.showToast("UITH is an unreleased plugin, Do NOT ask for help about bugs.", true)
    }

    override fun start(ctx: Context) {

        /* patch: automatically upload attachment to host */
        // nvm it was harder than i thought, maybe i'll add it after learning how to patch

        val defHost = settings.getString("defaultHost", "imgbb")

        val hostChoices = listOf(
                Utils.createCommandChoice("0x0.st", "0x0.st"),
                Utils.createCommandChoice("imgbb", "imgbb"),
                Utils.createCommandChoice("sxcu", "sxcu")
        )
        val args = listOf(
                Utils.createCommandOption(
                        ApplicationCommandType.STRING,
                        "host",
                        "Choose an image upload host, Your default host: $defHost",
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
            val host = it.getStringOrDefault("host", defHost)
            val message = it.getStringOrDefault("message", "")
            val send = it.getBoolOrDefault("send", settings.getBool("defaultSend", false))

            if (it.attachments.size > 1) { return@registerCommand CommandResult(
                    "Plugin does not support multiple file uploads. Please select ONE file.",
                    null, false, "UITH"
            )}
            if (it.attachments.size == 0) { return@registerCommand CommandResult(
                    "You have not selected an attachment to upload.",
                    null, false, "UITH"
            )}

            val file = File(it.attachments[0].data.toString())
            val resp = uploadTo(host, file, settings)
            LOG.info(resp)

            val fmt = if (message.isEmpty()) { resp }
                      else if (resp == "You have not set an sxcu subdomain in settings") {
                          return@registerCommand CommandResult(resp, null, false, "UITH") }
                      else { message + "\n" + resp }
            it.attachments.clear()
            CommandResult(fmt, null, send)
        }
    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}