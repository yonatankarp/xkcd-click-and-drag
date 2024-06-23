# XKCD—Click and Drag

![Click and Drag starting point](./entry-point.png)

This repository contains the complete image of the "Click and Drag" comic by XKCD. You can view the original comic [here](https://xkcd.com/1110/).

The repository includes the following files:

- [images/tiles](./images/tiles): The individual tiles as they appear on the XKCD website.
- [images/rows](./images/rows): Five files, each representing another part of the full comic.

Additionally,
you can get the full comic by using the file `xkcd-click-and-drag.png`.
Please be patient, as it is a large file!

For those who use Gimp,
you can open the file that combines all the rows by clicking
`xkcd-click-and-drag.xcf`.
Note that this file requires approximately 43GB of memory.

Enjoy 🎉

---

The code can be executed with the following parameters:

```shell
Usage: program [options]
Options:
  -h --help            Print this help message
  -d --directory       Specify the directory to save the images (default: /images)
  -f --fetch           Run the fetch process
  -c --combine         Run the combine process (using the maximum number of rows possible per image)
  -C --combine-all     Run the combine process with all images (each tile is 256x256 pixels)
  -a --all             Run both fetch and combine processes
```
