package net.zutha.model.constructs

import net.zutha.model.exceptions.SchemaViolationException
import net.zutha.model.datatypes.{ZUnboundedNNI}
import ZUnboundedNNI.{Finite,Infinity}
import net.zutha.model.db.DB.db

case class ZAssociationFieldType(role:ZRole, associationType:ZAssociationType) extends ZFieldType{

  def name = associationType.nameF(role)
  
  def propertyTypes = associationType.definedAssocProperties
  
  def companionAssociationFieldTypes = otherRoles.map(r => ZAssociationFieldType(r,associationType))

  def companionAssociationFieldType(otherRole:ZRole) = {
    require(otherRoles contains otherRole)
    ZAssociationFieldType(otherRole,associationType)
  }
  /**
   * @return a Set containing the other Roles that can be played in this field's Association.
   * If this field's Role can be played more than once, that Role will be included in the result Set
   */
  def otherRoles: Set[ZRole] = {
    val allRoles = associationType.definedRoles
    associationType.getRoleCardMax(role) match {
      case Finite(0) => throw new SchemaViolationException("Association Field uses Role that is not allowed by Association Type")
      case Finite(1) => allRoles - role
      case Finite(_) => allRoles
      case Infinity => allRoles
    }
  }

  def allowedPlayers = declaringTypes.flatMap(_.allInstances) //TODO filter out players based on other relevant rules

  def allowedPlayersOf(role:ZRole): Set[ZItem] = {
    val otherAssocFieldType = companionAssociationFieldType(role)
    otherAssocFieldType.allowedPlayers
  }


  def declarationAssociations: Set[ZAssociation] = {
    db.findAssociations(db.ASSOCIATION_FIELD_DECLARATION,false,
      db.ROLE.toRole -> role, db.ASSOCIATION_TYPE.toRole -> associationType)
  }

  //TODO make a rootDeclaringType method

  /**
   * @return the types that declare associationFields of this type
   */
  def declaringTypes: Set[ZType] = {
    declarationAssociations.map(_.getPlayers(db.ASSOCIATION_FIELD_DECLARER).head.toType)
  }

  def declarersAmongTypes(types: Set[ZType]):Set[ZType] = {
    val nonOverriddenDeclarations = declarationAssociations.filter{decl =>
      val overridingDeclarers = decl.overriddenBy.map(_.getPlayers(db.ASSOCIATION_FIELD_DECLARER).head.toType)
      (overridingDeclarers intersect types).size == 0
    }
    val nonOverriddenDeclarers = nonOverriddenDeclarations.map(_.getPlayers(db.ASSOCIATION_FIELD_DECLARER).head.toType)
    nonOverriddenDeclarers intersect types
  }

  def declarerForType(zType: ZType): Option[ZType]={
    val declarers = declarersAmongTypes(zType.ancestors)
    declarers.size match{
      case 0 => None
      case 1 => Some(declarers.head)
      case _ => throw new SchemaViolationException(zType + " has more than one supertype with a non-overridden declaration of "+this)
    }
  }
  def declarerForItem(item: ZItem): Option[ZType]={
    val nonOverriddenDeclarers = declarersAmongTypes(item.allTypes)
    nonOverriddenDeclarers.size match{
      case 0 => None
      case 1 => Some(nonOverriddenDeclarers.head)
      case _ => throw new SchemaViolationException(item + " has more than one type with a non-overridden declaration of "+this)
    }
  }

}
