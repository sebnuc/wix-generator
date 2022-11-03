package de.mobanisto.wix

import org.w3c.dom.Element


fun Element.createChild(tagName: String, block: Element.() -> Unit = {}): Element {
    val fragment = ownerDocument.createElement(tagName).apply(block)
    return appendChild(fragment) as Element
}

fun Element.createChild(tagName: String, id: String, block: Element.() -> Unit = {}): Element {
    val fragment = createChild(tagName).apply {
        setAttribute("Id", id)
        block()
    }
    return appendChild(fragment) as Element
}
