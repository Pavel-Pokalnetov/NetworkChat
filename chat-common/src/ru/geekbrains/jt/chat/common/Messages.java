package ru.geekbrains.jt.chat.common;

public class Messages {
    public static final String DELIMITER = "§";
    public static final String AUTH_REQUEST = "/auth_request";
    public static final String AUTH_ACCEPT = "/auth_accept";
    public static final String AUTH_DENY = "/auth_deny";
    public static final String MSG_BROADCAST = "/bcast";
    public static final String MSG_FORMAT_ERROR = "/msg_error";
    public static final String USER_LIST = "/user_list";
    public static final String USER_BROADCAST = "/user_bcast";
    public static final String REGISTRATION = "/user_reg";

    public static final String REGISTRATION_OK = "/user_reg";


    public static final String UNICAST = "/unicast";



    public static String getTypeBcastFromClient(String msg) {
        return USER_BROADCAST + DELIMITER + msg;
    }


    public static String getUserList(String users) {
        return USER_LIST +DELIMITER + users;
    }

    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String nickname) {
        return AUTH_ACCEPT + DELIMITER + nickname;
    }

    public static String getAuthDenied() {
        return AUTH_DENY;
    }

    public static String getMsgFormatError(String message) {
        return MSG_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getTypeBroadcast(String src, String message) {
        return MSG_BROADCAST + DELIMITER + System.currentTimeMillis() +
                DELIMITER + src + DELIMITER + message;
    }

    public static String getSrvUnicast(String message){
        return getMsgUnicast("SERVER",message);
    }

    public static String getMsgUnicast( String dst,String message){
        return UNICAST + DELIMITER + System.currentTimeMillis() +
    DELIMITER + dst + DELIMITER + message;
    }

    public static String getRegistrationMessage(String nickname, String login, String password){
        return  REGISTRATION+DELIMITER+nickname+DELIMITER+login+DELIMITER+password;
    }


}
