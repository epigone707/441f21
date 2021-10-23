package edu.umich.yanfuguo.kotlinjpcchatter

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Chatt(var username: String? = null,
            var message: String? = null,
            var timestamp: String? = null)