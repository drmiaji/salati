package com.salati.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.salati.theme.AlifThemes
import com.salati.theme.Black
import com.salati.theme.Primary
import com.salati.theme.White

@Composable
fun TextBody(
    modifier: Modifier? = Modifier,
    text: String,
    textColor: Color? = null,
    textStyle: TextStyle? = null,
    textAlign: TextAlign? = null,
) {
    Text(
        modifier = modifier ?: Modifier,
        text = text,
        color = textColor ?: if (isSystemInDarkTheme()) White else Black,
        textAlign = textAlign,
        style = textStyle ?: AlifThemes.TextStyles.body,
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewTextBody() {
    MaterialTheme {
        TextBody(text = "Add new schedule", textColor = Primary)
    }
}