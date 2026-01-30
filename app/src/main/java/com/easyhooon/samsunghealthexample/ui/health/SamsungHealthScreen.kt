package com.easyhooon.samsunghealthexample.ui.health

import android.app.DatePickerDialog
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import java.time.LocalDate

@Composable
fun SamsungHealthScreen(
    modifier: Modifier = Modifier,
    viewModel: SamsungHealthViewModel = hiltViewModel(),
) {
    val activity = LocalActivity.current
    val state by viewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Samsung Health",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Card(
            modifier = Modifier.padding(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "상태",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.statusMessage,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Button(
            onClick = { viewModel.checkPermissions() },
            enabled = !state.isLoading,
        ) {
            Text("상태 확인")
        }

        Button(
            onClick = { activity?.let { viewModel.requestPermissions(it) } },
            enabled = !state.isLoading,
        ) {
            Text("Samsung Health 권한 요청")
        }

        Card(
            modifier = Modifier.padding(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "걸음수 데이터",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "오늘: ${state.todaySteps} 걸음",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 20.sp,
                )
                Text(
                    text = "이번 주: ${state.weekSteps} 걸음",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Button(
            onClick = { viewModel.readSteps() },
            enabled = !state.isLoading,
        ) {
            Text(if (state.isLoading) "조회 중..." else "걸음수 조회")
        }

        Card(
            modifier = Modifier.padding(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "운동 데이터",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "오늘: ${state.todayExercises.size}개 운동",
                    style = MaterialTheme.typography.headlineSmall,
                    fontSize = 20.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))
                state.todayExercises.forEach { exercise ->
                    Text(
                        text = "${exercise.exerciseTypeName}: ${exercise.getDurationMinutes()}분, ${exercise.calorie.toInt()}kcal",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (exercise.distance > 0) {
                        Text(
                            text = "  거리: ${"%.2f".format(exercise.getDistanceKm())}km",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (exercise.meanHeartRate > 0) {
                        Text(
                            text = "  평균 심박수: ${exercise.meanHeartRate.toInt()}bpm, 최대: ${exercise.maxHeartRate.toInt()}bpm",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (exercise.meanSpeed > 0) {
                        Text(
                            text = "  평균 속도: ${"%.2f".format(exercise.meanSpeed)}m/s",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Button(
            onClick = { viewModel.readExercises() },
            enabled = !state.isLoading,
        ) {
            Text(if (state.isLoading) "조회 중..." else "오늘 운동 데이터 조회")
        }

        // 날짜 범위 선택
        Card(
            modifier = Modifier.padding(8.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "날짜 범위 선택",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))

                var startDate by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
                var endDate by remember { mutableStateOf(LocalDate.now()) }
                val context = LocalContext.current

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    startDate = LocalDate.of(year, month + 1, dayOfMonth)
                                },
                                startDate.year,
                                startDate.monthValue - 1,
                                startDate.dayOfMonth,
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "시작: ${startDate}",
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }

                    Button(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    endDate = LocalDate.of(year, month + 1, dayOfMonth)
                                },
                                endDate.year,
                                endDate.monthValue - 1,
                                endDate.dayOfMonth,
                            ).show()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "종료: ${endDate}",
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.readExercisesForCustomDateRange(startDate, endDate) },
                    enabled = !state.isLoading,
                ) {
                    Text(if (state.isLoading) "조회 중..." else "날짜 범위로 조회")
                }
            }
        }

        // 에러 표시
        state.errorLevel?.let { error ->
            Card(
                modifier = Modifier.padding(8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "⚠️ 오류",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Code: ${error.errorCode}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "Message: ${error.errorMessage}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (error.resolvable) {
                        Text(
                            text = "해결 가능한 오류입니다",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
