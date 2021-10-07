package com.aliucord.plugins

import android.content.Context

import com.aliucord.Http
import com.aliucord.Constants
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.entities.MessageEmbedBuilder
import com.aliucord.Utils.createCommandChoice
import com.aliucord.annotations.AliucordPlugin

import com.discord.models.commands.ApplicationCommandOption
import com.discord.api.commands.ApplicationCommandType

import java.util.*

data class Result(
        val url: String
)

@AliucordPlugin
class NekosLife : Plugin() {

    override fun start(ctx: Context) {
        val choices = listOf(
                createCommandChoice("anal", "anal"),
                createCommandChoice("avatar", "avatar"),
                createCommandChoice("boobs", "boobs"),
                createCommandChoice("blowjob image", "blowjob"),
                createCommandChoice("blowjob gif", "bj"),
                createCommandChoice("classic", "classic"),
                createCommandChoice("cuddle", "cuddle"),
                createCommandChoice("cum", "cum"),
                createCommandChoice("cum jpg", "cum_jpg"),
                createCommandChoice("ero", "ero"),
                createCommandChoice("ero feet", "erofeet"),
                createCommandChoice("ero kemo", "erokemo"),
                createCommandChoice("ero kitsune", "erok"),
                createCommandChoice("ero neko", "eron"),
                createCommandChoice("ero yuri", "eroyuri"),
                createCommandChoice("femdom", "femdom"),
                createCommandChoice("feet", "feet"),
                createCommandChoice("feet gif", "feetgif"),
                createCommandChoice("fox girl", "fox_girl"),
                createCommandChoice("futanari", "futanari"),
                createCommandChoice("gasm", "gasm"),
                createCommandChoice("gecg", "gecg"),
                createCommandChoice("kemonomimi", "kemonomimi"),
                createCommandChoice("kiss", "kiss"),
                createCommandChoice("kuni", "kuni"),
                createCommandChoice("hentai", "hentai"),
                createCommandChoice("holo", "holo"),
                createCommandChoice("holo ero", "holoero"),
                createCommandChoice("holo lewd", "hololewd"),
                createCommandChoice("lesbian", "les"),
                createCommandChoice("lewd", "lewd"),
                createCommandChoice("lewd kemo", "lewdkemo"),
                createCommandChoice("lewd kitsune", "lewdk"),
                createCommandChoice("neko", "neko"),
                createCommandChoice("neko gif", "ngif"),
                createCommandChoice("neko gif nsfw", "nsfw_neko_gif"),
                createCommandChoice("nsfw avatar", "nsfw_avatar"),
                createCommandChoice("pussy", "pussy"),
                createCommandChoice("pussy jpg", "pussy_jpg"),
                createCommandChoice("pwank", "pwank"),
                createCommandChoice("random hentai gif", "Random_hentai_gif"),
                createCommandChoice("small boobs", "smallboobs"),
                createCommandChoice("smug", "smug"),
                createCommandChoice("solo", "solo"),
                createCommandChoice("solo gif", "solog"),
                createCommandChoice("spank", "spank"),
                createCommandChoice("tits", "tits"),
                createCommandChoice("tickle", "tickle"),
                createCommandChoice("trap", "trap"),
                createCommandChoice("waifu", "waifu"),
                createCommandChoice("wallpaper", "wallpaper"),
                createCommandChoice("woof", "woof"),
                createCommandChoice("yuri", "yuri")
        )
        val args = ApplicationCommandOption(
                ApplicationCommandType.STRING,
                "category",
                "Category of image/gif to get",
                null,
                true,
                false,
                choices,
                null
        )
        val shouldSend = ApplicationCommandOption(
                ApplicationCommandType.BOOLEAN,
                "send",
                "Send to chat (WARNING: Use NSFW channel)",
                null,
                false,
                false,
                null,
                null
        )
        commands.registerCommand(
                "nekoslife",
                "Get images/gifs from nekos.life",
                listOf(args, shouldSend)
        )
        { ctx ->
            val choosen = ctx.getRequiredString("category")
            try {
                val result = Http.simpleJsonGet(
                        "https://nekos.life/api/v2/img/${choosen}",
                        Result::class.java).url

                if (ctx.getChannel().guildId == Constants.ALIUCORD_GUILD_ID) {
                    var embed = listOf(
                            MessageEmbedBuilder()
                                    .setColor(Random().nextInt(0xffffff + 1))
                                    .setImage(result)
                                    .setFooter("Won't send image/gif to chat in Aliucord server")
                                    .build()
                    )
                    return@registerCommand CommandsAPI.CommandResult(null, embed, false)
                } else {
                    var send = ctx.getBoolOrDefault("send", false)
                    if (send == false) {
                        var embed = listOf(
                                MessageEmbedBuilder()
                                        .setColor(Random().nextInt(0xffffff + 1))
                                        .setImage(result)
                                        .build()
                        )
                        return@registerCommand CommandsAPI.CommandResult(null, embed, false)
                    } else { return@registerCommand CommandsAPI.CommandResult(result, null, send) }
                }
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                return@registerCommand CommandsAPI.CommandResult("Oops, an error occured.", null, false)
            }
        }
    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}
