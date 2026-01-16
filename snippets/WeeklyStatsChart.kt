@Composable
fun AnimatedWeeklyChart(
    weekData: List<WeeklyBarData>,
    modifier: Modifier = Modifier
) {
    if (weekData.isEmpty()) return

    // ... Theme Colors ...
    val spotlightColor = MaterialTheme.colorScheme.primary
    val secondaryColor = spotlightColor.copy(alpha = 0.35f)
    val textMeasurer = rememberTextMeasurer()

    // --- ANIMATION STATE ---
    // Create one unique animation property for each bar for independent control
    val barAnimations = remember(weekData.size) {
        List(weekData.size) { Animatable(0f) }
    }

    // Trigger the "Wave" effect
    LaunchedEffect(weekData) {
        barAnimations.forEachIndexed { index, animatable ->
            // Staggered animation launch
            kotlinx.coroutines.launch {
                kotlinx.coroutines.delay(index * 100L)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(top = 32.dp, bottom = 12.dp)
    ) {
        // --- PRE-CALCULATIONS ---
        // Dynamically calculate bar width based on available screen space
        val slotWidth = size.width / weekData.size
        val barWidth = slotWidth * 0.85f
        val chartMaxHeight = size.height - 8.dp.toPx()

        weekData.forEachIndexed { index, day ->
            val xOffset = (index * slotWidth) + (slotWidth - barWidth) / 2
            
            // Calculate height based on animation value (0.0 -> 1.0)
            val targetHeight = day.ratio * chartMaxHeight
            val currentHeight = targetHeight * barAnimations[index].value
            val barTopY = chartMaxHeight - currentHeight

            if (currentHeight > 0) {
                drawRoundRect(
                    color = if (day.isSpotlight) spotlightColor else secondaryColor,
                    topLeft = Offset(x = xOffset, y = barTopY),
                    size = Size(width = barWidth, height = currentHeight),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }

            // Draw Day Label (M, T, W...)
            // ... Text drawing logic ...
        }
    }
}