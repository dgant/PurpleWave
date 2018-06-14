package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.GetAtLeast
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.BuildCannonsAtNatural
import Planning.Plans.Predicates.Milestones.{EnemyUnitsAtMost, MiningBasesAtLeast, UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Scouting.ScoutOn
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen2GateDTExpand

class PvP2GateDarkTemplar extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvPOpen2GateDTExpand)
  override val completionCriteria = new MiningBasesAtLeast(2)
  override val defaultWorkerPlan  = NoPlan()
  override val defaultScoutPlan   = new ScoutOn(Protoss.Gateway)
  override val defaultAttackPlan  = new Trigger(new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true), initialAfter = new Attack)
  override def blueprints = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon),   placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(this, building = Some(Protoss.Gateway), placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon), requireZone = Some(With.geography.ourNatural.zone)))
  
  override val buildOrder = Vector(
    // http://wiki.teamliquid.net/starcraft/2_Gateway_Dark_Templar_(vs._Protoss)
    // We get gas/core faster because of mineral locking + later scout
    GetAtLeast(8,   Protoss.Probe),
    GetAtLeast(1,   Protoss.Pylon),             // 8
    GetAtLeast(10,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Gateway),           // 10
    GetAtLeast(11,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Assimilator),       // 11
    GetAtLeast(13,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Zealot),            // 13
    GetAtLeast(14,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Pylon),             // 16 = 14 + Z
    GetAtLeast(16,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CyberneticsCore),   // 18 = 16 + Z
    GetAtLeast(17,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Zealot),            // 19 = 17 + Z
    GetAtLeast(18,  Protoss.Probe),
    GetAtLeast(3,   Protoss.Pylon),             // 22 = 18 + ZZ
    GetAtLeast(19,  Protoss.Probe),
    GetAtLeast(1,   Protoss.Dragoon),           // 23 = 19 + ZZ
    GetAtLeast(20,  Protoss.Probe),
    GetAtLeast(1,   Protoss.CitadelOfAdun),     // 26 = 20 + ZZ + D
    GetAtLeast(21,  Protoss.Probe),
    GetAtLeast(2,   Protoss.Dragoon),           // 27 = 21 + ZZ + D
    GetAtLeast(2,   Protoss.Gateway),           // 29 = 21 + ZZ + DD
    GetAtLeast(3,   Protoss.Pylon),
    GetAtLeast(1,   Protoss.TemplarArchives),
    GetAtLeast(4,   Protoss.Zealot),            // 33 = 21 + ZZZZ + DD
    GetAtLeast(22,  Protoss.Probe),
    GetAtLeast(4,   Protoss.Pylon),             // 34 = 22 + ZZZZ + DD
    GetAtLeast(23,  Protoss.Probe),
    GetAtLeast(2,   Protoss.DarkTemplar),
    GetAtLeast(24,  Protoss.Probe))
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToFFE
  )
  
  override val buildPlans = Vector(
    new RequireSufficientSupply,
    new If(
      new And(
        new EnemyUnitsAtMost(0, Protoss.Observer),
        new EnemyUnitsAtMost(0, Protoss.Observatory),
        new UnitsAtMost(2, Protoss.DarkTemplar)),
        new TrainContinuously(Protoss.DarkTemplar, 3, 1)),
    new TrainContinuously(Protoss.Dragoon),
    new TrainWorkersContinuously,
    new BuildCannonsAtNatural(2),
    new RequireMiningBases(2))
}
