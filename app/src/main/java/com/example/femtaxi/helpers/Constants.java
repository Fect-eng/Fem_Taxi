package com.example.femtaxi.helpers;

public class Constants {

    public class Firebase {
        public class Nodo {
            public static final String DRIVER_ACTIVE = "drive_active";
            public static final String DRIVER_WORKING = "drive_working";
            public static final String CLIENT = "client";
            public static final String DRIVER = "driver";
            public static final String CLIENT_BOOKING = "clientBooking";
            public static final String HISTORY_BOOKING = "historyBooking";
            public static final String TOKEN = "tokens";
            public static final String INFO = "info";
            public static final String IMAGE_CLIENT = "image_client";
            public static final String IMAGE_DRIVER = "image_driver";
        }

        public class Client {
            public static final String UID = "UId";
            public static final String NAME = "name";
            public static final String EMAIL = "email";
            public static final String PHOTO = "photo";
        }
    }

    public class PERMISSION {
        public static final int PICK_IMAGE_REQUEST = 1000;
        public static final int PICK_CAMERA_REQUEST = 2000;
    }

    public class REQUEST {
        public static final int REQUEST_CODE_GALLERY = 100;
        public static final int REQUEST_CODE_CAMERA = 200;
    }

    public class Preferences {
        public static final String SHARED_PREF_NAME = "SHARED_PREF_NAME";
        public static final String PREF_IS_CLIENT = "PREF_IS_CLIENT";
        public static final String PREF_IS_DRIVER = "PREF_IS_DRIVER";
    }

    public class Extras {
        public static final String EXTRA_CLIENT_ID = "EXTRA_CLIENT_ID";
        public static final String EXTRA_MINUT = "EXTRA_MINUT";
        public static final String EXTRA_KM = "EXTRA_KM";
        public static final String EXTRA_ADDRESS_ORIGIN = "EXTRA_ADDRESS_ORIGIN";
        public static final String EXTRA_ORIGIN_LAT = "EXTRA_ORIGIN_LAT";
        public static final String EXTRA_ORIGIN_LONG = "EXTRA_ORIGIN_LONG";
        public static final String EXTRA_ADDRESS_DESTINO = "EXTRA_ADDRESS_DESTINO";
        public static final String EXTRA_DESTINO_LAT = "EXTRA_DESTINO_LAT";
        public static final String EXTRA_DESTINO_LONG = "EXTRA_DESTINO_LONG";
        public static final String EXTRA_PRICE = "EXTRA_PRICE";
        public static final String EXTRA_IS_CONNECTED = "EXTRA_IS_CONNECTED";
    }
}
