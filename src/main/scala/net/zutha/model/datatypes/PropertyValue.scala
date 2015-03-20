package net.zutha.model.datatypes


trait PropertyValue {
  /** @return the canonical string value that would be resolved to this PropertyValue */
  def asString: String
}
