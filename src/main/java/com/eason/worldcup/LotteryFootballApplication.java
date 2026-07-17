package com.eason.worldcup;

import com.eason.worldcup.util.ApplicationTime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class LotteryFootballApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ApplicationTime.UTC_PLUS_EIGHT_ZONE));
        SpringApplication.run(LotteryFootballApplication.class, args);
    }

}
