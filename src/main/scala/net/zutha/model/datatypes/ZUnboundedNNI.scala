package net.zutha.model.datatypes

import javax.naming.directory.SchemaViolationException

object ZUnboundedNNI extends DataType{
  def apply(value: String): ZUnboundedNNI = value match {
    case "*" => Infinity
    case value => {
      try {
        val intVal = value.toInt
        apply(intVal)
      } catch {
        case _ => throw new SchemaViolationException("Invalid value: "+value+" for ZUnboundedNNI property.")
      }
    }
  }
  def apply(value: Int): ZUnboundedNNI = {
      if (value < 0) throw new SchemaViolationException("ZUnboundedNNI properties cannot be negative.")
      else Finite(value)
  }
  def unapply(value: String): Option[ZUnboundedNNI] = try {
      Some(apply(value))
    } catch {
      case _ => None
    }
  def unapply(propValue: PropertyValue): Option[ZUnboundedNNI] = propValue match {
    case v@Finite(_) => Some(v)
    case Infinity => Some(Infinity)
    case _ => None
  }

  def default = Infinity

  object Infinity extends ZUnboundedNNI {
    def asString = "*"
    def compare(other: ZUnboundedNNI) = other match {
      case Infinity => 0
      case Finite(otherVal) => 1
    }
  }
  case class Finite(value: Int) extends ZUnboundedNNI {
    def asString = value.toString

    def compare(other: ZUnboundedNNI) = other match {
      case Infinity => -1
      case Finite(otherVal) => (value - otherVal).signum
    }
  }

  //implicit conversions
  implicit def intToUnboundedNNI(value:Int):ZUnboundedNNI = apply(value)
}

trait ZUnboundedNNI extends PropertyValue with Ordered[ZUnboundedNNI]

