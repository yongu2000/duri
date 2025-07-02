package com.duri.config.dev.data;

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
public class DummyCommentStatDataBatchInsert implements CommandLineRunner {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss.SSS");

    private final JdbcTemplate jdbcTemplate;

    int commentStatCount = 2_000_020;

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

        logExecutionTime("CommentStat 데이터 생성",
            () -> insertCommentStats(2095679, 2_000_002, commentStatCount, 100_000));

        log.info("📊 데이터 삽입 완료");
    }

    private void logExecutionTime(String title, Runnable task) {
        log.info("[{}] 👉 {} 시작", LocalDateTime.now().format(formatter), title);
        long start = System.currentTimeMillis();

        task.run();

        long end = System.currentTimeMillis();
        log.info("[{}] ✅ {} 완료 ({}ms)", LocalDateTime.now().format(formatter), title, end - start);
    }

    private void insertCommentStats(int idStart, int commentIdStart, int totalStats,
        int chunkSize) {
        String sql =
            "INSERT INTO comment_stat (comment_stat_id, comment_id, comment_count, created_at, modified_at) "
                +
                "VALUES (?, ?, ?, ?, ?)";

        Random random = new Random();
        LocalDate start = LocalDate.of(2015, 1, 1);
        LocalDate end = LocalDate.of(2025, 6, 15);

        for (int offset = 0; offset < totalStats; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, totalStats - offset);
            int offsetStart = idStart + offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int id = offsetStart + i;
                    int commentId = commentIdStart + i;
                    int count = 0;
                    Timestamp timestamp = Timestamp.valueOf(randomDateTime(start, end, random));

                    ps.setInt(1, id);
                    ps.setInt(2, commentId);
                    ps.setInt(3, count);
                    ps.setTimestamp(4, timestamp);
                    ps.setTimestamp(5, timestamp);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            log.info("📈 comment_stat {}개 삽입 완료 ({}~{})", batchSize, offsetStart,
                offsetStart + batchSize - 1);
        }
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
