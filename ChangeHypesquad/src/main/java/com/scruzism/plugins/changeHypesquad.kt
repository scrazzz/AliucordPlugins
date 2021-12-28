package com.scruzism.plugins

import android.content.Context

import com.aliucord.Http
import com.aliucord.Logger
import com.aliucord.utils.GsonUtils
import com.aliucord.Utils.createCommandChoice
import com.aliucord.Utils.createCommandOption
import com.aliucord.api.CommandsAPI.CommandResult
import com.aliucord.entities.Plugin
import com.aliucord.annotations.AliucordPlugin
import com.discord.api.commands.ApplicationCommandType

class HouseData(
    val house_id: Int
)

@AliucordPlugin
class ChangeHypesquad : Plugin() {

    private val LOG = Logger("ChangeHypesquad")

    override fun start(ctx: Context) {
        val args = listOf(
            createCommandOption(
                ApplicationCommandType.STRING,
                "house",
                "Choose your new house",
                choices = listOf(
                    createCommandChoice("Bravery", "1"),
                    createCommandChoice("Brilliance", "2"),
                    createCommandChoice("Balance", "3"),
                ),
                required = true
            )
        )

        commands.registerCommand("ChangeHypesquad", "Change your Hypesquad House", args) {
            val house = it.getRequiredString("house")
            val jsonPayload = GsonUtils.fromJson("{ house_id: ${house.toInt()} }", HouseData::class.java)
            try {
                val resp = Http.Request.newDiscordRequest("/hypesquad/online", "POST").executeWithJson(jsonPayload)
                // val respInfo = "isOk: ${resp.ok()}")
                CommandResult("Your house has ${ if (resp.ok()) "been" else "not" } changed.", null, false)
            }
            catch (t: Throwable) {
                LOG.error(t)
                CommandResult("An error occurred.", null, false)
            }
        }
    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}
