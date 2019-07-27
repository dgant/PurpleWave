package Planning.Plans.GamePlans.Zerg.ZvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.{Aggression, Attack, Hunt}
import Planning.Plans.Compound.{If, _}
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.EnemyStrategy
import ProxyBwapi.Races.{Protoss, Zerg}

object ZvPIdeas {
  
  class ShouldDoSpeedlingAllIn extends EnemyStrategy(
    With.fingerprints.cannonRush,
    With.fingerprints.proxyGateway)
  
  class DoSpeedlingAllIn extends Parallel(
    new Aggression(1.2),
    new BuildOrder(Get(10, Zerg.Zergling)),
    new If(
      new Or(
        new GasAtLeast(100),
        new UpgradeComplete(Zerg.ZerglingSpeed, 1, Zerg.ZerglingSpeed.upgradeFrames(1))),
      new CapGasAt(0)),
    new FlipIf(
      new SafeAtHome,
      new Pump(Zerg.Zergling),
      new Pump(Zerg.Drone, 9)),
    new Build(
      Get(1, Zerg.Extractor),
      Get(Zerg.ZerglingSpeed)),
    new RequireMiningBases(3),
    new If(
      new MineralsAtLeast(400),
      new RequireMiningBases(4)))
  
  class OneBaseProtoss extends EnemyStrategy(
    With.fingerprints.cannonRush,
    With.fingerprints.proxyGateway,
    With.fingerprints.twoGate,
    With.fingerprints.oneGateCore)
  
  class TwoBaseProtoss extends EnemyStrategy(
    With.fingerprints.nexusFirst,
    With.fingerprints.forgeFe,
    With.fingerprints.gatewayFe)
  
  class OverpoolSpendLarva extends Latch(new Or(new OverpoolSpendLarvaOnDrones, new OverpoolSpendLarvaOnZerglings))
  
  class OverpoolSpendLarvaOnDrones extends EnemyStrategy(With.fingerprints.forgeFe, With.fingerprints.nexusFirst)
  
  class OverpoolSpendLarvaOnZerglings extends Or(
    new EnemyStrategy(
      With.fingerprints.cannonRush,
      With.fingerprints.proxyGateway,
      With.fingerprints.twoGate,
      With.fingerprints.gatewayFirst,
      With.fingerprints.oneGateCore,
      With.fingerprints.gatewayFe))
  
  class OverpoolBuildLarvaOrDrones extends Trigger(
    new OverpoolSpendLarva,
    new Trigger(
      new OverpoolSpendLarvaOnZerglings,
      new BuildOrder(Get(12, Zerg.Zergling)),
      new BuildOrder(Get(14, Zerg.Drone))))

  class OverlordSpeedVsCloakedThreats extends If(
    new Or(
      new EnemyHasShown(Protoss.DarkTemplar),
      new EnemyHasShown(Protoss.HighTemplar),
      new EnemyHasShown(Protoss.Archon),
      new EnemyHasShown(Protoss.TemplarArchives),
      new EnemyHasShown(Protoss.Arbiter),
      new EnemyHasShown(Protoss.ArbiterTribunal)),
    new Build(
      Get(Zerg.Extractor),
      Get(Zerg.SpawningPool),
      Get(Zerg.Lair),
      Get(Zerg.OverlordSpeed)))

  class BurrowVsReaver extends If(
    new Or(new EnemyHasShown(Protoss.Shuttle), new EnemyHasShown(Protoss.Reaver)),
    new Build(Get(Zerg.Burrow)))

  class NeedAntiAir extends Or(
    new EnemyHasShown(Protoss.Corsair),
    new EnemyHasShown(Protoss.Scout),
    new EnemyHasShown(Protoss.Carrier),
    new EnemyHasShown(Protoss.Interceptor),
    new EnemyHasShown(Protoss.Stargate))

  class TechToSpireVsAir extends If(
    new NeedAntiAir,
    new Build(
      Get(Zerg.Extractor),
      Get(Zerg.SpawningPool),
      Get(Zerg.Lair),
      Get(Zerg.Spire)))

  class TechToHydrasVsAir extends If(
    new NeedAntiAir,
    new Build(
      Get(Zerg.Extractor),
      Get(Zerg.SpawningPool),
      Get(Zerg.HydraliskDen)))

  // New stuff

  class AttackPlans extends Parallel(
    new Hunt(Zerg.Scourge, Protoss.Shuttle),
    new Hunt(Zerg.Scourge, Protoss.Corsair),
    new Hunt(Zerg.Mutalisk, Protoss.HighTemplar),
    new Attack
  )

}
