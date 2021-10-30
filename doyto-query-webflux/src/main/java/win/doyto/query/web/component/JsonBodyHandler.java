package win.doyto.query.web.component;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.HandlerResultHandler;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.AbstractMessageWriterResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import win.doyto.query.web.response.ErrorCode;
import win.doyto.query.web.response.JsonBody;


/**
 * RestResponseAdvice
 *
 * @author f0rb on 2017-01-15.
 */
@Slf4j
@Component
public class JsonBodyHandler extends AbstractMessageWriterResultHandler implements HandlerResultHandler {
    private final MethodParameter errorCodeParameter;
    private final ErrorCode defaultResponse;

    @SneakyThrows
    public JsonBodyHandler(ServerCodecConfigurer serverCodecConfigurer, RequestedContentTypeResolver resolver) {
        super(serverCodecConfigurer.getWriters(), resolver, ReactiveAdapterRegistry.getSharedInstance());
        setOrder(90);
        defaultResponse = ErrorCode.build(null);
        errorCodeParameter = new MethodParameter(JsonBodyHandler.class.getDeclaredMethod("errorCodeParameter"), -1);
    }

    @SuppressWarnings("unused")
    private Mono<ErrorCode> errorCodeParameter() {
        return null;
    }

    @Override
    public boolean supports(HandlerResult result) {
        boolean supports = isMono(result) && needTransform(result) && isAnnotatedByJsonBody(result);
        MethodParameter returnType = result.getReturnTypeSource();
        log.debug("Support method: [{}] {}#{}", supports, returnType.getDeclaringClass().getName(), returnType.getExecutable().getName());
        return supports;
    }

    private boolean isMono(HandlerResult result) {
        Class<?> resolve = result.getReturnType().resolve();
        return Mono.class.isAssignableFrom(resolve);
    }

    private boolean needTransform(HandlerResult result) {
        return !ErrorCode.class.isAssignableFrom(result.getReturnType().resolveGeneric(0));
    }

    private boolean isAnnotatedByJsonBody(HandlerResult result) {
        MethodParameter returnType = result.getReturnTypeSource();
        return returnType.getDeclaringClass().isAnnotationPresent(JsonBody.class);
    }

    @Override
    @SuppressWarnings({"unchecked", "java:S2259"})
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
        Mono<ErrorCode> body = ((Mono<?>) result.getReturnValue())
                .<ErrorCode>map(ErrorCode::build)
                .defaultIfEmpty(defaultResponse);
        return writeBody(body, errorCodeParameter, result.getReturnTypeSource(), exchange);
    }
}
