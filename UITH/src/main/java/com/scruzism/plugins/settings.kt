package com.scruzism.plugins

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
import com.aliucord.utils.DimenUtils
import com.aliucord.views.Divider

import com.lytefast.flexinput.R
import com.discord.views.CheckedSetting
import com.discord.utilities.color.ColorCompat
import com.discord.utilities.view.text.TextWatcher

class PluginSettings(private val settings: SettingsAPI) : SettingsPage() {

    fun createCheckedSetting(title: String, subtitle: String, setting: String, defValue: Boolean): CheckedSetting {
        val ctx = requireContext()
        return Utils.createCheckedSetting(ctx, CheckedSetting.ViewType.SWITCH, title, subtitle).apply {
            isChecked = settings.getBool(setting, defValue)
            setOnCheckedListener {
                settings.setBool(setting, it)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewBound(view: View?) {
        super.onViewBound(view)
        setActionBarTitle("UITH")
        val ctx = requireContext()
        val p = DimenUtils.defaultPadding

        TextView(ctx).apply {
            text = "• Enter a default host to upload the attachment."
            setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorOnPrimary))
            setPadding(p, p, p, p)
            addView(this)
        }
        val defaultHostInput = TextInput(ctx, "Enter default host")
        val et = defaultHostInput.editText
        et.maxLines = 1
        et.setText(settings.getString("defaultHost", "imgbb"))
        //
        val defButton = Button(ctx).apply {
            text = "Save"
            setOnClickListener {
                settings.setString("defaultHost", et.text.toString().lowercase())
                Utils.showToast("Default host saved.")
            }
        }
        //
        et.addTextChangedListener(object : TextWatcher() {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (!defaultHosts.containsValue(s.toString().lowercase())) {
                    defButton.alpha = 0.5f
                    defButton.isClickable = false
                }
                else {
                    defButton.alpha = 1f
                    defButton.isClickable = true
                }
            }
        })

        val defaultHostHelp = StringBuilder("Supported hosts:\n\n- imgbb\n- 0x0.st\n- sxcu")
        val defaultHostHelpText = TextView(ctx).apply {
            text = defaultHostHelp.toString()
            setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorOnPrimary))
            setPadding(p, p, p, p)
        }

        addView(defaultHostInput)
        addView(defButton)
        addView(defaultHostHelpText)
        addView(Divider(ctx))


        /* SXCU Setting */
        val sxcuInfo = TextView(ctx).apply {
            text = "• Enter a sxcu sub domain to upload the attachment.\n• Only applicable if default host is set as \"sxcu\"."
            setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorOnPrimary))
            setPadding(p, p, p, p)
        }

        val input = TextInput(ctx, "Enter PUBLIC sxcu subdomain")

        val button = Button(ctx).apply {
            text = "Save"
            setOnClickListener {
                settings.setString("sxcuSubdomain", input.editText.text.toString())
                Utils.showToast("Saved.")
            }
        }

        input.apply {
            editText.maxLines = 1
            editText.setText(settings.getString("sxcuSubdomain", null))
            editText.addTextChangedListener(object : TextWatcher() {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (s.isEmpty()) {
                        button.alpha = 0.5f
                        button.isClickable = false
                    }
                    else {
                        button.alpha = 1f
                        button.isClickable = true
                    }
                }
            })
        }

        val helpText = TextView(ctx).apply {
            text = StringBuilder("* Go to https://sxcu.net/domains and select a PUBLIC subdomain.\n")
                    .append("* Then paste the subdomain in the box (e.g: discord-cdn.is-terrible.com)").toString()
            linksClickable = true
            setTextColor(ColorCompat.getThemedColor(ctx, R.b.colorOnPrimary))
            Linkify.addLinks(this, Linkify.WEB_URLS)
            setPadding(p, p, p, p)
        }

        addView(sxcuInfo)
        addView(input)
        addView(button)
        addView(helpText)

        addView(Divider(ctx))

        /* Send by default setting */
        val defaultSend = createCheckedSetting(
                "Send", "Send the attachment to chat by default",
                "defaultSend", false)
        addView(defaultSend)
    }

}