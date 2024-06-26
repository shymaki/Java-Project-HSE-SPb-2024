package files

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.ImageBitmap
import network.RetrofitClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object AvatarsDownloader {
    val ProfilePictures = mutableStateMapOf<Int, ImageBitmap?>()
    
    fun downloadProfilePicture(userId: Int) {
        if (ProfilePictures.containsKey(userId)) {
            return
        }
        RetrofitClient.retrofitCall.getUserProfilePicture(userId).enqueue(
            object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.code() == 200) {
                        response.body()?.bytes()?.let {
                            if (it.isNotEmpty()) {
                                val bitmap = bytesToImageBitmap(it)
                                ProfilePictures[userId] = bitmap
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("error on downloading profile picture")
                }

            }
        )
    }
}

expect fun bytesToImageBitmap(byteArray: ByteArray): ImageBitmap
