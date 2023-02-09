package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Planning.Predicates.MacroFacts
import Utilities.Time.Minutes

class SquadAttackNew extends Squad {
  override def toString: String = f"$mode ${vicinity.base.map(_.name).getOrElse(vicinity.zone.name).take(4)}"

  override def launch(): Unit = {} // This squad receives its units from Tactician

  trait AttackMode
  object RazeBase     extends AttackMode { override val toString = "Raze"     }
  object RazeProxy    extends AttackMode { override val toString = "Deprox"   }
  object PushMain     extends AttackMode { override val toString = "Push"     }
  object CrushArmy    extends AttackMode { override val toString = "Crush"    }
  object ContainArmy  extends AttackMode { override val toString = "Contain"  }
  object ClearMap     extends AttackMode { override val toString = "Clear"    }

  var mode: AttackMode = PushMain

  def chooseMode(): AttackMode = {
    val proxies       = With.units.enemy.filter(_.proxied).toVector
    val faster        = MacroFacts.safeSkirmishing && ((With.self.isZerg && ! With.enemies.exists(_.isZerg)) || (With.self.isProtoss && With.enemies.forall(_.isTerran)))
    val basesOccupied = units.view.flatMap(_.base).filter(_.isEnemy).toSet

    if (basesOccupied.nonEmpty) {
      mode      = RazeBase
      vicinity  = basesOccupied.map(_.heart.center).minBy(attackKeyDistanceTo)
      setTargets(SquadAutomation.rankedAround(this))

    } else if (proxies.nonEmpty) {
      mode      = RazeProxy
      vicinity  = proxies.map(_.pixel).minBy(attackKeyDistanceTo)
      setTargets(proxies ++ SquadAutomation.rankedAround(this))

    } else if (MacroFacts.killPotential) {
      mode      = PushMain
      vicinity  = With.scouting.enemyHome.center
      setTargets(SquadAutomation.rankedEnRoute(this))

    } else if (MacroFacts.safePushing && With.scouting.enemyProximity > 0.5) {
      mode      = CrushArmy
      vicinity  = With.scouting.enemyMuscleOrigin.center
      setTargets(SquadAutomation.rankedEnRoute(this))

    } else if (MacroFacts.safePushing) {
      mode      = ContainArmy
      vicinity  = With.scouting.enemyThreatOrigin.center
      setTargets(SquadAutomation.rankedEnRoute(this))

    } else {
      mode      = ClearMap
      vicinity  =
        Maff.orElse(
          Maff.orElse(
            With.geography.enemyBases.filterNot(b => With.scouting.enemyMain.exists(_.metro.bases.contains(b))),
            With.geography.enemyBases.filterNot(b => With.scouting.enemyMain.contains(b) || With.scouting.enemyNatural.contains(b)))
          .toSeq
          .sortBy(b => keyDistanceTo(b.heart.center) - With.scouting.enemyThreatOrigin.groundPixels(b.heart)),
        With.geography.preferredExpansionsEnemy.filter(_.lastFrameScoutedByUs < With.frame - Minutes(1)()),
        With.geography.preferredExpansionsEnemy)
          .headOption
          .map(_.heart.center)
          .getOrElse(With.scouting.enemyThreatOrigin.center)
      setTargets(SquadAutomation.rankedAround(this))
    }

    mode
  }


  override def run(): Unit = {
    chooseMode()
    if (Seq(ContainArmy, ClearMap).contains(mode)) {
      // TODO: Scour stray units, like SquadDefendBase does
    }
    SquadAutomation.formAndSend(this)
    if (Seq(RazeBase, ClearMap).contains(mode) && ! MacroFacts.safePushing) {
      units.foreach(_.intent.setCanSneak(true))
    }
  }
}
