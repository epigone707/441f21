package edu.umich.yanfuguo.kotlinChatter

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import edu.umich.yanfuguo.kotlinChatter.ChattStore.chatts
import edu.umich.yanfuguo.kotlinChatter.ChattStore.getChatts

class MainActivity : AppCompatActivity() {
    private lateinit var chattListAdapter: ChattListAdapter
    private lateinit var view: MainView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = MainView(this)

        chattListAdapter = ChattListAdapter(this, chatts)
        view.chattListView.setAdapter(chattListAdapter)

        view.postButton.setOnClickListener {
            startActivity(Intent(this, PostActivity::class.java))
        }

        view.refreshContainer.setOnRefreshListener { refreshTimeline() }

        setContentView(view)

        refreshTimeline()
    }

    private fun refreshTimeline() {
        getChatts(applicationContext) {
            runOnUiThread {
                // inform the list adapter that data set has changed
                // so that it can redraw the screen.
                chattListAdapter.notifyDataSetChanged()
            }
            // stop the refreshing animation upon completion:
            view.refreshContainer.isRefreshing = false
        }
    }
}