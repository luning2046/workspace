package io.rong.imkit.common;

public class RCloudConst {

    public static final String RES_DIC = "rong_cloud";

    public static class Parcel {
        public static final int EXIST_SEPARATOR = 1;
        public static final int NON_SEPARATOR = 0;
        public static final int FALG_ONE_SEPARATOR = 100;
        public static final int FALG_TOW_SEPARATOR = 200;
        public static final int FALG_THREE_SEPARATOR = 300;
    }

    public static class API {
        public static String HOST = "";

    }

    public static class SYS {
        public static final int MAX_IMAGE_CACHESIZE = 131072;
        public static final int DISCUSSION_PEOPLE_MAX_COUNT = 50;
    }

    public static class EXTRA {
        public static final String CONTENT = "extra_fragment_content";
        public static final String USERS = "extra_users";
        public static final String USER = "extra_user";
        public static final String CONVERSATION = "extra_conversation";
        public static final String NOTICATION_DATA_FLAG = "notication_data_flag";
    }

}
