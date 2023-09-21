package com.ninecmed.tablet.events;

public class WandConnStatusEvent {
    //TODO delete this if not required in future
    private boolean isConnected;

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
