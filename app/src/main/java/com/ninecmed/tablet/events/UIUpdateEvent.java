package com.ninecmed.tablet.events;

public class UIUpdateEvent {
    private int frag;

    private boolean uiUpdateSuccess;

    public boolean isUiUpdateSuccess() {
        return uiUpdateSuccess;
    }

    public void setUiUpdateSuccess(boolean uiUpdateSuccess) {
        this.uiUpdateSuccess = uiUpdateSuccess;
    }

    public int getFrag() {
        return frag;
    }

    public void setFrag(int fragment) {
        this.frag = fragment;
    }
}
