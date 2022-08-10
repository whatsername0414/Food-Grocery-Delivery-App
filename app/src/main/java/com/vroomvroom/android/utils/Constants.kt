package com.vroomvroom.android.utils

object Constants {
    //Names
    const val VROOMVROOM_DATABASE = "vroomvroom_database"
    const val CART_ITEM_TABLE = "cart_item_table"
    const val SEARCH_TABLE = "search_table"
    const val CART_ITEM_OPTION_TABLE = "cart_item_option_table"
    const val USER_TABLE = "user_table"
    const val LOCATION_TABLE = "location_table"
    const val PREFERENCES_STORE_NAME = "user_preferences"

    //Identifiers
    const val PERMISSION_LOCATION_REQUEST_CODE = 1
    const val CHANNEL_ID = "VroomVroomID"
    const val CHANNEL_NAME = "Vroom Vroom"
    const val REQUEST_CHECK_SETTINGS = 0x1
    const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"

    val DELIVERY_RANGE_CITIES = listOf("Bacacay", "Legazpi City",
        "Ligao", "Malilipot", "Malinao", "Santo Domingo", "Tabaco City", "Tiwi")
    //Favorite Direction
    const val ADD_TO_FAVORITES = 1
    const val REMOVE_FROM_FAVORITES = 0

    //Payment Types
    const val CASH_ON_DELIVERY = "Cash On Delivery"
    const val GCASH = "GCash"

    //OrderStatus
    const val PENDING = "Pending"
    const val CONFIRMED = "Confirmed"
    const val TO_RECEIVE = "To Receive"
    const val DELIVERED = "Delivered"
    const val CANCELLED = "Cancelled"
    const val CONFIRMED_TAB_POSITION = 1
    const val TO_RECEIVE_TAB_POSITION = 2

    //Date Format
    const val FORMAT_DD_MMM_YYYY_HH_MM_SS: String = "dd MMM yyyy HH:mm:ss"
    const val DEFAULT_SERVER_TIME_FORMAT: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    //Saved State
    const val SUCCESS: String = "SUCCESS"

    //Scroll Threshold
    const val SCROLL_THRESHOLD = 500
}

enum class ClickType {
    NEGATIVE,
    POSITIVE
}