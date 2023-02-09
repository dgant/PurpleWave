package Tactic.Squads

import Lifecycle.With
import Planning.Predicates.MacroFacts

class SquadAttackNew extends Squad {
  override def toString: String = f"Atk ${vicinity.base.map(_.name).getOrElse(vicinity.zone.name).take(4)}"

  override def launch(): Unit = {} // This squad receives its units from Tactician

  trait AttackMode
  object CrushArmy    extends AttackMode
  object ContainArmy  extends AttackMode
  object PushMain     extends AttackMode
  object ClearMap     extends AttackMode

  var mode: AttackMode = PushMain

  def chooseMode(): AttackMode = {
    val faster    = (With.self.isZerg && ! With.enemies.exists(_.isZerg)) || (With.self.isProtoss && With.enemies.forall(_.isTerran))
    val safe      = MacroFacts.confidenceAway11 >= 0
    val stronger  = MacroFacts.confidenceAway11 >= 0.25


    mode
  }


  override def run(): Unit = {


  }
}
