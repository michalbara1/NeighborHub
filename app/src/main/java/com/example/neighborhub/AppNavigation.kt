import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.neighborhub.PostsListScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "postsList") {
        composable("postsList") {
            PostsListScreen(navController)
        }
        composable("postDetails/{post}") { backStackEntry ->
            val post = backStackEntry.arguments?.getString("post") ?: ""
            PostDetailsScreen(post, navController)
        }
    }
}
