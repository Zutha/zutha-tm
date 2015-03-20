package net.zutha.model.builder

import net.zutha.model.constructs._
import net.zutha.model.db.DB.db
import net.liftweb.http.S
import net.liftweb.common.{Failure, Full, Empty, Box}

/** used for constructing a new Item starting from a single constraint
 *  in the form of an associationFieldType with at least one other rolePlayer specified
 */
class ItemBuilder(val requiredAssociationFieldType: ZAssociationFieldType,
                   otherRolePlayer:(ZRole,ZItem), additionalRolePlayers:(ZRole,ZItem)*) {

  //TODO if requiredAssocFieldType is subtype:has-direct-supertype this affects allowed types
  //TODO itemType effects allowed supertypes (because of type-has-root-item-constraint

  private var _itemName: String = ""

  /** true if the requiredAssociationFieldType is neither type-instance or item-has-trait */
  private var requiredFieldIsMisc = false

  private var availableItemTypes: Set[ZItemType] = Set()
  private var availableTraits: Set[ZTrait] = Set()
  private var _allowedTraits: Set[ZTrait] = Set()
  private var _selectedItemType: Box[ZItemType] = Empty
  private var _selectedTrait: Box[ZTrait] = Empty

  private var itemTypePropertySets: Set[PropertySetBuilder] = Set()
  private var traitPropertySets: Set[PropertySetBuilder] = Set()
  private var itemTypeAssociationFieldSets: Set[AssociationFieldSetBuilder] = Set()
  private var traitAssociationFieldSets: Set[AssociationFieldSetBuilder] = Set()

  //calculate the allowed ItemTypes and Traits based on the requiredAssociationFieldType
  requiredAssociationFieldType match {
    //If the requiredAssociationFieldType is instance:has-direct-type then require the specified ItemType
    case aft if aft == ZAssociationFieldType(db.INSTANCE,db.TYPE_INSTANCE) => { //item-type specified
      if(additionalRolePlayers.size > 0) throw new IllegalArgumentException("type-instance only has 2 roles")
      val specifiedItemType = otherRolePlayer match{
        case (role,player) if role==db.TYPE.toRole && player.isItemType => player.toItemType
        case (role,player) => throw new IllegalArgumentException(
          "other RolePlayer cannot be ("+role.name+","+player.name+") in an instance:has-type association-field")
      }
      availableItemTypes = Set(specifiedItemType)
      availableTraits = Set() //included for clarity as availableTraits is already empty
      selectedItemType = specifiedItemType //triggers creation of field sets
    }
    //If the requiredAssociationFieldType is item:has-trait then require the specified Trait
    case aft if aft == ZAssociationFieldType(db.ITEM.toRole,db.ITEM_HAS_TRAIT) => {
      if(additionalRolePlayers.size > 0) throw new IllegalArgumentException("item-has-trait only has 2 roles")
      val specifiedTrait = otherRolePlayer match{
        case (role,player) if role==db.TRAIT.toRole && player.isTrait => player.toTrait
        case (role,player) => throw new IllegalArgumentException(
          "other RolePlayer cannot be ("+role.name+","+player.name+") in an item:has-trait association-field")
      }
      availableTraits = Set(specifiedTrait)
      _allowedTraits = availableTraits
      selectedTrait = specifiedTrait //triggers creation of field sets
      availableItemTypes = specifiedTrait.compatibleItemTypes
      //TODO default SelectedItemType should be the one with max worth (or perhaps usage)
      selectedItemType = availableItemTypes.maxBy(_.zid) //triggers creation of field sets
    }
    //If the requiredAssociationFieldType is something else then find the traits and itemTypes that allow it
    case assocFieldType => {
      requiredFieldIsMisc = true
      val declaringTypes = assocFieldType.declaringTypes
      val availableTypes = declaringTypes.flatMap(_.descendants)
      availableTraits = availableTypes.filter(_.isTrait).map(_.toTrait)
      val allowingItemTypes = availableTypes.filter(_.isItemType).map(_.toItemType)
      availableItemTypes = allowingItemTypes ++ availableTraits.flatMap(_.compatibleItemTypes)
      selectedItemType = availableItemTypes.maxBy(_.zid)
    }
  }

  // --------------- Getters -------------------
  def itemName = _itemName
  def allowedItemTypes = availableItemTypes
  def allowedTraits = _allowedTraits
  def selectedItemType = _selectedItemType.open_!
  def selectedTrait = _selectedTrait
  def propertySets = itemTypePropertySets ++ traitPropertySets
  def associationFieldSets = itemTypeAssociationFieldSets ++ traitAssociationFieldSets
  def fieldSets: Set[FieldSetBuilder] = propertySets ++ associationFieldSets

  // --------------- Setters --------------------
  def itemName_= (name: String) {
    //TODO check for uniqueness of proposed name
    _itemName = name
  }

  /** Set the selectedItemType and update fieldSets accordingly
   * if requiredAssociationFieldType is type-instance this function should only be run once, at initialization
   */
  def selectedItemType_= (itemType: ZItemType) {
    //throw an exception if the selectedItemType is not allowed
    if (! allowedItemTypes.contains(itemType))
        throw new IllegalArgumentException(itemType.name + " is not one of the allowed Item Types")

    //nothing needs to be done unless the selectedItemType has changed
    if(_selectedItemType != Full(itemType)) {
      _selectedItemType = Full(itemType)

      //recalculate Field Sets required by itemType
      itemTypePropertySets = itemType.requiredPropertySets.map(new PropertySetBuilder(this,_))
      itemTypeAssociationFieldSets = for{
        assocField <- itemType.requiredAssociationFieldSets
        if(assocField.associationFieldType != ZAssociationFieldType(db.INSTANCE,db.TYPE_INSTANCE)) //remove type-instance field
      } yield new AssociationFieldSetBuilder(this,assocField)

      if(requiredFieldIsMisc){ //we need to make sure we have the requiredAssocField
        itemTypeAssociationFieldSets.find(afsb =>
            afsb.fieldSetType.associationFieldType == requiredAssociationFieldType) match{
          case None => { //need to create the requiredAssocFieldSet
              val requiredAssocFTDeclarer = requiredAssociationFieldType.declarerForType(itemType)
              requiredAssocFTDeclarer match {
                case Some(declarer) =>
                  //if the requiredAssociationFieldType is miscellaneous, allowed by itemType
                  // and not among itemTypeAssociationFieldSets then add it
                  itemTypeAssociationFieldSets += makeRequiredAssocFieldSet(declarer)
                  _allowedTraits = Set()
                case None =>
                  //if the requiredAssociationFieldType is not provided by itemType
                  // then it must be provided by at least one of the traits this itemType allows,
                  // so set the allowedTraits to the set of Traits that are allowed by itemType and which
                  // allow the requiredAssociationFieldType
                  _allowedTraits = itemType.compatibleTraits intersect availableTraits
                  if (_allowedTraits.size == 1) {
                    selectedTrait = _allowedTraits.head
                  }
                  else {
                    _selectedTrait = Empty; updateTraitDefinedFields
                  }
              }
            }
          case Some(afsb) => makeRequiredAssocField(afsb)
        }
      }
    }
  }

   /** Set the selectedTrait and update fieldSets accordingly
    * If requiredAssociationFieldType is type-instance, this function should never be called.
    * If requiredAssociationFieldType is item-has-trait it should only be run once at initialization.
    * If requiredAssociationFieldType is misc. then it should only be called when the assocFieldType
    *   is not provided by the selectedItemType. Otherwise allowedTraits will be empty.
    */
  def selectedTrait_= (selectedTrait: ZTrait) {
    //throw an exception if the selectedTrait is not allowed
    if (! allowedTraits.contains(selectedTrait))
        throw new IllegalArgumentException(selectedTrait.name + " is not one of the allowed Traits")

    if(_selectedTrait != Some(selectedTrait)) {
      _selectedTrait = Some(selectedTrait)
      updateTraitDefinedFields

      //need to create the requiredAssociationField in appropriate field set of the selectedTrait
      if(requiredFieldIsMisc){
        traitAssociationFieldSets.find(afsb =>
            afsb.fieldSetType.associationFieldType == requiredAssociationFieldType) match{
          case None => //need to create the requiredAssocFieldSet
            val requiredAssocFTDeclarer = requiredAssociationFieldType.declarerForType(selectedTrait)
            requiredAssocFTDeclarer match {
              case Some(declarer) => {
                //if the requiredAssociationFieldType is allowed by selectedTrait then add it
                traitAssociationFieldSets += makeRequiredAssocFieldSet(declarer)
              }
              //the requiredAssociationFieldType should be allowed by selectedTrait
              // because allowedTraits contains only traits that allow it
              case None =>
                throw new Exception("no declarer of " + requiredAssociationFieldType +
                  " found among " + selectedTrait.name +"'s supertypes")
            }
          case Some(afsb) => makeRequiredAssocField(afsb)
        }
      }
    }
  }


  def build(): Box[ZItem] = {
    //TODO check item validity

    if(itemName=="") {
      S.error("name-error","Item must have a name")
      return Empty
    } else S.error("name-error","") //clear the error message if the name causes no errors

    val item = db.createItem(selectedItemType, itemName)

    //add trait
    for(t <- selectedTrait){
      item.addTrait(t)
    }

    //create properties
    for(ps <- propertySets; p <- ps.properties){
      p.build(item)
    }

    //create associations
    for(as <- associationFieldSets; a <- as.associationFields){
      a.build(item)
    }
    Full(item)
  }

  // -------------- Helper Methods ---------------

  private def updateTraitDefinedFields{
   //recalculate Field Sets required by selectedTrait
    _selectedTrait match {
      case Full(tr) => {
        traitPropertySets = tr.requiredPropertySets.map(new PropertySetBuilder(this,_))
        traitAssociationFieldSets = tr.requiredAssociationFieldSets.map(new AssociationFieldSetBuilder(this,_))
      }
      case _ => {
        traitPropertySets = Set()
        traitAssociationFieldSets = Set()
      }
    }
  }

  private def makeRequiredAssocFieldSet(fieldDeclarer:ZType) = {
    val assocFieldSetType = ZAssociationFieldSetType(fieldDeclarer,requiredAssociationFieldType)
    val requiredAssocFieldSetBuilder = new AssociationFieldSetBuilder(this,assocFieldSetType)
    makeRequiredAssocField(requiredAssocFieldSetBuilder)
    requiredAssocFieldSetBuilder
  }
  private def makeRequiredAssocField(assocFieldSetBuilder: AssociationFieldSetBuilder){
    val assocFieldBuilder = assocFieldSetBuilder.addAssociationField match{
      case Full(afb) => afb
      case _ => throw new Exception("requiredAssociationField cannot be created because "
        +assocFieldSetBuilder +" is not allowing new members")
    }
    assocFieldBuilder.addLockedRolePlayer(otherRolePlayer)
    additionalRolePlayers.foreach(assocFieldBuilder.addLockedRolePlayer(_))
    //TODO validate that the specified rolePlayers are valid for this associationField
  }
}
