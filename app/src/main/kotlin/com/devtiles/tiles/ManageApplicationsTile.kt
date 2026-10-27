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

class ManageApplicationsTile : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        openManageApplicationsScreen()
    }

    private fun updateTile() {
        qsTile?.let { tile ->
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.tile_manage_apps)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.subtitle = null
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                tile.stateDescription = null
            }
            tile.updateTile()
        }
    }

    private fun openManageApplicationsScreen() {
        val candidates = listOf(
            // Direct shortcut requested by user: com.android.settings.applications.ManageApplications
            Intent(Intent.ACTION_MAIN).setComponent(
                ComponentName("com.android.settings", "com.android.settings.applications.ManageApplications")
            ),
            // Standard Android Manage Applications Settings
            Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS),
            // Generic Application Settings
            Intent(Settings.ACTION_APPLICATION_SETTINGS),
            // Manage All Applications (Android 11+)
            Intent("android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS")
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
                // Try next candidate
            } catch (e: Exception) {
                // Try next candidate
            }
        }

        if (!launched) {
            Toast.makeText(
                this,
                "Only available for Xiaomi/Redmi/Poco devices.",
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
