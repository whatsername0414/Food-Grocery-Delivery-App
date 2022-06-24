package com.vroomvroom.android.view.ui.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.vroomvroom.android.R
import com.vroomvroom.android.utils.ClickType

@SuppressLint("InflateParams")
class CommonAlertDialog(activity: Activity) {

    private var dialog: AlertDialog
    private var view: View

    init {
        val dialogBuilder = AlertDialog.Builder(activity)
        val layoutInflater = activity.layoutInflater
        view = layoutInflater.inflate(R.layout.common_alert_dialog_layout, null)
        dialogBuilder.setView(view)
        dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun show(
        title: String,
        message: String,
        leftButtonTitle: String = "",
        rightButtonTitle: String,
        isButtonLeftVisible: Boolean = true,
        isCancellable: Boolean = true,
        listener: (ClickType) -> Unit
    ) {
        val titleTv = view.findViewById<TextView>(R.id.title_tv)
        val messageTv = view.findViewById<TextView>(R.id.message_tv)
        val leftButton = view.findViewById<TextView>(R.id.btn_left)
        val rightButton = view.findViewById<MaterialButton>(R.id.btn_right)

        dialog.setCancelable(isCancellable)
        leftButton.isVisible = isButtonLeftVisible

        titleTv.text = title
        messageTv.text = message
        leftButton.text = leftButtonTitle
        rightButton.text = rightButtonTitle

        leftButton.setOnClickListener {
            listener.invoke(ClickType.NEGATIVE)
        }
        rightButton.setOnClickListener {
            listener.invoke(ClickType.POSITIVE)
        }

        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

}