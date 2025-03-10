package com.courr.checkbook.dialog

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.courr.checkbook.listview.SearchViewModel
import com.courr.checkbook.viewmodel.MyInfoViewModel
import com.courr.checkbook.viewmodel.RepleViewModel
import kotlinx.coroutines.launch
import java.util.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RepleMoreDialog(
    searchViewModel: SearchViewModel,
    myInfoViewModel: MyInfoViewModel,
    repleViewModel: RepleViewModel,
    userId: String,
    myId: String,
    push: String,
    info_push: String,
    onSuccesss: () -> Unit,
    onError: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val currentUser by myInfoViewModel.currentUser.observeAsState()
    val user = currentUser?.email?.substringBefore("@") ?: null
    var my_data = false
    if (user!=null){
        my_data = if (userId.equals(user)) true else false
    }


    val titleText =
        if (my_data) {
            "정말 삭제하시겠습니까?"
        } else {
            "정말 신고하시겠습니까?"
        }

    val buttonText =
        if (my_data) {
            "삭제"
        } else {
            "신고"
        }

    AlertDialog(
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        onDismissRequest = onDismiss,
        title = { Text(text = titleText) },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    if (my_data) {
                        coroutineScope.launch {
                            repleViewModel.deleteReple(
                                userId,
                                push,
                                info_push,
                                onError = { errorMessage ->
                                    onError(errorMessage)
                                    isLoading = false
                                },
                                onSuccess = {
                                    onSuccesss()
                                    isLoading = false
                                })
                        }
                    } else {
                        coroutineScope.launch {
                            repleViewModel.declareReple(
                                myId,
                                push,
                                onError = { errorMessage ->
                                    onError(errorMessage)
                                    isLoading = false
                                },
                                onSuccess = {
                                    onSuccesss()
                                    isLoading = false
                                })
                        }
                    }

                },
                enabled = !isLoading
            ) {
                Text(buttonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}