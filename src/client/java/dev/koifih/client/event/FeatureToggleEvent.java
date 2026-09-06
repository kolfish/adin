package dev.koifih.client.event;

import dev.koifih.client.feature.Feature;

public record FeatureToggleEvent(Feature feature, boolean enabled) implements Event {
}
