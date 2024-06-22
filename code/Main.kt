import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

private const val IMAGE_DIRECTORY = "/images"

suspend fun fetchImageFromUrl(imageUrl: String): InputStream? {
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

suspend fun saveImageFromUrl(input: InputStream, outputFilePath: String) {
    withContext(Dispatchers.IO) {
        val bufferedImage = ImageIO.read(input)
        ImageIO.write(bufferedImage, "png", File(outputFilePath))
    }
}

suspend fun repeatInDirection(
    x: String,
    y: String,
    xTimes: Int = 50,
    yTimes: Int = 50
) {
    repeat(xTimes) { i ->
        repeat(yTimes) { j ->
            runCatching {
                val index = "${i + 1}$x${j + 1}$y"
                println("Fetching $index.png")
                fetchImageFromUrl(imageUrl = "http://imgs.xkcd.com/clickdrag/$index.png")
                    ?.let {
                        saveImageFromUrl(
                            input = it,
                            outputFilePath = "$IMAGE_DIRECTORY/$index.png"
                        )
                    }
            }.onFailure {
                println("Couldn't find more images on index ${i + 1}n${j + 1}w: ${it.message}")
            }
        }
    }
}

fun main() {
    runBlocking {
        launch(Dispatchers.IO) { repeatInDirection("n", "w", 9, 33) }
        launch(Dispatchers.IO) { repeatInDirection("n", "e", 9, 48) }
        launch(Dispatchers.IO) { repeatInDirection("s", "w", 5, 17) }
        launch(Dispatchers.IO) { repeatInDirection("s", "e", 5, 7) }
    }
}

