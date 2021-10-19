package com.aliucord.plugins

import android.content.Context

import com.aliucord.Utils
import com.aliucord.Logger
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.Utils.createCommandChoice
import com.aliucord.annotations.AliucordPlugin

import com.discord.api.commands.ApplicationCommandType


private fun add(n1: Long, n2: Long) = n1 + n2
private fun sub(n1: Long, n2: Long) = n1 - n2
private fun mul(n1: Long, n2: Long) = n1 * n2
private fun div(n1: Long, n2: Long) = n1 / n2
/*{
    if (n2 == 0) { return "You fool" }
    else { n1 / n2 }
}*/

@AliucordPlugin
class Calc : Plugin() {
    private val LOGGER = Logger("Calc")

    override fun start(ctx: Context) {
        val opChoices = listOf(
                createCommandChoice("+", "+"),
                createCommandChoice("-", "-"),
                createCommandChoice("*", "*"),
                createCommandChoice("/", "/")
        )
        val args = listOf(Utils.createCommandOption(
                ApplicationCommandType.INTEGER,
                "num1",
                "Enter first number",
                null,
                true
        ), Utils.createCommandOption(
                ApplicationCommandType.STRING,
                "operation",
                "Enter the operation",
                null,
                true,
                default = false,
                channelTypes = emptyList(),
                choices = opChoices
        ), Utils.createCommandOption(
                ApplicationCommandType.INTEGER,
                "num2",
                "Enter the second number",
                null,
                true
        ))

        commands.registerCommand(
                "calc",
                "Calculate simple numbers",
                args
        ) { ctx -> try {
            val n1 = ctx.getRequiredLong("num1")
            val op = ctx.getRequiredString("operation")
            val n2 = ctx.getRequiredLong("num2")
            var result: String

            when (op) {
                "+" -> CommandsAPI.CommandResult(add(n1, n2).toString())
                "-" -> CommandsAPI.CommandResult(sub(n1, n2).toString())
                "*" -> CommandsAPI.CommandResult(mul(n1, n2).toString())
                "/" -> CommandsAPI.CommandResult(div(n1, n2).toString())
                else -> CommandsAPI.CommandResult("Invalid operator")
            }
        } catch (t: Throwable) {
            LOGGER.error(t)
            CommandsAPI.CommandResult("An error occurred.", null, false)
        }
        }
    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}
