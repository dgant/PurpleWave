package Planning.Plans.GamePlans.Protoss

import Macro.BuildRequests.{BuildRequest, RequestUnitAtLeast, RequestUpgradeLevel}
import Planning.Composition.UnitMatchers.{UnitMatchType, UnitMatchWarriors}
import Planning.Plans.Army.{ConsiderAttacking, ControlEnemyAirspace, DefendChokes, DefendHearts}
import Planning.Plans.Compound.{IfThenElse, _}
import Planning.Plans.Information.{ScoutAt, ScoutExpansionsAt}
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Milestones.{UnitsAtLeast, UnitsAtMost, UnitsExactly}
import Planning.Plans.Macro.Reaction.{EnemyHydralisks, EnemyMassMutalisks, EnemyMassZerglings, EnemyMutalisks}
import ProxyBwapi.Races.Protoss

class ProtossVsZerg extends Parallel {
  
  description.set("Protoss vs Zerg")
  
  private val lateGameBuild = Vector[BuildRequest] (
    RequestUnitAtLeast(4,   Protoss.Gateway),
    RequestUnitAtLeast(1,   Protoss.CitadelOfAdun),
    RequestUpgradeLevel(    Protoss.DragoonRange),
    RequestUpgradeLevel(    Protoss.ZealotSpeed),
    RequestUnitAtLeast(6,   Protoss.Gateway),
    RequestUnitAtLeast(1,   Protoss.TemplarArchives),
    RequestUpgradeLevel(    Protoss.GroundDamage, 2),
    RequestUnitAtLeast(10,  Protoss.Gateway),
    RequestUpgradeLevel(    Protoss.GroundDamage, 3),
    RequestUnitAtLeast(2,   Protoss.Forge),
    RequestUnitAtLeast(7,   Protoss.PhotonCannon),
    RequestUpgradeLevel(    Protoss.GroundDamage, 2),
    RequestUpgradeLevel(    Protoss.GroundArmor,  2),
    RequestUnitAtLeast(8,   Protoss.Gateway),
    RequestUnitAtLeast(10,  Protoss.Gateway),
    RequestUpgradeLevel(    Protoss.GroundDamage, 3),
    RequestUpgradeLevel(    Protoss.GroundArmor, 3)
  )
  
  private val plusOneWeapons = Vector[BuildRequest] (
    RequestUnitAtLeast(1,   Protoss.Forge),
    RequestUpgradeLevel(         Protoss.GroundDamage, 1)
  )
  
  private class WhenSafeTakeNatural extends IfThenElse(
    new UnitsAtLeast(12, UnitMatchWarriors),
    new RequireMiningBases(2)
  )
  
  private class WhenSafeTakeThirdBase extends IfThenElse(
    new And(
      new UnitsAtLeast(16, UnitMatchWarriors),
      new UnitsAtLeast(1, UnitMatchType(Protoss.Corsair)),
      new Or(
        new UnitsAtLeast(1, UnitMatchType(Protoss.Reaver)),
        new UnitsAtLeast(1, UnitMatchType(Protoss.DarkTemplar)))
    ),
    new RequireMiningBases(3)
  )
  
  private class BuildZealotsInitially extends IfThenElse(
    new UnitsExactly(0, UnitMatchType(Protoss.CyberneticsCore)),
    new Build(ProtossBuilds.OpeningTwoGate1012Zealots)
  )
  
  private class RespondToMutalisksWithCorsairs extends IfThenElse(
    new EnemyMutalisks,
    new Parallel(
      new Build(ProtossBuilds.TechCorsairs),
      new TrainContinuously(Protoss.Corsair, 10)
    )
  )
  
  private class RespondToHydrasWithReavers_OrGetCorsairTech extends IfThenElse(
    new EnemyHydralisks,
    new Build(ProtossBuilds.TechReavers),
    new Build(ProtossBuilds.TechCorsairs)
  )
  
  private class BuildZealotsOrDragoons_BasedOnMutalisksAndZerglings extends IfThenElse (
    new Or(
      new EnemyMassMutalisks,
      new And(
        new EnemyMutalisks,
        new UnitsAtLeast(8, UnitMatchType(Protoss.Zealot))
      )
    ),
    new Parallel(
      new Build(RequestUpgradeLevel(Protoss.DragoonRange)),
      new TrainContinuously(Protoss.Dragoon)
    ),
    new IfThenElse(
      new Or(
        new EnemyMassZerglings,
        new UnitsAtMost(8, UnitMatchType(Protoss.Zealot))
      ),
      new TrainContinuously(Protoss.Zealot),
      new TrainGatewayUnitsContinuously
    )
  )
  
  private class AttackWhenWeHaveArmy extends IfThenElse(
    new UnitsAtLeast(10, UnitMatchWarriors),
    new ConsiderAttacking,
    new IfThenElse(
      new UnitsAtLeast(5, UnitMatchWarriors),
      new DefendChokes,
      new DefendHearts)
  )
  
  private class EatOverlordsUntilMutalisksArrive extends IfThenElse(
    new EnemyMutalisks,
    new ScoutExpansionsAt(100),
    new Parallel(
      new ScoutExpansionsAt(40),
      new ControlEnemyAirspace,
      new ConsiderAttacking { attackers.get.unitMatcher.set(UnitMatchType(Protoss.Corsair)) }
    )
  )
  
  private val earlyZealotCount = 8
  
  children.set(Vector(
    new RequireMiningBases(1),
    new Build(ProtossBuilds.OpeningTwoGate1012),
    new WhenSafeTakeNatural,
    new WhenSafeTakeThirdBase,
    new BuildZealotsInitially,
    new RequireSufficientPylons,
    new TrainProbesContinuously,
    new BuildAssimilators,
    new RespondToMutalisksWithCorsairs,
    new TrainContinuously(Protoss.DarkTemplar, 3),
    new TrainContinuously(Protoss.Reaver, 2),
    new TrainContinuously(Protoss.Corsair, 3),
    new Build(plusOneWeapons),
    new RespondToHydrasWithReavers_OrGetCorsairTech,
    new BuildZealotsOrDragoons_BasedOnMutalisksAndZerglings,
    new Build(lateGameBuild),
    new ScoutAt(10),
    new EatOverlordsUntilMutalisksArrive,
    new AttackWhenWeHaveArmy
  ))
}
