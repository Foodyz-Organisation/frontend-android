package com.example.damprojectfinal.user.feature_chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.damprojectfinal.core.model.ChatListItem

@Composable
fun ChatItemNew(chat: ChatListItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .shadow(8.dp, RoundedCornerShape(16.dp), clip = false)
            .background(Color.White)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== AVATAR WITH ONLINE INDICATOR =====
            Box(
                modifier = Modifier.size(56.dp)
            ) {
                val showImage = !chat.avatarUrl.isNullOrBlank()
                val loadFailed = remember { mutableStateOf(false) }

                if (showImage && !loadFailed.value) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(chat.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar for ${chat.title}",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        onError = { loadFailed.value = true }
                    )
                }

                if (!showImage || loadFailed.value) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF3E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.initials.takeIf { it.isNotBlank() } ?: "??",
                            color = Color(0xFFB87300),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Online indicator (green dot)
                if (chat.isOnline) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .offset(x = 2.dp, y = 2.dp)
                    )
                }
            }

            // ===== CHAT DETAILS (MIDDLE COLUMN) =====
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Chat name (bold)
                Text(
                    text = chat.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2A37),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Last message preview (light gray)
                Text(
                    text = chat.subtitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ===== RIGHT COLUMN: TIMESTAMP & UNREAD BADGE =====
            Column(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.End
            ) {
                // Timestamp (light gray)
                Text(
                    text = chat.updatedTime,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFB0B0B0)
                )

                // Unread counter badge (if applicable)
                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFF59E0B), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = chat.unreadCount.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
