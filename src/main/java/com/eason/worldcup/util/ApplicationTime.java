package com.eason.worldcup.util;

import java.time.LocalDate;
import java.time.ZoneId;

public final class ApplicationTime {

    public static final ZoneId UTC_PLUS_EIGHT_ZONE = ZoneId.of("Asia/Shanghai");

    private ApplicationTime() {
    }

    public static LocalDate today() {
        return LocalDate.now(UTC_PLUS_EIGHT_ZONE);
    }

}
