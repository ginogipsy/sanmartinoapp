package com.ginogipsy.sanmartinoapp.i18n

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import com.ginogipsy.sanmartinoapp.data.model.Language
import java.util.Locale

val LocalLanguage = compositionLocalOf<MutableState<Language>> {
    error("LocalLanguage not provided")
}

fun defaultLanguageFromLocale(systemTag: String): Language =
    if (systemTag.startsWith("it", ignoreCase = true)) Language.IT else Language.EN

fun mutableLanguageState(initial: Language): MutableState<Language> =
    mutableStateOf(initial)

@Composable
@ReadOnlyComposable
fun localizedStringResource(@StringRes id: Int): String {
    val lang = LocalLanguage.current.value
    val locale = if (lang == Language.IT) Locale.ITALIAN else Locale.ENGLISH
    val context = LocalContext.current
    val config = Configuration(context.resources.configuration).apply {
        setLocale(locale)
    }
    return context.createConfigurationContext(config).resources.getString(id)
}

@Composable
@ReadOnlyComposable
fun localizedStringResource(@StringRes id: Int, vararg formatArgs: Any): String {
    val lang = LocalLanguage.current.value
    val locale = if (lang == Language.IT) Locale.ITALIAN else Locale.ENGLISH
    val context = LocalContext.current
    val config = Configuration(context.resources.configuration).apply {
        setLocale(locale)
    }
    return context.createConfigurationContext(config).resources.getString(id, *formatArgs)
}
