package com.awada.synapse.data

import android.content.Context
import kotlinx.serialization.json.Json
import com.awada.synapse.R

object IconCatalogManager {
    private var catalog: IconCatalog? = null
    
    fun load(context: Context): IconCatalog {
        if (catalog == null) {
            val json = context.resources
                .openRawResource(R.raw.icons_catalog)
                .bufferedReader()
                .use { it.readText() }
            catalog = Json.decodeFromString<IconCatalog>(json)
        }
        return catalog!!
    }
    
    fun getCatalog(): IconCatalog? = catalog
}
