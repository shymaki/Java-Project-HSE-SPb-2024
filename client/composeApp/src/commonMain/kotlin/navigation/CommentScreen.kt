package navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.material.icons.rounded.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import files.AvatarsDownloader.ProfilePictures
import files.AvatarsDownloader.downloadProfilePicture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.AuthManager
import model.Comment
import model.Post
import model.User
import network.CommentCreate
import network.RetrofitClient
import platform_depended.Platform
import platform_depended.getPlatform
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ui.AppTheme
import ui.CommentCard
import ui.PostCard

class CommentScreen(private val postId: Int) : Screen {
    private var commentText: MutableState<String>? = null
    private val replyTo = mutableStateOf<Comment?>(null)
    private var coroutineScope: CoroutineScope? = null
    private var initialized = false

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        commentText = remember { mutableStateOf("") }
        val highlightedCommentId = remember { mutableStateOf<Int?>(null) }

        DisposableEffect(null) {
            onDispose {
                initialized = false
            }
        }

        val lazyListState = rememberLazyListState()
        val reachedBottom = remember {
            derivedStateOf {
                val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
                lastVisibleItem?.index ==
                        lazyListState.layoutInfo.totalItemsCount - 1 &&
                        initialized
            }
        }

        val refreshHelper = remember { mutableStateOf(RefreshCommentsHelper()) }
        coroutineScope = rememberCoroutineScope()

        LaunchedEffect(reachedBottom.value) {
            coroutineScope?.launch {
                while (reachedBottom.value) {
                    refreshHelper.value.loadMore()
                    delay(1000)
                }
            }
        }

        Scaffold(
            topBar = {
                Row(Modifier.fillMaxWidth()) {
                    BackButton(LocalNavigator.currentOrThrow)
                    if (getPlatform() == Platform.DESKTOP) {
                        Spacer(Modifier.weight(1f))
                        RefreshButton.Content()
                    }
                }
            },
            bottomBar = {
                Column(
                    modifier = Modifier.fillMaxWidth().background(Color.White).padding(7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    replyTo.value?.let { reply ->
                        Row(
                            modifier = Modifier
                                .widthIn(max = 500.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colors.background)
                                .padding(6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Reply,
                                contentDescription = null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(MaterialTheme.colors.primaryVariant)
                                    .padding(3.dp),
                                tint = AppTheme.black
                            )
                            Spacer(Modifier.width(13.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    refreshHelper.value.users[reply.authorId]?.name ?: "",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.black
                                )
                                Text(
                                    reply.text,
                                    maxLines = 1,
                                    fontSize = 12.sp,
                                    color = AppTheme.black
                                )
                            }
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colors.primaryVariant,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        replyTo.value = null
                                    }.padding(2.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.widthIn(max = 500.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = commentText?.value ?: "",
                            onValueChange = {
                                commentText?.value = it
                            },
                            label = {
                                Text("Комментарий")
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                        IconButton(
                            onClick = {
                                coroutineScope?.launch {
                                    refreshHelper.value.sendComment()
                                }
                            },
                            enabled = commentText?.value?.isNotEmpty() ?: false
                        ) {
                            Icon(
                                Icons.Rounded.Send, contentDescription = "Send comment",
                                tint = AppTheme.black
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                Modifier.padding(
                    bottom = innerPadding.calculateBottomPadding(),
                    top = innerPadding.calculateTopPadding()
                )
            ) {
                RefreshableContent(refreshHelper) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(
                            top = 10.dp,
                            bottom = 10.dp
                        ),
                        state = lazyListState
                    ) {
                        item {
                            refreshHelper.value.post.value?.let { post ->
                                post.user = refreshHelper.value.users[post.userId]
                                PostCard(
                                    post,
                                    isInCommentsScreen = true,
                                    afterDeletePost = {
                                        navigator?.pop()
                                    },
                                    profilePicture = ProfilePictures[post.userId]
                                )
                                Spacer(Modifier.size(15.dp))
                            }
                        }
                        itemsIndexed(refreshHelper.value.comments) { index, comment ->
                            comment.user = refreshHelper.value.users[comment.authorId]
                            CommentCard(
                                comment,
                                afterDeleteComment = {
                                    refreshHelper.value.load()
                                },
                                isFirstInList = index == 0,
                                isLastInList = index == refreshHelper.value.comments.size - 1,
                                profilePicture = ProfilePictures[comment.authorId],
                                onReply = {
                                    replyTo.value = comment
                                },
                                reply = if (comment.replyToCommentId == null ||
                                    comment.replyToCommentId == -1
                                ) {
                                    null
                                } else {
                                    val replyComment = refreshHelper.value.comments[
                                        refreshHelper.value.idByComment[comment.replyToCommentId]!!
                                    ]
                                    Pair(
                                        refreshHelper.value.users[replyComment.authorId]?.name
                                            ?: "",
                                        replyComment.text
                                    )
                                },
                                onReplyClick = {
                                    refreshHelper.value.idByComment[comment.replyToCommentId]?.let {
                                        coroutineScope?.launch {
                                            lazyListState.scrollToItem(it + 1)
                                            highlightedCommentId.value = comment.replyToCommentId
                                            delay(800)
                                            highlightedCommentId.value = null
                                        }
                                    }
                                },
                                highlightedCommentId = highlightedCommentId
                            )
                        }
                    }
                }
            }
        }
    }

    inner class RefreshCommentsHelper : Refreshable() {
        val post = mutableStateOf<Post?>(null)
        val comments = mutableStateListOf<Comment>()
        val idByComment = mutableStateMapOf<Int, Int>()
        val users = mutableStateMapOf<Int, User>()

        fun sendComment() {
            if (commentText == null) {
                return
            }
            RetrofitClient.retrofitCall.addComment(
                postId,
                CommentCreate(
                    AuthManager.currentUser.id,
                    commentText?.value ?: "",
                    replyTo.value?.id ?: -1
                )
            ).enqueue(object : Callback<network.Comment> {
                override fun onResponse(
                    call: Call<network.Comment>,
                    response: Response<network.Comment>
                ) {
                    if (response.code() == 200) {
                        commentText?.value = ""
                        replyTo.value = null
//                        load()
                    } else {
                        println("sending comment wrong code")
                    }
                }

                override fun onFailure(call: Call<network.Comment>, t: Throwable) {
                    println("sending comment failure")
                }
            })
        }

        private fun getUsersList(userIds: Set<Int>) {
            RetrofitClient.retrofitCall.getUsersList(userIds)
                .enqueue(object : Callback<List<network.User>> {
                    override fun onFailure(
                        call: Call<List<network.User>>,
                        t: Throwable
                    ) {
                        println("get users list failure")
                    }

                    override fun onResponse(
                        call: Call<List<network.User>>,
                        response: Response<List<network.User>>
                    ) {
                        response.body()?.let {
                            it.forEach { user ->
                                users[user.userId] = user.convertUser()
                                if (!ProfilePictures.containsKey(user.userId)) {
                                    downloadProfilePicture(user.userId)
                                }
                            }
                        }
                    }
                })
        }

        private fun getPost(postId: Int, userIds: MutableSet<Int>) {
            RetrofitClient.retrofitCall.getPost(postId).enqueue(object : Callback<network.Post> {
                override fun onFailure(call: Call<network.Post>, t: Throwable) {
                    println("refresh comments screen failure")
                }

                override fun onResponse(
                    call: Call<network.Post>,
                    response: Response<network.Post>
                ) {
                    response.body()?.let {
                        post.value = it.convertPost()
                    }
                    post.value?.let {
                        userIds.add(it.userId)
                    }

                    getUsersList(userIds)
                }
            })
        }

        private fun getComments() {
            RetrofitClient.retrofitCall.getComments(postId)
                .enqueue(object : Callback<List<network.Comment>> {
                    override fun onFailure(call: Call<List<network.Comment>>, t: Throwable) {
                        println("refresh comments screen failure")
                        isRefreshing = false
                    }

                    override fun onResponse(
                        call: Call<List<network.Comment>>,
                        response: Response<List<network.Comment>>
                    ) {
                        if (response.code() == 200) {
                            comments.clear()
                            idByComment.clear()
                            val userIds = mutableSetOf<Int>()
                            response.body()?.let {
                                comments.addAll(it.mapIndexed { index, comment ->
                                    userIds.add(comment.authorId)
                                    idByComment[comment.id] = index
                                    comment.convertComment()
                                })
                                initialized = true
                            }
                            getPost(postId, userIds)
                        } else {
                            println("refresh comments screen wrong code")
                        }
                        isRefreshing = false
                    }
                })
        }

        fun loadMore() {
            coroutineScope?.launch {
                RetrofitClient.retrofitCall.getMoreComments(
                    postId,
                    comments.lastOrNull()?.id ?: -1
                ).enqueue(object : Callback<List<network.Comment>> {
                    override fun onResponse(
                        call: Call<List<network.Comment>>,
                        response: Response<List<network.Comment>>
                    ) {
                        if (response.code() == 200) {
                            response.body()?.let {
                                val userIds = mutableSetOf<Int>()
                                val commentsCnt = comments.size
                                comments.addAll(it.mapIndexed { index, comment ->
                                    userIds.add(comment.authorId)
                                    idByComment[comment.id] = commentsCnt + index
                                    comment.convertComment()
                                })
                                getUsersList(userIds)
                            }
                        } else {
                            println("wrong code on getting more comments")
                        }
                    }

                    override fun onFailure(call: Call<List<network.Comment>>, t: Throwable) {
                        println("failure on getting more comments")
                    }

                })
            }
        }

        override fun load() {
            isRefreshing = true
            getComments()
        }
    }
}
