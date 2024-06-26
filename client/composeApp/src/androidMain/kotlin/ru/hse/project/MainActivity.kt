package ru.hse.project

import App
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import platform_depended.AuthStorage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthStorage.init(this)
        val statusBarColor = Color.parseColor("#FFEBF4F8")
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                statusBarColor, statusBarColor
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.WHITE,
                Color.WHITE
            )
        )
        setContent {
            Box(Modifier.safeDrawingPadding()) {
                App()
            }
        }
    }
}

//@Preview
//@Composable
//fun PreviewSignUpUserDataForm() {
//    SignUpUserDataForm()
//}

//@Composable
//fun PreviewChooseInterestsScreen() {
//    val tags = listOf(Tag(0, "asdfghjkl"), Tag(1, "qwerty"), Tag(2, "zxcvbnm"),
//        Tag(0, "asdfghjkl"), Tag(1, "qwerty"), Tag(2, "zxcvbnm"),
//        Tag(0, "asdfghjkl"), Tag(1, "qwerty"), Tag(2, "zxcvbnm"),
//        Tag(0, "asdfghjkl"), Tag(1, "qwerty"), Tag(2, "zxcvbnm"),
//        Tag(0, "asdfghjkl"), Tag(1, "qwerty"), Tag(2, "zxcvbnm"),
//        Tag(0, "asdfghjkl"), Tag(1, "qwerty"), Tag(2, "zxcvbnm")
//    )
//    ChooseInterestsForm(tags)
//}

//@Preview
//@Composable
//fun PreviewPostCard() {
//    Column(
//        modifier = Modifier.background(Color.LightGray).padding(10.dp).fillMaxWidth()
////        modifier = Modifier.padding(10.dp).fillMaxWidth()
//            .fillMaxHeight(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        PostCard(
//            Post(
//                0,
//                0,
//                LocalDateTime.MIN,
//                listOf(Tag(0, "asdfghjkl"), Tag(1, "qwerty"), Tag(2, "zxcvbnm"),
//                    Tag(0, "asdfghjkl"), Tag(1, "qwerty"), Tag(2, "zxcvbnm"),
//                    Tag(0, "asdfghjkl"), Tag(1, "qwerty"), Tag(2, "zxcvbnm"),
//                    Tag(0, "asdfghjkl"), Tag(1, "qwerty"), Tag(2, "zxcvbnm"),
//                    Tag(0, "asdfghjkl"), Tag(1, "qwerty"), Tag(2, "zxcvbnm"),
//                    Tag(0, "asdfghjkl"), Tag(1, "qwerty"), Tag(2, "zxcvbnm")),
//                "Post title",
//                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor",
//                emptyList(),
//                1000,
//                100
//            )
//        )
//    }
//}

//@Preview
//@Composable
//fun PreviewNotificationWidget() {
//    Column(
//        modifier = Modifier.background(Color.LightGray).padding(10.dp).fillMaxWidth()
//            .fillMaxHeight(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        NotificationWidget(
//            Notification(
//                0,
//                Post(
//                    0,
//                    0,
//                    LocalDateTime.MIN,
//                    emptyList(),
//                    "Post title",
//                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor",
//                    emptyList(),
//                    1000,
//                    100
//                ),
//                Post(
//                    0,
//                    0,
//                    LocalDateTime.MIN,
//                    emptyList(),
//                    "Post title",
//                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor",
//                    emptyList(),
//                    1000,
//                    100
//                )
//            )
//        )
//    }
//}
//
//@Preview
//@Composable
//fun PreviewNewPostForm() {
//    NewPostForm()
//}
