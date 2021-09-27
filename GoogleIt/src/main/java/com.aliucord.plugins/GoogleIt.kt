package com.aliucord.plugins

import android.content.Context
//import androidx.annotation.NonNull

import com.aliucord.Http
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.annotations.AliucordPlugin
import com.discord.models.commands.ApplicationCommandOption
import com.discord.api.commands.ApplicationCommandType

@AliucordPlugin
class GoogleIt : Plugin() {

    override fun start(ctx: Context) {
        val query = listOf(
                ApplicationCommandOption(
                        ApplicationCommandType.STRING,
                        "query",
                        "The query to search",
                        null,
                        true,
                        false,
                        null,
                        null
                )
        )
        commands.registerCommand(
                "googleit",
                "Generates a google.it link",
                query
        )
        { ctx ->
            try {
                val url = Http.QueryBuilder("https://google.it/search")
                        .append("q", ctx.getString("query"))
                        .toString()
                CommandsAPI.CommandResult(url)
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                CommandsAPI.CommandResult("An error occured", null, false)
            }
        }
    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}
