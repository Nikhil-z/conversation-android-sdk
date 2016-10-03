/*
 * Copyright (c) 2016 Nexmo Inc
 * All rights reserved.
 *
 */
package com.nexmo.sdk.conversation.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Common utilities.
 */
public class DateUtil {

    static final DateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());

    static final ThreadLocal<DateFormat> ISO_8601_DATE_FORMAT =
            new ThreadLocal<DateFormat>() {
                @Override
                protected DateFormat initialValue() {
                    DATE_FORMAT.setLenient(false);
                    DATE_FORMAT.setTimeZone(Calendar.getInstance().getTimeZone());
                    return DATE_FORMAT;
                }
            };

    public static Date formatIso8601DateString(String timestamp) throws ParseException {
        return ISO_8601_DATE_FORMAT.get().parse(timestamp);
    }

    public static String formatIso8601DateString(Date date) {
        return (date != null ? ISO_8601_DATE_FORMAT.get().format(date) : null);
    }

}
