package com.scruzism.plugins

import android.content.Context

import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.Logger
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.annotations.AliucordPlugin
import com.discord.api.commands.ApplicationCommandType

import java.net.URLEncoder

data class APIResponse(
	val screenshot: String
)


@AliucordPlugin
class ScreenshotAPI : Plugin() {

    private val log = Logger("ScreenshotAPI")

    override fun start(ctx: Context) {
        val args = listOf(
                Utils.createCommandOption(
                        ApplicationCommandType.STRING,
                        "url",
                        "Enter website URL",
                        required = true
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.BOOLEAN,
                        "send",
                        "Send to chat",
                )
        )

        commands.registerCommand("screenshot", "Screenshot a website", args) {
            val url = URLEncoder.encode(it.getRequiredString("url"))
            val shouldSend = it.getBoolOrDefault("send", false)
            try {
                val httpUrl = StringBuilder("https://shot.screenshotapi.net/screenshot")
				    .append("&url=$url&output=json&file_type=png&block_ads=true&wait_for_event=load&ttl=15").toString()
				log.debug(url)
                val result = Http.Request(httpUrl, "GET").execute().json(APIResponse::class.java).screenshot

                CommandsAPI.CommandResult(result, null, shouldSend, "ScreenshotAPI")
            }
            catch (t: Throwable) {
                log.error(t)
                CommandsAPI.CommandResult(
                        "An error occured. Check Debug Logs",
                        null, false, "IP Info")
            }
        }

    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}
