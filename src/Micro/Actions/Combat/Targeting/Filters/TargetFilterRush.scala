package Micro.Actions.Combat.Targeting.Filters
import Lifecycle.With
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.GameTime

object TargetFilterRush extends TargetFilter {
  val timeThreshold = GameTime(4, 30)()
  override def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    With.frame > timeThreshold
      || ! target.unitClass.isBuilding
      || ! target.base.exists(b => With.scouting.enemyMain.contains(b) || With.scouting.enemyNatural.contains(b))
      || target.base.forall(_.resourcePathTiles.forall(With.grids.friendlyVision.ever))
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
