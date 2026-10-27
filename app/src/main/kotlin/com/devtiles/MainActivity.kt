package com.devtiles

import android.app.StatusBarManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.devtiles.databinding.ActivityMainBinding
import com.devtiles.tiles.DeveloperOptionsTile
import com.devtiles.tiles.PrivateDnsTile
import com.devtiles.tiles.UsbDebuggingTile
import com.devtiles.tiles.NetworkModeTile
import com.devtiles.tiles.ManageApplicationsTile
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val adbCommand =
        "adb shell pm grant com.devtiles android.permission.WRITE_SECURE_SETTINGS"

    private var isHelpExpanded = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        updateRootStatus()
        updatePermissionStatus()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updateRootStatus()
        updatePermissionStatus()
    }

    private fun setupListeners() {
        binding.helpTooltipBtn.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            showPermissionHelpTooltip(view)
        }

        binding.permissionStatusCard.setOnClickListener {
            binding.adbCommandCard.visibility =
                if (binding.adbCommandCard.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }

        binding.copyCommandBtn.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("ADB Command", adbCommand))
            Toast.makeText(this, "ADB command copied to clipboard", Toast.LENGTH_SHORT).show()
        }

        // Tapping Developer Options card prompts to add tile to control panel
        binding.cardTileDevOptions.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            requestAddTileToControlPanel(
                DeveloperOptionsTile::class.java,
                getString(R.string.tile_developer_options),
                R.drawable.ic_developer_mode
            )
        }

        // Tapping USB Debugging card prompts to add tile to control panel
        binding.cardTileUsb.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            requestAddTileToControlPanel(
                UsbDebuggingTile::class.java,
                getString(R.string.tile_usb_debugging),
                R.drawable.ic_usb
            )
        }

        // Tapping Private DNS card prompts to add tile to control panel
        binding.cardTileDns.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            requestAddTileToControlPanel(
                PrivateDnsTile::class.java,
                getString(R.string.tile_private_dns),
                R.drawable.ic_dns
            )
        }

        // Tapping Network Mode card prompts to add tile to control panel
        binding.cardTileNetwork.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            requestAddTileToControlPanel(
                NetworkModeTile::class.java,
                getString(R.string.tile_network_mode),
                R.drawable.ic_network_mode
            )
        }

        // Tapping Manage Apps card prompts to add tile to control panel
        binding.cardTileManageApps.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            requestAddTileToControlPanel(
                ManageApplicationsTile::class.java,
                getString(R.string.tile_manage_apps),
                R.drawable.ic_manage_apps
            )
        }

        binding.helpSectionToggle.setOnClickListener {
            isHelpExpanded = !isHelpExpanded
            binding.helpSectionContent.visibility = if (isHelpExpanded) View.VISIBLE else View.GONE
            binding.helpExpandIcon.rotation = if (isHelpExpanded) 0f else 180f
        }
    }

    /**
     * Prompts the user to add the Quick Settings tile directly to their Control Panel.
     * Uses Android 13+ (API 33+) native StatusBarManager.requestAddTileService,
     * with an informative guidance dialog fallback for earlier versions.
     */
    private fun requestAddTileToControlPanel(
        tileClass: Class<*>,
        tileDisplayName: String,
        iconResId: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val statusBarManager = getSystemService(StatusBarManager::class.java)
            if (statusBarManager == null) {
                showManualAddDialog(tileDisplayName)
                return
            }

            val componentName = ComponentName(this, tileClass)
            val icon = Icon.createWithResource(this, iconResId)

            try {
                statusBarManager.requestAddTileService(
                    componentName,
                    tileDisplayName,
                    icon,
                    mainExecutor
                ) { statusCode ->
                    when (statusCode) {
                        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED -> {
                            Toast.makeText(
                                this,
                                "$tileDisplayName added to Quick Settings panel!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> {
                            Toast.makeText(
                                this,
                                "$tileDisplayName is already in your Quick Settings panel",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED -> {
                            // User dismissed or canceled
                        }
                    }
                }
            } catch (e: Exception) {
                showManualAddDialog(tileDisplayName)
            }
        } else {
            showManualAddDialog(tileDisplayName)
        }
    }

    private fun showManualAddDialog(tileName: String) {
        val message = "To add " + tileName + " to your Quick Settings panel:\n\n" +
            "1. Pull down twice from the top of your screen to open Quick Settings.\n" +
            "2. Tap the Edit (pencil) button.\n" +
            "3. Find '" + tileName + "' under DevTiles.\n" +
            "4. Drag it up into your active tiles."

        MaterialAlertDialogBuilder(this)
            .setTitle("Add " + tileName + " to Control Panel")
            .setMessage(message)
            .setPositiveButton("Got It", null)
            .show()
    }

    private fun hasWriteSecureSettings(): Boolean {
        return checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS") ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun updatePermissionStatus() {
        if (hasWriteSecureSettings()) {
            binding.permissionStatusCard.setBackgroundResource(R.drawable.bg_neu_card_granted)
            binding.permissionStatusIcon.text = "✓"
            binding.permissionStatusIcon.setTextColor(ContextCompat.getColor(this, R.color.neu_success))
            binding.permissionStatusTitle.text = "Permission Granted"
            binding.permissionStatusDesc.text = "All tiles are ready to use."
            binding.adbCommandCard.visibility = View.GONE
        } else {
            binding.permissionStatusCard.setBackgroundResource(R.drawable.bg_neu_card_warning)
            binding.permissionStatusIcon.text = "!"
            binding.permissionStatusIcon.setTextColor(ContextCompat.getColor(this, R.color.neu_warning))
            binding.permissionStatusTitle.text = "Permission Required"
            binding.permissionStatusDesc.text = "Run the ADB command below to enable all tiles."
            binding.adbCommandCard.visibility = View.VISIBLE
        }
    }

    private fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        return paths.any { java.io.File(it).exists() } ||
            (Build.TAGS != null && Build.TAGS.contains("test-keys"))
    }

    private fun updateRootStatus() {
        if (isDeviceRooted()) {
            binding.rootStatusDot.setBackgroundResource(R.drawable.bg_neu_status_dot_green)
            binding.rootStatusChip.text = "ROOTED"
            binding.rootStatusChip.setTextColor(ContextCompat.getColor(this, R.color.neu_success))
        } else {
            binding.rootStatusDot.setBackgroundResource(R.drawable.bg_neu_status_dot_red)
            binding.rootStatusChip.text = "UNROOTED"
            binding.rootStatusChip.setTextColor(ContextCompat.getColor(this, R.color.neu_text_secondary))
        }
    }

    private var tooltipPopup: PopupWindow? = null

    private fun showPermissionHelpTooltip(anchorView: View) {
        if (tooltipPopup?.isShowing == true) {
            tooltipPopup?.dismiss()
            tooltipPopup = null
            return
        }

        val tooltipView = layoutInflater.inflate(R.layout.view_permission_tooltip, null)
        val tooltipText = tooltipView.findViewById<TextView>(R.id.tooltipText)
        val tooltipContainer = tooltipView.findViewById<View>(R.id.tooltipContainer)

        val linkText = "Learn how to grant permission."
        val spannable = SpannableString(linkText)
        spannable.setSpan(UnderlineSpan(), 0, linkText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tooltipText.text = spannable
        tooltipText.paintFlags = tooltipText.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        val openLinkAction = View.OnClickListener {
            tooltipPopup?.dismiss()
            tooltipPopup = null
            try {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/mirahtisham13/DevTiles")
                )
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Could not open browser", Toast.LENGTH_SHORT).show()
            }
        }

        tooltipText.setOnClickListener(openLinkAction)
        tooltipContainer.setOnClickListener(openLinkAction)

        val popup = PopupWindow(
            tooltipView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            isTouchable = true
            elevation = 16f
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setOnDismissListener {
                tooltipPopup = null
            }
        }

        tooltipPopup = popup

        tooltipView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val popupHeight = tooltipView.measuredHeight

        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val anchorY = location[1]

        if (anchorY > popupHeight + 120) {
            popup.showAsDropDown(anchorView, 0, -(anchorView.height + popupHeight + 16))
        } else {
            popup.showAsDropDown(anchorView, 0, 16)
        }
    }
}
