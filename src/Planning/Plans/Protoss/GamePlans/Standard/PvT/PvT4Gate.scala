package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.RequestAtLeast
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{FlipIf, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvTEarly4Gate

class PvT4Gate extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvTEarly4Gate)
  override val completionCriteria = new MiningBasesAtLeast(2)
  override val aggression         = 1.2
  override val defaultAttackPlan  = new Attack
  override val buildOrder         = ProtossBuilds.Opening_10Gate12Gas14Core
  
  override val buildPlans = Vector(
    new UpgradeContinuously(Protoss.DragoonRange),
    new FlipIf(
      new UnitsAtLeast(15, Protoss.Dragoon),
      new Parallel(
        new BuildOrder(
          RequestAtLeast(3, Protoss.Dragoon),
          RequestAtLeast(4, Protoss.Gateway)),
        new TrainContinuously(Protoss.Dragoon)),
      new RequireMiningBases(2)
    ))
}

