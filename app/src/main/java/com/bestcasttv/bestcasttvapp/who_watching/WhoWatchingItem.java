package com.bestcasttv.bestcasttvapp.who_watching;

public class WhoWatchingItem {
    String profileID;
    String profileName;
    String profilePictureID;
    String profilePictureTitle;
    String profilePicture;
    String lastLogin;
    int language;
    int isChild;
    int isHavePin;
    Boolean editable;
    Boolean enableAddUser;

    public WhoWatchingItem(String profileID, String profileName, String profilePictureID, String profilePictureTitle, String profilePicture, String lastLogin, int language, int isChild, int isHavePin, Boolean editable, Boolean enableAddUser) {
        this.profileID = profileID;
        this.profileName = profileName;
        this.profilePictureID = profilePictureID;
        this.profilePictureTitle = profilePictureTitle;
        this.profilePicture = profilePicture;
        this.lastLogin = lastLogin;
        this.language = language;
        this.isChild = isChild;
        this.isHavePin = isHavePin;
        this.editable = editable;
        this.enableAddUser = enableAddUser;
    }

    public String getProfileID() {
        return profileID;
    }

    public void setProfileID(String profileID) {
        this.profileID = profileID;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfilePictureID() {
        return profilePictureID;
    }

    public void setProfilePictureID(String profilePictureID) {
        this.profilePictureID = profilePictureID;
    }

    public String getProfilePictureTitle() {
        return profilePictureTitle;
    }

    public void setProfilePictureTitle(String profilePictureTitle) {
        this.profilePictureTitle = profilePictureTitle;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(String lastLogin) {
        this.lastLogin = lastLogin;
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        this.language = language;
    }

    public int getIsChild() {
        return isChild;
    }

    public void setIsChild(int isChild) {
        this.isChild = isChild;
    }

    public int getIsHavePin() {
        return isHavePin;
    }

    public void setIsHavePin(int isHavePin) {
        this.isHavePin = isHavePin;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public Boolean getEnableAddUser() {
        return enableAddUser;
    }

    public void setEnableAddUser(Boolean enableAddUser) {
        this.enableAddUser = enableAddUser;
    }
}
