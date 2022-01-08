package com.scruzism.plugins

// https://github.com/tsudoko/xshare/blob/master/app/src/main/java/re/flande/xshare/Uploader.kt#L13
data class Config(
        var Name: String?,
        var DestinationType: String?,
        var RequestType: String = "POST",  // imp
        var RequestURL: String?,           // imp
        var FileFormName: String?,         // imp
        var Headers: Map<String, String>?,
        var Arguments: Map<String, String>?,
        // var RegexList: Array<String>?,
        var ResponseType: String?,
        var URL: String?
)
