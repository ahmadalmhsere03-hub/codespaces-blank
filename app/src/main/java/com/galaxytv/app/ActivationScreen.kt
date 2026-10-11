package com.galaxytv.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Night = Color(0xFF080B1C)
private val NeonBlue = Color(0xFF39A7FF)
private val NeonPurple = Color(0xFF9A59FF)

@Composable
internal fun ActivationScreen(
    isLoading: Boolean,
    message: String?,
    onActivate: (String) -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    val panelShape = RoundedCornerShape(18.dp)

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(Color(0xFF0A1230), Night, Color(0xFF140B2A))),
        ),
    ) {
        val compact = maxWidth < 760.dp
        Row(
            modifier = Modifier.fillMaxSize().padding(
                horizontal = if (compact) 20.dp else 56.dp,
                vertical = 16.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (compact) 18.dp else 42.dp),
        ) {
            Column(
                modifier = Modifier.weight(0.9f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher),
                    contentDescription = stringResource(R.string.logo_description),
                    modifier = Modifier.size(if (compact) 96.dp else 128.dp).clip(RoundedCornerShape(24.dp)),
                )
                Text("GALAXY", color = Color(0xFFB8D8FF), fontSize = 29.sp, fontWeight = FontWeight.Light)
                Text("TV", color = NeonPurple, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.brand_subtitle), color = Color(0xFF9BA9D3), fontSize = 13.sp)
            }

            Column(
                modifier = Modifier.weight(1.1f).clip(panelShape)
                    .background(Color(0xE9111730))
                    .border(1.dp, NeonBlue.copy(alpha = 0.7f), panelShape)
                    .padding(horizontal = if (compact) 16.dp else 28.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(stringResource(R.string.login_title), color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.login_prompt), color = Color(0xFFAAB6D5), fontSize = 13.sp)

                Spacer(Modifier.height(1.dp))
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalLayoutDirection provides LayoutDirection.Ltr,
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = toAsciiDigits(it) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.activation_code)) },
                        placeholder = { Text(stringResource(R.string.activation_hint)) },
                        singleLine = true,
                        enabled = !isLoading,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            if (code.isNotEmpty() && !isLoading) onActivate(code)
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonBlue,
                            unfocusedBorderColor = Color(0xFF454C76),
                            focusedLabelColor = NeonBlue,
                            unfocusedLabelColor = Color(0xFF9BA9D3),
                            cursorColor = NeonBlue,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                        ),
                    )
                }

                if (message != null) {
                    Text(message, color = Color(0xFFFF858D), fontSize = 12.sp)
                }

                Button(
                    onClick = { onActivate(code) },
                    enabled = code.isNotEmpty() && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF377EF5),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF30374E),
                        disabledContentColor = Color(0xFF8B94AC),
                    ),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(19.dp),
                            color = Color.White,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.checking))
                    } else {
                        Text(stringResource(R.string.activate), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    "GalaxyTV",
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF65708F),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun toAsciiDigits(value: String): String = buildString {
    value.forEach { character ->
        when (character) {
            in '0'..'9' -> append(character)
            in '\u0660'..'\u0669' -> append(('0'.code + character.code - '\u0660'.code).toChar())
            in '\u06F0'..'\u06F9' -> append(('0'.code + character.code - '\u06F0'.code).toChar())
        }
    }
}
