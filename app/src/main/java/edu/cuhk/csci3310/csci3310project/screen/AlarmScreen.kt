package edu.cuhk.csci3310.csci3310project.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cuhk.csci3310.csci3310project.R
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme

@Composable
fun AlarmScreen(
    onStartTask: () -> Unit = {}
){
    CSCI3310ProjectTheme {
        Scaffold () { paddingValues ->
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                var date = "2005.04.16"
                var dayInWeek = "Wednesday"
                var time = "09:20"
                var memo = "Commuting to school"


                Text(
                    text = "$date  $dayInWeek",
                    fontSize = 20.sp
                )

                Text(
                    text = time,
                    fontSize = 70.sp
                )

                Spacer(modifier = Modifier.size(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.alarm_24px),
                    contentDescription = null,
                    modifier = Modifier
                        .size(130.dp)
                )

                Spacer(modifier = Modifier.size(25.dp))

                Text(
                    text = memo,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.size(15.dp))

                Button(
                    onClick = onStartTask,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "START YOUR ALARM TASK!",
                        fontFamily = FontFamily.Default
                    )
                }

            }


        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmScreenPreview() {
    CSCI3310ProjectTheme {
        AlarmScreen()
    }
}