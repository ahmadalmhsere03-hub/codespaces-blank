package com.galaxytv.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
internal fun StartupSplashScreen(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.logo_login),
        contentDescription = stringResource(R.string.logo_description),
        contentScale = ContentScale.Crop,
        modifier = modifier.fillMaxSize(),
    )
}
