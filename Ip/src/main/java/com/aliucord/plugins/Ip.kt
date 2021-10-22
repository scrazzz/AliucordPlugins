package com.aliucord.plugins

import android.content.Context

import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.Logger
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.annotations.AliucordPlugin
import com.discord.api.commands.ApplicationCommandType

import java.net.URLEncoder.encode
import org.json.JSONObject

@AliucordPlugin
class Ip : Plugin() {

    private val LOGGER = Logger("Plugin - IP")

    override fun start(ctx: Context) {
        val args = listOf(Utils.createCommandOption(
                ApplicationCommandType.STRING,
                "query",
                "Enter IPv4/IPv6 address or domain name",
                required = true
        ))

        commands.registerCommand("ip", "IP lookup", args) {
            val query = it.getRequiredString("query")
            try {
                val url    = "http://ip-api.com/json/${query}"
                val result = Http.Request(url, "GET").execute().text()
                val fmt    = JSONObject(result).toString(4)
                val toSend = "```py\n${fmt}\n```"
                CommandsAPI.CommandResult(toSend, null, false, "IP Info")
            }
            catch (t: Throwable) {
                LOGGER.error(t)
                CommandsAPI.CommandResult(
                        "An error occured. Check Debug Logs",
                        null, false, "IP Info")
            }
        }

    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}