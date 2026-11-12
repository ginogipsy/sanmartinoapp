package com.ginogipsy.sanmartinoapp

import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.compose.rememberNavController
import com.ginogipsy.sanmartinoapp.data.model.Language
import com.ginogipsy.sanmartinoapp.i18n.LocalLanguage
import com.ginogipsy.sanmartinoapp.i18n.defaultLanguageFromLocale
import com.ginogipsy.sanmartinoapp.i18n.mutableLanguageState
import com.ginogipsy.sanmartinoapp.navigation.SanMartinoNavGraph
import com.ginogipsy.sanmartinoapp.ui.theme.SanMartinoAppTheme
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SanMartinoAppTheme {
                val systemTag = LocaleList.getDefault()
                    .get(0)?.toLanguageTag().orEmpty()
                val initial = defaultLanguageFromLocale(systemTag)
                val languageState = remember { mutableLanguageState(initial) }

                val currentLang = languageState.value
                val locale = if (currentLang == Language.IT) Locale.ITALIAN else Locale.ENGLISH
                val currentConfig = LocalConfiguration.current
                val currentContext = LocalContext.current

                val localizedConfig = remember(currentConfig, locale) {
                    Configuration(currentConfig).apply {
                        setLocale(locale)
                    }
                }
                val localizedContext = remember(currentContext, localizedConfig) {
                    currentContext.createConfigurationContext(localizedConfig)
                }

                CompositionLocalProvider(
                    LocalConfiguration provides localizedConfig,
                    LocalContext provides localizedContext,
                    LocalLanguage provides languageState,
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.sfondo_legno),
                            contentDescription = "Sfondo legno",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        val navController = rememberNavController()
                        SanMartinoNavGraph(navController = navController)
                    }
                }
            }
        }
    }
}
