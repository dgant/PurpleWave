package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Compound.{If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones.{EnemyHasShown, UnitsAtLeast, UnitsAtMost, UpgradeComplete}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT2GateRangeExpandCarrier

class PvT2GateExpandCarrier extends GameplanTemplate {

  override val activationCriteria = new Employing(PvT2GateRangeExpandCarrier)

  override def scoutPlan: Plan = new ScoutOn(Protoss.CyberneticsCore)
  override def attackPlan: Plan = new Parallel(
    new PvTIdeas.AttackWithCarrierFleet,
    new If(
      new Or(
        new Not(new EnemyHasShown(Terran.SpiderMine)),
        new Not(new EnemyStrategy(With.fingerprints.twoFac)),
        new Latch(new UnitsAtLeast(4, Protoss.Carrier, complete = true))),
      super.attackPlan)
  )

  override def buildOrder: Seq[BuildRequest] = ProtossBuilds.PvT2GateRangeExpand

  override def buildPlans = Vector(
    new RequireMiningBases(2),
    new If(new UnitsAtLeast(4, Protoss.Carrier), new Build(Get(Protoss.CarrierCapacity))),
    new If(new UnitsAtLeast(4, Protoss.Carrier), new RequireMiningBases(3)),
    new If(new UnitsAtLeast(4, Protoss.Carrier), new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.ZealotSpeed))),
    new BuildGasPumps,
    new Pump(Protoss.Carrier),
    new BuildCannonsAtExpansions(2),
    new If(
      new UnitsAtLeast(2, Protoss.Stargate),
      new If(
        new UpgradeComplete(Protoss.AirDamage, 3),
        new UpgradeContinuously(Protoss.AirArmor),
        new UpgradeContinuously(Protoss.AirDamage))),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Pump(Protoss.Observer, 2),
    new Pump(Protoss.Dragoon),
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.twoFac),
        new UnitsAtMost(0, Protoss.FleetBeacon)),
      new Build(
        Get(Protoss.RoboticsFacility),
        Get(5, Protoss.Gateway),
        Get(Protoss.Observatory))),
    new Pump(Protoss.Zealot),
    new Build(
      Get(Protoss.Stargate),
      Get(Protoss.FleetBeacon),
      Get(2, Protoss.Stargate)),
    new Build(Get(4, Protoss.Gateway)),
    new RequireMiningBases(3),
    new Build(Get(Protoss.TemplarArchives)),
    new Build(Get(3, Protoss.Stargate)),
    new Build(Get(6, Protoss.Gateway)),
    new Build(Get(4, Protoss.Stargate)),
    new RequireMiningBases(4),
    new Build(Get(10, Protoss.Gateway)),
  )
}
