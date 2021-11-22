package com.ivyclub.contact.ui.main.friend

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ivyclub.contact.model.FriendListData
import com.ivyclub.contact.util.FriendListViewType
import com.ivyclub.data.ContactRepository
import com.ivyclub.data.model.FriendData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val repository: ContactRepository
) : ViewModel() {

    private var searchInputString = ""
    private var originEntireFriendList: List<FriendListData> = emptyList()// 다른 뷰홀더는 없고 친구들만 있는 데이터
    private var orderedEntireFriendList: List<FriendListData> =
        emptyList()// 모든 뷰타입으로 정렬된 전체 친구 데이터

    private val _isSearchViewVisible = MutableLiveData(false)
    val isSearchViewVisible: LiveData<Boolean> get() = _isSearchViewVisible
    private val _friendList = MutableLiveData<List<FriendListData>>()
    val friendList: LiveData<List<FriendListData>> get() = _friendList
    private val _isClearButtonVisible = MutableLiveData(false)
    val isClearButtonVisible: LiveData<Boolean> get() = _isClearButtonVisible
    private val _isInLongClickedState = MutableLiveData(false)
    val isInLongClickedState: LiveData<Boolean> get() = _isInLongClickedState
    private val foldedGroupNameList = mutableListOf<String>()
    val longClickedId = mutableListOf<Long>()

    init {
        getFriendDataWithFlow()
    }

    // DB에서 친구 목록 가져와서 그룹 별로 친구 추가
    fun getFriendDataWithFlow() {
        viewModelScope.launch {
            repository.loadFriendsWithFlow()
                .buffer()
                .transform { friendsData ->
                    emit(friendsData.toFriendListData())
                }
                .collect { newFriendsData ->
                    val favoriteFriendsListData =
                        newFriendsData.filter { it.isFavoriteFriend }.map { it.copy() }
                    favoriteFriendsListData.forEach { it.groupName = "즐겨찾기" }
                    val definedFriendList =
                        newFriendsData.groupBy { it.groupName }.toSortedMap().values.flatten()
                            .filterNot { it.groupName == "친구" }.toMutableList() // 그룹 지정이 된 친구 리스트
                    val undefinedFriendList =
                        newFriendsData.filter { it.groupName == "친구" } // 그룹 지정이 되지 않은 친구 리스트
                    val sortedFriendList =
                        (favoriteFriendsListData + definedFriendList + undefinedFriendList).toMutableList()
                    val newFriendList = sortedFriendList.addGroupView()
                    _friendList.postValue(newFriendList)
                    originEntireFriendList = definedFriendList + undefinedFriendList
                    orderedEntireFriendList = newFriendList
                }
        }
    }

    fun onEditTextClicked(inputString: CharSequence) {
        searchInputString = inputString.toString()
        setClearButtonVisibility(inputString.toString())
        if (searchInputString.isEmpty()) { // 입력한 글자가 없다면 원래 리스트로 교체
            _friendList.value = orderedEntireFriendList
            return
        }
        sortNameWith(inputString.toString())
    }

    fun setSearchViewVisibility() {
        _isSearchViewVisible.value = !(_isSearchViewVisible.value ?: true)
        setClearButtonVisibility(searchInputString)
    }

    fun manageGroupFolded(groupName: String) {
        if (foldedGroupNameList.contains(groupName)) { // 그룹 다시 펼치기
            foldedGroupNameList.remove(groupName)
            val groupIndex = getGroupIndex(groupName) ?: return
            val newList = generateNewList(groupName, groupIndex)
            _friendList.value = newList
        } else { // 그룹 접기
            foldedGroupNameList.add(groupName)
            val newList =
                _friendList.value?.filterNot { it.groupName == groupName && it.viewType == FriendListViewType.FRIEND }
                    ?: emptyList()
            _friendList.value = newList
        }
    }

    // 클릭이 되었으면 true, 해제되었으면 false로 넘어온다.
    // isAdd가 true면 삽입, false면 제거
    fun setLongClickedId(isAdd: Boolean, friendId: Long) {
        if (isAdd) {
            longClickedId.add(friendId)
        } else {
            longClickedId.remove(friendId)
        }
        _isInLongClickedState.value = longClickedId.isNotEmpty()
    }

    fun updateFriendsGroup(groupName: String?) {
        if (groupName == null) return
        viewModelScope.launch {
            repository.updateGroupOf(longClickedId, groupName)
            initLongClickedId() // 그룹 이동이 끝나서 저장된 값들 초기화
            getFriendDataWithFlow() // 리스트 업데이트
            clearLongClickedId() // long clicked된 id 값들 처리 해제
        }
    }

    fun clearLongClickedId() {
        longClickedId.clear()
        _isInLongClickedState.value = longClickedId.isNotEmpty()
    }

    fun getOrderedEntireFriendList() = orderedEntireFriendList

    private fun initLongClickedId() {
        longClickedId.clear()
    }

    private fun sortNameWith(inputString: String) {
        val sortedList =
            originEntireFriendList.filter { it.name.contains(inputString) }.toMutableList()
        if (inputString.isEmpty()) {
            _friendList.value = originEntireFriendList
        } else {
            _friendList.value = sortedList
        }
    }

    // 검색창이 내려와있고, 텍스트가 입력된 상황이라면 X 버튼을 활성화
    private fun setClearButtonVisibility(inputString: String) {
        _isClearButtonVisible.value = _isSearchViewVisible.value == true && inputString.isNotEmpty()
    }

    // 중간에 그룹 뷰 데이터를 넣어주는 함수
    private fun MutableList<FriendListData>.addGroupView(): MutableList<FriendListData> {
        if (this.isEmpty()) return this
        for (index in this.size - 1 downTo 0) {
            val friendData = this[index]
            if (index == 0) { // 처음은 무조건 groupName으로 시작해야 한다.
                this.add(index, getGroupData(this[index].groupName))
            } else if (index > 0) {
                val nextFriendData = this[index - 1]
                // 만약 다음 친구와 서로 다른 그룹에 있다면,
                // 이전 그룹과 분리하기 위해 선을 긋고, 그룹 뷰를 추가한다.
                if (friendData.groupName != nextFriendData.groupName) {
                    this.add(index, getGroupData(friendData.groupName))
                    this.add(index, getGroupDividerData())
                }
            }
        }
        this.add(this.size, getGroupDividerData())
        return this
    }

    private fun getGroupData(groupName: String): FriendListData {
        return FriendListData(groupName = groupName, viewType = FriendListViewType.GROUP_NAME)
    }

    private fun getGroupDividerData(): FriendListData {
        return FriendListData(viewType = FriendListViewType.GROUP_DIVIDER)
    }

    // 리사이클러 뷰에 맞는 데이터 클래스로 변경
    private fun List<FriendData>.toFriendListData(): List<FriendListData> {
        val convertedFriendList = mutableListOf<FriendListData>()
        this.forEach {
            val changedData = FriendListData(
                id = it.id,
                phoneNumber = it.phoneNumber,
                name = it.name,
                groupName = it.groupName,
                viewType = FriendListViewType.FRIEND,
                isFavoriteFriend = it.isFavorite
            )
            convertedFriendList.add(changedData)
        }
        return convertedFriendList.toList()
    }

    private fun MutableList<FriendListData>.addFavoriteGroup(): List<FriendListData> {
        val first = listOf(getGroupData("즐겨찾기"))
        val favoriteFriendList = this.filter { it.isFavoriteFriend }.sortedBy { it.name }
        val divider = getGroupDividerData()
        return first + favoriteFriendList + divider + this
    }

    private fun getGroupIndex(groupName: String): Int? {
        _friendList.value?.forEachIndexed { index, friendListData ->
            if (friendListData.groupName == groupName && friendListData.viewType == FriendListViewType.GROUP_NAME) {
                return index + 1
            }
        }
        return null
    }

    private fun generateNewList(groupName: String, groupIndex: Int): List<FriendListData> {
        val firstPart = _friendList.value?.subList(0, groupIndex) ?: emptyList()
        val middlePart =
            originEntireFriendList.filter { it.groupName == groupName && it.viewType == FriendListViewType.FRIEND }
        val lastPart =
            _friendList.value?.subList(groupIndex, _friendList.value?.size ?: 0) ?: emptyList()
        return firstPart + middlePart + lastPart
    }
}