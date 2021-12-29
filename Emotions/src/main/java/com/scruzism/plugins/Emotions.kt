package com.scruzism.plugins

import android.content.Context

import com.aliucord.Http
import com.aliucord.Logger
import com.aliucord.entities.Plugin
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.Utils.createCommandChoice
import com.aliucord.Utils.createCommandOption
import com.aliucord.api.CommandsAPI.CommandResult
import com.discord.api.commands.ApplicationCommandType

class Result(val url: String)

private fun makeReq(chosen: String): String {
    val base = "https://nekos.life/api/v2/img"
    val r = Http.simpleJsonGet("$base/$chosen", Result::class.java)
    return r.url
}

@AliucordPlugin
class Emotions : Plugin() {

    private val LOG = Logger("Emotions")

    override fun start(ctx: Context) {
        val choices = listOf(
                createCommandChoice(
                        "hug",
                        "hug"),
                createCommandChoice(
                        "kiss",
                        "kiss"),
                createCommandChoice(
                        "pat",
                        "pat"),
                createCommandChoice(
                        "cuddle",
                        "cuddle"),
                createCommandChoice(
                        "slap",
                        "slap"),
                createCommandChoice(
                        "tickle",
                        "tickle")
        )
        val args = listOf(
                createCommandOption(
                        ApplicationCommandType.STRING,
                        "show",
                        "Choose an emotion to show",
                        required = true,
                        choices = choices
                ),
                createCommandOption(
                        ApplicationCommandType.USER,
                        "user",
                        "Choose the user to show this to",
                        required = false
                )
        )

        commands.registerCommand("emotions", "Show emotions to someone", args) {
            val chosen = it.getRequiredString("show")
            val user = it.getUserOrDefault("user", it.me) // default to you
            val me = it.me

            try {
                val emotion = makeReq(chosen)
                if (me.id != user.id) {
                    return@registerCommand CommandResult("<@${user.id}>\n$emotion")
                }
                // filter yourself and just send the url
                return@registerCommand CommandResult("$emotion")
            }
            catch (ex: Throwable) {
                LOG.error(ex)
                return@registerCommand CommandResult("An error occured, check debug logs", null, false)
            }

        }
    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}
