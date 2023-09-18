package com.ninecmed.tablet.events;

public class WandConnStatusEvent {
    private boolean isConnected;

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
