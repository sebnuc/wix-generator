package de.mobanisto.wix

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Stack
import java.util.UUID
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.visitFileTree

class GenerateFilesWxs(private val dir: Path, private val output: Path) {

    fun execute() {
        val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = documentBuilder.newDocument()

        createDocument(doc, dir)

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
    private fun createDocument(doc: Document, dir: Path) {
        val wix = doc.createElement("Wix").apply {
            setAttribute("xmlns", "http://schemas.microsoft.com/wix/2006/wi")
            doc.appendChild(this)
        }
        val fragment = wix.createChild("Fragment", "FragmentFiles")
        val targetDir = fragment.createChild("DirectoryRef", "TARGETDIR")
        val programFiles = targetDir.createChild("Directory", "ProgramFiles64Folder")
        val installDir = programFiles.createChild("Directory", "INSTALLDIR").apply {
            setAttribute("Name", "Hello World")
        }
        val ids = buildFileTree(installDir, dir)

        val componentGroup = fragment.createChild("ComponentGroup", "Files")
        for (id in ids) {
            componentGroup.createChild("ComponentRef", id)
        }
    }

    @OptIn(ExperimentalPathApi::class)
    private fun buildFileTree(installDir: Element, dir: Path): List<String> {
        val stack = Stack<Element>()
        stack.push(installDir)
        var current = dir
        var fileId = 1
        val list: MutableList<String> = ArrayList()
        dir.visitFileTree {
            onPreVisitDirectory { directory, _ ->
                val relative = current.relativize(directory)
                if (relative.toString().isEmpty()) {
                    return@onPreVisitDirectory FileVisitResult.CONTINUE
                }
                val indent = "  ".repeat(stack.size - 1)
                println("${indent}directory: $relative")
                val uuid = UUID.randomUUID()
                stack.peek().createChild("Directory", fileId(uuid)).apply {
                    setAttribute("Name", relative.toString())
                    stack.push(this)
                }
                current = directory
                FileVisitResult.CONTINUE
            }

            onPostVisitDirectory { _, _ ->
                current = current.parent
                val top = stack.peek()
                if (!top.hasChildNodes()) {
                    top.parentNode.removeChild(top)
                }
                stack.pop()
                FileVisitResult.CONTINUE
            }

            onVisitFile { file, _ ->
                val relative = current.relativize(file)
                val relativeToDir = dir.relativize(file)
                if (relativeToDir.equals(Paths.get("app", ".jpackage.xml"))) {
                    return@onVisitFile FileVisitResult.CONTINUE
                }
                val indent = "  ".repeat(stack.size - 1)
                println("${indent}file: $relative")
                val id = "File${fileId++}"
                list.add(id)
                val uuid = UUID.randomUUID()
                val component = stack.peek().createChild("Component", id).apply {
                    setAttribute("Guid", "{$uuid}")
                }
                component.createChild("File", fileId(uuid)).apply {
                    setAttribute("KeyPath", "yes")
                    setAttribute("Source", file.toString())
                }
                FileVisitResult.CONTINUE
            }
        }
        return list
    }

    private fun fileId(uuid: UUID): String {
        return "file" + uuid.toString().replace("-", "")
    }

    private fun Element.createChild(tagName: String, id: String): Element {
        val fragment = createChild(tagName).apply {
            setAttribute("Id", id)
        }
        return appendChild(fragment) as Element
    }

    private fun Element.createChild(tagName: String): Element {
        val fragment = ownerDocument.createElement(tagName)
        return appendChild(fragment) as Element
    }
}
