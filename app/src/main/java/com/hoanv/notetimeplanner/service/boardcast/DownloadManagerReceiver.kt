package com.hoanv.notetimeplanner.service.boardcast

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import es.dmoral.toasty.Toasty

class DownloadManagerReceiver() : BroadcastReceiver() {
        @SuppressLint("Range")
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                // Lấy ID của download từ intent
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                // Kiểm tra xem download có thành công không
                val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        // Hiển thị Toast khi tải xong
                        Toasty.info(context, "Tải xuống thành công", Toast.LENGTH_SHORT).show()
                    } else {
                        // Hiển thị Toast khi tải thất bại
                        Toasty.info(context, "Tải xuống thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.d("DownloadManagerReceiver", "Received broadcast")

                cursor.close()
            }
        }
    }