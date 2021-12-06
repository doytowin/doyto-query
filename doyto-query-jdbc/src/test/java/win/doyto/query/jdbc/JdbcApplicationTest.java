package win.doyto.query.jdbc;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * JdbcApplicationTest
 *
 * @author f0rb on 2021-11-28
 */
@ActiveProfiles("test")
@Transactional
@Rollback
@SpringBootTest
abstract class JdbcApplicationTest {
}
