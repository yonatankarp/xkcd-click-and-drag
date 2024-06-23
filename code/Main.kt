import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

private const val imageDirectory = "/images"

fun main(args: Array<String>) {
    if (args.isEmpty() || args.contains("--help") || args.contains("-h")) {
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

    when {
        runCombineAll -> Combiner(imageDirectory, 256, 256).combineAll()
        runCombine -> Combiner(imageDirectory).combine(6)
    }
}

private fun printHelp() {
    println("Usage: program [options]")
    println("Options:")
    println("  -h --help            Print this help message")
    println("  -d --directory       Specify the directory to save the images (default: /images)")
    println("  -f --fetch           Run the fetch process")
    println("  -c --combine         Run the combine process")
    println("  -C --combine-all     Run the combine process with all images (each tile is 256x256 pixels)")
    println("  -a --all             Run both fetch and combine processes")
}

private fun Array<String>.shouldFetch() =
    this.contains("--fetch") || this.contains("-f") || this.contains("--all") || this.contains("-a")

private fun Array<String>.shouldCombineRows() =
    this.contains("--combine") || this.contains("-c") || this.contains("--all") || this.contains("-a")

private fun Array<String>.shouldCombineAll() =
    this.contains("--combine-all") || this.contains("-C")

private fun Array<String>.getImageDirectoryOrDefault() =
    this.firstOrNull { it.startsWith("--directory=") || it.startsWith("-d=") }
        ?.substringAfter("=")
        ?: imageDirectory
