package net.zutha.model.topicmap.constructs

import scala.collection.JavaConversions._
import net.zutha.model.topicmap.TMConversions._
import net.zutha.util.Cache._
import net.zutha.model.exceptions.SchemaViolationException
import net.zutha.model.datatypes.{ZNonNegativeInteger, ZUnboundedNNI}
import org.tmapi.core.{Topic}
import net.zutha.model.db.DB.db
import net.zutha.model.constructs.{ZPropertyType, ZAssociation, ZRole, ZAssociationType}
import net.zutha.model.datatypes.ZUnboundedNNI.Finite

object TMAssociationType{
  val getItem = makeCache[Topic,String,TMAssociationType](_.getId, topic => new TMAssociationType(topic))
  def apply(topic: Topic):TMAssociationType = getItem(topic)
}
class TMAssociationType protected (topic: Topic) extends TMTrait(topic) with ZAssociationType{
  def getAllSuperAssociationTypes: Set[ZAssociationType] = ancestors.filter(_.isAssociationType).map{_.toAssociationType}

  // --------- Role Players ------------

  lazy val getAssocRoleConstraints = {
    val allAssocRoleConstraints = getAllSuperAssociationTypes.flatMap{at =>
      db.findAssociations(db.ASSOCIATION_ROLE_CONSTRAINT,false,
      db.ASSOCIATION_TYPE.toRole -> at)}
    val nonOverridden = allAssocRoleConstraints.filter(_.overriddenBy.intersect(allAssocRoleConstraints).isEmpty)
    nonOverridden
  }
  def getAssocRoleConstraint(role: ZRole): ZAssociation = {
    //require(definedRoles.contains(role))
    val roleDeclaringAncestors = getAssocRoleConstraints.filter(_.getPlayers(db.ROLE.toRole).head.toRole == role)
    roleDeclaringAncestors.size match {
      case 0 => throw new SchemaViolationException("Association Type: '"+this.name+"' missing declaring ancestor for role: '"+role.name+"'")
      case 1 => roleDeclaringAncestors.toSeq.head
      case _ => throw new SchemaViolationException("role: "+role.name+" is declared more than once without being overridden")
    }
  }
  lazy val definedRoles = {
    val definedRoles = getAssocRoleConstraints.map(_.getPlayers(db.ROLE.toRole).head.toRole)
    definedRoles
  }

  def getRoleCardMin(role: ZRole) = getAssocRoleConstraint(role).getPropertyValue(db.ROLE_CARD_MIN).getOrElse(
    throw new SchemaViolationException("association-role-constraint associations must have a role-card-min property")) match {
    case value: ZNonNegativeInteger => value
    case _ => throw new SchemaViolationException("card-min properties must have datatype: ZNonNegativeInteger")
  }
  def getRoleCardMax(role: ZRole) =
    getAssocRoleConstraint(role).getPropertyValue(db.ROLE_CARD_MAX).getOrElse(
      throw new SchemaViolationException("association-role-constraint associations must have a role-card-max property")) match {
    case value: ZUnboundedNNI => value
    case _ => throw new SchemaViolationException("card-max properties must have datatype: ZUnboundedNNI")
  }

  // --------- Association Properties ------------

  def getAssocPropertyConstraints = {
    val allAssocPropertyConstraints = getAllSuperAssociationTypes.flatMap{at =>
      db.findAssociations(db.ASSOCIATION_PROPERTY_CONSTRAINT,false,
        db.ASSOCIATION_TYPE.toRole -> at)}
    val nonOverridden = allAssocPropertyConstraints.filter(_.overriddenBy.intersect(allAssocPropertyConstraints).isEmpty)
    nonOverridden
  }

  def getAssocPropertyConstraint(propType: ZPropertyType) = {
    val propDeclaringAncestors = getAssocPropertyConstraints.filter(_.getRoles(db.PROPERTY_TYPE).head.getPlayer.toRole == propType)
    propDeclaringAncestors.size match {
      case 0 => throw new SchemaViolationException("Association Type: '"+this.name+"' missing declaring ancestor for property: '"+propType.name+"'")
      case 1 => propDeclaringAncestors.toSeq.head
      case _ => throw new SchemaViolationException("property: "+propType.name+" is declared more than once without being overridden")
    }
  }

  def definedAssocProperties = {
    val definedProps = getAssocPropertyConstraints.map(_.getRoles(db.PROPERTY_TYPE).head.getPlayer.toAssocPropertyType)
    definedProps
  }

  def getAssocPropCardMin(propType: ZPropertyType) = getAssocPropertyConstraint(propType).getPropertyValue(db.PROPERTY_CARD_MIN).getOrElse(
    throw new SchemaViolationException("association-property-constraint associations must have a property-card-min property")) match {
    case value: ZNonNegativeInteger => value
    case _ => throw new SchemaViolationException("card-min properties must have datatype: ZNonNegativeInteger")
  }
  def getAssocPropCardMax(propType: ZPropertyType) =
    getAssocPropertyConstraint(propType).getPropertyValue(db.PROPERTY_CARD_MAX).getOrElse(
      throw new SchemaViolationException("association-property-constraint associations must have a property-card-max property")) match {
    case value: ZUnboundedNNI => value
    case _ => throw new SchemaViolationException("card-max properties must have datatype: ZUnboundedNNI")
  }

  def isBinary = {
    val noProps = definedAssocProperties.isEmpty
    val twoRoles = definedRoles.forall{r =>
      getRoleCardMin(r) == Finite(1) &&
      getRoleCardMax(r) == Finite(1)
    }
    noProps && twoRoles
  }
}

