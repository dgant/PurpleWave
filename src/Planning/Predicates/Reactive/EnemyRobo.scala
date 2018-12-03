package Planning.Predicates.Reactive

import Planning.Plans.Compound.Or
import Planning.Predicates.Milestones.EnemyHasShown
import ProxyBwapi.Races.Protoss

class EnemyRobo extends Or(
  new EnemyHasShown(Protoss.RoboticsFacility),
  new EnemyHasShown(Protoss.RoboticsSupportBay),
  new EnemyHasShown(Protoss.Observatory),
  new EnemyHasShown(Protoss.Shuttle),
  new EnemyHasShown(Protoss.Reaver),
  new EnemyHasShown(Protoss.Observer))