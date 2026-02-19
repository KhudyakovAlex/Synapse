package com.awada.synapse.components

import android.content.Context
import com.awada.synapse.R
import com.awada.synapse.data.IconCatalogManager

fun iconResId(context: Context, iconId: Int, fallback: Int = R.drawable.controller_100_default): Int {
    val catalog = IconCatalogManager.load(context)
    val info = catalog.findById(iconId) ?: return fallback
    val resId = context.resources.getIdentifier(info.resourceName, "drawable", context.packageName)
    return if (resId != 0) resId else fallback
}

