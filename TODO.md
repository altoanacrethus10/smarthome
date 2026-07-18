# TODO - My Bookings (Tenant)

## Steps
1. Inspect existing booking-related classes:
   - BookingAdapter / BookingDetailActivity existence
   - MyBookings entry navigation (TenantHomeFragment)
   - Bottom nav badge implementation
2. Create new tenant screen:
   - `MyBookingsActivity` with Toolbar + TabLayout (Upcoming/Past/Cancelled)
3. Create new layouts:
   - `activity_my_bookings.xml`
   - `item_booking_tenant.xml` (card UI requirements)
   - empty state layout (illustration + buttons)
4. Implement real-time Firestore listener:
   - `FirestoreHelper` + `BookingRepository` to listen where `tenantId == userId`
   - order by `moveInDate` descending
5. Implement adapter + tab filtering logic:
   - upcoming: pending + confirmed
   - past: completed
   - cancelled: cancelled
6. Implement cancel booking flow:
   - confirmation dialog
   - set booking status to `cancelled`
   - show refund status after cancellation (may require model/fields)
7. Implement “View Details” navigation to `BookingDetailActivity`.
8. Implement bottom nav badge count (pending/upcoming):
   - update badge from real-time data listener
9. Wire tenant navigation:
   - update TenantHomeFragment “View Bookings” to open `MyBookingsActivity`.
10. Build & run app; verify compilation + basic UI.

