package Placement

import ProxyBwapi.UnitClasses.UnitClass
import Utilities.ByOption

class PreplacementType(val buildings: UnitClass*) {
  var width: Int = ByOption.max(buildings.map(_.width)).getOrElse(1)
  var height: Int = ByOption.max(buildings.map(_.height)).getOrElse(1)

  def this(theWidth: Int, theHeight: Int) {
    this()
    width = theWidth
    height = theHeight
  }

  def allows(building: UnitClass): Boolean = {
    width >= building.width && height >= building.height && (buildings.isEmpty || buildings.contains(building))
  }

  val requireWalkable: Boolean = true
  val requireBuildable: Boolean = true
  val walkableAfter: Boolean = false
  val buildableAfter: Boolean = false
}
