import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Fetcher(private val imageDirectory: String) {

    suspend fun repeatInDirection(
        latitude: String,
        longitude: String,
        latitudeSteps: Int = 50,
        longitudeSteps: Int = 50
    ) {
        repeat(latitudeSteps) { i ->
            repeat(longitudeSteps) { j ->
                runCatching {
                    val index = "${i + 1}$latitude${j + 1}$longitude"
                    println("Fetching $index.png")
                    fetchImageFromUrl(imageUrl = "http://imgs.xkcd.com/clickdrag/$index.png")
                        ?.let {
                            saveImageFromUrl(
                                input = it,
                                outputFilePath = "$imageDirectory/$index.png"
                            )
                        }
                }.onFailure {
                    println("Couldn't find more images on index ${i + 1}n${j + 1}w: ${it.message}")
                }
            }
        }
    }

    private suspend fun fetchImageFromUrl(imageUrl: String): InputStream? {
        HttpClient(CIO).use { client ->
            val response = client.get(imageUrl)
            if (response.status.value == 200) {
                return response.bodyAsChannel().toInputStream()
            } else {
                println("Failed to fetch image from $imageUrl")
                return null
            }
        }
    }

    private suspend fun saveImageFromUrl(input: InputStream, outputFilePath: String) {
        withContext(Dispatchers.IO) {
            val bufferedImage = ImageIO.read(input)
            ImageIO.write(bufferedImage, "png", File(outputFilePath))
        }
    }
}
