package edu.cuhk.csci3310.csci3310project.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cuhk.csci3310.csci3310project.R
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTypingScreen(){
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
                        IconButton(onClick = { /* TODO: Add navigation later */ }) {
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
                    TextButton(
                        onClick = { /* TODO: Add action later */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp, vertical = 0.dp)
                            .padding(bottom = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Text Examples",
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Light,
                            )
                        }
                    }
                    Button(
                        onClick = { /* TODO: Add action later */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 0.dp)
                            .padding(top = 0.dp, bottom = 30.dp),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Text(
                            text = "DONE SETTING!",
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
                        text = "EXAMPLE",
                        fontFamily = FontFamily.Default,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.size(5.dp))

                ComparisonTextField(
                    promptText = "The quick brown fox jumps over the lazy dog.",
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                    fontSize = 20
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewAlarmTypingScreen() {
    CSCI3310ProjectTheme {
        AlarmTypingScreen()
    }
}