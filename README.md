# 📱 EasyHiddify VPN

[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-26%2B-brightgreen.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-green.svg)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/DI-Hilt-orange.svg)](https://dagger.dev/hilt/)
[![License](https://img.shields.io/badge/License-GPLv3-red.svg)](https://www.gnu.org/licenses/gpl-3.0)

**EasyHiddify VPN** is a demonstration Android application and reference client for working with the [`easy_hiddify_lib`](https://github.com/YaslikS/easy_hiddify_lib) library based on **Hiddify Core (Libbox / sing-box)**.

The app demonstrates building a full-featured VPN client with a modern UI powered by **Jetpack Compose**, dependency management via **Hilt**, local storage with **Room**, advanced routing capabilities, and support for Split Tunneling.

---

## ✨ Application Features

- 🛡 **VPN Connection & Server Management:**
  - Starting and stopping connections via **VLESS** (REALITY, TLS, Flow), **Trojan**, **Shadowsocks**, and raw **JSON** configurations.
  - Adding, editing, deleting, and copying multiple server configurations saved locally.
- ⚙️ **Advanced Settings & Routing:**
  - **AdBlock (geosite):** Blocks ads and trackers at the DNS level.
  - **FakeIP:** Drastically accelerates DNS resolution for faster browsing.
  - **Multiplexing (Mux):** Combines multiple connections to reduce latency and save battery.
  - **LAN Proxy Share:** Opens a local HTTP/SOCKS5 proxy (port 10808) to share the VPN with other devices on your Wi-Fi.
  - **Network Tuning:** Custom DNS, MTU settings, IPv6 toggling, Strict Route, and LAN Bypass.
- 📱 **App Selection (Split Tunneling):**
  - Automatic scanning of all installed applications on the device.
  - Passing the list of selected `packageName`s to the VPN service for traffic splitting.
  - Custom direct domains bypassing the VPN (e.g., `*.ru`, `sberbank.com`).
- 📝 **Diagnostics & Monitoring:**
  - Real-time connection speed and total traffic usage display.
  - Dedicated **Log Screen** to view and share core diagnostic logs in real-time.
- 🎨 **Modern UI / UX:**
  - Entirely built with **Jetpack Compose** using Material Design 3 guidelines.
  - Edge-to-edge support with nested `Scaffold` architecture.
  - Toggling UI element states is locked during an active VPN connection for safety.

---

## 🏗 Project Architecture

The project is divided into standalone modules:

```text
easy_hiddify_vpn/
├── app/                  # Main app module (Compose UI, ViewModels, Hilt, Navigation)
├── easy_hiddify_lib/     # Connected VPN library (Git Submodule or module)
└── database/             # Room local database module (DAO, Entities, Repositories)
```

### Key Components:
- **`MainApplication`** — entry point (`@HiltAndroidApp`), initializing the library `EasyHiddify.init(this)`.
- **`MainScreen` / `ServerViewModel`** — main VPN control screen: start/stop, traffic stats, and CRUD operations for your saved servers/configs.
- **`AppsScreen` / `AppsViewModel`** — split tunneling screen with a searchable list of installed applications and toggles.
- **`SettingsScreen`** — advanced VPN tuning UI interacting directly with `hiddify.settings`.
- **`LogScreen`** — real-time console log viewer to debug connections.
- **`AppsDao` / `ServerDao`** — data layer for working with the Room local database.

---

## 🛠 Cloning and Building the Project

Since the `easy_hiddify_lib` library module is linked to the project as a **Git Submodule**, clone the project using the recursive flag `--recursive`:

```bash
# Cloning the project along with all submodules
git clone --recursive https://github.com/YaslikS/easy_hiddify_vpn.git
```

If you have already cloned the project using the standard command, fetch the submodules manually:

```bash
git submodule update --init --recursive
git submodule foreach 'git lfs pull'
```

Open the project in **Android Studio** and click **Sync Project with Gradle Files**.

---

## ⚖️ License and Disclaimer

This project is distributed under the **GNU General Public License v3.0 (GPLv3)**.

### Third Parties and Components:
- **[easy_hiddify_lib](https://github.com/YaslikS/easy_hiddify_lib)** — Android wrapper library over the VPN core.
- **[hiddify-core](https://github.com/hiddify/hiddify-core)** — VPN core ([GPLv3 License](https://github.com/hiddify/hiddify-core/blob/main/LICENSE)).
- **[sing-box](https://github.com/SagerNet/sing-box)** — Universal network proxy platform ([GPLv3 License](https://github.com/SagerNet/sing-box/blob/main/LICENSE)).

### ⚠️ Disclaimer
The application and source code are provided on an **"AS IS"** basis solely for educational and research purposes. The author assumes no responsibility or liability for the use of this software, potential network blocks, or violations of local laws. Users are solely responsible for complying with the laws of their country when using VPN technologies.
