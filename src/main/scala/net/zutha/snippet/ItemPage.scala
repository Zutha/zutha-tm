package net.zutha.snippet

import net.liftweb.util.Helpers._
import xml.{Text, NodeSeq}
import net.zutha.lib.uri.ItemInfo
import net.zutha.model.constructs.ZItem


class ItemPage(itemInfo: ItemInfo) {

  private val item: ZItem = itemInfo.item

  def title(content: NodeSeq): NodeSeq = {
    val viewStr = itemInfo.view match {
      case "" => ""
      case view => " :: " + view
    }
    Text("Zutha.net - " + item.name + viewStr)
  }

  def summary = SnippetUtils.itemSummary(item)



}
