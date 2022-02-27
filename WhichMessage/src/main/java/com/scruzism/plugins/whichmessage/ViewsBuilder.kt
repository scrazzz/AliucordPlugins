package com.scruzism.plugins.whichmessage

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.aliucord.Constants
import com.aliucord.utils.MDUtils.parser
import com.discord.models.message.Message
import com.discord.models.user.CoreUser
import com.discord.stores.StoreGuilds
import com.discord.stores.StoreStream
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.textprocessing.MessageParseState
import com.discord.utilities.textprocessing.MessageRenderContext
import com.discord.utilities.textprocessing.node.EditedMessageNode
import com.facebook.drawee.span.DraweeSpanStringBuilder
import com.lytefast.flexinput.R


class ViewsBuilder(
        context: Context,
        message: Message
) {
    private val ctx = context
    private val msg = message
    private val author = CoreUser(msg.author)
    private val guildStore: StoreGuilds = StoreStream.getGuilds()
    private val guildId = StoreStream.getGuildSelected().selectedGuildId

    private val nodes = parser.parse(msg.content, MessageParseState.`access$getInitialState$cp`())
    private val builder = DraweeSpanStringBuilder()

    private fun getNameToDisplay(): String? {
        return guildStore.getMember(guildId, author.id)?.nick
    }

    private fun getColor(): Int? {
        val color = guildStore.getMember(guildId, author.id)?.color
        return if (color == Color.BLACK) null; else color
    }

    fun getViews(): List<View> {
        if (msg.editedTimestamp != null) nodes.add(EditedMessageNode(ctx))
        for (node in nodes) node.render(builder, MessageRenderContext(ctx, msg.id, true))

        val views = mutableListOf<View>(
                TextView(ctx)
                        .apply {
                            text = getNameToDisplay() ?: author.username
                            typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_medium)
                            textSize = 17.0f
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            setTextColor(getColor() ?: ColorCompat.getThemedColor(ctx, R.b.colorTextNormal))
                        },
        )

        if (msg.content != "") views.add(
                TextView(ctx)
                        .apply {
                            text = builder
                            typeface = ResourcesCompat.getFont(ctx, Constants.Fonts.whitney_medium)
                            textSize = 14.0f
                            maxLines = 3
                            ellipsize = TextUtils.TruncateAt.END
                            setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorTextNormal))
                        }
        )

        if (msg.attachments.size > 0 || msg.embeds.size > 0) views.add(
                ImageView(ctx)
                        .apply {
                            val drawable = ContextCompat.getDrawable(ctx, R.e.ic_image_library_24dp)
                            drawable!!.mutate().setTint(ColorCompat.getThemedColor(ctx, R.b.colorTextMuted))
                            setImageDrawable(drawable)
                        }
        )

        return views
    }
}