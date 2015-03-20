package net.zutha.model.constructs

import net.zutha.model.db.DB._
import net.zutha.model.exceptions.SchemaViolationException
import net.zutha.model.datatypes.{ZUnboundedNNI, ZNonNegativeInteger}

object ZAssociationFieldSetType{
  def apply(declaringType: ZType, assocFT:ZAssociationFieldType):ZAssociationFieldSetType =
    ZAssociationFieldSetType(declaringType,assocFT.role,assocFT.associationType)
}
case class ZAssociationFieldSetType(declaringType: ZType, role: ZRole, associationType: ZAssociationType)
    extends ZFieldSetType{

  def associationFieldType = ZAssociationFieldType(role,associationType)

  //TODO find the only non-overridden declaration
  lazy val declarationAssociation = db.findAssociations(db.ASSOCIATION_FIELD_DECLARATION,true,
      db.ASSOCIATION_FIELD_DECLARER -> declaringType,
      db.ROLE.toRole -> role,
      db.ASSOCIATION_TYPE.toRole -> associationType
    ).headOption.getOrElse(
      throw new SchemaViolationException(this + "is missing an association-field-declaration"))

  lazy val cardMin = declarationAssociation.getPropertyValue(db.ASSOCIATION_CARD_MIN).getOrElse(
    throw new SchemaViolationException("association-field-declaration associations must have an association-card-min property"))
  match {
    case value: ZNonNegativeInteger => value
    case _ => throw new SchemaViolationException("card-min properties must have datatype: ZNonNegativeInteger")
  }

  lazy val cardMax = declarationAssociation.getPropertyValue(db.ASSOCIATION_CARD_MAX).getOrElse(
    throw new SchemaViolationException("association-field-declaration associations must have an association-card-max property"))
  match {
    case value: ZUnboundedNNI => value
    case _ => throw new SchemaViolationException("card-mx properties must have datatype: ZUnboundedNNI")
  }
}
