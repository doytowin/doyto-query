package win.doyto.query.demo.module.menu;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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

    @JsonSerialize
    private String platform;

    @SuppressWarnings("unused")
    private String getPlatform() {
        return "01".equals(platform) ? "_" + platform : "";
    }

    @Override
    public String toCacheKey() {
        return id + "-" + platform;
    }
}
