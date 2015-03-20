package net.zutha.model.builder

import net.zutha.model.constructs.{ZFieldSetType, ZFieldType}


trait FieldSetBuilder {
  def fieldType: ZFieldType
  def fieldSetType: ZFieldSetType
  def fields: Set[FieldBuilder]
}
