# e-Driver License Reader (Macnatic Tape Swipe Reader)

An Android application designed to read and parse data from the magnetic stripe (MSR) on a Thai Driving License. Built using **Kotlin** and **Jetpack Compose** for a modern, responsive user interface.

---

## 🚀 Features

1. **Dual MSR Support**
   - **Sunmi POS Terminals (Built-in MSR):** Binds directly to the Sunmi PaySDK Service (`SunmiPaySdkManager`) to capture track data from the terminal's built-in magnetic card slot (e.g., Sunmi V2 Pro, T2, etc.).
   - **External USB Readers (USB HID MSR):** Intercepts keyboard emulation inputs via `dispatchKeyEvent` for generic USB MSR readers (e.g., MSR90, MagTek) instantly upon connection (Plug-and-Play).

2. **Thai Driving License Parsing**
   - Scans raw Track 1, Track 2, and Track 3 data.
   - Automatically extracts critical metadata:
     - **Citizen ID** (13-digit National ID)
     - **License Number**
     - **First Name & Last Name** (English)
     - **Date of Birth**
     - **Expiration Date**
     - **License Type** (e.g., Personal Car or Personal Motorcycle)

3. **JPEG 2000 (JP2) Photo Support**
   - Locates and decodes holder photos in `.jp2` (JPEG 2000) format matching the Citizen ID (e.g., `1234567890123.jp2`) to display on the result screen.

4. **Floating Log Overlay**
   - Incorporates a floating debug button that overlays real-time system logs over the UI for easy on-device debugging and tracking of swipe events.

---

## 📊 System Flow Diagram

The application processes input from MSR swipes, runs parsing logic, decodes target images, and displays results as outlined below:

```text
[ App Launch ]
        │
        ├──────────────────────────────┐
        ▼                              ▼
(USB HID MSR Swipe)           (Sunmi Terminal MSR)
  - Capture keyboard events     - Bind to Sunmi PaySDK
  - Buffer characters to ENTER  - Start checkCard MSR polling
        │                              │
        └──────────────┬───────────────┘
                       │ (Raw Track 1, 2, 3 data)
                       ▼
            [ Parse Track Data ]
            - Call ThaiDrivingLicenseParser.parse
            - Extract: Citizen ID, Names, DOB, Expiry
                       │
                       ▼
          [ Retrieve & Load Photo ]
            - Search for {citizen_id}.jp2 in storage
            - If found: Decode JP2 to Bitmap
            - If not found: Use default avatar icon
                       │
                       ▼
          [ Display Loading Screen (1.2s) ]
                       │
                       ▼
          [ Display ResultScreen ]
            - Centered key-value columns with watermark
            - Toggle layout to view raw track logs
                       │
                       ▼
            [ Back Button / Reset State ]
            - Press back arrow or bottom Back button
            - Clear licenseData to reset state to scan screen
```

---

## 📂 Project Structure

```text
app/src/main/java/com/example/macnatic_tape_swipe_reader/
├── features/
│   ├── monitor_logging/             # Floating log overlay utility
│   └── msr/
│       ├── models/
│       │   └── ThaiDrivingLicense.kt# License data model
│       └── parsers/
│           └── ThaiDrivingLicenseParser.kt # Parser logic for Track 1, 2, 3
├── services/
│   └── SunmiPaySdkManager.kt        # Sunmi PaySDK service wrapper
└── view/
    ├── components/                  # Composable UI sub-components
    ├── MainActivity.kt              # App entrypoint (USB detection & keyboard dispatch)
    ├── MsrScannerScreen.kt          # Screen state routing
    ├── ResultScreen.kt              # Scanned details result page
    └── FormScreen.kt                # Manual backup input form screen
```

---

## 🛠 System Requirements

- **Android SDK:** Minimum API Level 26 (Android 8.0) or higher.
- **Hardware Support:**
  - For built-in reader: A Sunmi terminal containing a magnetic card slot and the `com.sunmi.pay.hardware_v3` system package installed.
  - For external reader: Any Android device supporting USB OTG with a connected USB MSR reader.

---

## 📷 JP2 Photo Setup

The app searches for holder photos matching the 13-digit Citizen ID (e.g., `1234567890123.jp2`) in the following locations on the device:

1. **App external storage:** `/sdcard/Android/data/com.example.macnatic_tape_swipe_reader/files/{citizen_id}.jp2`
2. **Public Download folder:** `/sdcard/Download/{citizen_id}.jp2`
3. **App assets directory:** Inside the `assets/` folder in the project source.

*Note: If no image is found, a default avatar placeholder is displayed instead.*
