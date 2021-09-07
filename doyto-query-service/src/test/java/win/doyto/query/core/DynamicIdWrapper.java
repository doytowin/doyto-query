package win.doyto.query.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Transient;

/**
 * DynamicIdWrapper
 *
 * @author f0rb
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DynamicIdWrapper implements IdWrapper<Integer> {

    private Integer id;

    @Transient
    private String user;

    @Transient
    private String project;

    private String locale;

}
