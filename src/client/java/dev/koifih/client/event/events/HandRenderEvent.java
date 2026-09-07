package dev.koifih.client.event.events;

import dev.koifih.client.event.Event;
import dev.koifih.client.render.EntityFill;

public final class HandRenderEvent implements Event {
    private EntityFill fill;

    public EntityFill fill() {
        return fill;
    }

    public void setFill(EntityFill fill) {
        this.fill = fill;
    }
}
