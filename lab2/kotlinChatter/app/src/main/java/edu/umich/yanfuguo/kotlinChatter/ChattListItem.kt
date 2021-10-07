package edu.umich.yanfuguo.kotlinChatter

import android.content.Context
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

class ChattListItem(context: Context): ConstraintLayout(context) {
    val usernameTextView: TextView
    val timestampTextView: TextView
    val messageTextView: TextView

    init {
        usernameTextView = TextView(context).apply {
            id = generateViewId()
            textSize = 18.0f
        }
        timestampTextView = TextView(context).apply {
            id = generateViewId()
            textSize = 14.0f
        }
        messageTextView = TextView(context).apply {
            id = generateViewId()
            textSize = 18.0f
            setLineSpacing(0.0f, 1.2f)
        }
        // id for ChattListItem
        id = generateViewId()
        addView(usernameTextView)
        addView(timestampTextView)
        addView(messageTextView)

        val fill = LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT).apply {
            setPadding(context.dp2px(5.82f), context.dp2px(7.27f),
                context.dp2px(5.82f), context.dp2px(13.82f))
        }
        setLayoutParams(fill)

        with (ConstraintSet()) {
            clone(this@ChattListItem)

            connect(usernameTextView.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
            connect(usernameTextView.id, ConstraintSet.START, id, ConstraintSet.START)

            connect(timestampTextView.id, ConstraintSet.TOP, id, ConstraintSet.TOP)
            connect(timestampTextView.id, ConstraintSet.END, id, ConstraintSet.END)

            val margin = context.dp2px(8f)
            connect(messageTextView.id, ConstraintSet.TOP, usernameTextView.id, ConstraintSet.BOTTOM, margin)
            connect(messageTextView.id, ConstraintSet.START, id, ConstraintSet.START)

            applyTo(this@ChattListItem)
        }
    }
}