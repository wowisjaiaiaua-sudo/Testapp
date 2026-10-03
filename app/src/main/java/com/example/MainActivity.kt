package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.GameViewModel
import com.example.ui.Screen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.IconKitScreen
import com.example.ui.screens.LevelEditorScreen
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Fullscreen immersive mode for arcade experience
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MyApplicationTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF090D1E)
                ) {
                    AppNavigation(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()

    when (val screen = currentScreen) {
        is Screen.MENU -> MenuScreen(viewModel)
        is Screen.LEVEL_SELECT -> LevelSelectScreen(viewModel)
        is Screen.GAME -> GameScreen(
            meta = screen.meta,
            initialObjects = screen.objects,
            isPracticeInitial = screen.isPractice,
            customLevelId = screen.customLevelId,
            viewModel = viewModel
        )
        is Screen.ICON_KIT -> IconKitScreen(viewModel)
        is Screen.LEVEL_EDITOR -> LevelEditorScreen(
            editingLevelId = screen.editingLevelId,
            viewModel = viewModel
        )
        is Screen.STATS -> StatsScreen(viewModel)
    }
}
