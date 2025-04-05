package edu.cuhk.csci3310.csci3310project.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.CenterAlignedTopAppBar
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
import edu.cuhk.csci3310.csci3310project.R
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmOffScreen(previewScale: Float? = null){
    CSCI3310ProjectTheme {
        Scaffold(

        ) { paddingValues ->
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
                    onClick = { /*TODO*/ },
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
    AlarmOffScreen(previewScale = 1f)
}