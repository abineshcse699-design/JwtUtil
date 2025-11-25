package com.example.task2.controller;

import com.example.task2.dto.RegisterRequest;
import com.example.task2.entity.Role;
import com.example.task2.entity.User;
import com.example.task2.repository.RoleRepository;
import com.example.task2.repository.UserRepository;
import com.example.task2.security.JwtUtil;
import io.jsonwebtoken.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



import java.beans.Encoder;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(JwtUtil jwtUtil, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //RegisterUser Api
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest){
        //check username exists
        if(userRepository.findByUsername(registerRequest.getUsername()).isPresent()){
            return ResponseEntity.badRequest().body("username is already taken");
        }
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());

        String encodedPassword=passwordEncoder.encode(registerRequest.getPassword());
        newUser.setPassword(encodedPassword);
        System.out.println("EncodedPassword:"+encodedPassword);

        //convert Rolename into role entities and assign user
        Set<Role>roles=new HashSet<>();
        for (String roleName:registerRequest.getRoles()){
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found:" + roleName)) ;  // ← missing ;

            roles.add(role);

        }
        newUser.setRoles(roles);
        userRepository.save(newUser);
        return ResponseEntity.ok("User Resgister sucessfully");
    }
    //login Api
    @PostMapping("/login")
    public ResponseEntity<String>login(@RequestBody User loginRequest){
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),loginRequest.getPassword()));

        }catch (Exception e){
            System.out.println("Exception:"+e);
        }
        String token=jwtUtil.generateToken(loginRequest.getUsername());
        return ResponseEntity.ok(token);
    }

}





