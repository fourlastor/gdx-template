package io.github.fourlastor.monster.extension

import com.artemis.EntityEdit
import com.artemis.World

inline fun World.create(action: EntityEdit.() -> Unit) = create().also { edit(it).apply(action) }
