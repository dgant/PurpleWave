package Placement

import Mathematics.Maff
import ProxyBwapi.UnitClasses.UnitClass


class PreplacementRequirement(val buildings: UnitClass*) {
  var width: Int = Maff.max(buildings.map(_.tileWidth)).getOrElse(1)
  var height: Int = Maff.max(buildings.map(_.tileHeight)).getOrElse(1)

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

  override def toString: String = (
      if (buildableAfter) "Unused"
      else if (walkableAfter) "Walkway"
      else if (buildings.nonEmpty) buildings.mkString(", ")
      else "Building"
    ) + " " + dimensionString

  private def dimensionString: String = width + "x" + height
}
