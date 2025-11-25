package com.example.task2.security;

import com.example.task2.entity.Role;
import com.example.task2.entity.User;
import com.example.task2.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Component

public class JwtUtil {
    //secreat key


    private static final SecretKey secretKey =Keys.secretKeyFor(SignatureAlgorithm.HS512);
    //expiration Time
    private final int jwtExpirationMs=86400000;

    private UserRepository  userRepository;
    public JwtUtil(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    public String generateToken(String username){
        Optional<User> user =userRepository.findByUsername(username);
        Set<Role> roles=user.get().getRoles();


        return Jwts.builder().setSubject(username).claim("roles",roles.stream()
                .map(role -> role.getName()).collect(Collectors.joining(",")))
                .setIssuedAt(new Date()).setExpiration(new Date(new Date().getTime()+jwtExpirationMs))
                .signWith(secretKey).compact();

    }
    //Extract user name
    public String extractUsername(String token){
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();

    }
   //Extract roles
    public Set<String> extractRoles(String token){
        String rolesString =Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().get("roles",String.class);
        return Set.of(rolesString);

    }
    //Token Validation
    public boolean isTokenValid(String token){
        try{
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;

        }catch(JwtException |  IllegalArgumentException exception){
            return false;

        }
    }

}




