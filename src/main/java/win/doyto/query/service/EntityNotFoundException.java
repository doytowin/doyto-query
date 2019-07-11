package win.doyto.query.service;

/**
 * EntityNotFoundException
 *
 * @author f0rb on 2019-06-10
 */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException() {
        super("Entity not found!");
    }
}
