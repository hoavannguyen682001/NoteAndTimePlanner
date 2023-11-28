package com.hoanv.notetimeplanner.ui.main.tasks.list

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.hoanv.notetimeplanner.Application
import com.hoanv.notetimeplanner.data.models.Category
import com.hoanv.notetimeplanner.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor() : BaseViewModel() {

}