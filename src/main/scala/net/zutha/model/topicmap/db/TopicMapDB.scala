package net.zutha.model.topicmap.db

import scala.collection.JavaConversions._
import scala.collection.mutable.Map
import org.tmapi.core._
import org.tmapix.io.CTMTopicMapReader
import tools.nsc.io.{File}
import net.liftweb.common.{Loggable}

import net.zutha.model.constants._
import ZuthaConstants._
import ApplicationConstants._
import net.zutha.model.db.DB
import net.zutha.model.topicmap.TMConversions._
import org.tmapi.index.{Index, TypeInstanceIndex}
import de.topicmapslab.majortom.model.index.{ITransitiveTypeInstanceIndex, ISupertypeSubtypeIndex}
import net.zutha.model.topicmap.AmbiguityWorkarounds
import net.zutha.model.exceptions.{SchemaViolationException, SchemaItemMissingException}
import net.zutha.model.topicmap.constructs.TMItem
import de.topicmapslab.majortom.model.core.{IScope, ITopicMap, ITopicMapSystem}
import net.zutha.model.constructs._

class TopicMapDB extends DB with  MajortomDB with ZtmTopics with Loggable{
  val ENABLE_TRANSACTIONS = true

  val tmSystem: ITopicMapSystem = makeTopicMapSystem

  // Zutha Topic Map
  lazy val tm: ITopicMap = {
    Option(tmSystem.getTopicMap(ZUTHA_TOPIC_MAP_URI)) match {
      case Some(ztm) => ztm
      case None => {
        val newtm = tmSystem.createTopicMap(ZUTHA_TOPIC_MAP_URI)
        generateSchema()
        newtm
      }
    }
  }

  // ZID Ticker
  private lazy val zidTicker = new ZIDTicker(tm)
  def getNextZID: Zid = zidTicker.getNext

  def printTMLocators(){
    for(loc <- tmSystem.getLocators) {
      println (loc.getReference)
    }
  }

  def generateSchema(){
    val tmFile = File(TM_DATA_PATH + "tm.ctm")
    val zidTickerFile = File(TM_DATA_PATH + "zid-ticker.dat")

    //regenerate tm.ctm if needed
    val inputFileNames = List("schema_templates", "schema", "core","basic-entities","books","person","test-data")
    val inputFiles = inputFileNames.map{fn => File(TM_DATA_PATH + fn + ".ctm")}

    val changesMade = inputFiles.exists{f => f.lastModified > tmFile.lastModified}
    if( !tmFile.exists || changesMade ) {
      val lines = inputFiles.flatMap(_.chars.getLines)
      val namedZidPlaceholderMap = Map[String,String]()
      val matcher = """(%zid%)([\w]+%)?""".r
      val writer = tmFile.printWriter()

      //Replace %zid% markers with unique ZIDs
      //For markers of the form %zid%<tag>%,
      // replace all markers with the same tag with an identical ZID
      lines.foreach{line =>
        val newLine = matcher.replaceAllIn(line,{m =>
          def newZID = "zid:"+getNextZID.toString
          m.group(2) match {
            case tag:String => namedZidPlaceholderMap.getOrElseUpdate(tag,newZID)
            case _ => newZID //this is an anonymous ZID placeholder
          }
        })
        //        newLine foreach (writer write _)
        writer println newLine
      }
      writer.flush()
      writer.close

      //save the final value of the zid-ticker
      val lastUsedZID = zidTicker.getCurrentAsLong.toString
      zidTickerFile.writeAll(lastUsedZID)
    }

    //make a topic map from the ctm schema
    val source: java.io.File = tmFile.jfile
    val reader: CTMTopicMapReader = new CTMTopicMapReader(tm, source,ZUTHA_TOPIC_MAP_URI)
    reader.read()

    //set ZID Ticker start value
    val tickerValue = zidTickerFile.slurp().toLong
    zidTicker.setTickerValue(tickerValue)

    //redo all the supertype-subtype associations so that they are indexed
    val index = getIndex(classOf[TypeInstanceIndex])
    val akoAssociations = index.getAssociations(SUPERTYPE_SUBTYPE).toSeq
    for( assoc <- akoAssociations ){
      val subtype = assoc.getPlayersOfRoleT(SUBTYPE).head
      val supertype = assoc.getPlayersOfRoleT(SUPERTYPE).head
      subtype.addSupertype(supertype)
    }

    //create all the type-instance associations (majortom-redis omits them)
    for(t <- AmbiguityWorkarounds.getAllTopics(tm)){
      val types = t.getTypes.toSet
      val theType = types.headOption.getOrElse(
        throw new SchemaViolationException("All topics must have a type")
      )
      if(theType != ANONYMOUS_TOPIC){
        t.setType(theType.toType)
      }
    }

    logger.info("database has been reset back to schema")
  }

  /**
   * @param identifier the Zutha Identifier of the schema item to retrieve
   * @return the schema item with the given identifier
   * @throws SchemaItemMissingException if the requested topic does not exist
   */
  protected def getSchemaItem(identifier: String): ZItem = tm.lookupTopicBySI(ZSI_PREFIX + identifier) match {
    case Some(topic) => topic.toItem
    case None => throw new SchemaItemMissingException
  }

  protected def getOrCreateSchemaTopic(si: String): Topic = tm.getOrCreateTopicBySI(si)

  def getItemByZid(zid: Zid) = tm.lookupTopicByZID(zid).map{_.toItem}

  // create constructs

  def createItem(topicType: ZType): ZItem = {
    val zid = getNextZID
    val zidUri = ZID_PREFIX + zid
    val loc = tm.createLocator(zidUri)
    val item: TMItem = tm.createTopicBySubjectIdentifier(loc)
    item.setType(topicType)
    item
  }

  def createItem(itemType: ZItemType, name: String): ZItem = {
    val item = createItem(itemType)
    val nameObject = item.createName(NAME, name)
    val nameReifier = createItem(NAME.toItemType)
    nameObject.setReifier(nameReifier)
    item
  }

  def createRawAssociation(assocType: Topic, rolePlayers: (Topic, Topic)*) = {
    val assoc = tm.createAssociation(assocType)
    for((r,p) <- rolePlayers){
      assoc.createRole(r,p)
    }
    assoc
  }

  def createAssociation(assocType: ZAssociationType, rolePlayers: (ZRole, ZItem)*) = {
    val rolePlayerTopics: Seq[(Topic,Topic)] = rolePlayers.map{case (r,i) => (r,i): (Topic,Topic)}
    val assoc = createRawAssociation(assocType, rolePlayerTopics:_*)
    val assocReifier = createItem(assocType.toItemType)
    assoc.setReifier(assocReifier)
    assoc
  }

  def createRawScope(topics: Topic*): IScope = {
    tm.createScope(topics.toSeq)
  }

  //*********************************************************
  //*********************************************************
  //***************      DB Access methods      *************
  //*********************************************************
  //*********************************************************

  // ------------ Internal Methods -------------

  private def getIndex[T <: Index](clazz: Class[T]) = {
    val index = tm.getStore.getIndex(clazz)
    if (!index.isOpen()) {
      index.open();
    }
    index
  }

  private def topicsToItems(topics: Set[Topic]): Set[ZItem] = {
    topics.filterNot(_.isAnonymous).map(_.toItem)
  }

  private def rawAncestorsOfType(topic: Topic) = {
    val supertypes = topic.getSupertypes.toSet
    supertypes + topic
  }

  private def rawDescendentsOfType(topic: Topic) = {
    val index = getIndex(classOf[ISupertypeSubtypeIndex])
    val subtypes = index.getSubtypes(topic).toSet
    subtypes
  }

  private def rawAllInstancesOfType(topic: Topic): Set[Topic] = {
    val index = getIndex(classOf[ITransitiveTypeInstanceIndex])
    val instances = AmbiguityWorkarounds.getTopics(index,topic).toSet
    instances
  }

  // ------------ Exported Methods -------------

  def directTypesOfItem(item: ZItem): Set[ZType] = {
    val rawTypes = item.getTypes.toSet
    val zTypes = topicsToItems(rawTypes)
    zTypes.map(_.toType)
  }

  def ancestorsOfType(zType: ZType): Set[ZType] = {
    val supertypes = rawAncestorsOfType(zType).map(_.toType)
    supertypes
  }

  def allTypesOfItem(item: ZItem): Set[ZType] = {
    val directTypes = item.getTypes.toSet
    val allTypesRaw = directTypes.flatMap(rawAncestorsOfType(_))
    val allTypes = topicsToItems(allTypesRaw).map(_.toType)
    allTypes
  }

  def itemIsA(item: ZItem, zType: ZType): Boolean = {
    allTypesOfItem(item).contains(zType)
  }

  def allInstancesOfType(zType: ZType): Set[ZItem] = {
    val instancesRaw = rawAllInstancesOfType(zType)
    val instances = topicsToItems(instancesRaw)
    instances
  }

  def allItems: Set[ZItem] = allInstancesOfType(ITEM)

  def descendantsOfType(zType: ZType): Set[ZType] = {
    val rawDesc = rawDescendentsOfType(zType)
    val descendents = topicsToItems(rawDesc).map(_.toType)
    descendents
  }

  /** get all associations of type assocType with the given (role,player) pairs
   *  @param assocType matched associations must have this type (transitive)
   *  @param strict If true, matched associations must have exactly the set of rolePlayers given.
   *    If false, matched associations  must have at least the set of rolePlayers given
   *  @param rolePlayers a set of (Role,Player) pairs that matched associations must contain
   **/
  def findAssociations(assocType: ZAssociationType, strict: Boolean, rolePlayers:(ZRole,ZItem)*): Set[ZAssociation] = {
    val requiredRolePlayers = rolePlayers.toSet
    val candidateAssocSets = for {
      (r,p) <- requiredRolePlayers
    } yield {
      p.getRolesPlayed(r,assocType).map(_.getParent)
    }
    val intersectingAssocSets: Set[Association] = candidateAssocSets.reduceLeft(_ intersect _).toSet
    val results = if(strict){
      intersectingAssocSets.collect{
        case a if(requiredRolePlayers == a.rolePlayers) => a.toZAssociation
      }
    } else intersectingAssocSets.map(_.toZAssociation)

    results

//    val typeIndex = getIndex(classOf[TypeInstanceIndex])
//    val allAssocRaw = typeIndex.getAssociations(assocType: Topic).toSet
//    val allAssoc = allAssocRaw.map((a:Association) => a.toZAssociation)
//    val results = allAssoc.filter{assoc =>
//      val assocRolePlayers = assoc.rolePlayers
//      if(strict) requiredRolePlayers == assocRolePlayers
//      else requiredRolePlayers.forall(assocRolePlayers contains)
//    }
  }

  def traverseAssociation(item: ZItem,
                          role: ZRole,
                          assocType: ZAssociationType,
                          otherRole: ZRole): Set[ZItem] = {
    val startTopic: Topic = item
    val rolesPlayed = startTopic.getRolesPlayed(role, assocType).toSet
    val otherPlayers = rolesPlayed.flatMap{r =>
      r.getParent.getPlayersOfRoleT(otherRole).map(_.toItem)
    }
    otherPlayers
  }

  def allAssociations : Set[ZAssociation] =
    for (assoc <- tm.asInstanceOf[TopicMap].getAssociations.toSet if !assoc.isAnonymous)
    yield assoc.toZAssociation


  //Topic-Map specific methods

  /** check if this Topic is an Anonymous Topic which doesn't exist in the ZDM
   *  @param topic
   *  @return true if this topic is an AnonymousTopic
   */
  def topicIsAnonymous(topic: Topic): Boolean = {
    topic.getTypes.toSet.contains(ANONYMOUS_TOPIC)
  }

  /** check if this Association is has a player which is an Anonymous Topic
   *  @param association
   *  @return true if this association is anonymous
   */
  def associationIsAnonymous(association: Association): Boolean = {
    val players = association.getRoles.map(_.getPlayer).toSet
    val isAnon = players.exists(_.isAnonymous)
    isAnon
  }
}
