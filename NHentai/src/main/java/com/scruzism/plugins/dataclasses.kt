package com.scruzism.plugins

// nhentai info
data class Result(
        val id: Int,
        val media_id: Int,
        val title: Title,
        val images: Images,
        val upload_date: Int,
        val num_pages: Int,
        val num_favorites: Int
) {
    data class Title(
            val pretty: String
    )
    data class Images(val cover: Cover) {
        data class Cover(
                var t: String,
                var h: Int,
                var w: Int
        )
    }
}

// nhentai pages
data class PageData(var media_id: Int, var images: Images) {
    data class Images(var pages: List<Pages>) {
        data class Pages(
                var t: String,
                var h: Int,
                var w: Int
        )
    }
}

// nhentai search
data class SearchResult(val result: List<Result>) {
    data class Result(
            val id: Int,
            val title: com.scruzism.plugins.Result.Title,
            val upload_date: Int,
            val num_pages: Int,
            val num_favorites: Int
    )
}
