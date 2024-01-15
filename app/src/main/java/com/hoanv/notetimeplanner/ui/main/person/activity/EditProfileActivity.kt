package com.hoanv.notetimeplanner.ui.main.person.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.aminography.primecalendar.civil.CivilCalendar
import com.aminography.primedatepicker.picker.PrimeDatePicker
import com.aminography.primedatepicker.picker.callback.SingleDayPickCallback
import com.bumptech.glide.Glide
import com.hoanv.notetimeplanner.R
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.databinding.ActivityEditProfileBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.ui.evenbus.ReloadUserInfo
import com.hoanv.notetimeplanner.utils.Pref
import com.hoanv.notetimeplanner.utils.ResponseState
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import java.io.File


@AndroidEntryPoint
class EditProfileActivity : BaseActivity<ActivityEditProfileBinding, EditProfileVM>() {
    override val viewModel: EditProfileVM by viewModels()

    private val listGenders = listOf(
        "Nam", "Nữ", "Khác"
    )

    private var imageUri: Uri? = null
    private lateinit var registerImagePicker: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var takePicture: ActivityResultLauncher<Uri>
    private lateinit var userInfo: UserInfo

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            takePicture.launch(createImageUri())
        }
    }

    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
        bindViewModel()
    }

    private fun initView() {
        intent.getParcelableExtra<UserInfo>("USER")?.let {
            userInfo = it
        }

        binding.run {
            userInfo.run {
                edtUserName.setText(userInfo.userName)
                edtEmail.setText(userInfo.userEmail)
                edtPassword.setText(userInfo.userPassword)
                tvBirthDay.text = birthDay

                Glide.with(this@EditProfileActivity).load(photoUrl ?: "")
                    .placeholder(R.drawable.img_user_avatar).dontAnimate().into(ivProfile)

                val index = when (gender) {
                    "Nam" -> {
                        0
                    }

                    "Nữ" -> {
                        1
                    }

                    else -> {
                        2
                    }
                }
                val adapter = ArrayAdapter(
                    this@EditProfileActivity, android.R.layout.simple_spinner_item, listGenders
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spnGender.adapter = adapter
                spnGender.setSelection(index)
            }

            registerImagePicker =
                registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    if (uri != null) {
                        imageUri = uri
                        Log.d("imageUri", "$imageUri")
                        Glide.with(this@EditProfileActivity).load(uri).into(ivProfile)
                    }
                }

            takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
                if (it) {
                    Log.d("imageUri", "$imageUri")
                    Glide.with(this@EditProfileActivity).load(imageUri)
                        .placeholder(R.drawable.img_user_avatar).dontAnimate().into(ivProfile)
                }
            }
        }
    }

    private fun initListener() {
        binding.run {
            tvBirthDay.setOnSingleClickListener {
                dateRangePicker().show(supportFragmentManager, "Calendar")
            }

            ivGallery.setOnSingleClickListener {
                handleOptionMenu(ivGallery)
            }

            ivProfile.setOnSingleClickListener {
                handleOptionMenu(ivGallery)
            }

            ivBack.setOnSingleClickListener {
                finish()
            }

            tvAccpet.setOnSingleClickListener {
                if (imageUri != null) {
                    viewModel.upLoadAvatar(imageUri!!)
                } else {
                    viewModel.updateUserInfo(loadUserInfo(null))
                }
            }
        }
    }

    private fun bindViewModel() {
        binding.run {
            viewModel.run {
                imageUrl.observe(this@EditProfileActivity) {
                    viewModel.updateUserInfo(loadUserInfo(it))
                }

                updateTriggerS.observe(this@EditProfileActivity) { state ->
                    when (state) {
                        ResponseState.Start -> {

                        }

                        is ResponseState.Success -> {
                            toastSuccess(state.data)
                            EventBus.getDefault().post(ReloadUserInfo(true))
                        }

                        is ResponseState.Failure -> {
                            toastError(state.throwable?.message)
                            Log.d("###", "${state.throwable?.message}")
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private fun handleOptionMenu(view: View) {
        val popupMenu = PopupMenu(this@EditProfileActivity, view)
        popupMenu.inflate(R.menu.avatar_option)
        val popupHelper =
            MenuPopupHelper(this@EditProfileActivity, popupMenu.menu as MenuBuilder, view)
        popupHelper.setForceShowIcon(true)


        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.itemCamera -> {
                    requestPermission()
                }

                R.id.itemGallery -> {
                    registerImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            }
            false
        }

        popupHelper.show()
    }

    private fun loadUserInfo(imageUrl: String?): UserInfo {
        binding.run {
            return UserInfo(
                uid = this@EditProfileActivity.userInfo.uid,
                userName = edtUserName.text.toString(),
                gender = spnGender.selectedItem.toString(),
                birthDay = tvBirthDay.text.toString(),
                userEmail = edtEmail.text.toString(),
                userPassword = edtPassword.text.toString(),
                photoUrl = imageUrl ?: userInfo.photoUrl,
                userToken = this@EditProfileActivity.userInfo.userToken
            )
        }
    }

    private fun dateRangePicker(): PrimeDatePicker {
        val callback = SingleDayPickCallback { str ->
            binding.run {
                tvBirthDay.text = getString(
                    R.string.date_selected,
                    if (str.date < 10) "0${str.date}" else "${str.date}",
                    if (str.month < 9) "0${str.month + 1}" else "${str.month + 1}",
                    "${str.year}"
                )
            }
        }
        return PrimeDatePicker.bottomSheetWith(CivilCalendar()).pickSingleDay(callback)
            .initiallyPickedSingleDay(CivilCalendar()).build()
    }

    private fun createImageUri(): Uri? {
        val image = File(filesDir, "${System.currentTimeMillis()}.png")
        imageUri = FileProvider.getUriForFile(this, "com.hoanv.notetimeplanner.FileProvider", image)
        return imageUri
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            takePicture.launch(createImageUri())
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityEditProfileBinding =
        ActivityEditProfileBinding.inflate(inflater)
}