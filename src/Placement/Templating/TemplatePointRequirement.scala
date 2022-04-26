package Placement.Templating

import Mathematics.Maff
import Placement.Access.PlaceLabels.{Gas, PlaceLabel, TownHall}
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

  def withLabels(newLabels: PlaceLabel*): TemplatePointRequirement = {
    labels ++= newLabels
    this
  }

  def dimensions: (Int, Int) = (width, height)

  def isTownHall = labels.contains(TownHall)
  def isGas = labels.contains(Gas)

  override def toString: String = (
    if (buildableAfter) "Unused"
    else if (walkableAfter) "Hallway"
    else if (buildings.exists(_.isGas)) "Gas"
    else if (buildings.nonEmpty) buildings.take(3).map(_.toString.take(4)).mkString(", ") + (if (buildings.size > 3) "..." else "")
    else if (labels.nonEmpty) (if (buildings.nonEmpty) "\n" else "") + labels.take(3).mkString(", ") + (if (labels.size > 3) "..." else "")
    else f"$width x $height")
}
