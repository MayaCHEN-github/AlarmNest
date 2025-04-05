package edu.cuhk.csci3310.csci3310project.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


val mPlusRoundedFont = FontFamily(
    Font(resId = edu.cuhk.csci3310.csci3310project.R.font.mplusrounded1c_bold, FontWeight.Bold)
)

@Composable
fun ComparisonTextField(
    modifier: Modifier = Modifier,
    promptText: String,
    initialText: String = "",
    fontWeight: FontWeight = FontWeight.Bold,
    fontSize: Int = 32,
    defaultColor: Color = Color.White,
    correctColor: Color = Color(0xFF4CAF50),
    wrongColor: Color = Color.Red,
    onValueChange: (String) -> Unit = {}
) {
    var inputText by remember { mutableStateOf(initialText) }
    val isTooLong = inputText.length > promptText.length
    val lineHeight = (fontSize * 1.25).sp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        // 底层提示文本
        Text(
            text = promptText,
            color = defaultColor,
            fontFamily = mPlusRoundedFont,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            style = LocalTextStyle.current,
            modifier = Modifier.align(Alignment.TopStart),
            softWrap = true,
            maxLines = Int.MAX_VALUE,
            lineHeight = lineHeight
        )

        // 上层输入文本 - 带颜色对照
        BasicTextField(
            value = inputText,
            onValueChange = { newValue ->
                val truncatedValue = newValue.take(promptText.length)
                inputText = truncatedValue
                onValueChange(truncatedValue)
            },
            textStyle = LocalTextStyle.current.copy(
                fontFamily = mPlusRoundedFont,
                fontWeight = fontWeight,
                fontSize = fontSize.sp,
                color = Color.Transparent,
                lineHeight = lineHeight
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box {
                    Text(
                        text = createComparisonText(
                            input = inputText,
                            prompt = promptText,
                            correctColor = correctColor,
                            wrongColor = wrongColor
                        ),
                        fontFamily = mPlusRoundedFont,
                        fontWeight = fontWeight,
                        fontSize = fontSize.sp,
                        color = Color.Unspecified,
                        softWrap = true,
                        maxLines = Int.MAX_VALUE,
                        lineHeight = lineHeight
                    )
                    innerTextField()
                }
            }
        )

    }
}

private fun createComparisonText(
    input: String,
    prompt: String,
    correctColor: Color,
    wrongColor: Color
): AnnotatedString {
    return buildAnnotatedString {
        val minLength = minOf(input.length, prompt.length)

        for (i in 0 until minLength) {
            val color = if (input[i] == prompt[i]) {
                correctColor
            } else {
                wrongColor
            }
            withStyle(SpanStyle(color = color)) {
                append(input[i])
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun ComparisonTextFieldPreviews1() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        // 1. 空输入状态
        Text("1. 空输入状态")
        ComparisonTextField(
            promptText = "Please type here"
        )

        // 2. 完全匹配状态
        Text("2. 完全匹配状态")
        ComparisonTextField(
            promptText = "Hello",
            initialText = "Hello"
        )

        // 3. 部分匹配状态
        Text("3. 部分匹配状态")
        ComparisonTextField(
            promptText = "Hello",
            initialText = "Hal"
        )

        // 4. 输入过长状态
        Text("4. 输入过长状态")
        ComparisonTextField(
            promptText = "Hello World",
            initialText = "Hello World Extra "
        )

        // 5. 大小写混合状态
        Text("5. 大小写混合状态")
        ComparisonTextField(
            promptText = "Hello",
            initialText = "HeLLo"
        )

        // 6. 特殊字符状态
        Text("6. 特殊字符状态")
        ComparisonTextField(
            promptText = "Pass#123",
            initialText = "Pass@123"
        )

        // 7. 长文本状态
        Text("7. 长文本状态")
        ComparisonTextField(
            promptText = "Hello World, this is a long long long long long text",
            initialText = "H"
        )

        // 8. 错误长文本状态
        Text("8. 错误文本状态")
        ComparisonTextField(
            promptText = "Hello World, this is a long long long long long text",
            initialText = "Hello World, this is a long long long long long text this is a long long long long long text"
        )

        // 9. 自定义字体大小
        Text("9. 自定义字体大小")
        ComparisonTextField(
            promptText = "Custom Font Size",
            initialText = "Custom",
            fontSize = 20
        )
    }
}
