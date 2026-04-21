package pol.rubiano.docdimtool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import pol.rubiano.docdimtool.ui.screens.MainScreen
import pol.rubiano.docdimtool.ui.theme.DocDimToolTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DocDimToolTheme {
                MainScreen()
            }
        }
    }
}