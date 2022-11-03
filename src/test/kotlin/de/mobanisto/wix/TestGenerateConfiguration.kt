package de.mobanisto.wix

import de.topobyte.system.utils.SystemPaths

fun main() {
    val upgradeCode = "fd9ba43a-7461-4230-bd8a-1617f9c192fe"
    val vendor = "Mobanisto"
    val productName = "Test Notifications"
    val version = "0.0.1"
    val description = "An application for testing notifications"

    val project = SystemPaths.HOME.resolve("github/sebkur/test-compose-for-desktop-notifications")
    val dir = project.resolve("build/compose/tmp/jpackage/images/win-msi.image/Test Notifications")
    val outputFiles = project.resolve("wix/Files.wxs")
    val outputProduct = project.resolve("wix/Product.wxs")
    println("dir: $dir")
    println("output files: $outputFiles")
    println("output product: $outputProduct")
    val executables = GenerateFilesWxs(outputFiles, dir, productName).execute()
    val mainExecutable = executables[0]
    GenerateProductWxs(outputProduct, upgradeCode, vendor, productName, version, description, mainExecutable).execute()
}
