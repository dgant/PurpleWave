package Micro.Actions.Scouting

import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Mathematics.Points.{PixelRectangle, TileRectangle}
import Mathematics.Shapes.Spiral
import Micro.Actions.Action
import Micro.Agency.Commander
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Seconds

object CannonRush extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = With.blackboard.cannonRush().nonEmpty

  val arrivalKey = "CannonRush: Arrived"

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val base              = With.blackboard.cannonRush().get
    val foundations       = With.placement.get(base).filter(_.point.requirement.labels.contains(PlaceLabels.CannonRush)).sortBy(_.tile.groundTiles(base.heart)).sortBy(_.point.requirement.buildings.contains(Protoss.PhotonCannon))
    val cannon            = foundations.find(_.point.requirement.buildings.contains(Protoss.PhotonCannon))
    val startedRushing    = With.units.ours.exists(u => u.unitClass.isBuilding && u.base.contains(base))
    val framesUntilReady  =
      Math.max(
        MacroFacts.framesUntilUnit(Protoss.Forge) - Protoss.Pylon.buildFramesFull,
        With.accounting.framesToMineMinerals(Math.max(0, 200 - With.self.minerals)))

    if (cannon.isEmpty) return

    val cannonPixel = cannon.get.tile.topLeftPixel.add(32, 32)

    if ( ! startedRushing && framesUntilReady > Math.max(Seconds(5)(), unit.framesToTravelTo(cannonPixel))) return

    val arrived = unit.agent.blackboard.contains(arrivalKey) || unit.pixelDistanceCenter(cannonPixel) < 48
    if (arrived) {
      unit.agent.blackboard.put(arrivalKey, true)
    } else {
      unit.agent.decision.set(cannonPixel)
      Commander.move(unit)
      return
    }

    foundations.foreach(foundation => {
      if ( ! With.units.ours.exists(u => u.unitClass.isBuilding && u.tileTopLeft == foundation.tile)) {
        //val building = ?(MacroFacts.haveComplete(Protoss.Forge), foundation.point.requirement.buildings.head, Protoss.Pylon)
        val building    = foundation.point.requirement.buildings.head
        val tile        = foundation.tile
        val areaCenter  = foundation.tile.topLeftPixel.add(32, 32)
        val areaStart   = areaCenter.subtract(building.dimensionLeft, building.dimensionUp)
        val areaEnd     = areaCenter.add(building.dimensionRightInclusive, building.dimensionDownInclusive)
        val rectangle   = new TileRectangle(tile, 2, 2)

        if (PixelRectangle(unit.pixelStart, unit.pixelEndExclusive).intersects(PixelRectangle(areaStart, areaEnd))) {
          val t = Spiral(3)
            .map(unit.pixel.tile.add)
            .find(t => t.walkable && ! rectangle.contains(t))
            .map(_.center)
            .getOrElse(areaCenter.project(unit.pixel, 64))
          unit.agent.decision.set(t)
          Commander.move(unit)
        } else if (unit.moving && unit.pixelDistanceCenter(foundation.tile.topLeftPixel.add(32, 32)) < 71) {
          Commander.stop(unit)
        } else {
          Commander.build(unit, building, tile)
        }
        return
      }
    })

    Maff.minBy(With.units.enemy.filter(_.orderTarget.contains(unit)))(_.totalHealth).foreach(target => {
      unit.agent.toAttack = Some(target)
      Commander.attack(unit)
    })
  }
}
