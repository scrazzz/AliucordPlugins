package com.aliucord.plugins

import android.content.Context

import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.Logger
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.annotations.AliucordPlugin
import com.discord.api.commands.ApplicationCommandType

import org.json.JSONObject
import java.lang.StringBuilder

@AliucordPlugin
class Ip : Plugin() {

    private val LOGGER = Logger("Plugin - IP")

    override fun start(ctx: Context) {
        val args = listOf(
                Utils.createCommandOption(
                        ApplicationCommandType.STRING,
                        "query",
                        "Enter IPv4/IPv6 address or domain name",
                        required = true
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.BOOLEAN,
                        "send",
                        "Send to chat",
                )
        )

        commands.registerCommand("ip", "IP lookup", args) {
            val query = it.getRequiredString("query")
            val shouldSend = it.getBoolOrDefault("send", false)
            try {
                val url = StringBuilder("http://ipwhois.app/json/$query?objects=")
                        .append("success,ip,type,as,org,isp,continent,country,country_capital,city,region,")
                        .append("country_phone,latitude,longitude,timezone,currency,completed_requests")
                        .toString()
                val result = Http.Request(url, "GET").execute().text()
                val fmt    = JSONObject(result).toString(4)
                val toSend = "```py\n${fmt}\n```"
                CommandsAPI.CommandResult(toSend, null, shouldSend, "IP Info")
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