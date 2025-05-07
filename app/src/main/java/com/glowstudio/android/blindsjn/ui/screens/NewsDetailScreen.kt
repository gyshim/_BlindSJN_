/*
* 뉴스 세부 기사 확인 스크린
*
*
* */


package com.glowstudio.android.blindsjn.ui.screens

import android.text.Html
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.core.text.HtmlCompat
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.Alignment


/**
 * Displays a detailed news article screen with title, image, body text, and an optional link to the full article.
 *
 * Shows the article's image (if available), renders the title and main content (prioritizing content, then description, then link), and provides a button to open the full article in a browser if a link is provided.
 *
 * @param title The article's title, which may contain HTML formatting.
 * @param content The main content of the article, used as the primary body text if present.
 * @param description A fallback summary of the article, used if content is absent.
 * @param imageUrl URL of the article's image to display at the top, or null if unavailable.
 * @param link URL to the full article, used as a fallback for body text and to enable external navigation.
 */
@Composable
fun NewsDetailScreen(
    title: String,
    content: String?,
    description: String?,
    imageUrl: String?,
    link: String?
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "기사 이미지",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        val bodyText = when {
            !content.isNullOrBlank() -> content
            !description.isNullOrBlank() -> description
            !link.isNullOrBlank() -> link
            else -> "내용이 없습니다."
        }

        Text(
            text = HtmlCompat.fromHtml(bodyText, HtmlCompat.FROM_HTML_MODE_LEGACY).toString(),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 버튼 추가
        if (!link.isNullOrBlank()) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                    context.startActivity(intent)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("전체 기사 보기")
            }
        }
    }
}
