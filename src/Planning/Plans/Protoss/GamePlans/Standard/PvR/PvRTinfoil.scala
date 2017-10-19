package Planning.Plans.Protoss.GamePlans.Standard.PvR

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, RequestAtLeast}
import Planning.Plans.Compound.{And, Check, If}
import Planning.Plans.GamePlans.GameplanModeTemplateVsRandom
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvR.PvROpenTinfoil

class PvRTinfoil extends GameplanModeTemplateVsRandom {
  
  override val activationCriteria = new Employing(PvROpenTinfoil)
  override val completionCriteria = new UnitsAtLeast(1, Protoss.CyberneticsCore)
  
  override val aggression = 0.7
  
  override def buildOrder: Seq[BuildRequest] = Vector(
    RequestAtLeast(8, Protoss.Probe),
    RequestAtLeast(1, Protoss.Pylon),
    RequestAtLeast(9, Protoss.Probe),
    RequestAtLeast(1, Protoss.Forge),
    RequestAtLeast(10, Protoss.Probe),
    RequestAtLeast(2, Protoss.PhotonCannon))
  
  override def buildPlans = Vector(
    new If(
      new And(
        new Check(() => With.self.gas >= 50),
        new UnitsAtLeast(1, Protoss.CyberneticsCore),
        new UnitsAtLeast(1, Protoss.Assimilator)),
      new TrainContinuously(Protoss.Dragoon),
      new TrainContinuously(Protoss.Zealot, 4)),
    new Build(
      RequestAtLeast(1, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Assimilator),
      RequestAtLeast(1, Protoss.CyberneticsCore)))
}
