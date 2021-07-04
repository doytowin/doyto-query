package win.doyto.query.entity;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.validation.CreateGroup;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;

import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * AbstractId
 *
 * @author f0rb on 2021-06-27
 */
@Getter
@Setter
public abstract class AbstractPersistable<I extends Serializable> implements Persistable<I>, Serializable {
    private static final long serialVersionUID = -4538555675455803732L;
    @Id
    @GeneratedValue
    @Null(groups = CreateGroup.class)
    @NotNull(groups = {UpdateGroup.class, PatchGroup.class})
    protected I id;
}
