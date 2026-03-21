package com.josephlimbert.weighttracker.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.tappableElement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.google.firebase.auth.FirebaseUser
import com.josephlimbert.weighttracker.R
import com.josephlimbert.weighttracker.data.model.User
import com.josephlimbert.weighttracker.ui.shared.LoadingIndicator
import com.josephlimbert.weighttracker.ui.theme.WeightTrackerTheme
import kotlinx.serialization.Serializable

enum class WeightUnit(val label: String, val value: String) {
    POUND(label = "Pound (lb)", value = "lbs"),
    KILO(label = "Kilogram (kg)", value = "kgs")
}

@Serializable
data object Settings : NavKey

@Composable
fun SettingsScreen(
    modifier: Modifier,
    onNavigateToAuth: () -> Unit,
    onNavigateToSetGoal: () -> Unit,
    onSignOUt: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val weightUnit by viewModel.weightUnit.collectAsStateWithLifecycle("lbs")
    val user by viewModel.user.collectAsStateWithLifecycle(null)
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle(null)

    if (user == null || userProfile == null) {
        LoadingIndicator(modifier = modifier)
    } else {
        SettingsScreenContent(
            modifier = modifier,
            weightUnit = weightUnit,
            user = user,
            userProfile = userProfile!!,
            onChangeWeightUnit = { unit -> viewModel.changeWeightUnit(user!!.uid, unit) },
            onNavigateToSetGoal = onNavigateToSetGoal,
            onSignOut = {
                viewModel.signOut()
                if (user!!.isAnonymous) {
                    viewModel.deleteUserProfile(user!!.uid)
                }
                onSignOUt()
            },
            onLinkAccount = onNavigateToAuth
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreenContent(
    modifier: Modifier,
    weightUnit: String,
    user: FirebaseUser?,
    userProfile: User,
    onChangeWeightUnit: (weightUnit: String) -> Unit,
    onNavigateToSetGoal: () -> Unit,
    onSignOut: () -> Unit,
    onLinkAccount: () -> Unit) {
    val scrollState = rememberScrollState()

    Scaffold() { innerPadding ->
        LazyColumn(modifier = modifier
            .fillMaxSize()
            .padding(
                top = innerPadding.calculateTopPadding() + 10.dp,
                start = 10.dp,
                end = 10.dp,
                bottom = 10.dp
            ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top
            ) {

                if (user?.isAnonymous == true) {
                    item() {
                        ListItem(
                            onClick = onLinkAccount,
                            content = {
                                Text(
                                    "Create Account",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            supportingContent = { Text("Register to sync data across devices") },
                            trailingContent = {
                                Icon(
                                    ImageVector.vectorResource(R.drawable.chevron_right_icon),
                                    contentDescription = "create account"
                                )
                            }
                        )
                    }
                } else {
                    item {
                        ListItem(
                            headlineContent = {
                                Text(
                                    "Profile",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            supportingContent = { Text(userProfile.email) },
                            trailingContent = {
                                ProfileDropdownMenu(
                                    modifier = Modifier,
                                    onChangePassword = {},
                                    onSignOut = onSignOut)
                            }
                        )
                        HorizontalDivider()
                    }
                }

            item {
                ListItem(
                    headlineContent = { Text("Goal Weight", style = MaterialTheme.typography.titleMedium) },
                    supportingContent = { Text(userProfile.goalWeight.toString() + " " + weightUnit)},
                    trailingContent = { IconButton(
                        onClick = onNavigateToSetGoal,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            ImageVector.vectorResource(R.drawable.edit_icon),
                            contentDescription = "change goal weight")
                    } }
                )
                HorizontalDivider()
            }

            item {
                Text(
                    "Weight Unit",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(ListItemDefaults.ContentPadding))

                WeightUnit.entries.forEach { unit ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = unit.value == weightUnit,
                                onClick = { onChangeWeightUnit(unit.value) },
                                role = Role.RadioButton
                            ).padding(ListItemDefaults.ContentPadding)
                    ) {
                        RadioButton(selected = unit.value == weightUnit, onClick = null)
                        Text(unit.label, modifier = Modifier.padding(start = 16.dp))
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun ProfileDropdownMenu(modifier: Modifier, onChangePassword: () -> Unit, onSignOut: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        IconButton(
            onClick = { expanded = !expanded},
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Icon(
                ImageVector.vectorResource(R.drawable.settings_icon),
                contentDescription = "profile settings"
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false}
        ) {
            DropdownMenuItem(text = { Text("Change Password") }, onClick = {
                onChangePassword()
                expanded = false
            })
            DropdownMenuItem(text = { Text("Sign Out", color = MaterialTheme.colorScheme.error) }, onClick = {
                onSignOut()
                expanded = false
            })
        }
    }
}

@Composable
@Preview(showSystemUi = false)
fun SettingsScreenPreview() {
    WeightTrackerTheme {
        SettingsScreenContent(modifier = Modifier, weightUnit = "lbs", user = null, userProfile = User(email = "hoodzmtb@gmail.com"), onLinkAccount = {}, onChangeWeightUnit = {}, onNavigateToSetGoal = {}, onSignOut = {})
    }
}