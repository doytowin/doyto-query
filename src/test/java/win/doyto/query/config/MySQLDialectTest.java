package win.doyto.query.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * MySQLDialectTest
 *
 * @author f0rb on 2020-04-03
 */
class MySQLDialectTest {
    @Test
    void page() {
        MySQLDialect mysqlDialect = new MySQLDialect();
        String pageSql = mysqlDialect.buildPageSql("SELECT username, password FROM user WHERE valid = true", 10, 50000);
        assertEquals("SELECT a.username, a.password FROM user a JOIN (SELECT id FROM user WHERE valid = true LIMIT 10 OFFSET 50000) b ON a.id = b.id", pageSql);
    }

    @Test
    void update() {
        MySQLDialect mysqlDialect = new MySQLDialect();
        String pageSql = mysqlDialect.buildPageSql("UPDATE user SET user_level = ?, valid = ? WHERE id = ?", 10, 20);
        assertEquals("UPDATE user SET user_level = ?, valid = ? WHERE id = ? LIMIT 10", pageSql);
    }

    @Test
    void delete() {
        MySQLDialect mysqlDialect = new MySQLDialect();
        String pageSql = mysqlDialect.buildPageSql("DELETE FROM user WHERE username = ?", 10, 20);
        assertEquals("DELETE FROM user WHERE username = ? LIMIT 10", pageSql);
    }

    @Test
    void join() {
        MySQLDialect mysqlDialect = new MySQLDialect();
        String pageSql = mysqlDialect.buildPageSql("SELECT u.username, ur.role_id FROM t_user u LEFT JOIN t_user_and_role ur ON u.id = ur.user_id", 10, 20);
        assertEquals("SELECT u.username, ur.role_id FROM t_user u LEFT JOIN t_user_and_role ur ON u.id = ur.user_id LIMIT 10 OFFSET 20", pageSql);
    }

    @Test
    void star() {
        MySQLDialect mysqlDialect = new MySQLDialect();
        String pageSql = mysqlDialect.buildPageSql("SELECT * FROM user WHERE valid = true", 10, 50000);
        assertEquals("SELECT a.* FROM user a JOIN (SELECT id FROM user WHERE valid = true LIMIT 10 OFFSET 50000) b ON a.id = b.id", pageSql);
    }
}