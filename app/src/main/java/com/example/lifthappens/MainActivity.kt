package com.example.lifthappens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifthappens.ui.theme.LiftHappensTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds
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

@Composable
fun LiftHappensApp() {
    var selectedScreen by remember { mutableStateOf<CardItem?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0D0E10)
    ) {
        if (selectedScreen == null) {
            HomeScreen(
                onCardClick = { card -> selectedScreen = card }
            )
        } else {
            PlaceholderScreen(
                card = selectedScreen!!,
                onBackClick = { selectedScreen = null }
            )
        }
    }
}

@Composable
fun HomeScreen(
    onCardClick: (CardItem) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var currentTime by remember { mutableStateOf(LocalTime.now()) }
    var currentDate by remember { mutableStateOf(LocalDate.now()) }

    // Live clock ticker
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalTime.now()
            currentDate = LocalDate.now()
            delay(1.seconds)
        }
    }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d") }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }

    val formattedDate = currentDate.format(dateFormatter)
    val formattedTime = currentTime.format(timeFormatter)

    val limeColor = Color(0xFFB1FF37)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Bar: Dumbbell icon & Lift Happens title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = "Dumbbell Icon",
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

        // Date and Time Section
        Text(
            text = formattedDate,
            color = Color(0xFF9AA0A6),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formattedTime,
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
                Text(
                    text = "Search exercises, workouts, dates",
                    color = Color(0xFF8E9196),
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF8E9196)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF16181A),
                unfocusedContainerColor = Color(0xFF16181A),
                disabledContainerColor = Color(0xFF16181A),
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Cards Grid (2x2)
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
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16181A)
        )
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
                Text(
                    text = card.title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = card.subtitle,
                    color = Color(0xFF8E9196),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    card: CardItem,
    onBackClick: () -> Unit
) {
    val limeColor = Color(0xFFB1FF37)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp)
    ) {
        // Top Bar with Back Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF16181A), RoundedCornerShape(20.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = card.title,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Content Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16181A)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = card.icon,
                    contentDescription = card.title,
                    tint = limeColor,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = card.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = card.subtitle,
                    color = Color(0xFF8E9196),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Placeholder screen. Detailed view and features coming soon.",
                    color = Color(0xFF8E9196),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
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
