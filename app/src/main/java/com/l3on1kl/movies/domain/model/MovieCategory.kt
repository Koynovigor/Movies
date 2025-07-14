package com.l3on1kl.movies.domain.model

enum class MovieCategory(val id: Int, val title: String) {
    ACTION(28, "Боевик"),
    COMEDY(35, "Комедия"),
    DRAMA(18, "Драма"),
    HORROR(27, "Ужасы")
}
