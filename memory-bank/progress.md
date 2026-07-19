# Progress

**Phase 2 (navigation system & UX structure) — done this session:**

- Both roles now have exactly 5 bottom-nav tabs matching the spec: Owner =
  Dashboard/Properties/Messages/Bookings/Profile, Tenant =
  Home/Saved/Messages/Bookings/Profile.
- Built a real Messages feature from scratch (previously chat had no list
  entry point — `ChatActivity` was only ever opened contextually from a
  property's Contact button). New: `models/Conversation.java`,
  `adapters/ConversationAdapter.java`, `fragments/MessagesFragment.java` +
  `fragment_messages.xml`/`item_conversation.xml`. Real-time Firestore
  listener on `chats` (`whereArrayContains("participants", uid)`, sorted
  client-side by `lastTimestamp` to avoid a composite-index requirement),
  resolves other-user name/avatar and property title via existing
  `UserRepository`/`HouseRepository`, taps into the existing `ChatActivity`.
- Tenant's old "Houses" tab was dropped (Home/`DashboardFragment` already
  covers search/browse/recommendations per spec) and its full-featured
  search+filter UI (`BrowseHousesFragment`) was preserved by hosting it in a
  new standalone `BrowseHousesActivity`, reachable from "View All"/"Explore"
  entry points instead of a tab.
- Fixed 4 navigation calls that would've silently broken once tenant's
  `nav_houses` tab was removed: `DashboardFragment`'s explore-search quick
  action, `SavedPropertiesFragment`'s and `MyBookingsFragment`'s empty-state
  "Browse Properties" buttons (the latter was previously wired in the layout
  but never connected in Java — a pre-existing dead button), and
  `MainActivity.openRequestedSection()`'s browse/listings deep-link handler.
- Fixed the active-tab indicator: `activity_main.xml` had
  `itemIconTint`/`itemTextColor` both hardcoded to the same solid color, so
  selected and unselected tabs were visually identical. New
  `res/color/bottom_nav_item_color.xml` selector fixes this.
- Added a back-press handler: previously pressing back from any non-Home tab
  exited the app immediately (fragments were swapped via `.replace()` with
  no back stack). Now it returns to Home first, exits on a second press.
- Added a fade transition between tab switches (was an instant cut).
- Verified role-based navigation isolation: bottom nav menus differ per
  role, `MainActivity.handleNavigation()` gates shared tab ids by
  `isOwner()`, `HouseDetailActivity.applyRoleBasedVisibility()` already
  hides owner-only actions from tenants. Found and spawned a separate
  follow-up (not fixed here, out of navigation scope): any Owner can see
  Edit/Manage buttons on *any* property's detail page, not just their own —
  `HouseDetailActivity` checks role but not `house.getOwnerId()`.

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

**Phase 1 (foundation & UI consistency) — done this session:**

- Removed dead code: `DashboardActivity`/`activity_dashboard.xml` (superseded
  by `DashboardFragment`, zero live callers) and `ComplainsActivity`
  (misspelled manifest-duplicate shim of `ComplaintsActivity`).
- Fixed all 4 dead "Coming Soon" toast buttons: `ProfileFragment` Payment
  History → `ReportActivity`, Notifications → `NotificationsActivity`
  (both already existed, just weren't wired); `ReceiptActivity` Print → real
  `PrintManager`/`WebView` print job; `SettingsActivity` Terms of Service →
  new `TermsOfServiceActivity` (mirrors `PrivacyPolicyActivity` pattern).
- Removed `MainActivity.onResume()`'s hardcoded fake "2" notification badge
  (no real unread-count data source existed).
- Added `SwipeRefreshLayout` loading indicators to the 4 bottom-nav-adjacent
  screens that lacked them: `BrowseHousesFragment`, `MyListingsFragment`,
  `SavedPropertiesFragment` (all newly wrapped), and `MyBookingsFragment`
  (layout already had one, just wasn't wired to Java — now is).
- Added an empty state ("No properties available") to `BrowseHousesFragment`,
  which previously went fully blank on an empty result set.
- Fixed 6 silent-failure paths: `HouseDetailActivity` (favorite
  add/remove, owner-info load, reviews load — were empty `onError` bodies or
  log-only), `ChatActivity` (Firestore listener error now surfaces a Toast
  instead of silently stopping updates), `BookingSummaryActivity` (masked
  availability-update failure now at least logged).
- Fixed one real style inconsistency: `fragment_profile.xml` logout button
  now uses the app's own `Widget.SmartHome.Button.TextButton` instead of a
  raw MDC style.
- Confirmed via audit: theme is MDC1 (`Theme.MaterialComponents.DayNight`),
  not true M3 — user explicitly chose to polish within MDC1 rather than
  migrate to `Theme.Material3.*` for this phase (bigger/riskier, out of scope).
- Scoped out: full dp/sp → `@dimen` sweep across all 46 layouts (audit found
  it's widespread — e.g. `item_booking_tenant.xml`/`dialog_booking.xml` are
  100% hardcoded). Judged too high-risk to do blind without a working build/
  emulator in this sandbox; flagged as a separate follow-up task instead of
  bundling into this diff.

**Not started / backlog**

- Real compiler verification (`./gradlew assembleDebug`) — still blocked in
  this sandbox by the same loopback-networking restriction as last session
  (confirmed again this session, unrelated to the code). Needs to run in a
  normal dev environment / Android Studio before trusting any of this as
  fully done. Static verification only: full re-read of every layout file
  whose root tag was restructured (confirmed balanced/well-formed), plus
  grep sweeps for all renamed/deleted symbols (zero dangling references).
- Manual UI QA (both roles, light/dark theme, rotation) — not yet performed.
- Full `@dimen` standardization sweep across all layouts (see above) —
  spawned as a separate task, not done here.
- `ProfileFragment`'s in-page Settings/Help/Share rows still duplicate the
  kebab (see activeContext.md open question, carried over from last session).

**Known issues**

- None found in static review, pending the real build.

_Keep bullets factual and small; link issues or PRs when useful._
