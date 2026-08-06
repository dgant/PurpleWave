package Micro.Actions.Scouting

import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Mathematics.Points.PixelRectangle
import Micro.Actions.Action
import Micro.Agency.Commander
import Placement.Access.PlaceLabels
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?
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
      val finalBuilding = foundation.point.requirement.buildings.head
      val desiredBuilding = ?(
        MacroFacts.haveComplete(Protoss.Forge) || ! With.units.enemy.exists(_.orderTarget.contains(unit)),
        finalBuilding,
        Protoss.Pylon)

      val existing = With.units.ours.find(u => u.unitClass.isBuilding && u.tileTopLeft == foundation.tile)

      if (existing.exists(e => (e.unitClass != desiredBuilding && With.self.minerals >= desiredBuilding.mineralPrice) || (e.remainingCompletionFrames < 12 && ! finalBuilding(e)))) {
        existing.foreach(Commander.cancel)
      } else if (existing.isEmpty) {
        val tile          = foundation.tile
        val areaCenter    = tile.topLeftPixel.add(32, 32)
        val areaStart     = areaCenter.subtract (desiredBuilding.dimensionLeft,           desiredBuilding.dimensionUp)
        val areaEnd       = areaCenter.add      (desiredBuilding.dimensionRightExclusive, desiredBuilding.dimensionDownExclusive)
        val rectangle     = PixelRectangle(areaStart, areaEnd)
        val unitRectangle = PixelRectangle(unit.pixelStart, unit.pixelEndExclusive)
        val distance      = unit.pixelDistanceCenter(areaCenter)
        val escapeDistance = Protoss.Probe.dimensionMax + desiredBuilding.dimensionMax + 10
        if (unitRectangle.intersects(rectangle)) {
          unit.agent.decision.set(areaCenter.project(unit.pixel, escapeDistance).walkableTile.center)
          Commander.move(unit)
        } else if (distance <= 70) { // Threshold of distance to build without moving
          if (unit.moving) {
            Commander.hold(unit)
          } else {
            Commander.build(unit, desiredBuilding, tile)
          }
        } else if (distance < 120) {
          unit.agent.decision.set(
              rectangle
                .nearestPoint(unit.pixel)
                .project(unit.pixel, unit.unitClass.dimensionMax)
                .walkableTile
                .center)
          Commander.move(unit)
        } else {
          unit.agent.decision.set(areaCenter)
          Commander.move(unit)
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
