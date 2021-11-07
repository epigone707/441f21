package edu.umich.yanfuguo.kotlinjpcchatter

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import edu.umich.yanfuguo.kotlinjpcchatter.ChattStore.addUser
import edu.umich.yanfuguo.kotlinjpcchatter.R.string.clientID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@Composable
fun SigninView(context: Context, navController: NavHostController) {
    var isPresented by rememberSaveable{ mutableStateOf(false) }

    if (isPresented) {
        return
    }
    // Build a GoogleSignInClient with specified GoogleSignInOptions.
    val signinClient = GoogleSignIn.getClient(context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // DEFAULT_SIGN_IN options request user's ID and basic profile.
            // ID Token is not part of DEFAULT_SIGN_IN and must be additionally
            // requested, as shown. Email address, if desired, must also be
            // requested with .requestEmail(/*void*/), not done here.
            .requestIdToken(stringResource(clientID))
            .build())

    fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        val account = try {
            completedTask.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Refer to the GoogleSignInStatusCodes class reference for more information:
            // https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignInStatusCodes?hl=pt-br
            context.toast("Failed Google SignIn ${e.localizedMessage}\nIs application.id in Module's build.gradle as registered?")
            navController.popBackStack(route="MainView",inclusive = false)
            return
        }

        // Sign-In succeded and we try to register user with Chatter back end.
        // If registration succeeded, we return user to PostView so that they
        // can post a chatt. Otherwise, we return user to MainView. In both cases,
        // we toggle isPresented of SigninView() to true to prevent duplicated sign in.

        account?.let {
            // Successful SignIn, toggle isPresented and addUser to Chatter back end, obtain chatterID
            isPresented = true
            MainScope().launch {
                if (addUser(context, it.idToken)) {
                    navController.popBackStack()
                } else {
                    context.toast("Sign in problem.  Please try again.")
                    navController.popBackStack("MainView", inclusive = false)
                }
            }
        }
    }

    // Since we are in a composable that can be recomposed at any time, we use
    // rememberLauncherForActivityResult() instead of registerForActivityResult()
    // in creating the contract so that it persists across recompositions. Also
    // note that rememberLauncherForActivity() can only be invoked directly from
    // inside a @Composable function.
    val forSigninResult =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(result.data))
        }
    // Once have we created the forSigninResult activity-result contract,
    // we check whether the user is signed in. If the user is not signed in,
    // we set up a standard Google Sign-In button that launches the Google
    // Sign-In activity when clicked. The Google Sign-In activity is launched
    // with the forSigninResult contract.

    // Is the user signed in?
    getLastSignedInAccount(context) ?: run {
        // User is not signed in
        Box(contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(1f)) {
            // Set the size of the signinButton
            // The SignInButton() as defined in Google’s Sign-In SDK is a traditional
            // Android View UI element, not a composable. We wrap this UI element inside
            // AndroidView() composable, which then allows us to wrap it inside a Box()
            // composable inside our SigninView() composable.
            AndroidView(factory = { context ->
                SignInButton(context).apply {
                    setSize(SignInButton.SIZE_STANDARD)
                    setOnClickListener {
                        // and call GoogleSignIn() when the button is clicked
                        forSigninResult.launch(signinClient.signInIntent)
                    }
                }
            })
        }
        return
    }
    // User is SignedIn, refresh idToken
    signinClient.silentSignIn().addOnCompleteListener(context.mainExecutor) { task ->
        handleSignInResult(task)
    }
}

