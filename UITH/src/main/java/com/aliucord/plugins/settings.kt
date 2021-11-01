package com.aliucord.plugins

import android.view.View
import android.text.Editable
import android.widget.TextView
import android.text.util.Linkify
import android.annotation.SuppressLint

import com.aliucord.Utils
import com.aliucord.views.Button
import com.aliucord.views.TextInput
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage

import com.lytefast.flexinput.R
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.view.text.TextWatcher

class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {

    @SuppressLint("SetTextI18n")
    override fun onViewBound(view: View?) {
        super.onViewBound(view)
        setActionBarTitle("UITH - sxcu subdomain settings")
        val ctx = requireContext()
        val input = TextInput(ctx)
        input.hint = "Enter PUBLIC sxcu subdomain"
        val editText = input.editText!!
        val button = Button(ctx)
        button.text = "Save"
        button.setOnClickListener {
            settings.setString("sxcuSubdomain", editText.text.toString())
            Utils.showToast("Saved.")
            close()
        }
        editText.maxLines = 1
        editText.setText(settings.getString("sxcuSubdomain", null))
        editText.addTextChangedListener(object : TextWatcher() {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        val helpInfo = StringBuilder("\n\n* Go to https://sxcu.net/domains and select a PUBLIC subdomain.\n\n")
                .append("* Then paste the subdomain in the box (e.g: discord-cdn.is-terrible.com)\n\n")
                .append("* Using a private domain won't work because it would need an API key and ")
                .append("this plugin does not support that as of now.")
        val helpText = TextView(ctx)
        helpText.text = helpInfo.toString()
        helpText.linksClickable = true
        helpText.setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorOnPrimary))
        Linkify.addLinks(helpText, Linkify.WEB_URLS)
        addView(input)
        addView(button)
        addView(helpText)
    }

}