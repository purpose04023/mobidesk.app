# Project Plan

MobiDesk App: An Android / Cross-Platform Flutter application inspired by Microsoft's Windows App / Windows 365 Cloud PC interface. Users manage and connect directly to virtual cloud PCs (Windows VMs win-01 through win-05 hosted on an Ubuntu KVM/libvirt server mobilab-server). Features include: Direct Native RDP connection (Option 1 - rdp:// scheme / RDP launch), Supabase Auth and PostgreSQL DB (profiles, virtual_machines, pc_assignments) with Role-Based Access Control (Admin vs Student), Node.js Express Backend Bridge for KVM lifecycle control (virsh domstate, virsh start/stop), Student Dashboard displaying assigned Cloud PC card with hardware specs and status badge, Admin Dashboard displaying grid of all VMs with power controls, and Microsoft Fluent Design aesthetics.

## Project Brief

# MobiDesk - Android App Project Brief (MVP)

MobiDesk is an Android application inspired by Microsoft's Windows App / Windows 365 Cloud PC interface. It allows users to manage and directly connect to cloud-hosted Windows virtual machines (VMs `win-01` through `win-05`) running on an Ubuntu KVM/libvirt server (`mobilab-server`).

---

## Features

1. **Authentication & Role-Based Access Control (RBAC)**
   - Supabase Auth sign-in routing users to dedicated role-based views (**Student** vs **Admin**).

2. **Student Dashboard & Cloud PC Management**
   - Card view displaying assigned Cloud PC hardware specs and real-time status badge with VM lifecycle actions (Start/Stop) via a Node.js Express bridge to KVM/libvirt (`virsh`).

3. **Admin Management Dashboard**
   - Centralized grid view listing all virtual machines (`win-01` through `win-05`) on the server with real-time status monitoring and VM power controls.

4. **Direct Native RDP Connection**
   - One-tap initiation of Remote Desktop sessions by dispatching `rdp://` URI scheme intents directly to native Android RDP client applications.

5. **Microsoft Fluent-Inspired Adaptive Design**
   - Modern visual styling reflecting Microsoft Fluent Design principles, optimizing layout presentation across diverse Android screen sizes.

---

## High-Level Tech Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose (Fluent Design aesthetics)
* **Navigation & Adaptive Strategy:** **Jetpack Navigation 3** (state-driven navigation) and **Compose Material Adaptive** library (for responsive layouts)
* **Asynchronous & Reactive Core:** Kotlin Coroutines & Flow
* **Networking & Integration:** Ktor Client (REST communication with Node.js Express KVM bridge) & Supabase Kotlin SDK (Authentication & data models)
* **Architecture Pattern:** MVVM with Unidirectional Data Flow (UDF)

## Implementation Steps

### Task_1_Data_Network_Auth: Setup Supabase Kotlin SDK integration, Ktor REST API client for Node.js Express KVM bridge, data models for VMs and user roles, and Authentication repository/ViewModel.
- **Status:** COMPLETED
- **Updates:** Dependencies added for Supabase Kotlin SDK, Ktor HTTP client, Kotlinx Serialization. Supabase client provider created. KvmBridgeClient REST API client built with full VM lifecycle endpoint support. UserProfile, UserRole, VmStatus, VmDetails, VmAssignment data models implemented with JSON serialization. AuthRepository and AuthViewModel implemented with sign-in, session state, and role management. Unit tests passing and assembleDebug passing.
- **Acceptance Criteria:**
  - Supabase client configured with API credentials
  - Ktor client configured to communicate with Node.js Express KVM bridge
  - Data models created for VM status, user role (Student/Admin), and VM actions
  - Auth repository and ViewModel handling sign-in and role retrieval
  - build pass

### Task_2_Student_Dashboard: Implement Navigation, Fluent Design UI theme, Login screen, and Student Dashboard displaying assigned Cloud PC specs and VM lifecycle actions.
- **Status:** COMPLETED
- **Updates:** Microsoft Fluent Design Theme and custom UI components (MicaCard, StatusBadge, SpecChip, FluentButton, FluentTextField) created. Navigation routing implemented with Jetpack Navigation 3. LoginScreen implemented with AuthViewModel, error handling, and demo shortcuts. StudentDashboardScreen and StudentDashboardViewModel implemented displaying assigned Cloud PC specs and real-time status. VM lifecycle actions (Start, Stop, Restart) integrated with KvmBridgeClient and VmRepository. Unit tests passing (12/12) and assembleDebug passing.
- **Acceptance Criteria:**
  - Fluent Design inspired UI theme and components created
  - Navigation routing between Login, Student Dashboard, and Admin Dashboard based on user role
  - Student Dashboard displays assigned VM details (win-01..05) with real-time status
  - VM lifecycle actions (Start/Stop) communicate with Express KVM bridge
  - build pass

### Task_3_Admin_Dashboard_RDP: Implement Admin Management Dashboard with VM grid view (win-01 through win-05), power controls, and rdp:// URI Intent launcher for direct native RDP connection.
- **Status:** COMPLETED
- **Updates:** Implemented RdpLauncher utility generating rdp:// URI scheme intents with Play Store fallback. Implemented AdminDashboardViewModel and AdminDashboardScreen with system metrics banner, hypervisor VM pool grid (win-01 through win-05), assigned student profiles, and VM power controls. Responsive adaptive layout implemented for phones and tablets using Compose Adaptive. RDP launcher connected to both Student and Admin dashboards. 15 unit tests passing and assembleDebug passing.
- **Acceptance Criteria:**
  - Admin Dashboard displays grid/list of all VMs (win-01 to win-05) with live status monitoring
  - Admin VM power controls trigger Start/Stop commands via Ktor API
  - One-tap RDP button dispatches rdp:// URI scheme Intent to external RDP client
  - Adaptive screen layout support across phones and tablets
  - build pass
- **Duration:** N/A

### Task_4_Run_and_Verify: Build, execute, and verify complete MobiDesk app. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - All features (Auth, Student Dashboard, Admin Dashboard, VM controls, RDP launch) verified functional
- **StartTime:** 2026-10-26 20:44:51 IST

