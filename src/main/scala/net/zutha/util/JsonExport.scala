package net.zutha.util

import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import net.zutha.model.constructs.{ZAssociation, ZProperty, ZItem}
import net.zutha.model.constants.ApplicationConstants._
import tools.nsc.io.File
import net.zutha.model.db.DB._

object JsonExportApp extends App {
  JsonExport.export
}

object JsonExport {
  def itemToJson(item: ZItem): JValue = {
    val props = item.getAllProperties
    val json =
      ("zids" -> item.zids.toSeq.sorted) ~
      ("type" -> item.itemType.zid) ~
      ("properties" -> props.map( propToJson ))

    json
  }

  def propToJson(prop: ZProperty): JValue = {
    ("zids" -> prop.zids.toSeq.sorted) ~
    ("type" -> prop.propertyType.zid) ~
    ("value" -> prop.valueString) ~
    ("scope" -> prop.scope.scopeItems.toSeq.map(_.zid).sorted)
  }

  def assocToJson(assoc: ZAssociation): JValue = {
    val json =
      ("zids" -> assoc.zids.toSeq.sorted) ~
      ("type" -> assoc.associationType.zid) ~
      ("roleplayers" -> assoc.playedRoles.map{role =>
        ("role" -> role.zid) ~
        ("players" -> assoc.getPlayers(role).toSeq.map(_.zid).sorted)
      }) ~
      ("properties" -> assoc.associationProperties.map{ case (propType, propVal) =>
        ("type" -> propType.zid) ~
        ("value" -> propVal.asString)
      })

    json
  }

  def export {

    val everyItem = db.allItems
    val items = everyItem filterNot { _.hasType( db.REIFIED_ASSOCIATION.toType ) }
    val everyAssoc = db.allAssociations
    val assocs = everyAssoc filter { _.hasType( db.REIFIED_ASSOCIATION.toType ) }

    val itemJson = items.toSeq.sortBy(_.zid) map itemToJson
    val assocJson = assocs.toSeq.sortBy(_.zid) map assocToJson
    val json: JValue =
      ("items" -> itemJson) ~
        ("associations" -> assocJson)

    val jsonText =  pretty( render( json ) )
    val jsonFile = File(JSON_DATA_PATH + "schema.json")
    val writer = jsonFile.printWriter()
    writer print jsonText
    writer.flush()
    writer.close()
  }

}
