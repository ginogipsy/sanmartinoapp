package com.ginogipsy.sanmartinoapp.i18n

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import com.ginogipsy.sanmartinoapp.data.model.Language

val LocalLanguage = compositionLocalOf<MutableState<Language>> {
    error("LocalLanguage not provided")
}

fun defaultLanguageFromLocale(systemTag: String): Language =
    if (systemTag.startsWith("it", ignoreCase = true)) Language.IT else Language.EN

fun mutableLanguageState(initial: Language): MutableState<Language> =
    mutableStateOf(initial)
