package com.stefanini.internship.authorizationserver.controllers;

import com.stefanini.internship.authorizationserver.dao.User;
import com.stefanini.internship.authorizationserver.dto.PostUserRequest;
import com.stefanini.internship.authorizationserver.services.AuthorizationUsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.stefanini.internship.authorizationserver.utils.AppConstants.API_ROOT_VERSION;

@RestController
@Slf4j
@RequestMapping(API_ROOT_VERSION+"users")
public class UsersController {

    private AuthorizationUsersService authorizationUsersService;

    public UsersController(AuthorizationUsersService authorizationUsersService) {
        this.authorizationUsersService = authorizationUsersService;
    }

    @GetMapping()
    public ResponseEntity getAllUsers(HttpServletRequest request){
        log.info("{} request for url '{}' received",request.getMethod(), request.getRequestURI());
        List<User> users = authorizationUsersService.getAllUsers();
        log.debug("getOneUser endpoint responds with HTTP.200");
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{username}")
    public ResponseEntity getOneUser(@PathVariable String username, HttpServletRequest request){
        log.info("{} request for url '{}' received",request.getMethod(), request.getRequestURI());
        User user = authorizationUsersService.getUser(username);
        log.debug("getOneUser endpoint responds with HTTP.200");
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity createUser(@RequestBody PostUserRequest user){
        log.info(String.format("POST controllers createUser matched for user = '%s'",user.getUsername()));
        authorizationUsersService.createUser(user);
        log.debug("Controller createUser responds with HTTP.201");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{username}")
    public ResponseEntity deleteUser(@PathVariable String username){
        log.info(String.format("DELETE controllers deleteUser matched for user = '%s'",username));
        authorizationUsersService.deleteUser(username);
        log.debug("DELETE controllers deleteUser return HTTP.204");
        return ResponseEntity.noContent().build();
    }
}
