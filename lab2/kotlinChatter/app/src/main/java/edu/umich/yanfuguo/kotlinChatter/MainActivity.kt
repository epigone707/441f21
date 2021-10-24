package edu.umich.yanfuguo.kotlinChatter

import android.Manifest
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import androidx.activity.result.contract.ActivityResultContracts
import edu.umich.yanfuguo.kotlinChatter.ChattStore.chatts
import edu.umich.yanfuguo.kotlinChatter.ChattStore.getChatts

class MainActivity : AppCompatActivity() {
    private lateinit var chattListAdapter: ChattListAdapter
    private lateinit var view: MainView
    private var xdown: Float = 0f
    private var ydown: Float = 0f

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
        val contract = ActivityResultContracts.RequestPermission()
        val locationPermissionRequest = registerForActivityResult(contract){
                granted ->
            if (!granted) {
                toast("Fine location access denied", false)
                finish()
            }
        }
        locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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

    // detect swipe left gesture and launch MapsActivity
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        super.dispatchTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                xdown = event.x
                ydown = event.y
            }
            MotionEvent.ACTION_UP -> {
                if ((xdown - event.x) > 100 && abs(event.y - ydown) < 100) {
                    startActivity(Intent(this, MapsActivity::class.java))
                }
            }
        }
        return false
    }
}