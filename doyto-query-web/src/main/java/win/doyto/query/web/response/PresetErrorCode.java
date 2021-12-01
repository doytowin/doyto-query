package win.doyto.query.web.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

/**
 * PresetErrorCode
 *
 * @author f0rb
 */
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PresetErrorCode implements ErrorCode {
    SUCCESS("访问成功"),

    ERROR("系统内部异常"),
    REQUEST_BODY_ERROR("请求内容异常"),
    ARGUMENT_TYPE_MISMATCH("参数类型异常"),
    ARGUMENT_FORMAT_ERROR("参数格式错误: %s"),
    ARGUMENT_VALIDATION_FAILED("参数校验失败"),
    HTTP_METHOD_NOT_SUPPORTED("该接口不支持%s请求"),
    DUPLICATE_KEY_EXCEPTION("该数据已存在"),
    FILE_UPLOAD_OVER_MAX_SIZE("文件超过指定大小"),
    ENTITY_NOT_FOUND("查询记录不存在"),

    ;

    private final Integer code;

    private final String message;

    PresetErrorCode(String message) {
        this.code = Index.count++;
        this.message = message;
    }

    public ErrorCode build(Object... args) {
        return ErrorCode.build(code, String.format(message, args));
    }

    private static class Index {
        private static int count = 0;
    }
}
