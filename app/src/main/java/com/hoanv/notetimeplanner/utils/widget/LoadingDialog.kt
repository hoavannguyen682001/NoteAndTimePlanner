package com.hoanv.notetimeplanner.utils.widget

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.hoanv.notetimeplanner.R

class LoadingDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return Dialog(requireContext(), R.style.ProgressLoading).apply {
            setContentView(R.layout.dialog_loading)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
    }

    fun show(fragmentManager: FragmentManager) {
        if (!isAdded) show(fragmentManager, TAG)
    }

    fun safeDismiss() {
        if (isAdded) {
            this.dismiss()
        }
    }

    companion object {
        private const val TAG = "LoadingDialog"
    }
}