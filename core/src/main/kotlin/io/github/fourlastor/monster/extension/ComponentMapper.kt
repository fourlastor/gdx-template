package io.github.fourlastor.monster.extension

import com.artemis.Component
import com.artemis.ComponentMapper

inline fun <T : Component> Int.onMapper(mapper: ComponentMapper<T>, action: (T) -> Unit) {
    takeIf { mapper.has(this) }
        ?.let { mapper.get(it) }
        ?.apply(action)
}