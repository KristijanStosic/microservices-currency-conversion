package apiGateway.authentication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import authentication.dtos.UserDto;

@Configuration
@EnableWebFluxSecurity
public class ApiGatewayAuthentication {
	
	/*@Bean
	public MapReactiveUserDetailsService userDetailsService(BCryptPasswordEncoder encoder) {
		List<UserDetails> users = new ArrayList<>();
		users.add(User.withUsername("user")
				.password(encoder.encode("password1"))
				.roles("USER")
				.build());
		
		users.add(User.withUsername("admin")
				.password(encoder.encode("password2"))
				.roles("ADMIN")
				.build());
		
		return new MapReactiveUserDetailsService(users);
	}*/
	
	@Bean
	public MapReactiveUserDetailsService userDetailsService(BCryptPasswordEncoder encoder) {
		List<UserDetails> users = new ArrayList<>();
		List<UserDto> usersFromDatabase;
		
		ResponseEntity<UserDto[]> response = 
		new RestTemplate().getForEntity("http://localhost:8770/users/all", UserDto[].class);
		
		usersFromDatabase = Arrays.asList(response.getBody());
		
		for(UserDto ud: usersFromDatabase) {
			users.add(User.withUsername(ud.getEmail())
					.password(encoder.encode(ud.getPassword()))
					.roles(ud.getRole())
					.build());
		}
		
		return new MapReactiveUserDetailsService(users);
	}
	
	@Bean
	public BCryptPasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	 @Bean
	 public ServerAuthenticationEntryPoint customServerAuthenticationEntryPoint() {
	    return new CustomServerAuthenticationEntryPoint();
	}
	 
	 @Bean
	 public ServerAccessDeniedHandler customServerAccessDeniedHandler () {
		 return new CustomAccessDeniedHandler();
	 }

	@Bean
	public SecurityWebFilterChain filterChain(ServerHttpSecurity http, ServerAuthenticationEntryPoint authenticationEntryPoint,  CustomAccessDeniedHandler accessDeniedHandler) throws Exception {
		http.csrf().disable().authorizeExchange()
				
				.pathMatchers("/currency-exchange/**").permitAll()
				.pathMatchers("/crypto-exchange/**").permitAll()
				
				.pathMatchers("/currency-conversion/**").hasRole("USER")
				.pathMatchers("/crypto-conversion/**").hasRole("USER")
				
				.pathMatchers("/users/all").hasAnyRole("ADMIN", "OWNER")
				.pathMatchers("/users/{email}").permitAll()
				.pathMatchers("/users/create").hasAnyRole("ADMIN", "OWNER")
				.pathMatchers("/users/update/{email}").hasAnyRole("ADMIN", "OWNER")
				.pathMatchers("/users/delete/{email}").hasRole("OWNER")
				
				.pathMatchers("/bank-account/create").hasRole("ADMIN")
				.pathMatchers("/bank-account/{email}").hasRole("ADMIN")
				.pathMatchers("/bank-account/update/{email}").hasRole("ADMIN")
				.pathMatchers("/bank-account/update-email/{oldEmail}/for/{newEmail}").hasRole("ADMIN")
				.pathMatchers("/bank-account/update-balance/from/{from}/to/{to}/quantity/{quantity}/user/{email}").permitAll()
				.pathMatchers("/bank-account/delete/{email}").hasRole("ADMIN")
				
				.pathMatchers("/crypto-wallet/{email}").hasRole("ADMIN")
				.pathMatchers("/crypto-wallet/create").hasRole("ADMIN")
				.pathMatchers("/crypto-wallet/update/{email}").hasRole("ADMIN")
				.pathMatchers("/crypto-wallet/update-email/{oldEmail}/for/{newEmail}").hasRole("ADMIN")
				.pathMatchers("/crypto-wallet/update-balance/from/{from}/to/{to}/quantity/{quantity}/user/{email}").permitAll()
				.pathMatchers("/crypto-wallet/delete/{email}").hasRole("ADMIN")
				
				.pathMatchers("/trade-service/from/{from}/to/{to}/quantity/{quantity}").hasRole("USER")
				
				.and()
				.httpBasic()
				.authenticationEntryPoint(authenticationEntryPoint)
				.and()
				.exceptionHandling().accessDeniedHandler(accessDeniedHandler)
				.and()
				//This adds a filter to the filter chain. The filter is defined as a lambda function that takes two parameters: exchange and chain.
				// The exchange represents the current server exchange, and chain represents the remaining filter chain.
				.addFilterAfter((exchange, chain) -> {
					//This retrieves the security context from ReactiveSecurityContextHolder. The ReactiveSecurityContextHolder provides access to
					//the current security context in a reactive environment.
					//The getContext() method returns a Mono that emits the security context. The following map operation extracts the authentication object from the security context.
					return ReactiveSecurityContextHolder.getContext()
							.map(context -> context.getAuthentication())
							.flatMap(authentication -> {
								// This retrieves the user's role from the authentication object.
								// The authentication object represents the currently authenticated user and contains information such as the user's authorities,
								// credentials, etc. In this case, it assumes that the user has only one authority and retrieves its value using getAuthority().
								String role = authentication.getAuthorities().iterator().next().getAuthority();
								String email = authentication.getName();

								// This creates a new ServerWebExchange instance with an added header.
								// It uses the mutate() method of the original exchange to create a builder for modifying the request.
								// The header() method is used to add a new header named "X-User-Role" with the value of the role variable.
								// Finally, the build() method creates the modified exchange.
								ServerWebExchange modifiedExchange = exchange.mutate()
										.request(builder -> builder.header("X-User-Role", role))
										.request(builder -> builder.header("X-User-Email", email))
										.build();
								//This applies the modified exchange to the remaining filter chain by calling the filter() method on the chain.
								// It passes the modified exchange to the next filter in the chain.
								return chain.filter(modifiedExchange);
							});
					//This specifies the position of the filter in the filter chain.
					// The SecurityWebFiltersOrder.AUTHORIZATION argument indicates that the filter should be added after the authorization filter.
					// The SecurityWebFiltersOrder class provides constants representing the order of various security filters.
				}, SecurityWebFiltersOrder.AUTHORIZATION)
				//This continues the configuration by invoking the authorizeExchange() method.
				// This method returns an ServerHttpSecurity.AuthorizeExchangeSpec object, allowing further configuration of authorization rules for different exchanges.
				.authorizeExchange();
		
		return http.build();
	}
}
