package com.duri.config.dev.data;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("dev")
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class DummyUserDataBatchInsert implements CommandLineRunner {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss.SSS");

    private final JdbcTemplate jdbcTemplate;

    int userCount = 5_000_000;

    @Override
    public void run(String... args) {

        log.info("[{}] Jdbc Data Insertion Started", LocalDateTime.now().format(formatter));

        long start = System.currentTimeMillis();

//        insertDummyData();

        long end = System.currentTimeMillis();
        log.info("[{}] Jdbc Data Insertion Finished in {}ms", LocalDateTime.now().format(formatter),
            end - start);
    }

    public void insertDummyData() {

        logExecutionTime("유저 데이터 생성", () -> insertDummyUsers(5_000_001, userCount));

        int totalUsers = countTableRows("user");

        log.info("📊 데이터 삽입 결과");
        log.info("Users: {}", totalUsers);
    }

    private void logExecutionTime(String title, Runnable task) {
        log.info("[{}] 👉 {} 시작", LocalDateTime.now().format(formatter), title);
        long start = System.currentTimeMillis();

        task.run();

        long end = System.currentTimeMillis();
        log.info("[{}] ✅ {} 완료 ({}ms)", LocalDateTime.now().format(formatter), title, end - start);
    }

    private void insertDummyUsers(int userIdStart, int totalUsers) {
        String sql =
            "INSERT INTO user (user_id, email, username, password, name, birthday, gender, position, role, couple_code, created_at, modified_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int chunkSize = 100_000;

        for (int offset = 0; offset < totalUsers; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, totalUsers - offset);
            insertUserChunk(userIdStart + offset, batchSize, sql);
            log.info("✅ 유저 {} ~ {} 삽입 완료", offset + 1, offset + batchSize);
        }
    }

    private void insertUserChunk(int offset, int count, String sql) {
        Random random = new Random();
        LocalDate startBirthday = LocalDate.of(1970, 1, 1);
        LocalDate endBirthday = LocalDate.of(2013, 12, 31);

        LocalDate startCreatedAt = LocalDate.of(2015, 1, 1);
        LocalDate endCreatedAt = LocalDate.of(2025, 6, 15);

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int id = offset + i;

                LocalDate birthday = randomDate(startBirthday, endBirthday, random);
                LocalDateTime createdAt = randomDateTime(startCreatedAt, endCreatedAt, random);
                Timestamp timestamp = Timestamp.valueOf(createdAt);

                int coupleIndex = (id - 1) / 2 + 1;
                String gender = (id % 2 == 1) ? "MALE" : "FEMALE";
                String position = (id % 2 == 1) ? "LEFT" : "RIGHT";

                ps.setInt(1, id);
                ps.setString(2, "email" + id + "@example.com");
                ps.setString(3, "username" + id);
                ps.setString(4, "$2a$10$JzTozJ0.sqRGrTqUIMgTX.x8M2456B2X4uLiP0rO3AXdCeovCokjC");
                ps.setString(5, "name" + id);
                ps.setDate(6, Date.valueOf(birthday));
                ps.setString(7, gender);
                ps.setString(8, position);
                ps.setString(9, "USER");
                ps.setString(10, "couplecode" + coupleIndex);
                ps.setTimestamp(11, timestamp);
                ps.setTimestamp(12, timestamp);
            }

            @Override
            public int getBatchSize() {
                return count;
            }
        });
    }

    private LocalDate randomDate(LocalDate start, LocalDate end, Random random) {
        int days = (int) (end.toEpochDay() - start.toEpochDay());
        return start.plusDays(random.nextInt(days + 1));
    }

    private LocalDateTime randomDateTime(LocalDate start, LocalDate end, Random random) {
        LocalDate randomDate = randomDate(start, end, random);
        int hour = random.nextInt(24);
        int minute = random.nextInt(60);
        int second = random.nextInt(60);
        return randomDate.atTime(hour, minute, second);
    }

    private int countTableRows(String tableName) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName,
            Integer.class);
        return count != null ? count : 0;
    }
}
