package com.multipaint.protocol;

public final class Protocol {
    public static final String SEP = "|";

    public static final String HELLO = "HELLO";
    public static final String FILE_CREATE = "FILE_CREATE";
    public static final String FILE_LIST_REQUEST = "FILE_LIST_REQUEST";
    public static final String FILE_LIST_RESPONSE = "FILE_LIST_RESPONSE";
    public static final String JOIN = "JOIN";
    public static final String DRAW_PATH = "DRAW_PATH";
    public static final String CUT_AREA = "CUT_AREA";
    public static final String PASTE_AREA = "PASTE_AREA";
    public static final String CLEAR = "CLEAR";
    public static final String ERROR = "ERROR";
    public static final String JOIN_OK = "JOIN_OK";

    private Protocol() {}
}
