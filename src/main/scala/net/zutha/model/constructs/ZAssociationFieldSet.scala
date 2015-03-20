package net.zutha.model.constructs

import net.zutha.model.exceptions.SchemaViolationException

object ZAssociationFieldSet{
  def apply(parentItem: ZItem, assocFT:ZAssociationFieldType): ZAssociationFieldSet =
    ZAssociationFieldSet(parentItem,assocFT.role,assocFT.associationType)
  def apply(parentItem: ZItem, assocFST:ZAssociationFieldSetType): ZAssociationFieldSet =
    ZAssociationFieldSet(parentItem,assocFST.role,assocFST.associationType)
}

case class ZAssociationFieldSet(parentItem: ZItem, role: ZRole, associationType: ZAssociationType)
    extends ZFieldSet{

  def associationFieldSetType = {
    val declaringType = fieldType.declarerForItem(parentItem) match{
      case None => throw new SchemaViolationException(this + " has no non-overridden association-field-declarations")
      case Some(t) => t
    }
    ZAssociationFieldSetType(declaringType,role,associationType)
  }

  def fieldType = ZAssociationFieldType(role,associationType)
  def associationFields = parentItem.getAssociationFields(fieldType)
  def fields = associationFields.map(af => af)
  def otherAssociationFieldTypes: Set[ZAssociationFieldType] = fieldType.companionAssociationFieldTypes
  def otherRoles: Set[ZRole] = fieldType.otherRoles
  def propertyTypes: Set[ZAssociationPropertyType] = associationType.definedAssocProperties

  def isEmpty = associationFields.isEmpty
  def cardMin = associationFieldSetType.cardMin
  def cardMax = associationFieldSetType.cardMax
}
