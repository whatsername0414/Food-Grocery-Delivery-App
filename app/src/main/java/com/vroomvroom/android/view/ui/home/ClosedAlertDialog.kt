package com.vroomvroom.android.view.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.vroomvroom.android.R

@SuppressLint("InflateParams")
class ClosedAlertDialog(val app: Activity) {

    private lateinit var dialog: AlertDialog
    private lateinit var view: View

    fun showDialog(opening: String) {
        val dialogBuilder = AlertDialog.Builder(app)
        val layoutInflater = app.layoutInflater
        view = layoutInflater.inflate(R.layout.alert_dialog_layout, null)
        val messageTv = view.findViewById<TextView>(R.id.messageTv)
        messageTv.text = app.resources.getString(R.string.closed_alert_label, opening)
        val closeImg = view.findViewById<ImageView>(R.id.close_img)
        dialogBuilder.setView(view)
        dialog = dialogBuilder.create()
        dialog.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            closeImg.setOnClickListener {
                dismiss()
            }
            show()
        }
    }
}