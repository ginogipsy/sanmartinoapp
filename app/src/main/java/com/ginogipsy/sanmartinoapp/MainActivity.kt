package com.ginogipsy.sanmartinoapp

import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.ginogipsy.sanmartinoapp.i18n.LocalLanguage
import com.ginogipsy.sanmartinoapp.i18n.defaultLanguageFromLocale
import com.ginogipsy.sanmartinoapp.i18n.mutableLanguageState
import com.ginogipsy.sanmartinoapp.navigation.SanMartinoNavGraph
import com.ginogipsy.sanmartinoapp.ui.theme.SanMartinoAppTheme

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

                CompositionLocalProvider(LocalLanguage provides languageState) {
                    val navController = rememberNavController()
                    SanMartinoNavGraph(navController = navController)
                }
            }
        }
    }
}
