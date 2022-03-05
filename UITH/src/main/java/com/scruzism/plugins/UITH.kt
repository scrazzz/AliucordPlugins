package com.scruzism.plugins

import android.content.Context
import android.webkit.MimeTypeMap

import com.aliucord.Http
import com.aliucord.Logger
import com.aliucord.Utils
import com.aliucord.entities.Plugin
import com.aliucord.annotations.AliucordPlugin

import com.aliucord.patcher.before
import com.aliucord.utils.GsonUtils
import com.aliucord.api.CommandsAPI.CommandResult
import com.discord.api.commands.ApplicationCommandType
import com.discord.widgets.chat.MessageContent
import com.discord.widgets.chat.MessageManager
import com.discord.widgets.chat.input.ChatInputViewModel
import com.lytefast.flexinput.model.Attachment
//import com.discord.api.message.LocalAttachment
//import com.discord.utilities.attachments.AttachmentUtilsKt.toAttachment

import com.google.gson.JsonSyntaxException
import org.json.JSONException
import org.json.JSONObject

import java.io.File
import java.io.IOException
import java.lang.IndexOutOfBoundsException
import java.util.regex.Pattern


private fun newUpload(file: File, data: Config, log: Logger): String {
    val lock = Object()
    val result = StringBuilder()

    // thanks Link
    synchronized(lock) {
        Utils.threadPool.execute {
            try {
                val params = mutableMapOf<String, Any>()
                val resp = Http.Request("${data.RequestURL}", "POST")

                if (data.Headers != null)
                {
                    for ((k, v) in data.Headers!!.entries) {
                        resp.setHeader(k, v)
                    }
                }

                if (data.Arguments != null)
                {
                    for ((k, v) in data.Arguments!!.entries) {
                        params[k] = v
                    }
                }
                params["${data.FileFormName}"] = file
                result.append(resp.executeWithMultipartForm(params).text())
            }
            catch (ex: Throwable) {
                if (ex is IOException) {
                    log.debug("${ex.message} | ${ex.cause} | $ex | ${ex.printStackTrace()}")
                }
                log.error(ex)
            }
            finally {
                synchronized(lock) {
                    lock.notifyAll()
                }
            }
        }
        lock.wait(9_000)
    }
    try {
        log.debug(JSONObject(result.toString()).toString(4))
    } catch (e: JSONException) {
        log.debug(result.toString())
    }
    return result.toString()
}

@AliucordPlugin
class UITH : Plugin() {

    init {
        settingsTab = SettingsTab(PluginSettings::class.java).withArgs(settings)
    }

    private val LOG = Logger("UITH")

    // source: https://github.com/TymanWasTaken/aliucord-plugins/blob/main/EncryptDMs/src/main/kotlin/tech/tyman/plugins/encryptdms/EncryptDMs.kt#L321-L326
    private val textContentField = MessageContent::class.java.getDeclaredField("textContent").apply { isAccessible = true }
    private fun MessageContent.set(text: String) = textContentField.set(this, text)

    // compile regex before uploading to speed up process
    private var re = try {
        settings.getString("regex", "https:\\/\\/[\\w./-]*").toRegex().toString()
    } catch (e: Throwable) {
        LOG.error(e)
    }
    private val pattern = Pattern.compile(re.toString())

    override fun start(ctx: Context) {

        val args = listOf(
                Utils.createCommandOption(
                        ApplicationCommandType.SUBCOMMAND, "add", "Add sharex config",
                        subCommandOptions = listOf(
                                Utils.createCommandOption(
                                        ApplicationCommandType.STRING,
                                        "sharex",
                                        "Add sharex config (paste the contents)",
                                        required = true
                                )
                        )
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.SUBCOMMAND, "current", "View current UITH settings"
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.SUBCOMMAND, "disable", "Disable plugin",
                        subCommandOptions = listOf(
                                Utils.createCommandOption(
                                        ApplicationCommandType.BOOLEAN,
                                        "disable",
                                        required = true
                                )
                        )
                )
        )
        commands.registerCommand("uith", "Upload Image To Host", args) {
            if (it.containsArg("add")) {
                val config = try {
                    GsonUtils.fromJson(it.getSubCommandArgs("add")?.get("sharex").toString(), Config::class.java)
                }
                catch (ex: JsonSyntaxException) {
                    return@registerCommand CommandResult("Invalid sharex file data provided", null, false)
                }
                if (config?.RequestURL.isNullOrEmpty()) {
                    return@registerCommand CommandResult("\"RequestURL\" must not be empty!", null, false)
                }
                if (config?.FileFormName.isNullOrEmpty()) {
                    return@registerCommand CommandResult("\"FileFormName\" must not be empty!", null, false)
                }
                LOG.debug(config.toString())
                settings.setString("sxcuConfig", it.getSubCommandArgs("add")?.get("sharex").toString())

                return@registerCommand CommandResult("Set data successfully", null, false)
            }

            if (it.containsArg("current")) {
                val configData = settings.getString("sxcuConfig", null)
                val configRegex = settings.getString("regex", null)
                val settingsUploadAllAttachments = settings.getBool("uploadAllAttachments", false)
                val settingsPluginOff = settings.getBool("pluginOff", false)
                val sb = StringBuilder()
                sb.append("sxcu config:```\n$configData\n```\n\n")
                sb.append("regex:```\n$configRegex\n```\n\n")
                sb.append("uploadAllAttachments: `$settingsUploadAllAttachments`\n")
                sb.append("pluginOff: `$settingsPluginOff`")
                return@registerCommand CommandResult(sb.toString(), null, false)
            }

            if (it.containsArg("disable")) {
                val set = it.getSubCommandArgs("disable")?.get("disable").toString()
                if (set.lowercase() == "true") settings.setString("pluginOff", set)
                if (set.lowercase() == "false") settings.setString("pluginOff", set)
                return@registerCommand CommandResult(
                        "Plugin Disabled: ${settings.getString("pluginOff", false.toString())}", null, false
                )
            }

            CommandResult("", null, false)
        }

        patcher.before<ChatInputViewModel>(
                "sendMessage",
                Context::class.java,
                MessageManager::class.java,
                MessageContent::class.java,
                List::class.java,
                Boolean::class.javaPrimitiveType!!,
                Function1::class.java
        ) {
            val context = it.args[0] as Context
            val content = it.args[2] as MessageContent
            val plainText = content.textContent
            val attachments = (it.args[3] as List<Attachment<*>>).toMutableList()
            val firstAttachment = try { attachments[0] } catch (t: IndexOutOfBoundsException) { return@before }

            // Check if plugin is OFF
            if (settings.getBool("pluginOff", false)) { return@before }

            // Check if multiple attachments provided
            if (attachments.size > 1) {
                Utils.showToast("UITH: Multiple attachment uploads are not supported!", true)
                return@before
            }

            // Check file type and don't upload if `uploadAllAttachments` is false
            // (There might be a better way to do this lol)
            val mime = MimeTypeMap.getSingleton().getExtensionFromMimeType(context.getContentResolver().getType(firstAttachment.uri)) as String
            if (mime !in arrayOf("png", "jpg", "jpeg", "webp")) {
                if (settings.getBool("uploadAllAttachments", false) == false) {
                    return@before
                }
            }

            // Don't try to upload if no sxcu config given
            val sxcuConfig = settings.getString("sxcuConfig", null)
            if (sxcuConfig == null) {
                LOG.debug("sxcuConfig not provided, skipping upload...")
                return@before
            }
            val configData = GsonUtils.fromJson(sxcuConfig, Config::class.java)
            //val file = toLocalAttachment(attachments[0])
            val json = newUpload(File(firstAttachment.data.toString()), configData, LOG)

            // match URL from regex
            val url = try {
                val matcher = pattern.matcher(json)
                matcher.find()
                matcher.group()
            } catch (ex: Throwable) {
                Utils.showToast("UITH: An error occurred, check debug logs", true)
                LOG.error(ex)
                return@before
            }

            // Send message with the URL received from host
            content.set("$plainText\n$url")
            it.args[2] = content
            it.args[3] = emptyList<Attachment<*>>()
            return@before
        }

    }

    override fun stop(ctx: Context) {
        patcher.unpatchAll()
        commands.unregisterAll()
    }

}