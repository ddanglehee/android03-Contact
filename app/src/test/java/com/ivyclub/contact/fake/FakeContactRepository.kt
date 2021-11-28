package com.ivyclub.contact.fake

import com.ivyclub.data.ContactRepository
import com.ivyclub.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.sql.Date

class FakeContactRepository : ContactRepository {

    private val friendList = mutableListOf<FriendData>()

    override suspend fun loadFriends(): List<FriendData> {
        return friendList
    }

    override suspend fun saveFriend(friendData: FriendData) {
        friendList.add(friendData)
    }

    override suspend fun setFavorite(id: Long, state: Boolean) {
        // todo
    }

    override suspend fun getFriendDataById(id: Long): FriendData {
        return FriendData(
            "",
            "",
            "",
            -1L,
            emptyList(),
            true,
            emptyMap()
        ) // todo
    }

    override suspend fun updateFriend(
        phoneNumber: String,
        name: String,
        birthday: String,
        groupId: Long,
        extraInfo: Map<String, String>,
        id: Long
    ) {
        // todo
    }

    override fun loadFriendsWithFlow(): Flow<List<FriendData>> {
        return flow {
            emit(friendList)
        }
    }

    override suspend fun getFavoriteFriends(): List<FriendData> {
        return emptyList() // todo
    }

    override suspend fun deleteFriend(id: Long) {
        // todo
    }

    override suspend fun getLastFriendId(): Long {
        return -1L // todo
    }

    override fun setShowOnBoardingState(state: Boolean) {
        // todo
    }

    override suspend fun getShowOnBoardingState(): Boolean {
        return true // todo
    }

    override suspend fun setNotificationTime(start: Int, end: Int) {
        // todo
    }

    override suspend fun setNotificationOnOff(state: Boolean) {
        // todo
    }

    override fun loadPlanListWithFlow(): Flow<List<SimplePlanData>> {
        return flow {

        } // todo
    }

    override suspend fun getPlanDataById(planId: Long): PlanData {
        return PlanData(emptyList(), Date(-1L), "", "", "", "") // todo
    }

    override suspend fun savePlanData(planData: PlanData, lastParticipants: List<Long>): Long {
        return -1L // todo
    }

    override suspend fun deletePlanData(planData: PlanData) {
        // todo
    }

    override suspend fun getSimpleFriendDataListByGroup(groupId: Long): List<SimpleFriendData> {
        return emptyList() // todo
    }

    override suspend fun getSimpleFriendDataById(friendId: Long): SimpleFriendData {
        return SimpleFriendData(-1L, "", "") // todo
    }

    override suspend fun getSimpleFriendData(): List<SimpleFriendData> {
        return emptyList() // todo
    }

    override suspend fun getPlansByIds(planIds: List<Long>): List<PlanData> {
        return emptyList() // todo
    }

    override suspend fun getPlanListAfter(current: Long): List<SimplePlanData> {
        return emptyList() // todo
    }

    override suspend fun updatePlansParticipants(newParticipants: List<Long>, planId: Long) {
        // todo
    }

    override fun getStartAlarmHour(): Int {
        return 0 // todo
    }

    override fun getEndAlarmHour(): Int {
        return 0 // todo
    }

    override suspend fun loadGroups(): List<GroupData> {
        return emptyList() // todo
    }

    override suspend fun saveNewGroup(groupData: GroupData) {
        // todo
    }

    override suspend fun updateGroupOf(targetFriend: List<Long>, targetGroup: Long) {
        // todo
    }

    override suspend fun deleteGroup(groupData: GroupData) {
        // todo
    }

    override suspend fun getGroupNameById(id: Long): String {
        return " "// todo
    }

    override fun loadGroupsWithFlow(): Flow<List<GroupData>> {
        return flow {
            emit(
                listOf(
                    GroupData(
                        "friend"
                    )
                )
            )
        }
    }

    override suspend fun updateGroupName(id: Long, name: String) {
        // todo
    }

    override suspend fun savePassword(password: String) {
        // todo
    }

    override suspend fun getPassword(): String {
        return "" // todo
    }

    override suspend fun removePassword() {
        // todo
    }

    override suspend fun setFingerPrintState(state: Boolean) {
        // todo
    }

    override suspend fun getFingerPrintState(): Boolean {
        return true // todo
    }
}