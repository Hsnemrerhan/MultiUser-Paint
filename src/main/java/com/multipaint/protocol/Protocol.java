package com.multipaint.protocol;

/**
 * Metin tabanlı, | ayraclı satır protokolü (satır sonu: \n).
 */
public final class Protocol {
    public static final String SEP = "|";

    public static final String HELLO = "HELLO";
    public static final String FILE_CREATE = "FILE_CREATE";
    public static final String FILE_LIST_REQUEST = "FILE_LIST_REQUEST";
    public static final String FILE_LIST_RESPONSE = "FILE_LIST_RESPONSE";
    public static final String JOIN = "JOIN";
    public static final String DRAW_PATH = "DRAW_PATH";
    public static final String CUT_AREA = "CUT_AREA";
    /** İstemciler arası senkron için: PASTE_AREA|destX|destY|base64Png */
    public static final String PASTE_AREA = "PASTE_AREA";
    public static final String CLEAR = "CLEAR";
    public static final String ERROR = "ERROR";

    /** Sunucu: JOIN başarılı, ardından geçmiş mesajlar gönderilir */
    public static final String JOIN_OK = "JOIN_OK";

    private Protocol() {}
}
