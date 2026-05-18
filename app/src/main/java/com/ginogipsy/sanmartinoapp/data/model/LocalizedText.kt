package com.ginogipsy.sanmartinoapp.data.model

enum class Language { IT, EN }

data class LocalizedText(val it: String, val en: String) {
    fun pick(lang: Language): String = if (lang == Language.IT) it else en
}
