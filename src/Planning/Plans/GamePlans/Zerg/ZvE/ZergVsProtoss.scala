package Planning.Plans.GamePlans.Zerg.ZvE

import Lifecycle.With
import Macro.BuildRequests.{RequestAnother, RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchOr
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{MatchingRatio, TrainContinuously, TrainMatchingRatio, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Macro.Zerg.BuildSunkensAtNatural
import Planning.Plans.Predicates.Milestones.{EnemyUnitsAtLeast, UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Plans.Predicates.SafeAtHome
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import ProxyBwapi.Races.{Protoss, Zerg}

class ZergVsProtoss extends GameplanModeTemplate {
  
  override val buildOrder = Vector(
    RequestAtLeast(9, Zerg.Drone),
    RequestAtLeast(2, Zerg.Overlord),
    RequestAtLeast(1, Zerg.SpawningPool),
    RequestAtLeast(12, Zerg.Drone),
    RequestAtLeast(2, Zerg.Hatchery),
    RequestAtLeast(6, Zerg.Zergling)
  )
  
  class ReactiveSunkensVsRush extends If(
    new EnemyStrategy(
      With.intelligence.fingerprints.proxyGateway,
      With.intelligence.fingerprints.twoGate),
    new Parallel(
      new TrainContinuously(Zerg.SunkenColony),
      new BuildSunkensAtNatural(2)))
  
  class ReactiveZerglingsVsZealots extends If(
    new And(
      new UnitsAtMost(0, Zerg.Spire),
      new UnitsAtMost(0, Zerg.HydraliskDen, complete = true)),
    new TrainMatchingRatio(Zerg.Zergling, 0, 30, Seq(
      MatchingRatio(Protoss.Zealot, 5.0),
      MatchingRatio(Protoss.Gateway, 2.0)
    )))
  
  class DoSpeedlingAllIn extends Parallel(
    new FlipIf(
      new SafeAtHome,
      new TrainContinuously(Zerg.Zergling),
      new TrainContinuously(Zerg.Drone, 9)),
    new Build(
      RequestAtLeast(1, Zerg.Extractor),
      RequestUpgrade(Zerg.ZerglingSpeed)),
    new RequireMiningBases(3))
  
  class ReactToProxyGates extends If(
    new EnemyStrategy(With.intelligence.fingerprints.nexusFirst),
    new DoSpeedlingAllIn)
    
  class ReactToNexusFirst extends If(
    new EnemyStrategy(With.intelligence.fingerprints.nexusFirst),
    new DoSpeedlingAllIn)
  
  class ReactToCannonRush extends If(
    new EnemyStrategy(With.intelligence.fingerprints.cannonRush),
    new DoSpeedlingAllIn)
  
  class DefendAgainstOneBase extends Parallel(
    new TrainContinuously(Zerg.Drone, 12),
    new If(
      new Not(new SafeAtHome),
      new BuildSunkensAtNatural(6)),
    new Build(RequestAtLeast(1, Zerg.Extractor)),
    new TrainContinuously(Zerg.Drone, 16),
    new UpgradeContinuously(Zerg.ZerglingSpeed),
    new Build(
      RequestAtLeast(1, Zerg.Lair),
      RequestAtLeast(1, Zerg.HydraliskDen),
      RequestTech(Zerg.LurkerMorph)))
    
  override def buildPlans: Seq[Plan] = Vector(
    new ReactToProxyGates,
    new ReactToCannonRush,
    new ReactToNexusFirst,
    new ReactiveSunkensVsRush,
    new ReactiveZerglingsVsZealots,
    new ReactToNexusFirst,
    new Trigger(
      new Or(
        new EnemyStrategy(
          With.intelligence.fingerprints.proxyGateway,
          With.intelligence.fingerprints.cannonRush,
          With.intelligence.fingerprints.twoGate,
          With.intelligence.fingerprints.oneGateCore,
          With.intelligence.fingerprints.nexusFirst)),
      new DefendAgainstOneBase),
    new Trigger(
      new Or(
        new EnemyStrategy(
          With.intelligence.fingerprints.proxyGateway,
          With.intelligence.fingerprints.cannonRush,
          With.intelligence.fingerprints.twoGate,
          With.intelligence.fingerprints.oneGateCore,
          With.intelligence.fingerprints.gatewayFe,
          With.intelligence.fingerprints.forgeFe,
          With.intelligence.fingerprints.nexusFirst),
        new EnemyBasesAtLeast(2),
        new EnemyUnitsAtLeast(1, UnitMatchOr(Protoss.Gateway, Protoss.Forge, Protoss.PhotonCannon))),
      new Parallel(postDiscovery: _*),
      new If(
        new UnitsAtLeast(3, Zerg.Larva),
        new Build(RequestAnother(1, Zerg.Drone))))
  )
    
  def postDiscovery = Vector(
    new TrainContinuously(Zerg.Drone, 11),
    new RequireMiningBases(2),
    new TrainContinuously(Zerg.Drone, 13),
    new TrainContinuously(Zerg.Lurker),
    new If(
      new UnitsAtMost(5, UnitMatchOr(Zerg.Lurker, Zerg.LurkerEgg, Zerg.Hydralisk)),
      new TrainContinuously(Zerg.Hydralisk, 5)),
    new UpgradeContinuously(Zerg.GroundArmor),
    new UpgradeContinuously(Zerg.AirArmor),
    new TrainContinuously(Zerg.Mutalisk),
    new RequireMiningBases(3),
    new TrainWorkersContinuously,
    new Build(
      RequestAtLeast(1, Zerg.Extractor),
      RequestUpgrade(Zerg.ZerglingSpeed),
      RequestAtLeast(1, Zerg.Lair),
      RequestAtLeast(2, Zerg.Extractor),
      RequestAtLeast(1, Zerg.Spire)),
    new BuildGasPumps,
    new RequireMiningBases(5)
  )
}
