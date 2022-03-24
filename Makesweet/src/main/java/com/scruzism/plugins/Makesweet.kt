package com.scruzism.plugins

import android.content.Context
import android.net.Uri

import com.aliucord.Http
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.annotations.AliucordPlugin
import com.discord.api.commands.ApplicationCommandType

import java.io.File
import java.io.FileOutputStream
import java.net.URLEncoder

private fun buildReq(path: String, method: String = "POST"): Http.Request{
    val base = "https://mkswt.herokuapp.com"
    return Http.Request(base + path, method)
}

private fun makeTempFile(response: Http.Response, mContext: Context): File {
    val tempFile = File.createTempFile("temp", ".gif", mContext.cacheDir)
    val os = FileOutputStream(tempFile)
    response.pipe(os)
    tempFile.deleteOnExit()
    return tempFile
}

@AliucordPlugin
class Makesweet : Plugin() {

    private var log = Logger("makesweet")

    override fun start(ctx: Context) {

        val args = listOf(
                Utils.createCommandOption(
                        ApplicationCommandType.SUBCOMMAND,
                        "text",
                        subCommandOptions = listOf(
                                Utils.createCommandOption(
                                        ApplicationCommandType.STRING,
                                        "leftText",
                                        "Enter text for left side of locket",
                                        required = true
                                ),
                                Utils.createCommandOption(
                                        ApplicationCommandType.STRING,
                                        "rightText",
                                        "Enter text for right side of locket",
                                        required = false
                                )
                        )
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.SUBCOMMAND,
                        "image",
                        subCommandOptions = listOf(
                                Utils.createCommandOption(
                                        ApplicationCommandType.STRING,
                                        "imageUrl",
                                        "Enter the image URL",
                                        required = true
                                )
                        )
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.SUBCOMMAND,
                        "textAndImage",
                        subCommandOptions = listOf(
                                Utils.createCommandOption(
                                        ApplicationCommandType.STRING,
                                        "text",
                                        "Add a text",
                                        required = true
                                ),
                                Utils.createCommandOption(
                                        ApplicationCommandType.STRING,
                                        "imageUrl",
                                        "Enter an image URL",
                                        required = true
                                )
                        )
                )
        )

        commands.registerCommand("makesweet", "makesweet API", args) {
            if (it.containsArg("text")) {
                val leftText = URLEncoder.encode(it.getSubCommandArgs("text")?.get("leftText").toString(), "UTF-8")
                val rawRightText = it.getSubCommandArgs("text")?.get("rightText").toString()
                val rightText = URLEncoder.encode("&text=$rawRightText", "UTF-8") // I know, it's weird

                val url = StringBuilder("/text?text=$leftText")
                if (rawRightText != "null") { url.append(rightText) }
                val resp = buildReq(url.toString()).execute()
                val file = makeTempFile(resp, ctx)
                it.addAttachment(Uri.fromFile(file).toString(), "makesweet.gif")
            }

            if (it.containsArg("image")) {
                val imageUrl = it.getSubCommandArgs("image")?.get("imageUrl").toString()
                val imageResp = Http.Request(imageUrl).execute()
                val image = makeTempFile(imageResp, ctx)
                val resp = buildReq("/image").executeWithMultipartForm(mapOf("file" to image))
                val file = makeTempFile(resp, ctx)
                it.addAttachment(Uri.fromFile(file).toString(), "makesweet.gif")
            }

            if (it.containsArg("textAndImage")) {
                val text = URLEncoder.encode(it.getSubCommandArgs("textAndImage")?.get("text").toString(), "UTF-8")
                val imageUrl = it.getSubCommandArgs("textAndImage")?.get("imageUrl").toString()
                val imageResp = Http.Request(imageUrl).execute()
                val image = makeTempFile(imageResp, ctx)
                val resp = buildReq("/image?text=${text}").executeWithMultipartForm(mapOf("file" to image))
                val file = makeTempFile(resp, ctx)
                it.attachments.clear()
                it.addAttachment(Uri.fromFile(file).toString(), "makesweet.gif")
            }

            CommandsAPI.CommandResult("")
        }

    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}