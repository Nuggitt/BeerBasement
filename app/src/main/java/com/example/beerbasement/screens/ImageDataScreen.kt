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
import com.example.beerbasement.R

@Composable
fun ImageDataScreen(imageUri: Uri) {
    val painter = rememberAsyncImagePainter(
        model = imageUri,

    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Displaying the image from the URI
        Image(
            painter = painter,
            contentDescription = "Captured Image",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f), // Keeps the aspect ratio intact
            alignment = Alignment.Center
        )

        Spacer(modifier = Modifier.height(16.dp)) // Adds space between the image and text

        // Optionally add a title or other information about the image
        Text(
            text = "Captured Image",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
