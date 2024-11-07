/**
 * Copyright © 2016-2024 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.service.update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thingsboard.rule.engine.api.NotificationCenter;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.notification.info.GeneralNotificationInfo;
import org.thingsboard.server.common.data.notification.targets.platform.SystemAdministratorsFilter;
import org.thingsboard.server.dao.notification.DefaultNotifications;
import org.thingsboard.server.queue.util.AfterStartUp;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeprecationService {

    private final NotificationCenter notificationCenter;

    @Value("${queue.type}")
    private String queueType;

    @AfterStartUp(order = Integer.MAX_VALUE)
    public void checkDeprecation() {
        checkQueueTypeDeprecation();
    }

    private void checkQueueTypeDeprecation() {
        String queueTypeName;
        switch (queueType) {
            case "aws-sqs" -> queueTypeName = "AWS SQS";
            case "pubsub" -> queueTypeName = "PubSub";
            case "service-bus" -> queueTypeName = "Azure Service Bus";
            case "rabbitmq" -> queueTypeName = "RabbitMQ";
            default -> {
                return;
            }
        }

        log.warn("WARNING: Starting with ThingsBoard 4.0, {} will no longer be supported as a message queue for microservices. " +
                "Please migrate to Apache Kafka. This change will not impact any rule nodes", queueTypeName);
        notificationCenter.sendGeneralWebNotification(TenantId.SYS_TENANT_ID, new SystemAdministratorsFilter(),
                DefaultNotifications.queueTypeDeprecation.toTemplate(), new GeneralNotificationInfo(Map.of(
                        "queueType", queueTypeName
                )));
    }

}
