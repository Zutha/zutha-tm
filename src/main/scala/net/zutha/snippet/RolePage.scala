package net.zutha.snippet

import net.liftweb.util.Helpers._
import net.zutha.model.constructs.ZItem
import xml.{Text, NodeSeq}
import net.zutha.lib.uri.{ItemLoc, RoleInfo, ItemInfo}
import net.liftweb.util.ValueCell
import net.liftweb.http.WiringUI
import net.zutha.model.auth.CurrentUser

class RolePage(roleInfo: RoleInfo) {

  private val item = roleInfo.item
  private val assocType = roleInfo.assocType
  private val role = roleInfo.role
  private val otherRole = roleInfo.otherRole

  val rolePlayers = ValueCell[Set[ZItem]]{
    item.getAssociationFields(role,assocType).flatMap{_.getPlayers(otherRole)}
  }

  val createItemForm = if(CurrentUser.loggedIn){
    Some(new CreateItemForm(roleInfo,rolePlayers))
  } else None

  def title(content: NodeSeq): NodeSeq = {
    val viewStr = roleInfo.view match {
      case "" => ""
      case view => " :: " + view
    }
    Text("Zutha.net - " + item.name + " :: " + assocType.nameF(role)
      + " :: " + otherRole.name + viewStr)
  }

  def summary = SnippetUtils.itemSummary(item)

  def roleListName = {
    ".role *" #> role.name &
    ".role [href]" #> ItemLoc.makeUri(role) &
    ".assoc_type *" #> assocType.nameF(role) &
    ".assoc_type [href]" #> ItemLoc.makeUri(assocType) &
    ".other_role *" #> otherRole.name &
    ".other_role [href]" #> ItemLoc.makeUri(otherRole)
  }

  val rolePlayersSorted = rolePlayers.lift(_.toSeq.sortBy(_.name)) //TODO sort by worth

  private def renderRolePlayer(player: ZItem) = SnippetUtils.itemSummary(player,false)

  def renderRolePlayers = {//TODO use history and calculateDeltas
    val selStatic = ".role-player" #> rolePlayersSorted.get.map{rp => renderRolePlayer(rp)} //send initial html response with role-players rendered
    val selDynamic = selStatic & "#role-players" #> WiringUI.toNode(rolePlayersSorted){(rps,ns) =>
      val sel = ".role-player" #> rps.map{rp => renderRolePlayer(rp)}
      sel(ns)
    }
    if(CurrentUser.loggedIn) selStatic & selDynamic else selStatic //this was an attempt to make WiringUI.toNode send the transformed output in the initial request but it didn't work
  }

  def renderCreateItemForm = "*" #> createItemForm.map(_.render)
}
