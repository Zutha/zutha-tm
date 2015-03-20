package net.zutha.model.builder

import net.zutha.model.constructs._
import net.zutha.model.datatypes.ZUnboundedNNI._

class PropertySetBuilder(val parent: ItemBuilder, val fieldSetType: ZPropertySetType)
    extends FieldSetBuilder{
  val fieldType = fieldSetType.propertyType
  private var _properties: Set[PropertyBuilder] = Set()

  def properties = _properties
  def fields = properties.map(p => p)

  /**
   * @return Some(new Property) if a new one is allowed
   *  or None if this PropertySet is not allowed to have more members
   */
  def addProperty: Option[PropertyBuilder] = {
    if(_properties.size < fieldSetType.cardMax){
      val newBuilder = new PropertyBuilder(parent,fieldType)
      _properties += newBuilder
      Some(newBuilder)
    }
    else None

  }

  /**
   * Removes the given Property from this PropertySet
   * @returns true if it was removed successfully or if it was not found
   *  and false if this PropertySet is not allowed to have less members
   */
  def removeProperty(toRemove:PropertyBuilder):Boolean = {
    if(_properties.size > fieldSetType.cardMin){
      _properties -= toRemove
      true
    }
    else false
  }
}
