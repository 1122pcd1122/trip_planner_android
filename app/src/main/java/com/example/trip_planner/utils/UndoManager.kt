package com.example.trip_planner.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 撤销操作管理器
 * 支持删除操作的撤销功能
 */
class UndoManager(
    private val scope: CoroutineScope,
    private val undoTimeout: Long = 5000L
) {
    private var pendingAction: PendingAction? by mutableStateOf(null)

    data class PendingAction(
        val id: String,
        val type: ActionType,
        val data: Any,
        val undoAction: suspend () -> Unit,
        val timestamp: Long = System.currentTimeMillis()
    )

    enum class ActionType {
        DELETE_TRIP,
        DELETE_FAVORITE,
        DELETE_HISTORY
    }

    fun deleteWithUndo(
        id: String,
        type: ActionType,
        data: Any,
        deleteAction: () -> Unit,
        undoAction: suspend () -> Unit
    ) {
        pendingAction = PendingAction(
            id = id,
            type = type,
            data = data,
            undoAction = undoAction
        )

        deleteAction()

        scope.launch {
            delay(undoTimeout)
            pendingAction = null
        }
    }

    fun undo() {
        pendingAction?.let { action ->
            scope.launch {
                action.undoAction()
                pendingAction = null
            }
        }
    }

    fun dismiss() {
        pendingAction = null
    }

    fun hasPendingAction(): Boolean = pendingAction != null

    fun peekPendingAction(): PendingAction? = pendingAction
}
