package Placement

import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.{Direction, SpecificPoints}
import Utilities.ByOption

import scala.collection.mutable.ArrayBuffer

class Preplacement {

  private var initialized: Boolean = false

  val preplacement: Preplacer = new Preplacer
  val fits = new ArrayBuffer[Fit]

  def update(): Unit = {
    initialize()
  }

  private def initialize(): Unit = {
    if (initialized) return
    initialized = true

    With.geography.bases.foreach(b => preplacement.place(Fit(b.townHallTile, PreplacementTemplates.townhall)))
    With.geography.bases.foreach(b => b.resourcePathTiles.foreach(t => preplacement.place(Fit(t, PreplacementTemplates.walkway))))
    With.geography.zones.foreach(preplaceZone)
  }

  private def preplaceZone(zone: Zone): Unit = {
    val anyExit = zone.exit.map(_.pixelCenter.tileIncluding).orElse(ByOption.minBy(zone.tiles)(_.tileDistanceFast(SpecificPoints.tileMiddle)))
    val anyBack = anyExit.flatMap(o => ByOption.maxBy(zone.tiles)(_.tileDistanceFast(o)))

    if (anyExit.isEmpty || anyBack.isEmpty) return

    val bounds = zone.boundary
    val exit = anyExit.get
    val back = anyBack.get
    val exitEdge = bounds.cornerTilesInclusive.sortBy(_.tileDistanceManhattan(exit)).take(2).minBy(t => Math.min(t.x, t.y))
    val backEdge = bounds.cornerTilesInclusive.sortBy(_.tileDistanceManhattan(back)).take(2).minBy(t => Math.min(t.x, t.y))
    val exitSide = bounds.cornerTilesInclusive.maxBy(_.tileDistanceManhattan(exitEdge))
    val backSide = bounds.cornerTilesInclusive.maxBy(_.tileDistanceManhattan(backEdge))
    val directionExit = new Direction(anyBack.get, anyExit.get)
    val directionBack = new Direction(anyExit.get, anyBack.get)

    fits ++= preplacement.fit     (exit,      bounds, directionBack, PreplacementTemplates.batterycannon)
    fits ++= preplacement.fitAny  (exitEdge,  bounds, directionBack, PreplacementTemplates.initialLayouts)
    fits ++= preplacement.fitAny  (exitEdge,  bounds, directionBack, PreplacementTemplates.gateways, 1)
    fits ++= preplacement.fitAny  (backEdge,  bounds, directionExit, PreplacementTemplates.tech, 1)
    fits ++= preplacement.fitAny  (exitEdge,  bounds, directionBack, PreplacementTemplates.gateways, 1)
    fits ++= preplacement.fitAny  (backEdge,  bounds, directionExit, PreplacementTemplates.tech)
    fits ++= preplacement.fitAny  (exitEdge,  bounds, directionBack, PreplacementTemplates.gateways)
  }
}
