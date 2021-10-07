package edu.umich.yanfuguo.kotlinChatter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
//import edu.umich.yanfuguo.kotlinChatter.databinding.ListitemChattBinding

// ChattListAdapter is the controller that intermediates between the view and the model.
// reference: https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView
// an ArrayAdapter that convert Chatt into View
class ChattListAdapter(context: Context, users: ArrayList<Chatt?>) :
    ArrayAdapter<Chatt?>(context, 0, users) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // as? is safe-casting, safe-casting returns null if fail
        // in this case, if fail, create a new ChattListItem
        val listItemView = convertView as? ChattListItem ?: ChattListItem(context)

        listItemView.setBackgroundColor(Color.parseColor(if (position % 2 == 0) "#E0E0E0" else "#EEEEEE"))

        getItem(position)?.run {
            listItemView.usernameTextView.text = username
            listItemView.messageTextView.text = message
            listItemView.timestampTextView.text = timestamp
        }

        return listItemView
    }
}