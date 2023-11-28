package com.hoanv.notetimeplanner.utils.extension

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.util.Size
import androidx.annotation.CheckResult
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun Bitmap.resize(maxSize: Int): Bitmap {
    var width: Int = this.width
    var height: Int = this.height

    val bitmapRatio = width.toFloat() / height.toFloat()
    if (bitmapRatio > 1) {
        width = maxSize
        height = (width / bitmapRatio).toInt()
    } else {
        height = maxSize
        width = (height * bitmapRatio).toInt()
    }
    return Bitmap.createScaledBitmap(this, width, height, true)
}

fun Bitmap.crop(crop: Rect): Bitmap {
    require(crop.left < crop.right && crop.top < crop.bottom) { "Cannot use negative crop" }
    require(crop.left >= 0 && crop.top >= 0 && crop.bottom <= this.height && crop.right <= this.width) {
        "Crop is outside the bounds of the image"
    }
    return Bitmap.createBitmap(this, crop.left, crop.top, crop.width(), crop.height())
}

@CheckResult
fun maxAspectRatioInSize(area: Size, aspectRatio: Float): Size {
    var width = area.width
    var height = (width / aspectRatio).roundToInt()

    return if (height <= area.height) {
        Size(area.width, height)
    } else {
        height = area.height
        width = (height * aspectRatio).roundToInt()
        Size(min(width, area.width), height)
    }
}

@CheckResult
fun Size.scaleAndCenterWithin(containingSize: Size): Rect {
    val aspectRatio = width.toFloat() / height

    // Since the preview image may be at a different resolution than the full image, scale the
    // preview image to be circumscribed by the fullImage.
    val scaledSize = maxAspectRatioInSize(containingSize, aspectRatio)
    val left = (containingSize.width - scaledSize.width) / 2
    val top = (containingSize.height - scaledSize.height) / 2
    return Rect(
        /* left */ left,
        /* top */ top,
        /* right */ left + scaledSize.width,
        /* bottom */ top + scaledSize.height
    )
}

fun Bitmap.size(): Size = Size(this.width, this.height)

fun cropImage(fullImage: Bitmap, previewSize: Size, cardFinder: Rect): Bitmap {
    require(
        cardFinder.left >= 0 &&
                cardFinder.right <= previewSize.width &&
                cardFinder.top >= 0 &&
                cardFinder.bottom <= previewSize.height
    ) { "Card finder is outside preview image bounds" }

    // Scale the previewImage to match the fullImage
    val scaledPreviewImage = previewSize.scaleAndCenterWithin(fullImage.size())
    val previewScale = scaledPreviewImage.width().toFloat() / previewSize.width

    // Scale the cardFinder to match the scaledPreviewImage
    val scaledCardFinder = Rect(
        (cardFinder.left * previewScale).roundToInt(),
        (cardFinder.top * previewScale).roundToInt(),
        (cardFinder.right * previewScale).roundToInt(),
        (cardFinder.bottom * previewScale).roundToInt()
    )

    // Position the scaledCardFinder on the fullImage
    val cropRect = Rect(
        max(0, scaledCardFinder.left + scaledPreviewImage.left),
        max(0, scaledCardFinder.top + scaledPreviewImage.top),
        min(fullImage.width, scaledCardFinder.right + scaledPreviewImage.left),
        min(fullImage.height, scaledCardFinder.bottom + scaledPreviewImage.top)
    )

    return fullImage.crop(cropRect)
}

fun storeImage(image: Bitmap, photoFile: File): Uri {
    try {
        val fos = FileOutputStream(photoFile)
        image.compress(Bitmap.CompressFormat.PNG, 90, fos)
        fos.close()
    } catch (_: FileNotFoundException) {
    } catch (_: IOException) {
    }
    return Uri.fromFile(photoFile)
}