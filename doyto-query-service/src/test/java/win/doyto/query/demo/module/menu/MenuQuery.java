package win.doyto.query.demo.module.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import win.doyto.query.core.PageQuery;

import java.util.Objects;
import javax.persistence.Transient;

/**
 * MenuQuery
 *
 * @author f0rb
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class MenuQuery extends PageQuery {

    @Transient
    private String platform;

    private String menuNameLike;

    @Override
    public MenuIdWrapper toIdWrapper() {
        Objects.requireNonNull(platform);
        return new MenuIdWrapper(null, platform);
    }

}
