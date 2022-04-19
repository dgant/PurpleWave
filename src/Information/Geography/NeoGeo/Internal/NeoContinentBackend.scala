package Information.Geography.NeoGeo.Internal

import Information.Geography.NeoGeo.NeoContinent
import java.awt.Color

import scala.collection.mutable

class NeoContinentBackend extends NeoContinent {
  val walks = new mutable.ArrayBuffer[Int]
  var color = new Color(0, 0, 0)

  override def walkable: Seq[Int] = walks
}
