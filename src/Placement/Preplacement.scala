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
    val exit = zone.exit.map(_.pixelCenter.tileIncluding).orElse(ByOption.minBy(zone.tiles)(_.tileDistanceFast(SpecificPoints.tileMiddle)))
    val back = exit.flatMap(o => ByOption.maxBy(zone.tiles)(_.tileDistanceFast(o)))

    if (exit.isEmpty || back.isEmpty) return

    val directionExit = new Direction(back.get, exit.get)
    val directionBack = new Direction(exit.get, back.get)

    fits ++= preplacement.fit(exit.get, directionBack, PreplacementTemplates.batterycannon)
    fits ++= preplacement.fitAny(exit.get, directionBack, PreplacementTemplates.initialLayouts)
    fits ++= preplacement.fitAny(exit.get, directionBack, PreplacementTemplates.gateways, 1)
    fits ++= preplacement.fitAny(back.get, directionExit, PreplacementTemplates.tech, 1)
    fits ++= preplacement.fitAny(exit.get, directionBack, PreplacementTemplates.gateways, 1)
    fits ++= preplacement.fitAny(back.get, directionExit, PreplacementTemplates.tech)
    fits ++= preplacement.fitAny(exit.get, directionBack, PreplacementTemplates.gateways)
  }
}
