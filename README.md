# DevTiles 🛠️

**DevTiles** is a sleek, modern utility for Android that adds essential developer toggles and shortcuts directly to your Quick Settings panel.

Designed with a clean neumorphic aesthetic, DevTiles gives developers and power users one-tap access to frequently used system controls without constantly digging through Android Settings.

---

## ⚡ Available Quick Settings Tiles

| Tile | Description | Requires Secure Permission |
| :--- | :--- | :---: |
| **Developer Options** | Toggle Developer Options on/off directly from Quick Settings | Yes |
| **USB Debugging** | Quickly toggle ADB USB debugging | Yes |
| **Private DNS** | Cycle between Private DNS modes (*Off*, *Automatic*, *Strict/Custom*) | Yes |
| **Network Mode** | Open the preferred network type and band settings | No |
| **Manage Apps** | Direct shortcut to the installed applications list | No |

---

## How to Grant ADB Permission

DevTiles requires the "WRITE_SECURE_SETTINGS" permission to enable its controls. Choose either method below.

### Method 1: Using a Laptop

1. Enable Developer Options and USB Debugging on your phone.
2. Download Android Platform Tools from the official website: https://developer.android.com/tools/releases/platform-tools
3. Extract the downloaded ZIP file.
4. Open a terminal inside the extracted "platform-tools" folder.
5. Connect your phone to the laptop using a USB cable and approve the USB debugging prompt.
6. Run:

```bash
./adb devices
./adb shell pm grant com.devtiles android.permission.WRITE_SECURE_SETTINGS
```

> **Note:** On Windows, use `adb` instead of `./adb` in the commands.

7. Reopen DevTiles and verify that permission is granted.

---

### Method 2: Using LADB on Android

Download LADB: https://github.com/Jobians/ladb-apk-download/releases/download/v2.6/LADB-v2.6.apk

1. Connect your phone to a Wi-Fi network. Wireless Debugging requires Wi-Fi.
2. Enable Developer Options on your phone.
3. Open Settings → Developer Options → Wireless Debugging and enable it.
4. Tap Pair device with pairing code.
5. Note the pairing code and the port displayed in the IP address and port. The port is the number after the last colon (":"), usually the last five digits.
6. Open LADB and enter the pairing port and pairing code in the appropriate fields. You do not need to enter the IP address.
7. After pairing, let LADB establish its connection. If required, use the separate connection port displayed on the main Wireless Debugging screen.
8. Once the LADB shell opens, execute:

```bash
pm grant com.devtiles android.permission.WRITE_SECURE_SETTINGS
```

9. Reopen DevTiles and verify that permission is granted.

> **Important:** The pairing port and connection port are different. Use the pairing port with the pairing code during pairing, and the connection port when establishing the wireless ADB connection if prompted. No root access or laptop is required for the LADB method.

---

## 📲 How to Add Tiles to Quick Settings

### Android 13+ (API 33+)
1. Open the **DevTiles** app.
2. Tap any tile card under **Available Tiles**.
3. Confirm the system prompt to add the tile directly to your Quick Settings panel.

### Manual Addition (All Android Versions)
1. Swipe down twice from the top of the screen to open the full **Quick Settings** panel.
2. Tap the **Edit (pencil)** icon.
3. Scroll down to find the tiles under the **DevTiles** header.
4. Drag and drop your desired tiles into the active tile area.
5. Tap **Done** or back out of the edit screen.

---

## 🏗️ Building from Source

### Prerequisites
- JDK 17
- Android SDK 34
- Gradle 8.7+ (Wrapper included)

### Build Debug APK
```bash
git clone https://github.com/mirahtisham13/DevTiles.git
cd DevTiles
./gradlew assembleDebug
```
The output APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

---

## 📄 License
This project is open-source. Feel free to contribute or report issues!
