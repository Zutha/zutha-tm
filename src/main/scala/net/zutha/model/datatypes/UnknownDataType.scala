package net.zutha.model.datatypes


object UnknownDataType extends DataType{
  def apply(value: String): UnknownDataType = new UnknownDataType(value)
  
  def unapply(value: String): Option[UnknownDataType] = Some(UnknownDataType(value))

  def unapply(propValue: PropertyValue): Option[UnknownDataType] = propValue match{
    case v:UnknownDataType => Some(v)
    case _ => None
  }

  def default = UnknownDataType("")
}

class UnknownDataType private (value: String) extends PropertyValue {
  def asString = value
}
