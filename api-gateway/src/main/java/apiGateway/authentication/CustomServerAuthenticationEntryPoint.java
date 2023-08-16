package apiGateway.authentication;

import org.springframework.core.io.buffer.DataBuffer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
	
	private ObjectMapper objectMapper = new ObjectMapper();

	@Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = new ErrorResponse("401 - Unauthorized!", "Please add username/password to continue further!");
        byte[] errorResponseBytes;
        try {
            errorResponseBytes = objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException jpe) {
            errorResponseBytes = "{\"message\":\"Error creating JSON response\"}".getBytes();
        }

        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(errorResponseBytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }
}