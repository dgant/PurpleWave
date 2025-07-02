package Placement.Templating

import Mathematics.Maff
import Placement.Access.PlaceLabels.{Gas, MacroHatch, PlaceLabel, TownHall}
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass

import scala.collection.mutable.ArrayBuffer

class TemplatePointRequirement(val buildings: UnitClass*) {
  val labels = new ArrayBuffer[PlaceLabel]
  val buildableBefore : Boolean   = true
  val walkableBefore  : Boolean   = true
  val buildableAfter  : Boolean   = false
  val walkableAfter   : Boolean   = false
  var width           : Int       = Maff.max(buildings.map(_.tileWidth)).getOrElse(1)
  var height          : Int       = Maff.max(buildings.map(_.tileHeight)).getOrElse(1)

  def this(argWidth: Int, argHeight: Int) {
    this()
    width = argWidth
    height = argHeight
  }

  def withLabels(newLabels: PlaceLabel*): TemplatePointRequirement = {
    labels ++= newLabels
    this
  }

  def dimensions: (Int, Int) = (width, height)

  def isTownHall  : Boolean = labels.contains(TownHall)
  def isMacroHatch: Boolean = labels.contains(MacroHatch)
  def isGas       : Boolean = labels.contains(Gas)
  def powers      : Boolean = buildings.contains(Protoss.Pylon)

  override def toString: String = {
    if (buildableAfter) return "Unused"
    if (walkableAfter)  return "Hallway"
    if (isGas)          return "Gas"
    if (isTownHall)     return "Town Hall"
    if (buildings.isEmpty && labels.isEmpty) return f"$width x $height"
    val buildingList        = buildings.take(3).map(_.toString.take(4)).mkString(", ")
    val buildingTerminator  = if (buildings.size > 3) "..." else ""
    val optionalSeparator   = if (buildings.nonEmpty && labels.nonEmpty) "\n---\n" else ""
    val labelList           = labels.take(3).mkString("\n")
    val labelTerminator     = if (labels.size > 5) "..." else ""
    f"$buildingList$buildingTerminator$optionalSeparator$labelList$labelTerminator"
  }
}
