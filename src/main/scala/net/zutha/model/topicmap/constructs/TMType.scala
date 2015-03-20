package net.zutha.model.topicmap.constructs

import scala.collection.JavaConversions._
import org.tmapi.core.Topic

import net.zutha.model.topicmap.TMConversions._
import net.zutha.model.db.DB.db
import net.zutha.util.Cache._
import net.zutha.model.constructs.{ZPropertySetType, ZAssociationFieldSetType, ZType}
import net.zutha.model.datatypes.ZUnboundedNNI.Finite

object TMType{
  val getItem = makeCache[Topic,String,TMType](_.getId, topic => new TMType(topic))
  def apply(topic: Topic):TMType = getItem(topic)
}
class TMType protected (topic: Topic) extends TMItem(topic) with ZType {
  def isAbstract: Boolean = {
    val abstractConstAssoc = topic.getRolesPlayed(db.TYPE,db.ABSTRACT_CONSTRAINT)
    !abstractConstAssoc.isEmpty
  }

  def hasAncestor(superType: ZType): Boolean = ancestors.contains(superType)

  lazy val ancestors: Set[ZType] = {
    val ancestors = db.ancestorsOfType(this) + this //make sure this item itself is included
    ancestors
  }
  def descendants: Set[ZType] = {
    val descendants = db.descendantsOfType(this) + this
    descendants
  }

  // --------------- defined fields ---------------
  lazy val declaresFields: Boolean = {
    val propertyDefRoles = topic.getRolesPlayed(db.PROPERTY_DECLARER,db.PROPERTY_DECLARATION)
    val assocDefRoles = topic.getRolesPlayed(db.ASSOCIATION_FIELD_DECLARER,db.ASSOCIATION_FIELD_DECLARATION)
    propertyDefRoles.size > 0 || assocDefRoles.size > 0
  }

  lazy val declaredPropertySets = {
    val definedPropTypes = db.traverseAssociation(topic,db.PROPERTY_DECLARER,db.PROPERTY_DECLARATION,
      db.PROPERTY_TYPE.toRole).map(_.toPropertyType)
    definedPropTypes.map(ZPropertySetType(this,_))
  }

  lazy val declaredAssociationFieldSets = {
    val declAssociations = db.findAssociations(db.ASSOCIATION_FIELD_DECLARATION,false,
      db.ASSOCIATION_FIELD_DECLARER -> this
    )
    declAssociations.map{declAssoc =>
      val role = declAssoc.getPlayers(db.ROLE.toRole).head.toRole
      val assocType = declAssoc.getPlayers(db.ASSOCIATION_TYPE.toRole).head.toAssociationType
      ZAssociationFieldSetType(this,role,assocType)
    }.toSet
  }

  lazy val allowedPropertySets: Set[ZPropertySetType] = {
    val fieldSets = ancestors.flatMap(_.declaredPropertySets)
    fieldSets
  }

  lazy val allowedAssociationFieldSets: Set[ZAssociationFieldSetType] = {
    val fieldSets = ancestors.flatMap(_.declaredAssociationFieldSets)
    fieldSets
  }

  def requiredPropertySets = allowedPropertySets.filter(_.cardMin >= Finite(1))

  def requiredAssociationFieldSets = allowedAssociationFieldSets.filter(_.cardMin >= Finite(1))
}
