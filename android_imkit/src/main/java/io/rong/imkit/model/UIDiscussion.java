package io.rong.imkit.model;

import io.rong.imkit.utils.ParcelUtils;
import io.rong.imlib.RongIMClient.Discussion;
import io.rong.imlib.RongIMClient.UserInfo;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class UIDiscussion implements RCloudType, Parcelable {

    private String id;
    private String name;
    private String adminId;
    private List<String> userIds;
    private String portiantUrl;
    private ArrayList<UserInfo> members;
    private int memberCount;
    private boolean isOpen = true;//0:开放 1:非开放

    private boolean isAlertNewMessage;
    private boolean isShowMemberName;

    private ArrayList<String> memberPortaintUrl;

    private ArrayList<String> portaintUrl;

    public UIDiscussion() {

    }

    public UIDiscussion(Parcel in) {

        id = ParcelUtils.readStringFromParcel(in);
        name = ParcelUtils.readStringFromParcel(in);
        adminId = ParcelUtils.readStringFromParcel(in);
        userIds = ParcelUtils.readListStingToParcel(in);

        portiantUrl = ParcelUtils.readStringFromParcel(in);
        members = (ArrayList<UserInfo>) ParcelUtils.readListFromParcel(in, UserInfo.class);
        memberCount = in.readInt();
        isAlertNewMessage = in.readInt() == 1 ? true : false;
        isShowMemberName = in.readInt() == 1 ? true : false;
        memberPortaintUrl = (ArrayList<String>) ParcelUtils.readListStingToParcel(in);
        isOpen = in.readInt() == 1 ? true : false;
    }

    public UIDiscussion(Discussion discussion) {

        id = discussion.getId();
        name = discussion.getName();
        adminId = discussion.getCreatorId();
        userIds = discussion.getMemberIdList();
        isOpen = discussion.isOpen();
        isAlertNewMessage = false;

    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPortiantUrl() {
        return portiantUrl;
    }

    public void setPortiantUrl(String portiantUrl) {
        this.portiantUrl = portiantUrl;
    }

    public ArrayList<UserInfo> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<UserInfo> members) {
        this.members = members;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public boolean isAlertNewMessage() {
        return isAlertNewMessage;
    }

    public void setAlertNewMessage(boolean isAlertNewMessage) {
        this.isAlertNewMessage = isAlertNewMessage;
    }

    public boolean isShowMemberName() {
        return isShowMemberName;
    }

    public void setShowMemberName(boolean isShowMemberName) {
        this.isShowMemberName = isShowMemberName;
    }

    public ArrayList<String> getPortaintUrl() {
        return portaintUrl;
    }

    public void setPortaintUrl(ArrayList<String> portaintUrl) {
        this.portaintUrl = portaintUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        ParcelUtils.writeStringToParcel(dest, id);
        ParcelUtils.writeStringToParcel(dest, name);
        ParcelUtils.writeStringToParcel(dest, adminId);
        ParcelUtils.writeListStingToParcel(dest, userIds);

        ParcelUtils.writeStringToParcel(dest, portiantUrl);
        ParcelUtils.writeListToParcel(dest, members);
        dest.writeInt(memberCount);
        dest.writeInt(isAlertNewMessage == true ? 1 : 0);
        dest.writeInt(isShowMemberName == true ? 1 : 0);

        ParcelUtils.writeListStingToParcel(dest, memberPortaintUrl);
        dest.writeInt(isOpen ? 1 : 0);

    }

    public static final Creator<UIDiscussion> CREATOR = new Creator<UIDiscussion>() {

        @Override
        public UIDiscussion createFromParcel(Parcel source) {

            return new UIDiscussion(source);

        }

        @Override
        public UIDiscussion[] newArray(int size) {

            return new UIDiscussion[size];

        }

    };

    public ArrayList<String> getMemberPortaintUrl() {
        return memberPortaintUrl;
    }

    public void setMemberPortaintUrl(ArrayList<String> memberPortaintUrl) {
        this.memberPortaintUrl = memberPortaintUrl;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(ArrayList<String> userIds) {
        this.userIds = userIds;
    }

}
