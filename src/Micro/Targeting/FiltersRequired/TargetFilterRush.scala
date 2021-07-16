package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Micro.Targeting.TargetFilter
import Planning.UnitMatchers.MatchProxied
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.GameTime

object TargetFilterRush extends TargetFilter {
  simulationSafe = true
  private val timeThreshold = GameTime(4, 45)()
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = With.frame < timeThreshold
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    ! target.unitClass.isBuilding
    || target.is(MatchProxied)
    || target.zone.walledIn
    || target.isAny(
        Terran.Bunker,
        Terran.MissileTurret,
        Protoss.PhotonCannon,
        Protoss.ShieldBattery,
        Zerg.SpawningPool,
        Zerg.Spire,
        Zerg.CreepColony,
        Zerg.SunkenColony,
        Zerg.SporeColony)
  )
}
