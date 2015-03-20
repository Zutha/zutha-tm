package net.zutha.lib.uri

import net.zutha.model.constants.ApplicationConstants._
import net.liftweb.http.{PermRedirectResponse, LiftResponse, Req}
import net.zutha.model.constructs.{ZAssociationType, ZRole, ZItem}

object UriRedirector {
  /* redirect: /item/0IZ?<params>
   *        => /item/012/Looked_Up_Name/details.html?<params>
   * redirect: /item/0IZ/wrong_name
   *        => /item/012/Corrected_Name/details.html
   * redirect: /item/0IZ//099Y/Association_Name
   *        => /item/012/Looked_Up_Name/099Y/Association_Name/assoc_table.html
   * redirect: /item/0867/someimage.png/
   *        => /item/0867/someimage.png/details.html
   * redirect: /item/0867/
   *        => /item/0867/someimage.png/details.html
   * redirect: /file/0867
   *        => /file/0867/someimage.png
   */
  def unapply(req: Req): Option[LiftResponse] = req match {
    case r @ Req(path,suffix,reqType) =>
      def makeResponse(targetPath: String) = Some(PermRedirectResponse(targetPath,r))
      path match {
      //match: /item/<fixableZID>...
      case "item"::ZIDLookup(itemFixed,item)::tail => tail match {
        //redirect: /item/<fixableZID>
        //      => /item/<fixedZID>/<correctName>
        case Nil => makeResponse(ItemLoc.makeUri(item))

        //match: /item/<fixable_zid>/<wrong_name>...
        case itemName::tail2 => (itemFixed || itemName!=UriName(item.name), tail2) match {
          //redirect: /item/<fixableZID>/<wrongName>
          //      => /item/<fixedZID>/<correctName>
          case (true,Nil) => makeResponse(ItemLoc.makeUri(item))

          //redirect: /item/<fixableZID>/<wrongName>/<view>(.html)?
          //       => /item/<fixedZID>/<correctName>/<view>.html
          case (fixed,view::Nil) if(fixed || suffix=="") => makeResponse(ItemLoc.makeUri(item,view))

          //match /item/<fixableItemZID>/<wrongItemName>/<fixableRoleZID>/<wrongRoleName>/<fixableAssocTypeZID>/<wrongAssocTypeName>...
          case (fixed1,ZIDLookup(roleFixed,ZRole(role))::roleName::ZIDLookup(assocFixed,ZAssociationType(assoc))::assocName::tail3)
             => (fixed1 || roleFixed || roleName!=UriName(role.name) || assocFixed || assocName!=UriName(assoc.nameF(role)),tail3) match {
            //redirect: /item/<fixableItemZID>/<wrongItemName>/<fixableRoleZID>/<wrongRoleName>/<fixableAssocTypeZID>/<wrongAssocTypeName>
            //       => /item/<itemZID>/<itemName>/<roleZID>/<roleName>/<assocTypeZID>/<assocTypeName>
            case (true,Nil) => makeResponse(AssocLoc.makeUri(item,role,assoc))
            //redirect: /item/<fixableItemZID>/<wrongItemName>/<fixableRoleZID>/<wrongRoleName>/<fixableAssocTypeZID>/<wrongAssocTypeName>/<view>(.html)?
            //       => /item/<itemZID>/<itemName>/<roleZID>/<roleName>/<assocTypeZID>/<assocTypeName>/<view>.html
            case (fixed,view::Nil) if(fixed || suffix=="") => makeResponse(AssocLoc.makeUri(item,role,assoc,view))

            //match /item/<fixableItemZID>/<wrongItemName>/<fixableRoleZID>/<wrongRoleName>/<fixableAssocTypeZID>/<wrongAssocTypeName>/<fixableOtherRoleZID>/<wrongOtherRoleName>...
            case (fixed2,ZIDLookup(otherRoleFixed,ZRole(otherRole))::otherRoleName::tail4)
              => (fixed2 || otherRoleFixed || otherRoleName!=UriName(otherRole.name), tail4) match {
              //redirect: /item/<fixableItemZID>/<wrongItemName>/<fixableRoleZID>/<wrongRoleName>/<fixableAssocTypeZID>/<wrongAssocTypeName>/<fixableOtherRoleZID>/<wrongOtherRoleName>
              //       => /item/<itemZID>/<itemName>/<roleZID>/<roleName>/<assocTypeZID>/<assocTypeName>/<otherRoleZID>/<otherRoleName>
              case (true,Nil) => makeResponse(RoleLoc.makeUri(item,role,assoc,otherRole))
              //redirect: /item/<fixableItemZID>/<wrongItemName>/<fixableRoleZID>/<wrongRoleName>/<fixableAssocTypeZID>/<wrongAssocTypeName>/<fixableOtherRoleZID>/<wrongOtherRoleName>/<view>(.html)?
              //       => /item/<itemZID>/<itemName>/<roleZID>/<roleName>/<assocTypeZID>/<assocTypeName>/<otherRoleZID>/<otherRoleName>/<view>.html
              case (fixed,view::Nil) if(fixed || suffix=="") => makeResponse(RoleLoc.makeUri(item,role,assoc,otherRole,view))
              case _ => None //uri is either correct or invalid, but not fixable
            }
            case _ => None //uri is either correct or invalid, but not fixable
          }
          case _ => None //uri is either correct or invalid, but not fixable
        }
        case _ => None //uri is either correct or invalid, but not fixable
      }
      //match: /file/<fixableZID>...
      case "file"::ZIDLookup(wasRepaired,item)::tail => tail match {
        //redirect: /file/<fixableZID>
        //       => /file/<fixedZID>/<looked_up_name>
        case Nil => makeResponse(FileUri(item))
        //redirect: /file/<fixable_zid>/<wrong_name>
        //       => /file/<fixedZID>/<correctName>
        case filename::Nil if{ //TODO match filename and file_extension separately against a File item
          val ext = if(suffix=="") "" else "."+suffix
          (wasRepaired || filename!=UriName(item.name))} => makeResponse(FileUri(item))
        case _ => None
      }
      //match: /<stem>...
      case _ => None //no need to redirect

    }
  }
}
//TODO make FileLoc, File View, and File ItemType
object FileUri {
  /** returns a URI string of the form /item/<zid>/<item name>/details.html */
  def apply(item: ZItem): String = "/file/"+item.zid+"/"+UriName(item.name)
}
