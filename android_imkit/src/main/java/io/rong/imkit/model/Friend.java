package io.rong.imkit.model;

import io.rong.imkit.libs.pinyin.PinyinHelper;

import android.text.TextUtils;

import com.sea_monster.core.resource.model.Resource;

public class Friend implements IFriend, RCloudType, Comparable<Friend>, IFilterModel {

    private String userId;
    private String nickname;
    private String nicknamePinyin;
    private String portrait;
    private char searchKey;

    private Resource portraitResource;

    private boolean isCall = false;

    private boolean isAdd = false;
    private boolean isSub = false;
    private boolean isDel = false;
    private boolean isSelected=false;


    public Friend() {

    }

    @Override
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        createSeachKey(nickname);
    }

    private final void createSeachKey(String nickname) {

        if (TextUtils.isEmpty(nickname)) {
            return;
        }

        nicknamePinyin = PinyinHelper.getInstance().getPinyins(nickname, "");

        if (nicknamePinyin != null && nicknamePinyin.length() > 0) {
            char key = nicknamePinyin.charAt(0);
            if (key >= 'A' && key <= 'Z') {

            } else if (key >= 'a' && key <= 'z') {
                key -= 32;
            } else {
                key = '#';
            }
            searchKey = key;
        } else {
            searchKey = '#';
        }
    }

    @Override
    public String getNicknamePinyin() {
        return nicknamePinyin;
    }

    public void setNicknamePinyin(String nicknamePinyin) {
        this.nicknamePinyin = nicknamePinyin;
    }

    @Override
    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
        setPortraitResource(new Resource(portrait));
    }

    @Override
    public int compareTo(Friend another) {
        return getNicknamePinyin().compareTo(another.getNicknamePinyin());
    }

    @Override
    public String getFilterKey() {
        return getNickname() + getNicknamePinyin();
    }

    @Override
    public char getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(char searchKey) {
        this.searchKey = searchKey;
    }

    public Resource getPortraitResource() {
        return portraitResource;
    }

    public void setPortraitResource(Resource portraitResource) {
        this.portraitResource = portraitResource;
    }

    public boolean isAdd() {
        return isAdd;
    }

    public void setAdd(boolean isAdd) {
        this.isAdd = isAdd;
    }

    public boolean isSub() {
        return isSub;
    }

    public void setSub(boolean isSub) {
        this.isSub = isSub;
    }

    public boolean isDel() {
        return isDel;
    }

    public void setDel(boolean isDel) {
        this.isDel = isDel;
    }

    public boolean isCall() {
        return isCall;
    }

    public void setCall(boolean isCall) {
        this.isCall = isCall;
    }
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

}
