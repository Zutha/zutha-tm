package net.zutha.model.constructs

import net.zutha.model.db.DB._
import net.zutha.model.exceptions.SchemaViolationException
import net.zutha.model.datatypes.{ZUnboundedNNI, ZNonNegativeInteger}


case class ZPropertySetType(definingType: ZType, propertyType: ZPropertyType)
    extends ZFieldSetType{

  //TODO find the only non-overridden declaration
  lazy val declarationAssociation = db.findAssociations(db.PROPERTY_DECLARATION,true,
      db.PROPERTY_DECLARER -> definingType,
      db.PROPERTY_TYPE.toRole -> propertyType
    ).headOption.getOrElse(
      throw new SchemaViolationException(this + "is missing a property-declaration"))

  def cardMin = declarationAssociation.getPropertyValue(db.PROPERTY_CARD_MIN).getOrElse(
    throw new SchemaViolationException("property-declaration associations must have a property-card-min property")) match {
    case value: ZNonNegativeInteger => value
    case _ => throw new SchemaViolationException("card-min properties must have datatype: ZNonNegativeInteger")
  }

  def cardMax = declarationAssociation.getPropertyValue(db.PROPERTY_CARD_MAX).getOrElse(
    throw new SchemaViolationException("property-declaration associations must have a property-card-max property")) match {
    case value: ZUnboundedNNI => value
    case _ => throw new SchemaViolationException("card-mx properties must have datatype: ZUnboundedNNI")
  }

}
