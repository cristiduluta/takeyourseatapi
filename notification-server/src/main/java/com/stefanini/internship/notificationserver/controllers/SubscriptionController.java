package com.stefanini.internship.notificationserver.controllers;

import com.stefanini.internship.notificationserver.exceptions.SubscriptionException;
import com.stefanini.internship.notificationserver.model.dao.SubscriptionDao;
import com.stefanini.internship.notificationserver.services.SubscriptionService;
import com.stefanini.internship.notificationserver.services.SubscriptionServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/v01")
public class SubscriptionController {

	private SubscriptionService pushNotificationsService;

	public SubscriptionController(SubscriptionServiceImpl webPushNotificationsService) {
		this.pushNotificationsService = webPushNotificationsService;
	}

	@GetMapping("/get/subscriptions")
	public ResponseEntity retrieveAllPushNotifications() {
		List<SubscriptionDao> pushNotificationsList = pushNotificationsService.findAllPushNotifications();
		if (pushNotificationsList.isEmpty()) {
			throw new SubscriptionException("List with WebPushNotifications is Empty");
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(pushNotificationsList);
		}
	}

	@PostMapping("/subscriptions")
	public ResponseEntity createNotificationSubscription(@RequestBody SubscriptionDao subscriptionDao) {

		SubscriptionDao savedNotification = pushNotificationsService.save(subscriptionDao);

		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(savedNotification.getId()).toUri();

		return ResponseEntity.created(location).build();

	}


}



