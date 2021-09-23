package edu.umich.yanfuguo.kotlinChatter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import coil.load
import edu.umich.yanfuguo.kotlinChatter.databinding.ListitemChattBinding

// ChattListAdapter is the controller that intermediates between the view and the model.
// reference: https://guides.codepath.com/android/Using-an-ArrayAdapter-with-ListView
// an ArrayAdapter that convert Chatt into View
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

            // show image
            imageUrl?.let {
                listItemView.chattImage.setVisibility(View.VISIBLE)
                listItemView.chattImage.load(it) {
                    crossfade(true)
                    crossfade(1000)
                }
            } ?: run {
                // if no image has been posted with a chatt,
                // there won’t be a big empty space between this and next chatt
                listItemView.chattImage.setVisibility(View.GONE)
                listItemView.chattImage.setImageBitmap(null)
            }

            videoUrl?.let {
                listItemView.videoButton.visibility = View.VISIBLE
                listItemView.videoButton.setOnClickListener { v: View ->
                    if (v.id == R.id.videoButton) {
                        val intent = Intent(context, VideoPlayActivity::class.java)
                        intent.putExtra("VIDEO_URI", Uri.parse(it))
                        context.startActivity(intent)
                    }
                }
            } ?: run {
                listItemView.videoButton.visibility = View.INVISIBLE
                listItemView.videoButton.setOnClickListener(null)
            }
        }

        return listItemView.root
    }
}