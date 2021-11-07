package edu.umich.yanfuguo.kotlinjpcchatter

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import edu.umich.yanfuguo.kotlinjpcchatter.ChattStore.chatts
import edu.umich.yanfuguo.kotlinjpcchatter.ChattStore.getChatts

@Composable
fun MainView(context: Context, navController: NavHostController) {
    /*
        Even though isRefreshing is a MutableState, it is declared
        inside a composable, which means that it is destroyed and
        recreated every time the composable is recomposed.
        To retain the value a MutableState across recompositions,
        we tag it with remember {}.
     */
    var isRefreshing by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(R.string.chatter),
                    fontSize = 20.sp
                )
            })
        },
        floatingActionButton = {
            FloatingActionButton(
                backgroundColor = Color(0xFFFFC107),
                contentColor = Color(0xFF00FF00),
                modifier = Modifier.padding(0.dp, 0.dp, 8.dp, 8.dp),
                onClick = {
                    // navigate to PostView
                    navController.navigate("PostView")
                }
            ) {
                Icon(Icons.Default.Add, stringResource(R.string.post))
            }
        }
    ) {
        // content of Scaffold
        /*
            When the user swipes down on the screen, SwipeRefresh shows a “loading”
            icon and run the onRefresh lambda expression. The variable isRefreshing
            is used to control when SwipeRefresh finishes running and stops showing
            the “loading” icon. SwipeRefresh requires isRefreshing to be a published
            state variable, which is why we declare it a mutableStateOf()
         */
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            modifier = Modifier.background(color = Color(0xFFEFEFEF)),
            onRefresh = {
                getChatts()
                isRefreshing = false
            }
        ) {
            // describe the View
            LazyColumn(
                verticalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 0.dp)
                    .background(color = Color(0xFFEFEFEF))
            ) {
                items(count = chatts.size) { index ->
                    ChattListRow(index, chatts[index])
                }
            }
        }
    }
}