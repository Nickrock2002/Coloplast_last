package com.ninecmed.tablet.events;

public class UIUpdateEvent {
    private TabEnum tabEnum;

    private boolean uiUpdateSuccess;

    public boolean isUiUpdateSuccess() {
        return uiUpdateSuccess;
    }

    public void setUiUpdateSuccess(boolean uiUpdateSuccess) {
        this.uiUpdateSuccess = uiUpdateSuccess;
    }

    public TabEnum getTabEnum() {
        return tabEnum;
    }

    public void setTabEnum(TabEnum tabEnum) {
        this.tabEnum = tabEnum;
    }
}
