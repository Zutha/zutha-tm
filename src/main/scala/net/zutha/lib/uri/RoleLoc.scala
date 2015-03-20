package net.zutha.lib.uri

import net.liftweb.sitemap.Loc
import net.liftweb.util.Helpers
import xml.{NodeSeq, Text}
import net.zutha.model.constants.ApplicationConstants._
import net.liftweb.http.{RewriteResponse, ParsePath, RewriteRequest}
import net.liftweb.common.{Full, Empty}
import net.zutha.model.constructs.{ZAssociationFieldType, ZRole, ZAssociationType, ZItem}

object RoleInfo{
  def apply(item:ZItem, role:ZRole, assoc:ZAssociationType, otherRole:ZRole):RoleInfo = RoleInfo(item,role,assoc,otherRole,"")
}
case class RoleInfo(item:ZItem, role:ZRole, assocType:ZAssociationType, otherRole:ZRole, view: String){
  val assocFieldType = ZAssociationFieldType(role,assocType)
  val otherAssocFieldType = ZAssociationFieldType(otherRole,assocType)
}

object RoleLoc extends Loc[RoleInfo] {
  def name = "Role"

  /**
   * Generate a link based on the current page
   */
  val link = new Loc.Link[RoleInfo](List("role-views"),true){
    override def pathList(in: RoleInfo): List[String] ={
      val item = in.item
      val role = in.role
      val assoc = in.assocType
      val otherRole = in.otherRole
      val view = in.view
      val maybeView = if(view=="") Nil else (view+"."+HTML_EXT)::Nil
      val stem: List[String] = List("item",item.zid,UriName(item.name),role.zid,UriName(role.name),
        assoc.zid,UriName(assoc.nameF(role)),otherRole.zid,UriName(otherRole.name))
      stem++maybeView
    }
    override def createPath(value: RoleInfo): String =
      pathList(value).map(Helpers.urlEncode).mkString("/","/","")
  }

  val text = new Loc.LinkText(calcLinkText _)
  def calcLinkText(in: RoleInfo): NodeSeq =
    Text(in.item.name)

  def makeUri(item:ZItem, role:ZRole, assoc:ZAssociationType, otherRole:ZRole, view: String): String = link.createPath(RoleInfo(item,role,assoc,otherRole,view))
  def makeUri(item:ZItem, role:ZRole, assoc:ZAssociationType, otherRole:ZRole): String = makeUri(item,role,assoc,otherRole,"")

  def defaultValue = Empty

  def params = Nil

  override val rewrite: LocRewrite = Full({
    // item/<itemZID>/<itemName>/<roleZID>/<roleName>/<assocZID>/<assocName>/<otherRoleZID>/<otherRoleName>
    case RewriteRequest(ParsePath(
      List("item",ZIDLookup(itemFixed,item),itemName,ZIDLookup(roleFixed,ZRole(role)),roleName,
        ZIDLookup(assocFixed,ZAssociationType(assoc)),assocName,ZIDLookup(otherRoleFixed,ZRole(otherRole)),otherRoleName),
        suffix,absolute,endSlash),_,_)
      if(!itemFixed && itemName==UriName(item.name) && !roleFixed && roleName==UriName(role.name) &&
        !assocFixed && assocName==UriName(assoc.nameF(role)) && !otherRoleFixed && otherRoleName==UriName(otherRole.name) &&
        !endSlash && absolute && suffix=="")
    => (RewriteResponse("role-views"::DEFAULT_ROLE_VIEW::Nil), RoleInfo(item,role,assoc,otherRole))
    // item/<itemZID>/<itemName>/<roleZID>/<roleName>/<assocZID>/<assocName>/<otherRoleZID>/<otherRoleName>/<view>.html
    case RewriteRequest(ParsePath(
      List("item",ZIDLookup(itemFixed,item),itemName,ZIDLookup(roleFixed,ZRole(role)),roleName,
        ZIDLookup(assocFixed,ZAssociationType(assoc)),assocName,ZIDLookup(otherRoleFixed,ZRole(otherRole)),otherRoleName,view),
        suffix,absolute,endSlash),_,_)
      if(!itemFixed && itemName==UriName(item.name) && !roleFixed && roleName==UriName(role.name) &&
        !assocFixed && assocName==UriName(assoc.nameF(role)) && !otherRoleFixed && otherRoleName==UriName(otherRole.name) &&
        !endSlash && absolute && suffix==HTML_EXT)
    => (RewriteResponse("role-views"::view::Nil), RoleInfo(item,role,assoc,otherRole,view))

  })
}
