package edu.umich.yanfuguo.kotlinjpcchatter

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley.newRequestQueue
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.reflect.full.declaredMemberProperties

// ChattStore is the Model of our app
object ChattStore {
    val chatts = mutableStateListOf<Chatt>()
    private val nFields = Chatt::class.declaredMemberProperties.size

    private lateinit var queue: RequestQueue
    private const val serverUrl = "https://3.144.110.108/"

    fun postChatt(context: Context, chatt: Chatt) {
        val jsonObj = mapOf(
            "username" to chatt.username,
            "message" to chatt.message,
            "audio" to chatt.audio
        )
        val postRequest = JsonObjectRequest(Request.Method.POST,
            serverUrl+"postaudio/", JSONObject(jsonObj),
            {
                Log.d("postChatt", "chatt posted!")
                // call getChatts() here to retrieve an updated list of chatts
                // from the back end. We take advantage of reactive update of
                // MainView to have it display an updated timeline automatically
                // without the user having to swipe to refresh. Recall that thanks
                // to reactive UI, MainView will update automatically when the chatt
                // array in ChattStore is updated.
                getChatts(context){}
            },
            { error -> Log.e("postChatt", error.localizedMessage ?: "JsonObjectRequest error") }
        )

        if (!this::queue.isInitialized) {
            queue = newRequestQueue(context)
        }
        queue.add(postRequest)
    }

    fun getChatts(context: Context, completion: () -> Unit) {
        val getRequest = JsonObjectRequest(serverUrl+"getaudio/",
            { response ->
                chatts.clear()
                val chattsReceived = try { response.getJSONArray("chatts") } catch (e: JSONException) { JSONArray() }
                for (i in 0 until chattsReceived.length()) {
                    val chattEntry = chattsReceived[i] as JSONArray
                    if (chattEntry.length() == nFields) {
                        chatts.add(Chatt(username = chattEntry[0].toString(),
                            message = chattEntry[1].toString(),
                            timestamp = chattEntry[2].toString(),
                            audio = chattEntry[3].toString()
                        ))
                    } else {
                        Log.e("getChatts", "Received unexpected number of fields: " + chattEntry.length().toString() + " instead of " + nFields.toString())
                    }
                }
                completion()
            }, { completion() }
        )

        if (!this::queue.isInitialized) {
            queue = newRequestQueue(context)
        }
        queue.add(getRequest)
    }
}