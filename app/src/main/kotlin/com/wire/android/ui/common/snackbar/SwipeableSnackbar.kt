/*
 * Wire
 * Copyright (C) 2023 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 */

// TODO uncomment when anchoredDraggable will be available on [composeBom] version
// import androidx.compose.foundation.gestures.AnchoredDraggableState
// import androidx.compose.foundation.gestures.DraggableAnchors
// import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * A swipeable [Snackbar] that allows users to manually dismiss it by dragging.
 *
 * This composable function extends the default Snackbar behavior by adding a draggable gesture.
 * The Snackbar can be swiped horizontally to dismiss it, based on predefined positional and velocity thresholds.
 *
 * @param hostState The state of the [SnackbarHostState] this Snackbar is associated with. This allows
 * the Snackbar to notify its host when it's dismissed.
 * @param data The [SnackbarData] containing the message and optional action to display on the Snackbar.
 * @param onDismiss An optional callback function to be executed when the Snackbar is swiped away.
 * The default behavior will dismiss the current Snackbar from the [hostState].
 * @see Snackbar
 * @see SnackbarData
 * @see SnackbarHostState
 */

@Composable
fun SwipeableSnackbar(
    hostState: SnackbarHostState,
    data: SnackbarData,
    onDismiss: () -> Unit = { hostState.currentSnackbarData?.dismiss() },
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current

    val currentScreenWidth = with(density) { configuration.screenWidthDp.dp.toPx() }

// TODO uncomment when anchoredDraggable will be available on [composeBom] version
//    val anchors = DraggableAnchors {
//        SnackBarState.Visible at 0f
//        SnackBarState.Dismissed at currentScreenWidth
//    }

    // Determines how far the user needs to drag (as a fraction of total distance) for an action to be triggered.
    // In this example, the Snackbar will trigger an action if dragged to half (0.5) of its width.
    val positionalThreshold: (Float) -> Float = { distance -> distance * 0.5f }

    // Determines the minimum velocity (in pixels per second) with which the user needs to drag for an action to be triggered,
    // even if the positional threshold hasn't been reached.
    // Here, it's set to 125 device-independent pixels per second.
    val velocityThreshold: () -> Float = with(density) { { 125.dp.toPx() } }

// TODO uncomment when anchoredDraggable will be available on [composeBom] version
//    val state = remember {
//        AnchoredDraggableState(
//            initialValue = SnackBarState.Visible,
//            anchors = anchors,
//            positionalThreshold = positionalThreshold,
//            velocityThreshold = velocityThreshold,
//            animationSpec = SpringSpec(),
//            confirmValueChange = { true }
//        )
//    }

// TODO uncomment when anchoredDraggable will be available on [composeBom] version
//    LaunchedEffect(state.currentValue) {
//        if (state.currentValue == SnackBarState.Dismissed) {
//            onDismiss()
//        }
//    }

    Snackbar(
        snackbarData = data,
        modifier = Modifier
// TODO uncomment when anchoredDraggable will be available on [composeBom] version
//            .anchoredDraggable(
//                state = state,
//                orientation = Orientation.Horizontal
//            )
//            .offset {
//                IntOffset(
//                    state
//                        .requireOffset()
//                        .roundToInt(), 0
//                )
//            }
    )
}

// TODO uncomment when anchoredDraggable will be available on [composeBom] version
// private enum class SnackBarState { Visible, Dismissed }