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
                                        "text",
                                        "Enter text",
                                        required = true
                                )
                        )
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.SUBCOMMAND,
                        "image"
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.SUBCOMMAND,
                        "textAndImage",
                        subCommandOptions = listOf(
                                Utils.createCommandOption(
                                        ApplicationCommandType.STRING,
                                        "text",
                                        "Add text and pick an image",
                                        required = true
                                )
                        )
                )
        )

        commands.registerCommand("makesweet", "makesweet API", args) {
            if (it.containsArg("text")) {
                val text = it.getSubCommandArgs("text")?.get("text").toString()
                val resp = buildReq("/text?text=${URLEncoder.encode(text, "UTF-8")}").execute()
                val file = makeTempFile(resp, ctx)
                it.addAttachment(Uri.fromFile(file).toString(), "makesweet.gif")
            }

            if (it.containsArg("image")) {
                val image = try { File(it.attachments[0].data.toString()) } catch (t: Throwable) {
                    log.error(t)
                    return@registerCommand CommandsAPI.CommandResult("You have not provided an attachment or an error occurred", null, false)
                }
                val resp = buildReq("/image").executeWithMultipartForm(mapOf("file" to image))
                val file = makeTempFile(resp, ctx)
                it.attachments.clear()
                it.addAttachment(Uri.fromFile(file).toString(), "makesweet.gif")
            }

            if (it.containsArg("textAndImage")) {
                val text = it.getSubCommandArgs("textAndImage")?.get("text").toString()
                val image = try { File(it.attachments[0].data.toString()) } catch (t: Throwable) {
                    log.error(t)
                    return@registerCommand CommandsAPI.CommandResult("You have not provided an attachment or an error occurred", null, false)
                }
                val resp = buildReq("/image?text=${URLEncoder.encode(text, "UTF-8")}").executeWithMultipartForm(mapOf("file" to image))
                val file = makeTempFile(resp, ctx)
                it.attachments.clear()
                it.addAttachment(Uri.fromFile(file).toString(), "makesweet.gif")
            }

            CommandsAPI.CommandResult("")
        }

    }

    override fun stop(ctx: Context) = commands.unregisterAll()

}
