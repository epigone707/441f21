package edu.umich.yanfuguo.kotlinjpcchatter

import android.content.Context
import android.util.Log
import androidx.compose.runtime.*
import edu.umich.yanfuguo.kotlinjpcchatter.ChatterID.expiration
import edu.umich.yanfuguo.kotlinjpcchatter.ChatterID.id
import edu.umich.yanfuguo.kotlinjpcchatter.ChatterID.save
import edu.umich.yanfuguo.kotlinjpcchatter.R.string.clientID
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.time.Instant
import kotlin.reflect.full.declaredMemberProperties

// ChattStore is the Model of our app
object ChattStore: CoroutineScope by MainScope() {
    val chatts = mutableStateListOf<Chatt>()
    private val nFields = Chatt::class.declaredMemberProperties.size

    private const val serverUrl = "https://3.144.110.108/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(serverUrl)
        .build()
    private val chatterAPIs = retrofit.create(ChatterAPIs::class.java)

    private val retrofitExCatcher = CoroutineExceptionHandler { _, error ->
        Log.e("Retrofit exception", error.localizedMessage ?: "NETWORKING ERROR")
    }

//    fun postChatt(context: Context, chatt: Chatt) {
//        val jsonObj = mapOf(
//            "username" to chatt.username,
//            "message" to chatt.message
//        )
//        val postRequest = JsonObjectRequest(
//            Request.Method.POST,
//            serverUrl+"postchatt/", JSONObject(jsonObj),
//            { Log.d("postChatt", "chatt posted!") },
//            { error -> Log.e("postChatt", error.localizedMessage ?: "JsonObjectRequest error") }
//        )
//
//        if (!this::queue.isInitialized) {
//            queue = newRequestQueue(context)
//        }
//        queue.add(postRequest)
//    }
    suspend fun postChatt(chatt: Chatt): Boolean {
        val jsonObj = mapOf(
                "chatterID" to id,
                "message" to chatt.message
            )
        val requestBody = JSONObject(jsonObj).toString().toRequestBody("application/json".toMediaType())

        lateinit var response: Response<ResponseBody>
        withContext(retrofitExCatcher) {
            // Use Retrofit's suspending POST request and wait for the response
            response = chatterAPIs.postchatt(requestBody)
        }
        if (!response.isSuccessful) {
            Log.e("postChatt", response.errorBody()?.string() ?: "Retrofit error")
            // Android Studio false positive WARNING on .string()
            // https://github.com/square/retrofit/issues/3255
        }
        return response.isSuccessful
    }

//    fun getChatts(context: Context, completion: () -> Unit) {
//        val getRequest = JsonObjectRequest(serverUrl+"getchatts/",
//            { response ->
//                chatts.clear()
//                val chattsReceived = try { response.getJSONArray("chatts") } catch (e: JSONException) { JSONArray() }
//                for (i in 0 until chattsReceived.length()) {
//                    val chattEntry = chattsReceived[i] as JSONArray
//                    if (chattEntry.length() == nFields) {
//                        chatts.add(Chatt(username = chattEntry[0].toString(),
//                            message = chattEntry[1].toString(),
//                            timestamp = chattEntry[2].toString()))
//                    } else {
//                        Log.e("getChatts", "Received unexpected number of fields: " + chattEntry.length().toString() + " instead of " + nFields.toString())
//                    }
//                }
//                completion()
//            }, { completion() }
//        )
//
//        if (!this::queue.isInitialized) {
//            queue = newRequestQueue(context)
//        }
//        queue.add(getRequest)
//    }
    fun getChatts() {
        // MainScope
        launch(retrofitExCatcher) {
            // Use Retrofit's suspending GET request and wait for the response
            val response = chatterAPIs.getchatts()
            if (response.isSuccessful) {
                val chattsReceived = try {
                    JSONObject(response.body()?.string() ?: "").getJSONArray("chatts")
                    // Android Studio false positive WARNING on .string()
                    // https://github.com/square/retrofit/issues/3255
                } catch (e: JSONException) {
                    JSONArray()
                }

                chatts.clear()
                for (i in 0 until chattsReceived.length()) {
                    val chattEntry = chattsReceived[i] as JSONArray
                    if (chattEntry.length() == nFields) {
                        chatts.add(
                            Chatt(
                                username = chattEntry[0].toString(),
                                message = chattEntry[1].toString(),
                                timestamp = chattEntry[2].toString()
                            )
                        )
                    } else {
                        Log.e(
                            "getChatts",
                            "Received unexpected number of fields: " + chattEntry.length()
                                .toString() + " instead of " + nFields.toString()
                        )
                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    suspend fun addUser(context: Context, idToken: String?): Boolean {
        var userAdded = false

        idToken ?: return userAdded

        val jsonObj = mapOf(
            "clientID" to context.getString(clientID),
            "idToken" to idToken
        )
        val requestBody = JSONObject(jsonObj).toString().toRequestBody("application/json".toMediaType())

        withContext(retrofitExCatcher) {
            // Use Retrofit's suspending POST request and wait for the response
            val response = chatterAPIs.adduser(requestBody)

            if (response.isSuccessful) {
                val responseObj = JSONObject(response.body()?.string() ?: "")
                // obtain chatterID from back end
                // Upon receiving a chatterID from the Chatter back-end server,
                // addUser() stores it in the ChatterID singleton along with a
                // computed expiration time. If we did not retrieve a valid chatterID
                // from the back end, we return false.
                id = try { responseObj.getString("chatterID") } catch (e: JSONException) { null }
                expiration = Instant.now().plusSeconds(try { responseObj.getLong("lifetime") } catch (e: JSONException) { 0L })

                id?.let {
                    userAdded = true
                    // will save() chatterID later
                    save(context)
                }
            } else {
                Log.e("addUser", response.errorBody()?.string() ?: "Retrofit error")
            }
        }
        return userAdded
    }
}

/**
 * Retrofit is set up differently from Volley and OkHttp3:
 * whereas in Volley and OkHttp3 we constructed each API Url
 * as we’re about to make an HTTP request, in Retrofit we create,
 * ahead of time, an interface in which each API Url is encoded
 * into its own method.
 */
interface ChatterAPIs {
    @GET("getchatts/")
    suspend fun getchatts(): Response<ResponseBody>

    @POST("postauth/")
    suspend fun postchatt(@Body requestBody: RequestBody): Response<ResponseBody>

    @POST("adduser/")
    suspend fun adduser(@Body requestBody: RequestBody): Response<ResponseBody>
}