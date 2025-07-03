package Gameplans.Zerg.ZvP

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Placement.Access.{PlaceLabels, PlacementQuery}
import ProxyBwapi.Races.{Protoss, Zerg}
import Utilities.SwapIf
import Utilities.Time.Seconds
import Utilities.UnitFilters.IsHatchlike

class ZvP3HatchBust extends GameplanImperative {

  override def executeBuild(): Unit = {
    once(9, Zerg.Drone)
    once(2, Zerg.Overlord)
    once(12, Zerg.Drone)
    requireMiningBases(2)
    get(Zerg.SpawningPool)
    once(15, Zerg.Drone)
    //requireMiningBases(3)
    get(1, Zerg.Hatchery, PlaceLabels.MacroHatch)
    get(Zerg.Extractor, new PlacementQuery(Zerg.Extractor).requireBase(With.geography.ourMain))
    once(6, Zerg.Zergling)
    once(3, Zerg.Overlord)
    get(Zerg.ZerglingSpeed)
    scoutOn(Zerg.Hatchery, 3)
  }

  override def executeMain(): Unit = {
    if (upgradeComplete(Zerg.HydraliskSpeed) || unitsComplete(Zerg.Hydralisk) < 3 || ! enemyHasUpgrade(Protoss.DragoonRange)) {
      attack()
    }

    if ( ! haveGasForUpgrade(Zerg.ZerglingSpeed)) {
      gasLimitCeiling(100)
    } else if ( ! upgradeComplete(Zerg.ZerglingSpeed)) {
      gasLimitCeiling(0)
    } else if ( ! have(Zerg.HydraliskDen)) {
      gasLimitCeiling(50)
    } else if ( ! haveGasForUpgrade(Zerg.HydraliskRange)) {
      gasLimitCeiling(250)
    } else {
      gasLimitCeiling(125)
    }

    once(24, Zerg.Zergling)
    if ( ! upgradeComplete(Zerg.ZerglingSpeed, 1, Zerg.Zergling.buildFrames + Seconds(15)())) {
      pump(Zerg.Zergling)
    }
    get(Zerg.HydraliskDen)
    get(Zerg.HydraliskSpeed)
    get(Zerg.HydraliskRange)
    get(18, Zerg.Drone)

    if (upgradeComplete(Zerg.ZerglingAttackSpeed, 1, Zerg.Zergling.buildFrames * 2) || ! upgradeComplete(Zerg.HydraliskSpeed, 1, Zerg.Hydralisk.buildFrames)) {
      pump(Zerg.Zergling)
    }
    once(9, Zerg.Hydralisk)
    SwapIf(
      safeDefending || With.scouting.enemyProximity < 0.4,
      pump(Zerg.Hydralisk),
      pump(Zerg.Drone, 7 * unitsComplete(IsHatchlike)))

    pumpGasPumps(units(Zerg.Drone) / 7)
    pump(Zerg.Zergling)
    get(6, Zerg.Hatchery)
  }
}
