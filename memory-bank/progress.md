# Progress

**What works**

- Single-entry-point navigation restructure implemented across the codebase:
  bottom nav (4 tabs owner / 5 tabs tenant) + slim kebab (Settings, Share App)
  + notification bell. Drawer fully removed (`activity_main.xml`,
  `nav_header_drawer.xml`, `drawer_menu*.xml` all gone).
- Settings screen reorganized (Account → Preferences → Notifications →
  Support [now includes My Complaints] → About → standalone Log Out).
  Delete Account flow fully removed (UI, dialog, handler — not just hidden).
- All known callers of the 4 deleted Activities (`DashboardFragment`,
  `ReceiptActivity`, `HouseDetailActivity`, `MyBookingsTenantAdapter`'s stray
  import) were updated; `AndroidManifest.xml` entries removed; unused
  `nav_*` drawer/bottom-nav string resources swept from `strings.xml`.
- Static verification pass (grep sweep across `src/main` for every deleted
  class/resource name, plus manual read-through of all 4 new fragments and
  their layouts' view-id lists against the Java) found zero remaining
  dangling references.

**Not started / backlog**

- Real compiler verification (`./gradlew assembleDebug`) — blocked in this
  session's sandbox by a loopback-networking restriction unrelated to the
  code. Needs to be run in a normal dev environment / Android Studio before
  trusting this as fully done.
- Manual UI QA (both roles, light/dark theme, rotation) — not yet performed.
- `ProfileFragment`'s in-page Settings/Help/Share rows still duplicate the
  kebab (see activeContext.md open question).

**Known issues**

- None found in static review, pending the real build.

_Keep bullets factual and small; link issues or PRs when useful._
