import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

private const val IMAGE_DIRECTORY = "/images"
private const val TILE_HEIGHT = 2048
private const val TILE_WIDTH = 2048

data class Tile(val x: Int, val y: Int, val image: BufferedImage)
data class Dimension(val minX: Int, val maxX: Int, val minY: Int, val maxY: Int)

private fun parseTileName(tileName: String): Pair<Int, Int> {
    val regex = """(\d+)([ns])(\d+)([ew])""".toRegex()
    val matchResult = regex.matchEntire(tileName)
    return if (matchResult != null) {
        val (yNum, yDir, xNum, xDir) = matchResult.destructured
        val y = if (yDir == "n") -yNum.toInt() else yNum.toInt() - 1 // Adjust 's' part by -1 to correct the row placement
        val x = if (xDir == "e") xNum.toInt() else -xNum.toInt()
        Pair(x, y)
    } else {
        throw IllegalArgumentException("Invalid tile name: $tileName")
    }
}

private fun loadTiles(directory: String, targetWidth: Int, targetHeight: Int): List<Tile> {
    val tileFiles = File(directory).listFiles { _, name -> name.endsWith(".png") } ?: arrayOf()
    return tileFiles.mapNotNull { file ->
        try {
            val tileName = file.nameWithoutExtension
            val (x, y) = parseTileName(tileName)
            val image = ImageIO.read(file)
            val resizedImage = resizeImage(image, targetWidth, targetHeight)
            Tile(x, y, resizedImage)
        } catch (e: Exception) {
            println("Failed to process file ${file.name}: ${e.message}")
            null
        }
    }
}

private fun resizeImage(originalImage: BufferedImage, targetWidth: Int, targetHeight: Int): BufferedImage {
    val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
    resizedImage.createGraphics().apply {
        setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        drawImage(originalImage, 0, 0, targetWidth, targetHeight, null)
        dispose()
    }
    return resizedImage
}

private fun findDimensions(tiles: List<Tile>): Dimension {
    val minX = tiles.minOf { it.x }
    val maxX = tiles.maxOf { it.x }
    val minY = tiles.minOf { it.y }
    val maxY = tiles.maxOf { it.y }
    return Dimension(minX, maxX, minY, maxY)
}

private fun buildRows(tiles: List<Tile>) {

    val dimension = findDimensions(tiles)

    // Process each row separately
    for (y in dimension.minY..dimension.maxY) {
        val rowTiles = tiles.filter { it.y == y }

        val rowImageWidth = (dimension.maxX - dimension.minX + 1) * TILE_WIDTH

        // Create the row image
        val rowImage = BufferedImage(rowImageWidth, TILE_HEIGHT, BufferedImage.TYPE_INT_RGB)
        val g2d = rowImage.createGraphics().apply {
            // Fill the row image with the appropriate background color
            color = if (y < 0) Color.WHITE else Color.BLACK
            fillRect(0, 0, rowImageWidth, TILE_HEIGHT)
        }

        // Draw tiles onto the row image
        for (tile in rowTiles) {
            val x = (tile.x - dimension.minX) * TILE_WIDTH
            g2d.drawImage(tile.image, x, 0, null)
        }

        g2d.dispose()

        runCatching {
            ImageIO.write(rowImage, "png", File("$IMAGE_DIRECTORY/output/row_${y + abs(dimension.minY)}.png"))
            println("Row $y image saved successfully.")
        }.onFailure {  println("Failed to save the row $y image: ${it.message}") }
    }
}

fun main() {
    val tiles = loadTiles(IMAGE_DIRECTORY, TILE_WIDTH, TILE_HEIGHT)

    if (tiles.isEmpty()) {
        println("No tiles were loaded. Please check the directory and file names.")
        return
    }

    buildRows(tiles)

    println("All rows processed successfully.")
}
