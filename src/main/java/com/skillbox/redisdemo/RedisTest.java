package com.skillbox.redisdemo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static java.lang.System.out;

public class RedisTest {

    private static final int DELETE_SECONDS_AGO = 2;
    private static final int RPS = 19;
    private static final int USERS = 20;
    private static final int SLEEP = 1; // 1 millisecond
    private static final SimpleDateFormat DF = new SimpleDateFormat("HH:mm:ss");

    private static void logUserPaid(int userId) {
        String log = String.format("[%s] Пользователь № %d оплатил услугу", DF.format(new Date()), userId);
        out.println(log);
    }

    public static void main(String[] args) throws InterruptedException {

        RedisStorage redis = new RedisStorage();
        redis.init();

        for (int seconds = 0; seconds <= 5; seconds++) {
            Set<Integer> processedUsers = new HashSet<>();

            if (seconds >= 1) {
                int last = new Random().nextInt(USERS);
                logUserPaid(last);
                redis.moveToFront(last);
            }

            for (int request = 0; request <= RPS; request++) {
                int userId = new Random().nextInt(USERS);
                while (processedUsers.contains(userId)) {
                    userId = new Random().nextInt(USERS);
                }
                processedUsers.add(userId);
                redis.logPageVisit(userId);
                Thread.sleep(SLEEP);
                out.println("Показываем пользователя № " + userId);
                redis.deleteOldEntries(DELETE_SECONDS_AGO);
            }

            redis.deleteOldEntries(DELETE_SECONDS_AGO);
        }
        redis.shutdown();
    }
}
