package win.doyto.query.web.response;

/**
 * ErrorCode
 *
 * @author f0rb on 2021-10-30
 */
public interface ErrorCode {

    Integer getCode();

    String getMessage();

    default boolean isSuccess() {
        return Integer.valueOf(0).equals(getCode());
    }

    static <D> JsonResponse<D> build(D data) {
        return new JsonResponse<D>().setData(data);
    }
}
