package com.example.checkbook.dialog

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.checkbook.listview.SearchViewModel
import com.example.checkbook.viewmodel.MyInfoViewModel
import com.google.firebase.database.core.Context
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MoreDialog(
    searchViewModel: SearchViewModel,
    userId: String,
    push: String,
    info_type:String,
    onSuccesss: () -> Unit,
    onError: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val titleText =
        if (info_type.equals("info")) {
            "정말 삭제하시겠습니까?"
        } else {
            "정말 신고하시겠습니까?"
        }

    val buttonText =
        if (info_type.equals("info")) {
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
                    if (info_type.equals("info")) {
                        coroutineScope.launch {
                            searchViewModel.deleteMyInfo(
                                userId,
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
                    } else {
                        coroutineScope.launch {
                            searchViewModel.declareInfo(
                                userId,
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