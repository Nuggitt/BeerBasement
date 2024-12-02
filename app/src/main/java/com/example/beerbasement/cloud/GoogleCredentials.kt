import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import java.io.InputStream
import java.util.Collections

// Funktion til at hente servicekontoens legitimationsoplysninger fra assets
fun getCredentialsFromAssets(context: Context): GoogleCredentials {
    val inputStream: InputStream = context.assets.open("beerbasementproject-4cd09c30fd67.json")
    return GoogleCredentials.fromStream(inputStream)
        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"))
}
