package io.rong.imkit.fragment;

import io.rong.imkit.RCloudContext;
import io.rong.imkit.adapter.FriendListAdapter;
import io.rong.imkit.adapter.FriendListAdapter.OnFilterFinished;
import io.rong.imkit.adapter.FriendListAdapter.ViewHolder;
import io.rong.imkit.adapter.FriendMultiChoiceAdapter;
import io.rong.imkit.model.Friend;
import io.rong.imkit.utils.ResourceUtils;
import io.rong.imkit.veiw.ActionBar;
import io.rong.imkit.veiw.PinnedHeaderListView;
import io.rong.imkit.veiw.SearchHeaderView;
import io.rong.imkit.veiw.SwitchGroup;
import io.rong.imkit.veiw.SwitchGroup.ItemHander;
import io.rong.imkit.veiw.SwitchItemView;
import io.rong.imlib.RongIMClient.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;

public class FriendListFragment extends ActionBaseFrament implements ItemHander, OnClickListener, TextWatcher, OnFilterFinished, OnItemClickListener {

    protected FriendListAdapter mAdapter;
    private PinnedHeaderListView mListView;
    private SwitchGroup mSwitchGroup;
    private SearchHeaderView mSearchHeader;
    private EditText mEditText;

    protected List<Friend> mFriendsList;
    protected int mFilterFriendCount;
    private boolean isMultiChoice = false;

    private ArrayList<String> mSelectedItemIds;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(ResourceUtils.getLayoutResourceId(this.getActivity(), "rc_fragment_friend_list"), null);
        mListView = getViewById(view, android.R.id.list);
        mSwitchGroup = getViewById(view, android.R.id.message);
        mSearchHeader = new SearchHeaderView(this.getActivity());
        mEditText = mSearchHeader.getEditText();

        mListView.addHeaderView(mSearchHeader);
        // mListView.setPinnedHeaderView(LayoutInflater.from(this.getActivity()).inflate(R.layout.item_friend_index,
        // mListView, false));
        mListView.setPinnedHeaderView(getInflateView("rc_item_friend_index", mListView, false));
        mListView.setFastScrollEnabled(false);

        mListView.setOnItemClickListener(this);
        mSwitchGroup.setItemHander(this);
        mEditText.addTextChangedListener(this);

        mListView.setHeaderDividersEnabled(false);
        mListView.setFooterDividersEnabled(false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        getActionBar().setOnBackClick(new OnClickListener() {

            @Override
            public void onClick(View v) {
                FriendListFragment.this.getActivity().finish();
            }
        });

        ArrayList<UserInfo> userInfos = null;

        if (RCloudContext.getInstance().getGetFriendsProvider() != null) {
            userInfos = (ArrayList<UserInfo>) RCloudContext.getInstance().getGetFriendsProvider().getFriends();
        }

        mFriendsList = new ArrayList<Friend>();

        if (userInfos != null) {
            for (UserInfo userInfo : userInfos) {
                Friend friend = new Friend();
                friend.setNickname(userInfo.getName());
                friend.setPortrait(userInfo.getPortraitUri());
                friend.setUserId(userInfo.getUserId());
                mFriendsList.add(friend);
            }
        }

        mFriendsList = sortFriends(mFriendsList);

        if (mSelectedItemIds != null&&isMultiChoice) {

            for (String id : mSelectedItemIds) {
                for (Friend friend : mFriendsList) {
                    if (id.equals(friend.getUserId())) {
                        friend.setSelected(true);
                        break;
                    }
                }
            }
        }

        mAdapter = isMultiChoice ? new FriendMultiChoiceAdapter(getActivity(), mFriendsList,mSelectedItemIds) : new FriendListAdapter(this.getActivity(), mFriendsList);

        // mAdapter.addObserver();
        mListView.setAdapter(mAdapter);
        // mListView.addScrollStateListener(mAdapter);

        fillData();

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    private final void fillData() {

        mAdapter.removeAll();
        mAdapter.setAdapterData(mFriendsList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFilterFinished() {
        if (mFriendsList != null && mFriendsList.size() == 0) {
            return;
        }

        if (mAdapter == null || mAdapter.isEmpty()) {
        } else {
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mAdapter != null) {
            mAdapter.getFilter().filter(s);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void onClick(View v) {

        if (v instanceof SwitchItemView) {

            CharSequence tag = ((SwitchItemView) v).getText();

            if (mAdapter != null && mAdapter.getSectionIndexer() != null) {
                Object[] sections = mAdapter.getSectionIndexer().getSections();
                int size = sections.length;

                for (int i = 0; i < size; i++) {
                    if (tag.equals(sections[i])) {
                        int index = mAdapter.getPositionForSection(i);
                        mListView.setSelection(index + mListView.getHeaderViewsCount());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object tagObj = view.getTag();

        if (tagObj != null && tagObj instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) tagObj;
            mAdapter.onItemClick(viewHolder.friend.getUserId(), viewHolder.choice);
            return;
        }
    }

    @Override
    public void onDestroyView() {
        if (mAdapter != null) {
            mAdapter.destroy();
            mAdapter = null;
        }
        super.onDestroyView();
    }

    public boolean isMultiChoice() {
        return isMultiChoice;
    }

    public void setMultiChoice(boolean isMultiChoice, ArrayList<String> selectedItemIds) {
        this.isMultiChoice = isMultiChoice;
        this.mSelectedItemIds = selectedItemIds;
    }

    private ArrayList<Friend> sortFriends(List<Friend> friends) {

        String[] searchLetters = ResourceUtils.getArrayById(getActivity(), "rc_search_letters");
        HashMap<String, ArrayList<Friend>> userMap = new HashMap<String, ArrayList<Friend>>();

        ArrayList<Friend> friendsArrayList = new ArrayList<Friend>();

        for (Friend friend : friends) {
            String letter = new String(new char[]{friend.getSearchKey()});

            if (userMap.containsKey(letter)) {
                ArrayList<Friend> friendList = userMap.get(letter);
                friendList.add(friend);
            } else {
                ArrayList<Friend> friendList = new ArrayList<Friend>();
                friendList.add(friend);

                userMap.put(letter, friendList);
            }
        }

        for (int i = 0; i < searchLetters.length; i++) {
            String letter = searchLetters[i];
            ArrayList<Friend> fArrayList = userMap.get(letter);
            if (fArrayList != null) {
                friendsArrayList.addAll(fArrayList);
            }
        }

        return friendsArrayList;
    }
}