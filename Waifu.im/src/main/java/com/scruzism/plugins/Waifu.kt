package com.scruzism.plugins

import android.content.Context

import com.aliucord.Http
import com.aliucord.Utils
import com.aliucord.Logger
import com.aliucord.entities.Plugin
import com.aliucord.entities.MessageEmbedBuilder
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.Utils.createCommandChoice
import com.aliucord.api.CommandsAPI.CommandResult

import com.discord.api.commands.ApplicationCommandType
import com.discord.api.message.embed.MessageEmbed

class Result(
        val images: List<Images>
) {
    data class Images(
            val width: String,
            val height: String,
            val url: String
    )
}

private fun request(tag: String, isNsfw: Boolean, log: Logger): Result {
    val url = StringBuilder("https://api.waifu.im/random/?gif=false&selected_tags=$tag")
    if (isNsfw) {
        url.append("&is_nsfw=true")
    }
    return Http.simpleJsonGet(url.toString(), Result::class.java)
}

private fun createEmbed(url: String, height: String, width: String): MessageEmbed {
    return MessageEmbedBuilder()
            .setRandomColor()
            .setImage(url, null, height.toInt(), width.toInt())
            .setAuthor("waifu.im")
            .build()
}


@AliucordPlugin
class Waifu : Plugin() {

    private val log = Logger("Waifu.im")

    override fun start(ctx: Context) {
        // sfwChoices
        val sfwChoices = listOf(
                createCommandChoice("maid", "maid"),
                createCommandChoice("mori-calliope", "mori-calliope"),
                createCommandChoice("oppai", "oppai"),
                createCommandChoice("uniform", "uniform"),
                createCommandChoice("raiden-shogun","raiden-shogun"),
                createCommandChoice("selfies", "selfies"),
                createCommandChoice("waifu", "waifu"),
        )
        // TODO: nsfwChoices

        val args = listOf(
                Utils.createCommandOption(
                        ApplicationCommandType.STRING,
                        "tags",
                        "Choose a tag - Default: waifu",
                        choices = sfwChoices
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.BOOLEAN,
                        "is_nsfw",
                        "Whether the image should be NSFW - Default: false",
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.BOOLEAN,
                        "send",
                        "Send image to chat - Default: false"
                )
        )

        commands.registerCommand("waifu", "Get images from waifu.im API", args) {
            val sfwTag = it.getStringOrDefault("tags", "waifu")
            val isNsfw = it.getBoolOrDefault("is_nsfw", false)
            val send = it.getBoolOrDefault("send", false)

            val result = request(sfwTag, isNsfw, log).images[0]

            if (!send) {
                val embed = createEmbed(result.url, result.height, result.width)
                return@registerCommand CommandResult(null, mutableListOf(embed), false)
            }

            CommandResult(result.url, null, send)
        }
    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}
