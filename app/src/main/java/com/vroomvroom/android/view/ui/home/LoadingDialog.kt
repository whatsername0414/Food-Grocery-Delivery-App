package com.vroomvroom.android.view.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.vroomvroom.android.R
import com.vroomvroom.android.utils.Utils.timer

@SuppressLint("InflateParams")
class LoadingDialog(val app: Activity) {

    private var dialog: AlertDialog
    private var view: View

    init {
        val dialogBuilder = AlertDialog.Builder(app)
        val layoutInflater = app.layoutInflater
        view = layoutInflater.inflate(R.layout.loading_dialog_layout, null)
        dialogBuilder.setView(view)
        dialogBuilder.setCancelable(false)
        dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }
    
    @SuppressLint("SetTextI18n")
    fun show() {
        val progressBar = view.findViewById<ProgressBar>(R.id.place_order_progress)
        progressBar.visibility = View.VISIBLE
        val message = view.findViewById<TextView>(R.id.dialog_message_tv)
        message.text = "Creating order..."
    }

    @SuppressLint("SetTextI18n")
    fun showSuccess() {
        val progressBar = view.findViewById<ProgressBar>(R.id.place_order_progress)
        progressBar.visibility = View.GONE

        val messageTv = view.findViewById<TextView>(R.id.dialog_message_tv)
        messageTv.text = "Place order successful"

        val successMark = view.findViewById<ImageView>(R.id.success_mark)
        successMark.visibility = View.VISIBLE
        val avd = successMark.drawable as AnimatedVectorDrawable
        avd.start()

        val dismissTv = view.findViewById<TextView>(R.id.dismiss_tv)
        dismissTv.visibility = View.VISIBLE
        dismissTv.setOnClickListener {
            dismiss()
        }
        timer(dialog).start()
    }

    fun dismiss() {
        dialog.dismiss()
    }
}