package net.zutha.model.builder

import net.zutha.model.constructs._
import net.liftweb.common.{Full, Empty, Box}


class AssociationFieldSetBuilder(val parent: ItemBuilder, val fieldSetType:ZAssociationFieldSetType)
    extends FieldSetBuilder{
  val fieldType = fieldSetType.associationFieldType
  private var _associationFields: Set[AssociationFieldBuilder] = Set()

  val role = fieldType.role
  val associationType = fieldType.associationType
  val otherRoles = fieldType.otherRoles
  val propertyTypes = fieldType.propertyTypes
  
  def associationFields = _associationFields
  def fields = associationFields.map(af => af)

  /**
   * @return Some(new assocField) if a new one is allowed
   *  or None if this AssocFieldSet is not allowed to have more members
   */
  def addAssociationField: Box[AssociationFieldBuilder] = {
    if(_associationFields.size < fieldSetType.cardMax){
      val newBuilder = new AssociationFieldBuilder(parent,fieldType)
      _associationFields += newBuilder
      Full(newBuilder)
    }
    else Empty
  }
  /**
   * Removes the given AssocField from this AssocFieldSet
   * @returns true if it was removed successfully or if it was not found
   *  and false if this AssocFieldSet is not allowed to have less members
   */
  def removeAssociationField(toRemove:AssociationFieldBuilder):Boolean = {
    if(_associationFields.size > fieldSetType.cardMin){
      _associationFields -= toRemove
      true
    }
    else false
  }
}
