package com.example.dialerapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

data class ContactItem(val name: String, val number: String)
data class CallLogItem(val name: String, val number: String, val type: String, val date: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF6750A4),
                    secondary = Color(0xFF625B71),
                    background = Color(0xFF141218),
                    surface = Color(0xFF1D1B20)
                )
            ) {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val permissions = arrayOf(
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {}

    LaunchedEffect(Unit) {
        val needsPermission = permissions.any {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needsPermission) {
            permissionLauncher.launch(permissions)
        }
    }

    Scaffold(
        topBar = {
            if (selectedTab != 0 && selectedTab != 3) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {},
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text("Search contacts or history...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {}
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Dialpad, contentDescription = "Keypad") },
                    label = { Text("Keypad") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Contacts, contentDescription = "Contacts") },
                    label = { Text("Contacts") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> KeypadScreen { number -> makeCall(context, number) }
                1 -> CallHistoryScreen(searchQuery) { number -> makeCall(context, number) }
                2 -> ContactsScreen(searchQuery) { number -> makeCall(context, number) }
                3 -> SettingsScreen()
            }
        }
    }
}

@Composable
fun KeypadScreen(onCall: (String) -> Unit) {
    var dialedNumber by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp)
        ) {
            Text(
                text = dialedNumber,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
        }

        val buttons = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            buttons.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    row.forEach { digit ->
                        DialButton(text = digit) {
                            dialedNumber += digit
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(56.dp))

                FloatingActionButton(
                    onClick = { if (dialedNumber.isNotEmpty()) onCall(dialedNumber) },
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(32.dp))
                }

                IconButton(
                    onClick = {
                        if (dialedNumber.isNotEmpty()) {
                            dialedNumber = dialedNumber.dropLast(1)
                        }
                    },
                    modifier = Modifier.size(56.dp)
                ) {
                    if (dialedNumber.isNotEmpty()) {
                        Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun DialButton(text: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color(0xFF2B2930))
            .clickable { onClick() }
    ) {
        Text(text = text, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
fun ContactsScreen(filter: String, onCall: (String) -> Unit) {
    val context = LocalContext.current
    val contacts = remember { mutableStateListOf<ContactItem>() }

    LaunchedEffect(Unit) {
        contacts.clear()
        val resolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )
        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(nameIdx) ?: "Unknown"
                val num = it.getString(numIdx) ?: ""
                contacts.add(ContactItem(name, num))
            }
        }
    }

    val filteredList = contacts.filter {
        it.name.contains(filter, ignoreCase = true) || it.number.contains(filter)
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        items(filteredList) { contact ->
            ListItem(
                headlineContent = { Text(contact.name, fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text(contact.number) },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = contact.name.firstOrNull()?.uppercase() ?: "?",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCall(contact.number) }
            )
        }
    }
}

@Composable
fun CallHistoryScreen(filter: String, onCall: (String) -> Unit) {
    val context = LocalContext.current
    val history = remember { mutableStateListOf<CallLogItem>() }

    LaunchedEffect(Unit) {
        history.clear()
        val resolver = context.contentResolver
        val cursor = resolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER,
                CallLog.Calls.TYPE,
                CallLog.Calls.DATE
            ),
            null, null,
            "${CallLog.Calls.DATE} DESC LIMIT 100"
        )
        cursor?.use {
            val nameIdx = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val numIdx = it.getColumnIndex(CallLog.Calls.NUMBER)
            val typeIdx = it.getColumnIndex(CallLog.Calls.TYPE)
            while (it.moveToNext()) {
                val name = it.getString(nameIdx) ?: it.getString(numIdx) ?: "Unknown"
                val num = it.getString(numIdx) ?: ""
                val type = when (it.getInt(typeIdx)) {
                    CallLog.Calls.INCOMING_TYPE -> "Incoming"
                    CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                    CallLog.Calls.MISSED_TYPE -> "Missed"
                    else -> "Call"
                }
                history.add(CallLogItem(name, num, type, ""))
            }
        }
    }

    val filteredList = history.filter {
        it.name.contains(filter, ignoreCase = true) || it.number.contains(filter)
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        items(filteredList) { item ->
            ListItem(
                headlineContent = { Text(item.name, fontWeight = FontWeight.Medium) },
                supportingContent = { Text("${item.type} • ${item.number}") },
                leadingContent = {
                    Icon(
                        imageVector = if (item.type == "Missed") Icons.Default.CallMissed else Icons.Default.Call,
                        contentDescription = null,
                        tint = if (item.type == "Missed") Color.Red else Color.LightGray
                    )
                },
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onCall(item.number) }
            )
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("dialer_settings", Context.MODE_PRIVATE) }
    var disableProximity by remember {
        mutableStateOf(prefs.getBoolean("disable_proximity", false))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Call Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Disable proximity sensor during calls",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Keeps your screen on when holding the phone near your ear during calls.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = disableProximity,
                    onCheckedChange = { isChecked ->
                        disableProximity = isChecked
                        prefs.edit().putBoolean("disable_proximity", isChecked).apply()
                    }
                )
            }
        }
    }
}

fun makeCall(context: Context, number: String) {
    if (number.isBlank()) return
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(number)}")).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
        context.startActivity(intent)
    }
}
