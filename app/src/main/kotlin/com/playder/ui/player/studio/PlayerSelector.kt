package com.playder.ui.player.studio

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.playder.R
import com.playder.constants.PlayerStyle
import com.playder.ui.player.styles.AlternatePlayerStyle
import com.playder.ui.player.styles.StyleActions
import kotlin.math.abs
import kotlin.math.roundToInt

/** the selector's geometry at progress [e] (0 the full player, 1 cards) on a [w] x [h] screen */
internal class CardGeometry(val e: Float, val w: Float, val h: Float, gapPx: Float) {
    val scale = 1f + (CardScale - 1f) * e
    val stride = w * scale + gapPx
    val centerY = h / 2f + (CardCenterY * h - h / 2f) * e
    val cardW = w * scale
    val cardH = h * scale
    fun centerX(page: Int, position: Float) = w / 2f + (page - position) * stride
    fun visible(page: Int, position: Float) = abs(centerX(page, position) - w / 2f) < w / 2f + cardW / 2f
}

/**
 * Applies the selector card transform to the live player sheet: the player shrinks into the card
 * of the style it's showing, which follows the selector's scroll. The backgrounds box (the cover
 * editor scrolled down) shrinks it the same way, into a box higher up.
 */
fun Modifier.studioSheetLayer(studio: PlayerStudioState): Modifier = graphicsLayer {
    // under the middle part's still, nothing of the live player needs drawing
    if (studio.sheetCovered) {
        alpha = 0f
        return@graphicsLayer
    }
    val e = studio.selector.value
    val b = studio.backdrop.value * studio.cover.value
    if (e <= 0.0005f && b <= 0.0005f) return@graphicsLayer
    val w = size.width
    val h = size.height
    val gap = CardGap.toPx()
    var scale = 1f
    var tx = 0f
    var ty = 0f
    var corner = 0f
    if (e > 0.0005f) {
        val g = CardGeometry(e, w, h, gap)
        scale = g.scale
        tx = g.centerX(studio.livePage, studio.page.value) - w / 2f
        ty = g.centerY - h / 2f
        corner = CardCorner.toPx() * e.coerceIn(0f, 1f)
    }
    if (b > 0.0005f) {
        val s = 1f + (BoxScale - 1f) * b
        scale *= s
        // the live box sits where the applied background is among the boxes being swiped through
        tx += (studio.backdropCommitted - studio.backdropPage.value) * (w * s + gap)
        ty += (BoxCenterY * h - h / 2f) * b
        corner = maxOf(corner, BoxCorner.toPx() * b.coerceIn(0f, 1f))
    }
    scaleX = scale
    scaleY = scale
    translationX = tx
    translationY = ty
    // the shape is in the layer's own (unscaled) units
    shape = RoundedCornerShape(corner.coerceAtLeast(0f) / scale.coerceAtLeast(0.01f))
    clip = true
}

/**
 * The player selector: every style as a card, the live player one of them. Holding the player (or
 * "edit player") shrinks the player straight into its card - the same pixels, nothing swapped or
 * faded - while the other styles slide in beside it, each previewed as itself. Swiping looks
 * through them; a tap on a card, Set, or a swipe down puts the style in view on (the back button
 * leaves it as it was). The editable styles have Edit beside Set; the others' Set fills the width.
 *
 * Laid out like the rest of the app: a small "Styles" header with the style in view under it and
 * the back button beside it, the cards, page dots, and one tight row of buttons. Every card stays
 * composed while the selector is up and only its layer moves, so a swipe never has to build a
 * card on the way.
 */
@Composable
fun PlayerSelectorOverlay(
    studio: PlayerStudioState,
    styles: List<PlayerStyle>,
    onSet: (Int) -> Unit,
    /** the editor for the focused style (its index) */
    onEdit: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shown by remember(studio) { derivedStateOf { studio.selector.value > 0.0005f || studio.stage == StudioStage.SELECTOR } }
    if (!shown) return
    val active = studio.stage == StudioStage.SELECTOR
    if (active) BackHandler { studio.back() }

    val density = LocalDensity.current
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val previewActions = remember { StyleActions(onOtherMenu = {}, onEditPlayer = {}) }
    val touchSlop = LocalViewConfiguration.current.touchSlop
    val currentOnSet by rememberUpdatedState(onSet)
    val focused by remember(studio, styles.size) {
        derivedStateOf { studio.page.value.roundToInt().coerceIn(0, styles.lastIndex) }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val gapPx = with(density) { CardGap.toPx() }
        val last = styles.lastIndex
        fun geometry() = CardGeometry(studio.selector.value, w, h, gapPx)
        // where the cards sit once the selector is up: the dots and the buttons hang under them
        val restingBottomPx = CardGeometry(1f, w, h, gapPx).let { it.centerY + it.cardH / 2f }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!active) Modifier else Modifier.pointerInput(styles.size, w) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val tracker = VelocityTracker()
                            tracker.addPointerInputChange(down)
                            val startPage = studio.page.value
                            var dx = 0f
                            var dy = 0f
                            var mode = 0 // 0 undecided, 1 horizontal, 2 vertical
                            var released = false
                            val stride = CardGeometry(1f, w, h, gapPx).stride
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) {
                                    released = true
                                    break
                                }
                                val delta = change.positionChangeIgnoreConsumed()
                                dx += delta.x
                                dy += delta.y
                                tracker.addPointerInputChange(change)
                                if (mode == 0 && (abs(dx) > touchSlop || abs(dy) > touchSlop)) {
                                    mode = if (abs(dx) >= abs(dy)) 1 else 2
                                }
                                if (mode == 1) {
                                    change.consume()
                                    // past the ends it resists
                                    val raw = startPage - dx / stride
                                    studio.page.value = when {
                                        raw < 0f -> raw * 0.3f
                                        raw > last -> last + (raw - last) * 0.3f
                                        else -> raw
                                    }
                                } else if (mode == 2) {
                                    change.consume()
                                    // pulling down lets the cards sink back toward the player
                                    studio.selector.value = (1f - (dy.coerceAtLeast(0f) / (h * 0.9f))).coerceIn(0f, 1f)
                                }
                            }
                            val velocity = tracker.calculateVelocity()
                            when (mode) {
                                0 -> if (released) {
                                    // a tap on a card puts that style on: the player grows out of it
                                    val g = CardGeometry(1f, w, h, gapPx)
                                    val y = down.position.y
                                    if (y > g.centerY - g.cardH / 2f && y < g.centerY + g.cardH / 2f) {
                                        val tapped = (studio.page.value + (down.position.x - w / 2f) / g.stride).roundToInt()
                                        if (tapped in 0..last) currentOnSet(tapped)
                                    }
                                }
                                1 -> {
                                    val flick = (-velocity.x / stride).coerceIn(-3f, 3f)
                                    val target = (studio.page.value + flick * 0.3f).roundToInt().coerceIn(0, last)
                                    studio.page.animateTo(target.toFloat(), velocity = flick)
                                }
                                // pulled down far (or fast) enough: the style in view goes on, as with Set
                                2 -> if (dy > 120.dp.toPx() || velocity.y > 1_400f) {
                                    currentOnSet(studio.page.value.roundToInt().coerceIn(0, last))
                                } else {
                                    studio.selector.animateTo(1f)
                                }
                            }
                        }
                    }
                ),
        ) {
            // the other styles, as themselves
            styles.forEachIndexed { index, style ->
                key(style) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val g = geometry()
                                val position = studio.page.value
                                // the live card is the player itself, except while a newly picked
                                // style is being built behind it: then its card covers it, and
                                // fades into it as it grows
                                val live = index == studio.livePage
                                val visible = g.visible(index, position) && (!live || studio.handoff.value > 0.001f)
                                alpha = if (!visible) 0f else if (live) studio.handoff.value.coerceIn(0f, 1f) else 1f
                                scaleX = g.scale
                                scaleY = g.scale
                                translationX = g.centerX(index, position) - w / 2f
                                translationY = g.centerY - h / 2f
                                // springs settle a hair past their ends: a corner must never go below zero
                                shape = RoundedCornerShape(CardCorner.toPx() * g.e.coerceIn(0f, 1f) / g.scale.coerceAtLeast(0.01f))
                                clip = true
                            }
                            .background(Color.Black),
                    ) {
                        // every card built, the live one too: picking a style hands over to its
                        // card, which mustn't be built in the frame it has to cover the player
                        if (style == PlayerStyle.DEFAULT) {
                            FlagshipPreview()
                        } else if (style == PlayerStyle.TURNTABLE) {
                            FlagshipPreview(turntable = true)
                        } else {
                            AlternatePlayerStyle(style = style, actions = previewActions, preview = true, modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }

            // a hairline round every card, the live one included
            Canvas(modifier = Modifier.fillMaxSize()) {
                val g = geometry()
                if (g.e < 0.02f) return@Canvas
                val stroke = 1.dp.toPx()
                val radius = CardCorner.toPx() * g.e.coerceIn(0f, 1f)
                val position = studio.page.value
                styles.indices.forEach { index ->
                    if (!g.visible(index, position)) return@forEach
                    val cx = g.centerX(index, position)
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.16f * g.e.coerceIn(0f, 1f)),
                        topLeft = Offset(cx - g.cardW / 2f + stroke / 2f, g.centerY - g.cardH / 2f + stroke / 2f),
                        size = Size(g.cardW - stroke, g.cardH - stroke),
                        cornerRadius = CornerRadius(radius),
                        style = Stroke(stroke),
                    )
                }
            }

            // header: back, and "Styles" with the style in view under it, sliding down from above
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = statusTop + 10.dp, start = 22.dp, end = 22.dp)
                    .height(40.dp)
                    .graphicsLayer {
                        val e = studio.selector.value.coerceIn(0f, 1f)
                        translationY = -(1f - e) * (statusTop.toPx() + 70.dp.toPx())
                        alpha = e
                    },
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable(enabled = active) { studio.back() },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back_ios),
                        contentDescription = androidx.compose.ui.res.stringResource(com.playder.R.string.cd_back),
                        tint = Color.White,
                        modifier = Modifier.size(15.dp).offset(x = 2.dp),
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("Styles", color = Color.White, style = studioText(StudioType.Title, FontWeight.Bold))
                    AnimatedContent(
                        targetState = styles.getOrNull(focused)?.selectorName.orEmpty(),
                        transitionSpec = {
                            (slideInVertically(tween(320)) { it } + fadeIn(tween(240))) togetherWith
                                (slideOutVertically(tween(280)) { -it } + fadeOut(tween(180)))
                        },
                        label = "selectorStyleName",
                    ) { name ->
                        Text(name, color = Color.White.copy(alpha = 0.6f), style = studioText(StudioType.Subtitle, FontWeight.Medium))
                    }
                }
            }

            // under the cards: the page dots and the buttons for the card in view
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .layout { measurable, constraints ->
                        val p = measurable.measure(constraints.copy(minHeight = 0))
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            p.placeWithLayer(0, (restingBottomPx + 18.dp.toPx()).roundToInt()) {
                                val e = studio.selector.value.coerceIn(0f, 1f)
                                translationY = (1f - e) * 160.dp.toPx()
                                alpha = e
                            }
                        }
                    },
            ) {
                Canvas(modifier = Modifier.size(width = (styles.size * 11).dp, height = 5.dp)) {
                    val dot = 5.dp.toPx()
                    val step = 11.dp.toPx()
                    val position = studio.page.value.coerceIn(0f, last.toFloat())
                    val start = (size.width - (styles.size - 1) * step - dot) / 2f
                    styles.indices.forEach { i ->
                        // the dot in view is white, its neighbours fade in as a swipe reaches them
                        val near = (1f - abs(position - i)).coerceIn(0f, 1f)
                        drawCircle(
                            color = Color.White.copy(alpha = 0.32f + 0.68f * near),
                            radius = dot / 2f,
                            center = Offset(start + i * step + dot / 2f, size.height / 2f),
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                val style = styles.getOrNull(focused)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (style?.editable == true) {
                        CardButton("Edit", enabled = active, onClick = { onEdit(focused) }, modifier = Modifier.weight(1f))
                    }
                    CardButton("Set", enabled = active, onClick = { currentOnSet(focused) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CardButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.15f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, color = Color.White, style = studioText(StudioType.Button))
    }
}
