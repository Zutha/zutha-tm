package net.zutha.util

import scala.collection.mutable.Map

object Cache {

  def makeCache[IN,K,V](makeKey:IN=>K, makeVal:IN=>V) = {
    val items: Map[K,V] = Map()
    (in: IN) => {
      val key = makeKey(in)
      if (items.contains(key)) items(key)
      else {
        val item = makeVal(in)
        items.put(key, item)
        item
      }
    }
  }
}
