package net.zutha.model.datatypes

import javax.naming.directory.SchemaViolationException

object ZNonNegativeInteger extends DataType{
  def apply(value: String): ZNonNegativeInteger = try {
      val intVal = value.toInt
      apply(intVal)
    } catch {
      case _ => throw new SchemaViolationException("Invalid value: "+value+" for ZNonNegativeInteger property.")
    }
  def apply(value: Int): ZNonNegativeInteger = {
      if (value < 0) throw new SchemaViolationException("ZNonNegativeInteger properties cannot be negative.")
      else new ZNonNegativeInteger(value)
  }
  def unapply(value: String): Option[ZNonNegativeInteger] = try {
      Some(apply(value))
    } catch {
      case _ => None
    }
  def unapply(propValue: PropertyValue): Option[ZNonNegativeInteger] = propValue match {
    case v:ZNonNegativeInteger => Some(v)
    case _ => None
  }

  def default = ZNonNegativeInteger("0")

}
class ZNonNegativeInteger private (value: Int) extends ZUnboundedNNI.Finite(value) {

}
