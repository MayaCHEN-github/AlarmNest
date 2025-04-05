package edu.cuhk.csci3310.csci3310project.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import edu.cuhk.csci3310.csci3310project.R
import edu.cuhk.csci3310.csci3310project.screen.uiComponent.ComparisonTextField
import edu.cuhk.csci3310.csci3310project.screen.uiComponent.MotivationalQuotes
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTypingScreen(navController: NavController) {
    val context = LocalContext.current
    var isInputCorrect by remember { mutableStateOf(false) }
    
    // 随机选择一个励志小语
    val randomQuote = remember {
        MotivationalQuotes.quotes.random()
    }
    
    CSCI3310ProjectTheme{
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Typing Alarm",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                painter = painterResource(id = R.drawable.arrow_back_ios_24px),
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                Column(
                    modifier = Modifier.imePadding()
                ) {
                    Button(
                        onClick = { 
                            if (isInputCorrect) {
                                navController.navigate("alarm_off_screen")
                            } else {
                                Toast.makeText(context, "输入不正确，请重新输入", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 0.dp)
                            .padding(top = 0.dp, bottom = 30.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(
                            text = "DONE TYPING!",
                            fontSize = 15.sp,
                            fontFamily = FontFamily.Default
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Top
            ) {
                Button(
                    onClick = { /*这个按钮没有任何功能*/ },
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .height(28.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "TYPE THIS!",
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.size(5.dp))

                ComparisonTextField(
                    promptText = randomQuote,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                    fontSize = 20,
                    onInputStateChange = { correct -> isInputCorrect = correct }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewAlarmTypingScreen() {
    CSCI3310ProjectTheme {
        AlarmTypingScreen(navController = rememberNavController())
    }
}