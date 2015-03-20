package net.zutha.model.datatypes

import net.zutha.model.exceptions.SchemaViolationException

object ZFieldLock extends DataType{
  def apply(value: String): ZFieldLock = value match {
    case "inherit" => Inherit
    case value => {
      try {
        val intVal = value.toInt
        apply(intVal)
      } catch {
        case _ => throw new SchemaViolationException("Invalid value: "+value+" for ZFieldLock property.")
      }
    }
  }
  def apply(value: Int): ZFieldLock = {
      if (value < 0) throw new SchemaViolationException("ZFieldLock properties cannot be negative.")
      else Specified(value)
  }
  def unapply(value: String): Option[ZFieldLock] = try {
      Some(apply(value))
    } catch {
      case _ => None
    }
  def unapply(propValue: PropertyValue): Option[ZFieldLock] = propValue match {
    case Inherit => Some(Inherit)
    case s@Specified(_) => Some(s)
    case _ => None
  }

  def default = ZFieldLock("")

  object Inherit extends ZFieldLock {
    def asString = "inherit"
  }
  case class Specified(value: Int) extends ZFieldLock {
    def asString = value.toString
  }
}

trait ZFieldLock extends PropertyValue

