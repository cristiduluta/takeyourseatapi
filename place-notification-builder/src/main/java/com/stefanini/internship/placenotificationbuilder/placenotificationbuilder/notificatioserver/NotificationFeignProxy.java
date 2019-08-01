package com.stefanini.internship.placenotificationbuilder.placenotificationbuilder.notificatioserver;

import com.stefanini.internship.placenotificationbuilder.placenotificationbuilder.model.dto.NotificationBuilder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "place-notification-builder", url ="localhost:8087" )
public interface NotificationFeignProxy {

	@PostMapping("/api/v01/notifications/{reviewer}/managers")
	 ResponseEntity receiveManagerNotificationJSON(@PathVariable("reviewer") String reviewer,
												   @RequestBody NotificationBuilder objectJSON,
												   @RequestHeader("Authorization") String token);

}
