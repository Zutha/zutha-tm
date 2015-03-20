package net.zutha.model.constructs

import net.zutha.model.datatypes.{PropertyValue, DataType}

trait ZProperty extends ZField{
  def toProperty: ZProperty;
  def zid: String
  def zids: Set[String]
  def propertyType: ZPropertyType;
  def valueString: String
  def value: PropertyValue
  def parent: ZItem
  def reifier: ZItem
  def scope: ZScope
  def dataTypeItem: ZItem
  def dataType: DataType
}
