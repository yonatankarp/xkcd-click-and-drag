import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.abs

data class Tile(val x: Int, val y: Int, val image: BufferedImage)
data class Dimension(val minX: Int, val maxX: Int, val minY: Int, val maxY: Int)

class Combiner(
    private val imageDirectory: String,
    private val tileHeight: Int = 2048,
    private val tileWidth: Int = 2048
) {
    private val tiles = loadTiles(imageDirectory, tileWidth, tileHeight)

    fun combine(maxRows: Int = Int.MAX_VALUE) {
        if (tiles.isEmpty()) {
            println("No tiles were loaded. Please check the directory and file names.")
            return
        }

        buildRows(tiles, maxRows)

        println("All rows processed successfully.")
    }

    fun combineAll() {

        val dimension = findDimensions(tiles)
        val finalImageWidth = (dimension.maxX - dimension.minX + 1) * tileWidth
        val finalImageHeight = (dimension.maxY - dimension.minY + 1) * tileHeight

        val finalImage = BufferedImage(finalImageWidth, finalImageHeight, BufferedImage.TYPE_INT_RGB)
        val g2d = finalImage.createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        }

        // Fill the final image with the appropriate background colors
        for (y in 0 until finalImageHeight step tileHeight) {
            g2d.color = if (y / tileHeight + dimension.minY < 0) Color.WHITE else Color.BLACK
            g2d.fillRect(0, y, finalImageWidth, tileHeight)
        }

        // Draw tiles onto the final image
        for (tile in tiles) {
            val x = (tile.x - dimension.minX) * tileWidth
            val y = (tile.y - dimension.minY) * tileHeight
            val resizedImage = resizeImage(tile.image, tileWidth, tileHeight)
            g2d.drawImage(resizedImage, x, y, null)
        }

        g2d.dispose()

        runCatching {
            ImageIO.write(finalImage, "png", File("$imageDirectory/output/combine-all.png"))
            println("Low-resolution image saved successfully.")
        }.onFailure { println("Failed to save the low-resolution image: ${it.message}") }
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

    private fun buildRows(tiles: List<Tile>, maxRows: Int) {

        val dimension = findDimensions(tiles)

        // Process each row separately
        for (y in dimension.minY..minOf(dimension.maxY, dimension.minY + maxRows - 1)) {
            val rowTiles = tiles.filter { it.y == y }

            val rowImageWidth = (dimension.maxX - dimension.minX + 1) * tileWidth

            // Create the row image
            val rowImage = BufferedImage(rowImageWidth, tileHeight, BufferedImage.TYPE_INT_RGB)
            val g2d = rowImage.createGraphics().apply {
                // Fill the row image with the appropriate background color
                color = if (y < 0) Color.WHITE else Color.BLACK
                fillRect(0, 0, rowImageWidth, tileHeight)
            }

            // Draw tiles onto the row image
            for ((index, tile) in rowTiles.sortedBy { it.x }.withIndex()) {
                val x = index * tileWidth
                g2d.drawImage(tile.image, x, 0, null)
            }

            g2d.dispose()

            runCatching {
                ImageIO.write(rowImage, "png", File("$imageDirectory/output/row_${y + abs(dimension.minY)}.png"))
                println("Row $y image saved successfully.")
            }.onFailure {  println("Failed to save the row $y image: ${it.message}") }
        }
    }

    private fun findDimensions(tiles: List<Tile>): Dimension {
        val minX = tiles.minOf { it.x }
        val maxX = tiles.maxOf { it.x }
        val minY = tiles.minOf { it.y }
        val maxY = tiles.maxOf { it.y }
        return Dimension(minX, maxX, minY, maxY)
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
}








