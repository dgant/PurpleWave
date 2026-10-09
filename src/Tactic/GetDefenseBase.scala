package Tactic

import Information.Geography.Types.Base
import Lifecycle.With

object GetDefenseBase {
  def canSecure(bases: Iterable[Base]): Boolean =
    With.blackboard.takeUnownedBases() || bases.exists(b => b.isOurs || b.plannedExpoRecently)

  def apply(base: Base): Base = {
    base.natural.filter(b => b.isOurs || b.plannedExpoRecently).getOrElse(base)
  }
}
