package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class JourneyStats(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalPhotos: Int = 0,
    val totalJourneyDays: Int = 0,
    val storageUsageBytes: Long = 0L
)

class TimeMorphRepository(
    private val context: Context,
    private val database: TimeMorphDatabase
) {
    private val entryDao = database.dailyEntryDao()
    private val capsuleDao = database.timeCapsuleDao()
    private val milestoneDao = database.milestoneDao()

    val allEntries: Flow<List<DailyEntry>> = entryDao.getAllOfficialEntries()
    val latestEntry: Flow<DailyEntry?> = entryDao.getLatestEntry()
    val allCapsules: Flow<List<TimeCapsule>> = capsuleDao.getAllCapsules()
    val allMilestones: Flow<List<Milestone>> = milestoneDao.getAllMilestones()

    val journeyStats: Flow<JourneyStats> = allEntries.map { entries ->
        calculateStats(entries)
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDefaultsIfEmpty()
        }
    }

    private suspend fun seedDefaultsIfEmpty() {
        val existingCapsules = capsuleDao.getAllCapsules().first()
        if (existingCapsules.isEmpty()) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            cal.timeInMillis = now
            cal.add(Calendar.MONTH, 3)
            val unlock3Months = cal.timeInMillis

            cal.timeInMillis = now
            cal.add(Calendar.MONTH, 6)
            val unlock6Months = cal.timeInMillis

            cal.timeInMillis = now
            cal.add(Calendar.YEAR, 1)
            val unlock1Year = cal.timeInMillis

            val defaultCapsules = listOf(
                TimeCapsule(
                    id = "capsule_3m",
                    title = "90-Day Morph",
                    durationMonths = 3,
                    startDate = now - (18L * 24 * 60 * 60 * 1000), // Started 18 days ago
                    unlockDate = unlock3Months,
                    isStrictLock = true,
                    isUnlocked = false,
                    customNote = "Quarterly facial transformation and fitness progress"
                ),
                TimeCapsule(
                    id = "capsule_6m",
                    title = "Half-Year Odyssey",
                    durationMonths = 6,
                    startDate = now - (18L * 24 * 60 * 60 * 1000),
                    unlockDate = unlock6Months,
                    isStrictLock = true,
                    isUnlocked = false,
                    customNote = "Noticeable hair, skin, and structural changes over 180 days"
                ),
                TimeCapsule(
                    id = "capsule_1y",
                    title = "365-Day Transformation",
                    durationMonths = 12,
                    startDate = now - (18L * 24 * 60 * 60 * 1000),
                    unlockDate = unlock1Year,
                    isStrictLock = true,
                    isUnlocked = false,
                    customNote = "The ultimate 1-year transformation time-lapse movie"
                )
            )
            capsuleDao.insertCapsules(defaultCapsules)
        }

        val existingMilestones = milestoneDao.getAllMilestones().first()
        if (existingMilestones.isEmpty()) {
            val defaultMilestones = listOf(
                Milestone(
                    id = "ms_first_photo",
                    title = "First Frame",
                    description = "Captured your initial baseline transformation photo",
                    targetCount = 1,
                    currentCount = 1,
                    isUnlocked = true,
                    unlockedDate = System.currentTimeMillis() - (18L * 86400000),
                    category = "FRAMES"
                ),
                Milestone(
                    id = "ms_streak_7",
                    title = "7-Day Rhythm",
                    description = "Maintained a daily capture streak for a full week",
                    targetCount = 7,
                    currentCount = 7,
                    isUnlocked = true,
                    unlockedDate = System.currentTimeMillis() - (11L * 86400000),
                    category = "STREAK"
                ),
                Milestone(
                    id = "ms_streak_30",
                    title = "30-Day Momentum",
                    description = "Captured a selfie for 30 consecutive days",
                    targetCount = 30,
                    currentCount = 18,
                    isUnlocked = false,
                    category = "STREAK"
                ),
                Milestone(
                    id = "ms_streak_100",
                    title = "Centurion",
                    description = "Achieve an uninterrupted 100-day daily journey",
                    targetCount = 100,
                    currentCount = 18,
                    isUnlocked = false,
                    category = "STREAK"
                ),
                Milestone(
                    id = "ms_streak_365",
                    title = "Year of Morph",
                    description = "Complete 365 frames documenting your life transformation",
                    targetCount = 365,
                    currentCount = 18,
                    isUnlocked = false,
                    category = "STREAK"
                ),
                Milestone(
                    id = "ms_note_first",
                    title = "Reflective Mind",
                    description = "Logged your first daily micro-reflection note",
                    targetCount = 1,
                    currentCount = 1,
                    isUnlocked = true,
                    unlockedDate = System.currentTimeMillis() - (18L * 86400000),
                    category = "NOTE"
                ),
                Milestone(
                    id = "ms_capsule_created",
                    title = "Capsule Pioneer",
                    description = "Created your first locked time capsule",
                    targetCount = 1,
                    currentCount = 1,
                    isUnlocked = true,
                    unlockedDate = System.currentTimeMillis() - (18L * 86400000),
                    category = "CAPSULE"
                ),
                Milestone(
                    id = "ms_capsule_unlocked",
                    title = "Future Unlocked",
                    description = "Witness your completed transformation video after unlock date",
                    targetCount = 1,
                    currentCount = 0,
                    isUnlocked = false,
                    category = "CAPSULE"
                )
            )
            milestoneDao.insertMilestones(defaultMilestones)
        }

        // Seed initial timeline entries for immediate demo/visualization if none exist
        val existingEntries = entryDao.getEntryCount().first()
        if (existingEntries == 0) {
            seedSampleEntries()
        }
    }

    private suspend fun seedSampleEntries() {
        val photosDir = File(context.filesDir, "timemorph_photos")
        if (!photosDir.exists()) photosDir.mkdirs()

        val sampleDays = 14
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -sampleDays)

        val notes = listOf(
            "Day 1: Baseline established. Consistent lighting & posture.",
            "Day 2: Morning routine locked in. Feeling motivated.",
            "Day 3: Clean shave, perfect ghost alignment today.",
            "Day 4: Great workout session, energy peaking.",
            "Day 5: Long work day, but never skipping a frame.",
            "Day 6: Weekend morning frame. Noticeable focus.",
            "Day 7: Full week milestone completed! Habit formed.",
            "Day 8: New hair cut, matching angle with overlay.",
            "Day 9: Fresh start to the week.",
            "Day 10: Double digit streak reached!",
            "Day 11: Perfect face symmetry score 98%.",
            "Day 12: Slight tan from afternoon run.",
            "Day 13: Evening reflection. Journey feels effortless now.",
            "Day 14: Ready for tomorrow's milestone."
        )

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (i in 0 until sampleDays) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val dateStr = dateFormat.format(cal.time)
            val file = File(photosDir, "frame_$dateStr.png")

            // Generate clean procedural portrait image
            if (!file.exists()) {
                val bitmap = createSyntheticFaceBitmap(dayIndex = i + 1, totalDays = sampleDays)
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                }
            }

            val entry = DailyEntry(
                id = UUID.randomUUID().toString(),
                dateString = dateStr,
                timestamp = cal.timeInMillis,
                imagePath = file.absolutePath,
                thumbnailPath = file.absolutePath,
                alignmentScore = 92 + (i % 8),
                note = notes.getOrElse(i) { "Day ${i + 1} captured." },
                isOfficial = true,
                isImported = false,
                syncState = "SYNCED",
                createdAt = cal.timeInMillis,
                updatedAt = cal.timeInMillis
            )
            entryDao.insertEntry(entry)
        }
    }

    // Creates high-fidelity stylized transformation face bitmap for local previews
    private fun createSyntheticFaceBitmap(dayIndex: Int, totalDays: Int): Bitmap {
        val width = 480
        val height = 640
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient dark background
        val bgPaint = Paint().apply {
            color = AndroidColor.rgb(15 + dayIndex, 20 + dayIndex, 32 + dayIndex)
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Grid lines
        val gridPaint = Paint().apply {
            color = AndroidColor.argb(30, 56, 189, 248)
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(width / 3f, 0f, width / 3f, height.toFloat(), gridPaint)
        canvas.drawLine(2 * width / 3f, 0f, 2 * width / 3f, height.toFloat(), gridPaint)
        canvas.drawLine(0f, height / 3f, width.toFloat(), height / 3f, gridPaint)
        canvas.drawLine(0f, 2 * height / 3f, width.toFloat(), 2 * height / 3f, gridPaint)

        // Face oval
        val facePaint = Paint().apply {
            color = AndroidColor.rgb(220 - dayIndex, 190, 160 + (dayIndex * 2))
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val faceRect = RectF(width * 0.28f, height * 0.22f, width * 0.72f, height * 0.62f)
        canvas.drawOval(faceRect, facePaint)

        // Hair arc
        val hairPaint = Paint().apply {
            color = AndroidColor.rgb(35, 30, 30)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val hairRect = RectF(width * 0.26f, height * 0.16f, width * 0.74f, height * 0.36f)
        canvas.drawArc(hairRect, 180f, 180f, true, hairPaint)

        // Eyes
        val eyePaint = Paint().apply {
            color = AndroidColor.rgb(30, 41, 59)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(width * 0.40f, height * 0.38f, 12f, eyePaint)
        canvas.drawCircle(width * 0.60f, height * 0.38f, 12f, eyePaint)

        // Eyeballs / highlights
        val highlightPaint = Paint().apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(width * 0.41f, height * 0.37f, 4f, highlightPaint)
        canvas.drawCircle(width * 0.61f, height * 0.37f, 4f, highlightPaint)

        // Smile
        val mouthPaint = Paint().apply {
            color = AndroidColor.rgb(180, 80, 80)
            strokeWidth = 5f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
        val mouthRect = RectF(width * 0.42f, height * 0.49f, width * 0.58f, height * 0.53f)
        canvas.drawArc(mouthRect, 10f, 160f, false, mouthPaint)

        // Shoulders
        val bodyPaint = Paint().apply {
            color = AndroidColor.rgb(30 + (dayIndex * 4) % 60, 40, 70)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val bodyRect = RectF(width * 0.15f, height * 0.60f, width * 0.85f, height * 1.0f)
        canvas.drawOval(bodyRect, bodyPaint)

        // Watermark badge in bottom corner
        val textPaint = Paint().apply {
            color = AndroidColor.rgb(0, 230, 118)
            textSize = 24f
            isAntiAlias = true
            isFakeBoldText = true
        }
        canvas.drawText("TIMEMORPH #$dayIndex", 32f, height - 36f, textPaint)

        return bitmap
    }

    suspend fun saveDailyEntry(
        bitmap: Bitmap,
        note: String,
        alignmentScore: Int = 95
    ): DailyEntry = withContext(Dispatchers.IO) {
        val photosDir = File(context.filesDir, "timemorph_photos")
        if (!photosDir.exists()) photosDir.mkdirs()

        val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val file = File(photosDir, "frame_$todayDateStr.jpg")

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }

        val existing = entryDao.getEntryByDate(todayDateStr)
        val entry = DailyEntry(
            id = existing?.id ?: UUID.randomUUID().toString(),
            dateString = todayDateStr,
            timestamp = System.currentTimeMillis(),
            imagePath = file.absolutePath,
            thumbnailPath = file.absolutePath,
            alignmentScore = alignmentScore,
            note = note.take(200),
            isOfficial = true,
            isImported = false,
            syncState = "LOCAL",
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        entryDao.insertEntry(entry)

        // Update milestones
        val totalEntries = entryDao.getEntryCount().first()
        milestoneDao.setMilestoneStatus("ms_first_photo", true, System.currentTimeMillis(), totalEntries)
        if (note.isNotBlank()) {
            milestoneDao.setMilestoneStatus("ms_note_first", true, System.currentTimeMillis(), 1)
        }

        entry
    }

    suspend fun deleteEntry(id: String) = withContext(Dispatchers.IO) {
        entryDao.deleteEntry(id)
    }

    suspend fun updateNote(id: String, note: String) = withContext(Dispatchers.IO) {
        val entry = allEntries.first().find { it.id == id }
        if (entry != null) {
            entryDao.updateEntry(entry.copy(note = note.take(200), updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun createCapsule(title: String, durationMonths: Int, isStrict: Boolean, note: String = "") = withContext(Dispatchers.IO) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, durationMonths)
        val capsule = TimeCapsule(
            id = UUID.randomUUID().toString(),
            title = title,
            durationMonths = durationMonths,
            startDate = System.currentTimeMillis(),
            unlockDate = cal.timeInMillis,
            isStrictLock = isStrict,
            isUnlocked = false,
            customNote = note
        )
        capsuleDao.insertCapsule(capsule)
        milestoneDao.setMilestoneStatus("ms_capsule_created", true, System.currentTimeMillis(), 1)
    }

    suspend fun unlockCapsule(id: String) = withContext(Dispatchers.IO) {
        val capsule = capsuleDao.getCapsuleById(id)
        if (capsule != null) {
            capsuleDao.updateCapsule(capsule.copy(isUnlocked = true))
            milestoneDao.setMilestoneStatus("ms_capsule_unlocked", true, System.currentTimeMillis(), 1)
        }
    }

    private fun calculateStats(entries: List<DailyEntry>): JourneyStats {
        if (entries.isEmpty()) return JourneyStats()

        val sortedDates = entries.map { it.dateString }.distinct().sorted()
        val totalPhotos = entries.size

        // Streak calculation
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var currentStreak = 0
        var maxStreak = 0
        var tempStreak = 0
        var prevDate: LocalDate? = null

        val localDates = sortedDates.mapNotNull {
            try { LocalDate.parse(it, formatter) } catch (_: Exception) { null }
        }

        for (date in localDates) {
            if (prevDate == null) {
                tempStreak = 1
            } else {
                val diffDays = java.time.temporal.ChronoUnit.DAYS.between(prevDate, date)
                if (diffDays == 1L) {
                    tempStreak++
                } else if (diffDays > 1L) {
                    tempStreak = 1
                }
            }
            if (tempStreak > maxStreak) {
                maxStreak = tempStreak
            }
            prevDate = date
        }

        // Current streak check
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val lastDate = localDates.lastOrNull()
        currentStreak = if (lastDate != null && (lastDate == today || lastDate == yesterday)) {
            tempStreak
        } else {
            0
        }

        val firstDate = localDates.firstOrNull()
        val totalDays = if (firstDate != null) {
            (java.time.temporal.ChronoUnit.DAYS.between(firstDate, today) + 1).toInt().coerceAtLeast(1)
        } else 1

        // Calculate storage usage
        var totalBytes = 0L
        val dir = File(context.filesDir, "timemorph_photos")
        if (dir.exists()) {
            dir.listFiles()?.forEach { totalBytes += it.length() }
        }

        return JourneyStats(
            currentStreak = currentStreak.coerceAtLeast(1),
            longestStreak = maxStreak.coerceAtLeast(currentStreak),
            totalPhotos = totalPhotos,
            totalJourneyDays = totalDays,
            storageUsageBytes = totalBytes
        )
    }
}
