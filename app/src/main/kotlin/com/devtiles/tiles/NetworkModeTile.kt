package com.devtiles.tiles

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.devtiles.R

class NetworkModeTile : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        openPhoneInfoScreen()
    }

    private fun updateTile() {
        qsTile?.let { tile ->
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.tile_network_mode)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = null
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                tile.stateDescription = null
            }
            tile.updateTile()
        }
    }

    private fun openPhoneInfoScreen() {
        // Candidate intents to launch Phone Info / RadioInfo / Testing screen
        val candidates = listOf(
            Intent(Intent.ACTION_MAIN).setComponent(
                ComponentName("com.android.phone", "com.android.phone.settings.RadioInfo")
            ),
            Intent(Intent.ACTION_MAIN).setComponent(
                ComponentName("com.android.settings", "com.android.settings.RadioInfo")
            ),
            Intent(Intent.ACTION_MAIN).setComponent(
                ComponentName("com.android.settings", "com.android.settings.TestingSettings")
            )
        )

        var launched = false

        for (intent in candidates) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                val resolveInfo = packageManager.resolveActivity(intent, 0)
                if (resolveInfo != null) {
                    launchIntent(intent)
                    launched = true
                    break
                }
            } catch (e: ActivityNotFoundException) {
                // Try next candidate
            } catch (e: SecurityException) {
                // Protected activity on this OEM, try next or fallback
            } catch (e: Exception) {
                // Ignore and try next candidate
            }
        }

        // If direct hidden Phone Info activity is blocked, try fallback to Mobile Network settings
        if (!launched) {
            val fallbackIntents = listOf(
                Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS),
                Intent(Settings.ACTION_DATA_ROAMING_SETTINGS),
                Intent(Settings.ACTION_WIRELESS_SETTINGS)
            )

            for (fallback in fallbackIntents) {
                fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    val resolveInfo = packageManager.resolveActivity(fallback, 0)
                    if (resolveInfo != null) {
                        launchIntent(fallback)
                        Toast.makeText(
                            this,
                            "Direct Phone Info blocked. Dial *#*#4636#*#* to open manually.",
                            Toast.LENGTH_LONG
                        ).show()
                        launched = true
                        break
                    }
                } catch (e: Exception) {
                    // Try next fallback
                }
            }
        }

        // If all launches failed, notify user gracefully without crashing
        if (!launched) {
            Toast.makeText(
                this,
                "Phone Info screen blocked on this device. Dial *#*#4636#*#* to open manually.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun launchIntent(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
