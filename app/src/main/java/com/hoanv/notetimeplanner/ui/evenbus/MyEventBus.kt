package com.hoanv.notetimeplanner.ui.evenbus

import com.hoanv.notetimeplanner.data.models.UserInfo

class UserInfoEvent(val userInfo: UserInfo)

class CheckReloadListTask(val isReload: Boolean)