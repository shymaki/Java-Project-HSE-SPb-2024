package ui

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import model.AuthManager
import model.Comment
import navigation.UserProfileScreen
import network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.format.DateTimeFormatter

@Composable
fun CommentCard(
    comment: Comment,
    isAnswerToAnswer: Boolean = false,
    afterDeleteComment: (() -> Unit)? = null,
    isFirstInList: Boolean = false,
    isLastInList: Boolean = false,
    profilePicture: ImageBitmap? = null,
    onReply: (() -> Unit)? = null,
    reply: Pair<String, String>? = null,
    onReplyClick: (() -> Unit)? = null,
    highlightedCommentId: MutableState<Int?>? = null
) {
    val shape = RoundedCornerShape(
        topStart = if (isFirstInList) 10.dp else 0.dp,
        topEnd = if (isFirstInList) 10.dp else 0.dp,
        bottomStart = if (isLastInList) 10.dp else 0.dp,
        bottomEnd = if (isLastInList) 10.dp else 0.dp
    )
    val rating = remember { mutableStateOf(comment.likesCount) }
    val navigator = LocalNavigator.current

    val cardModifier = if (comment.id == highlightedCommentId?.value) {
        val primaryColor = MaterialTheme.colors.primary
        val infiniteTransition = rememberInfiniteTransition()
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.0f,
            targetValue = 0.3f,
            animationSpec = InfiniteRepeatableSpec(
                animation = tween(durationMillis = 400, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Modifier.drawWithContent {
            drawContent()
            drawRect(
                color = primaryColor.copy(alpha = alpha),
                topLeft = Offset(0f, -9f),
                size = size.copy(height = size.height)
            )
        }
    } else Modifier
    
    Card(
        elevation = 0.dp,
        shape = shape,
        modifier = cardModifier.widthIn(max = 500.dp).fillMaxWidth()
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                Spacer(Modifier.width(35.dp))
                if (profilePicture == null) {
                    Image(
                        Icons.Rounded.Person,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(Color(0xfff0f2f5)),
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.primaryVariant)
                            .clickable {
                                comment.user?.let {
                                    navigator?.push(UserProfileScreen(it))
                                }
                            }
                    )
                } else {
                    Image(
                        profilePicture,
                        contentDescription = null,
                        modifier = Modifier
                            .size(35.dp)
                            .clip(CircleShape)
                            .clickable {
                                comment.user?.let {
                                    navigator?.push(UserProfileScreen(it))
                                }
                            }
                    )
                }
                Spacer(Modifier.size(10.dp))
                Column {
                    Row {
                        Column(Modifier.weight(1f)) {
                            Text(
                                comment.user?.name ?: "",
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.black
                            )
                            if (reply != null) {
                                Spacer(Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Max)
                                        .clip(RoundedCornerShape(7.dp))
                                        .background(MaterialTheme.colors.background)
                                        .clickable { 
                                            onReplyClick?.invoke()
                                        }
                                        .padding(6.dp)
                                ) {
                                    Box(
                                       Modifier
                                           .fillMaxHeight()
                                           .width(4.dp)
                                           .clip(RoundedCornerShape(2.dp))
                                           .background(MaterialTheme.colors.primaryVariant)
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Column {
                                        Text(
                                            reply.first,
                                            maxLines = 1,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AppTheme.black
                                        )
                                        Text(
                                            reply.second,
                                            maxLines = 1,
                                            fontSize = 12.sp,
                                            color = AppTheme.black
                                        )
                                    }
                                }
                                Spacer(Modifier.height(3.dp))
                            }
                            Text(comment.text, fontSize = 14.sp, color = AppTheme.black)
                        }
                        Column {
                            if (comment.authorId == AuthManager.currentUser.id) {
                                Box {
                                    val expanded = remember { mutableStateOf(false) }
                                    Icon(
                                        Icons.Rounded.MoreVert,
                                        contentDescription = "Show dropdown menu",
                                        modifier = Modifier.size(25.dp).clip(CircleShape)
                                            .clickable {
                                                expanded.value = true
                                            }.padding(3.dp),
                                        tint = AppTheme.black
                                    )
                                    DropdownMenu(
                                        expanded = expanded.value,
                                        onDismissRequest = {
                                            expanded.value = false
                                        }
                                    ) {
                                        DropdownMenuItem(
                                            onClick = {
                                                deleteComment(comment, afterDeleteComment)
                                                expanded.value = false
                                            }
                                        ) {
                                            Text("Удалить комментарий")
                                        }
                                    }
                                }
                            }
                            if (isAnswerToAnswer) {
                                Icon(
                                    Icons.Rounded.KeyboardArrowUp,
                                    contentDescription = "To previous comment",
                                    modifier = Modifier.size(31.dp).clip(CircleShape).clickable { }
                                        .padding(3.dp),
                                    tint = AppTheme.black
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = comment.dateTime.format(
                                DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy")
                            ),
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            Icons.Rounded.Reply, contentDescription = "Answer",
                            modifier = Modifier.size(30.dp).clip(CircleShape)
                                .clickable {
                                    onReply?.invoke()
                                }.padding(5.dp),
                            tint = AppTheme.black
                        )
                        Icon(
                            Icons.Rounded.KeyboardArrowUp,
                            contentDescription = "Upvote comment",
                            modifier = Modifier.size(30.dp).clip(CircleShape)
                                .clickable {
                                    likeComment(comment, rating)
                                }.padding(5.dp),
                            tint = AppTheme.black
                        )
                        Text(rating.value.toString(), color = AppTheme.black, fontSize = 12.sp)
                        Icon(
                            Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Downvote comment",
                            modifier = Modifier.size(30.dp).clip(CircleShape)
                                .clickable {
                                    dislikeComment(comment, rating)
                                }.padding(5.dp),
                            tint = AppTheme.black
                        )
                    }
                }
            }
            if (!isLastInList) {
                Divider(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 50.dp/*95.dp*/, end = 20.dp, bottom = 5.dp),
                    color = Color.LightGray,
                    thickness = 0.5.dp
                )
            }
        }
    }
}

private fun deleteComment(comment: Comment, afterDeleteComment: (() -> Unit)?) {
    RetrofitClient.retrofitCall.deleteComment(comment.postId, comment.id).enqueue(
        object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.code() == 200) {
                    afterDeleteComment?.invoke()
                } else {
                    println("wrong code on deleting comment")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                println("failure on deleting comment")
            }
        }
    )
}

private fun likeComment(comment: Comment, rating: MutableState<Int>) {
    val userId = AuthManager.currentUser.id
//    val retrofitCall = RetrofitClient.retrofit.create(ApiInterface::class.java)
    val retrofitCall = RetrofitClient.retrofitCall
    retrofitCall.likeComment(comment.postId, comment.id, userId).enqueue(object : Callback<Int> {
        override fun onResponse(call: Call<Int>, response: Response<Int>) {
            if (response.code() == 200) {
                response.body()?.let {
                    rating.value = it
                }
            }
        }

        override fun onFailure(call: Call<Int>, t: Throwable) {}
    })
}

private fun dislikeComment(comment: Comment, rating: MutableState<Int>) {
    val userId = AuthManager.currentUser.id
//    val retrofitCall = RetrofitClient.retrofit.create(ApiInterface::class.java)
    val retrofitCall = RetrofitClient.retrofitCall
    retrofitCall.dislikeComment(comment.postId, comment.id, userId).enqueue(object : Callback<Int> {
        override fun onResponse(call: Call<Int>, response: Response<Int>) {
            if (response.code() == 200) {
                response.body()?.let {
                    rating.value = it
                }
            }
        }

        override fun onFailure(call: Call<Int>, t: Throwable) {}
    })
}
