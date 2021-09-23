package edu.umich.yanfuguo.kotlinChatter

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.Menu.FIRST
import android.view.Menu.NONE
import android.view.MenuItem
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import edu.umich.yanfuguo.kotlinChatter.ChattStore.postChatt
import edu.umich.yanfuguo.kotlinChatter.databinding.ActivityPostBinding

class PostActivity : AppCompatActivity() {

    private lateinit var view: ActivityPostBinding
    private var enableSend = true
    private lateinit var forCropResult: ActivityResultLauncher<Intent>

    private var imageUri: Uri? = null
    private var videoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = ActivityPostBinding.inflate(layoutInflater)
        setContentView(view.root)
        val contract = ActivityResultContracts.RequestMultiplePermissions()
        // provide registerForActivityResult() with a callback handler in the form of a trailing lambda expression
        registerForActivityResult(contract) { results ->
            results.forEach { result ->
                if (!result.value) {
                    toast("${result.key} access denied")
                    finish()
                }
            }
        }.launch(arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE))

        val cropIntent = initCropIntent()
        // save the registered contract in a local variable, forPickedResult
        // providing registerForActivityResult() with a callback handler in the form of a anonymous function
        val forPickedResult =
            registerForActivityResult(ActivityResultContracts.GetContent(), fun(uri: Uri?) {
                uri?.let {
                    if (it.toString().contains("video")) {
                        videoUri = it
                        view.videoButton.setImageResource(android.R.drawable.presence_video_busy)
                    } else {
                        val inStream = contentResolver.openInputStream(it) ?: return
                        imageUri = mediaStoreAlloc("image/jpeg")
                        // make a copy before crop
                        imageUri?.let {
                            val outStream = contentResolver.openOutputStream(it) ?: return
                            val buffer = ByteArray(8192)
                            var read: Int
                            while (inStream.read(buffer).also{ read = it } != -1) {
                                outStream.write(buffer, 0, read)
                            }
                            outStream.flush()
                            outStream.close()
                            inStream.close()
                        }
                        doCrop(cropIntent)
                    }
                } ?: run { Log.d("Pick media", "failed") }
            })
        view.albumButton.setOnClickListener {
            forPickedResult.launch("*/*")
        }

        forCropResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data.let {
                        imageUri?.run {
                            // If the uncropped original is taken by the camera, we can delete the original
                            // (otherwise, the app doesn’t have permission to delete it)
                            if (!toString().contains("ORIGINAL")) {
                                // delete uncropped photo taken for posting
                                contentResolver.delete(this, null, null)
                            }
                        }
                        imageUri = it
                        imageUri?.let { view.previewImage.display(it) }
                    }
                } else {
                    Log.d("Crop", result.resultCode.toString())
                }
            }


        // check wehther the device has a camera
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            toast("Device has no camera!")
            return
        }
        // If there’s a camera, set up the ActivityResultContracts to take picture
        val forTakePicResult =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    doCrop(cropIntent)
                } else {
                    Log.d("TakePicture", "failed")
                }
            }
        view.cameraButton.setOnClickListener {
            imageUri =  mediaStoreAlloc("image/jpeg")
            forTakePicResult.launch(imageUri)
        }

        val forTakeVideoResult =
            registerForActivityResult(ActivityResultContracts.TakeVideo()) {
                view.videoButton.setImageResource(android.R.drawable.presence_video_busy)
            }
        view.videoButton.setOnClickListener {
            videoUri = mediaStoreAlloc("video/mp4")
            forTakeVideoResult.launch(videoUri)
        }
    }

    private fun initCropIntent(): Intent? {
        // Is there any published Activity on device to do image cropping?
        val intent = Intent("com.android.camera.action.CROP")
        intent.type = "image/*"
        val listofCroppers = packageManager.queryIntentActivities(intent, 0)
        // No image cropping Activity published
        if (listofCroppers.size == 0) {
            toast("Device does not support image cropping")
            return null
        }

        intent.component = ComponentName(
            listofCroppers[0].activityInfo.packageName,
            listofCroppers[0].activityInfo.name)

        // create a square crop box:
        intent.putExtra("outputX", 500)
            .putExtra("outputY", 500)
            .putExtra("aspectX", 1)
            .putExtra("aspectY", 1)
            // enable zoom and crop
            .putExtra("scale", true)
            .putExtra("crop", true)
            .putExtra("return-data", true)

        return intent
    }

    /**
     * called when user pick a image from the album or take a picture
     */
    private fun doCrop(intent: Intent?) {
        intent ?: run {
            imageUri?.let { view.previewImage.display(it) }
            return
        }

        imageUri?.let {
            intent.data = it
            forCropResult.launch(intent)
        }
    }


    /**
     * When the user picks a photo, we allocate some scratch space
     * in Android’s MediaStore for use by the cropping function.
     * To take picture and record video, we also need to allocate
     * space in the MediaStore to store the picture/video.
     */
    private fun mediaStoreAlloc(mediaType: String): Uri? {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.MIME_TYPE, mediaType)
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)

        return contentResolver.insert(
            if (mediaType.contains("video"))
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            else
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.apply {
            add(NONE, FIRST, NONE, getString(R.string.send))
            getItem(0).setIcon(android.R.drawable.ic_menu_send).setEnabled(enableSend)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == FIRST) {
            enableSend = false
            invalidateOptionsMenu()
            submitChatt()
        }
        return super.onOptionsItemSelected(item)
    }


    fun submitChatt() {
        val chatt = Chatt(username = view.usernameTextView.text.toString(),
            message = view.messageTextView.text.toString())

        postChatt(applicationContext, chatt, imageUri, videoUri) { msg ->
            runOnUiThread {
                toast(msg)
            }
            finish()
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putParcelable("imageUri", imageUri)
        savedInstanceState.putParcelable("videoUri", videoUri)
        savedInstanceState.putBoolean("enableSend", enableSend)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        imageUri = savedInstanceState.getParcelable<Uri>("imageUri")
        imageUri?.let { view.previewImage.display(it) }
        videoUri = savedInstanceState.getParcelable<Uri>("videoUri")
        enableSend = savedInstanceState.getBoolean("enableSend")
    }


}