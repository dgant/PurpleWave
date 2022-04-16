package Macro.Architecture

import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.Shapes.Pylons

import scala.collection.mutable

abstract class ArchitectureDiff {
  def doo(): Unit
  def undo(): Unit
}

class ArchitectureDiffSeries extends ArchitectureDiff {
  val stack: mutable.ArrayBuffer[ArchitectureDiff] = new mutable.ArrayBuffer[ArchitectureDiff]()

  override def doo(): Unit = {
    stack.foreach(_.doo())
  }

  override def undo(): Unit = {
    stack.view.reverse.foreach(_.undo())
  }
}

class ArchitectureDiffExclude(tile: Tile, exclusion: Exclusion) extends ArchitectureDiff {
  val unbuildableBefore: Option[Exclusion] = With.architecture.unbuildable.get(tile)
  val unwalkableBefore: Option[Exclusion] = With.architecture.unwalkable.get(tile)
  val untownhallableBefore: Option[Exclusion] = With.architecture.untownhallable.get(tile)
  val ungassableBefore: Option[Exclusion] = With.architecture.ungassable.get(tile)

  override def doo(): Unit = {
    With.architecture.unbuildable.set(tile, Some(exclusion))
    With.architecture.unwalkable.set(tile, Some(exclusion))
    With.architecture.untownhallable.set(tile, Some(exclusion))
    With.architecture.ungassable.set(tile, Some(exclusion))
  }

  override def undo(): Unit = {
    With.architecture.unbuildable.set(tile, unbuildableBefore)
    With.architecture.unwalkable.set(tile, unwalkableBefore)
    With.architecture.untownhallable.set(tile, untownhallableBefore)
    With.architecture.ungassable.set(tile, ungassableBefore)
  }
}

class ArchitectureDiffPower(tile: Tile, frame: Int) extends ArchitectureDiff {
  val psi2Tiles: Array[Tile] = Pylons.points2.map(tile.add).filter(_.valid)
  val psi3Tiles: Array[Tile] = Pylons.points3.map(tile.add).filter(_.valid)
  val power2Before: Array[Int] = psi2Tiles.map(With.architecture.powerFrame2Height.get)
  val power3Before: Array[Int] = psi3Tiles.map(With.architecture.powerFrame3Height.get)

  override def doo(): Unit = {
    psi2Tiles.foreach(With.architecture.powerFrame2Height.set(_, frame))
    psi3Tiles.foreach(With.architecture.powerFrame3Height.set(_, frame))
  }

  override def undo(): Unit = {
    var i = 0
    while (i < psi2Tiles.length) {
      With.architecture.powerFrame2Height.set(psi2Tiles(i), power2Before(i))
      i += 1
    }
    i = 0
    while (i < psi3Tiles.length) {
      With.architecture.powerFrame2Height.set(psi3Tiles(i), power3Before(i))
      i += 1
    }
  }
}