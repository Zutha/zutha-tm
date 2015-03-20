package net.zutha.lib.uri

import net.liftweb.util.Helpers._

object UriName {
  def apply(in: String): String = in.replace(" ","_")
}
