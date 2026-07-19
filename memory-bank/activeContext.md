# Active context

**Current focus** (one short paragraph):

Just finished a navigation restructure: replaced the 3-surface nav (hamburger
drawer + bottom nav + kebab, with duplicated destinations) with a single
bottom-nav-driven architecture. Tenant gets 5 tabs (Home/Houses/Saved/Bookings/Profile),
Owner gets 4 (Home/Houses/Bookings/Profile, no Saved — owners can't favorite
houses). Settings and Share App now live only behind the kebab menu (⋮),
next to an always-visible notification bell. Drawer deleted outright.
Delete Account flow removed.

**In progress**:

- [ ] Run an actual `./gradlew assembleDebug` — could not verify in this
      session's environment (Gradle daemon failed with `java.io.IOException:
      Unable to establish loopback connection`, tried --no-daemon, sandbox
      override, and forcing IPv4 loopback; all failed the same way — looks
      like a host/network restriction outside the repo, not a project
      misconfig). Extensive static verification (id cross-checks, import
      sweeps, caller-site greps) was done instead — see progress.md.
- [ ] Manual QA pass in Android Studio/emulator as both Owner and Tenant
      accounts once the build succeeds.

**Decisions (recent)**:

- `AccountActivity`, `SavedPropertiesActivity`, `MyBookingsActivity`,
  `BookingsActivity` were deleted and ported to Fragments
  (`ProfileFragment`, `SavedPropertiesFragment`, `MyBookingsFragment`,
  `OwnerBookingsFragment`) hosted in `MainActivity`'s `content_frame`, since
  they're now bottom-nav tabs, not standalone screens.
- `FavoritesFragment` (a thinner duplicate of `SavedPropertiesActivity`) and
  `TenantHomeFragment` (dead code, zero callers) were deleted.
- Cross-tab navigation from inside a hosted fragment goes through
  `((MainActivity) requireActivity()).openTab(R.id.nav_x)`. From a
  standalone top-level Activity (e.g. `ReceiptActivity`, `HouseDetailActivity`),
  it's `new Intent(this, MainActivity.class).putExtra(MainActivity.EXTRA_OPEN_SECTION, "x")`.
- Owner's kebab "Add Property" item was dropped — `MyListingsFragment` already
  has its own FAB for this, kebab item was a redundant second entry point.

**Open questions**:

- `ProfileFragment` still has its own in-page Settings/Help & Support/Share App
  rows (ported as-is from `AccountActivity`) — technically a second entry point
  alongside the kebab. Left alone deliberately (out of the approved plan's
  literal scope), flagged for the user to decide if it should be trimmed too.

_Update when the task or branch focus changes._
