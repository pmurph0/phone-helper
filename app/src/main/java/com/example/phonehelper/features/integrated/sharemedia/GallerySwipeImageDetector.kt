package com.example.phonehelper.features.integrated.sharemedia

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.phonehelper.log

class GallerySwipeImageDetector(private val accessibilityService: AccessibilityService,
                                private val listener: Listener) {

    interface Listener {
        fun onSwipeToNextImage()
        fun onSwipeToPreviousImage()
    }

    companion object {
        private const val IMAGE_VIEW_RESOURCE_ID = "com.oneplus.gallery:id/filmstrip_item_scale_image_view"

        private const val SCROLL_DIRECTION_LEFT = -1
        private const val SCROLL_DIRECTION_RIGHT = 1
        private const val SCROLL_DIRECTION_NONE = 0
    }

    private var scrollingDirection = SCROLL_DIRECTION_NONE
        set(value) {
            val directionLabel: String = when (value) {
                SCROLL_DIRECTION_LEFT -> "SCROLL_DIRECTION_LEFT"
                SCROLL_DIRECTION_RIGHT -> "SCROLL_DIRECTION_RIGHT"
                else -> "SCROLL_DIRECTION_NONE"
            }
            log("scrollingDirection = $directionLabel")
            field = value
        }

    private var lastViewedImageViewNode: AccessibilityNodeInfo? = null

    fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                onWindowContentChanged()
            }
        }
    }

    private fun onScrolling(imageViewNodes: List<AccessibilityNodeInfo>) {
        if (lastViewedImageViewNode == null) return

        val rect = Rect()
        val trackNode = imageViewNodes.get(imageViewNodes.indexOf(lastViewedImageViewNode!!))
        trackNode!!.getBoundsInScreen(rect)

        if (rect.left > 0) {
            scrollingDirection = SCROLL_DIRECTION_LEFT
        } else if (rect.left == 0){
            scrollingDirection = SCROLL_DIRECTION_RIGHT
        } else {
            scrollingDirection = SCROLL_DIRECTION_NONE
        }
    }

    private fun onWindowContentChanged() {
        val currentImageViewNodes = ArrayList<AccessibilityNodeInfo>(2)
        getImageViewNodes(accessibilityService.rootInActiveWindow, currentImageViewNodes)
        if (currentImageViewNodes.size == 2) {
            onScrolling(currentImageViewNodes)
        } else if (currentImageViewNodes.size == 1) {
            onIdle(currentImageViewNodes.first())
        } else {
            log("No imageview nodes on screen!")
        }
    }

    private fun onIdle(imageViewNode: AccessibilityNodeInfo) {
        val wasScrolling = scrollingDirection != SCROLL_DIRECTION_NONE
        if (wasScrolling) {
            if (lastViewedImageViewNode != imageViewNode) {
                log("Changed image")
                if (scrollingDirection == SCROLL_DIRECTION_RIGHT) {
                    log("onSwipeToNextImage")
                    listener.onSwipeToNextImage()
                } else if (scrollingDirection == SCROLL_DIRECTION_LEFT){
                    log("onSwipeToPreviousImage")
                    listener.onSwipeToPreviousImage()
                }
            } else {
                log("Remained on same image")
            }
        }
        scrollingDirection = SCROLL_DIRECTION_NONE
        lastViewedImageViewNode = imageViewNode
    }

    private fun onIdleAfterScrolled() {

    }

    private fun cleanUp() {

    }

    private fun getImageViewNodes(node: AccessibilityNodeInfo, list: ArrayList<AccessibilityNodeInfo>) {
        if (node.viewIdResourceName == IMAGE_VIEW_RESOURCE_ID) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            log("found imageview, rect is ${rect.toShortString()}, is last viewed image = ${node == lastViewedImageViewNode}")
            list.add(node)
        }
        if (node.childCount > 0) {
            for (i in 0 until node.childCount) {
                getImageViewNodes(node.getChild(i), list)
            }
        }
    }

}