package com.foodplatform.notification.infrastructure.security;

import java.util.Set;
import java.util.UUID;

public record GatewayAuthenticatedUser(UUID userId, Set<String> roles) {}
