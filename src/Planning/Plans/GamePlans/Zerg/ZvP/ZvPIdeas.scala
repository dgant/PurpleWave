package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Army.Aggression
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Predicates.SafeAtHome
import Planning.Plans.Predicates.Scenarios.EnemyStrategy
import ProxyBwapi.Races.Zerg

object ZvPIdeas {
  
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
  
  class OneBaseProtoss extends EnemyStrategy(
    With.intelligence.fingerprints.cannonRush,
    With.intelligence.fingerprints.proxyGateway,
    With.intelligence.fingerprints.twoGate,
    With.intelligence.fingerprints.oneGateCore)
  
  class TwoBaseProtoss extends EnemyStrategy(
    With.intelligence.fingerprints.nexusFirst,
    With.intelligence.fingerprints.forgeFe,
    With.intelligence.fingerprints.gatewayFe)
}
