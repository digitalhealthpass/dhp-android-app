package com.merative.healthpass.extensions

fun <T> List<T>?.toArrayList(): ArrayList<T> = if (this == null) ArrayList() else ArrayList(this)

fun <T> List<T>?.toStringWithoutBrackets(): String {
    return this?.toString()
        ?.replace("[", "")?.replace("]", "")
        .orEmpty()
}

fun <Key, Value> Iterable<Value>?.toHashMapWithValue(keyTransformer: (model: Value) -> Key): HashMap<Key, Value> {
    val map = HashMap<Key, Value>()
    if (this == null)
        return map

    forEach {
        val key = keyTransformer(it)
        map[key] = it
    }

    return map
}

fun <Key, Value> Iterable<Key>?.toHashMapWithKey(valueTransformer: (model: Key) -> Value): HashMap<Key, Value> {
    val map = HashMap<Key, Value>()
    if (this == null)
        return map

    forEach {
        val key = it
        map[key] = valueTransformer(it)
    }

    return map
}

fun <T> ArrayList<T>?.orEmpty(): ArrayList<T> = this ?: ArrayList()

fun <T> ArrayList<T>.addOrReplace(model: T, samePosition: Boolean): Boolean {
    val indexOf = indexOf(model)
    remove(model)

    if (indexOf > -1 && samePosition) {
        add(indexOf, model)
    } else {
        add(model)
    }
    return indexOf > -1
}

fun <T> ArrayList<T>.replaceIndex(model: T, index: Int): Boolean {
    if (index > size - 1)
        return false
    removeAt(index)
    add(index, model)
    return true
}

fun <T> ArrayList<T>.addOrReplaceAll(newList: List<T>, samePosition: Boolean = false) {
    newList.forEach {
        addOrReplace(it, samePosition)
    }
}