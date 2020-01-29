package Planning.Predicates.Milestones

import Planning.Plans.Compound.Or
import ProxyBwapi.Races.{Protoss, Terran, Zerg}

class EnemyHasShownCloakedThreat extends Or(
  new EnemyHasShownWraithCloak,
  new EnemyHasShown(Terran.SpiderMine),
  new EnemyHasShown(Protoss.Arbiter),
  new EnemyHasShown(Protoss.ArbiterTribunal),
  new EnemyHasShown(Protoss.DarkTemplar),
  new EnemyHasShown(Protoss.TemplarArchives),
  new EnemyHasShown(Zerg.LurkerEgg),
  new EnemyHasShown(Zerg.Lurker))