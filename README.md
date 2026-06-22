# e-Driver License Reader (Macnatic Tape Swipe Reader)

แอปพลิเคชัน Android สำหรับอ่านข้อมูลจากแถบแม่เหล็ก (Magnetic Stripe Reader - MSR) บนใบอนุญาตขับขี่ของประเทศไทย (Thai Driving License) และนำมาแสดงผลอย่างถูกต้องตามมาตรฐานข้อมูลใบอนุญาตขับขี่ พัฒนาด้วย **Kotlin** และ **Jetpack Compose** สำหรับการออกแบบอินเตอร์เฟสสมัยใหม่

---

## 🚀 คุณสมบัติเด่น (Features)

1. **รองรับอุปกรณ์อ่านแถบแม่เหล็ก 2 ระบบ (Dual MSR Support)**
   - **อุปกรณ์ Sunmi (Built-in MSR):** เชื่อมต่อโดยตรงกับ Sunmi PaySDK Service (`SunmiPaySdkManager`) เพื่อดึงข้อมูลบัตรจากแถบแม่เหล็กในตัวเครื่อง POS (เช่น Sunmi V2 Pro, T2 เป็นต้น)
   - **หัวอ่านภายนอกแบบ USB (External USB MSR):** ดักจับข้อมูลอินพุตคีย์บอร์ดแบบ HID (Human Interface Device) ผ่าน `dispatchKeyEvent` รองรับหัวอ่าน USB MSR ทั่วไป (เช่น MSR90, MagTek) ทันทีที่เชื่อมต่อ (Plug-and-Play)

2. **ระบบถอดรหัสข้อมูลใบขับขี่ไทย (Thai Driving License Parsing)**
   - สแกนข้อมูลดิบจากแถบแม่เหล็กทั้ง **Track 1** และ **Track 2** (รวมถึง **Track 3** หากมี)
   - สกัดข้อมูลออกมาเป็นฟิลด์หลักโดยอัตโนมัติ:
     - **Citizen ID** (เลขประจำตัวประชาชน 13 หลัก)
     - **License Number** (เลขที่ใบขับขี่)
     - **Name** (ชื่อ-นามสกุล ภาษาอังกฤษ)
     - **Date of Birth** (วัน/เดือน/ปี เกิด)
     - **Expiration Date** (วันหมดอายุของบัตร)
     - **License Type** (ประเภทของใบอนุญาตขับขี่ เช่น รถยนต์ส่วนบุคคล หรือรถจักรยานยนต์ส่วนบุคคล)

3. **รองรับการแสดงผลรูปถ่ายจากไฟล์ JPEG 2000 (JP2)**
   - สามารถโหลดและถอดรหัสไฟล์รูปภาพนามสกุล `.jp2` (JPEG 2000) ของเจ้าของบัตรตามรหัส Citizen ID (เช่น `1234567890123.jp2`) เพื่อนำมาแสดงในหน้าโปรไฟล์

4. **Floating Log Overlay (ตัวแสดงบันทึกบันทึกหน้าจอ)**
   - มีปุ่ม Overlay ลอยแสดงผลการทำงานและข้อผิดพลาด (Logs) บนหน้าจอของอุปกรณ์แบบ Real-time เพื่อช่วยในการตรวจสอบแก้ไขปัญหาหน้างาน (Debugging) ได้อย่างสะดวกรวดเร็ว

---

## 📊 แผนผังการทำงาน (System Flow Diagram)

แอปพลิเคชันมีขั้นตอนการดักจับข้อมูลจากแถบแม่เหล็ก คัดแยกประมวลผล ไปจนถึงการถอดรหัสภาพและแสดงผลลัพธ์ ดังแผนภาพด้านล่างนี้:

```text
[ เริ่มต้นแอปพลิเคชัน ]
        │
        ├──────────────────────────────┐
        ▼                              ▼
(สแกนผ่านหัวอ่าน USB MSR)      (รูดผ่านตัวเครื่อง Sunmi)
  - ดักจับคีย์บอร์ดอินพุต          - ผูกระบบ Sunmi PaySDK
  - ทำการเก็บ Buffer จนพบ ENTER    - เปิดบริการ MSR Polling
        │                              │
        └──────────────┬───────────────┘
                       │ (ได้ข้อมูลดิบ Track 1, 2, 3)
                       ▼
         [ ประมวลผลข้อมูลแถบแม่เหล็ก ]
         - ส่งต่อให้ ThaiDrivingLicenseParser.parse
         - สกัดข้อมูล: Citizen ID, ชื่อ-นามสกุล, วันเกิด, วันหมดอายุ
                       │
                       ▼
           [ โหลดรูปถ่ายผู้ถือบัตร ]
         - ค้นหาไฟล์ {citizen_id}.jp2 จากความสำคัญโฟลเดอร์
         - หากพบ: ถอดรหัส JP2 -> Bitmap
         - หากไม่พบ: ใช้ไอคอน Profile Placeholder จำลอง
                       │
                       ▼
         [ แสดงหน้าโหลด Loading (1.2s) ]
                       │
                       ▼
         [ แสดงผลลัพธ์หน้าจอ ResultScreen ]
         - แสดงข้อมูลกึ่งกลางสมมาตรพร้อม Watermark
         - มีปุ่มขยายดูข้อมูลแทร็กดิบ (Raw Track Data)
                       │
                       ▼
         [ กลับสู่หน้าหลัก / เคลียร์สถานะ ]
         - ผู้ใช้กดย้อนกลับ (Toolbar) หรือกด "กลับไปหน้าเริ่ม"
         - เคลียร์ข้อมูลทั้งหมดในระบบให้เป็นค่าว่าง
```

---

## 📂 โครงสร้างโฟลเดอร์และโค้ดสำคัญ (Project Structure)

```text
app/src/main/java/com/example/macnatic_tape_swipe_reader/
├── features/
│   ├── monitor_logging/             # จัดการ Floating Log Overlay
│   └── msr/
│       ├── models/
│       │   └── ThaiDrivingLicense.kt# โมเดลข้อมูลใบขับขี่
│       └── parsers/
│           └── ThaiDrivingLicenseParser.kt # คลาสลอจิกวิเคราะห์และสกัดข้อมูลจาก Track 1, 2, 3
├── services/
│   └── SunmiPaySdkManager.kt        # ตัวเชื่อมต่อ SDK บริการอ่านแถบแม่เหล็กของ Sunmi
└── view/
    ├── components/                  # คอมโพเนนต์ UI ย่อย
    ├── MainActivity.kt              # Entrypoint จัดการคีย์บอร์ดอีเวนต์และ USB Connection
    ├── MsrScannerScreen.kt          # หน้าจอเตรียมพร้อมสำหรับการรูดบัตร
    ├── ResultScreen.kt              # หน้าจอแสดงผลลัพธ์ข้อมูลที่รูดได้
    └── FormScreen.kt                # หน้าจอกรอกข้อมูลแบบปกติ (Manual Override)
```

---

## 🛠 ความต้องการระบบและการติดตั้ง (System Requirements)

- **Android SDK:** ขั้นต่ำ API Level 26 (Android 8.0) ขึ้นไป
- **อุปกรณ์รันระบบ:**
  - กรณีใช้หัวอ่านในตัวเครื่อง: ต้องเป็นเครื่อง **Sunmi terminal** ที่มีช่องสแกนบัตรแถบแม่เหล็ก และติดตั้งตัวบริการระบบ `com.sunmi.pay.hardware_v3`
  - กรณีใช้หัวอ่าน USB ทั่วไป: อุปกรณ์ Android ใดๆ ที่รองรับ **USB OTG**

---

## 📷 การตั้งค่าไฟล์รูปภาพ JP2 (JP2 Photo Setup)

แอปพลิเคชันจะค้นหาไฟล์รูปภาพเจ้าของบัตรจากเลขบัตรประชาชน 13 หลัก (Citizen ID) ในโฟลเดอร์ต่อไปนี้บนอุปกรณ์:

1. **App external storage:** `/sdcard/Android/data/com.example.macnatic_tape_swipe_reader/files/{citizen_id}.jp2`
2. **Public Download directory:** `/sdcard/Download/{citizen_id}.jp2`
3. **App Assets folder:** โฟลเดอร์ `assets` ภายในโค้ดโปรเจค

*หมายเหตุ: หากไม่พบระบบจะใช้รูปภาพอวาตาร์จำลองแทน*
