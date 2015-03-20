package net.zutha.lib.uri

import net.liftweb.sitemap.Loc
import net.zutha.model.constructs.{ZRole, ZAssociationType, ZItem}
import net.liftweb.util.Helpers
import xml.{NodeSeq, Text}
import net.zutha.model.constants.ApplicationConstants._
import net.liftweb.http.{RewriteResponse, ParsePath, RewriteRequest}
import net.liftweb.common.{Full, Empty}

object AssocInfo{
  def apply(item: ZItem, role: ZRole, assoc: ZAssociationType):AssocInfo = AssocInfo(item,role,assoc,"")
}
case class AssocInfo(item: ZItem, role: ZRole, assoc: ZAssociationType, view: String)

object AssocLoc extends Loc[AssocInfo] {
  def name = "Assoc"

  /**
   * Generate a link based on the current page
   */
  val link = new Loc.Link[AssocInfo](List("assoc-views"),true){
    override def pathList(in: AssocInfo): List[String] ={
      val item = in.item
      val role = in.role
      val assoc = in.assoc
      val view = in.view
      val maybeView = if(view=="") Nil else (view+"."+HTML_EXT)::Nil
      val stem: List[String] = List("item",item.zid,UriName(item.name),role.zid,UriName(role.name),
        assoc.zid,UriName(assoc.nameF(role)))
      stem++maybeView
    }
    override def createPath(value: AssocInfo): String =
      pathList(value).map(Helpers.urlEncode).mkString("/","/","")
  }

  val text = new Loc.LinkText(calcLinkText _)
  def calcLinkText(in: AssocInfo): NodeSeq =
    Text(in.item.name)

  def makeUri(item: ZItem, role: ZRole, assoc: ZAssociationType, view: String): String = link.createPath(AssocInfo(item,role,assoc,view))
  def makeUri(item: ZItem, role: ZRole, assoc: ZAssociationType): String = makeUri(item,role,assoc,"")

  def defaultValue = Empty

  def params = Nil

  override val rewrite: LocRewrite = Full({
    // item/<itemZID>/<itemName>/<roleZID>/<roleName>/<assocZID>/<assocName>
    case RewriteRequest(ParsePath(
      List("item",ZIDLookup(itemFixed,item),itemName,ZIDLookup(roleFixed,ZRole(role)),roleName,
        ZIDLookup(assocFixed,ZAssociationType(assoc)),assocName),
    suffix,absolute,endSlash),_,_)
      if(!itemFixed && itemName==UriName(item.name) && !roleFixed && roleName==UriName(role.name) &&
        !assocFixed && assocName==UriName(assoc.nameF(role)) &&
        !endSlash && absolute && suffix=="")
    => item.getAssociationFieldSet(role,assoc) match { //TODO return AssociationFieldSet in AssocInfo
      case Some(afs) => (RewriteResponse("assoc-views"::DEFAULT_ASSOC_VIEW::Nil), AssocInfo(item,role,assoc))
      case None => throw new Exception("Requested Association Field Set does not exist.")
    }
    // item/<itemZID>/<itemName>/<roleZID>/<roleName>/<assocZID>/<assocName>/<view>.html
    case RewriteRequest(ParsePath(
      List("item",ZIDLookup(itemFixed,item),itemName,ZIDLookup(roleFixed,ZRole(role)),roleName,
        ZIDLookup(assocFixed,ZAssociationType(assoc)),assocName,view),
    suffix,absolute,endSlash),_,_)
      if(!itemFixed && itemName==UriName(item.name) && !roleFixed && roleName==UriName(role.name) &&
        !assocFixed && assocName==UriName(assoc.nameF(role)) &&
        !endSlash && absolute && suffix==HTML_EXT)
    => (RewriteResponse("assoc-views"::view::Nil), AssocInfo(item,role,assoc,view))

  })
}
