package com.vroomvroom.android.view.ui.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.bumptech.glide.Glide
import com.vroomvroom.android.R
import com.vroomvroom.android.databinding.PlacedOrderDialogLayoutBinding

@SuppressLint("InflateParams")
class LoadingDialog(val app: Activity) {

    private var binding: PlacedOrderDialogLayoutBinding
    private var dialog: AlertDialog

    init {
        val dialogBuilder = AlertDialog.Builder(app)
        val layoutInflater = app.layoutInflater
        binding = PlacedOrderDialogLayoutBinding.inflate(layoutInflater)
        dialogBuilder.setView(binding.root)
        dialogBuilder.setCancelable(false)
        dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
    
    fun show(title: String) {
        Glide
            .with(binding.root)
            .load(R.drawable.spinner_red_a30)
            .placeholder(R.drawable.ic_placeholder)
            .into(binding.spinner)
        binding.dialogTitle.text = title
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}