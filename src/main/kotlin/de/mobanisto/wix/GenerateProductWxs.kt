package de.mobanisto.wix

import de.mobanisto.wix.GenerateFilesWxs.FileEntry
import org.w3c.dom.Document
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.io.path.ExperimentalPathApi

class GenerateProductWxs(
        private val output: Path,
        private val upgradeCode: String,
        private val vendor: String,
        private val name: String,
        private val version: String,
        private val description: String,
        private val mainExecutable: FileEntry,
) {

    fun execute() {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = documentBuilder.newDocument()

        createDocument(doc)

        val transformerFactory = TransformerFactory.newInstance()
        val transformer = transformerFactory.newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        val source = DOMSource(doc)
        val os = Files.newOutputStream(output)
        os.use {
            val result = StreamResult(it)
            transformer.transform(source, result)
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private fun createDocument(doc: Document) {
        val iconId = "hello_ico"
        val iconSource = "hello.ico"

        val productId = UUID.nameUUIDFromBytes("$vendor/$name/$version".toByteArray(Charsets.UTF_8))
        val wix = doc.createElement("Wix").apply {
            setAttribute("xmlns", "http://schemas.microsoft.com/wix/2006/wi")
            doc.appendChild(this)
        }
        val product = wix.createChild("Product") {
            setAttribute("Id", productId.toString())
            setAttribute("Name", name)
            setAttribute("Version", version)
            setAttribute("Manufacturer", vendor)
            setAttribute("Language", "1033")
            setAttribute("UpgradeCode", upgradeCode)
        }
        product.createChild("Package") {
            setAttribute("Description", description)
            setAttribute("Manufacturer", vendor)
            setAttribute("InstallerVersion", "200")
            setAttribute("Compressed", "yes")
            setAttribute("InstallScope", "perMachine") // alternative: "perUser"
            // setAttribute("Platform", "x64") // Use of this switch is discouraged in favor of the -arch switch
        }
        product.createChild("WixVariable", "WixUIBannerBmp") {
            setAttribute("Value", "banner.bmp")
        }
        product.createChild("WixVariable", "WixUIDialogBmp") {
            setAttribute("Value", "dialog.bmp")
        }
        product.createChild("MediaTemplate") {
            setAttribute("EmbedCab", "yes")
        }
        product.createChild("Directory", "TARGETDIR") {
            setAttribute("Name", "SourceDir")
        }
        product.createChild("DirectoryRef", "TARGETDIR") {
            createChild("Directory", "ProgramMenuFolder") {
                createChild("Directory", "ApplicationProgramsFolder") {
                    setAttribute("Name", vendor)
                }
            }
        }
        product.createChild("DirectoryRef", "ApplicationProgramsFolder") {
            createChild("Component", "ApplicationShortcut") {
                val uuid = UUID.randomUUID()
                setAttribute("Guid", "{$uuid}")
                createChild("Shortcut", "ApplicationStartMenuShortcut") {
                    setAttribute("Name", name)
                    setAttribute("Description", description)
                    setAttribute("Target", "[#${mainExecutable.fileId}]")
                    setAttribute("WorkingDirectory", "APPLICATIONROOTDIRECTORY")
                    setAttribute("Icon", iconId)
                    createChild("ShortcutProperty") {
                        setAttribute("Key", "System.AppUserModel.ID")
                        setAttribute("Value", "$vendor.$name")
                    }
                }
                createChild("RemoveFolder", "CleanupShortcut") {
                    setAttribute("Directory", "ApplicationProgramsFolder")
                    setAttribute("On", "uninstall")
                }
                createChild("RegistryValue") {
                    setAttribute("Root", "HKCU")
                    setAttribute("Key", "Software\\$vendor\\$name")
                    setAttribute("Name", "installed")
                    setAttribute("Type", "integer")
                    setAttribute("Value", "1")
                    setAttribute("KeyPath", "yes")
                }
            }
        }
        product.createChild("Feature", "MainFeature") {
            createChild("ComponentGroupRef", "Files")
            createChild("ComponentRef", "ApplicationShortcut")
        }
        product.createChild("Property", "WIXUI_INSTALLDIR") {
            setAttribute("Value", "INSTALLDIR")
        }
        product.createChild("UIRef", "InstallUI")

        product.createChild("Icon", iconId) {
            setAttribute("SourceFile", iconSource)
        }
        product.createChild("Property", "ARPPRODUCTICON") {
            setAttribute("Value", iconId)
        }
    }
}
