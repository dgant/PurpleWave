package Planning.Plans.GamePlans.Protoss.Standard.FFA

import Macro.BuildRequests.Get
import Planning.Plans.Basic.NoPlan
import Planning.{Plan, Predicate}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Predicates.Always
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases, RequireMiningBasesFFA}
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import Planning.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Protoss

class ProtossHuntersFFAGatewayAggro extends GameplanTemplate {
  
  override val activationCriteria   : Predicate = new Always
  override val scoutPlan     : Plan = NoPlan()
  
  override val buildOrder = ProtossBuilds.NZ12Gas14Core
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(new UnitsAtLeast(1, Protoss.Dragoon),      new Build(Get(Protoss.DragoonRange))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar),  new Build(Get(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2, Protoss.HighTemplar),  new Build(Get(Protoss.HighTemplarEnergy))),
    new If(new UnitsAtLeast(2, Protoss.Reaver),       new Build(Get(Protoss.ScarabDamage))),
    new If(new UnitsAtLeast(1, Protoss.Shuttle),      new Build(Get(Protoss.ShuttleSpeed))),
    new If(new UnitsAtLeast(2, Protoss.Observatory),  new Build(Get(Protoss.ObserverSpeed))),
    new If(new UnitsAtLeast(4, Protoss.Zealot),       new Build(Get(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(2, Protoss.Arbiter),      new Build(Get(Protoss.Stasis))),
    new Pump(Protoss.Arbiter,      2),
    new Pump(Protoss.Observer,     2),
    new Pump(Protoss.Reaver,       2),
    new Pump(Protoss.HighTemplar,  8,  2),
    new Pump(Protoss.DarkTemplar,  2,  1),
    new Pump(Protoss.Shuttle,      1),
    new Pump(Protoss.Dragoon,      20, 6),
    new Pump(Protoss.Zealot),
    new Build(
      Get(3, Protoss.Gateway)),
    new BuildGasPumps,
    new Build(
      Get(1, Protoss.RoboticsFacility),
      Get(1, Protoss.Observatory),
      Get(1, Protoss.RoboticsSupportBay)),
    new RequireMiningBases(2),
    new Build(
      Get(5, Protoss.Gateway),
      Get(1, Protoss.CitadelOfAdun),
      Get(1, Protoss.TemplarArchives),
      Get(2, Protoss.Forge)),
    new BuildCannonsAtExpansions(3),
    new RequireMiningBasesFFA(3),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.GroundArmor),
    new Build(
      Get(12, Protoss.Gateway),
      Get(1, Protoss.Stargate),
      Get(1, Protoss.ArbiterTribunal),
      Get(20, Protoss.Gateway)),
    new RequireMiningBasesFFA(4),
    new UpgradeContinuously(Protoss.Shields),
    new RequireMiningBasesFFA(5)
  )
}
