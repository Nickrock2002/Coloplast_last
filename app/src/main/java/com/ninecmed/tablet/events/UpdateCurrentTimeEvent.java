package com.ninecmed.tablet.events;

public class UpdateCurrentTimeEvent {
    private String date;
    private String time;
    private boolean resetTheDefaultTextsOnTherapy;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean shouldResetTheDefaultTextsOnTherapy() {
        return resetTheDefaultTextsOnTherapy;
    }

    public void resetTheDefaultTextsOnTherapy(boolean resetTheDefaultTextsOnTherapy) {
        this.resetTheDefaultTextsOnTherapy = resetTheDefaultTextsOnTherapy;
    }
}
