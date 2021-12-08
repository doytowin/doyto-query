package win.doyto.query.service;

import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * NonNullToStringStyle
 *
 * @author f0rb on 2019-07-14
 */
class NonNullToStringStyle extends ToStringStyle {

    public static final NonNullToStringStyle NO_CLASS_NAME_NON_NULL_STYLE = new NonNullToStringStyle(false);

    private NonNullToStringStyle(boolean useClassName) {
        this.setUseClassName(useClassName);
        this.setUseIdentityHashCode(false);
    }

    @Override
    public void append(StringBuffer buffer, String fieldName, Object value, Boolean fullDetail) {
        if (value != null) {
            super.append(buffer, fieldName, value, fullDetail);
        }
    }


}
