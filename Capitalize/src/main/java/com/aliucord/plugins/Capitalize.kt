/*
MIT License

Copyright (c) 2021 scrazzz

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package com.aliucord.plugins

import android.content.Context

import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.annotations.AliucordPlugin
import com.discord.models.commands.ApplicationCommandOption
import com.discord.api.commands.ApplicationCommandType

@AliucordPlugin
class Capitalize : Plugin() {

    override fun start(ctx: Context) {
        val message = ApplicationCommandOption(
                ApplicationCommandType.STRING,
                "message",
                "Your message",
                null,
                true,
                false,
                null,
                null
        )
        commands.registerCommand(
                "capitalize", "Capitalize every word in your message", listOf(message)
        ) {
            ctx -> try {
                val message = ctx.getRequiredString("message")
                val to_send = message.capitalizeWords()
                CommandsAPI.CommandResult(to_send)
            } catch (t: Throwable) {
                t.printStackTrace()
                CommandsAPI.CommandResult("An error occured :/", null, false)
            }
        }

    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}

fun String.capitalizeWords(): String = split(" ").map { it.toLowerCase().capitalize() }.joinToString(" ")
