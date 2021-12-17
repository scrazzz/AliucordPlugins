package com.scruzism.plugins

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import com.aliucord.CollectionUtils

import com.aliucord.Utils
import com.aliucord.Logger
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.annotations.AliucordPlugin
import com.discord.api.commands.ApplicationCommandType
import com.aliucord.patcher.Hook
import com.discord.databinding.WidgetChatListActionsBinding
import com.discord.utilities.textprocessing.node.EditedMessageNode
import com.discord.utilities.view.text.SimpleDraweeSpanTextView
import com.discord.widgets.chat.list.WidgetChatList
import com.discord.widgets.chat.list.actions.WidgetChatListActions
import com.discord.widgets.chat.list.adapter.WidgetChatListAdapterItemMessage
import com.discord.widgets.chat.list.entries.MessageEntry
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.lytefast.flexinput.R

import java.lang.reflect.Field
import java.util.regex.Pattern

private fun translateMessage(bottomText: String): String {
    return Bottom.decodeString(bottomText)
}

private fun translateMessageToBottom(text: String): String {
    return Bottom.encodeString(text)
}

private val messageLoggerEditedRegex = Pattern.compile("(?:.+ \\(.+: .+\\)\\n)+(.+)\$")

private fun DraweeSpanStringBuilder.setTranslated(text: String, context: Context, log: Logger) {
    val contentStartIndex = messageLoggerEditedRegex.matcher(this.toString()).let {
        if (it.find()) {
            it.start(1)
        } else 0
    }
    this.replace(contentStartIndex, contentStartIndex + text.length, text)
    log.info("setTrans text: $text")
    val textEnd = this.length
    this.append(" (translated: From Bottom")
    this.setSpan(RelativeSizeSpan(0.75f), textEnd, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    if (textEnd != this.length) {
        this.setSpan(EditedMessageNode.Companion.`access$getForegroundColorSpan`(EditedMessageNode.Companion, context),
                textEnd, this.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

@AliucordPlugin
class AliuBottom : Plugin() {

    lateinit var pluginIcon: Drawable
    private val LOG = Logger("AliuBottom")
    private var chatList: WidgetChatList? = null
    private val translatedMessages = mutableMapOf<Long, String>()

    override fun load(ctx: Context) {
        pluginIcon = ContextCompat.getDrawable(ctx, R.e.ic_locale_24dp)!!
    }

    private fun patchProcessMessageText() {
        patcher.patch(WidgetChatList::class.java.getDeclaredConstructor(), Hook {
            chatList = it.thisObject as WidgetChatList
        })

        val mDraweeStringBuilder: Field = SimpleDraweeSpanTextView::class.java.getDeclaredField("mDraweeStringBuilder").apply {
            isAccessible = true
        }
        patcher.patch(WidgetChatListAdapterItemMessage::class.java, "processMessageText", arrayOf(SimpleDraweeSpanTextView::class.java, MessageEntry::class.java), Hook {
            val messageEntry = it.args[1] as MessageEntry
            val message = messageEntry.message ?: return@Hook
            val id = message.id
            val translated = translatedMessages[id] ?: return@Hook
            LOG.info("translated: $translated")
            if (translated != translateMessage(message.content)) {
                translatedMessages.remove(id)
                return@Hook
            }
            val textView = it.args[0] as SimpleDraweeSpanTextView
            val builder = mDraweeStringBuilder[textView] as DraweeSpanStringBuilder? ?: return@Hook
            val context = textView.context
            builder.setTranslated(translated, context, LOG)
            textView.setDraweeSpanStringBuilder(builder)
        })
    }

    private fun patchMessageContextMenu() {
        val viewId = View.generateViewId()
        val messageContextMenu = WidgetChatListActions::class.java
        val getBinding = messageContextMenu.getDeclaredMethod("getBinding").apply { isAccessible = true }

        patcher.patch(messageContextMenu.getDeclaredMethod("configureUI", WidgetChatListActions.Model::class.java), Hook {
            val menu = it.thisObject as WidgetChatListActions
            val binding = getBinding.invoke(menu) as WidgetChatListActionsBinding
            val translateButton = binding.a.findViewById<TextView>(viewId)
            translateButton.setOnClickListener { _ ->
                val message = (it.args[0] as WidgetChatListActions.Model).message
                var response: String
                Utils.threadPool.execute {
                    try {
                        response = translateMessage(message.content)
                        LOG.info("resp: $response")
                        translatedMessages[message.id] = response
                        if (chatList != null) {
                            val adapter = WidgetChatList.`access$getAdapter$p`(chatList)
                            val data = adapter.internalData
                            val i = CollectionUtils.findIndex(data) { m ->
                                m is MessageEntry && m.message.id == message.id
                            }
                            if (i != -1) adapter.notifyItemChanged(i)
                        }
                        Utils.showToast("Translated bottom message")
                        menu.dismiss()
                    }
                    catch (t: Throwable) {
                        LOG.error(t)
                        Utils.showToast("Could not translate bottom :(", true)
                        return@execute
                    }
                }
            }
        })

        patcher.patch(messageContextMenu, "onViewCreated", arrayOf(View::class.java, Bundle::class.java), Hook {
            val linearLayout = (it.args[0] as NestedScrollView).getChildAt(0) as LinearLayout
            val context = linearLayout.context
            linearLayout.addView(TextView(context, null, 0, R.i.UiKit_Settings_Item_Icon).apply {
                id = viewId
                text = "Translate Bottom"
                setCompoundDrawablesRelativeWithIntrinsicBounds(pluginIcon, null, null, null)
            })
        })
    }


    override fun start(ctx: Context) {
        patchMessageContextMenu()
        patchProcessMessageText()

        val args = listOf(
                Utils.createCommandOption(
                        ApplicationCommandType.STRING,
                        "text",
                        "Text to translate to bottom",
                        required = true
                ),
                Utils.createCommandOption(
                        ApplicationCommandType.BOOLEAN,
                        "send",
                        "Send translated bottom message to chat",
                )
        )
        commands.registerCommand("aliubottom", "Translate text to Bottom", args) {
            val text = it.getRequiredString("text")
            val send = it.getBoolOrDefault("send", false)

            val translatedBottom = translateMessageToBottom(text)
            try {
                return@registerCommand CommandsAPI.CommandResult(translatedBottom, null, send)
            }
            catch (t: Throwable) {
                LOG.error(t)
                return@registerCommand CommandsAPI.CommandResult("an err occrd", null, false)
            }
        }
    }

    override fun stop(ctx: Context) {
        commands.unregisterAll()
        patcher.unpatchAll()
    }

}