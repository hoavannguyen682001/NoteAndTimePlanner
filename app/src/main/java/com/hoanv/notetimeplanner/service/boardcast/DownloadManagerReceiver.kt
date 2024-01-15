package com.hoanv.notetimeplanner.service.boardcast

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DownloadManagerReceiver(private val downloadId: Long) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    // Tải về đã hoàn tất, xử lý tệp tin ở đây nếu cần
                    Toast.makeText(context, "Tải xuống thành công", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }