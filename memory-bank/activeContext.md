# Active context

**Current focus** (one short paragraph):

Completed Phase 1 of the 10-phase SmartHome improvement plan (foundation &
UI consistency) — see progress.md for the full list. Previous session's
navigation restructure (hamburger drawer + bottom nav + kebab → single
bottom-nav architecture) is the foundation this built on. User explicitly
chose "polish within MDC1" over an M3 theme migration for this phase.

**In progress**:

- [ ] Run an actual `./gradlew assembleDebug` — still blocked in this
      sandbox by the same `java.io.IOException: Unable to establish
      loopback connection` restriction as last session (re-confirmed this
      session). Static verification only (see progress.md) — needs a real
      dev environment before trusting this as fully done.
- [ ] Manual QA pass in Android Studio/emulator as both Owner and Tenant
      accounts once the build succeeds — covers both this phase's changes
      and the prior session's nav restructure.
- [ ] Separate follow-up task spawned (not started): full `@dimen`
      standardization sweep across all 46 layout files — deliberately not
      done in this session, judged too risky without a working build to
      visually verify.

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
