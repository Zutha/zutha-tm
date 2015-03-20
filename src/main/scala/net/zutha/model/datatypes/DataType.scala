package net.zutha.model.datatypes

import net.zutha.model.db.DB.db
import net.zutha.model.constructs.ZItem

object DataType{
  private val dataTypeMap = Map[ZItem,DataType](
    db.NON_NEGATIVE_INTEGER -> ZNonNegativeInteger,
    db.UNBOUNDED_NON_NEGATIVE_INTEGER -> ZUnboundedNNI,
    db.FIELD_LOCK -> ZFieldLock
  )
  
  def apply(dataTypeItem: ZItem): DataType = {
    dataTypeMap.getOrElse(dataTypeItem,UnknownDataType)
  }
}
trait DataType {
  /** Constructor for PropertyValue of this DataType */
  def apply(value: String): PropertyValue
  def unapply(value: String): Option[PropertyValue]
  def unapply(propValue: PropertyValue): Option[PropertyValue]
  def validate(value: String): Boolean = unapply(value) match {
    case Some(_) => true
    case None => false
  }
  def default: PropertyValue
}
