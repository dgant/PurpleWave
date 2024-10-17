package Planning.Plans.Gameplans.Protoss.FFA

import Planning.Predicates.{Never, Predicate}

class ProtossFFAMoney extends ProtossFFA {
  override def doExpand: Predicate = new Never
}
