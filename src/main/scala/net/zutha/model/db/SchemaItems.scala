package net.zutha.model.db

import net.zutha.model.constructs._

//TODO use objects for every schema item

trait SchemaItems {
  protected def getSchemaItem(identifier: String): ZItem

  //kinds of item type
  lazy val TYPE: ZItemType = getSchemaItem("type").toItemType
  lazy val ITEM_TYPE: ZItemType = getSchemaItem("item-type").toItemType
  lazy val TRAIT: ZItemType = getSchemaItem("trait").toItemType

  //kinds of Trait
  lazy val CONSTRUCT_TYPE: ZType = getSchemaItem("construct-type").toType
  lazy val PROPERTY_TYPE: ZType = getSchemaItem("property-type").toType
  lazy val ASSOCIATION_PROPERTY_TYPE: ZType = getSchemaItem("association-property-type").toType
  lazy val ASSOCIATION_TYPE: ZType = getSchemaItem("association-type").toType

  //item types
  lazy val ITEM: ZItemType = getSchemaItem("item").toItemType
  lazy val ROLE: ZItemType = getSchemaItem("role").toItemType
  lazy val DATATYPE: ZItemType = getSchemaItem("datatype").toItemType

  //construct types
  lazy val REIFIED_ASSOCIATION: ZItem = getSchemaItem("reified-association")

  //association types
  lazy val TYPE_INSTANCE: ZAssociationType = getSchemaItem("type-instance").toAssociationType
  lazy val SUPERTYPE_SUBTYPE: ZAssociationType = getSchemaItem("supertype-subtype").toAssociationType
  lazy val ITEM_HAS_TRAIT: ZAssociationType = getSchemaItem("item-has-trait").toAssociationType
  lazy val ITEM_TYPE_TRAIT_DECLARATION: ZAssociationType = getSchemaItem("item-type-trait-declaration").toAssociationType
  lazy val FIELD_DECLARATION: ZAssociationType = getSchemaItem("field-declaration").toAssociationType
  lazy val PROPERTY_DECLARATION: ZAssociationType = getSchemaItem("property-declaration").toAssociationType
  lazy val ASSOCIATION_FIELD_DECLARATION: ZAssociationType = getSchemaItem("association-field-declaration").toAssociationType
  lazy val ASSOCIATION_ROLE_CONSTRAINT: ZAssociationType = getSchemaItem("association-role-constraint").toAssociationType
  lazy val ASSOCIATION_PROPERTY_CONSTRAINT: ZAssociationType = getSchemaItem("association-property-constraint").toAssociationType
  lazy val ABSTRACT_CONSTRAINT: ZAssociationType = getSchemaItem("abstract-constraint").toAssociationType
  lazy val PROPERTY_DATATYPE_CONSTRAINT: ZAssociationType = getSchemaItem("property-datatype-constraint").toAssociationType
  lazy val OVERRIDES_DECLARATION: ZAssociationType = getSchemaItem("overrides-declaration").toAssociationType

  //roles
  lazy val SUPERTYPE: ZRole = getSchemaItem("supertype").toRole
  lazy val SUBTYPE: ZRole = getSchemaItem("subtype").toRole
  lazy val INSTANCE: ZRole = getSchemaItem("instance").toRole
  lazy val FIELD_DECLARER: ZRole = getSchemaItem("field-declarer").toRole
  lazy val PROPERTY_DECLARER: ZRole = getSchemaItem("property-declarer").toRole
  lazy val ASSOCIATION_FIELD_DECLARER: ZRole = getSchemaItem("association-field-declarer").toRole
  lazy val OVERRIDING_DECLARATION: ZRole = getSchemaItem("overriding-declaration").toRole
  lazy val OVERRIDDEN_DECLARATION: ZRole = getSchemaItem("overridden-declaration").toRole

  //property types
  lazy val ROLE_CARD_MIN: ZPropertyType = getSchemaItem("role-card-min").toPropertyType
  lazy val ROLE_CARD_MAX: ZPropertyType = getSchemaItem("role-card-max").toPropertyType
  lazy val ASSOCIATION_CARD_MIN: ZPropertyType = getSchemaItem("association-card-min").toPropertyType
  lazy val ASSOCIATION_CARD_MAX: ZPropertyType = getSchemaItem("association-card-max").toPropertyType
  lazy val PROPERTY_CARD_MIN: ZPropertyType = getSchemaItem("property-card-min").toPropertyType
  lazy val PROPERTY_CARD_MAX: ZPropertyType = getSchemaItem("property-card-max").toPropertyType

  lazy val NAME: ZPropertyType = getSchemaItem("name").toPropertyType
  lazy val ZID: ZPropertyType= getSchemaItem("zid").toPropertyType

  //datatypes
  lazy val NON_NEGATIVE_INTEGER: ZItem = getSchemaItem("non-negative-integer")
  lazy val UNBOUNDED_NON_NEGATIVE_INTEGER: ZItem = getSchemaItem("unbounded-non-negative-integer")
  lazy val FIELD_LOCK: ZItem = getSchemaItem("field-lock")

}
