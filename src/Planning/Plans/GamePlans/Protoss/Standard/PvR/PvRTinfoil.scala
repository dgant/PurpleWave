package Planning.Plans.GamePlans.Protoss.Standard.PvR

import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Compound.{And, If}
import Planning.Plans.GamePlans.GameplanModeTemplateVsRandom
import Planning.Plans.GamePlans.Protoss.Situational.PlacementForgeFastExpand
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Predicates.Economy.GasAtLeast
import Planning.Plans.Predicates.Employing
import Planning.Plans.Predicates.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR.PvROpenTinfoil

class PvRTinfoil extends GameplanModeTemplateVsRandom {
  
  override val activationCriteria = new Employing(PvROpenTinfoil)
  override val completionCriteria = new UnitsAtLeast(1, Protoss.CyberneticsCore)
  
  override val aggression = 0.7
  
  override def defaultPlacementPlan: Plan = new PlacementForgeFastExpand
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    Get(8, Protoss.Probe),
    Get(1, Protoss.Pylon),
    Get(9, Protoss.Probe),
    Get(1, Protoss.Forge),
    Get(10, Protoss.Probe),
    Get(2, Protoss.PhotonCannon))
  
  override def buildPlans = Vector(
    new If(
      new And(
        new GasAtLeast(50),
        new UnitsAtLeast(1, Protoss.CyberneticsCore),
        new UnitsAtLeast(1, Protoss.Assimilator)),
      new TrainContinuously(Protoss.Dragoon),
      new TrainContinuously(Protoss.Zealot, 4)),
    new Build(
      Get(1, Protoss.Gateway),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore)))
}
