package dev.liquid.osamaclient.util.cache

class BinaryHeapOpenSet(private var capacity: Int = 1024) {
    var items: Array<PathNode?> = arrayOfNulls(this.capacity)
    var size = 0

    fun add(node: PathNode){
        this.ensureCapacity()

        this.items[this.size] = node
        this.size++
        this.heapUp()
    }

    fun poll(): PathNode?{
        val nodeToPoll = this.items[0]
        this.items[0] = this.items[this.size - 1]
        this.size--
        this.heapDown()
        return nodeToPoll
    }

    fun isEmpty() = this.size == 0

    private fun heapUp(){
        var index = this.size - 1
        var parentIndex = this.parentIndex(index)

        while(index != 0 && this.items[parentIndex]!!.getFCost() > this.items[index]!!.getFCost()){
            this.swap(index, parentIndex)
            index = parentIndex
            parentIndex = this.parentIndex(index)
        }
    }

    private fun heapDown(){
        var index = 0

        while(leftChildIndex(index) < this.size){
            var smallChildIndex = this.leftChildIndex(index)
            val rightChildIndex = this.rightChildIndex(index)

            if(rightChildIndex < this.size && this.items[rightChildIndex]!!.getFCost() < this.items[smallChildIndex]!!.getFCost()){
                smallChildIndex = rightChildIndex
            }

            if(this.items[index]!!.getFCost() > this.items[smallChildIndex]!!.getFCost()){
                swap(index, smallChildIndex)
                index = smallChildIndex
            }else{
                break
            }
        }
    }

    private fun swap(index1: Int, index2: Int){
        val temp = this.items[index1]
        this.items[index1] = this.items[index2]
        this.items[index2] = temp
    }

    private fun ensureCapacity(){
        if(this.size == this.capacity){
            this.capacity *= 2
            this.items = this.items.copyOf(this.capacity)
        }
    }

    private fun parentIndex(childIndex: Int) = childIndex ushr 1
    private fun leftChildIndex(parentIndex: Int) = parentIndex * 2 + 1
    private fun rightChildIndex(parentIndex: Int) = parentIndex * 2 + 2
}