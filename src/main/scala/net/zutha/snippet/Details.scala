package net.zutha
package snippet

import scala.xml.{NodeSeq}
import net.liftweb.util.Helpers._
import model.constructs._
import lib.uri.{ItemLoc, AssocLoc, RoleLoc, ItemInfo}
import net.liftweb.common.Logger
import model.datatypes.ZUnboundedNNI.Finite

class Details(itemInfo: ItemInfo) extends Logger{

  private val item: ZItem = itemInfo.item
  private val propSets = item.getNonEmptyPropertySetsGrouped
  private val assocFieldSets = item.getNonEmptyAssociationFieldSetsGrouped
  private val fieldDefiningTypes = propSets.keySet union assocFieldSets.keySet
  
  def render: NodeSeq => NodeSeq = {
    ".field-group *" #> fieldDefiningTypes.toSeq.sortBy(_.zid).map(makeFieldGroup(_))
  }

  /** Render the set of fields that were defined by definingType */
  private def makeFieldGroup(definingType: ZType): NodeSeq => NodeSeq = {
    val definedAssocFields: Seq[ZAssociationFieldSet] = assocFieldSets.getOrElse(definingType,Set.empty)
      .toSeq.sortBy(_.associationType.zid)
    val simpleAssocFields = definedAssocFields.filter(af => af.otherRoles.size == 1 && af.propertyTypes.size == 0)
    val complexAssocFields = definedAssocFields.filter(af => af.otherRoles.size > 1 || af.propertyTypes.size > 0)
    val definedProps: Seq[ZPropertySet] = propSets.getOrElse(definingType,Set.empty)
      .toSeq.sortBy(_.propertyType.zid)

    ".field-group-name *" #> (definingType.name) &
    ".simple-field" #> {definedProps.map(makePropertySet(_)) ++ simpleAssocFields.map(makePlayerList(_))} &
    ".complex-field" #> complexAssocFields.map(makeAssocSetTable(_))
  }

  /** Render all properties in a Property Set*/
  def makePropertySet(propSet: ZPropertySet) = {
    def makeProperty(prop: ZProperty) = {
      ".value *" #> prop.valueString
    }
    ".field-type *" #> propSet.propertyType.name &
    ".field-list *" #> propSet.properties.map(makeProperty(_)) &
    ".more-link" #> List()
  }

  /** Render a list of the other players in a binary association field */
  def makePlayerList(assocFieldSet: ZAssociationFieldSet) = {
    val role = assocFieldSet.role
    val assocType = assocFieldSet.associationType
    val otherRole = assocFieldSet.otherRoles.head
    val otherPlayers = assocFieldSet.associationFields.flatMap(_.getPlayers(otherRole))

    def renderRolePlayer(player: ZItem) = {
      "a *" #> player.name &
      "a [href]" #> ItemLoc.makeUri(player)
    }

    def isSingleton = {
      val singletonField = assocFieldSet.cardMax == Finite(1)
      val binaryField = assocFieldSet.associationType.isBinary
      singletonField && binaryField
    }
    
    ".field-type *" #> assocFieldSet.associationType.nameF(assocFieldSet.role) &
    ".field-list" #> {
      ".value" #> otherPlayers.map(renderRolePlayer(_))} &
    ".more-link" #> {
      if(isSingleton)
        "*" #> List()
      else
        ".more-link [href]" #> RoleLoc.makeUri(item,role,assocType,otherRole) &
        ".other-role-plural" #> otherRole.nameF(role) //TODO get name with plural scope
    }
  }

  /** Render a table to display all association Fields in assocFieldSet*/
  def makeAssocSetTable(assocFieldSet: ZAssociationFieldSet) = {
    val role = assocFieldSet.role
    val assocType = assocFieldSet.associationType

    ".field-type *" #> assocFieldSet.associationType.nameF(role) &
    ".association-table" #> AssocTable.makeAssocSetTable(assocFieldSet) &
    ".more-link [href]" #> AssocLoc.makeUri(item,role,assocType)
  }

} //end of class
