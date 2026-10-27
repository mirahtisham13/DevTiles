package com.devtiles.tiles

import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

class PrivateDnsTile : TileService() {

    companion object {
        private const val PRIVATE_DNS_MODE = "private_dns_mode"
        private const val PRIVATE_DNS_SPECIFIER = "private_dns_specifier"
        private const val MODE_OFF = "off"
        private const val MODE_AUTO = "opportunistic"
        private const val MODE_CUSTOM = "hostname"
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        toggle()
    }

    private fun getCurrentMode(): String {
        return Settings.Global.getString(contentResolver, PRIVATE_DNS_MODE) ?: MODE_OFF
    }

    private fun getCustomHostname(): String? {
        return Settings.Global.getString(contentResolver, PRIVATE_DNS_SPECIFIER)
            ?.takeIf { it.isNotBlank() }
    }

    private fun toggle() {
        try {
            val currentMode = getCurrentMode()
            val hostname = getCustomHostname()

            when {
                // Currently ON with custom DNS → turn OFF
                currentMode == MODE_CUSTOM -> {
                    Settings.Global.putString(contentResolver, PRIVATE_DNS_MODE, MODE_OFF)
                }
                // Currently OFF or AUTO → try to switch to custom
                hostname != null -> {
                    Settings.Global.putString(contentResolver, PRIVATE_DNS_MODE, MODE_CUSTOM)
                }
                // No custom DNS configured → inform user, toggle between OFF and AUTO
                else -> {
                    Toast.makeText(
                        this,
                        "No custom DNS set. Go to Settings → Network → Private DNS to configure one.",
                        Toast.LENGTH_LONG
                    ).show()
                    val newMode = if (currentMode == MODE_OFF) MODE_AUTO else MODE_OFF
                    Settings.Global.putString(contentResolver, PRIVATE_DNS_MODE, newMode)
                }
            }
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
            val mode = getCurrentMode()
            tile.state = if (mode == MODE_CUSTOM || mode == MODE_AUTO) {
                Tile.STATE_ACTIVE
            } else {
                Tile.STATE_INACTIVE
            }
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
