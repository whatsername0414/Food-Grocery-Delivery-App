package com.vroomvroom.android.view.ui.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.vroomvroom.android.R

@SuppressLint("InflateParams")
class ReceiptDialog(activity: Activity) {

    private var dialog: AlertDialog
    private var view: View

    init {
        val dialogBuilder = AlertDialog.Builder(activity)
        val layoutInflater = activity.layoutInflater
        view = layoutInflater.inflate(R.layout.receipt_dialog_layout, null)
        dialogBuilder.setView(view)
        dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun show() {
        dialog.show()
    }

}