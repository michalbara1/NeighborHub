package com.example.neighborhub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.navigation.NavController
import com.example.neighborhub.model.Post


@Composable
fun PostsListScreen(navController: NavController) {
    val allPosts = listOf(
        Post("Help with grocery shopping", "Looking for someone to help me buy groceries.", "John Doe", "https://example.com/john_doe.jpg"),
        Post("Need a ride to the hospital", "I need a ride to the hospital for a check-up.", "Jane Smith", "https://example.com/jane_smith.jpg"),
        Post("Volunteer for dog walking", "Looking for someone to walk my dog while I'm at work.", "Alice Brown", "https://example.com/alice_brown.jpg"),
        Post("Need help with homework", "Struggling with my math homework, need assistance.", "Bob White", "https://example.com/bob_white.jpg"),
        Post("Looking for a jogging buddy", "Anyone interested in jogging in the park?", "Charlie Green", "https://example.com/charlie_green.jpg")
    )

    var searchQuery by remember { mutableStateOf("") }
    val filteredPosts = remember(searchQuery) {
        allPosts.filter { it.headline.contains(searchQuery, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search posts...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredPosts) { post ->
                PostItem(post, onClick = {
                    navController.navigate("postDetails/${post.headline}")
                })
            }
        }
    }
}

@Composable
fun PostItem(post: Post, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.headline, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = post.content, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Posted by: ${post.userName}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            // You can use Image composable here to load and display the user's photo (e.g., using Coil for image loading)
        }
    }
}
