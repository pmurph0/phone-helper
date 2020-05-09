package com.example.phonehelper.features.integrated.sharemedia

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.phonehelper.log

class GalleryMediaPositionTracker(private val accessibilityService: AccessibilityService) {

    companion object {
        private const val IMAGE_VIEW_RESOURCE_ID = "com.oneplus.gallery:id/filmstrip_item_scale_image_view"

        private const val SCROLL_DIRECTION_LEFT = -1
        private const val SCROLL_DIRECTION_RIGHT = 1
        private const val SCROLL_DIRECTION_NONE = 0
    }

    private var scrollingDirection = SCROLL_DIRECTION_NONE
        set(value) {
            when (value) {
                SCROLL_DIRECTION_LEFT -> log("scrolling to previous")
                SCROLL_DIRECTION_RIGHT -> log("scrolling to next")
            }
            field = value
        }

    private var lastViewedImageViewNode: AccessibilityNodeInfo? = null
    private var isDeleting = false
    var currentPosition = 0
        private set

    fun onAccessibilityEvent(event: AccessibilityEvent) {

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {

                onWindowContentChanged()
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                if (event.source?.viewIdResourceName?.contains("delete") == true) { //TODO use equals for better efficiency?
                    log("delete clicked")
                    onDeleteImageClick()
                }
            }

        }
    }

    private fun onDeleteImageClick() {
        isDeleting = true
    }

    private fun onScrolling(imageViewNodes: List<AccessibilityNodeInfo>) {
        if (lastViewedImageViewNode == null) return

        val rect = Rect()
        val trackNode = imageViewNodes.get(imageViewNodes.indexOf(lastViewedImageViewNode!!))
        trackNode.getBoundsInScreen(rect)

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
            onScrollStopped(imageViewNode)
        }
        scrollingDirection = SCROLL_DIRECTION_NONE
        lastViewedImageViewNode = imageViewNode
        isDeleting = false
    }

    private fun onScrollStopped(imageViewNode: AccessibilityNodeInfo) {
        if (lastViewedImageViewNode != imageViewNode) {
            log("Changed image")
            if (scrollingDirection == SCROLL_DIRECTION_RIGHT) {
                log("onSwipeToNextImage")
                if (!isDeleting) {
                    currentPosition++
                }
            } else if (scrollingDirection == SCROLL_DIRECTION_LEFT) {
                log("onSwipeToPreviousImage")
                currentPosition--
            }
        } else {
            log("Remained on same image")
        }
    }

    private fun getImageViewNodes(node: AccessibilityNodeInfo, list: ArrayList<AccessibilityNodeInfo>) {
        if (node.viewIdResourceName == IMAGE_VIEW_RESOURCE_ID) {
            list.add(node)
        }
        if (node.childCount > 0) {
            for (i in 0 until node.childCount) {
                val childNode = node.getChild(i)
                if (childNode != null) {
                    getImageViewNodes(childNode, list)
                }
            }
        }
    }

}