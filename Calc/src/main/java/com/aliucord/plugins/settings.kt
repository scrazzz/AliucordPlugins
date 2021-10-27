/*
 * Copyright (c) 2021 scrazzz
 * Licensed under the MIT License
 */

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
        setActionBarTitle("Calc - Wolfram API")
        val ctx = requireContext()
        val input = TextInput(ctx)
        input.hint = "Enter APPID"
        val editText = input.editText!!
        val button = Button(ctx)
        button.text = "Save"
        button.setOnClickListener {
            settings.setString("appid", editText.text.toString())
            Utils.showToast("Saved.")
            close()
        }
        editText.maxLines = 1
        editText.setText(settings.getString("appid", null))
        editText.addTextChangedListener(object : TextWatcher() {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })
        val helpInfo = StringBuilder("\nGETTING AN APPID\n\n1) Sign up/ Log in:\n")
                .append("- To use this plugin, you must register a Wolfram ID and sign in to the ")
                .append("Wolfram|Alpha Developer Portal: https://developer.wolframalpha.com/portal\n")
                .append("Upon logging in, go to the \"MY APPS\" tab to start creating your first app.\n\n")
                .append("2) Obtaining an AppID:\n- Click the \"Sign up to get your first AppID\" button to start the app creation process. ")
                .append("After a one-time survey about intended usage, the AppID creation dialog will appear. Give your application a name and simple description.\n\n")
                .append("After you have done these steps, copy and paste the AppID in the above box.")
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