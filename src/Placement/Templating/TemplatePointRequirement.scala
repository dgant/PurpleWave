package Placement.Templating

import Mathematics.Maff
import Placement.Access.PlaceLabels.PlaceLabel
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

class TemplatePointRequirement(val buildings: UnitClass*) {
  val labels = new ArrayBuffer[PlaceLabel]
  val buildableBefore : Boolean = true
  val walkableBefore  : Boolean = true
  val buildableAfter  : Boolean = false
  val walkableAfter   : Boolean = false
  var width   : Int = Maff.max(buildings.map(_.tileWidth)).getOrElse(1)
  var height  : Int = Maff.max(buildings.map(_.tileHeight)).getOrElse(1)

  def this(argWidth: Int, argHeight: Int) {
    this()
    width = argWidth
    height = argHeight
  }

  def dimensions: (Int, Int) = (width, height)

  override def toString: String = (
    if (buildableAfter) "Unused"
    else if (walkableAfter) "Hallway"
    else if (buildings.nonEmpty) buildings.mkString(", ")
    else "Building") + f" $width x $height"
}
