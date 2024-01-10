package com.hoanv.notetimeplanner.ui.main.person.activity

import android.net.Uri
import com.hoanv.notetimeplanner.data.repository.remote.RemoteRepo
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import com.hoanv.notetimeplanner.utils.Pref
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditProfileVM @Inject constructor(
    val remoteRepo: RemoteRepo
) : BaseViewModel() {


    fun updateUserInfo() {

    }

    fun upLoadAvatar(imageUri: Uri) {
        remoteRepo.uploadAvatar(Pref.userId, imageUri) {

        }
    }
}