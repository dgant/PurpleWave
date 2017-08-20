package Planning.Plans.Protoss.GamePlans

import Information.Geography.Types.Zone
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchType
import Planning.Plans.Army.{AllIn, Attack}
import Planning.Plans.Compound._
import Planning.Plans.Information.SwitchEnemyRace
import Planning.Plans.Macro.Automatic.{Gather, RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, FollowBuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps}
import Planning.Plans.Macro.Milestones.{EnemyUnitsAtLeast, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Scouting.Scout
import Planning.ProxyPlanner
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

class Proxy2Gate extends Parallel {
  
  lazy val proxyZone: Option[Zone] = ProxyPlanner.proxyAutomaticSneaky
  
  override def onUpdate(): Unit = {
    With.blackboard.maxFramesToSendAdvanceBuilder = Int.MaxValue
    super.onUpdate()
  }
  
  private class BasicPlan extends Parallel(
    new If(
      new Or(
        new EnemyUnitsAtLeast(1, UnitMatchType(Terran.Factory)),
        new EnemyUnitsAtLeast(1, UnitMatchType(Terran.Vulture)),
        new EnemyUnitsAtLeast(1, UnitMatchType(Terran.Wraith)),
        new EnemyUnitsAtLeast(1, UnitMatchType(Zerg.Spire)),
        new EnemyUnitsAtLeast(1, UnitMatchType(Zerg.Mutalisk)),
        new EnemyUnitsAtLeast(1, UnitMatchType(Zerg.LurkerEgg)),
        new EnemyUnitsAtLeast(1, UnitMatchType(Zerg.Lurker)),
        new EnemyUnitsAtLeast(1, UnitMatchType(Protoss.Scout)),
        new EnemyUnitsAtLeast(1, UnitMatchType(Protoss.Carrier)),
        new EnemyUnitsAtLeast(1, UnitMatchType(Protoss.CitadelOfAdun)),
        new EnemyUnitsAtLeast(1, UnitMatchType(Protoss.TemplarArchives)),
        new EnemyUnitsAtLeast(1, UnitMatchType(Protoss.DarkTemplar))),
      new AllIn),
    new TrainContinuously(Protoss.Zealot),
    new TrainWorkersContinuously,
    new Trigger(
      new Check(() => With.frame > 24 * 60 * 3),
      initialAfter = new TrainContinuously(Protoss.Gateway, 5)))
  
  private class OhNoTheyreTerranGetGoons extends Parallel(
    new Trigger(
      new Or(
        new And(
          new EnemyUnitsAtLeast(1, UnitMatchType(Terran.Vulture)),
          new Check(() => With.geography.enemyZones.exists(_.walledIn))),
        new Check(() => With.units.enemy.exists(u => u.flying && u.unitClass.isBuilding))),
      initialAfter = new Parallel(
        new TrainWorkersContinuously,
        new Build(RequestAtLeast(1, Protoss.CyberneticsCore)),
        new BuildGasPumps,
        new If(
          new And(
            new UnitsAtLeast(1, UnitMatchType(Protoss.CyberneticsCore), complete = true),
            new UnitsAtLeast(1, UnitMatchType(Protoss.Assimilator),     complete = true)),
          new Parallel(
            new UpgradeContinuously(Protoss.DragoonRange),
            new If(
              new Check(() => With.self.gas > 40),
              new TrainContinuously(Protoss.Dragoon),
              new TrainContinuously(Protoss.Zealot)),
            new Build(RequestAtLeast(5, Protoss.Gateway))),
          new Parallel(
            new TrainContinuously(Protoss.Zealot),
            new Build(RequestAtLeast(5, Protoss.Gateway))))),
      initialBefore = new BasicPlan))
  
  children.set(Vector(
    new ProposePlacement{
      override lazy val blueprints = Vector(
        new Blueprint(this, building = Some(Protoss.Pylon),   preferZone = proxyZone, respectHarvesting = false, placementProfile = Some(PlacementProfiles.proxyPylon)),
        new Blueprint(this, building = Some(Protoss.Gateway), preferZone = proxyZone, respectHarvesting = false, placementProfile = Some(PlacementProfiles.proxyBuilding)),
        new Blueprint(this, building = Some(Protoss.Gateway), preferZone = proxyZone, respectHarvesting = false, placementProfile = Some(PlacementProfiles.proxyBuilding)))
    },
    new Build(
      RequestAtLeast(1, Protoss.Nexus),
      RequestAtLeast(9, Protoss.Probe),
      RequestAtLeast(1, Protoss.Pylon)),
    
    // Crappy haxx to make this all work, and to not pull three Probes to build three buildings
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.Pylon), complete = false),
      new Build(RequestAtLeast(1, Protoss.Gateway))),
    new If(
      new UnitsAtLeast(1, UnitMatchType(Protoss.Gateway), complete = false),
      new Build(RequestAtLeast(2, Protoss.Gateway))),
    new Trigger(
      new UnitsAtLeast(2, UnitMatchType(Protoss.Gateway), complete = false),
      initialAfter = new Parallel(
        new RequireSufficientSupply,
        new SwitchEnemyRace(
          whenTerran  = new BasicPlan, // new OhNoTheyreTerranGetGoons,
          whenProtoss = new BasicPlan,
          whenZerg    = new BasicPlan,
          whenRandom  = new BasicPlan),
        new Scout)),
    
    new Attack,
    new FollowBuildOrder,
    new Gather
  ))
}