
# 📱 EasyHiddify VPN

[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-26%2B-brightgreen.svg)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-blue.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.x-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-green.svg)](https://developer.android.com/jetpack/compose)
[![Hilt](https://img.shields.io/badge/DI-Hilt-orange.svg)](https://dagger.dev/hilt/)
[![License](https://img.shields.io/badge/License-GPLv3-red.svg)](https://www.gnu.org/licenses/gpl-3.0)

**EasyHiddify VPN** is a demonstration Android application and reference client for working with the [`easy_hiddify_lib`](https://github.com/YaslikS/easy_hiddify_lib) library based on **Hiddify Core (Libbox / sing-box)**.

The app demonstrates building a full-featured VPN client with a modern UI powered by **Jetpack Compose**, dependency management via **Hilt**, local storage with **Room**, and support for Split Tunneling.

---

## ✨ Application Features

- 🛡 **VPN Connection:**
  - Starting and stopping connections via VLESS (REALITY, TLS, Flow), Shadowsocks, and JSON configurations.
- 📱 **App Selection Management (Split Tunneling):**
  - Automatic scanning of all installed applications on the device upon first launch.
  - Passing the list of selected `packageName`s to the VPN service for traffic splitting.
- 🎨 **Modern UI / UX:**
  - Entirely built with **Jetpack Compose**.
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
- **`MainScreen`** — main VPN control screen: start/stop, config input field, status indicator, traffic stats, and log console.
- **`AppsScreen`** — split tunneling screen with a list of installed applications and toggles.
- **`AppsViewModel`** — ViewModel containing business logic for searching, saving apps to Room, and collecting selected package lists.
- **`AppsRepo` / `AppsDao` / `AppDB`** — data layer for working with the Room local database.

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
