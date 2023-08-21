package cryptoWallet;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "users")
public interface UserProxy {

   @GetMapping("/users/{email}")
   UserDto getUserByEmail(@PathVariable("email") String email);
}