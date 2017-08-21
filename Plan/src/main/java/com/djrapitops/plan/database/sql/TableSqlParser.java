package main.java.com.djrapitops.plan.database.sql;

/**
 * SqlParser Class for parsing table creation, removal & modification statements.
 *
 * @author Rsl1122
 * @since 3.7.0
 */
public class TableSqlParser extends SqlParser {

    private int columns = 0;

    public TableSqlParser(String start) {
        super(start);
    }

    public static TableSqlParser createTable(String tableName) {
        return new TableSqlParser("CREATE TABLE IF NOT EXISTS " + tableName + " (");
    }

    public static String dropTable(String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    public TableSqlParser column(String column, String type) {
        if (columns > 0) {
            append(", ");
        }
        append(column).addSpace();
        append(type);

        columns++;
        return this;
    }


    public TableSqlParser foreignKey(String column, String refrencedTable, String referencedColumn) {
        if (columns > 0) {
            append(", ");
        }
        append("FOREIGN KEY(")
                .append(column)
                .append(") REFERENCES ")
                .append(refrencedTable)
                .append("(")
                .append(referencedColumn)
                .append(")");
        columns++;
        return this;
    }

    public TableSqlParser notNull() {
        addSpace();
        append("NOT NULL");
        return this;
    }

    public TableSqlParser unique() {
        addSpace();
        append("UNIQUE");
        return this;
    }

    public TableSqlParser defaultValue(boolean value) {
        return defaultValue(value ? "1" : "0");
    }

    public TableSqlParser defaultValue(String value) {
        addSpace();
        append("DEFAULT ").append(value);
        return this;
    }

    public TableSqlParser primaryKeyIDColumn(boolean mySQL, String column, String type) {
        if (columns > 0) {
            append(", ");
        }
        append(column).addSpace();
        append(type).addSpace();
        append((mySQL) ? "NOT NULL AUTO_INCREMENT" : "PRIMARY KEY");
        columns++;
        return this;
    }

    public TableSqlParser primaryKey(boolean mySQL, String column) {
        if (mySQL) {
            if (columns > 0) {
                append(", ");
            }
            append("PRIMARY KEY (").append(column).append(")");
            columns++;
        }
        return this;
    }

    /**
     * Used for ALTER TABLE sql statements.
     *
     * @param column column to modify
     * @return TableSqlParser object
     */
    public static TableSqlParser newColumn(String column, String type) {
        return new TableSqlParser("").column(column, type);
    }

    @Override
    public String toString() {
        append(")");
        return super.toString();
    }
}