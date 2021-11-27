package win.doyto.query.web.demo.module.menu;

import lombok.Getter;
import lombok.Setter;
import win.doyto.query.validation.PatchGroup;
import win.doyto.query.validation.UpdateGroup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * MenuRequest
 *
 * @author f0rb
 */
@Getter
@Setter
public class MenuRequest {

    @NotNull(groups = {UpdateGroup.class, PatchGroup.class})
    private Integer id;

    @NotBlank
    private String platform;

    private Integer parentId;

    private String menuName;

    private String memo;

    private Boolean valid = true;

}
