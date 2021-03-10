package Micro.Actions.Combat.Targeting.Filters
import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.GameTime

object TargetFilterRush extends TargetFilter {
  simulationSafe = true
  private val timeThreshold = GameTime(4, 30)()
  override def appliesTo(actor: FriendlyUnitInfo): Boolean = With.frame < timeThreshold
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    ! target.unitClass.isBuilding
      || ! target.base.exists(b => With.scouting.enemyMain.contains(b) || With.scouting.enemyNatural.contains(b))
      || target.base.forall(_.resourcePathTiles.forall(_.explored))
      || target.tileArea.contains(actor.agent.destination)
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
