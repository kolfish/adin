package dev.koifih.client.event.events;

import dev.koifih.client.event.Event;
import dev.koifih.client.render.entity.EntityFill;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public final class HandRenderEvent implements Event {
    @Getter
    private EntityFill fill;

    public void setFill(EntityFill fill) {
        this.fill = fill;
    }
}
