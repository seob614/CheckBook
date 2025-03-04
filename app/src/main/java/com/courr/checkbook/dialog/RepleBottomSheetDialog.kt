package com.courr.checkbook.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepleBottomSheetDialog(
    isVisible: Boolean,
    onClickCancel: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState()

    // 바텀 시트의 상태가 변경될 때마다 모달 상태를 업데이트
    LaunchedEffect(isVisible) {
        if (isVisible) {
            modalBottomSheetState.show() // 바텀 시트 열기
        } else {
            modalBottomSheetState.hide() // 바텀 시트 닫기
        }
    }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = { onClickCancel() },
            sheetState = modalBottomSheetState,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(Color.White)  // DragHandle의 배경을 흰색으로 설정
                ) {
                    Divider(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.LightGray)
                            .width(35.dp)
                            .height(3.dp)
                    )
                }
            }

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .background(Color.White),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "더 이상 정보가 없습니다.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()

                )
            }
        }
    }
}