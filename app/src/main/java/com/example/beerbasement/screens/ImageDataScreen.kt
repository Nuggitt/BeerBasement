import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter

@Composable
fun ImageDataScreen(imageUri: Uri) {
    val painter = rememberAsyncImagePainter(imageUri)

    // Adding a column layout for better UI
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Displaying the image from the URI
        Image(
            painter = painter,
            contentDescription = "Captured Image",
            modifier = Modifier.fillMaxSize()
        )

        // Optionally add a title or other information about the image
        Text(
            text = "Captured Image",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(16.dp)
        )
    }
}
