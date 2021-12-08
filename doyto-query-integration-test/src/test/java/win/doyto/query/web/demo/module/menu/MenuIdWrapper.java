package win.doyto.query.web.demo.module.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import win.doyto.query.core.IdWrapper;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuIdWrapper implements IdWrapper<Integer> {

    private Integer id;

    private String platform;

    @SuppressWarnings("unused")
    public String getPlatform() {
        return "01".equals(platform) ? "_" + platform : "";
    }

    @Override
    public String toCacheKey() {
        return id + "-" + platform;
    }
}
