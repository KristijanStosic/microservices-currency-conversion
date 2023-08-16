package apiGateway.authentication;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Mono;

@Component
public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {

	@Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
		exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        
        ErrorResponse errorResponse = new ErrorResponse("403 - Access Denied!", "You are not able to perform this action!");
        
        String errorResponseJson = "{\"error\":\"" + errorResponse.getError() + "\",\"message\":\"" + errorResponse.getMessage() + "\"}";

        byte[] errorResponseBytes = errorResponseJson.getBytes();
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorResponseBytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }


}
