package com.duri.config.dev.p6spy;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.hibernate.engine.jdbc.internal.FormatStyle;

public class P6SpyFormatter implements MessageFormattingStrategy {

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category,
        String prepared, String sql, String url) {
        sql = formatSql(category, sql);
        Date currentDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        return format.format(currentDate) + " | OperationTime : " + elapsed + "ms | " + sql;
    }

    private String formatSql(String category, String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        // DDL은 format 적용 안함
        if (category.contains("statement") && sql.trim().toLowerCase(Locale.ROOT)
            .startsWith("create")) {
            return sql;
        }

        // Hibernate SQL 포맷 적용
        if (category.equals("statement")) {
            String trimmedSQL = sql.trim().toLowerCase(Locale.ROOT);
            if (trimmedSQL.startsWith("select") ||
                trimmedSQL.startsWith("insert") ||
                trimmedSQL.startsWith("update") ||
                trimmedSQL.startsWith("delete")) {
                sql = FormatStyle.BASIC.getFormatter().format(sql);
                return "\nHeFormatSql(P6Spy sql,Hibernate format):\n" + sql;
            }
        }
        return "\nP6Spy sql:\n" + sql;
    }
}
