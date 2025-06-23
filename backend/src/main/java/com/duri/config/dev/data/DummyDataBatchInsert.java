package com.duri.config.dev.data;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
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
public class DummyDataBatchInsert implements CommandLineRunner {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
        "yyyy-MM-dd HH:mm:ss.SSS");

    private final JdbcTemplate jdbcTemplate;

    int userCount = 1_000_000;
    int coupleCount = 500_000;

    int postCount = 1_000_000;
    int couple1postCount = 1_000_000;

    int likePostCount = 500_000;
    int likePostCountForCouple1 = 1_000_000;

    int notiCountFromCouples = 1_000_000;
    int notiCountToUser1 = 1_000_000;

    int baseCommentCount = 1_000_000;
    int extraCommentCount = 1_000_000;
    int replyCommentCount = 1_000_000;

    int extraCommentStart = baseCommentCount + 1;
    int reply1Start = extraCommentStart + extraCommentCount + 1;
    int reply2Start = reply1Start + replyCommentCount + 1;

    @Override
    public void run(String... args) {

//        deleteAllData();

        log.info("[{}] Jdbc Data Insertion Started", LocalDateTime.now().format(formatter));

        long start = System.currentTimeMillis();

//        insertDummyData();

        long end = System.currentTimeMillis();
        log.info("[{}] Jdbc Data Insertion Finished in {}ms", LocalDateTime.now().format(formatter),
            end - start);
    }

    private void deleteAllData() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.update("DELETE FROM notification");
        jdbcTemplate.update("DELETE FROM like_post");

        jdbcTemplate.update("DELETE FROM comment_stat");
        jdbcTemplate.update("DELETE FROM comment");

        jdbcTemplate.update("DELETE FROM post_stat");
        jdbcTemplate.update("DELETE FROM post");

        jdbcTemplate.update("DELETE FROM couple");
        jdbcTemplate.update("DELETE FROM user");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        log.info("모든 데이터 삭제 완료");
    }

    public void insertDummyData() {

        logExecutionTime("유저 데이터 생성", () -> insertDummyUsers(userCount));
        logExecutionTime("커플 데이터 생성", () -> insertDummyCouples(coupleCount));

        logExecutionTime("게시글 데이터 생성", () -> insertDummyPosts(postCount));
        logExecutionTime("게시글 (couple_id=1) 데이터 생성",
            () -> insertPostsForCouple1(postCount + 1, couple1postCount));
        logExecutionTime("게시글 통계 데이터 생성", () -> insertPostStats(postCount + couple1postCount));

        logExecutionTime("좋아요 데이터 생성", () -> insertLikePosts(likePostCount));
        logExecutionTime("좋아요 데이터 생성 (couple_id=1)", () ->
            insertLikePostsForCouple1(likePostCount + 1, likePostCountForCouple1));

        logExecutionTime("알림 생성 (커플별 1개)",
            () -> insertNotificationsFromCouples(notiCountFromCouples));
        logExecutionTime("알림 생성 (user_to_id=1)",
            () -> insertNotificationsToUser1(notiCountFromCouples + 1, notiCountToUser1));

        logExecutionTime("기본 댓글 생성", () -> insertComments(baseCommentCount));
        logExecutionTime("1번 게시글 추가 댓글",
            () -> insertCommentsForPost1(extraCommentStart, extraCommentCount, 100_000));
        logExecutionTime("1번 댓글 대댓글",
            () -> insertReplies(reply1Start, replyCommentCount, 1, 1, 100_000));
        logExecutionTime("2번 댓글 대댓글",
            () -> insertReplies(reply2Start, replyCommentCount, 2, 2, 100_000));
        logExecutionTime("댓글 통계 생성", () -> insertCommentStats(baseCommentCount, 100_000));

        int totalUsers = countTableRows("user");
        int totalPosts = countTableRows("post");
        int totalComments = countTableRows("comment");
        int totalNotification = countTableRows("notification");
        int totalCommentStat = countTableRows("comment_stat");
        int totalPostStat = countTableRows("post_stat");
        int totalCouple = countTableRows("couple");
        int totalLikePost = countTableRows("like_post");

        log.info("📊 데이터 삽입 결과");
        log.info("Users: {}", totalUsers);
        log.info("Couple: {}", totalCouple);

        log.info("Posts: {}", totalPosts);
        log.info("Post Stat: {}", totalPostStat);

        log.info("Comments (대댓글 포함): {}", totalComments);
        log.info("Comment Stat: {}", totalCommentStat);

        log.info("Like Post: {}", totalLikePost);

        log.info("Notification: {}", totalNotification);
    }

    private void logExecutionTime(String title, Runnable task) {
        log.info("[{}] 👉 {} 시작", LocalDateTime.now().format(formatter), title);
        long start = System.currentTimeMillis();

        task.run();

        long end = System.currentTimeMillis();
        log.info("[{}] ✅ {} 완료 ({}ms)", LocalDateTime.now().format(formatter), title, end - start);
    }

    private void insertDummyUsers(int totalUsers) {
        String sql =
            "INSERT INTO user (user_id, email, username, password, name, birthday, gender, position, role, couple_code, created_at, modified_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int chunkSize = 100_000;

        for (int offset = 0; offset < totalUsers; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, totalUsers - offset);
            insertUserChunk(offset, batchSize, sql);
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
                int id = offset + i + 1;

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

    private void insertDummyCouples(int totalCouples) {
        String sql =
            "INSERT INTO couple (couple_id, user_left, user_right, code, name, bio, created_at, modified_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        int chunkSize = 100_000;

        for (int offset = 0; offset < totalCouples; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, totalCouples - offset);
            insertCoupleChunk(offset, batchSize, sql);
            log.info("✅ 커플 {} ~ {} 삽입 완료", offset + 1, offset + batchSize);
        }
    }

    private void insertCoupleChunk(int offset, int count, String sql) {
        Random random = new Random();
        LocalDate startCreatedAt = LocalDate.of(2015, 1, 1);
        LocalDate endCreatedAt = LocalDate.of(2025, 6, 15);

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int coupleId = offset + i + 1;

                int userLeftId = 2 * coupleId - 1;
                int userRightId = 2 * coupleId;

                LocalDateTime createdAt = randomDateTime(startCreatedAt, endCreatedAt, random);
                Timestamp timestamp = Timestamp.valueOf(createdAt);

                ps.setInt(1, coupleId);
                ps.setInt(2, userLeftId);
                ps.setInt(3, userRightId);
                ps.setString(4, "couplecode" + coupleId);
                ps.setString(5, "couplename" + coupleId);
                ps.setString(6, "couple" + coupleId + " 자기소개");
                ps.setTimestamp(7, timestamp);
                ps.setTimestamp(8, timestamp);
            }

            @Override
            public int getBatchSize() {
                return count;
            }
        });
    }

    private void insertDummyPosts(int totalPosts) {
        String sql =
            "INSERT INTO post (post_id, couple_id, title, address, road_address, place_name, place_url, phone, category, category_group, "
                +
                "date, x, y, scope, user_left_comment, user_left_rate, user_right_comment, user_right_rate, rate, status, created_at, modified_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int chunkSize = 100_000;

        for (int offset = 0; offset < totalPosts; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, totalPosts - offset);
            insertPostChunk(offset, batchSize, sql);
            log.info("✅ 게시글 {} ~ {} 삽입 완료", offset + 1, offset + batchSize);
        }
    }

    private void insertPostChunk(int offset, int count, String sql) {
        Random random = new Random();
        LocalDate startDate = LocalDate.of(2015, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 15);

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int id = offset + i + 1;
                int coupleId = (id + 1) / 2; // 게시글 2개 당 1커플
                int year = 2015 + random.nextInt(11);
                LocalDate postDate = LocalDate.of(year, 1, 1);

                int leftRate = random.nextInt(10) + 1;
                int rightRate = random.nextInt(10) + 1;
                double averageRate = (leftRate + rightRate) / 2.0;

                String scope = (id % 2 == 1) ? "PRIVATE" : "PUBLIC";
                String status = (id % 2 == 1) ? "PENDING" : "COMPLETE";

                LocalDateTime createdAt = randomDateTime(startDate, endDate, random);
                Timestamp timestamp = Timestamp.valueOf(createdAt);

                ps.setInt(1, id);
                ps.setInt(2, coupleId);
                ps.setString(3, "제목" + id);
                ps.setString(4, "address" + id);
                ps.setString(5, "도로명주소" + id);
                ps.setString(6, "장소" + id);
                ps.setString(7, "장소url" + id);
                ps.setString(8, "phone" + id);
                ps.setString(9, "category" + id);
                ps.setString(10, "categorygroup" + id);
                ps.setDate(11, Date.valueOf(postDate));
                ps.setDouble(12, random.nextDouble() * 100); // x
                ps.setDouble(13, random.nextDouble() * 100); // y
                ps.setString(14, scope);
                ps.setString(15, "왼쪽리뷰" + id);
                ps.setInt(16, leftRate);
                ps.setString(17, "오른쪽리뷰" + id);
                ps.setInt(18, rightRate);
                ps.setDouble(19, averageRate);
                ps.setString(20, status);
                ps.setTimestamp(21, timestamp);
                ps.setTimestamp(22, timestamp);
            }

            @Override
            public int getBatchSize() {
                return count;
            }
        });
    }

    private void insertPostStats(int totalStats) {
        String sql =
            "INSERT INTO post_stat (post_stat_id, post_id, like_count, comment_count, created_at, modified_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        int chunkSize = 100_000;

        for (int offset = 0; offset < totalStats; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, totalStats - offset);
            insertPostStatChunk(offset, batchSize, sql);
            log.info("📊 게시글 통계 {} ~ {} 삽입 완료", offset + 1, offset + batchSize);
        }
    }

    private void insertPostStatChunk(int offset, int count, String sql) {
        Random random = new Random();
        LocalDate start = LocalDate.of(2015, 1, 1);
        LocalDate end = LocalDate.of(2025, 6, 15);

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int id = offset + i + 1;
                LocalDateTime createdAt = randomDateTime(start, end, random);
                Timestamp timestamp = Timestamp.valueOf(createdAt);

                ps.setInt(1, id);
                ps.setInt(2, id); // post_id는 1:1 대응
                ps.setInt(3, random.nextInt(201)); // like_count: 0~200
                ps.setInt(4, 1); // comment_count 고정
                ps.setTimestamp(5, timestamp);
                ps.setTimestamp(6, timestamp);
            }

            @Override
            public int getBatchSize() {
                return count;
            }
        });
    }

    private void insertPostsForCouple1(int postIdStart, int count) {
        String sql =
            "INSERT INTO post (post_id, couple_id, title, address, road_address, place_name, place_url, phone, category, category_group, "
                +
                "date, x, y, scope, user_left_comment, user_left_rate, user_right_comment, user_right_rate, rate, status, created_at, modified_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int chunkSize = 100_000;
        Random random = new Random();
        LocalDate startDate = LocalDate.of(2015, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 15);

        for (int offset = 0; offset < count; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, count - offset);
            int currentOffset = postIdStart + offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int id = currentOffset + i;
                    int year = 2015 + random.nextInt(11);
                    LocalDate postDate = LocalDate.of(year, 1, 1);

                    int leftRate = random.nextInt(10) + 1;
                    int rightRate = random.nextInt(10) + 1;
                    double averageRate = (leftRate + rightRate) / 2.0;

                    String scope = (id % 2 == 1) ? "PRIVATE" : "PUBLIC";
                    String status = ((i % 2) == 0) ? "PENDING" : "COMPLETE"; // 절반씩

                    LocalDateTime createdAt = randomDateTime(startDate, endDate, random);
                    Timestamp timestamp = Timestamp.valueOf(createdAt);

                    ps.setInt(1, id); // post_id
                    ps.setInt(2, 1); // couple_id 고정
                    ps.setString(3, "제목" + id);
                    ps.setString(4, "address" + id);
                    ps.setString(5, "도로명주소" + id);
                    ps.setString(6, "장소" + id);
                    ps.setString(7, "장소url" + id);
                    ps.setString(8, "phone" + id);
                    ps.setString(9, "category" + id);
                    ps.setString(10, "categorygroup" + id);
                    ps.setDate(11, Date.valueOf(postDate));
                    ps.setDouble(12, random.nextDouble() * 100); // x
                    ps.setDouble(13, random.nextDouble() * 100); // y
                    ps.setString(14, scope);
                    ps.setString(15, "왼쪽리뷰" + id);
                    ps.setInt(16, leftRate);
                    ps.setString(17, "오른쪽리뷰" + id);
                    ps.setInt(18, rightRate);
                    ps.setDouble(19, averageRate);
                    ps.setString(20, status);
                    ps.setTimestamp(21, timestamp);
                    ps.setTimestamp(22, timestamp);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            log.info("✅ couple_id=1 게시글 {} ~ {} 삽입 완료", currentOffset + 1,
                currentOffset + batchSize);
        }
    }

    private void insertLikePosts(int totalLikes) {
        String sql =
            "INSERT INTO like_post (like_post_id, couple_id, post_id, created_at, modified_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        int chunkSize = 100_000;

        for (int offset = 0; offset < totalLikes; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, totalLikes - offset);
            insertLikePostChunk(offset, batchSize, sql);
            log.info("👍 like_post {} ~ {} 삽입 완료 (일반 커플)", offset + 1, offset + batchSize);
        }
    }

    private void insertLikePostChunk(int offset, int count, String sql) {
        Random random = new Random();
        LocalDate startDate = LocalDate.of(2015, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 15);

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                int likePostId = offset + i + 1;
                int coupleId = likePostId;
                int postId = offset + i + 1;

                Timestamp timestamp = Timestamp.valueOf(randomDateTime(startDate, endDate, random));

                ps.setInt(1, likePostId);
                ps.setInt(2, coupleId);
                ps.setInt(3, postId);
                ps.setTimestamp(4, timestamp);
                ps.setTimestamp(5, timestamp);
            }

            @Override
            public int getBatchSize() {
                return count;
            }
        });
    }


    private void insertLikePostsForCouple1(int offsetStart, int totalLikes) {
        String sql =
            "INSERT INTO like_post (like_post_id, couple_id, post_id, created_at, modified_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        int chunkSize = 100_000;
        Random random = new Random();
        LocalDate startDate = LocalDate.of(2015, 1, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 15);

        for (int offset = 0; offset < totalLikes; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, totalLikes - offset);
            int currentOffset = offsetStart + offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int likePostId = currentOffset + i + 1;
                    int postId = currentOffset + 1 + i + 1;

                    Timestamp timestamp = Timestamp.valueOf(
                        randomDateTime(startDate, endDate, random));

                    ps.setInt(1, likePostId);
                    ps.setInt(2, 1); // couple_id 고정
                    ps.setInt(3, postId);
                    ps.setTimestamp(4, timestamp);
                    ps.setTimestamp(5, timestamp);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            log.info("👍 like_post {} ~ {} 삽입 완료 (couple_id = 1)", currentOffset + 1,
                currentOffset + batchSize);
        }
    }

    private void insertNotificationsFromCouples(int total) {
        String sql =
            "INSERT INTO notification (notification_id, user_to_id, user_from_id, couple_from_id, content, type, confirmed, created_at, modified_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int chunkSize = 100_000;
        Random random = new Random();
        LocalDate start = LocalDate.of(2015, 1, 1);
        LocalDate end = LocalDate.of(2025, 6, 15);

        for (int offset = 0; offset < total; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, total - offset);
            int offsetStart = offset + 1;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int id = offsetStart + i;
                    int userToId = id;
                    int coupleFromId;
                    do {
                        coupleFromId = random.nextInt(coupleCount) + 1;
                    } while (coupleFromId == id); // 자기자신 제외

                    Timestamp timestamp = Timestamp.valueOf(randomDateTime(start, end, random));

                    ps.setInt(1, id);
                    ps.setInt(2, userToId);
                    ps.setNull(3, Types.INTEGER); // user_from_id 미정 또는 시스템 발송
                    ps.setInt(4, coupleFromId);
                    ps.setString(5, "알림" + id);
                    ps.setString(6, "POST");
                    ps.setBoolean(7, false);
                    ps.setTimestamp(8, timestamp);
                    ps.setTimestamp(9, timestamp);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            log.info("📨 알림 {} ~ {} 삽입 완료 (커플 알림)", offsetStart, offsetStart + batchSize - 1);
        }
    }

    private void insertNotificationsToUser1(int idStart, int total) {
        String sql =
            "INSERT INTO notification (notification_id, user_to_id, user_from_id, couple_from_id, content, type, confirmed, created_at, modified_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int chunkSize = 100_000;
        Random random = new Random();
        LocalDate start = LocalDate.of(2015, 1, 1);
        LocalDate end = LocalDate.of(2025, 6, 15);

        for (int offset = 0; offset < total; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, total - offset);
            int offsetStart = idStart + offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int id = offsetStart + i;
                    int userFromId = random.nextInt(userCount) + 1;
                    boolean confirmed = (i % 2 == 0); // true/false 번갈아

                    Timestamp timestamp = Timestamp.valueOf(randomDateTime(start, end, random));

                    ps.setInt(1, id);
                    ps.setInt(2, 1); // user_to_id 고정
                    ps.setInt(3, userFromId);
                    ps.setInt(4, 1); // couple_from_id 고정
                    ps.setString(5, "알림" + id);
                    ps.setString(6, "POST");
                    ps.setBoolean(7, confirmed);
                    ps.setTimestamp(8, timestamp);
                    ps.setTimestamp(9, timestamp);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            log.info("📨 알림 {} ~ {} 삽입 완료 (user_to_id = 1)", offsetStart,
                offsetStart + batchSize - 1);
        }
    }

    private void insertComments(int totalComments) {
        String sql =
            "INSERT INTO comment (comment_id, content, couple_id, post_id, parent_comment_id, reply_to_comment_id, created_at, modified_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        int chunkSize = 100_000;
        Random random = new Random();
        LocalDate start = LocalDate.of(2015, 1, 1);
        LocalDate end = LocalDate.of(2025, 6, 15);

        for (int offset = 0; offset < totalComments; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, totalComments - offset);
            int offsetStart = offset + 1;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int id = offsetStart + i;
                    Timestamp timestamp = Timestamp.valueOf(randomDateTime(start, end, random));

                    ps.setInt(1, id);
                    ps.setString(2, "댓글" + id);
                    ps.setInt(3, random.nextInt(coupleCount) + 1); // couple_id 1~500,000
                    ps.setInt(4, id); // post_id = comment_id (1~100만) 순차
                    ps.setNull(5, Types.INTEGER); // parent_comment_id 없음
                    ps.setNull(6, Types.INTEGER); // reply_to_comment_id 없음
                    ps.setTimestamp(7, timestamp);
                    ps.setTimestamp(8, timestamp);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            log.info("💬 댓글 {} ~ {} 삽입 완료", offsetStart, offsetStart + batchSize - 1);
        }
    }

    private void insertCommentsForPost1(int idStart, int totalCount, int chunkSize) {
        String sql =
            "INSERT INTO comment (comment_id, content, couple_id, post_id, parent_comment_id, reply_to_comment_id, created_at, modified_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Random random = new Random();
        LocalDate start = LocalDate.of(2015, 1, 1);
        LocalDate end = LocalDate.of(2025, 6, 15);

        for (int offset = 0; offset < totalCount; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, totalCount - offset);
            int currentStart = idStart + offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int id = currentStart + i;
                    Timestamp timestamp = Timestamp.valueOf(randomDateTime(start, end, random));

                    ps.setInt(1, id);
                    ps.setString(2, "댓글" + id);
                    ps.setInt(3, random.nextInt(coupleCount) + 1);
                    ps.setInt(4, 1); // post_id 고정
                    ps.setNull(5, Types.INTEGER);
                    ps.setNull(6, Types.INTEGER);
                    ps.setTimestamp(7, timestamp);
                    ps.setTimestamp(8, timestamp);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            log.info("💬 댓글 {}개 삽입 완료 ({}~{})", batchSize, currentStart,
                currentStart + batchSize - 1);
        }
    }

    private void insertReplies(int idStart, int totalCount, int parentId, int postId,
        int chunkSize) {
        String sql =
            "INSERT INTO comment (comment_id, content, couple_id, post_id, parent_comment_id, reply_to_comment_id, created_at, modified_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Random random = new Random();
        LocalDate start = LocalDate.of(2015, 1, 1);
        LocalDate end = LocalDate.of(2025, 6, 15);

        for (int offset = 0; offset < totalCount; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, totalCount - offset);
            int currentStart = idStart + offset;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int id = currentStart + i;
                    Timestamp timestamp = Timestamp.valueOf(randomDateTime(start, end, random));

                    ps.setInt(1, id);
                    ps.setString(2, "대댓글" + id);
                    ps.setInt(3, 1); // couple_id 고정
                    ps.setInt(4, postId);
                    ps.setInt(5, parentId);
                    ps.setInt(6, parentId);
                    ps.setTimestamp(7, timestamp);
                    ps.setTimestamp(8, timestamp);
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });

            log.info("↪ 대댓글 {}개 삽입 완료 ({}~{}, parent_comment_id = {})", batchSize, currentStart,
                currentStart + batchSize - 1, parentId);
        }
    }

    private void insertCommentStats(int totalStats, int chunkSize) {
        String sql =
            "INSERT INTO comment_stat (comment_stat_id, comment_id, comment_count, created_at, modified_at) "
                +
                "VALUES (?, ?, ?, ?, ?)";

        Random random = new Random();
        LocalDate start = LocalDate.of(2015, 1, 1);
        LocalDate end = LocalDate.of(2025, 6, 15);

        for (int offset = 0; offset < totalStats; offset += chunkSize) {
            int batchSize = Math.min(chunkSize, totalStats - offset);
            int offsetStart = offset + 1;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int id = offsetStart + i;
                    int count = (id == 1) ? extraCommentCount : 0;
                    Timestamp timestamp = Timestamp.valueOf(randomDateTime(start, end, random));

                    ps.setInt(1, id);
                    ps.setInt(2, id);
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

    private void insertCommentStats(int idStart, int totalStats, int chunkSize) {
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
                    int count = (id == 1) ? extraCommentCount : 0;
                    Timestamp timestamp = Timestamp.valueOf(randomDateTime(start, end, random));

                    ps.setInt(1, id);
                    ps.setInt(2, id);
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
