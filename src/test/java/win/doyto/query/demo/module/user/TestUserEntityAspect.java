package win.doyto.query.demo.module.user;

import lombok.Getter;
import org.springframework.stereotype.Component;
import win.doyto.query.entity.EntityAspect;

import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * TestUserEntityAspect
 *
 * @author f0rb on 2019-05-26
 */
@Component
public class TestUserEntityAspect implements EntityAspect<UserEntity> {
    @Getter
    private int times;

    @Override
    public void afterUpdate(UserEntity origin, UserEntity current) {
        assertNotSame(origin, current);
        times++;
    }

    @Override
    public void afterDelete(UserEntity userEntity) {
        if (userEntity.getId() == 1) {
            // trigger rollback
            throw new RuntimeException();
        }
    }
}
