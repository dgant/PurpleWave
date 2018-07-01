package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Predicates.Compound.{And, Latch}
import Planning.Plan
import Planning.Plans.Army.ConsiderAttacking
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplateVsRandom
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.Automatic.{CapGasAt, Pump, PumpWorkers, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones.{IfOnMiningBases, UnitsAtLeast, UnitsAtMost}
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.{UnitMatchWarriors, UnitMatchWorkers}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR.PvROpenTinfoil

class PvRTinfoil extends GameplanModeTemplateVsRandom {
  
  override val activationCriteria = new Employing(PvROpenTinfoil)
  override def defaultScoutPlan   = new ScoutOn(Protoss.Gateway, quantity = 2)

  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(8, Protoss.Probe),
    Get(1, Protoss.Pylon),
    Get(9, Protoss.Probe),
    Get(1, Protoss.Gateway),
    Get(10, Protoss.Probe),
    Get(2, Protoss.Gateway))

  override def defaultWorkerPlan: Plan = NoPlan()
  
  override def buildPlans = Vector(
    new CapGasAt(200),
    new Pump(Protoss.Observer, 1),
    new Pump(Protoss.Reaver, 3),
    new If(
      new UnitsAtLeast(3, Protoss.Reaver),
      new RequireMiningBases(2)),
    new FlipIf(
      new And(
        new UnitsAtLeast(9, UnitMatchWorkers),
        new UnitsAtMost(8, UnitMatchWarriors)),
      new PumpWorkers,
      new If(
        new And(
          new GasAtLeast(50),
          new UnitsAtLeast(1, Protoss.CyberneticsCore),
          new UnitsAtLeast(1, Protoss.Assimilator)),
        new Pump(Protoss.Dragoon),
        new Pump(Protoss.Zealot))),
    new UpgradeContinuously(Protoss.DragoonRange),
    new Build(
      Get(2, Protoss.Gateway),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore),
      Get(1, Protoss.RoboticsFacility),
      Get(1, Protoss.Observatory),
      Get(1, Protoss.RoboticsSupportBay),
      Get(2, Protoss.Nexus)),
    new IfOnMiningBases(2,
      new Parallel(
        new Build(Get(6, Protoss.Gateway)),
        new BuildGasPumps,
        new RequireMiningBases(3),
        new Build(Get(12, Protoss.Gateway))))
  )
}
