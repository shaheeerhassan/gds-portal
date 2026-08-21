package com.school.gdsportal.ui.parent.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.school.gdsportal.ui.theme.AccentParent
import com.school.gdsportal.ui.theme.BackgroundColor
import com.school.gdsportal.ui.theme.TextPrimary

@Composable
fun ParentDrawerContent(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = BackgroundColor,
        drawerContentColor = TextPrimary,
        modifier = Modifier.width(320.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "GDS Portal",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = "Parent",
                style = MaterialTheme.typography.bodyLarge,
                color = AccentParent
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable section for main items
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                ParentDrawerItem(
                    label = "Dashboard",
                    icon = Icons.Outlined.Home,
                    isSelected = currentRoute == "parent_dashboard",
                    onClick = { onNavigate("parent_dashboard") }
                )
                ParentDrawerItem(
                    label = "My Children",
                    icon = Icons.Outlined.FamilyRestroom,
                    isSelected = currentRoute.startsWith("my_children") || currentRoute.startsWith("children"),
                    onClick = { onNavigate("my_children") }
                )
                ParentDrawerItem(
                    label = "Announcements",
                    icon = Icons.Outlined.Campaign,
                    isSelected = currentRoute.startsWith("announcements"),
                    onClick = { onNavigate("announcements") }
                )
                ParentDrawerItem(
                    label = "Notifications",
                    icon = Icons.Outlined.Notifications,
                    isSelected = currentRoute.startsWith("notifications"),
                    onClick = { onNavigate("notifications") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            ParentDrawerItem(
                label = "Profile & Account",
                icon = Icons.Outlined.Person,
                isSelected = currentRoute.startsWith("parent_profile") ||
                        currentRoute.startsWith("parent_my_profile") ||
                        currentRoute.startsWith("parent_change_password"),
                onClick = { onNavigate("parent_profile_landing") }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ParentDrawerItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    val backgroundColor = if (isSelected) AccentParent.copy(alpha = 0.08f) else Color.Transparent
    val contentColor = when {
        isSelected -> AccentParent
        isDestructive -> MaterialTheme.colorScheme.error
        else -> TextPrimary
    }
    val fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp)
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(AccentParent)
                    .align(Alignment.CenterStart)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = if (isSelected) 12.dp else 0.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = fontWeight)
            )
            Spacer(modifier = Modifier.weight(1f))
            if (label != "Dashboard" && label != "Logout") {
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}