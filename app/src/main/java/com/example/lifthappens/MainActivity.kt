package com.example.lifthappens

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifthappens.ui.theme.LiftHappensTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiftHappensTheme(darkTheme = true) {
                LiftHappensApp()
            }
        }
    }
}

// --- Data Models & Seed Data ---
enum class CardItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    EXERCISES("Exercises", "Browse by muscle", Icons.Default.FitnessCenter),
    WORKOUTS("Workouts", "Your training plans", Icons.AutoMirrored.Filled.Assignment),
    HISTORY("History", "View past sessions", Icons.Default.DateRange),
    MEAL_PLANS("Meal Plans", "Your saved meal plans", Icons.Default.Restaurant)
}

data class Exercise(
    val id: String,
    val name: String,
    val category: String, // Abs, Back, Biceps, Cardio, Chest, Legs, Shoulders, Triceps, Stretches
    val description: String = "",
    val recordingType: String = "Weight & Reps", // Weight & Reps, Reps Only, Time Only, Distance & Time
    val defaultUnit: String = "lbs",
    val isCustom: Boolean = false,
    val isArchived: Boolean = false
)

data class WorkoutPlan(
    val id: String,
    val name: String,
    val exercises: List<String>
)

data class SetEntry(
    val setNumber: Int,
    var weight: String = "",
    var reps: String = "",
    var completed: Boolean = false
)

data class SessionLog(
    val id: String,
    val title: String,
    val date: LocalDate,
    val summary: String
)

val defaultExercises = listOf(
    Exercise("1", "Bench Press", "Chest", "Barbell bench press on flat bench", "Weight & Reps", "lbs", false),
    Exercise("2", "Incline Dumbbell Press", "Chest", "Upper chest dumbbell press", "Weight & Reps", "lbs", false),
    Exercise("3", "Barbell Row", "Back", "Bent-over barbell row", "Weight & Reps", "lbs", false),
    Exercise("4", "Pull-up", "Back", "Bodyweight pull-up", "Reps Only", "lbs", false),
    Exercise("5", "Squat", "Legs", "Barbell back squat", "Weight & Reps", "lbs", false),
    Exercise("6", "Leg Press", "Legs", "Machine leg press", "Weight & Reps", "lbs", false),
    Exercise("7", "Overhead Press", "Shoulders", "Standing barbell shoulder press", "Weight & Reps", "lbs", false),
    Exercise("8", "Barbell Curl", "Biceps", "Standing biceps curl", "Weight & Reps", "lbs", false),
    Exercise("9", "Tricep Extension", "Triceps", "Cable pushdown", "Weight & Reps", "lbs", false),
    Exercise("10", "Plank", "Abs", "Abdominal isometric hold", "Time Only", "sec", false),
    Exercise("11", "Crunches", "Abs", "Floor abdominal crunches", "Reps Only", "reps", false),
    Exercise("12", "Hamstring Stretch", "Stretches", "Static hamstring flexibility", "Time Only", "sec", false)
)

// --- Local JSON Persistence ---
fun saveExercisesToDisk(context: Context, exercises: List<Exercise>) {
    try {
        val jsonArray = JSONArray()
        exercises.forEach { ex ->
            val obj = JSONObject().apply {
                put("id", ex.id)
                put("name", ex.name)
                put("category", ex.category)
                put("description", ex.description)
                put("recordingType", ex.recordingType)
                put("defaultUnit", ex.defaultUnit)
                put("isCustom", ex.isCustom)
                put("isArchived", ex.isArchived)
            }
            jsonArray.put(obj)
        }
        File(context.filesDir, "exercises.json").writeText(jsonArray.toString())
    } catch (_: Exception) {}
}

fun loadExercisesFromDisk(context: Context): List<Exercise> {
    val file = File(context.filesDir, "exercises.json")
    if (!file.exists()) return defaultExercises
    try {
        val jsonArray = JSONArray(file.readText())
        val list = mutableListOf<Exercise>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            list.add(
                Exercise(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    category = obj.getString("category"),
                    description = obj.optString("description", ""),
                    recordingType = obj.optString("recordingType", "Weight & Reps"),
                    defaultUnit = obj.optString("defaultUnit", "lbs"),
                    isCustom = obj.optBoolean("isCustom", false),
                    isArchived = obj.optBoolean("isArchived", false)
                )
            )
        }
        return if (list.isEmpty()) defaultExercises else list
    } catch (_: Exception) {
        return defaultExercises
    }
}

val initialPlans = mutableStateListOf(
    WorkoutPlan("p1", "Upper Body A", listOf("Bench Press", "Barbell Row", "Overhead Press", "Barbell Curl")),
    WorkoutPlan("p2", "Leg Day", listOf("Squat", "Leg Press")),
    WorkoutPlan("p3", "Chest & Triceps", listOf("Incline Dumbbell Press", "Tricep Extension"))
)

val initialHistory = mutableStateListOf(
    SessionLog("s1", "Upper Body A", LocalDate.now().minusDays(2), "Bench Press: 135 lbs x 10, 140 lbs x 8"),
    SessionLog("s2", "Leg Day", LocalDate.now().minusDays(4), "Squat: 185 lbs x 10, 185 lbs x 10")
)

// --- Navigation State ---
sealed interface Screen {
    data object Home : Screen
    data class Feature(val card: CardItem) : Screen
    data object Settings : Screen
    data class ActiveWorkout(val planName: String, val exercises: List<String>) : Screen
    data class SearchResults(val query: String) : Screen
}

@Composable
fun LiftHappensApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val context = LocalContext.current
    val exercisesState = remember { mutableStateListOf<Exercise>() }

    // Load exercises on startup
    LaunchedEffect(Unit) {
        if (exercisesState.isEmpty()) {
            exercisesState.addAll(loadExercisesFromDisk(context))
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0D0E10)
    ) {
        when (val screen = currentScreen) {
            is Screen.Home -> {
                HomeScreen(
                    onCardClick = { card -> currentScreen = Screen.Feature(card) },
                    onSettingsClick = { currentScreen = Screen.Settings },
                    onSearchSubmit = { query ->
                        if (query.isNotBlank()) {
                            currentScreen = Screen.SearchResults(query)
                        }
                    }
                )
            }
            is Screen.Feature -> {
                when (screen.card) {
                    CardItem.EXERCISES -> ExerciseLibraryScreen(
                        exercises = exercisesState,
                        onExercisesChanged = { newList ->
                            exercisesState.clear()
                            exercisesState.addAll(newList)
                            saveExercisesToDisk(context, newList)
                        },
                        onBack = { currentScreen = Screen.Home }
                    )
                    CardItem.WORKOUTS -> WorkoutPlansScreen(
                        onBack = { currentScreen = Screen.Home },
                        onStartWorkout = { planName, exList ->
                            currentScreen = Screen.ActiveWorkout(planName, exList)
                        }
                    )
                    CardItem.HISTORY -> HistoryCalendarScreen(
                        onBack = { currentScreen = Screen.Home }
                    )
                    CardItem.MEAL_PLANS -> PlaceholderScreen(
                        title = "Meal Plans",
                        subtitle = "Your saved meal plans",
                        icon = screen.card.icon,
                        onBackClick = { currentScreen = Screen.Home }
                    )
                }
            }
            is Screen.Settings -> {
                SettingsScreen(
                    onBack = { currentScreen = Screen.Home }
                )
            }
            is Screen.ActiveWorkout -> {
                ActiveWorkoutScreen(
                    planName = screen.planName,
                    exerciseNames = screen.exercises,
                    onFinishWorkout = { summary ->
                        initialHistory.add(0, SessionLog(System.currentTimeMillis().toString(), screen.planName, LocalDate.now(), summary))
                        currentScreen = Screen.Home
                    },
                    onBack = { currentScreen = Screen.Home }
                )
            }
            is Screen.SearchResults -> {
                SearchResultsScreen(
                    query = screen.query,
                    exercises = exercisesState,
                    onBack = { currentScreen = Screen.Home },
                    onStartWorkout = { planName, exList ->
                        currentScreen = Screen.ActiveWorkout(planName, exList)
                    }
                )
            }
        }
    }
}

// --- Home Screen ---
@Composable
fun HomeScreen(
    onCardClick: (CardItem) -> Unit,
    onSettingsClick: () -> Unit,
    onSearchSubmit: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            currentDate = LocalDate.now()
            delay(1.seconds)
        }
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    val limeColor = Color(0xFFB1FF37)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "Dumbbell",
                    tint = limeColor,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Lift Happens",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF16181A), RoundedCornerShape(20.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF9AA0A6),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Date and Time
        Text(
            text = currentDate.format(dateFormatter),
            color = Color(0xFF9AA0A6),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = currentTime.format(timeFormatter),
            color = Color.White,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text("Search exercises, workouts, dates", color = Color(0xFF8E9196), fontSize = 15.sp)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF8E9196))
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF16181A),
                unfocusedContainerColor = Color(0xFF16181A),
                focusedBorderColor = limeColor,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )
        if (searchQuery.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { onSearchSubmit(searchQuery) },
                colors = ButtonDefaults.buttonColors(containerColor = limeColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search '$searchQuery'", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2x2 Cards Grid
        val cards = CardItem.entries
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            for (i in cards.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FeatureCard(
                        card = cards[i],
                        limeColor = limeColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onCardClick(cards[i]) }
                    )
                    if (i + 1 < cards.size) {
                        FeatureCard(
                            card = cards[i + 1],
                            limeColor = limeColor,
                            modifier = Modifier.weight(1f),
                            onClick = { onCardClick(cards[i + 1]) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureCard(
    card: CardItem,
    limeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16181A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = card.icon,
                    contentDescription = card.title,
                    tint = limeColor,
                    modifier = Modifier.size(30.dp)
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = Color(0xFF8E9196),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(text = card.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = card.subtitle, color = Color(0xFF8E9196), fontSize = 13.sp)
            }
        }
    }
}

// --- Exercise Library Screen (EX001–EX004) ---
@Composable
fun ExerciseLibraryScreen(
    exercises: List<Exercise>,
    onExercisesChanged: (List<Exercise>) -> Unit,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editExercise by remember { mutableStateOf<Exercise?>(null) }
    var filterCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Chest", "Back", "Legs", "Shoulders", "Biceps", "Triceps", "Abs", "Stretches")
    val limeColor = Color(0xFFB1FF37)

    val filteredExercises = if (filterCategory == "All") {
        exercises.sortedBy { it.name }
    } else {
        exercises.filter { it.category.equals(filterCategory, true) }.sortedBy { it.name }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp).background(Color(0xFF16181A), RoundedCornerShape(20.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text("Exercise Library", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.size(40.dp).background(limeColor, RoundedCornerShape(20.dp))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise", tint = Color.Black)
            }
        }

        // Category Filter Chips
        androidx.compose.foundation.lazy.LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val selected = filterCategory == cat
                Surface(
                    onClick = { filterCategory = cat },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) limeColor else Color(0xFF16181A)
                ) {
                    Text(
                        text = cat,
                        color = if (selected) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Exercise List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredExercises) { ex ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16181A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { editExercise = ex }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ex.name, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (ex.isCustom) Color(0xFF263322) else Color(0xFF262A2E)
                                ) {
                                    Text(
                                        text = if (ex.isCustom) "Custom" else "Built-in",
                                        color = if (ex.isCustom) limeColor else Color(0xFF9AA0A6),
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${ex.category} • ${ex.recordingType} (${ex.defaultUnit})",
                                color = Color(0xFF8E9196),
                                fontSize = 13.sp
                            )
                            if (ex.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(ex.description, color = Color(0xFF8E9196), fontSize = 12.sp)
                            }
                        }

                        // Edit / Delete actions for custom exercises
                        if (ex.isCustom) {
                            Row {
                                IconButton(onClick = { editExercise = ex }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF9AA0A6), modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = {
                                    val updated = exercises.filter { it.id != ex.id }
                                    onExercisesChanged(updated)
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFE57373), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Exercise Dialog
    if (showAddDialog || editExercise != null) {
        val editing = editExercise
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var category by remember { mutableStateOf(editing?.category ?: "Chest") }
        var desc by remember { mutableStateOf(editing?.description ?: "") }
        var recordingType by remember { mutableStateOf(editing?.recordingType ?: "Weight & Reps") }
        var defaultUnit by remember { mutableStateOf(editing?.defaultUnit ?: "lbs") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editExercise = null
            },
            title = { Text(if (editing == null) "Add Custom Exercise" else "Edit Exercise", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Exercise Name") }, singleLine = true)
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (e.g. Chest, Back, Abs)") }, singleLine = true)
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description / Notes") })
                    OutlinedTextField(value = recordingType, onValueChange = { recordingType = it }, label = { Text("Recording Type (Weight & Reps, Time)") }, singleLine = true)
                    OutlinedTextField(value = defaultUnit, onValueChange = { defaultUnit = it }, label = { Text("Default Unit (lbs, kg, sec)") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val updatedList = exercises.toMutableList()
                            if (editing == null) {
                                updatedList.add(
                                    Exercise(
                                        id = System.currentTimeMillis().toString(),
                                        name = name.trim(),
                                        category = category.trim(),
                                        description = desc.trim(),
                                        recordingType = recordingType.trim(),
                                        defaultUnit = defaultUnit.trim(),
                                        isCustom = true
                                    )
                                )
                            } else {
                                val idx = updatedList.indexOfFirst { it.id == editing.id }
                                if (idx != -1) {
                                    updatedList[idx] = editing.copy(
                                        name = name.trim(),
                                        category = category.trim(),
                                        description = desc.trim(),
                                        recordingType = recordingType.trim(),
                                        defaultUnit = defaultUnit.trim()
                                    )
                                }
                            }
                            onExercisesChanged(updatedList)
                            showAddDialog = false
                            editExercise = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = limeColor)
                ) {
                    Text(if (editing == null) "Add" else "Save", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    editExercise = null
                }) { Text("Cancel", color = Color.White) }
            },
            containerColor = Color(0xFF1A1C1E)
        )
    }
}

// --- Workout Plans Screen ---
@Composable
fun WorkoutPlansScreen(onBack: () -> Unit, onStartWorkout: (String, List<String>) -> Unit) {
    val limeColor = Color(0xFFB1FF37)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp).background(Color(0xFF16181A), RoundedCornerShape(20.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Workouts & Training Plans", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(initialPlans) { plan ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16181A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(plan.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${plan.exercises.size} exercises included", color = Color(0xFF8E9196), fontSize = 13.sp)
                            }
                            Button(
                                onClick = { onStartWorkout(plan.name, plan.exercises) },
                                colors = ButtonDefaults.buttonColors(containerColor = limeColor),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.Black)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Start", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = plan.exercises.joinToString(", "),
                            color = Color(0xFF9AA0A6),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// --- Active Workout Session Screen (with Rest Timer & Set Logging) ---
@Composable
fun ActiveWorkoutScreen(
    planName: String,
    exerciseNames: List<String>,
    onFinishWorkout: (String) -> Unit,
    onBack: () -> Unit
) {
    val limeColor = Color(0xFFB1FF37)
    val sessionSets = remember {
        mutableStateMapOf<String, MutableList<SetEntry>>().apply {
            exerciseNames.forEach { ex ->
                this[ex] = mutableStateListOf(SetEntry(1), SetEntry(2), SetEntry(3))
            }
        }
    }

    var timerSecondsRemaining by remember { mutableStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning, timerSecondsRemaining) {
        if (isTimerRunning && timerSecondsRemaining > 0) {
            delay(1.seconds)
            timerSecondsRemaining -= 1
            if (timerSecondsRemaining == 0) {
                isTimerRunning = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp)
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp).background(Color(0xFF16181A), RoundedCornerShape(20.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(planName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Active Session", color = limeColor, fontSize = 12.sp)
                }
            }
            Button(
                onClick = {
                    val summary = sessionSets.entries.joinToString("; ") { entry ->
                        val ex = entry.key
                        val sets = entry.value
                        val completed = sets.filter { it.completed }
                        if (completed.isNotEmpty()) "$ex: ${completed.joinToString { "${it.weight}lbs x ${it.reps}" }}" else ""
                    }
                    onFinishWorkout(if (summary.isNotBlank()) summary else "Completed workout session")
                },
                colors = ButtonDefaults.buttonColors(containerColor = limeColor)
            ) {
                Text("Finish", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        // Rest Timer Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16181A)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, contentDescription = "Timer", tint = limeColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (timerSecondsRemaining > 0) "Rest Timer: ${timerSecondsRemaining}s" else "Rest Timer Ready (90s)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = {
                        timerSecondsRemaining = 90
                        isTimerRunning = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262A2E))
                ) {
                    Text(if (isTimerRunning) "Reset 90s" else "Start 90s", color = limeColor)
                }
            }
        }

        // Exercises & Sets
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(exerciseNames) { exName ->
                val sets = sessionSets[exName] ?: remember { mutableStateListOf() }
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16181A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(exName, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            TextButton(onClick = {
                                sets.add(SetEntry(sets.size + 1))
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Set", tint = limeColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Set", color = limeColor)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Table header
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("SET", color = Color(0xFF8E9196), fontSize = 12.sp, modifier = Modifier.width(40.dp))
                            Text("LBS", color = Color(0xFF8E9196), fontSize = 12.sp, modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
                            Text("REPS", color = Color(0xFF8E9196), fontSize = 12.sp, modifier = Modifier.weight(1f).padding(horizontal = 4.dp))
                            Text("DONE", color = Color(0xFF8E9196), fontSize = 12.sp, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                        }

                        sets.forEach { setEntry ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${setEntry.setNumber}", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(40.dp))
                                OutlinedTextField(
                                    value = setEntry.weight,
                                    onValueChange = { setEntry.weight = it },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF22252A),
                                        unfocusedContainerColor = Color(0xFF22252A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = limeColor,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f).height(50.dp).padding(horizontal = 2.dp)
                                )
                                OutlinedTextField(
                                    value = setEntry.reps,
                                    onValueChange = { setEntry.reps = it },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF22252A),
                                        unfocusedContainerColor = Color(0xFF22252A),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = limeColor,
                                        unfocusedBorderColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f).height(50.dp).padding(horizontal = 2.dp)
                                )
                                IconButton(
                                    onClick = {
                                        setEntry.completed = !setEntry.completed
                                        if (setEntry.completed) {
                                            timerSecondsRemaining = 90
                                            isTimerRunning = true
                                        }
                                    },
                                    modifier = Modifier
                                        .width(50.dp)
                                        .size(40.dp)
                                        .background(if (setEntry.completed) limeColor else Color(0xFF22252A), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Complete",
                                        tint = if (setEntry.completed) Color.Black else Color(0xFF8E9196)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- History & Calendar Screen ---
@Composable
fun HistoryCalendarScreen(onBack: () -> Unit) {
    val limeColor = Color(0xFFB1FF37)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp).background(Color(0xFF16181A), RoundedCornerShape(20.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Workout History", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        // Calendar Header Simulation
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16181A)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("October 2026", color = limeColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                        Text(day, color = Color(0xFF8E9196), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    (1..7).forEach { d ->
                        val hasWorkout = d == 2 || d == 5 || d == 10
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (hasWorkout) limeColor else Color.Transparent,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$d",
                                    color = if (hasWorkout) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Text("Past Sessions", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(initialHistory) { session ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16181A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(session.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(session.date.toString(), color = limeColor, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(session.summary, color = Color(0xFF8E9196), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// --- Search Results Screen ---
@Composable
fun SearchResultsScreen(
    query: String,
    exercises: List<Exercise>,
    onBack: () -> Unit,
    onStartWorkout: (String, List<String>) -> Unit
) {
    val limeColor = Color(0xFFB1FF37)
    val matchedExercises = exercises.filter { it.name.contains(query, true) || it.category.contains(query, true) }
    val matchedPlans = initialPlans.filter { it.name.contains(query, true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp).background(Color(0xFF16181A), RoundedCornerShape(20.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Search: \"$query\"", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (matchedExercises.isNotEmpty()) {
                item { Text("Exercises", color = limeColor, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                items(matchedExercises) { ex ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16181A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(ex.name, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("Category: ${ex.category}", color = Color(0xFF8E9196), fontSize = 13.sp)
                        }
                    }
                }
            }

            if (matchedPlans.isNotEmpty()) {
                item { Text("Workout Plans", color = limeColor, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                items(matchedPlans) { plan ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16181A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(plan.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${plan.exercises.size} exercises", color = Color(0xFF8E9196), fontSize = 13.sp)
                            }
                            Button(
                                onClick = { onStartWorkout(plan.name, plan.exercises) },
                                colors = ButtonDefaults.buttonColors(containerColor = limeColor)
                            ) {
                                Text("Start", color = Color.Black)
                            }
                        }
                    }
                }
            }

            if (matchedExercises.isEmpty() && matchedPlans.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No matching exercises or workouts found.", color = Color(0xFF8E9196))
                    }
                }
            }
        }
    }
}

// --- Settings Screen ---
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val limeColor = Color(0xFFB1FF37)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp).background(Color(0xFF16181A), RoundedCornerShape(20.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Settings & Backup", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16181A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Units", color = limeColor, fontWeight = FontWeight.Bold)
                Text("Default Load Unit: Pounds (lbs)", color = Color.White)
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16181A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Offline Data Persistence", color = limeColor, fontWeight = FontWeight.Bold)
                Text("Exercises and custom definitions are automatically saved to local JSON storage.", color = Color(0xFF8E9196), fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onBackClick: () -> Unit
) {
    val limeColor = Color(0xFFB1FF37)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp).background(Color(0xFF16181A), RoundedCornerShape(20.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16181A))
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = limeColor, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = title, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = subtitle, color = Color(0xFF9AA0A6), fontSize = 15.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Text(text = "Coming soon in next update.", color = Color(0xFF8E9196), fontSize = 14.sp)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D0E10)
@Composable
fun LiftHappensAppPreview() {
    LiftHappensTheme(darkTheme = true) {
        LiftHappensApp()
    }
}
