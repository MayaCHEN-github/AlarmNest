package edu.cuhk.csci3310.csci3310project.frontend.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import edu.cuhk.csci3310.csci3310project.R
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.delay

@Composable
fun AlarmOffScreen(
    navController: NavController,
    previewScale: Float? = null,
    onStopAlarm: () -> Unit = {}
){
    CSCI3310ProjectTheme {
        Scaffold{ paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                var startAnimation by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (startAnimation) 1f else 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "scale"
                )

                LaunchedEffect(key1 = true) {
                    startAnimation = true
                    onStopAlarm() // 停止闹铃
                    delay(3000) // 等待2秒
                    navController.navigate("clock_list_screen") {
                        popUpTo(0) { inclusive = true } // 清除所有之前的导航栈
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.check_24px),
                    contentDescription = null,
                    modifier = Modifier
                        .scale(previewScale ?: scale)
                        .size(200.dp)
                )
                Text(
                    text = "Good Job!",
                    fontSize = 26.sp
                )

                Button(
                    onClick = { 
                        onStopAlarm() // 停止闹铃
                        navController.navigate("clock_list_screen") {
                            popUpTo(0) { inclusive = true } // 清除所有之前的导航栈
                        }
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "BACK TO HOME",
                        fontFamily = FontFamily.Default
                    )
                }
            }
        }
    }
}

@Preview(name = "动画结束状态")
@Composable
fun AlarmOffScreenPreviewEnd() {
    AlarmOffScreen(navController = rememberNavController(), previewScale = 1f)
}