package com.stefanini.internship.authorizationserver.services;

import com.stefanini.internship.authorizationserver.controllers.PlaceRequestAuthorizationController;
import com.stefanini.internship.authorizationserver.dao.User;
import com.stefanini.internship.authorizationserver.dao.repositories.UserRepository;
import com.stefanini.internship.authorizationserver.dto.AuthorizationResponse;
import com.stefanini.internship.authorizationserver.dto.PlaceRequest;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class PlaceRequestAuthorizationService {

    private AuthorizationService authorizationService;
    private UserRepository userRepository;
    private UserService userService;

    private final static Logger logger = Logger.getLogger(PlaceRequestAuthorizationController.class);

    public PlaceRequestAuthorizationService(AuthorizationService authorizationService, UserRepository userRepository, UserService userService) {
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public AuthorizationResponse authorizePlaceRequest(PlaceRequest placeRequest, String permissionString){

        logger.info(String.format("Authorization requested for PlaceRequest with id='%d' for Permission='%s'",placeRequest.getId(), permissionString));
        logger.debug(String.format("Check if class-wide permission='%s' for 'PlaceRequest' is granted",permissionString));
        AuthorizationResponse roleRequest = authorizationService.checkAuthorization("PlaceRequest", "write");
        if(roleRequest.isAuthorized()) {
            logger.info(String.format("Authorization granted for PlaceRequest with id='%d' for Permission='%s' due to class-wide permission",placeRequest.getId(), permissionString));
            return roleRequest;
        }
        switch (permissionString.toLowerCase()){
            case "read":
                return canUserReadPlaceRequest(placeRequest);
            case "approve":
                return canUserApprovePlaceRequest(placeRequest);
            default:
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST,"Unexpected permission "+permissionString);
        }

    }
    private AuthorizationResponse canUserReadPlaceRequest(PlaceRequest placeRequest){
        String authenticatedUserName = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.debug(String.format("Checking read permission for User='%s' for PlaceRequest with id = '%d'",authenticatedUserName,placeRequest.getId()));
        User authenticatedUser = userRepository.findByUsername(authenticatedUserName);
        User requestedManager = userService.getUserManager(placeRequest.getId());
        boolean isViewedByCreator = placeRequest.getUserId().equals(authenticatedUser.getId());
        boolean isViewedByManager = requestedManager.getUsername().equals(authenticatedUserName);
        logger.debug(String.format("Responding with authorization based on isViedByCreator='%s' or isViewedByManager='%s'", isViewedByCreator, isViewedByManager));
        return new AuthorizationResponse(isViewedByCreator || isViewedByManager,null);
    }

    private AuthorizationResponse canUserApprovePlaceRequest(PlaceRequest placeRequest){
        String authenticatedUserName = SecurityContextHolder.getContext().getAuthentication().getName();

        User requestManager = userService.getUserManager(placeRequest.getUserId());

        boolean isAuthorized = requestManager.getUsername().equals(authenticatedUserName);
        logger.debug(String.format("Responding with authorization based on isViewedByManager='%s'", isAuthorized));

        return new AuthorizationResponse(isAuthorized,null);
    }

}
