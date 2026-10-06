package ru.krugarrr.contacts

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import ru.krugarrr.contacts.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ContactsScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

fun hasContactsPermission(context: Context): Boolean {
    val result = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
    return result == PackageManager.PERMISSION_GRANTED
}

fun openDialer(context: Context, phone: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone))
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, R.string.no_dialer, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun ContactsScreen(modifier: Modifier) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasContactsPermission(context)) }
    var asked by rememberSaveable { mutableStateOf(false) }
    var contacts by rememberSaveable { mutableStateOf<ArrayList<Contact>?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission && !asked) {
            asked = true
            launcher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission && contacts == null) {
            contacts = loadContacts(context)
        }
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        if (!hasPermission) {
            Text(text = stringResource(R.string.no_permission), fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { launcher.launch(Manifest.permission.READ_CONTACTS) }) {
                Text(text = stringResource(R.string.grant))
            }
        } else if (contacts == null) {
            Text(text = stringResource(R.string.loading), fontSize = 18.sp)
        } else if (contacts!!.isEmpty()) {
            Text(text = stringResource(R.string.no_contacts), fontSize = 18.sp)
        } else {
            val list = contacts!!
            Text(text = stringResource(R.string.found, list.size), fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(list) { contact ->
                    ContactRow(contact) { openDialer(context, contact.phone) }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun ContactRow(contact: Contact, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    ) {
        Text(text = contact.name, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = contact.phone, fontSize = 16.sp, color = Color.Gray)
    }
}
