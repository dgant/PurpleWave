package NeoGeo.Internal

import java.awt.Color

import NeoGeo.NeoContinent

import scala.collection.mutable

class NeoContinentRaw extends NeoContinent {
  val walks = new mutable.HashSet[Int]
  var color = new Color(0, 0, 0)
}
