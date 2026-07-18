package com.example.smarthome.database.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smarthome.constants.AppConstants;
import com.example.smarthome.models.House;
import com.example.smarthome.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_HOUSES = "houses";
    private static final String TABLE_FAVORITES = "favorites";
    private static final String TABLE_BOOKINGS = "bookings";
    private static final String TABLE_CONTRACTS = "contracts";

    // Common Columns
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CREATED_AT = "created_at";

    // Users Table Columns
    private static final String COLUMN_USER_NAME = "name";
    private static final String COLUMN_USER_EMAIL = "email";
    private static final String COLUMN_USER_PHONE = "phone";
    private static final String COLUMN_USER_TYPE = "user_type";
    private static final String COLUMN_USER_AVATAR = "avatar";

    // Houses Table Columns
    private static final String COLUMN_HOUSE_OWNER_ID = "owner_id";
    private static final String COLUMN_HOUSE_OWNER_NAME = "owner_name";
    private static final String COLUMN_HOUSE_TITLE = "title";
    private static final String COLUMN_HOUSE_DESCRIPTION = "description";
    private static final String COLUMN_HOUSE_LOCATION = "location";
    private static final String COLUMN_HOUSE_LATITUDE = "latitude";
    private static final String COLUMN_HOUSE_LONGITUDE = "longitude";
    private static final String COLUMN_HOUSE_PRICE = "price";
    private static final String COLUMN_HOUSE_BEDROOMS = "bedrooms";
    private static final String COLUMN_HOUSE_BATHROOMS = "bathrooms";
    private static final String COLUMN_HOUSE_AREA = "area";
    private static final String COLUMN_HOUSE_TYPE = "property_type";
    private static final String COLUMN_HOUSE_IMAGES = "images";
    private static final String COLUMN_HOUSE_FEATURES = "features";
    private static final String COLUMN_HOUSE_AVAILABLE = "is_available";

    // Favorites Table Columns
    private static final String COLUMN_FAVORITE_USER_ID = "user_id";
    private static final String COLUMN_FAVORITE_HOUSE_ID = "house_id";

    // Bookings Table Columns
    private static final String COLUMN_BOOKING_HOUSE_ID = "house_id";
    private static final String COLUMN_BOOKING_HOUSE_TITLE = "house_title";
    private static final String COLUMN_BOOKING_TENANT_ID = "tenant_id";
    private static final String COLUMN_BOOKING_OWNER_ID = "owner_id";
    private static final String COLUMN_BOOKING_STATUS = "status";
    private static final String COLUMN_BOOKING_MOVE_IN = "move_in_date";
    private static final String COLUMN_BOOKING_TOTAL_PRICE = "total_price";

    // Contracts Table Columns
    private static final String COLUMN_CONTRACT_TENANT_ID = "tenant_id";
    private static final String COLUMN_CONTRACT_OWNER_ID = "owner_id";
    private static final String COLUMN_CONTRACT_HOUSE_ID = "house_id";
    private static final String COLUMN_CONTRACT_START = "start_date";
    private static final String COLUMN_CONTRACT_END = "end_date";
    private static final String COLUMN_CONTRACT_STATUS = "status";

    // Create Table Queries
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_ID + " TEXT PRIMARY KEY,"
            + COLUMN_USER_NAME + " TEXT,"
            + COLUMN_USER_EMAIL + " TEXT,"
            + COLUMN_USER_PHONE + " TEXT,"
            + COLUMN_USER_TYPE + " TEXT,"
            + COLUMN_USER_AVATAR + " TEXT,"
            + COLUMN_CREATED_AT + " TEXT"
            + ")";

    private static final String CREATE_TABLE_HOUSES = "CREATE TABLE " + TABLE_HOUSES + "("
            + COLUMN_ID + " TEXT PRIMARY KEY,"
            + COLUMN_HOUSE_OWNER_ID + " TEXT,"
            + COLUMN_HOUSE_OWNER_NAME + " TEXT,"
            + COLUMN_HOUSE_TITLE + " TEXT,"
            + COLUMN_HOUSE_DESCRIPTION + " TEXT,"
            + COLUMN_HOUSE_LOCATION + " TEXT,"
            + COLUMN_HOUSE_LATITUDE + " REAL,"
            + COLUMN_HOUSE_LONGITUDE + " REAL,"
            + COLUMN_HOUSE_PRICE + " REAL,"
            + COLUMN_HOUSE_BEDROOMS + " INTEGER,"
            + COLUMN_HOUSE_BATHROOMS + " INTEGER,"
            + COLUMN_HOUSE_AREA + " REAL,"
            + COLUMN_HOUSE_TYPE + " TEXT,"
            + COLUMN_HOUSE_IMAGES + " TEXT,"
            + COLUMN_HOUSE_FEATURES + " TEXT,"
            + COLUMN_HOUSE_AVAILABLE + " INTEGER,"
            + COLUMN_CREATED_AT + " TEXT"
            + ")";

    private static final String CREATE_TABLE_FAVORITES = "CREATE TABLE " + TABLE_FAVORITES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_FAVORITE_USER_ID + " TEXT,"
            + COLUMN_FAVORITE_HOUSE_ID + " TEXT,"
            + COLUMN_CREATED_AT + " TEXT"
            + ")";

    private static final String CREATE_TABLE_BOOKINGS = "CREATE TABLE " + TABLE_BOOKINGS + "("
            + COLUMN_ID + " TEXT PRIMARY KEY,"
            + COLUMN_BOOKING_HOUSE_ID + " TEXT,"
            + COLUMN_BOOKING_HOUSE_TITLE + " TEXT,"
            + COLUMN_BOOKING_TENANT_ID + " TEXT,"
            + COLUMN_BOOKING_OWNER_ID + " TEXT,"
            + COLUMN_BOOKING_STATUS + " TEXT,"
            + COLUMN_BOOKING_MOVE_IN + " TEXT,"
            + COLUMN_BOOKING_TOTAL_PRICE + " REAL,"
            + COLUMN_CREATED_AT + " TEXT"
            + ")";

    private static final String CREATE_TABLE_CONTRACTS = "CREATE TABLE " + TABLE_CONTRACTS + "("
            + COLUMN_ID + " TEXT PRIMARY KEY,"
            + COLUMN_CONTRACT_TENANT_ID + " TEXT,"
            + COLUMN_CONTRACT_OWNER_ID + " TEXT,"
            + COLUMN_CONTRACT_HOUSE_ID + " TEXT,"
            + COLUMN_CONTRACT_START + " TEXT,"
            + COLUMN_CONTRACT_END + " TEXT,"
            + COLUMN_CONTRACT_STATUS + " TEXT,"
            + COLUMN_CREATED_AT + " TEXT"
            + ")";

    private static DatabaseHelper instance;

    public DatabaseHelper(Context context) {
        super(context, AppConstants.DATABASE_NAME, null, AppConstants.DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_HOUSES);
        db.execSQL(CREATE_TABLE_FAVORITES);
        db.execSQL(CREATE_TABLE_BOOKINGS);
        db.execSQL(CREATE_TABLE_CONTRACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOUSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTRACTS);
        onCreate(db);
    }

    // ==================== USER OPERATIONS ====================

    public long insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, user.getId());
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_EMAIL, user.getEmail());
        values.put(COLUMN_USER_PHONE, user.getPhone());
        values.put(COLUMN_USER_TYPE, user.getUserType());
        values.put(COLUMN_USER_AVATAR, user.getAvatarUrl());
        values.put(COLUMN_CREATED_AT, user.getCreatedAt());

        return db.insert(TABLE_USERS, null, values);
    }

    public User getUser(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, COLUMN_ID + "=?",
                new String[]{userId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            User user = new User();
            user.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            user.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)));
            user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PHONE)));
            user.setUserType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_TYPE)));
            user.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_AVATAR)));
            user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public int updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_PHONE, user.getPhone());
        values.put(COLUMN_USER_AVATAR, user.getAvatarUrl());

        return db.update(TABLE_USERS, values, COLUMN_ID + "=?", new String[]{user.getId()});
    }

    public boolean isUserExists(String userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID},
                COLUMN_ID + "=?", new String[]{userId}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // ==================== HOUSE OPERATIONS ====================

    public long insertHouse(House house) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, house.getId());
        values.put(COLUMN_HOUSE_OWNER_ID, house.getOwnerId());
        values.put(COLUMN_HOUSE_OWNER_NAME, house.getOwnerName());
        values.put(COLUMN_HOUSE_TITLE, house.getTitle());
        values.put(COLUMN_HOUSE_DESCRIPTION, house.getDescription());
        values.put(COLUMN_HOUSE_LOCATION, house.getLocation());
        values.put(COLUMN_HOUSE_LATITUDE, house.getLatitude());
        values.put(COLUMN_HOUSE_LONGITUDE, house.getLongitude());
        values.put(COLUMN_HOUSE_PRICE, house.getPrice());
        values.put(COLUMN_HOUSE_BEDROOMS, house.getBedrooms());
        values.put(COLUMN_HOUSE_BATHROOMS, house.getBathrooms());
        values.put(COLUMN_HOUSE_AREA, house.getArea());
        values.put(COLUMN_HOUSE_TYPE, house.getPropertyType());
        values.put(COLUMN_HOUSE_IMAGES, listToString(house.getImages()));
        values.put(COLUMN_HOUSE_FEATURES, listToString(house.getFeatures()));
        values.put(COLUMN_HOUSE_AVAILABLE, house.isAvailable() ? 1 : 0);
        values.put(COLUMN_CREATED_AT, house.getCreatedAt());

        return db.insert(TABLE_HOUSES, null, values);
    }

    public List<House> getAllHouses() {
        List<House> houses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HOUSES, null, null, null, null, null, COLUMN_CREATED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                houses.add(houseFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return houses;
    }

    public House getHouse(String houseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HOUSES, null, COLUMN_ID + "=?",
                new String[]{houseId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            House house = houseFromCursor(cursor);
            cursor.close();
            return house;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public int updateHouse(House house) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_HOUSE_TITLE, house.getTitle());
        values.put(COLUMN_HOUSE_DESCRIPTION, house.getDescription());
        values.put(COLUMN_HOUSE_PRICE, house.getPrice());
        values.put(COLUMN_HOUSE_LOCATION, house.getLocation());
        values.put(COLUMN_HOUSE_BEDROOMS, house.getBedrooms());
        values.put(COLUMN_HOUSE_BATHROOMS, house.getBathrooms());
        values.put(COLUMN_HOUSE_AREA, house.getArea());
        values.put(COLUMN_HOUSE_TYPE, house.getPropertyType());
        values.put(COLUMN_HOUSE_IMAGES, listToString(house.getImages()));
        values.put(COLUMN_HOUSE_FEATURES, listToString(house.getFeatures()));
        values.put(COLUMN_HOUSE_AVAILABLE, house.isAvailable() ? 1 : 0);

        return db.update(TABLE_HOUSES, values, COLUMN_ID + "=?", new String[]{house.getId()});
    }

    private House houseFromCursor(Cursor cursor) {
        House house = new House();
        house.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        house.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_OWNER_ID)));
        house.setOwnerName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_OWNER_NAME)));
        house.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_TITLE)));
        house.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_DESCRIPTION)));
        house.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_LOCATION)));
        house.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_LATITUDE)));
        house.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_LONGITUDE)));
        house.setPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_PRICE)));
        house.setBedrooms(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_BEDROOMS)));
        house.setBathrooms(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_BATHROOMS)));
        house.setArea(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_AREA)));
        house.setPropertyType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_TYPE)));
        house.setImages(stringToList(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_IMAGES))));
        house.setFeatures(stringToList(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_FEATURES))));
        house.setAvailable(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_HOUSE_AVAILABLE)) == 1);
        house.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        return house;
    }

    public int deleteHouse(String houseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_HOUSES, COLUMN_ID + "=?", new String[]{houseId});
    }

    public boolean isHouseExists(String houseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HOUSES, new String[]{COLUMN_ID},
                COLUMN_ID + "=?", new String[]{houseId}, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public List<House> getHousesByOwner(String ownerId) {
        List<House> houses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_HOUSES, null, COLUMN_HOUSE_OWNER_ID + "=?",
                new String[]{ownerId}, null, null, COLUMN_CREATED_AT + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                houses.add(houseFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        if (cursor != null) cursor.close();
        return houses;
    }

    // ==================== FAVORITE OPERATIONS ====================

    public long addFavorite(String userId, String houseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAVORITE_USER_ID, userId);
        values.put(COLUMN_FAVORITE_HOUSE_ID, houseId);
        values.put(COLUMN_CREATED_AT, String.valueOf(System.currentTimeMillis()));

        return db.insert(TABLE_FAVORITES, null, values);
    }

    public int removeFavorite(String userId, String houseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_FAVORITES,
                COLUMN_FAVORITE_USER_ID + "=? AND " + COLUMN_FAVORITE_HOUSE_ID + "=?",
                new String[]{userId, houseId});
    }

    public boolean isFavorite(String userId, String houseId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, null,
                COLUMN_FAVORITE_USER_ID + "=? AND " + COLUMN_FAVORITE_HOUSE_ID + "=?",
                new String[]{userId, houseId}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<String> getUserFavorites(String userId) {
        List<String> favoriteIds = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{COLUMN_FAVORITE_HOUSE_ID},
                COLUMN_FAVORITE_USER_ID + "=?", new String[]{userId}, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                favoriteIds.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FAVORITE_HOUSE_ID)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return favoriteIds;
    }

    // ==================== BOOKING OPERATIONS ====================

    public long insertBooking(com.example.smarthome.models.Booking booking) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, booking.getId());
        values.put(COLUMN_BOOKING_HOUSE_ID, booking.getHouseId());
        values.put(COLUMN_BOOKING_HOUSE_TITLE, booking.getHouseTitle());
        values.put(COLUMN_BOOKING_TENANT_ID, booking.getTenantId());
        values.put(COLUMN_BOOKING_OWNER_ID, booking.getOwnerId());
        values.put(COLUMN_BOOKING_STATUS, booking.getStatus());
        values.put(COLUMN_BOOKING_MOVE_IN, booking.getMoveInDate());
        values.put(COLUMN_BOOKING_TOTAL_PRICE, booking.getTotalPrice());
        values.put(COLUMN_CREATED_AT, booking.getCreatedAt());

        return db.insert(TABLE_BOOKINGS, null, values);
    }

    public List<com.example.smarthome.models.Booking> getBookingsByTenant(String tenantId) {
        List<com.example.smarthome.models.Booking> bookings = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BOOKINGS, null, COLUMN_BOOKING_TENANT_ID + "=?",
                new String[]{tenantId}, null, null, COLUMN_CREATED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                bookings.add(bookingFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookings;
    }

    public int updateBookingStatus(String bookingId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOKING_STATUS, status);
        return db.update(TABLE_BOOKINGS, values, COLUMN_ID + "=?", new String[]{bookingId});
    }

    // ==================== CONTRACT OPERATIONS ====================

    public long insertContract(com.example.smarthome.models.Contract contract) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, contract.getId());
        values.put(COLUMN_CONTRACT_TENANT_ID, contract.getTenantId());
        values.put(COLUMN_CONTRACT_OWNER_ID, contract.getOwnerId());
        values.put(COLUMN_CONTRACT_HOUSE_ID, contract.getHouseId());
        values.put(COLUMN_CONTRACT_START, contract.getStartDate());
        values.put(COLUMN_CONTRACT_END, contract.getEndDate());
        values.put(COLUMN_CONTRACT_STATUS, contract.getStatus());
        values.put(COLUMN_CREATED_AT, contract.getCreatedAt());

        return db.insert(TABLE_CONTRACTS, null, values);
    }

    public List<com.example.smarthome.models.Contract> getContractsByUser(String userId) {
        List<com.example.smarthome.models.Contract> contracts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_CONTRACT_TENANT_ID + "=? OR " + COLUMN_CONTRACT_OWNER_ID + "=?";
        Cursor cursor = db.query(TABLE_CONTRACTS, null, selection,
                new String[]{userId, userId}, null, null, COLUMN_CREATED_AT + " DESC");

        if (cursor.moveToFirst()) {
            do {
                contracts.add(contractFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return contracts;
    }

    private com.example.smarthome.models.Contract contractFromCursor(Cursor cursor) {
        com.example.smarthome.models.Contract contract = new com.example.smarthome.models.Contract();
        contract.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        contract.setTenantId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTRACT_TENANT_ID)));
        contract.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTRACT_OWNER_ID)));
        contract.setHouseId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTRACT_HOUSE_ID)));
        contract.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTRACT_START)));
        contract.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTRACT_END)));
        contract.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTRACT_STATUS)));
        contract.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        return contract;
    }

    private com.example.smarthome.models.Booking bookingFromCursor(Cursor cursor) {
        com.example.smarthome.models.Booking booking = new com.example.smarthome.models.Booking();
        booking.setId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        booking.setHouseId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_HOUSE_ID)));
        booking.setHouseTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_HOUSE_TITLE)));
        booking.setTenantId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_TENANT_ID)));
        booking.setOwnerId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_OWNER_ID)));
        booking.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_STATUS)));
        booking.setMoveInDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_MOVE_IN)));
        booking.setTotalPrice(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BOOKING_TOTAL_PRICE)));
        booking.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        return booking;
    }

    // ==================== CLEAR DATA ====================

    public void clearAllTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, null, null);
        db.delete(TABLE_HOUSES, null, null);
        db.delete(TABLE_FAVORITES, null, null);
        db.delete(TABLE_BOOKINGS, null, null);
    }

    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    private List<String> stringToList(String str) {
        List<String> list = new ArrayList<>();
        if (str == null || str.isEmpty()) return list;
        String[] parts = str.split(",");
        for (String part : parts) {
            if (!part.trim().isEmpty()) list.add(part.trim());
        }
        return list;
    }
}
