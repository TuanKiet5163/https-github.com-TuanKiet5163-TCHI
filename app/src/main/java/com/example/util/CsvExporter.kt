package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.local.PostEntity
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    fun exportTransactionsToCsv(context: Context, posts: List<PostEntity>): File {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fileName = "TCHI_GiaoDich_${System.currentTimeMillis()}.csv"
        val exportFile = File(context.cacheDir, fileName)

        FileWriter(exportFile).use { writer ->
            // BOM for UTF-8 in Excel
            writer.write("\uFEFF")
            // Header
            writer.write("ID,Ten Mon Do,So Tien (VND),Danh Muc,Phuong Thuc,Ngay Tao,Quyen Rieng Tu,Ghi Chu\n")

            for (post in posts) {
                val dateStr = dateFormat.format(Date(post.createdAt))
                val escapedName = post.productName.replace("\"", "\"\"")
                val escapedNote = post.note.replace("\"", "\"\"")
                writer.write("\"${post.postId}\",\"$escapedName\",${post.amountVnd},\"${post.category}\",\"${post.paymentMethod}\",\"$dateStr\",\"${post.visibility}\",\"$escapedNote\"\n")
            }
        }

        return exportFile
    }

    fun shareCsvFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "TCHI - Xuất dữ liệu chi tiêu VNĐ")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Chia sẻ dữ liệu giao dịch TCHI")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
