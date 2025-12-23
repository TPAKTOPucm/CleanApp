package com.example.cleanapp.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Like(modifier: Modifier = Modifier, isLiked: Boolean) {
    Icon(
        modifier = modifier,
        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
        contentDescription = "Like icon",
        tint = if (isLiked) Color.Red else Color.Gray
    )
}