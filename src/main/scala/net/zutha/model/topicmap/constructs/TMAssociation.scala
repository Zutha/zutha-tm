package net.zutha.model.topicmap.constructs

import scala.collection.JavaConversions._
import net.zutha.model.topicmap.TMConversions._
import org.tmapi.core.{Topic, Association, Role}
import net.zutha.util.Cache._
import net.zutha.model.db.DB.db
import net.zutha.model.datatypes.PropertyValue
import net.zutha.model.exceptions.SchemaViolationException
import net.zutha.model.constructs._

object TMAssociation{
  val getItem = makeCache[Association,String,TMAssociation](_.getId, association => new TMAssociation(association))
  def apply(association: Association):TMAssociation = getItem(association)
}
class TMAssociation protected (association: Association) extends ZAssociation{
  def toZAssociation: ZAssociation = this
  def toAssociation = association
  def zid = Option(reifier).getOrElse( throw new SchemaViolationException("reified association has no reifier") ).zid
  def zids = Option(reifier).map(_.zids).getOrElse(Set())
  def reifier = association.getReifier
  def associationType = TMAssociationType(association.getType)
  def hasType(zType: ZType) = associationType.hasAncestor(zType)
  def associationFields: Set[ZAssociationField] = association.getRoles.toSet.map(TMAssociationField(_:Role))
  def playedRoles = association.getRoleTypes.map(TMRole(_:Topic)).toSet
  def players = rolePlayers.map(_._2)
  def getPlayers(role: ZRole): Set[ZItem] = rolePlayers.filter(_._1 == role).map(_._2)
  lazy val rolePlayers: Set[(ZRole,ZItem)] = association.getRolePlayersT.map{case (r,p) => (r.toRole,p.toItem)}
  def associationProperties: Set[(ZPropertyType,PropertyValue)] = {
    associationType.definedAssocProperties.flatMap{pt => getPropertyValues(pt).map(pv => (pt,pv))}
  }

  def getProperties(propType: ZPropertyType) = reifier.getProperties(propType)
  def getPropertyValues(propType: ZPropertyType) = reifier.getPropertyValues(propType)
  def getProperty(propType: ZPropertyType) = reifier.getProperty(propType)
  def getPropertyValue(propType: ZPropertyType) = reifier.getPropertyValue(propType)

  def addProperty(propType: ZPropertyType, value: PropertyValue) = association.getReifier.createOccurrence(propType,value.toString)

  lazy val overriddenBy:Set[ZAssociation] = {
    val ovDeclRoles = association.getReifier.getRolesPlayed(db.OVERRIDDEN_DECLARATION,db.OVERRIDES_DECLARATION).toSet
    val overriders = ovDeclRoles.map(_.getParent.getRoles(db.OVERRIDING_DECLARATION).head.getPlayer
      .getReified.asInstanceOf[Association].toZAssociation)
    overriders
  }
}
