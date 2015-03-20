package net.zutha.snippet

import net.liftweb.util.Helpers._
import xml.{Text, NodeSeq}
import net.zutha.lib.uri.{RoleLoc, AssocInfo, ItemLoc}
import net.zutha.model.constructs.{ZAssociationFieldSet, ZAssociationField, ZItem}

class AssocPage(assocInfo: AssocInfo) {

  val item = assocInfo.item
  val assocType = assocInfo.assoc
  val role = assocInfo.role
  val assocFieldSet: ZAssociationFieldSet = item.getAssociationFieldSet(role,assocType) match {
    case Some(fs) => fs
    case None => throw new Exception("Requested Association Field Set does not exist.")
  }

  def title(content: NodeSeq): NodeSeq = {
    val viewStr = assocInfo.view match {
      case "" => ""
      case view => " :: " + view
    }
    Text("Zutha.net - " + item.name + " :: " + assocType.nameF(role) + viewStr)
  }

  def summary = SnippetUtils.itemSummary(item)

  def assocTableName = {
    ".role *" #> role.name &
    ".role [href]" #> ItemLoc.makeUri(role) &
    ".assoc-type *" #> assocType.nameF(role) &
    ".assoc-type [href]" #> ItemLoc.makeUri(assocType)
  }

  def assocTable = ".association-table" #> AssocTable.makeAssocSetTable(assocFieldSet)
  
}
