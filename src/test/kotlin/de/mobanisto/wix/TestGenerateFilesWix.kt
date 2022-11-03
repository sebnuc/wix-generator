package de.mobanisto.wix

import de.topobyte.system.utils.SystemPaths

fun main() {
    val project = SystemPaths.HOME.resolve("github/sebkur/test-compose-for-desktop-notifications")
    val dir = project.resolve("build/compose/tmp/jpackage/images/win-msi.image/Test Notifications")
    val output = project.resolve("wix/Files.wxs")
    println("dir: $dir")
    println("output: $output")
    val task = GenerateFilesWxs(dir, output)
    task.execute()
}
