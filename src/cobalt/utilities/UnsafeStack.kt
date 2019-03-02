package cobalt.utilities

/**
 * A stack implementation that throws exceptions when peeking/popping and the stack is empty!
 *
 * Adapted from https://github.com/gazolla/Kotlin-Algorithm/tree/master/Stack
 */
class UnsafeStack<T>() {

    var items = mutableListOf<T>()

    fun isEmpty():Boolean = this.items.isEmpty()

    fun count():Int = this.items.count()

    fun push(element:T) {
        val position = this.count()
        this.items.add(position, element)
    }

    override fun toString() = this.items.toString()

    fun pop():T {
        if (this.isEmpty()) {
            throw StackEmptyException()
        } else {
            val item =  this.items.count() - 1
            return this.items.removeAt(item)
        }
    }

    fun peek():T {
        if (isEmpty()) {
            throw StackEmptyException()
        } else {
            return this.items[this.items.count() - 1]
        }
    }

}