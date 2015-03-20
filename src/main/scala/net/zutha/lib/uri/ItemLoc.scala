package net.zutha.lib.uri

import net.liftweb.sitemap.Loc
import net.zutha.model.constructs.ZItem
import net.zutha.model.constants.ApplicationConstants._
import net.liftweb.common.{Full, Empty}
import xml.{NodeSeq, Text}
import net.liftweb.http.{RewriteResponse, ParsePath, RewriteRequest}
import net.liftweb.util.Helpers

object ItemInfo{
  def apply(item: ZItem):ItemInfo = ItemInfo(item,"")
}
case class ItemInfo(item: ZItem, view: String)

object ItemLoc extends Loc[ItemInfo]{
  def name = "Item"

  /**
   * Generate a link based on the current page
   */
  val link = new Loc.Link[ItemInfo](List("item-views"),true){
    override def pathList(in: ItemInfo): List[String] ={
      val item = in.item
      val view = in.view
      val maybeView = if(view=="") Nil else (view+"."+HTML_EXT)::Nil
      "item"::item.zid::UriName(item.name)::maybeView
    }
    override def createPath(value: ItemInfo): String =
      pathList(value).map(Helpers.urlEncode).mkString("/","/","")
  }

  val text = new Loc.LinkText(calcLinkText _)
  def calcLinkText(in: ItemInfo): NodeSeq =
    Text(in.item.name)

  def makeUri(item: ZItem, view: String): String = link.createPath(ItemInfo(item,view))
  def makeUri(item: ZItem): String = makeUri(item,"")

  def defaultValue = Empty

  def params = Nil

  override val rewrite: LocRewrite = Full({
    // item/<validZID>/<correctName>
    case RewriteRequest(ParsePath("item"::ZIDLookup(wasRepaired,item)::name::Nil,suffix,absolute,endSlash),_,_)
      if(name==UriName(item.name) && !wasRepaired && !endSlash && absolute && suffix=="") =>
      (RewriteResponse("item-views"::DEFAULT_ITEM_VIEW::Nil), ItemInfo(item))
    // item/<validZID>/<correctName>/<view>.html
    case RewriteRequest(ParsePath("item"::ZIDLookup(wasRepaired,item)::name::view::Nil,suffix,absolute,endSlash),_,_)
      if(name==UriName(item.name) && !wasRepaired && !endSlash && absolute && suffix==HTML_EXT) =>
      (RewriteResponse("item-views"::view::Nil), ItemInfo(item,view))

  })
}
