/*
 * Copyright (c) 2021 scrazzz
 * Licensed under the MIT License
 */

package com.aliucord.plugins

import android.content.Context

import com.aliucord.Utils
import com.aliucord.Logger
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.Plugin
import com.aliucord.Http
import com.aliucord.annotations.AliucordPlugin

import com.discord.api.commands.ApplicationCommandType
import java.io.IOException

@AliucordPlugin
class Calc : Plugin() {

    private val LOGGER = Logger("Calc")
    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    override fun start(ctx: Context) {
        val args = listOf(
                Utils.createCommandOption(
                        ApplicationCommandType.STRING,
                        "input",
                        "Input a question",
                        required = true),
                Utils.createCommandOption(
                        ApplicationCommandType.BOOLEAN,
                        "send",
                        "Send to chat",
                        required = false)
        )

        commands.registerCommand(
                "calc",
                "Calculate your answer using wolfram API",
                args
        ) { ctx ->
            val input = ctx.getRequiredString("input")
            val send = ctx.getBoolOrDefault("send", false)

            val url = Http.QueryBuilder("https://api.wolframalpha.com/v1/result")
                    .append("appid", settings.getString("appid", null))
                    .append("i", input).toString()
            val result = Http.Request(url).execute()

            try {
                CommandResult(result.text(), null, send)
            }
            catch (ex: IOException) {
                if (ex is Http.HttpException) {
                    LOGGER.error((ex))
                    CommandResult(ex.message, null, false, "Calc")
                }
                LOGGER.error(ex)
                CommandResult("An error occured. ```\n${ex.message}\n```", null, false, "Calc")
            }
        }
    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}
