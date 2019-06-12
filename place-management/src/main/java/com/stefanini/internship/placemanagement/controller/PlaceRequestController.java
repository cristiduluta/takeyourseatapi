package com.stefanini.internship.placemanagement.controller;

import com.stefanini.internship.placemanagement.data.entities.Place;
import com.stefanini.internship.placemanagement.data.entities.PlaceRequest;
import com.stefanini.internship.placemanagement.data.entities.ReviewedRequest;
import com.stefanini.internship.placemanagement.data.entities.User;
import com.stefanini.internship.placemanagement.data.repositories.PlaceRepository;
import com.stefanini.internship.placemanagement.data.repositories.PlaceRequestRepository;
import com.stefanini.internship.placemanagement.data.repositories.ReviewedRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;


@RestController
@RequestMapping("/api")
public class PlaceRequestController {

    @Autowired
    PlaceRequestRepository placeRequestRepository;
    @Autowired
    PlaceRepository placeRepository;
    @Autowired
    ReviewedRequestRepository reviewedRequestRepository;
    private ReviewedRequest reviewedRequest = new ReviewedRequest();


    @GetMapping("/requests")
    public ResponseEntity getAllPlaceRequests() {
        if (placeRequestRepository.findAll().isEmpty()) {
            return ResponseEntity.notFound().build();
        } else
            return ResponseEntity.status(HttpStatus.OK).body(placeRequestRepository.findAll());
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity getPlaceRequestById(@PathVariable Long id) {
        if (placeRequestRepository.getPlaceRequestById(id) == null) {
            return ResponseEntity.notFound().build();
        } else
            return ResponseEntity.status(HttpStatus.OK).body(placeRequestRepository.getPlaceRequestById(id));
    }

    @RequestMapping(value = "/requests", params = "user", method = RequestMethod.GET)
    public ResponseEntity getPlaceRequestsByUser(@RequestParam Long user) {
        List<PlaceRequest> placeRequests = placeRequestRepository.getPlaceRequestsByUserId(user);
        if (placeRequests.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(placeRequests);
    }

    @RequestMapping(value = "/requests", params = "manager", method = RequestMethod.GET)
    public ResponseEntity getPlaceRequestsByManager(@RequestParam Long manager) {
        List<PlaceRequest> placeRequests = placeRequestRepository.getPlaceRequestsByManagerId(manager);
        if (placeRequests.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(placeRequests);
    }

    @PostMapping("/requests/{placeId}")
    public ResponseEntity createPlaceRequest(@RequestBody PlaceRequest placeRequest, @PathVariable Long placeId) {
        User manager = new User()
                .setId(2L)
                .setFirstName("Mang")
                .setLastName("Nal")
                .setEmail("ggw@gmail.com")
                .setJobTitle("JManag")
                .setPassword("root")
                .setEnabled(true)
                .setAccountNonExpired(true)
                .setCredentialsNonExpired(true)
                .setAccountNonLocked(true);
        User user = new User()
                .setId(1L)
                .setFirstName("Eric")
                .setLastName("Gutu")
                .setEmail("gutsueric@gmail.com")
                .setJobTitle("JR Software Developer")
                .setPassword("root")
                .setEnabled(true)
                .setAccountNonExpired(true)
                .setCredentialsNonExpired(true)
                .setAccountNonLocked(true)
                .setManagerId(manager.getId());

        if (placeRepository.getPlaceById(placeId) == null) {
            return new ResponseEntity<>("Place not found", HttpStatus.NOT_FOUND);
        }
        Place place = placeRepository.getPlaceById(placeId);
        placeRequest.setUserId(user.getId());
        placeRequest.setPlaceId(place.getId());
        placeRequest.setDateOf(new Timestamp(System.currentTimeMillis()));
        placeRequest.setManagerId(user.getManagerId());
        if (placeRequest.getUserId().equals(place.getUserId())) {
            return new ResponseEntity<>("this user is already on this place", HttpStatus.CONFLICT);
        }
        if (place.getUserId() != null) {
            return new ResponseEntity<>("This place is occupied", HttpStatus.CONFLICT);
        }
        if (placeRequestRepository.getPlaceRequestByPlaceIdAndUserId(placeId, placeRequest.getUserId()) != null) {
            if (placeRequestRepository.getPlaceRequestByPlaceIdAndUserId(placeId, placeRequest.getUserId()).getApproved() == null) {
                return new ResponseEntity<>("such placeRequest is pending", HttpStatus.CONFLICT);
            } else if (!placeRequestRepository.getPlaceRequestByPlaceIdAndUserId(placeId, placeRequest.getUserId()).getApproved())
                return ResponseEntity.ok().body(placeRequestRepository.save(placeRequest));
        }
        return ResponseEntity.ok().body(placeRequestRepository.save(placeRequest));
    }

    @Transactional
    @PutMapping(value = "/requests/{id}")
    public ResponseEntity declinePlaceRequest(@PathVariable Long id) {
        if (placeRequestRepository.getPlaceRequestById(id) == null) {
            return ResponseEntity.notFound().build();
        }
        if (placeRequestRepository.getPlaceRequestById(id).getApproved() != null) {
            return ResponseEntity.notFound().build();
        }
        PlaceRequest newPlaceRequest = placeRequestRepository.getPlaceRequestById(id);
        newPlaceRequest.setApproved(false);
        newPlaceRequest.setReviewedAt(new Timestamp(System.currentTimeMillis()));
        deletePlaceRequest(id);
        reviewedRequestRepository.save(createReviewedRequestByPlaceRequest(newPlaceRequest));
        return ResponseEntity.ok().body(createReviewedRequestByPlaceRequest(newPlaceRequest));
    }


    @Transactional
    @PatchMapping("/requests/{id}")
    public ResponseEntity acceptPlaceRequest(@RequestBody PlaceRequest placeRequest, @PathVariable Long id) {
        if (placeRequestRepository.getPlaceRequestById(id) == null)
            return new ResponseEntity<>("placeRequest not found", HttpStatus.NOT_FOUND);
        if (placeRequestRepository.getPlaceRequestById(id).getApproved() != null)
            return new ResponseEntity<>("placeRequest already has a response", HttpStatus.CONFLICT);
        PlaceRequest updatedPlaceRequest = placeRequestRepository.getPlaceRequestById(id);
        Place placeById = placeRepository.getPlaceById(updatedPlaceRequest.getPlaceId());
        if (placeRepository.getPlaceByUserId(updatedPlaceRequest.getUserId()) != null)
            placeRepository.getPlaceByUserId(updatedPlaceRequest.getUserId()).setUserId(null);
        updatedPlaceRequest.setApproved(placeRequest.getApproved());
        updatedPlaceRequest.setReviewedAt(new Timestamp(System.currentTimeMillis()));
        placeById.setUserId(updatedPlaceRequest.getUserId());
        declineAllPlaceRequestsIfPlaceAccepted(id);
        declineAllPlaceRequestsIfUserWasAcceptedOnPlace(updatedPlaceRequest.getUserId());
        reviewedRequestRepository.save(createReviewedRequestByPlaceRequest(updatedPlaceRequest));
        placeRepository.save(placeById);
        deletePlaceRequest(id);
        return ResponseEntity.ok().body(updatedPlaceRequest);
    }

    @Transactional
    public void declineAllPlaceRequestsIfPlaceAccepted(Long placeId) {
        List<PlaceRequest> placeRequests = placeRequestRepository.findAll();
        for (PlaceRequest placeRequest : placeRequests) {
            if (placeRequest.getPlaceId().equals(placeId) && placeRequest.getApproved() == null) {
                placeRequest.setApproved(false);
                placeRequest.setReviewedAt(new Timestamp(System.currentTimeMillis()));
                reviewedRequestRepository.save(createReviewedRequestByPlaceRequest(placeRequest));
                deletePlaceRequest(placeRequest.getId());
            }
        }
    }

    @Transactional
    public void declineAllPlaceRequestsIfUserWasAcceptedOnPlace(Long userId) {
        List<PlaceRequest> placeRequests = placeRequestRepository.findAll();
        for (PlaceRequest placeRequest : placeRequests) {
            if (placeRequest.getUserId().equals(userId) && placeRequest.getApproved() == null) {
                placeRequest.setApproved(false);
                placeRequest.setReviewedAt(new Timestamp(System.currentTimeMillis()));
                reviewedRequestRepository.save(createReviewedRequestByPlaceRequest(placeRequest));
                deletePlaceRequest(placeRequest.getId());
            }
        }
    }

    private ReviewedRequest createReviewedRequestByPlaceRequest(PlaceRequest placeRequest) {
        reviewedRequest.setId(placeRequest.getId());
        reviewedRequest.setUserId(placeRequest.getUserId());
        reviewedRequest.setApproved(placeRequest.getApproved());
        reviewedRequest.setDateOf(placeRequest.getDateOf());
        reviewedRequest.setPlaceId(placeRequest.getPlaceId());
        reviewedRequest.setReviewedAt(placeRequest.getReviewedAt());
        reviewedRequest.setManagerId(placeRequest.getManagerId());
        reviewedRequest.setId(placeRequest.getId());
        return reviewedRequest;
    }

    @Transactional
    public void deletePlaceRequest(Long placeRequestId) {
        placeRequestRepository.deletePlaceRequestById(placeRequestId);
    }
}