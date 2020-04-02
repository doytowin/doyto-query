package win.doyto.query.web.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PresetErrorCode
 *
 * @author f0rb
 */
@AllArgsConstructor
public enum PresetErrorCode implements ErrorCode {
    SUCCESS(0, "访问成功"),

    ERROR(1, "系统内部异常"),
    REQUEST_BODY_ERROR(2, "请求内容异常"),
    ARGUMENT_TYPE_MISMATCH(3, "参数类型异常"),
    ARGUMENT_FORMAT_ERROR(4, "参数格式错误: %s"),
    ARGUMENT_VALIDATION_FAILED(5, "参数校验失败"),
    HTTP_METHOD_NOT_SUPPORTED(6, "该接口不支持%s请求"),
    DUPLICATE_KEY_EXCEPTION(7, "相同字段或组合已存在"),
    FILE_UPLOAD_OVER_MAX_SIZE(8, "文件超过指定大小"),
    ENTITY_NOT_FOUND(9, "查询记录不存在"),

    ;

    @Getter
    private final Integer code;

    private final String message;

    private final ThreadLocal<Object[]> args = new ThreadLocal<>();

    public String getMessage() {
        try {
            Object[] t = this.args.get();
            return t != null ? String.format(message, t) : message;
        } finally {
            this.args.remove();
        }
    }

    public PresetErrorCode args(Object... args) {
        this.args.set(args);
        return this;
    }
}
