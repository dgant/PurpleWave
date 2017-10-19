package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtNatural, BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen12Nexus5Zealot

class PvPOpen12Nexus5Zealot extends GameplanModeTemplate {
  
  class PylonAtNatural extends ProposePlacement {
    override lazy val blueprints: Seq[Blueprint] = {
      val output = Vector(
        new Blueprint(this, building = Some(Protoss.Pylon)),
        new Blueprint(this, building = Some(Protoss.Pylon), requireZone = Some(With.geography.ourNatural.zone), placement = Some(PlacementProfiles.wallPylon), marginPixels = Some(marginPixels - 96.0)))
      output
    }
  }
  
  override val activationCriteria : Plan = new Employing(PvPOpen12Nexus5Zealot)
  override val completionCriteria : Plan = new UnitsAtLeast(3, Protoss.PhotonCannon, complete = true)
  
  override def defaultSupplyPlan: Plan = NoPlan()
  override def defaultWorkerPlan: Plan = NoPlan()
  
  override def defaultPlacementPlan: Plan = new PylonAtNatural
  
  override val buildOrder: Seq[BuildRequest] = Vector(
    // http://wiki.teamliquid.net/starcraft/Fast_Expand_(vs._Protoss)
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),             // 8
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Nexus),             // 12
    RequestAtLeast(1,   Protoss.Gateway),           // 12
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Gateway),           // 14
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),             // 15
    RequestAtLeast(2,   Protoss.Zealot),
    RequestAtLeast(1,   Protoss.Forge),             // 19
    RequestAtLeast(4,   Protoss.Zealot),            // 19
    RequestAtLeast(16,  Protoss.Probe),             // 20
    RequestAtLeast(5,   Protoss.Zealot)             // 21
  )
  
  override val buildPlans = Vector(
    new BuildCannonsAtNatural(3),
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new If(
      new UnitsAtLeast(5, UnitMatchWarriors),
      new Parallel(
        new Build(RequestAtLeast(1, Protoss.CyberneticsCore)),
        new BuildGasPumps),
      new TrainContinuously(Protoss.Zealot, 5))
  )
}
