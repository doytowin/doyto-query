package win.doyto.query.service;

import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * NonNullToStringStyle
 *
 * @author f0rb on 2019-07-14
 */
class NonNullToStringStyle extends ToStringStyle {

    public static final NonNullToStringStyle NON_NULL_STYLE = new NonNullToStringStyle();

    private NonNullToStringStyle() {
        this.setUseIdentityHashCode(false);
    }

    @Override
    public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
        if (value != null) {
            super.append(buffer, fieldName, value, fullDetail);
        }
    }


}
