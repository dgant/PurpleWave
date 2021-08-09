package NeoGeo.Internal

import java.awt.Color

import NeoGeo.NeoContinent

import scala.collection.mutable

class NeoContinentBackend extends NeoContinent {
  val walks = new mutable.ArrayBuffer[Int]
  var color = new Color(0, 0, 0)

  override def walkable: Seq[Int] = walks
}
