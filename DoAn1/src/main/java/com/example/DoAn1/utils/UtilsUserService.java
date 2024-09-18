package com.example.DoAn1.utils;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.DoAn1.entities.User;
import com.example.DoAn1.exception.ExceptionCode;
import com.example.DoAn1.exception.ExceptionUser;
import com.example.DoAn1.repository.UserRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class UtilsUserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UtilsHandleJwtToken utilsHandleJwtToken;

    @Value("${jwt.time}")
    private int jwtTime;

    public boolean checkUserExist(String email) {
        User user = this.userRepository.getUserByEmail(email);
        if (user == null) {
            return true; // email exist
        } else {
            throw new ExceptionUser(ExceptionCode.EmailExistInDatabase);
        }
    }

    public void sendJwtToClient(String JwtToken, HttpServletResponse httpServletResponse) {
        setCookie("jwtToken", JwtToken, httpServletResponse);
    }

    public void setCookie(String nameCookie, String valueCookie, HttpServletResponse httpServletResponse) {
        Cookie cookie = new Cookie(nameCookie, valueCookie);
        cookie.setHttpOnly(false); // it is used to access cookie from client
        cookie.setSecure(false); // Ensure the cookie is only sent over HTTPS
        cookie.setMaxAge(jwtTime);
        cookie.setPath("/"); // Cookie will be available to the entire application
        httpServletResponse.addCookie(cookie);
    }

    // support of support
    public String getCookie(HttpServletRequest req, String nameCk) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println(cookie.getName() + ": " + cookie.getValue());
                if (nameCk.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        throw new ExceptionUser(ExceptionCode.NotFoundJwtInCookie);
    }

    public User getUserFromId(String userId) {
        Optional optional = this.userRepository.findById(userId);
        return (User) optional.get();
    }

    public User getUserFromToken(String token) {
        String userId = this.utilsHandleJwtToken.verifyToken(token);
        User user = getUserFromId(userId);
        return user;
    }
}
