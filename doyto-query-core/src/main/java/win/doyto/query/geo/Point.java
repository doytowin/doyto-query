package win.doyto.query.geo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Point
 *
 * @author f0rb on 2021-11-30
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Point {
    private double x;
    private double y;

    public List<Double> toList() {
        return Collections.unmodifiableList(Arrays.asList(this.x, this.y));
    }
}
