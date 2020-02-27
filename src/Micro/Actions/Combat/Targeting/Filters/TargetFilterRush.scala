package Micro.Actions.Combat.Targeting.Filters
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterRush extends TargetFilter {
  val timeThreshold = GameTime(4, 30)()
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    With.frame > timeThreshold
      || ! target.unitClass.isBuilding
      || ! target.base.exists(b => With.intelligence.enemyMain.contains(b) || With.intelligence.enemyNatural.contains(b))
      || target.zone.walledIn
      || target.totalHealth < 300
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
