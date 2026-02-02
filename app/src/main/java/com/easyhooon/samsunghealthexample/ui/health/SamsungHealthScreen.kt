package com.easyhooon.samsunghealthexample.ui.health

import android.app.Activity
import android.app.DatePickerDialog
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.easyhooon.samsunghealthexample.model.ExerciseData
import com.easyhooon.samsunghealthexample.model.HealthError
import kotlinx.collections.immutable.ImmutableList
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun SamsungHealthScreen(
    modifier: Modifier = Modifier,
    viewModel: SamsungHealthViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SamsungHealthContent(
        modifier = modifier,
        state = state,
        onCheckPermissions = viewModel::checkPermissions,
        onRequestPermissions = viewModel::requestPermissions,
        onReadSteps = viewModel::readSteps,
        onReadExercises = viewModel::readExercises,
        onReadExercisesForDateRange = viewModel::readExercisesForCustomDateRange,
        onResolveError = viewModel::resolveError,
    )
}

@Composable
private fun SamsungHealthContent(
    modifier: Modifier = Modifier,
    state: SamsungHealthState,
    onCheckPermissions: () -> Unit,
    onRequestPermissions: (Activity) -> Unit,
    onReadSteps: () -> Unit,
    onReadExercises: () -> Unit,
    onReadExercisesForDateRange: (LocalDate, LocalDate) -> Unit,
    onResolveError: (Activity) -> Unit,
) {
    val activity = LocalActivity.current

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

        StatusCard(statusMessage = state.statusMessage)

        Button(
            onClick = onCheckPermissions,
            enabled = !state.isLoading,
        ) {
            Text("상태 확인")
        }

        Button(
            onClick = { activity?.let { onRequestPermissions(it) } },
            enabled = !state.isLoading,
        ) {
            Text("Samsung Health 권한 요청")
        }

        StepsCard(
            todaySteps = state.todaySteps,
            weekSteps = state.weekSteps,
        )

        Button(
            onClick = onReadSteps,
            enabled = !state.isLoading,
        ) {
            Text(if (state.isLoading) "조회 중..." else "걸음수 조회")
        }

        ExerciseCard(
            exercises = state.todayExercises,
            dateLabel = state.exerciseDateLabel,
        )

        Button(
            onClick = onReadExercises,
            enabled = !state.isLoading,
        ) {
            Text(if (state.isLoading) "조회 중..." else "오늘 운동 데이터 조회")
        }

        DateRangeSelectorCard(
            isLoading = state.isLoading,
            onReadExercisesForDateRange = onReadExercisesForDateRange,
        )

        state.errorLevel?.let { error ->
            ErrorCard(
                error = error,
                onResolve = { activity?.let { onResolveError(it) } },
            )
        }
    }
}

@Composable
private fun StatusCard(statusMessage: String) {
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
                text = statusMessage,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun StepsCard(
    todaySteps: Long,
    weekSteps: Long,
) {
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
                text = "오늘: $todaySteps 걸음",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp,
            )
            Text(
                text = "이번 주: $weekSteps 걸음",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    exercises: ImmutableList<ExerciseData>,
    dateLabel: String,
) {
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
                text = "$dateLabel: ${exercises.size}개 운동",
                style = MaterialTheme.typography.headlineSmall,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            exercises.forEach { exercise ->
                ExerciseItem(exercise = exercise)
            }
        }
    }
}

@Composable
private fun ExerciseItem(exercise: ExerciseData) {
    var isHeartRateExpanded by remember { mutableStateOf(false) }
    val dateTimeFormatter = remember {
        DateTimeFormatter.ofPattern("MM/dd HH:mm")
    }
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    val startDateTime = remember(exercise.startTime) {
        Instant.ofEpochMilli(exercise.startTime)
            .atZone(ZoneId.systemDefault())
            .format(dateTimeFormatter)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = "[$startDateTime] ${exercise.exerciseTypeName}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "  ${exercise.getDurationMinutes()}분, ${exercise.calorie.toInt()}kcal",
            style = MaterialTheme.typography.bodySmall,
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

        // 심박수 리스트 드롭다운
        if (!exercise.heartRates.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isHeartRateExpanded = !isHeartRateExpanded }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (isHeartRateExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isHeartRateExpanded) "접기" else "펼치기",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "심박수 기록 (${exercise.heartRates.size}개)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            AnimatedVisibility(visible = isHeartRateExpanded) {
                Column(
                    modifier = Modifier.padding(start = 16.dp),
                ) {
                    exercise.heartRates.forEach { sample ->
                        val time = Instant.ofEpochMilli(sample.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .format(timeFormatter)
                        Text(
                            text = "$time: ${sample.heartRate}bpm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun DateRangeSelectorCard(
    isLoading: Boolean,
    onReadExercisesForDateRange: (LocalDate, LocalDate) -> Unit,
) {
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    val context = LocalContext.current

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
                        text = "시작: $startDate",
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
                        text = "종료: $endDate",
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onReadExercisesForDateRange(startDate, endDate) },
                enabled = !isLoading,
            ) {
                Text(if (isLoading) "조회 중..." else "날짜 범위로 조회")
            }
        }
    }
}

@Composable
private fun ErrorCard(
    error: HealthError,
    onResolve: () -> Unit,
) {
    Card(
        modifier = Modifier.padding(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "오류",
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
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onResolve) {
                    Text("문제 해결")
                }
            }
        }
    }
}