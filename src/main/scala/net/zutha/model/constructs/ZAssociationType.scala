package net.zutha.model.constructs

import net.zutha.model.datatypes.{ZNonNegativeInteger, ZUnboundedNNI}

object ZAssociationType{
  def apply(item: ZItem): ZAssociationType = item.toAssociationType
  def unapply(item: ZItem): Option[ZAssociationType] =
    if(item.isAssociationType) Some(item.toAssociationType) else None
}
trait ZAssociationType extends ZType{
  def getAllSuperAssociationTypes: Set[ZAssociationType]

  def definedRoles: Set[ZRole]
  def getRoleCardMin(role: ZRole): ZNonNegativeInteger
  def getRoleCardMax(role: ZRole): ZUnboundedNNI

  def definedAssocProperties: Set[ZAssociationPropertyType]
  def getAssocPropCardMin(propType: ZPropertyType): ZNonNegativeInteger
  def getAssocPropCardMax(propType: ZPropertyType): ZUnboundedNNI

  def isBinary: Boolean
}
