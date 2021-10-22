package com.aliucord.plugins

import android.content.Context

import com.aliucord.Utils
import com.aliucord.Logger
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.Plugin
import com.aliucord.Http
import com.aliucord.annotations.AliucordPlugin

import com.discord.api.commands.ApplicationCommandType

class Result(val queryresult: QueryResult) {
    class QueryResult(val inputstring: String, val pods: List<Pod>)
    class Pod(val subpods: List<SubPods>)
    class SubPods(val plaintext: String)
}

private fun fmtSend(data: Result): String {
    return StringBuilder()
            .append("**Input:**\n`${data.queryresult.inputstring}`\n")
            .append("**Output:**\n`${data.queryresult.pods[1].subpods[0].plaintext}`")
            .toString()
}

@AliucordPlugin
class Calc : Plugin() {
    private val LOGGER = Logger("Calc")

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
        ) { ctx -> try {
            val url = Http.QueryBuilder("https://api.wolframalpha.com/v2/query")
                    .append("input", ctx.getRequiredString("input"))
                    .append("format", "plaintext")
                    .append("output", "JSON")
                    .append("appid", "no")
                    .toString()
            try {
                val result = Http.Request(url, "GET").execute().json(Result::class.java)
                val toSend = fmtSend(result)
                CommandResult(toSend)
            } catch (t: Throwable) {
                val resultAgain = Http.Request(url, "GET").execute().text()
                if ("Invalid appid" in resultAgain) {
                    CommandResult("An error occurred: `Invalid API Key`\nCheck Debug Logs for info.")
                } else {
                    CommandResult("An error occurred. Check Debug Logs.")
                }
            }
        }
        catch (t: Throwable) {
            LOGGER.error(t)
            CommandResult("An error occurred.", null, false)
        } }
    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}
