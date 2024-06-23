package com.yonatankarp.xkcd.clickanddrag

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val MAX_COMBINED_ROWS = 3
private const val DEFAULT_IMAGES_DIRECTORY = "/images"

fun main(args: Array<String>) {
    if (args.shouldPrintHelp()) {
        printHelp()
        return
    }

    val runFetch = args.shouldFetch()
    val runCombine = args.shouldCombineRows()
    val runCombineAll = args.shouldCombineAll()
    val imageDirectory = args.getImageDirectoryOrDefault()

    if (runFetch) {
        runBlocking {
            val fetcher = Fetcher(imageDirectory)
            launch(Dispatchers.IO) {
                fetcher.repeatInDirection(
                    latitude = "n",
                    longitude = "w",
                    latitudeSteps = 9,
                    longitudeSteps = 33
                )
            }
            launch(Dispatchers.IO) {
                fetcher.repeatInDirection(
                    latitude = "n",
                    longitude = "e",
                    latitudeSteps = 9,
                    longitudeSteps = 48
                )
            }
            launch(Dispatchers.IO) {
                fetcher.repeatInDirection(
                    latitude = "s",
                    longitude = "w",
                    latitudeSteps = 5,
                    longitudeSteps = 17
                )
            }
            launch(Dispatchers.IO) {
                fetcher.repeatInDirection(
                    latitude = "s",
                    longitude = "e",
                    latitudeSteps = 5,
                    longitudeSteps = 7
                )
            }
        }
    }

    val (width, height) = args.getTileSize(runCombineAll)

    when {
        runCombineAll -> Combiner(imageDirectory, width, height).combineAll()
        runCombine -> Combiner(imageDirectory).combine(MAX_COMBINED_ROWS)
    }
}

private fun printHelp() {
    println("Usage: program [options]")
    println("Options:")
    println("  -h --help            Print this help message")
    println("  -a --all             Run both fetch and combine processes")
    println("  -d --directory       Specify the directory to save the images (default: /images)")
    println("  -f --fetch           Run the fetch process")
    println("  -c --combine         Run the combine process")
    println("  -C --combine-all     Run the combine process with all images")
    println("  -s --size            Specify the size of the tiles (default: 2048x2048 for combine and 256x256 for combine-all)")
}

private fun Array<String>.shouldPrintHelp() =
    this.isEmpty() || this.contains("--help") || this.contains("-h")

private fun Array<String>.shouldFetch() =
    this.contains("--fetch") || this.contains("-f") || this.contains("--all") || this.contains(
        "-a"
    )

private fun Array<String>.shouldCombineRows() =
    this.contains("--combine") || this.contains("-c") || this.contains("--all") || this.contains(
        "-a"
    )

private fun Array<String>.shouldCombineAll() =
    this.contains("--combine-all") || this.contains("-C")

private fun Array<String>.getImageDirectoryOrDefault() =
    this.firstOrNull { it.startsWith("--directory=") || it.startsWith("-d=") }
        ?.substringAfter("=")
        ?: DEFAULT_IMAGES_DIRECTORY

private fun Array<String>.getTileSize(shouldCombineAllTiles: Boolean): Pair<Int, Int> {
    val tileSize =
        this.firstOrNull { it.startsWith("--size=") || it.startsWith("-s=") }
            ?.substringAfter("=")
            ?.split("x")
            ?.let { (width, height) -> Pair(width.toInt(), height.toInt()) }
            ?: if (shouldCombineAllTiles) Pair(256, 256) else Pair(2048, 2048)

    if (tileSize.first <= 0 || tileSize.second <= 0 || tileSize.first > 2048 || tileSize.second > 2048) {
        throw IllegalArgumentException("Tile size must be positive and less than or equal to 2048")
    }

    return tileSize
}
