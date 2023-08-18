package users;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
public class UserController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BankAccountProxy bankAccountProxy;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	SecurityContext securityContext = SecurityContextHolder.getContext();
	
	// Get the Auth object
	Authentication authentication = securityContext.getAuthentication();
	
	@GetMapping("/users/all")
	public ResponseEntity<List<User>> getAllUsers() {
		List<User> users = userRepository.findAll();
			
		if (users.isEmpty()) {
			throw new ApplicationException(
		              "users-not-found",
		              "Users not FOUND!",
		              HttpStatus.NOT_FOUND
		            );
		}
					
		return ResponseEntity.status(HttpStatus.OK).body(users);
	}
	
	@GetMapping("/users/{email}")
	public ResponseEntity<User> getUser(@PathVariable("email") String email) {
			String port = environment.getProperty("local.server.port");
			
			User user = userRepository.findByEmail(email);
			
			if (user == null) {
				throw new ApplicationException(
	                "user-not-found",
	                String.format("User with email=%s not found", email),
	                HttpStatus.NOT_FOUND
				);
		}
			
		user.setEnvironment(port);
			
		return ResponseEntity.status(HttpStatus.OK).body(user);
	}
	
	@PostMapping("/users/create")
	public ResponseEntity<User> createUser(@RequestBody User user, HttpServletRequest request) {
			String role = request.getHeader("X-User-Role");
				
			String port = environment.getProperty("local.server.port");
			
			User existingUser = userRepository.findByEmail(user.getEmail());
			
			User owner = userRepository.findByRole(Role.OWNER);
			
			if (user.getEmail().equals("") || user.getPassword().equals("") || user.getEmail() == null || user.getPassword() == null || user.getRole() == null) {
				throw new ApplicationException(
		                "insert-all-values",
		                "Insert all values",
		                HttpStatus.BAD_REQUEST
					);
			}
			
			if (existingUser != null) {
				throw new ApplicationException(
		                "user-with-this-email-already-exists",
		                String.format("User with email=%s already exists", user.getEmail()),
		                HttpStatus.CONFLICT
					);
			}
			
			if(owner != null && user.getRole().equals(Role.OWNER)){
				throw new ApplicationException(
		                "owner-already-exists-in-the-database",
		                "There is already a user with role OWNER in the database",
		                HttpStatus.CONFLICT
					);
			}
			
			if (role != null) {

				if(role.endsWith("ADMIN") && (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.OWNER))) {
					throw new ApplicationException(
			                "method-not-allowed-for-the-following-role",
			                String.format("Method not allowed for following role=%s", role),
			                HttpStatus.FORBIDDEN
						);
				}
				
				if (role.endsWith("USER")) {
					throw new ApplicationException(
			                "method-not-allowed-for-the-following-role",
			                String.format("Method not allowed for following role=%s", role),
			                HttpStatus.FORBIDDEN
						);
				}
			}
			
			String hashedPassword = passwordEncoder.encode(user.getPassword());
			
			user.setPassword(hashedPassword);
			
			user.setEnvironment(port);
			
			User createdUser = userRepository.save(user);

			return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
	}
	
	@PutMapping("/users/update/{email}")
	public ResponseEntity<User> updateUser(@PathVariable("email") String email, @RequestBody User user, HttpServletRequest request) {
		String port = environment.getProperty("local.server.port");	
		
		String role = request.getHeader("X-User-Role");
		
		User existingUser = userRepository.findByEmail(email);
		User checkNewEmail = userRepository.findByEmail(user.getEmail());
		
		User owner = userRepository.findByRole(Role.OWNER);
		 		
		if (existingUser == null) {
			throw new ApplicationException(
	                "user-not-found",
	                String.format("User with email=%s not found", email),
	                HttpStatus.NOT_FOUND
				);
		}
		
		if (checkNewEmail != null) {
			throw new ApplicationException(
	                "user-with-this-email-already-exists",
	                String.format("User with email=%s already exists", user.getEmail()),
	                HttpStatus.CONFLICT
				);
		}
		
		if(owner != null && user.getRole().equals(Role.OWNER)){
			throw new ApplicationException(
	                "owner-already-exists-in-the-database",
	                "There is already a user with role OWNER in the database",
	                HttpStatus.CONFLICT
				);
		}
		
		if (role != null) {

			if(role.endsWith("ADMIN") && (user.getRole().equals(Role.ADMIN) || user.getRole().equals(Role.OWNER))) {
				throw new ApplicationException(
		                "method-not-allowed-for-the-following-role",
		                String.format("Method not allowed for following role=%s", role),
		                HttpStatus.FORBIDDEN
					);
			}
		}
		
		
		String hashedPassword = passwordEncoder.encode(user.getPassword());
		
		existingUser.setEmail(user.getEmail());
		existingUser.setPassword(hashedPassword);
		existingUser.setRole(user.getRole());
		existingUser.setEnvironment(port);
		
		User updatedUser = userRepository.save(existingUser);
		
		if (updatedUser.getRole().equals(Role.USER)) {
			bankAccountProxy.updateBankAccountEmail(email, updatedUser.getEmail());
		}
		
		return ResponseEntity.status(HttpStatus.OK).body(updatedUser);
	}
	
	@DeleteMapping("/users/delete/{email}")
	public ResponseEntity<String> deleteUser(@PathVariable("email") String email) {
		try {

			User existingUser = userRepository.findByEmail(email); 
		
			if (existingUser == null) {
				throw new ApplicationException(
						"user-not-found",
						String.format("User with email=%s not found", email),
						HttpStatus.NOT_FOUND
						);
			}
		
			if (existingUser.getRole().equals(Role.USER)) {
				bankAccountProxy.deleteBankAccount(email);
			}
			
			userRepository.delete(existingUser);
			return ResponseEntity.status(HttpStatus.OK).body("User deleted successfully.");
		
		} catch (FeignException ex) {
			throw new ApplicationException("", ex.getMessage(), HttpStatus.BAD_GATEWAY);
		}
	}
 }
