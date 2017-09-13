package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Army.{ConsiderAttacking, DefendZones}
import Planning.Plans.Compound.{FlipIf, If, Parallel, Trigger}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Employing
import Planning.Plans.Information.Reactive.EnemyBio
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{EnemyHasTech, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.GamePlans.Standard.PvT.PvTIdeas.Require2BaseTech
import Planning.Plans.Scouting.ScoutExpansionsAt
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT.PvT2BaseCarrier

class PvT2BaseCarriers extends Mode {
  
  description.set("PvT 2 Base Carriers")
  
  override val activationCriteria: Plan = new Employing(PvT2BaseCarrier)
  override val completionCriteria: Plan = new UnitsAtLeast(2, Protoss.Nexus, complete = true)
  
  children.set(Vector(
    new Require2BaseTech,
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new FlipIf(
      new UnitsAtLeast(12, Protoss.Dragoon),
      new TrainContinuously(Protoss.Dragoon),
      new TrainContinuously(Protoss.Carrier)),
    new If(
      new UnitsAtLeast(1, Protoss.Carrier, complete = true),
      new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    new BuildOrder(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Stargate)),
    new If(
      new EnemyHasTech(Terran.WraithCloak),
      new Parallel(
        new Build(
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory)),
        new TrainContinuously(Protoss.Observer, 3))),
    new If(
      new EnemyBio,
      new UpgradeContinuously(Protoss.AirArmor),
      new UpgradeContinuously(Protoss.AirDamage)),
    new Build(
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestAtLeast(2, Protoss.Stargate),
      RequestAtLeast(4, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Forge)),
    new BuildCannonsAtExpansions(3),
    new RequireMiningBases(3),
    new Build(RequestAtLeast(4, Protoss.Stargate)),
    new RequireMiningBases(4),
    new UpgradeContinuously(Protoss.AirArmor),
    new UpgradeContinuously(Protoss.AirDamage),
    new ScoutExpansionsAt(100),
    new Trigger(
      new UnitsAtLeast(24, Protoss.Interceptor),
      initialBefore = new DefendZones,
      initialAfter = new Parallel(
        new ConsiderAttacking { attack.attackers.get.unitMatcher.set(Protoss.Carrier) },
        new DefendZones,
        new ConsiderAttacking))
  ))
}

