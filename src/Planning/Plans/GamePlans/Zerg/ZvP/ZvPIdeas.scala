package Planning.Plans.GamePlans.Zerg.ZvP

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Macro.Buildables.{Buildable, BuildableUpgrade}
import Micro.Squads.Goals.GoalWatchIslands
import Planning.Composition.UnitMatchers._
import Planning.Plans.Army.{Aggression, SquadPlan}
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{RequireBases, RequireMiningBases}
import Planning.Plans.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import Planning.Plans.Predicates.{OnMap, SafeAtHome}
import Planning.Plans.Scouting.Scout
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.Upgrades.Upgrade
import Strategery.Maps.ThirdWorld

object ZvPIdeas {
  
  class SafeForOverlords extends And(
    new FrameAtMost(GameTime(4, 0)()),
    new EnemyUnitsAtMost(0, UnitMatchOr(
      Protoss.Dragoon,
      Protoss.Corsair,
      Protoss.Stargate,
      Protoss.CyberneticsCore),
      complete = true))
  
  class ScoutSafelyWithOverlord extends If(
    new SafeForOverlords,
    new Scout(3) { scouts.get.unitMatcher.set(Zerg.Overlord) })
  
  class ScoutSafelyWithDrone extends If(
    new EnemyUnitsAtMost(0, UnitMatchOr(Protoss.Zealot, Protoss.PhotonCannon, Protoss.Dragoon), complete = true),
    new Scout)
  
  class ShouldDoSpeedlingAllIn extends EnemyStrategy(
    With.intelligence.fingerprints.cannonRush,
    With.intelligence.fingerprints.proxyGateway)
  
  class DoSpeedlingAllIn extends Parallel(
    new Aggression(1.2),
    new BuildOrder(RequestAtLeast(10, Zerg.Zergling)),
    new If(
      new Or(
        new GasAtLeast(100),
        new UpgradeComplete(Zerg.ZerglingSpeed, 1, Zerg.ZerglingSpeed.upgradeFrames(1))),
      new Do(() => { With.blackboard.gasLimitFloor = 0; With.blackboard.gasLimitCeiling = 0 })),
    new FlipIf(
      new SafeAtHome,
      new TrainContinuously(Zerg.Zergling),
      new TrainContinuously(Zerg.Drone, 9)),
    new Build(
      RequestAtLeast(1, Zerg.Extractor),
      RequestUpgrade(Zerg.ZerglingSpeed)),
    new RequireMiningBases(3),
    new If(
      new MineralsAtLeast(400),
      new RequireMiningBases(4)))
  
  class CapGasAt(value: Int) extends Do(() => {
    With.blackboard.gasLimitFloor = value
    With.blackboard.gasLimitCeiling = value
  })
  
  class CapGasAtRatioToMinerals(ratio: Double, margin: Int) extends Do(() => {
    val cap = (With.self.minerals * ratio + margin).toInt
    With.blackboard.gasLimitFloor = cap
    With.blackboard.gasLimitCeiling = cap
  })
  
  class GetGasFor(buildable: Buildable) extends If(
    new GasAtLeast(buildable.gas),
    new CapGasAt(0),
    new CapGasAt(buildable.gas))
  
  class GetGasForUpgrade(upgrade: Upgrade, level: Int = 1) extends If(
    new Or(
      new UpgradeComplete(upgrade, level, upgrade.upgradeFrames(level)),
      new GasAtLeast(upgrade.gasPrice(level))),
    new CapGasAt(0),
    new GetGasFor(BuildableUpgrade(upgrade)))
  
  class OneBaseProtoss extends EnemyStrategy(
    With.intelligence.fingerprints.cannonRush,
    With.intelligence.fingerprints.proxyGateway,
    With.intelligence.fingerprints.twoGate,
    With.intelligence.fingerprints.oneGateCore)
  
  class TwoBaseProtoss extends EnemyStrategy(
    With.intelligence.fingerprints.nexusFirst,
    With.intelligence.fingerprints.forgeFe,
    With.intelligence.fingerprints.gatewayFe)
  
  class ExpandAtDrones(drones: Int, bases: Int) extends If(
    new Or(
      new MineralsAtLeast(400),
      new UnitsAtLeast(drones, UnitMatchWorkers)),
    new RequireBases(bases))
  
  class MacroHatchAtDrones(drones: Int, hatcheries: Int) extends If(
    new Or(
      new MineralsAtLeast(300),
      new UnitsAtLeast(drones, UnitMatchWorkers)),
    new MacroHatchUpTo(hatcheries))
  
  class PlaceMacroHatch extends ProposePlacement {
    override lazy val blueprints = Vector(
      new Blueprint(this, building = Some(Zerg.Hatchery), placement = Some(PlacementProfiles.factory)))
  }
  
  class MacroHatchUpTo(hatcheries: Int) extends If(
    new UnitsExactly(hatcheries - 1, Zerg.HatcheryLairOrHive),
    new Parallel(
      new PlaceMacroHatch,
      new Build(RequestAtLeast(3, Zerg.Hatchery))))
  
  class TakeThirdWorldIslandsAfter(bases: Int) extends If(
    new OnMap(ThirdWorld),
    new IfOnMiningBases(bases,
      new Do(() => With.blackboard.allowIslandBases = true),
      new Do(() => With.blackboard.allowIslandBases = false)))
  
  
  class WatchIslands extends SquadPlan[GoalWatchIslands] { override val goal: GoalWatchIslands = new GoalWatchIslands }
  
  class WatchIslandsOnThirdWorld extends If(new OnMap(ThirdWorld), new WatchIslands)
}
