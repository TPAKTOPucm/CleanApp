package com.example.cleanapp.widget

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object MusicWidgetState {
    val trackTitleKey = stringPreferencesKey("music_widget_track_title")
    val isPlayingKey = booleanPreferencesKey("music_widget_is_playing")
}