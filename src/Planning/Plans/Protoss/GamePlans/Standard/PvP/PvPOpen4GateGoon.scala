package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Macro.BuildRequests.{BuildRequest, RequestAtLeast, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.RequireSufficientSupply
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen4GateGoon

class PvPOpen4GateGoon extends GameplanModeTemplate {
  
  override val activationCriteria : Plan = new Employing(PvPOpen4GateGoon)
  override val completionCriteria : Plan = new MiningBasesAtLeast(2)
  override def defaultAttackPlan  : Plan = new PvPIdeas.AttackSafely
  override val defaultWorkerPlan  : Plan = NoPlan()
  
  override val buildOrder: Seq[BuildRequest] = Vector(
    // http://wiki.teamliquid.net/starcraft/4_Gate_Goon_(vs._Protoss)
    RequestAtLeast(8,   Protoss.Probe),
    RequestAtLeast(1,   Protoss.Pylon),             // 8
    RequestAtLeast(10,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Gateway),           // 10
    RequestAtLeast(12,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Pylon),             // 12
    RequestAtLeast(13,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Zealot),            // 13
    RequestAtLeast(14,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Assimilator),       // 16
    RequestAtLeast(15,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.CyberneticsCore),   // 17
    RequestAtLeast(16,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Zealot),            // 18
    RequestAtLeast(18,  Protoss.Probe),
    RequestAtLeast(3,   Protoss.Pylon),             // 22
    RequestAtLeast(19,  Protoss.Probe),
    RequestAtLeast(1,   Protoss.Dragoon),           // 23
    RequestAtLeast(20,  Protoss.Probe),
    RequestUpgrade(Protoss.DragoonRange),           // 26
    RequestAtLeast(21,  Protoss.Probe),
    RequestAtLeast(2,   Protoss.Dragoon),           // 27
    //RequestAtLeast(23,  Protoss.Probe), // Skipping due to mineral locking
    RequestAtLeast(4,   Protoss.Gateway),           // 31
    RequestAtLeast(3,   Protoss.Dragoon),           // 31
    RequestAtLeast(4,   Protoss.Pylon),             // 33
    RequestAtLeast(7,   Protoss.Dragoon),           // 33
    RequestAtLeast(5,   Protoss.Pylon),             // 41
    RequestAtLeast(11,  Protoss.Dragoon)            // 41
  )
  
  override val buildPlans = Vector(
    new RequireSufficientSupply,
    new FlipIf(
      new UnitsAtLeast(20, Protoss.Dragoon),
      new RequireMiningBases(2)))
}
