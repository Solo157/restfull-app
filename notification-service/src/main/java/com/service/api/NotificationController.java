package com.service.api;

import com.service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Контроллер, ендпоинты которого доступны для пользователя, которые требуют прохождение аутентификации.
 */
@RestController
@RequestMapping("/api/notification/v1")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Получить все уведомления по аккаунту (т.е. пользователя).
     */
    @GetMapping("/notifications/{userId}/messages")
    public ResponseEntity<List<NotificationMessagesDTO>> getAccountNotificationMessages(@PathVariable String userId) {
        List<NotificationMessagesDTO> userNotificationMessages = notificationService.getUserNotificationMessages(userId);

        return ResponseEntity.status(HttpStatus.OK).body(userNotificationMessages);
    }

}
