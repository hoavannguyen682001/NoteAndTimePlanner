package com.hoanv.notetimeplanner.ui.main.person.activity

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.aminography.primecalendar.civil.CivilCalendar
import com.aminography.primedatepicker.picker.PrimeDatePicker
import com.aminography.primedatepicker.picker.callback.SingleDayPickCallback
import com.bumptech.glide.Glide
import com.hoanv.notetimeplanner.data.models.UserInfo
import com.hoanv.notetimeplanner.databinding.ActivityEditProfileBinding
import com.hoanv.notetimeplanner.ui.base.BaseActivity
import com.hoanv.notetimeplanner.utils.extension.setOnSingleClickListener
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class EditProfileActivity : BaseActivity<ActivityEditProfileBinding, EditProfileVM>() {
    override val viewModel: EditProfileVM by viewModels()


    private val genders = listOf(
        "Nam", "Nữ", "Khác"
    )

    private val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

    private lateinit var imageUri: Uri
    private lateinit var registerImagePicker: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var takePicture: ActivityResultLauncher<Uri>

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                takePicture.launch(imageUri)
            }
        }

    override fun init(savedInstanceState: Bundle?) {
        initView()
        initListener()
    }

    private fun initView() {
        val userInfo = intent.getParcelableExtra<UserInfo>("USER")

        imageUri = createImageUri()

        binding.run {
            userInfo?.let {
                edtUserName.setText(userInfo.userName)
                edtEmail.setText(userInfo.userEmail)
                edtPassword.setText(userInfo.userPassword)
            }

            tvBirthDay.text = date

            registerImagePicker =
                registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                    if (uri != null) {
                        Glide.with(this@EditProfileActivity).load(uri).into(ivAvatar)
                    }
                }

            takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {
                if (it) {
                    ivAvatar.setImageURI(null)
                    ivAvatar.setImageURI(imageUri)
                }
            }

            val adapter = ArrayAdapter(
                this@EditProfileActivity, android.R.layout.simple_spinner_item, genders
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spnGender.adapter = adapter
        }
    }

    private fun initListener() {
        binding.run {
            tvBirthDay.setOnSingleClickListener {
                dateRangePicker().show(supportFragmentManager, "Calendar")
            }

            ivGallery.setOnSingleClickListener {
//                registerImagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

                requestPermission()
            }

            ivBack.setOnSingleClickListener {
                finish()
            }
        }
    }

    private fun dateRangePicker(): PrimeDatePicker {
        val callback = SingleDayPickCallback { str ->
            binding.run {
                tvBirthDay.text = getString(
                    com.hoanv.notetimeplanner.R.string.date_selected,
                    if (str.date < 10) "0${str.date}" else "${str.date}",
                    if (str.month < 9) "0${str.month + 1}" else "${str.month + 1}",
                    "${str.year}"
                )
            }
        }
        return PrimeDatePicker.bottomSheetWith(CivilCalendar()).pickSingleDay(callback)
            .initiallyPickedSingleDay(CivilCalendar()).build()
    }

    private fun createImageUri(): Uri {
        val image = File(filesDir, "camera_photos.png")
        return FileProvider.getUriForFile(this, "com.hoanv.notetimeplanner.FileProvider", image)
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            takePicture.launch(imageUri)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun setupViewBinding(inflater: LayoutInflater): ActivityEditProfileBinding =
        ActivityEditProfileBinding.inflate(inflater)
}