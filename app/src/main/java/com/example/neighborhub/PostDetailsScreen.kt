import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.draw.clip
import coil.compose.rememberImagePainter
import com.example.neighborhub.model.Post


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(post: String, navController: NavController) {
    val allPosts = listOf(
        Post("Help with grocery shopping", "Looking for someone to help me buy groceries.", "John Doe", "https://example.com/john_doe.jpg"),
        Post("Need a ride to the hospital", "I need a ride to the hospital for a check-up.", "Jane Smith", "https://example.com/jane_smith.jpg"),
        Post("Volunteer for dog walking", "Looking for someone to walk my dog while I'm at work.", "Alice Brown", "https://example.com/alice_brown.jpg"),
        Post("Need help with homework", "Struggling with my math homework, need assistance.", "Bob White", "https://example.com/bob_white.jpg"),
        Post("Looking for a jogging buddy", "Anyone interested in jogging in the park?", "Charlie Green", "https://example.com/charlie_green.jpg")
    )

    val selectedPost = allPosts.find { it.headline == post }

    selectedPost?.let {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Post Details") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.TopStart
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = rememberImagePainter(it.userPhotoUrl),
                            contentDescription = "User Photo",
                            modifier = Modifier.size(40.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = it.userName, style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = it.headline, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it.content, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}