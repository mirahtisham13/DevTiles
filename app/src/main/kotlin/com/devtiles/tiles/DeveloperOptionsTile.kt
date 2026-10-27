package com.devtiles.tiles

import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class DeveloperOptionsTile : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        toggle()
    }

    private fun isEnabled(): Boolean {
        return try {
            Settings.Global.getInt(
                contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) == 1
        } catch (e: Exception) {
            false
        }
    }

    private fun toggle() {
        try {
            val newValue = if (isEnabled()) 0 else 1
            Settings.Global.putInt(
                contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                newValue
            )
            updateTile()
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "Permission missing. Run: adb shell pm grant com.devtiles android.permission.WRITE_SECURE_SETTINGS",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateTile() {
        qsTile?.let { tile ->
            val enabled = isEnabled()
            tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = null
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                tile.stateDescription = null
            }
            tile.updateTile()
        }
    }
}
