package com.abdallah.taskvault.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.abdallah.taskvault.R
import com.abdallah.taskvault.ui.AppViewModel
import com.abdallah.taskvault.ui.DeleteAccountState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    appViewModel: AppViewModel = hiltViewModel()
) {
    val currentUser by appViewModel.currentUser.collectAsState()
    val deleteState by appViewModel.deleteAccountState.collectAsStateWithLifecycle()
    val deleteError by appViewModel.deleteAccountError.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var copiedId by remember { mutableStateOf(false) }

    LaunchedEffect(copiedId) {
        if (copiedId) {
            snackbarHostState.showSnackbar(context.getString(R.string.profile_id_copied))
            copiedId = false
        }
    }

    LaunchedEffect(deleteState) {
        if (deleteState == DeleteAccountState.DONE) {
            appViewModel.resetDeleteAccountState()
            onNavigateBack()
        }
    }

    val googleSignInClient = remember {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, options)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_account_title)) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.delete_account_message))
                    if (deleteState == DeleteAccountState.ERROR && deleteError != null) {
                        Text(
                            text  = deleteError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick  = {
                        showDeleteDialog = false
                        appViewModel.deleteAccount()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text(stringResource(R.string.delete_account_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.profile_signout_title)) },
            text  = { Text(stringResource(R.string.profile_signout_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    googleSignInClient.signOut().addOnCompleteListener {
                        appViewModel.signOut()
                        onNavigateBack()
                    }
                }) { Text(stringResource(R.string.profile_signout_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            val initials = currentUser?.displayName
                ?.split(" ")
                ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
                ?.take(2)
                ?.joinToString("") ?: "?"

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text  = initials,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text  = currentUser?.displayName ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = currentUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // User ID card
            currentUser?.uid?.let { uid ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp)
                ) {
                    ListItem(
                        leadingContent = {
                            Icon(Icons.Default.Fingerprint, null, tint = MaterialTheme.colorScheme.primary)
                        },
                        headlineContent = { Text(stringResource(R.string.profile_user_id)) },
                        supportingContent = {
                            Text(
                                text  = uid,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(uid))
                                copiedId = true
                            }) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.profile_copy_id),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                    Text(
                        text     = stringResource(R.string.profile_user_id_hint),
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                    )
                }
            }

            HorizontalDivider()

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp)
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.profile_sync_status)) },
                    supportingContent = { Text(stringResource(R.string.profile_sync_subtitle)) },
                    trailingContent = {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text     = stringResource(R.string.profile_sync_active),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style    = MaterialTheme.typography.labelSmall,
                                color    = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                )
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick  = { showSignOutDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(R.string.profile_signout_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            val isDeleting = deleteState == DeleteAccountState.DELETING
            Button(
                onClick  = { showDeleteDialog = true },
                enabled  = !isDeleting,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = MaterialTheme.colorScheme.onError
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.delete_account_deleting), style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(R.string.delete_account_button), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
