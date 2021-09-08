package edu.umich.yanfuguo.kotlinChatter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import edu.umich.yanfuguo.kotlinChatter.databinding.ListitemChattBinding

// It is the controller that intermediates between the view and the model.
class ChattListAdapter(context: Context, users: ArrayList<Chatt?>) :
    ArrayAdapter<Chatt?>(context, 0, users) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = (convertView?.tag /* reuse binding */ ?: run {
            val rowView = LayoutInflater.from(context).inflate(R.layout.listitem_chatt, parent, false)
            rowView.tag = ListitemChattBinding.bind(rowView) // cache binding
            rowView.tag
        }) as ListitemChattBinding

        getItem(position)?.run {
            listItemView.usernameTextView.text = username
            listItemView.messageTextView.text = message
            listItemView.timestampTextView.text = timestamp
            listItemView.root.setBackgroundColor(Color.parseColor(if (position % 2 == 0) "#E0E0E0" else "#EEEEEE"))
        }

        return listItemView.root
    }
}