# Upload Image To Host (UITH)

**If you have installed this plugin during its development, you may need to delete UITH.json file in /Aliucord/settings folder in order for the new version to work properly.**

## Setting up
If you don't know what sharex is then this plugin isn't for you. Please don't ask how to get a .sxcu file. I assume you know what it is and where to get it from, so let me explain how to use this plugin.

### Adding sxcu file
Use the `/uith add` slash command to save the sxcu file contents. Here's the steps on how to do it:

1. Open your .sxcu file and copy the contents to clipboard.
2. Open Aliucord, type `/uith add` and paste the contents you copied.
![uith_add](https://user-images.githubusercontent.com/70033559/155849071-e984d188-c8b2-40d2-8124-c3206bc41240.jpg)
If everything goes well, you should see this message: ![uith_add_success](https://user-images.githubusercontent.com/70033559/155849188-62911578-467b-432c-ae66-68cb2e009613.jpg)
3. Try testing the plugin by sending an image selected from the attachments tab. You *should* (hopefully) get the link of the image which is sent to chat automatically.

NOTE: Your screen will freeze when you upload an image, it is normal. I will try to fix this in future updates.

NOTE: You can use `/uith current` to see your current configuration settings.

> If you don't get the image URL, join my server. Server invite link is there on the plugin settings page.

-----

## UITH Slash Commands
![uith_cmds](https://user-images.githubusercontent.com/70033559/155849604-7476eeba-92f0-429d-9169-cb10de3ab7b6.jpg)

-----

## UITH settings
![uith_settings](https://user-images.githubusercontent.com/70033559/155849677-8ed16727-dd43-476e-a05f-5b80aa15d966.jpg)

### Regex
Most of the time you don't need to configure this. I have it set so that it should hopefully get the image URL. But if it doesn't get the image URL for you, you can use your own regex to capture the URL. Check the Debug Logs when you upload an image, the whole response returned from your desired image host API is available: ![uith_resp](https://user-images.githubusercontent.com/70033559/155849960-e2512e7c-cc3c-491a-b5de-2a15422ba39c.jpg)

> If you need help with regex, join my server. Server invite link is there on the plugin settings page.

\* Every other settings are straightforward, I don't need to explain it.
